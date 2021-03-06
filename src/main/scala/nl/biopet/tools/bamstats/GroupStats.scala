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

package nl.biopet.tools.bamstats

import java.io.File

import htsjdk.samtools.SAMRecord
import nl.biopet.tools.bamstats.schema._
import nl.biopet.utils.Histogram

/**
  * Created by pjvanthof on 05/07/16.
  */
case class GroupStats(
    flagstat: FlagStats = new FlagStats(),
    mappingQualityHistogram: Histogram[Int] = new Histogram[Int](),
    insertSizeHistogram: Histogram[Int] = new Histogram[Int](),
    clippingHistogram: Histogram[Int] = new Histogram[Int](),
    leftClippingHistogram: Histogram[Int] = new Histogram[Int](),
    rightClippingHistogram: Histogram[Int] = new Histogram[Int](),
    _5_ClippingHistogram: Histogram[Int] = new Histogram[Int](),
    _3_ClippingHistogram: Histogram[Int] = new Histogram[Int]()) {

  /** This will add an other [[GroupStats]] inside `this` */
  def +=(other: GroupStats): GroupStats = {
    this.flagstat += other.flagstat
    this.mappingQualityHistogram += other.mappingQualityHistogram
    this.insertSizeHistogram += other.insertSizeHistogram
    this.clippingHistogram += other.clippingHistogram
    this.leftClippingHistogram += other.leftClippingHistogram
    this.rightClippingHistogram += other.rightClippingHistogram
    this._5_ClippingHistogram += other._5_ClippingHistogram
    this._3_ClippingHistogram += other._3_ClippingHistogram
    this
  }

  /**
    * Load a record into the Groupstats, which updates the histograms and flagstats.
    * @param samRecord
    */
  def loadRecord(samRecord: SAMRecord): Unit = {
    flagstat.loadRecord(samRecord)
    if (!samRecord.getReadUnmappedFlag) { // Mapped read
      mappingQualityHistogram.add(samRecord.getMappingQuality)
    }
    if (samRecord.getReadPairedFlag && samRecord.getProperPairFlag && samRecord.getFirstOfPairFlag && !samRecord.getSecondOfPairFlag)
      insertSizeHistogram.add(samRecord.getInferredInsertSize.abs)

    val leftClipping = samRecord.getAlignmentStart - samRecord.getUnclippedStart
    val rightClipping = samRecord.getUnclippedEnd - samRecord.getAlignmentEnd

    clippingHistogram.add(leftClipping + rightClipping)
    leftClippingHistogram.add(leftClipping)
    rightClippingHistogram.add(rightClipping)

    if (samRecord.getReadNegativeStrandFlag) {
      _5_ClippingHistogram.add(leftClipping)
      _3_ClippingHistogram.add(rightClipping)
    } else {
      _5_ClippingHistogram.add(rightClipping)
      _3_ClippingHistogram.add(leftClipping)
    }
  }

  def writeStatsToFiles(outputDir: File): Unit = {
    this.flagstat.writeReportToFile(new File(outputDir, "flagstats"))
    this.flagstat
      .writeSummaryToFile(new File(outputDir, "flagstats.summary.json"),
                          includeCrossCounts = false)
    this.mappingQualityHistogram
      .writeHistogramToTsv(new File(outputDir, "mapping_quality.tsv"))
    this.insertSizeHistogram
      .writeHistogramToTsv(new File(outputDir, "insert_size.tsv"))
    this.clippingHistogram
      .writeHistogramToTsv(new File(outputDir, "clipping.tsv"))
    this.leftClippingHistogram
      .writeHistogramToTsv(new File(outputDir, "left_clipping.tsv"))
    this.rightClippingHistogram
      .writeHistogramToTsv(new File(outputDir, "right_clipping.tsv"))
    this._5_ClippingHistogram
      .writeHistogramToTsv(new File(outputDir, "5_prime_clipping.tsv"))
    this._3_ClippingHistogram
      .writeHistogramToTsv(new File(outputDir, "3_prime_clipping.tsv"))
  }

  def toSummaryMap: Map[String, Map[String, Any]] = {
    Map(
      "flagstats" -> flagstat.toSummaryMap(false),
      "mapping_quality" -> Map(
        "histogram" -> mappingQualityHistogram.toSummaryMap,
        "general" -> mappingQualityHistogram.aggregateStats),
      "insert_size" -> Map("histogram" -> insertSizeHistogram.toSummaryMap,
                           "general" -> insertSizeHistogram.aggregateStats),
      "clipping" -> Map("histogram" -> clippingHistogram.toSummaryMap,
                        "general" -> clippingHistogram.aggregateStats),
      "left_clipping" -> Map("histogram" -> leftClippingHistogram.toSummaryMap,
                             "general" -> leftClippingHistogram.aggregateStats),
      "right_clipping" -> Map(
        "histogram" -> rightClippingHistogram.toSummaryMap,
        "general" -> rightClippingHistogram.aggregateStats),
      "5_prime_clipping" -> Map(
        "histogram" -> _5_ClippingHistogram.toSummaryMap,
        "general" -> _5_ClippingHistogram.aggregateStats),
      "3_prime_clipping" -> Map(
        "histogram" -> _3_ClippingHistogram.toSummaryMap,
        "general" -> _3_ClippingHistogram.aggregateStats)
    )
  }

  def statsToData(): Data =
    Data(
      flagStats = flagstat.toFlagStatsData,
      mappingQualityHistogram = mappingQualityHistogram.toDoubleArray,
      insertSizeHistogram = insertSizeHistogram.toDoubleArray,
      clippingHistogram = clippingHistogram.toDoubleArray,
      leftClippingHistogram = leftClippingHistogram.toDoubleArray,
      rightClippingHistogram = rightClippingHistogram.toDoubleArray,
      _5_ClippingHistogram = _5_ClippingHistogram.toDoubleArray,
      _3_ClippingHistogram = _3_ClippingHistogram.toDoubleArray
    )

}

object GroupStats {
  def statsFromData(data: Data): GroupStats =
    new GroupStats(
      flagstat = FlagStats.fromFlagStatsData(data.flagStats),
      mappingQualityHistogram =
        Histogram.fromDoubleArray(data.mappingQualityHistogram),
      insertSizeHistogram = Histogram.fromDoubleArray(data.insertSizeHistogram),
      clippingHistogram = Histogram.fromDoubleArray(data.clippingHistogram),
      leftClippingHistogram =
        Histogram.fromDoubleArray(data.leftClippingHistogram),
      rightClippingHistogram =
        Histogram.fromDoubleArray(data.rightClippingHistogram),
      _5_ClippingHistogram =
        Histogram.fromDoubleArray(data._5_ClippingHistogram),
      _3_ClippingHistogram =
        Histogram.fromDoubleArray(data._3_ClippingHistogram)
    )
}
