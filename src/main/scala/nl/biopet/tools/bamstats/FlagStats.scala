package nl.biopet.tools.bamstats

class FlagStats(reads: Seq[ReadProperties]) {
  lazy val all: Int = reads.length
  lazy val mapped: Int = reads.count(_.mapped)
  lazy val duplicates: Int = reads.count(_.duplicate)
  lazy val firstOfPair: Int = reads.count(_.firstOfPair)
  lazy val secondOfPair: Int = reads.count(_.secondOfPair)
  lazy val readNegativeStrand: Int = reads.count(_.readNegativeStrand)
  lazy val notPrimaryAlignment: Int = reads.count(_.notPrimaryAlignment)
  lazy val readPaired: Int = reads.count(_.readPaired)
  lazy val properPair: Int = reads.count(_.properPair)
  lazy val mateNegativeStrand: Int = reads.count(_.mateNegativeStrand)
  lazy val mateUnmapped: Int = reads.count(_.mateUnmapped)
  lazy val readFailsVendorQualityCheck: Int = reads.count(_.readFailsVendorQualityCheck)
  lazy val supplementaryAlignment: Int = reads.count(_.supplementaryAlignment)
  lazy val secondaryOrSupplementary: Int = reads.count(_.secondaryOrSupplemantary)
}
