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

import org.scalatest.Matchers
import org.scalatest.testng.TestNGSuite
import org.testng.annotations.Test

/**
  * Created by pjvan_thof on 19-7-16.
  */
class StatsTest extends TestNGSuite with Matchers {
  @Test
  def testEqual(): Unit = {
    val s1 = Stats()
    val s2 = Stats()

    s1 shouldBe s2

    s1.mappingQualityHistogram.add(1)
    s1 should not be s2

    s2.mappingQualityHistogram.add(1)
    s1 shouldBe s2
  }

  @Test
  def testEmpty(): Unit = {
    val stats = Stats()

    stats.clippingHistogram.countsMap shouldBe empty
    stats.insertSizeHistogram.countsMap shouldBe empty
    stats.mappingQualityHistogram.countsMap shouldBe empty
    stats.leftClippingHistogram.countsMap shouldBe empty
    stats.rightClippingHistogram.countsMap shouldBe empty
    stats._5_ClippingHistogram.countsMap shouldBe empty
    stats._3_ClippingHistogram.countsMap shouldBe empty
  }

  @Test
  def testPlus(): Unit = {
    val s1 = Stats()
    val s2 = Stats()

    s2._3_ClippingHistogram.add(1)

    s1._3_ClippingHistogram.get(1) shouldBe None
    s1 += s2
    s1._3_ClippingHistogram.get(1) shouldBe Some(1)
  }
}
