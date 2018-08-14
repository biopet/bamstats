package nl.biopet.tools.bamstats

import java.io.{File, PrintWriter}

import htsjdk.samtools.SAMRecord
import nl.biopet.utils.conversions
import play.api.libs.json.Json

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

  def +=(other: FlagStats) = {
    this.flagStats.keys.foreach { method =>
      this.flagStats(method) += other.flagStats(method)
      this.crossCounts(method).keys foreach (method2 => {
        this.crossCounts(method)(method2) += other.crossCounts(method)(method2)
      })
    }
  }

  def toSummaryMap: Map[String, Any] = {
    FlagMethods.flagStatsToMap(flagStats) ++ Map(
      "cross_counts" -> FlagMethods.crossCountsToMap(crossCounts))
  }

  def writeAsTsv(file: File): Unit = {
    val writer = new PrintWriter(file)
    flagStats.foreach {
      case (flag, count) =>
        writer.println(s"${flag.outerEnum.toString()}\t$count")
    }
    writer.close()
  }

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
    buffer.append("\n")

    buffer.append(crossReport() + "\n")
    buffer.append(crossReport(fraction = true) + "\n")

    buffer.toString()
  }

  def crossReport(fraction: Boolean = false)
    : String = {
    val buffer = new StringBuilder

    for (t <- 0 until names.size) // Header for table
      buffer.append("\t#" + (t + 1))
    buffer.append("\n")

    for (t <- 0 until names.size) {
      buffer.append("#" + (t + 1) + "\t")
      for (t2 <- 0 until names.size) {
        val reads = crossCounts(t)(t2)
        if (fraction) {
          val percentage = (reads.toFloat / totalCounts(t).toFloat) * 100
          buffer.append(f"$percentage%.4f" + "%")
        } else buffer.append(reads)
        if (t2 == names.size - 1) buffer.append("\n")
        else buffer.append("\t")
      }
    }
    buffer.toString()
  }


  def writeReportToFile(outputFile: File): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println(report)
    writer.close()
  }

  def summary: String = {
    val map = (for (t <- 0 until names.size) yield {
      names(t) -> totalCounts(t)
    }).toMap ++ Map(
      "Singletons" -> crossCounts(
        names.find(_._2 == "Mapped").map(_._1).getOrElse(-1))(
        names.find(_._2 == "MateUnmapped").map(_._1).getOrElse(-1)))

    Json.stringify(conversions.mapToJson(map))
  }

  def writeSummaryTofile(outputFile: File): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println(summary)
    writer.close()
  }
}
