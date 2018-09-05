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

import java.io.File

import htsjdk.samtools.SamReaderFactory
import nl.biopet.test.BiopetTest
import nl.biopet.tools.bamstats.generate.Generate
import org.testng.annotations.Test

class MultiSampleBamTest extends BiopetTest {

  val multiSampleSam
    : File = resourceFile("/readgroups/merge.sam").getAbsoluteFile
  val sm1lib1rg1Sam
    : File = resourceFile("/readgroups/sm1-lib1-rg1.sam").getAbsoluteFile
  val sm1lib1rg2Sam
    : File = resourceFile("/readgroups/sm1-lib1-rg2.sam").getAbsoluteFile
  val sm1lib2rg1Sam
    : File = resourceFile("/readgroups/sm1-lib2-rg1.sam").getAbsoluteFile
  val sm2lib1rg1Sam
    : File = resourceFile("/readgroups/sm2-lib1-rg1.sam").getAbsoluteFile

  @Test
  def multiSampleSamRead(): Unit = {
    val samReader = SamReaderFactory.makeDefault().open(multiSampleSam)
    val root = Generate.extractStatsAll(samReader)
    root.samples.keySet shouldBe Set("sm1", "sm2")
    root.samples("sm1").libraries.keySet shouldBe Set("lib1", "lib2")
    root.samples("sm2").libraries.keySet shouldBe Set("lib1")
    root.samples("sm1").libraries("lib1").readgroups.keySet shouldBe Set(
      "sm1-lib1-rg1",
      "sm1-lib1-rg2")
    root.samples("sm1").libraries("lib2").readgroups.keySet shouldBe Set(
      "sm1-lib2-rg1")
    root.samples("sm2").libraries("lib1").readgroups.keySet shouldBe Set(
      "sm2-lib1-rg1")
  }

  @Test
  def multiSampleMerge(): Unit = {
    val mergedRoot = {
      val samReader = SamReaderFactory.makeDefault().open(multiSampleSam)
      Generate.extractStatsAll(samReader)
    }
    val rgBams: Seq[File] = List(
      sm1lib1rg1Sam,
      sm1lib1rg2Sam,
      sm1lib2rg1Sam,
      sm2lib1rg1Sam
    )
    val rgRoots = rgBams.map { bamFile =>
      val samReader = SamReaderFactory.makeDefault().open(bamFile)
      Generate.extractStatsAll(samReader)
    }
    rgRoots.reduce(_ + _) shouldBe mergedRoot
  }
}
