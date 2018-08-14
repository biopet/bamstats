package nl.biopet.tools.bamstats

import java.io.File

import htsjdk.samtools.SAMRecord

import scala.collection.mutable

class FlagStats {

  private val flagStats: mutable.Map[FlagMethods.Value, Long] =
    FlagMethods.emptyResult
  private val crossCounts
    : mutable.Map[FlagMethods.Value, mutable.Map[FlagMethods.Value, Long]] =
    FlagMethods.emptyCrossResult

  def loadRecord(record: SAMRecord): Unit = {
    flagStats.keys.foreach { method =>
      if (method.method(record)) {
        flagStats(method) += 1
        crossCounts(method).keys.foreach(cross => {
          if (cross.method(record)) crossCounts(method)(cross) += 1
        })
      }
    }
  }

  def toSummaryMap() = ???
  def writeReportToFile(file: File) = ???
  def writeSummaryToFile(file: File) = ???

  def report(): String = {
    val buffer = new mutable.StringBuilder()

    buffer.append("Number\tTotal Flags\tFraction\tName\n")
    val totalFlags: Option[Long] = flagStats.get(FlagMethods.Total)
    flagStats.foreach {
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

  def +=(other: FlagStats): FlagStats = {
    ???
  }

}
