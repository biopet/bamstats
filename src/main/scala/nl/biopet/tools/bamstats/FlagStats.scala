package nl.biopet.tools.bamstats

import java.io.{File, PrintWriter}
import htsjdk.samtools.SAMRecord
import nl.biopet.utils.conversions
import play.api.libs.json.Json
import scala.collection.mutable
import util.Properties.lineSeparator

class FlagStats {

  private val flagStats: mutable.Map[FlagMethods.Value, Long] =
    FlagMethods.emptyResult
  private val crossCounts
    : mutable.Map[FlagMethods.Value, mutable.Map[FlagMethods.Value, Long]] =
    FlagMethods.emptyCrossResult

  def flagstatsSorted: List[(FlagMethods.Value, Long)] = {
    flagStats.toList.sortBy { case (method, _) => method.id }
  }

  def crossCountsSorted
    : List[(FlagMethods.Value, List[(FlagMethods.Value, Long)])] = {
    crossCounts.toList.sortBy { case (method, _) => method.id }.map {
      case (method, countsMap) =>
        val sortedMap = countsMap.toList.sortBy {
          case (method, _) => method.id
        }
        (method, sortedMap)
    }
  }
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

  def +=(other: FlagStats): Unit = {
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

    buffer.append(s"Number\tTotal Flags\tFraction\tName$lineSeparator")
    val totalFlags: Option[Long] = flagStats.get(FlagMethods.Total)
    flagStats.toList
      .sortBy { case (method, _) => method.id } // Sort by order of defining (?)
      .foreach {
        case (method: FlagMethods.Value, count: Long) =>
          val percentage = totalFlags
            .map(totalCount => f"${(count.toDouble / totalCount) * 100}%.4f")
            .getOrElse("N/A")
          buffer.append(
            s"#${method.id}\t$count\t$percentage\t${method.outerEnum.toString()}$lineSeparator")
      }
    buffer.append(lineSeparator)

    buffer.append(crossReport() + lineSeparator)
    buffer.append(crossReport(fraction = true) + lineSeparator)

    buffer.toString()
  }

  /**
    * This returns a tsv table. All the flag ids are in the first row and the first column.
    * The table body contains counts
    * @param fraction use percentages instead of counts
    * @return a tsv table
    */
  def crossReport(fraction: Boolean = false): String = {
    val buffer = new StringBuilder
    val crossCountsSorted = crossCounts.toList.sortBy {
      case (method, _) => method.id
    }

    // Create header line
    crossCountsSorted
      .foreach {
        case (method, _) =>
          buffer.append(s"\t#${method.id}")
      }
    buffer.append(lineSeparator)
    crossCountsSorted.foreach {
      case (method, countsMap) =>
        // Create a prefix to the counts line
        buffer.append(s"#${method.id}")
        // Get the total number of counts if we need the percentage later
        val totalCount: Option[Long] =
          if (fraction) countsMap.get(FlagMethods.Total) else None
        // Foreach count get the percentage or count. End the line with a line separator.
        countsMap.toList.sortBy { case (method, _) => method.id }.foreach {
          case (_, count) => {
            if (fraction) {
              val percentage = totalCount
                .map(total => f"${((count.toFloat / total) * 100)}%.4f" + "%")
                .getOrElse("N/A")
              buffer.append(s"\t$percentage")
            } else {
              buffer.append(s"\t$count")
            }
          }
          buffer.append(lineSeparator)
        }
    }
    buffer.toString()
  }

  def writeReportToFile(outputFile: File): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println(report)
    writer.close()
  }

  /**
    *
    * @return A json string with the summary
    */
  def summary: String = {
    val map = flagStats.toList.sortBy { case (method, _) => method.id }.map {
      case (method, count) =>
        (method.outerEnum.toString() ->)
    }
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
