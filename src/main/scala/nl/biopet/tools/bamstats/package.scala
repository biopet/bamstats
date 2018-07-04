package nl.biopet.tools

package object bamstats {
  case class GroupID(sample: String, library: String, readgroup: String)
  case class Stats(groupID: GroupID, stats: GroupStats)
}
