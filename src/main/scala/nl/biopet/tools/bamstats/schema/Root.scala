package nl.biopet.tools.bamstats.schema

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
              case (readgroupID, readgroupData) => GroupID(sampleId,libraryId,readgroupID) -> readgroupData
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

  def asStats: List[GroupStats] = {
    readgroups.map {
      case (readgroupID, readgroupData) => Stats(readgroupID, GroupStats.statsFromData(readgroupData.data
      ))
    }
  }
  def +(other: Root): Root = {
    ???
  }
}
object Root {

}
