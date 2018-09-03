package nl.biopet.tools.bamstats.merge

import nl.biopet.test.BiopetTest

class MergeTest extends BiopetTest {

  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      Merge.main(Array())
    }
  }

}
