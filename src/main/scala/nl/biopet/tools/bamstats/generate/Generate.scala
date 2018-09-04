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

import htsjdk.samtools._
import nl.biopet.tools.bamstats.GroupStats
import nl.biopet.tools.bamstats.schema.{BamstatsRoot, GroupID, Stats}
import nl.biopet.utils.conversions
import nl.biopet.utils.ngs.bam._
import nl.biopet.utils.ngs.intervals.{BedRecord, BedRecordList}
import nl.biopet.utils.tool.ToolCommand
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

object Generate extends ToolCommand[Args] {
  def emptyArgs: Args = Args()

  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val sequenceDict: SAMSequenceDictionary =
      cmdArgs.referenceFasta match {
        case Some(reference) =>
          validateReferenceInBam(cmdArgs.bamFile, reference)
        case _ =>
          logger.warn(
            "Reference from BAM file not validated with external reference.")
          getDictFromBam(cmdArgs.bamFile)
      }
    val samReader = SamReaderFactory.makeDefault().open(cmdArgs.bamFile)
    val root: BamstatsRoot = cmdArgs.bedFile match {
      case Some(bed: File) if cmdArgs.onlyUnmapped =>
        throw new IllegalArgumentException(
          "Cannot extract stats from regions and unmapped regions at the same time")
      // When a BED file is specified extract regions stats
      case Some(bed: File) if !cmdArgs.onlyUnmapped =>
        val regions: Iterator[BedRecord] =
          BedRecordList
            .fromFile(bed)
            .combineOverlap
            .validateContigs(sequenceDict)
            .allRecords
            .toIterator
        //Get groupstats for each region
        val regionStats = regions.map { region =>
          extractStatsRegion(samReader, region, cmdArgs.scatterMode)
        }
        regionStats.reduce(_ + _)
      case None if cmdArgs.onlyUnmapped =>
        extractStatsUnmappedReads(samReader)
      case None if !cmdArgs.onlyUnmapped => extractStatsAll(samReader)
    }
    samReader.close()
    val combinedStats: GroupStats = root.combinedStats
    if (cmdArgs.tsvOutputs) {
      writeStatsToTsv(combinedStats, outputDir = cmdArgs.outputDir)
    }

    val statsWriter = new PrintWriter(
      new File(cmdArgs.outputDir, "bamstats.json"))
    statsWriter.println(Json.stringify(groupedStats.toJson))
    statsWriter.close()

    val totalStats = combinedStats.toSummaryMap
    val summaryWriter = new PrintWriter(
      new File(cmdArgs.outputDir, "bamstats.summary.json"))
    summaryWriter.println(Json.stringify(conversions.mapToJson(totalStats)))
    summaryWriter.close()

    logger.info("Done")
  }

  def getGroupIdList(samReader: SamReader): List[GroupID] = {
    samReader.getFileHeader.getReadGroups.map(GroupID.fromSamReadGroup)
  }.toList

  def newStatsMap(readgroups: Seq[String]): Map[String, GroupStats] = {
    readgroups.map(_ -> GroupStats()).toMap
  }

  /**
    * This method iterates over all SAMrecords in the iterator and returns the groupstats
    * The SAMRecordIterator is closed in this method.
    * @param samRecordIterator A samrecord iterator containing all the reads you are interested in.
    *                          (I.e after a SAMReader.query)
    * @return A GroupStats object with al the stats from the samrecords
    */
  def extractStats(samRecordIterator: SAMRecordIterator,
                   readgroups: Seq[SAMReadGroupRecord],
                   condition: SAMRecord => Boolean): BamstatsRoot = {
    val readGroupIds = readgroups
      .map(readgroup => readgroup.getId -> GroupID.fromSamReadGroup(readgroup))
      .toMap
    val stats = newStatsMap(readGroupIds.keys.toSeq)
    samRecordIterator.foreach { record =>
      if (condition(record)) {
        val rgId = Option(record.getAttribute("RG"))
          .map(_.toString)
          .getOrElse(throw new IllegalStateException(
            s"No readgroup found on read $record"))
        stats
          .getOrElse(
            rgId,
            throw new IllegalStateException(
              s"readgroup found on record but not in header: $rgId, record: $record"))
          .loadRecord(record)
      }
    }
    samRecordIterator.close()
    val bamStatsRoots = stats.map {
      case (rgId: String, groupStats: GroupStats) =>
        BamstatsRoot.fromGroupStats(readGroupIds(rgId), groupStats)
    }
    bamStatsRoots.reduce(_ + _)
  }

  /**
    * This methods reads the stats for all records in the SamReader.
    * The samReader is not closed afterwards, so it can be reused.
    * @param samReader A SamReader
    * @return GroupStats for all records in the SamReader.
    */
  def extractStatsAll(samReader: SamReader): BamstatsRoot = {
    extractStats(samReader.iterator(),
                 samReader.getFileHeader.getReadGroups,
                 _ => true)
  }

  /**
    * Takes a samReader and returns all the stats for all unmapped reads.
    * @param samReader a samReader
    * @return GroupStats for all unmapped reads
    */
  def extractStatsUnmappedReads(samReader: SamReader): BamstatsRoot = {
    extractStats(samReader.queryUnmapped(),
                 samReader.getFileHeader.getReadGroups,
                 _ => true)
  }

  /**
    * Takes a samReader and retunrs the GroupStats for all reads in the described region
    * @param samReader a SamReader
    * @param region a BedRecord describing the region
    * @param scatterMode if True, only reads that originate (i.e. have their start position) in the
    *                    region are counted.
    *                    This is useful when scattering over regions to make sure that reads are not counted
    *                    twice. One time when the start position is in the region, and one time when
    *                    the end position is in the region.
    * @return
    */
  def extractStatsRegion(samReader: SamReader,
                         region: BedRecord,
                         scatterMode: Boolean = false): BamstatsRoot = {
    val samRecordIterator: SAMRecordIterator =
      samReader.query(region.chr, region.start, region.end, false)
    def recordInInterval(samRecord: SAMRecord): Boolean = {
      (!scatterMode || samRecord.getAlignmentStart > region.start && samRecord.getAlignmentStart <= region.end)
    }
    extractStats(samRecordIterator,
                 samReader.getFileHeader.getReadGroups,
                 recordInInterval)
  }

  /**
    * Write GroupStats to tsv files
    * @param stats a GroupStats object
    * @param outputDir the directory where the tsv files are written.
    */
  def writeStatsToTsv(stats: GroupStats, outputDir: File): Unit = {
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

    stats.leftClippingHistogram.writeFilesAndPlot(outputDir,
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
         |To generate stats from `file.bam`:
         |${example("-b",
                    "file.bam",
                    "-o",
                    "output_dir",
                    "--sample",
                    "patient0",
                    "--library",
                    "libI",
                    "--readgroup",
                    "RG1")}
         |
         |To generate stats from `file.bam`, and output the result also as TSV:
         |${example("-o",
                    "output_dir",
                    "-b",
                    "file.bam",
                    "--sample",
                    "patient0",
                    "--library",
                    "libI",
                    "--readgroup",
                    "RG1")}
         |
         |To generate stats from certain regions in `file.bam`,
         |validate the regions and bam with `reference.fa` and also include unmapped reads:
         |${example("-R",
                    "reference.fa",
                    "-o",
                    "output_dir",
                    "-b",
                    "file.bam",
                    "--bedFile",
                    "regions.bed",
                    "--sample",
                    "patient0",
                    "--library",
                    "libI",
                    "--readgroup",
                    "RG1")}
     """.stripMargin
}
