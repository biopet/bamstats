/*
 * Copyright (c) 2014 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.bamstats

import java.io.{File, PrintWriter}
import java.util.Locale

import htsjdk.samtools.SAMRecord
import nl.biopet.tools.bamstats.schema.FlagStatsData
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
          case (innerMethod, _) => innerMethod.id
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

  def ==(other: FlagStats): Boolean = {
    this.flagStats == other.flagStats &&
    this.crossCounts == other.crossCounts
  }

  def addFlagStatsData(flagStatsData: FlagStatsData): Unit = {
    val newFlagStats = flagStatsData.flagStats.map {
      case (name, count) =>
        FlagMethods.nameToVal(name) -> count
    }
    require(newFlagStats.keySet == flagStats.keySet,
            "Imported FlagStat data incompatible.")

    val newCrossCounts: Map[FlagMethods.Value, Map[FlagMethods.Value, Long]] =
      flagStatsData.crossCounts.map {
        case (name, map) =>
          val innerMap = map.map {
            case (name, count) => FlagMethods.nameToVal(name) -> count
          }
          require(innerMap.keySet == flagStats.keySet,
                  "Imported crossCount data incompatible")
          FlagMethods.nameToVal(name) -> innerMap
      }
    require(newCrossCounts.keySet == crossCounts.keySet,
            "Imported crossCount data incompatible")

    newFlagStats.foreach {
      case (method, count) =>
        this.flagStats(method) += count
    }
    newCrossCounts.foreach {
      case (method, map) =>
        map.foreach {
          case (innerMethod, count) =>
            this.crossCounts(method)(innerMethod) += count
        }
    }
  }

  def toFlagStatsData(): FlagStatsData = {
    new FlagStatsData(flagStats = FlagMethods.flagStatsToMap(flagStats),
                      FlagMethods.crossCountsToMap(crossCounts))
  }

  def toSummaryMap(includeCrossCounts: Boolean = true): Map[String, Any] = {
    FlagMethods.flagStatsToMap(flagStats) ++ {
      if (includeCrossCounts)
        Map("crossCounts" -> FlagMethods.crossCountsToMap(crossCounts))
      else Map()
    } ++
      Map(
        "singletons" -> crossCounts(FlagMethods.mapped)(
          FlagMethods.mateUnmapped))

  }

  def writeAsTsv(file: File): Unit = {
    val writer = new PrintWriter(file)
    flagstatsSorted.foreach {
      case (flag, count) =>
        writer.println(s"${flag.name}\t$count")
    }
    writer.close()
  }

  def report(): String = {
    val buffer = new mutable.StringBuilder()

    buffer.append(s"Number\tTotal Flags\tPercentage\tName$lineSeparator")
    val totalFlags: Option[Long] = flagStats.get(FlagMethods.total)
    flagstatsSorted
      .foreach {
        case (method: FlagMethods.Value, count: Long) =>
          val percentage = totalFlags
            .map(
              totalCount =>
                "%.4f".formatLocal(Locale.US,
                                   (count.toDouble / totalCount) * 100) + "%")
            .getOrElse("N/A")
          buffer.append(
            s"#${method.id}\t$count\t$percentage\t${method.name}$lineSeparator")
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
    // Create header line
    crossCountsSorted
      .foreach {
        case (method, _) =>
          buffer.append(s"\t#${method.id}")
      }
    buffer.append(lineSeparator)

    val totalCount: Option[Long] =
      if (fraction) flagStats.toMap.get(FlagMethods.total) else None
    crossCountsSorted.foreach {
      case (method, countsList) =>
        // Create a prefix to the counts line
        buffer.append(s"#${method.id}")
        // Get the total number of counts if we need the percentage later

        // Foreach count get the percentage or count. End the line with a line separator.
        countsList.foreach {
          case (_, count) =>
            if (fraction) {
              val percentage =
                totalCount
                  .map(
                    total =>
                      "%.4f".formatLocal(Locale.US,
                                         (count.toFloat / total) * 100) + "%")
                  .getOrElse("N/A")
              buffer.append(s"\t$percentage")
            } else {
              buffer.append(s"\t$count")
            }
        }
        buffer.append(lineSeparator)
    }
    buffer.toString()
  }

  def writeReportToFile(outputFile: File): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println(report())
    writer.close()
  }

  /**
    *
    * @return A json string with the summary
    */
  def toSummaryJson(includeCrossCounts: Boolean = true): String = {
    Json.stringify(conversions.mapToJson(toSummaryMap(includeCrossCounts)))
  }

  def writeSummaryToFile(outputFile: File,
                         includeCrossCounts: Boolean = true): Unit = {
    val writer = new PrintWriter(outputFile)
    writer.println(toSummaryJson(includeCrossCounts))
    writer.close()
  }
}

object FlagStats {
  def fromFlagStatsData(flagStatsData: FlagStatsData): FlagStats = {
    val newFlagStats = new FlagStats
    newFlagStats.addFlagStatsData(flagStatsData)
    newFlagStats
  }
}
