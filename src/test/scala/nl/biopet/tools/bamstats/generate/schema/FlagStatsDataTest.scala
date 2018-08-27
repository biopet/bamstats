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
  def conversionToAndFromFlagStatsData(): Unit = {
    val flagStatsData: FlagStatsData = flagstats.toFlagStatsData
    val schemaFlagstats: FlagStats = FlagStats.fromFlagStatsData(flagStatsData)
    schemaFlagstats.flagStatsToMap shouldBe flagstats.flagStatsToMap
    schemaFlagstats.crossCountsToMap shouldBe flagstats.crossCountsToMap
    schemaFlagstats == flagstats shouldBe true
    // TODO: Implement hashmethod so below is true
    //schemaFlagstats shouldBe flagstats
  }
}
