package nl.biopet.tools.bamstats

import htsjdk.samtools.{SamReader, SamReaderFactory}
import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class MultiSampleBamTest extends BiopetTest {

  val multiSampleSam: SamReader = SamReaderFactory
    .makeDefault()
    .open(resourceFile("/readgroups/merge.sam").getAbsoluteFile)
  @Test
  def multiSampleSamRead(): Unit = {}
}
