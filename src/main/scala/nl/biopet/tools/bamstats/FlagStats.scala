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
import nl.biopet.utils.conversions
import play.api.libs.json.Json

import scala.collection.mutable
import util.Properties.lineSeparator

class FlagStats {

  private val orderedMethodKeys: List[FlagMethods.Value] = {
    FlagMethods.values.toList.sortBy(_.id)
  }

  private val emptyFlagStats = Array.fill(orderedMethodKeys.size)(0L)
  private val flagStats: Array[Long] = emptyFlagStats
  private val crossCounts: Array[Array[Long]] = Array.fill(orderedMethodKeys.size)(emptyFlagStats)



  def flagstatsSorted: List[(FlagMethods.Value, Long)] = {
    orderedMethodKeys.zip(flagStats)
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

  def toSummaryMap(includeCrossCounts: Boolean = true): Map[String, Any] = {
    flagStatsToMap ++ {
      if (includeCrossCounts)
        Map("crossCounts" -> crossCountsToMap)
      else Map()
    } ++
      Map(
        "singletons" -> crossCounts(FlagMethods.mapped)(
          FlagMethods.mateUnmapped))

  }

  private def flagStatsMapper(
      mutableFlagStats: mutable.Map[FlagMethods.Value, Long])
    : Map[String, Long] = {
    mutableFlagStats.map {
      case (method, count) =>
        method.name -> count
    }.toMap
  }

  def flagStatsToMap: Map[String, Long] = flagStatsMapper(flagStats)

  def crossCountsToMap: Map[String, Map[String, Long]] = {
    crossCounts.map {
      case (method, flagstats) =>
        method.name -> flagStatsMapper(flagstats)
    }.toMap
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
