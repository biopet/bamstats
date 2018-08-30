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

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class ScatterAndMergeTest extends BiopetTest {
  @Test
  def testScatterAndMergeTest(): Unit = {
    val outputDir = File.createTempFile("scatterAndMerge", ".d")
    outputDir.delete()
    outputDir.mkdirs()
    val bamFile: File = resourceFile("/paired_valid.bam").getAbsoluteFile
    val referenceFile: File = resourceFile("/fake_chrQ.fa").getAbsoluteFile

    val scatterFiles = Seq("/scatter1.bed", "/scatter2.bed", "/scatter3.bed")
      .map(resourceFile(_).getAbsoluteFile)
    val bamStatsGenerateArguments = Array[String]("generate",
                                                  "-b",
                                                  bamFile.toString,
                                                  "--sample",
                                                  "sample",
                                                  "--library",
                                                  "library",
                                                  "--readgroup",
                                                  "readgroup",
                                                  "--reference",
                                                  referenceFile.toString)
    scatterFiles.foreach { bedFile =>
      val bedOutputDir = new File(outputDir, s"${bedFile.getName}.d")
      bedOutputDir.delete()
      bedOutputDir.mkdir()
      BamStats.main(
        bamStatsGenerateArguments ++ Array("--bedFile",
                                           bedFile.toString,
                                           "--outputDir",
                                           bedOutputDir.toString))
    }
  }
}
