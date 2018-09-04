package nl.biopet.tools.bamstats

import java.io.File

import htsjdk.samtools.SamReaderFactory
import nl.biopet.test.BiopetTest
import nl.biopet.tools.bamstats.generate.Generate
import org.testng.annotations.Test

class MultiSampleBamTest extends BiopetTest {

  val multiSampleSam
    : File = resourceFile("/readgroups/merge.sam").getAbsoluteFile

  @Test
  def multiSampleSamRead(): Unit = {
    val samReader = SamReaderFactory.makeDefault().open(multiSampleSam)
    val root = Generate.extractStatsAll(samReader)
    root.samples.keySet shouldBe Set("sm1", "sm2")
  }

}
