package nl.biopet.tools.bamstats

import java.io.File

import com.google.common.io.Files
import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class BamStatsTest extends ToolTest[Args] {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      BamStats.main(Array())
    }
  }

  val pairedBam01 = new File(resourcePath("/paired01.bam"))

  @Test
  def testMain(): Unit = {
    val outputDir = Files.createTempDir()
    outputDir.deleteOnExit()
    BamStats.main(
      Array("-b", pairedBam01.getAbsolutePath, "-o", outputDir.getAbsolutePath))

    new File(outputDir, "bamstats.json") should exist
    new File(outputDir, "bamstats.summary.json") should exist

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
    BamStats.main(
      Array("-b",
        pairedBam01.getAbsolutePath,
        "-o",
        outputDir.getAbsolutePath,
        "--tsvOutputs"))

    new File(outputDir, "bamstats.json") should exist
    new File(outputDir, "bamstats.summary.json") should exist

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
