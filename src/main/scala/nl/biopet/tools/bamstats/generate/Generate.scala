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
import nl.biopet.tools.bamstats.schema.BamstatsRoot
import nl.biopet.tools.bamstats.{GroupID, GroupStats}
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

    val stats: GroupStats = cmdArgs.bedFile match {
      case Some(bed: File) if cmdArgs.onlyUnmapped =>
        throw new IllegalArgumentException(
          "Cannot extract stats from regions and unmapped regions at the same time")
      // When a BED file is specified extract regions stats
      case Some(bed: File) if !cmdArgs.onlyUnmapped =>
        val regions =
          BedRecordList
            .fromFile(bed)
            .combineOverlap
            .validateContigs(sequenceDict)
        val regionStats = regions.allRecords.map { region =>
          extractStatsRegion(cmdArgs.bamFile, region, cmdArgs.scatterMode)
        }.toList
        // Add all regions stats together
        regionStats.reduce(_ += _)
      case None if cmdArgs.onlyUnmapped =>
        extractStatsUnmappedReads(cmdArgs.bamFile)
      case None if !cmdArgs.onlyUnmapped => extractStats(cmdArgs.bamFile)
    }

    if (cmdArgs.tsvOutputs) {
      writeStatsToTsv(stats, outputDir = cmdArgs.outputDir)
    }

    val groupedStats = BamstatsRoot.fromGroupStats(
      GroupID(sample = cmdArgs.sample,
              library = cmdArgs.library,
              readgroup = cmdArgs.readgroup),
      stats)
    val statsWriter = new PrintWriter(
      new File(cmdArgs.outputDir, "bamstats.json"))
    statsWriter.println(Json.stringify(groupedStats.toJson))
    statsWriter.close()

    val totalStats = stats.toSummaryMap
    val summaryWriter = new PrintWriter(
      new File(cmdArgs.outputDir, "bamstats.summary.json"))
    summaryWriter.println(Json.stringify(conversions.mapToJson(totalStats)))
    summaryWriter.close()

    logger.info("Done")
  }

  def extractStats(bamFile: File): GroupStats = {
    val samReader: SamReader =
      SamReaderFactory.makeDefault().open(bamFile)
    val stats = GroupStats()
    samReader.iterator().foreach(stats.loadRecord)
    samReader.close()
    stats
  }

  def extractStatsUnmappedReads(bamFile: File): GroupStats = {
    val samReader: SamReader =
      SamReaderFactory.makeDefault().open(bamFile)
    val stats = GroupStats()
    samReader.queryUnmapped().foreach(stats.loadRecord)
    samReader.close()
    stats
  }

  def extractStatsRegion(bamFile: File,
                         region: BedRecord,
                         scatterMode: Boolean = false): GroupStats = {
    val samReader: SamReader =
      SamReaderFactory.makeDefault().open(bamFile)
    val stats = GroupStats()

    val samRecordIterator: SAMRecordIterator =
      samReader.query(region.chr, region.start, region.end, false)
    samRecordIterator.foreach { samRecord =>
      // Read based stats
      // If scatterMode is false, continue.
      // If scatterMode is true, determine whether the alignment start is within the region.
      if (!scatterMode || samRecord.getAlignmentStart > region.start && samRecord.getAlignmentStart <= region.end) {
        stats.loadRecord(samRecord)
      }
    }
    samReader.close()
    stats
  }

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
