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
import nl.biopet.tools.bamstats.generate.Generate.{
  extractStatsAll,
  extractStatsRegion,
  extractStatsUnmappedReads
}
import nl.biopet.tools.bamstats.schema.BamstatsRoot
import nl.biopet.utils.ngs.intervals.{BedRecord, BedRecordList}
import org.testng.annotations.Test

class ScatterAndMergeTest extends BiopetTest {
  @Test
  def scattersWithBed(): Unit = {
    val bamFile
      : File = resourceFile("/fake_chrQ1000simreads.bam").getAbsoluteFile
    val referenceFile: File = resourceFile("/fake_chrQ.fa").getAbsoluteFile
    val samReader = SamReaderFactory.makeDefault().open(bamFile)
    val regions: List[BedRecord] =
      BedRecordList.fromReference(referenceFile).scatter(5000).flatten
    val regionStats: List[BamstatsRoot] = regions.map { region =>
      extractStatsRegion(samReader, region, scatterMode = true)
    }
    val unmappedStats: BamstatsRoot = extractStatsUnmappedReads(samReader)

    val mergedStats: BamstatsRoot =
      (regionStats ++ List(unmappedStats)).reduce(_ + _)
    val totalStats: BamstatsRoot = extractStatsAll(samReader)
    samReader.close()
    mergedStats shouldBe totalStats
  }
}
