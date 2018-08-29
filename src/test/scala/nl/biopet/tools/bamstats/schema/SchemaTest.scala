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

package nl.biopet.tools.bamstats.schema

import java.io.File

import nl.biopet.test.BiopetTest
import nl.biopet.tools.bamstats.{FlagMethods, GroupID}
import org.testng.annotations.{DataProvider, Test}

class SchemaTest extends BiopetTest {

  @Test
  def testRoot(): Unit = {
    val root: BamstatsRoot =
      BamstatsRoot.fromFile(resourceFile("/json/bamstats.json"))
    root.validate()
    root.readgroups.foreach {
      case (groupId, stats) =>
        groupId shouldBe GroupID("sample", "library", "readgroup")
    }
  }

  @DataProvider(name = "wrongJson")
  def provider(): Array[Array[Any]] = {
    Array(
      Array(
        resourceFile("/json/bamstatsIncorrectFlagstatKeys.json"),
        List(
          "FlagStatsData incompatible. Missing and/or unknown names in flagstats.",
          "Missing: properPair,readFailsVendorQualityCheck",
          "Unknown: properPairs")
      ),
      Array(
        resourceFile("/json/bamstatsIncorrectCrosscountsKeys.json"),
        List(
          "FlagStatsData incompatible. Internally corrupt. CrossCount keys do not match flagstats keys.")
      ),
      Array(
        resourceFile("/json/bamstatsCrosscountsIncorrectRowSize.json"),
        List(
          s"Number of rows (24) not equal to number of methods (${FlagMethods.values.toList.length})")
      ),
      Array(
        resourceFile("/json/bamstatsCrosscountsIncorrectColumnSize.json"),
        List(
          s"Number of columns (24) not equal to number of methods (${FlagMethods.values.toList.length}) on row 5")
      ),
      Array(
        resourceFile("/json/bamstatsCrosscountsInvalidMatrix.json"),
        List(
          "Crossline at (5,5) with value '8' is not equal to the value in the totals column at (5,1) with value '9'")
      ),
      Array(
        resourceFile("/json/bamstatsCrosscountsContradictFlagstats.json"),
        List(
          "Internally corrupt FlagStatsData. The CrossCounts table totals do not equal the flagstats.")
      )
    )
  }

  @Test(dataProvider = "wrongJson")
  def testValidationExceptions(json: File, messages: List[String]): Unit = {
    val root: BamstatsRoot = BamstatsRoot.fromFile(json)
    val errorMessage = intercept[IllegalArgumentException] {
      root.validate()
    }.getMessage
    errorMessage should include("requirement failed:")
    messages.foreach(errorMessage should include(_))
  }

  /**
    * This test was added to test otherwise unreachable error in crosscounts.
    */
  @Test
  def testCrosscountValidationError(): Unit = {
    val root: BamstatsRoot =
      BamstatsRoot.fromFile(
        resourceFile("/json/bamstatsIncorrectCrosscountsKeys.json"))
    root.readgroups.headOption.foreach {
      case (_, data) =>
        val errorMessage = intercept[IllegalArgumentException] {
          data.data.flagStats.crossCounts.validate()
        }.getMessage
        errorMessage should include(
          "FlagStatsData incompatible. Missing and/or unknown names in crosscounts.")
        errorMessage should include(
          "Missing: properPair,readFailsVendorQualityCheck")
        errorMessage should include("Unknown: properPairs")
    }
  }

}
