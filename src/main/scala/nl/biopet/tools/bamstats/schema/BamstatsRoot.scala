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

package nl.biopet.tools.bamstats.schema

import java.io.File

import htsjdk.samtools.SAMReadGroupRecord
import nl.biopet.tools.bamstats.GroupStats
import nl.biopet.tools.bamstats.schema.Implicits._
import nl.biopet.utils.{conversions, io}
import play.api.libs.json._

case class BamstatsRoot(samples: Map[String, Sample]) {

  /**
    * Flattens the BamstatsRoot object into a map of GroupIDs and ReadgroupData
    * @return a Map[GroupID, Readgroup]
    */
  def readgroups: Map[GroupID, Readgroup] = {
    samples.flatMap {
      case (sampleId: String, sampleData: Sample) =>
        sampleData.libraries.flatMap {
          case (libraryId: String, libraryData: Library) =>
            libraryData.readgroups.map {
              case (readgroupID: String, readgroupData: Readgroup) =>
                GroupID(sampleId, libraryId, readgroupID) -> readgroupData
            }
        }

    }
  }

  /**
    * Converts  this object to json
    * @return a JSvalue representing this object.
    */
  def toJson: JsValue = {
    Json.toJson(this)
  }

  /**
    * Write this object to a Json file
    * @param file the output file.
    */
  def writeFile(file: File): Unit = {
    io.writeLinesToFile(file, Json.stringify(toJson) :: Nil)
  }

  /**
    * Convert this object to a list of stats grouped by readgroup.
    * @return
    */
  def asStatsList: List[Stats] = {
    readgroups.map {
      case (readgroupID: GroupID, readgroupData: Readgroup) =>
        Stats(readgroupID, GroupStats.statsFromData(readgroupData.data))
    }
  }.toList

  /**
    * validate this object.
    */
  def validate(): Unit = readgroups foreach {
    case (_, rg) => rg.data.validate()
  }

  def +(other: BamstatsRoot): BamstatsRoot = {
    ???
  }

  def combinedStats: GroupStats = {
    val groupStatsIterator = samples.valuesIterator.flatMap {
      _.libraries.valuesIterator.flatMap {
        _.readgroups.valuesIterator.map { _.data.asGroupStats }
      }
    }
    groupStatsIterator.reduce(_ += _)
  }
}

object BamstatsRoot {

  /**
    * Create a new BamstatsRoot from json
    * @param json the JsValue containing the json information
    * @return A BamstatsRoot object
    */
  def fromJson(json: JsValue): BamstatsRoot = {
    Json.fromJson[BamstatsRoot](json) match {
      case JsSuccess(root: BamstatsRoot, _) => root
      case e: JsError =>
        throw new IllegalStateException(e.errors.mkString("\n"))
    }
  }

  /**
    * Create a new BamstatsRoot from a json file.
    * @param file a file in json format
    * @return a BamstatsRoot object
    */
  def fromFile(file: File): BamstatsRoot = {
    fromJson(conversions.fileToJson(file))
  }

  /**
    * Uses a groupID and groupstats to create a new BamstatsRoot
    * @param groupID the unique identifier consisting of a sample, library and readgroup ID.
    * @param groupStats the stats for the readgroup
    * @return a BamstatsRoot object
    */
  def fromGroupStats(groupID: GroupID, groupStats: GroupStats): BamstatsRoot = {
    fromStats(Stats(groupID, groupStats))
  }

  /**
    * Converts a stats object into a BamstatsRoot object
    * @param stats a Stats object containing a group ID and readgroupData
    * @return a BamstatsRoot object
    */
  def fromStats(stats: Stats): BamstatsRoot = {
    BamstatsRoot(
      Map(
        stats.groupID.sample ->
          Sample(
            Map(stats.groupID.library ->
              Library(Map(stats.groupID.readgroup ->
                Readgroup(stats.stats.statsToData())))))))
  }

  /**
    * Converts a list of stats to a bamstatsRoots object. Merges stats from the
    * same readgroup
    * @param groups A list of stats objects
    * @return a BamstatsRoot object
    */
  def fromStatsList(groups: List[Stats]): BamstatsRoot = {
    BamstatsRoot(
      groups.groupBy(_.groupID.sample).map {
        case (sampleID, sampleGroups) =>
          sampleID -> Sample(
            sampleGroups.groupBy(_.groupID.library).map {
              case (libraryID, libraryGroups) =>
                libraryID -> Library(
                  libraryGroups.groupBy(_.groupID.readgroup).map {
                    case (readgroupID, readgroups) =>
                      val statsList: List[GroupStats] = readgroups.map(_.stats)
                      val addedStats: GroupStats = statsList.reduce(_ += _)
                      readgroupID -> Readgroup(addedStats.statsToData())
                  }
                )
            }
          )
      }
    )
  }

}
