package nl.biopet.tools.bamstats

package object schema {
  case class Flagstat()
  case class Sample(libraries: Map[String, Library],
                    seqstat: Option[Aggregation])
  case class Library(readgroups: Map[String, Readgroup],
                     seqstat: Option[Aggregation])
  case class Aggregation(bla: String)
  case class Readgroup(bamStats: Data)
}
