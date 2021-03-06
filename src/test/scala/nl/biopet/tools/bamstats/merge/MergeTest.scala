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

package nl.biopet.tools.bamstats.merge

import java.io.File

import nl.biopet.tools.bamstats.schema.BamstatsRoot
import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class MergeTest extends ToolTest[Args] {

  def toolCommand: Merge.type = Merge

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
