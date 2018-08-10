package nl.biopet.tools.bamstats

class FlagStats(reads: Seq[ReadProperties]) {
  lazy val all: Long = reads.length
  lazy val mapped: Long = reads.count(_.mapped)
  lazy val duplicate: Long = reads.count(_.duplicate)
  lazy val firstOfPair: Long = reads.count(_.firstOfPair)
  lazy val secondOfPair: Long = reads.count(_.secondOfPair)
  lazy val readNegativeStrand: Long = reads.count(_.readNegativeStrand)
  lazy val notPrimaryAlignment: Long = reads.count(_.notPrimaryAlignment)
  lazy val readPaired: Long = reads.count(_.readPaired)
  lazy val properPair: Long = reads.count(_.properPair)
  lazy val mateNegativeStrand: Long = reads.count(_.mateNegativeStrand)
  lazy val mateUnmapped: Long = reads.count(_.mateUnmapped)
  lazy val readFailsVendorQualityCheck: Long =
    reads.count(_.readFailsVendorQualityCheck)
  lazy val supplementaryAlignment: Long = reads.count(_.supplementaryAlignment)
  lazy val secondaryOrSupplementary: Long =
    reads.count(_.secondaryOrSupplementary)
  lazy val firstNormalSecondInverted: Long =
    reads.count(_.firstNormalSecondInverted)
  lazy val firstNormalSecondNormal: Long =
    reads.count(_.firstNormalSecondNormal)
  lazy val firstInvertedSecondInverted: Long =
    reads.count(_.firstInvertedSecondInverted)
  lazy val firstInvertedSecondNormal: Long =
    reads.count(_.firstInvertedSecondNormal)
  lazy val mateInSameStrand: Long = reads.count(_.mateInSameStrand)
  lazy val mateOnOtherChromosome: Long = reads.count(_.mateOnOtherChromosome)

  def toMap: Map[String, Long] =
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
  def toCrossCounts: Map[String, Map[String, Long]] = {
    val keys = reads.headOption.map(_.map.keys).getOrElse(Seq())
    keys
      .map(
        x => x -> new FlagStats(reads.filter(_.map(x))).toMap
      )
      .toMap
  }
}
