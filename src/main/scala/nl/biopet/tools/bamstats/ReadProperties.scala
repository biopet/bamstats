package nl.biopet.tools.bamstats

import htsjdk.samtools.SAMRecord

class ReadProperties(record: SAMRecord) {

  val mapped: Boolean = !record.getReadUnmappedFlag
  val mateMapped: Boolean =
    if (record.getReadPairedFlag) !record.getMateUnmappedFlag else false

  val duplicate: Boolean = record.getDuplicateReadFlag
  val firstOfPair: Boolean =
    if (record.getReadPairedFlag) record.getFirstOfPairFlag else false
  val secondOfPair: Boolean =
    if (record.getReadPairedFlag) record.getSecondOfPairFlag else false
  val readNegativeStrand: Boolean = record.getReadNegativeStrandFlag
  val notPrimaryAlignment: Boolean = record.isSecondaryAlignment
  val readPaired: Boolean = record.getReadPairedFlag
  val properPair: Boolean =
    if (record.getReadPairedFlag) record.getProperPairFlag else false
  val mateNegativeStrand: Boolean =
    if (record.getReadPairedFlag)
      record.getMateNegativeStrandFlag
    else false
  val readFailsVendorQualityCheck: Boolean =
    record.getReadFailsVendorQualityCheckFlag
  val supplementaryAlignment: Boolean = record.getSupplementaryAlignmentFlag
  val secondaryOrSupplemantary: Boolean = record.isSecondaryOrSupplementary

  val mappingQuality: Int = record.getMappingQuality
  def mappingQualityGreaterThan(mappingQuality: Int): Boolean = {
    record.getMappingQuality > mappingQuality
  }

  // Below functions check read orientation

  val firstNormalSecondInverted: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag != record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }
  val firstNormalSecondNormal: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }

  val firstInvertedSecondInverted: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag == record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }

  val firstInvertedSecondNormal: Boolean = {
    if (record.getReadPairedFlag &&
        record.getReferenceIndex == record.getMateReferenceIndex && record.getReadNegativeStrandFlag != record.getMateNegativeStrandFlag &&
        ((record.getFirstOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getFirstOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && record.getReadNegativeStrandFlag && record.getAlignmentStart < record.getMateAlignmentStart) ||
        (record.getSecondOfPairFlag && !record.getReadNegativeStrandFlag && record.getAlignmentStart > record.getMateAlignmentStart)))
      true
    else false
  }

  val mateInSameStrand: Boolean = {
    record.getReadPairedFlag && record.getReadNegativeStrandFlag && record.getMateNegativeStrandFlag &&
    record.getReferenceIndex == record.getMateReferenceIndex
  }

  val mateOnOtherChromsome: Boolean = {
    record.getReadPairedFlag && record.getReferenceIndex != record.getMateReferenceIndex
  }
}
