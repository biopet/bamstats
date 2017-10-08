package nl.biopet.tools.bamstats

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class BamStatsTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      BamStats.main(Array())
    }
  }
}
