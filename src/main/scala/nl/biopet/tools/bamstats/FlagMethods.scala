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

import htsjdk.samtools.SAMRecord

import scala.collection.mutable

object FlagMethods extends Enumeration {
  protected case class Val(method: SAMRecord => Boolean) extends super.Val {
    def name: String = toString
  }
  implicit def valueToVal(x: Value): Val = x.asInstanceOf[Val]

  val Total = Val { _ =>
    true
  }
  val mapped = Val { !_.getReadUnmappedFlag }
  val mateUnmapped = Val { record =>
    record.getReadPairedFlag && record.getMateUnmappedFlag
  }
  val duplicate = Val { _.getDuplicateReadFlag }
  val firstOfPair = Val { record =>
    record.getReadPairedFlag && record.getFirstOfPairFlag
  }
  val secondOfPair = Val { record =>
    record.getReadPairedFlag && record.getSecondOfPairFlag
  }
  val readNegativeStrand = Val { _.getReadNegativeStrandFlag }
  val notPrimaryAlignment = Val { _.isSecondaryAlignment }
  val readPaired = Val { _.getReadPairedFlag }
  val properPair = Val { record =>
    record.getReadPairedFlag && record.getProperPairFlag
  }
  val mateNegativeStrand = Val { record =>
    record.getReadPairedFlag && record.getMateNegativeStrandFlag
  }
  val readFailsVendorQualityCheck = Val { _.getReadFailsVendorQualityCheckFlag }
  val supplementaryAlignment = Val { _.getSupplementaryAlignmentFlag }
  def mappingQualityGreaterThan(mappingQuality: Int): Val = Val {
    _.getMappingQuality > mappingQuality
  }

  // Below functions check read orientation
  val firstNormalSecondInverted = Val { record =>
    record.getReadPairedFlag &&
    record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag != record.getMateNegativeStrandFlag &&
    ((record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart))
  }

  val firstNormalSecondNormal = Val { record =>
    record.getReadPairedFlag &&
    record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
    ((record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart))
  }

  val firstInvertedSecondInverted = Val { record =>
    record.getReadPairedFlag &&
    record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
    ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart))
  }

  val firstInvertedSecondNormal = Val { record =>
    record.getReadPairedFlag &&
    record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag != record.getMateNegativeStrandFlag &&
    ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
    (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart))
  }

  def emptyResult: mutable.Map[FlagMethods.Value, Long] = {
    val map = mutable.Map[FlagMethods.Value, Long]()
    values.foreach(x => map += (x -> 0L))
    map
  }

  def emptyCrossResult
    : mutable.Map[FlagMethods.Value, mutable.Map[FlagMethods.Value, Long]] = {
    val map =
      mutable.Map[FlagMethods.Value, mutable.Map[FlagMethods.Value, Long]]()
    values.foreach(method => map += (method -> emptyResult))
    map
  }

  def flagStatsToMap(
      flagStats: mutable.Map[FlagMethods.Value, Long]): Map[String, Long] = {
    flagStats.map {
      case (method, count) => {
        (method.name -> count)
      }
    }.toMap
  }

  def crossCountsToMap(
      crosscounts: mutable.Map[FlagMethods.Value,
                               mutable.Map[FlagMethods.Value, Long]])
    : Map[String, Map[String, Long]] = {
    crosscounts.map {
      case (method, flagstats) =>
        (method.name -> flagStatsToMap(flagstats))
    }.toMap
  }

  def getFlagStats(samRecords: Seq[SAMRecord]): Map[FlagMethods.Value, Long] = {
    val results = emptyResult
    samRecords.foreach { record =>
      results.keys.foreach { flagMethod =>
        if (flagMethod.method(record)) results(flagMethod) += 1
      }
    }
    results.toMap
  }

  def getCrossCounts(samRecords: Seq[SAMRecord])
    : Map[FlagMethods.Value, Map[FlagMethods.Value, Long]] = {
    val results = mutable.Map[FlagMethods.Value, Map[FlagMethods.Value, Long]]()
    values.foreach { method =>
      results(method) = {
        getFlagStats(samRecords.filter(method.method))
      }
    }
    results.toMap
  }

}
