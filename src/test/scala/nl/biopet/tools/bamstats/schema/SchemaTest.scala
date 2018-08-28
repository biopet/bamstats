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
import nl.biopet.tools.bamstats.GroupID
import org.testng.annotations.{DataProvider, Test}

class SchemaTest extends BiopetTest {

  @Test
  def testRoot(): Unit = {
    val root: Root = Root.fromFile(resourceFile("/bamstats.json"))
    root.validate()
    root.readgroups.headOption.foreach {
      case (groupId, stats) =>
        groupId shouldBe GroupID("sample", "library", "readgroup")
    }
  }

  @DataProvider(name = "wrongJson")
  def provider(): Array[Array[Any]] = {
    Array(
      Array(
        resourceFile("/bamstatsIncorrectFlagstatKeys.json"),
        List(
          "FlagStatsData incompatible. Missing and/or unknown names in flagstats.",
          "Missing: properPair,readFailsVendorQualityCheck",
          "Unknown: properPairs")
      )
    )
  }

  @Test(dataProvider = "wrongJson")
  def testValidationExceptions(json: File, messages: List[String]): Unit = {
    val root: Root = Root.fromFile(json)
    val errorMessage = intercept[IllegalArgumentException] {
      root.validate()
    }.getMessage
    errorMessage should include("requirement failed:")
    messages.foreach(errorMessage should include(_))
  }

}
