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
import nl.biopet.tools.bamstats.schema.BamstatsRoot
import nl.biopet.utils.ngs.intervals.BedRecordList
import org.testng.annotations.{AfterClass, Test}
import nl.biopet.tools.scatterregions.ScatterRegions.scatterRegions
import scala.collection.mutable.ListBuffer

class ScatterAndMergeTest extends BiopetTest {

  val testDirs: ListBuffer[File] = ListBuffer()
  def tempDir(prefix: String, suffix: String): File = {
    val dir = File.createTempFile(prefix, suffix)
    dir.delete()
    dir.mkdirs()
    testDirs += dir
    dir
  }

  def mkdir(file: File): File = {
    file.delete()
    file.mkdirs()
    file
  }

  @AfterClass
  def deleteTestDirs(): Unit = {
    testDirs.toList.foreach { dir =>
      dir.delete()
    }
  }

  val bamFile: File = resourceFile("/fake_chrQ1000simreads.bam").getAbsoluteFile
  val referenceFile: File = resourceFile("/fake_chrQ.fa").getAbsoluteFile

  val bamStatsGenerateArguments: Array[String] = Array[String](
    "generate",
    "-b",
    bamFile.toString,
    "--sample",
    "sample",
    "--library",
    "library",
    "--readgroup",
    "readgroup",
    "--reference",
    referenceFile.toString
  )
  @Test
  def scattersWithBed(): Unit = {
    val outputDir = tempDir("scatterAndMerge", ".d")
    val scattersDir = mkdir(new File(outputDir, "scatters"))
    scatterRegions(referenceFile, scattersDir, scatterSize = 5000)
    val scatterFiles = scattersDir.listFiles()
    val outputDirs =
      scatterFiles.map(file => new File(outputDir, s"${file.getName}.d"))
    outputDirs.foreach(mkdir)

    scatterFiles.zip(outputDirs).foreach {
      case (bedFile, bedOutputDir) =>
        BamStats.main(
          bamStatsGenerateArguments ++ Array("--bedFile",
                                             bedFile.toString,
                                             "--outputDir",
                                             bedOutputDir.toString,
                                             "--scatterMode"))
    }

    // Get unmapped reads as well
    val unmappedDir = mkdir(new File(outputDir, "unmapped.d"))
    BamStats.main(
      bamStatsGenerateArguments ++ Array("--onlyUnmapped",
                                         "--outputDir",
                                         unmappedDir.toString))

    val bamStatsFiles: Seq[File] = outputDirs.map(dir =>
      new File(dir, "bamstats.json")) ++ Seq(
      new File(unmappedDir, "bamstats.json"))

    val mergedBamstatsFile = new File(outputDir, "merged_bamstats.json")

    val inputBamStats: Array[String] =
      bamStatsFiles.flatMap(file => Seq("-i", file.toString)).toArray
    println(inputBamStats.mkString("\n"))
    BamStats.main(
      Array("merge") ++ inputBamStats ++ Array("-o",
                                               mergedBamstatsFile.toString))

    val completeBamstatsDir: File = mkdir(
      new File(outputDir, "complete_bamstats.d"))

    val completeBamstatsFile = new File(completeBamstatsDir, "bamstats.json")
    BamStats.main(
      bamStatsGenerateArguments ++ Array("--outputDir",
                                         completeBamstatsDir.toString))

    // Since doubleArrays do not have a fixed order, it is the stats which need to be compared.
    val completeStats = BamstatsRoot.fromFile(completeBamstatsFile)
    val mergedStats = BamstatsRoot.fromFile(mergedBamstatsFile)
    mergedStats == completeStats shouldBe true
  }
}
