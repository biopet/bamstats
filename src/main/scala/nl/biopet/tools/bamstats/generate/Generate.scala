/*
 * Copyright (c) 2014 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.bamstats.generate

import java.io.{File, PrintWriter}

import htsjdk.samtools.{SAMSequenceDictionary, SamReader, SamReaderFactory}
import nl.biopet.tools.bamstats.GroupStats
import nl.biopet.utils.conversions
import nl.biopet.utils.ngs.intervals.BedRecord
import nl.biopet.utils.tool.ToolCommand
import play.api.libs.json.Json
import nl.biopet.utils.ngs.bam._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.collection.JavaConversions._

object Generate extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)
  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val sequenceDict =
      validateReferenceInBam(cmdArgs.bamFile, cmdArgs.referenceFasta)

    init(cmdArgs.outputDir,
         cmdArgs.bamFile,
         sequenceDict,
         cmdArgs.binSize,
         cmdArgs.threadBinSize,
         cmdArgs.tsvOutputs)

    logger.info("Done")
  }

  /**
    * This is the main running function of [[Generate]]. This will start the threads and collect and write the results.
    *
    * @param outputDir All output files will be placed here
    * @param bamFile Input bam file
    * @param referenceDict Dict for scattering
    * @param binSize stats binsize
    * @param threadBinSize Thread binsize
    */
  def init(outputDir: File,
           bamFile: File,
           referenceDict: SAMSequenceDictionary,
           binSize: Int,
           threadBinSize: Int,
           tsvOutput: Boolean): Unit = {
    val contigs = referenceDict.getSequences
      .flatMap(
        r =>
          BedRecord(r.getSequenceName, 0, r.getSequenceLength)
            .scatter(threadBinSize))
    val groups =
      contigs.foldLeft((List[List[BedRecord]](), List[BedRecord](), 0L)) {
        case ((finalList, tempList, oldSize), b) =>
          if (oldSize < threadBinSize)
            (finalList, b :: tempList, oldSize + b.length)
          else (tempList :: finalList, b :: Nil, b.length)
      }
    val contigsFutures =
      (groups._2 :: groups._1).map(x => processThread(x, bamFile))

    val unmappedStats = processUnmappedReads(bamFile)
    val (stats, contigStats) = waitOnFutures(contigsFutures)
    stats += Await.result(unmappedStats, Duration.Inf)

    if (tsvOutput) {
      stats.flagstat.writeAsTsv(
        new File(outputDir, "flagstats.tsv")
      )

      stats.insertSizeHistogram.writeFilesAndPlot(outputDir,
                                                  "insertsize",
                                                  "Insertsize",
                                                  "Reads",
                                                  "Insertsize distribution")

      stats.mappingQualityHistogram.writeFilesAndPlot(
        outputDir,
        "mappingQuality",
        "Mapping Quality",
        "Reads",
        "Mapping Quality distribution")

      stats.clippingHistogram.writeFilesAndPlot(outputDir,
                                                "clipping",
                                                "CLipped bases",
                                                "Reads",
                                                "Clipping distribution")

      stats.leftClippingHistogram.writeFilesAndPlot(
        outputDir,
        "left_clipping",
        "CLipped bases",
        "Reads",
        "Left Clipping distribution")

      stats.rightClippingHistogram.writeFilesAndPlot(
        outputDir,
        "right_clipping",
        "CLipped bases",
        "Reads",
        "Right Clipping distribution")

      stats._3_ClippingHistogram.writeFilesAndPlot(
        outputDir,
        "3prime_clipping",
        "CLipped bases",
        "Reads",
        "3 Prime Clipping distribution")

      stats._5_ClippingHistogram.writeFilesAndPlot(
        outputDir,
        "5prime_clipping",
        "CLipped bases",
        "Reads",
        "5 Prime Clipping distribution")
    }

    val statsWriter = new PrintWriter(new File(outputDir, "bamstats.json"))
    val totalStats = stats.toSummaryMap
    val statsMap = Map(
      "total" -> totalStats,
      "contigs" -> contigStats
    )
    statsWriter.println(Json.stringify(conversions.mapToJson(statsMap)))
    statsWriter.close()

    val summaryWriter = new PrintWriter(
      new File(outputDir, "bamstats.summary.json"))
    summaryWriter.println(Json.stringify(conversions.mapToJson(totalStats)))
    summaryWriter.close()
  }

  /**
    * This method will wait when all futures are complete and collect a single [[GroupStats]] instance
    *
    * @param futures List of futures to monitor
    * @param msg Optional message for logging
    * @return Output stats
    */
  def waitOnFutures(
      futures: List[Future[Map[BedRecord, GroupStats]]],
      msg: Option[String] = None): (GroupStats, Map[String, GroupStats]) = {
    msg.foreach(m =>
      logger.info(s"Start monitoring jobs for '$m', ${futures.size} jobs"))
    futures.foreach(_.onFailure { case t => throw new RuntimeException(t) })
    val totalSize = futures.size
    val totalStats = GroupStats()
    val contigStats: mutable.Map[String, GroupStats] = mutable.Map()

    def wait(todo: List[Future[Map[BedRecord, GroupStats]]]): Unit = {
      try {
        logger.info(s"${totalSize - todo.size}/$totalSize tasks done")
        val completed = todo.groupBy(_.isCompleted)
        completed.getOrElse(true, Nil).foreach { f =>
          Await.result(f, Duration.Inf).foreach {
            case (region, stats) =>
              totalStats += stats
              if (contigStats.contains(region.chr))
                contigStats(region.chr) += stats
              else contigStats(region.chr) = stats
          }
        }
        if (completed.contains(false)) {
          Thread.sleep(10000)
          wait(completed(false))
        }
      } catch {
        case _: TimeoutException =>
          wait(todo)
      }
    }

    wait(futures)

    msg.foreach(m => logger.info(s"All jobs for '$m' are done"))
    (totalStats, contigStats.toMap)
  }

  /**
    * This method will process 1 thread bin
    *
    * @param scatters bins to check, there should be no gaps withing the scatters
    * @param bamFile Input bamfile
    * @return Output stats
    */
  def processThread(scatters: List[BedRecord],
                    bamFile: File): Future[Map[BedRecord, GroupStats]] =
    Future {
      logger.debug(s"Start task on ${scatters.size} regions")
      val samReader: SamReader = SamReaderFactory.makeDefault().open(bamFile)
      val results = scatters.map { bedRecord =>
        bedRecord -> processRegion(bedRecord, samReader)
      }
      samReader.close()

      results.toMap
    }

  def processRegion(bedRecord: BedRecord, samReader: SamReader): GroupStats = {
    //logger.debug(s"Start on $bedRecord")
    val totalStats = GroupStats()
    val it =
      samReader.query(bedRecord.chr, bedRecord.start, bedRecord.end, false)
    for (samRecord <- it) {

      // Read based stats
      if (samRecord.getAlignmentStart > bedRecord.start && samRecord.getAlignmentStart <= bedRecord.end) {
        totalStats.flagstat.loadRecord(samRecord)
        if (!samRecord.getReadUnmappedFlag) { // Mapped read
          totalStats.mappingQualityHistogram.add(samRecord.getMappingQuality)
        }
        if (samRecord.getReadPairedFlag && samRecord.getProperPairFlag && samRecord.getFirstOfPairFlag && !samRecord.getSecondOfPairFlag)
          totalStats.insertSizeHistogram.add(
            samRecord.getInferredInsertSize.abs)

        val leftClipping = samRecord.getAlignmentStart - samRecord.getUnclippedStart
        val rightClipping = samRecord.getUnclippedEnd - samRecord.getAlignmentEnd

        totalStats.clippingHistogram.add(leftClipping + rightClipping)
        totalStats.leftClippingHistogram.add(leftClipping)
        totalStats.rightClippingHistogram.add(rightClipping)

        if (samRecord.getReadNegativeStrandFlag) {
          totalStats._5_ClippingHistogram.add(leftClipping)
          totalStats._3_ClippingHistogram.add(rightClipping)
        } else {
          totalStats._5_ClippingHistogram.add(rightClipping)
          totalStats._3_ClippingHistogram.add(leftClipping)
        }

        //TODO: Bin Support
      }

      //TODO: bases counting
    }
    it.close()
    totalStats
  }

  /**
    * This method will only count the unmapped fragments
    *
    * @param bamFile Input bamfile
    * @return Output stats
    */
  def processUnmappedReads(bamFile: File): Future[GroupStats] = Future {
    val stats = GroupStats()
    val samReader = SamReaderFactory.makeDefault().open(bamFile)
    for (samRecord <- samReader.queryUnmapped()) {
      stats.flagstat.loadRecord(samRecord)
    }
    samReader.close()
    stats
  }

  def tsvToMap(tsvFile: File): Map[String, Array[Long]] = {
    val reader = Source.fromFile(tsvFile)
    val it = reader.getLines()
    val header = it.next().split("\t")
    val arrays =
      header.zipWithIndex.map(x => x._2 -> (x._1 -> ArrayBuffer[Long]()))
    for (line <- it) {
      val values = line.split("\t")
      require(values.size == header.size,
              s"Line does not have the number of field as header: $line")
      for (array <- arrays) {
        array._2._2.append(values(array._1).toLong)
      }
    }
    reader.close()
    arrays.map(x => x._2._1 -> x._2._2.toArray).toMap
  }

  def descriptionText: String =
    s"""
         |$toolName reports clipping stats, flag stats, insert size and mapping quality on a BAM file. It outputs
         |a JSON file, but can optionally also output in TSV format.
     """.stripMargin

  def manualText: String =
    s"""
         |$toolName requires a BAM file and an output directory for its stats.
         |Optionally a reference fasta file can be added against which the BAM file will be validated.
         |There are also fllags to set the binsize of stats, the size of the region per thread, and whether
         |to also output in TSV format.
         |
     """.stripMargin

  def exampleText: String =
    s"""
         |To validate `file.bam`:
         |${example("-b", "file.bam", "-o", "output_dir")}
         |
       |To validate `file.bam` to `reference.fa` and output the result also as TSV, while setting
         |bin size and thread bin size to 200:
         |${example("-R",
                    "reference.fa",
                    "-o",
                    "output_dir",
                    "-b",
                    "file.bam",
                    "--binSize",
                    "200",
                    "--threadBinSize",
                    "200",
                    "--tsvOutputs")}
     """.stripMargin
}
