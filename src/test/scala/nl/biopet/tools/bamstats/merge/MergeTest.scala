package nl.biopet.tools.bamstats.merge

import java.io.File

import nl.biopet.test.BiopetTest
import nl.biopet.tools.bamstats.schema.BamstatsRoot
import org.testng.annotations.Test

class MergeTest extends BiopetTest {

  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      Merge.main(Array())
    }
  }

  @Test
  def testMergeFile(): Unit = {
    val scatteredStats = List(
      resourceFile("/stats/scatter-0/bamstats.json"),
      resourceFile("/stats/scatter-1/bamstats.json"),
      resourceFile("/stats/scatter-2/bamstats.json"),
      resourceFile("/stats/unmapped/bamstats.json")
    )

    val totalStatsFile = resourceFile("/stats/complete/bamstats.json")
    val mergedOutputFile = File.createTempFile("mergedStats", ".json")

    val inputStats: Array[String] = scatteredStats
      .flatMap(statsFile => Array("-i", statsFile.getAbsolutePath))
      .toArray

    Merge.main(inputStats ++ Array("-o", mergedOutputFile.getAbsolutePath))

    BamstatsRoot.fromFile(mergedOutputFile) == BamstatsRoot.fromFile(
      totalStatsFile) shouldBe true
  }
}
