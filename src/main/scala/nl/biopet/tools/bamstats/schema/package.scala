package nl.biopet.tools.bamstats

package object schema {
  case class Flagstat()
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
