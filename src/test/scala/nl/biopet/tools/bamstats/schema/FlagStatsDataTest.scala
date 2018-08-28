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

package nl.biopet.tools.bamstats.schema

import htsjdk.samtools.{SAMRecord, SamReader, SamReaderFactory}
import nl.biopet.test.BiopetTest
import nl.biopet.tools.bamstats.FlagStats
import nl.biopet.tools.bamstats.schema.{CrossCounts, FlagStatsData}
import nl.biopet.tools.bamstats.schema.Implicits._
import org.testng.annotations.Test
import play.api.libs.json.Json

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

  @Test
  def coversionToAndFromJson(): Unit = {
    val flagstatsJson = Json.toJson(flagstats.toFlagStatsData)
    val flagstatsFromJson: FlagStatsData = Json
      .fromJson[FlagStatsData](flagstatsJson)
      .getOrElse(FlagStatsData(Map(), CrossCounts(List(), List())))
    val flagstatsConverted = FlagStats.fromFlagStatsData(flagstatsFromJson)
    flagstatsConverted == flagstats shouldBe true
  }

}
