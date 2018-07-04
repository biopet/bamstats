package nl.biopet.tools.bamstats.schema

import play.api.libs.json._
import java.io.File

import nl.biopet.tools.bamstats.{GroupID, GroupStats, Stats}
import nl.biopet.utils.{conversions, io}
import play.api.libs.json._

case class Root(samples: Map[String, Sample], bamStats: Option[Aggregation]) {
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

  def validate(): Unit = {
    readgroups.foreach {
      case (_, readgroupData) => readgroupData.data.validate()
    }
  }
  def +(other: Root): Root = {
    ???
  }
}
object Root {
  def fromJson(json: JsValue): Root = {
    Json.fromJson[Root](json) match {
      case JsSuccess(root: Root, path: JsPath) => root
      case e: JsError =>
        throw new IllegalStateException(e.errors.mkString("\n"))
    }
  }

  def fromFile(file: File): Root = {
    fromJson(conversions.fileToJson(file))
  }

  def fromGroupStats(groups: List[Stats]): Root = ???

}
