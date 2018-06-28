package nl.biopet.tools.bamstats

package object schema {
  case class Sample(libraries: Map[String, Library],
                    seqstat: Option[Aggregation])
  case class Library(readgroups: Map[String, Readgroup],
                     seqstat: Option[Aggregation])
  case class Aggregation()
  case class Readgroup(bamStats: Data)

}
