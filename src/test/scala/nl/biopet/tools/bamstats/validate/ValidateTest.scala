package nl.biopet.tools.bamstats.validate

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class ValidateTest extends BiopetTest {

  @Test
  def testMain(): Unit = {
    Validate.main(Array("-i", resourcePath("/stats/complete/bamstats.json")))
  }

  @Test
  def testCorrupted(): Unit = {
    intercept[java.lang.IllegalArgumentException] {
      Validate.main(Array("-i", resourcePath("/stats/corrupted/bamstats.json")))
    }.getMessage shouldBe
      "requirement failed: Internally corrupt FlagStatsData. " +
        "The CrossCounts table totals do not equal the flagstats."
  }
}
