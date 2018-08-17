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

import htsjdk.samtools.{SAMRecord, SamReader, SamReaderFactory}
import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

import scala.collection.JavaConversions.collectionAsScalaIterable
class FlagStatsTest extends BiopetTest {

  val flagstats: FlagStats = new FlagStats()
  val samReader: SamReader =
    SamReaderFactory.makeDefault().open(resourceFile("/11_target.sam"))
  val recordsList: Iterable[SAMRecord] =
    collectionAsScalaIterable[SAMRecord](samReader.iterator().toList)
  recordsList.foreach(flagstats.loadRecord)

  @Test
  def testFlagstatsResults(): Unit = {
    val values = flagstats.toSummaryMap()
    val crossCounts: Map[String, Map[String, Long]] = values
      .getOrElse("crossCounts", Map())
      .asInstanceOf[Map[String, Map[String, Long]]]
    values("mapped") shouldBe 28
    values("readPaired") shouldBe 28
    values("properPair") shouldBe 26
    values("duplicate") shouldBe 0
    values("firstOfPair") shouldBe 15
    values("secondOfPair") shouldBe 13
    values("mateInSameStrand") shouldBe 0
    values("mateOnOtherChromosome") shouldBe 2
    values("singletons") shouldBe 0
    crossCounts("firstOfPair")("mapped") shouldBe 15
  }

  @Test
  def testFlagstatsReport(): Unit = {
    flagstats.report() should include("28\t100.0000%\ttotal")
    flagstats.report() should include("26\t92.8571%\tproperPair")
    flagstats.report() should include("15\t53.5714%\tfirstOfPair")
    flagstats.report() should include("13\t46.4286%\tsecondOfPair")
    flagstats.report() should include("2\t7.1429%\tmateOnOtherChromosome")
    flagstats.report() should include("#1\t#2\t#3\t#4")
    flagstats.report() should include("28\t28\t0\t0")
    flagstats.report() should include("100.0000%\t100.0000%\t0.0000%\t0.0000%")
  }

  @Test
  def testFlagStatsOrder(): Unit = {
    val keyList = flagstats.flagstatsSorted.map {
      case (method, _) => method
    }
    keyList shouldBe FlagMethods.values.toList
    // Also reverse list to make sure test works
    keyList shouldNot be(FlagMethods.values.toList.reverse)
  }

  @Test
  def testCrossCountsOrder(): Unit = {
    flagstats.crossCountsSorted.foreach {
      case (_, countsMap) =>
        val keyList = countsMap.map { case (method, _) => method }
        keyList shouldBe FlagMethods.values.toList
        keyList shouldNot be(FlagMethods.values.toList.reverse)
    }
    val keyList = flagstats.crossCountsSorted.map { case (method, _) => method }
    keyList shouldBe FlagMethods.values.toList
    keyList shouldNot be(FlagMethods.values.toList.reverse)
  }
}
