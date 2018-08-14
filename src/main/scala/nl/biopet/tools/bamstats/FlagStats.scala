package nl.biopet.tools.bamstats

import htsjdk.samtools.SAMRecord

import scala.collection.mutable

class FlagStats(samRecords: Seq[SAMRecord]) {

  def flagStats: Map[FlagMethods.Value, Long] =
    FlagMethods.getFlagStats(samRecords)

  def crossCounts: Map[FlagMethods.Value, Map[FlagMethods.Value, Long]] =
    FlagMethods.getCrossCounts(samRecords)

  def report(): String = {
    val buffer = new mutable.StringBuilder()
    val stats = flagStats
    buffer.append("Number\tTotal Flags\tFraction\tName\n")
    val totalFlags: Option[Long] = stats.get(FlagMethods.Total)
    stats.foreach {
      case (method: FlagMethods.Value, count: Long) =>
        val percentage = totalFlags
          .map(totalCount => f"${(count.toDouble / totalCount) * 100}%.4f")
          .getOrElse("N/A")
        buffer.append(
          s"#${method.id}\t$count\t$percentage\t${method.outerEnum.toString()}\n")
    }
    buffer.toString()
  }

  def crossReport(
      crossCounts: Map[FlagMethods.Value, Map[FlagMethods.Value, Long]])
    : String = {
    val buffer = new mutable.StringBuilder()
    buffer.toString()
  }

  def +(other: FlagStats): FlagStats = {
    ???
  }

}
