package nl.biopet.tools.bamstats

import htsjdk.samtools.SAMRecord

import scala.collection.mutable

object FlagMethods extends Enumeration {
  protected case class Val(method: SAMRecord => Boolean)
    extends super.Val
  implicit def valueToVal(x: Value): Val = x.asInstanceOf[Val]

  val Total = Val { _ => true}
  val Mapped = Val { !_.getReadUnmappedFlag}
  val mateUnmapped = Val {record => record.getReadPairedFlag && record.getMateUnmappedFlag}
  val duplicate = Val {_.getDuplicateReadFlag}
  val firstOfPair = Val {record => record.getReadPairedFlag && record.getFirstOfPairFlag}
  val secondOfPair = Val {record => record.getReadPairedFlag && record.getSecondOfPairFlag}
  val readNegativeStrand = Val {_.getReadNegativeStrandFlag}
  val notPrimaryAlignment = Val {_.isSecondaryAlignment}
  val readPaired = Val {_.getReadPairedFlag}
  val properPair = Val {record => record.getReadPairedFlag && record.getProperPairFlag }
  val mateNegativeStrand = Val {record => record.getReadPairedFlag && record.getMateNegativeStrandFlag}
  val readFailsVendorQualityCheck = Val {_.getReadFailsVendorQualityCheckFlag}
  val supplementaryAlignment = Val {_.getSupplementaryAlignmentFlag}
  def mappingQualityGreaterThan(mappingQuality: Int): Val = Val { _.getMappingQuality > mappingQuality}

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

  val firstInvertedSecondInverted = Val {record =>
    record.getReadPairedFlag &&
      record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
      ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart))
  }

  val firstInvertedSecondNormal = Val {record =>
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

  def report(flagstats: Map[FlagMethods.Value, Long]) = {
    val buffer = new mutable.StringBuilder()
    buffer.append("Number\tTotal Flags\tFraction\tName\n")
    val totalFlags: Option[Long] = flagstats.get(Total)
    flagstats.foreach { case (method: FlagMethods.Value, count: Long) =>
      val percentage = totalFlags.map(totalCount => f"${(count.toDouble / totalCount) * 100}%.4f").getOrElse("N/A")
      buffer.append(s"#${method.id}\t$count\t$percentage\t${method.outerEnum.toString()}\n")
    }
  }

  def crossReport(crossCounts: Map[FlagMethods.Value, Map[FlagMethods.Value, Long]]): String = {
    val buffer = new mutable.StringBuilder()

  }

  def getFlagStats(samRecords: Seq[SAMRecord]): Map[FlagMethods.Value, Long] = {
    val results = emptyResult
    samRecords.foreach { record =>
      results.keys.foreach { flagMethod =>
        if (flagMethod(record)) results(flagMethod) += 1
      }
    }
    results.toMap
  }

  def getCrossCounts(samRecords: Seq[SAMRecord]): Map[FlagMethods.Value, Map[FlagMethods.Value, Long]] = {
    val results = mutable.Map[FlagMethods.Value, Map[FlagMethods.Value, Long]]()
    values.foreach { method =>
      results(method) = {
        getFlagStats(samRecords.filter(method.method))
      }
    }
    results.toMap
  }




}
