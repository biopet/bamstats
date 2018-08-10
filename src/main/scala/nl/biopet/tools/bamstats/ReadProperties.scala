package nl.biopet.tools.bamstats

import htsjdk.samtools.SAMRecord

class ReadProperties(record: SAMRecord) {

  lazy val mapped: Boolean = !record.getReadUnmappedFlag
  lazy val mateUnmapped: Boolean =
    if (record.getReadPairedFlag) record.getMateUnmappedFlag else false

  lazy val duplicate: Boolean = record.getDuplicateReadFlag
  lazy val firstOfPair: Boolean =
    if (record.getReadPairedFlag) record.getFirstOfPairFlag else false
  lazy val secondOfPair: Boolean =
    if (record.getReadPairedFlag) record.getSecondOfPairFlag else false
  lazy val readNegativeStrand: Boolean = record.getReadNegativeStrandFlag
  val notPrimaryAlignment: Boolean = record.isSecondaryAlignment
  lazy val readPaired: Boolean = record.getReadPairedFlag
  lazy val properPair: Boolean =
    if (record.getReadPairedFlag) record.getProperPairFlag else false
  lazy val mateNegativeStrand: Boolean =
    if (record.getReadPairedFlag)
      record.getMateNegativeStrandFlag
    else false
  lazy val readFailsVendorQualityCheck: Boolean =
    record.getReadFailsVendorQualityCheckFlag
  lazy val supplementaryAlignment: Boolean =
    record.getSupplementaryAlignmentFlag
  lazy val secondaryOrSupplementary: Boolean = record.isSecondaryOrSupplementary

  lazy val mappingQuality: Int = record.getMappingQuality
  def mappingQualityGreaterThan(mappingQuality: Int): Boolean = {
    record.getMappingQuality > mappingQuality
  }

  // Below functions check read orientation

  lazy val firstNormalSecondInverted: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag != record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }
  lazy val firstNormalSecondNormal: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }

  lazy val firstInvertedSecondInverted: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }

  lazy val firstInvertedSecondNormal: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag != record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }

  lazy val mateInSameStrand: Boolean = {
    record.getReadPairedFlag && record.getReadNegativeStrandFlag && record.getMateNegativeStrandFlag &&
    record.getReferenceIndex == record.getMateReferenceIndex
  }

  lazy val mateOnOtherChromosome: Boolean = {
    record.getReadPairedFlag && record.getReferenceIndex != record.getMateReferenceIndex
  }

  lazy val map: Map[String, Boolean] = {
    Map(
      "Mapped" -> mapped,
      "Duplicate" -> duplicate,
      "FirstOfPair" -> firstOfPair,
      "SecondOfPair" -> secondOfPair,
      "ReadNegativeStrand" -> readNegativeStrand,
      "NotPrimaryAlignment" -> notPrimaryAlignment,
      "ReadPaired" -> readPaired,
      "ProperPair" -> properPair,
      "MateNegativeStrand" -> mateNegativeStrand,
      "MateUnmapped" -> mateUnmapped,
      "ReadFailsVendorQualityCheck" -> readFailsVendorQualityCheck,
      "SupplementaryAlignment" -> supplementaryAlignment,
      "SecondaryOrSupplementary" -> secondaryOrSupplementary,
      "First normal, second read inverted (paired end orientation)" -> firstInvertedSecondInverted,
      "First normal, second read normal" -> firstNormalSecondNormal,
      "First inverted, second read inverted" -> firstInvertedSecondInverted,
      "First inverted, second read normal" -> firstInvertedSecondNormal,
      "Mate in same strand" -> mateInSameStrand,
      "Mate on other chr" -> mateOnOtherChromosome
    )
  }
}
