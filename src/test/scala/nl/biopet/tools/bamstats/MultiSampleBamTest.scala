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

}
