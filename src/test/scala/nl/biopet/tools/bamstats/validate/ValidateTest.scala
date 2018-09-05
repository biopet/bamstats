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

package nl.biopet.tools.bamstats.validate

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class ValidateTest extends ToolTest[Args] {

  def toolCommand: Validate.type = Validate

  override def minExampleWords: Int =
    10 //A big example does not make sense for a tool with only one input.
  override def minManualWords: Int =
    0 // A manual does not make sense for such a small tool. The usage says it all.

  @Test
  def testMain(): Unit = {
    Validate.main(Array("-i", resourcePath("/stats/complete/bamstats.json")))
  }

  @Test
  def testCorrupted(): Unit = {
    intercept[IllegalArgumentException] {
      Validate.main(Array("-i", resourcePath("/stats/corrupted/bamstats.json")))
    }.getMessage shouldBe
      "requirement failed: Internally corrupt FlagStatsData. " +
        "The CrossCounts table totals do not equal the flagstats."
  }
}
