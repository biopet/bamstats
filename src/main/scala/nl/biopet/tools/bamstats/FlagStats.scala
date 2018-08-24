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

  // orders flag methods by ID. Used to make sure array layouts are consistent
  private val orderedMethods: List[FlagMethods.Value] = {
    FlagMethods.values.toList.sortBy(_.id)
  }

  private val orderedNames: List[String] = orderedMethods.map(_.name)
  private val orderedKeys: List[Int] = orderedMethods.map(_.id)

  private val emptyFlagStats = Array.fill(orderedMethods.size)(0L)

  // Representation as arrays instead of maps is chosen because an index is a lot faster than a hashmap.
  // Given bam files may contain millions of reads, this is the most prudent option.
  private val flagStats: Array[Long] = emptyFlagStats
  private val crossCounts: Array[Array[Long]] =
    Array.fill(orderedMethods.size)(emptyFlagStats)

  /**
    * Method that returns a sorted list of flagstats. Can be used as a to map conversion
    * @return
    */
  def flagstatsSortedNames: List[(String, Long)] = {
    orderedNames.zip(flagStats)
  }

  def flagstatsSortedMethods: List[(FlagMethods.Value, Long)] = {
    orderedMethods.zip(flagStats)
  }

  def crossCountsSortedNames: List[(String, List[(String, Long)])] = {
    orderedNames.zip(crossCounts).map {
      case (name, stats) =>
        name -> orderedNames.zip(stats)
    }
  }

  def crossCountsSortedMethods
    : List[(FlagMethods.Value, List[(FlagMethods.Value, Long)])] = {
    orderedMethods.zip(crossCounts).map {
      case (method, stats) =>
        method -> orderedMethods.zip(stats)
    }
  }

  def totalReads = flagStats(FlagMethods.total.id)

  def loadRecord(record: SAMRecord): Unit = {
    // First check which indexes are positive for the flag
    val positiveResults: List[Int] = orderedMethods.flatMap { method =>
      if (method(record)) {
        Some(method.id)
      } else None
    }
    // Add 1 for the positive indexes. For the crosscounts also add 1 for the positive indexes.
    // With this method only the positive index positions are traversed over.
    // Negative index positions are ignored, which should speed up things.
    positiveResults.foreach { index =>
      flagStats(index) += 1
      positiveResults.foreach { innerIndex =>
        crossCounts(index)(innerIndex) += 1
      }
    }
  }

  def +=(other: FlagStats): Unit = {
    orderedKeys.foreach { key =>
      this.flagStats(key) += other.flagStats(key)
      orderedKeys.foreach { innerKey =>
        this.crossCounts(key)(innerKey) += other.crossCounts(key)(innerKey)
      }
    }
  }

  def toSummaryMap(includeCrossCounts: Boolean = true): Map[String, Any] = {
    flagStatsToMap ++ {
      if (includeCrossCounts)
        Map("crossCounts" -> crossCountsToMap)
      else Map()
    } ++
      Map(
        "singletons" -> crossCounts(FlagMethods.mapped.id)(
          FlagMethods.mateUnmapped.id))

  }

  def flagStatsToMap: Map[String, Long] = flagstatsSortedNames.toMap

  def crossCountsToMap: Map[String, Map[String, Long]] = {
    crossCountsToMap.map {
      case (name, stats) => name -> stats
    }
  }

  def writeAsTsv(file: File): Unit = {
    val writer = new PrintWriter(file)
    flagstatsSortedNames.foreach {
      case (name, count) =>
        writer.println(s"$name\t$count")
    }
    writer.close()
  }

  def report(): String = {
    val buffer = new mutable.StringBuilder()

    buffer.append(s"Number\tTotal Flags\tPercentage\tName$lineSeparator")
    flagstatsSortedMethods
      .foreach {
        case (method: FlagMethods.Value, count: Long) =>
          val percentage = totalReads match {
            case 0 => "N/A"
            case _ =>
              "%.4f".formatLocal(Locale.US, (count.toDouble / totalReads) * 100) + "%"
          }
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
    crossCountsSortedMethods
      .foreach {
        case (method, _) =>
          buffer.append(s"\t#${method.id}")
      }
    buffer.append(lineSeparator)

    crossCountsSortedMethods.foreach {
      case (method, countsList) =>
        // Create a prefix to the counts line
        buffer.append(s"#${method.id}")
        // Get the total number of counts if we need the percentage later

        // Foreach count get the percentage or count. End the line with a line separator.
        countsList.foreach {
          case (_, count) =>
            if (fraction) {
              val percentage = totalReads match {
                case 0 => "N/A"
                case _ =>
                  "%.4f".formatLocal(Locale.US,
                                     (count.toDouble / totalReads) * 100) + "%"
              }
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
