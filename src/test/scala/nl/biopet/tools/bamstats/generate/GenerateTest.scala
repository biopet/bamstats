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

import java.io.File

import com.google.common.io.Files
import nl.biopet.tools.bamstats.GroupID
import nl.biopet.tools.bamstats.schema.BamstatsRoot
import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

import scala.io.Source

class GenerateTest extends ToolTest[Args] {
  def toolCommand: Generate.type = Generate
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      Generate.main(Array())
    }
  }

  val testGroupID: GroupID = GroupID("xf33hai", "3rasdsq", "a7kac")
  val pairedBam01 = new File(resourcePath("/paired01.bam"))
  val groupIdArgs: Array[String] = Array("--sample",
                                         testGroupID.sample,
                                         "--library",
                                         testGroupID.library,
                                         "--readgroup",
                                         testGroupID.readgroup)

  @Test
  def testMain(): Unit = {
    val outputDir = Files.createTempDir()
    outputDir.deleteOnExit()
    Generate.main(Array("-b",
                        pairedBam01.getAbsolutePath,
                        "-o",
                        outputDir.getAbsolutePath) ++ groupIdArgs)

    val bamstatsFile = new File(outputDir, "bamstats.json")
    bamstatsFile should exist
    val bamstatsSummaryFile = new File(outputDir, "bamstats.summary.json")
    bamstatsSummaryFile should exist

    BamstatsRoot.fromFile(bamstatsFile).validate()

    // Some content testing here. Extensive testing should be done in FlagStatsTest
    val bamstatsContents = Source.fromFile(bamstatsFile).getLines.mkString
    bamstatsContents should include(testGroupID.sample)
    bamstatsContents should include(testGroupID.library)
    bamstatsContents should include(testGroupID.readgroup)
    bamstatsContents should include("\"total\":14")
    bamstatsContents should include("\"mapped\":12")
    bamstatsContents should include("\"properPair\":12")
    bamstatsContents should include("\"crossCounts\":{")
    val summaryContents = Source.fromFile(bamstatsSummaryFile).getLines.mkString
    summaryContents should not include "\"crossCounts\":{"
    bamstatsContents should include(testGroupID.sample)
    bamstatsContents should include(testGroupID.library)
    bamstatsContents should include(testGroupID.readgroup)
    bamstatsContents should include("\"total\":14")
    bamstatsContents should include("\"mapped\":12")
    bamstatsContents should include("\"properPair\":12")

    // Make sure TSVs are not produced.
    new File(outputDir, "flagstats.tsv") shouldNot exist
    new File(outputDir, "insertsize.stats.tsv") shouldNot exist
    new File(outputDir, "insertsize.histogram.tsv") shouldNot exist
    new File(outputDir, "mappingQuality.stats.tsv") shouldNot exist
    new File(outputDir, "mappingQuality.histogram.tsv") shouldNot exist
    new File(outputDir, "clipping.stats.tsv") shouldNot exist
    new File(outputDir, "clipping.histogram.tsv") shouldNot exist

    new File(outputDir, "flagstats") shouldNot exist
    new File(outputDir, "flagstats.summary.json") shouldNot exist
    new File(outputDir, "mapping_quality.tsv") shouldNot exist
    new File(outputDir, "insert_size.tsv") shouldNot exist
    new File(outputDir, "clipping.tsv") shouldNot exist
    new File(outputDir, "left_clipping.tsv") shouldNot exist
    new File(outputDir, "right_clipping.tsv") shouldNot exist
    new File(outputDir, "5_prime_clipping.tsv") shouldNot exist
    new File(outputDir, "3_prime_clipping.tsv") shouldNot exist
  }

  @Test
  def testTsvOutputs(): Unit = {
    val outputDir = Files.createTempDir()
    outputDir.deleteOnExit()
    Generate.main(
      Array("-b",
            pairedBam01.getAbsolutePath,
            "-o",
            outputDir.getAbsolutePath,
            "--tsvOutputs") ++ groupIdArgs)

    new File(outputDir, "bamstats.json") should exist
    new File(outputDir, "bamstats.summary.json") should exist
    BamstatsRoot.fromFile(new File(outputDir, "bamstats.json")).validate()
    new File(outputDir, "flagstats.tsv") should exist
    new File(outputDir, "insertsize.stats.tsv") should exist
    new File(outputDir, "insertsize.histogram.tsv") should exist
    new File(outputDir, "mappingQuality.stats.tsv") should exist
    new File(outputDir, "mappingQuality.histogram.tsv") should exist
    new File(outputDir, "clipping.stats.tsv") should exist
    new File(outputDir, "clipping.histogram.tsv") should exist

    new File(outputDir, "flagstats") shouldNot exist
    new File(outputDir, "flagstats.summary.json") shouldNot exist
    new File(outputDir, "mapping_quality.tsv") shouldNot exist
    new File(outputDir, "insert_size.tsv") shouldNot exist
    new File(outputDir, "clipping.tsv") shouldNot exist
    new File(outputDir, "left_clipping.tsv") shouldNot exist
    new File(outputDir, "right_clipping.tsv") shouldNot exist
    new File(outputDir, "5_prime_clipping.tsv") shouldNot exist
    new File(outputDir, "3_prime_clipping.tsv") shouldNot exist
  }

}
