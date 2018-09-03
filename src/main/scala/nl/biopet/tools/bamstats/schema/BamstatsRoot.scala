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

import nl.biopet.tools.bamstats.GroupStats
import nl.biopet.tools.bamstats.schema.Implicits._
import nl.biopet.utils.{conversions, io}
import play.api.libs.json._

case class BamstatsRoot(samples: Map[String, Sample]) {
  def readgroups: Map[GroupID, Readgroup] = {
    samples.flatMap {
      case (sampleId, sampleData) =>
        sampleData.libraries.flatMap {
          case (libraryId, libraryData) =>
            libraryData.readgroups.map {
              case (readgroupID, readgroupData) =>
                GroupID(sampleId, libraryId, readgroupID) -> readgroupData
            }
        }

    }
  }

  def toJson: JsValue = {
    Json.toJson(this)
  }

  def writeFile(file: File): Unit = {
    io.writeLinesToFile(file, Json.stringify(toJson) :: Nil)
  }

  def asStats: List[Stats] = {
    readgroups.map {
      case (readgroupID, readgroupData) =>
        Stats(readgroupID, GroupStats.statsFromData(readgroupData.data))
    }
  }.toList

  def validate(): Unit = readgroups foreach {
    case (_, rg) => rg.data.validate()
  }

}
object BamstatsRoot {
  def fromJson(json: JsValue): BamstatsRoot = {
    Json.fromJson[BamstatsRoot](json) match {
      case JsSuccess(root: BamstatsRoot, _) => root
      case e: JsError =>
        throw new IllegalStateException(e.errors.mkString("\n"))
    }
  }

  def fromFile(file: File): BamstatsRoot = {
    fromJson(conversions.fileToJson(file))
  }

  def fromGroupStats(groupID: GroupID, groupStats: GroupStats): BamstatsRoot = {
    fromStats(List(Stats(groupID, groupStats)))
  }

  def fromStats(groups: List[Stats]): BamstatsRoot = {
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
