package nl.biopet.tools.bamstats.generate.schema

import htsjdk.samtools.{SAMRecord, SamReader, SamReaderFactory}
import nl.biopet.test.BiopetTest
import nl.biopet.tools.bamstats.FlagStats
import nl.biopet.tools.bamstats.schema.FlagStatsData
import org.testng.annotations.Test

import scala.collection.JavaConversions.collectionAsScalaIterable

class FlagStatsDataTest extends BiopetTest {

  val flagstats: FlagStats = new FlagStats()
  val samReader: SamReader =
    SamReaderFactory.makeDefault().open(resourceFile("/11_target.sam"))
  val recordsList: Iterable[SAMRecord] =
    collectionAsScalaIterable[SAMRecord](samReader.iterator().toList)
  recordsList.foreach(flagstats.loadRecord)

  @Test
  def conversionToAndFrom(): Unit = {
    val flagStatsData: FlagStatsData = flagstats.toFlagStatsData
    FlagStats.fromFlagStatsData(flagStatsData) shouldBe flagstats
  }
}
