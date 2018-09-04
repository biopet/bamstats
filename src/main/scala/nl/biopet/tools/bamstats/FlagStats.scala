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
import nl.biopet.tools.bamstats.schema.{CrossCounts, FlagStatsData}
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

  /**
    * Creates an empty flagstats array
    * @return An array of length FlagMethods.values.size and filled with 0
    */
  private def emptyFlagstatArray: Array[Long] =
    Array.fill(orderedMethods.size)(0L)

  // Representation as arrays instead of maps is chosen because an index is a lot faster than a hashmap.
  // Given bam files may contain millions of reads, this is the most prudent option.
  private val flagStats: Array[Long] = emptyFlagstatArray
  private val crossCounts: Array[Array[Long]] =
    Array.fill(orderedMethods.size)(emptyFlagstatArray)

  /**
    * Method to return flagstats with names.
    * @return a sorted list of flagstats with names
    */
  def flagstatsSortedNames: List[(String, Long)] = {
    orderedNames.zip(flagStats)
  }

  /**
    * Method to return flagstats with methods.
    * @return a sorted list of flagstats with methods
    */
  def flagstatsSortedMethods: List[(FlagMethods.Value, Long)] = {
    orderedMethods.zip(flagStats)
  }

  /**
    * Returns a crosscount table with names
    * @return a sorted list of names with crosscounts.
    */
  def crossCountsSortedNames: List[(String, List[(String, Long)])] = {
    orderedNames.zip(crossCounts).map {
      case (name, stats) =>
        name -> orderedNames.zip(stats)
    }
  }

  /***
    * Returns a crosscount table with methods
    * @return a sorted list of methods with crosscounts.
    */
  def crossCountsSortedMethods
    : List[(FlagMethods.Value, List[(FlagMethods.Value, Long)])] = {
    orderedMethods.zip(crossCounts).map {
      case (method, stats) =>
        method -> orderedMethods.zip(stats)
    }
  }

  /**
    * Gets the total number of reads by reading it from the flagstats array
    * @return the total number of reads.
    */
  def totalReads: Long = flagStats(FlagMethods.total.id)

  /**
    * Loads one SAMrecord and tests it for the flags.
    * Then updates the internal flagstats and crosscounts counters
    * @param record a SAMrecord
    */
  def loadRecord(record: SAMRecord): Unit = {
    // First check which index positions have a 'true' flag
    val positiveResults: List[Int] = orderedMethods.flatMap { method =>
      if (method.method(record)) {
        Some(method.id)
      } else None
    }
    // Only update the positions which have a true flag.
    // This is a lot faster than traversing over all the positions
    positiveResults.foreach { index =>
      flagStats(index) += 1
      positiveResults.foreach { innerIndex =>
        crossCounts(index)(innerIndex) += 1
      }
    }
  }

  /**
    * Add another flagstats instance
    * @param other another FlagStats instance
    */
  def +=(other: FlagStats): Unit = {
    require(this.orderedMethods == other.orderedMethods,
            "FlagMethods of two flagstat objects do not match. Cannot merge")
    orderedKeys.foreach { key =>
      this.flagStats(key) += other.flagStats(key)
      orderedKeys.foreach { innerKey =>
        this.crossCounts(key)(innerKey) += other.crossCounts(key)(innerKey)
      }
    }
  }

  /**
    * Method to add FlagStatsData which can be read from JSON
    * @param flagStatsData the FlagStatsData
    */
  def addFlagStatsData(flagStatsData: FlagStatsData): Unit = {
    flagStatsData.validate()

    // Add flagstats data by converting name string to index int.
    flagStatsData.flagStats.toList.foreach {
      case (name, count) =>
        this.flagStats(FlagMethods.nameToVal(name).id) += count
    }

    // Add crosccounts data.
    // First convert the keysList to list of indexes (int).
    val indexedCrossCountsKeys =
      flagStatsData.crossCounts.keys.map(FlagMethods.nameToVal(_).id)

    // Zip the rows with the correct indexes
    indexedCrossCountsKeys.zip(flagStatsData.crossCounts.counts) foreach {
      case (index, countsArray) =>
        // Zip the columns with the correct indexes
        indexedCrossCountsKeys.zip(countsArray).foreach {
          case (innerIndex, count) =>
            this.crossCounts(index)(innerIndex) += count
        }
    }
  }

  /**
    * Returns a FlagStatsData object that can be serialized into data.
    * @return a FlagStatsData object
    */
  def toFlagStatsData: FlagStatsData = {
    val crossCountsData: CrossCounts = CrossCounts(
      keys = orderedNames,
      counts = crossCounts.map(_.toList).toList
    )
    FlagStatsData(flagStatsToMap, crossCounts = crossCountsData)
  }

  /**
    * Create a summary map of the gathered counts
    * @param includeCrossCounts Whether Crosscounts should be included in the summary
    * @return a summary map
    */
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

  /**
    * Convert flagstats to a map
    * @return a map with method names as a key and counts as value.
    */
  def flagStatsToMap: Map[String, Long] = flagstatsSortedNames.toMap

  /**
    * Converts crosscounts to a map
    * @return a map with method names as a key and flagstats maps as values
    */
  def crossCountsToMap: Map[String, Map[String, Long]] = {
    crossCountsSortedNames.map {
      case (name, stats) => name -> stats.toMap
    }.toMap
  }

  def ==(other: FlagStats): Boolean = {
    this.flagStatsToMap == other.flagStatsToMap
    this.crossCountsToMap == other.crossCountsToMap
  }

  /**
    * Write a TSV file with names in the first column and counts in the second.
    * @param file the output file
    */
  def writeAsTsv(file: File): Unit = {
    val writer = new PrintWriter(file)
    flagstatsSortedNames.foreach {
      case (name, count) =>
        writer.println(s"$name\t$count")
    }
    writer.close()
  }

  /**
    * A report that includes a table of the flagstats including percentages.
    * Also includes crosscounts with raw numbers and percentages
    * @return a string containing the report
    */
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

  /**
    * Write the report to file
    * @param outputFile the file to which the report will be written.
    */
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

  /**
    * Write the summary map to a tsv file
    * @param outputFile the file
    * @param includeCrossCounts whether crosscunts should be included.
    */
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
