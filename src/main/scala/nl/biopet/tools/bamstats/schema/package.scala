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

package object schema {
  case class Sample(libraries: Map[String, Library])
  case class Library(readgroups: Map[String, Readgroup])
  case class Readgroup(data: Data)
  case class SingleFlagStats(all: Long,
                             mapped: Long,
                             duplicates: Long,
                             firstOfPair: Long,
                             secondOfPair: Long,
                             readNegativeStrand: Long,
                             secondaryAlignment: Long,
                             readPaired: Long,
                             properPair: Long,
                             mateNegativeStrand: Long,
                             mateUnmapped: Long,
                             readFailsVendorQualityCheck: Long,
                             supplementaryAlignemnt: Long,
                             secondaryOrSupplementary: Long,
                             firstNormalSecondReadInverted: Long,
                             firstNormalSecondReadNormal: Long,
                             firstInvertedSecondReadInverted: Long,
                             firstInvertedSecondReadNormal: Long,
                             mateInSameStrand: Long,
                             mateOnOtherChromosome: Long,
                             singletons: Long)
  case class CombinedFlagStats(keys: IndexedSeq[String],
                               value: IndexedSeq[IndexedSeq[Long]])
  case class FlagStats(singleFlagStats: SingleFlagStats,
                       combinedFlagStats: CombinedFlagStats)
}
