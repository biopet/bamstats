package nl.biopet.tools.bamstats.schema

import java.io.File
import nl.biopet.utils.io
import nl.biopet.tools.bamstats.GroupStats
import nl.biopet.utils.Counts
import play.api.libs.json.{JsValue, Json}
import nl.biopet.tools.bamstats.schema.Implicits._
import nl.biopet.utils.Counts.Implicits._
case class Data(flagStats: FlagStats,
                mappingQualityHistogram: Counts.DoubleArray[Int],
                insertSizeHistogram: Counts.DoubleArray[Int],
                clippingHistogram: Counts.DoubleArray[Int],
                leftClippingHistogram: Counts.DoubleArray[Int],
                rightClippingHistogram: Counts.DoubleArray[Int],
                _5_ClippingHistogram: Counts.DoubleArray[Int],
                _3_ClippingHistogram: Counts.DoubleArray[Int]) {

  def asGroupStats: GroupStats = GroupStats.statsFromData(this)

  def toJson: JsValue = {
    Json.toJson(this)
  }

  def writeFile(file: File): Unit = {
    io.writeLinesToFile(file, Json.stringify(toJson) :: Nil)
  }

  def addHistogram(map1: Map[Int, Long],
                   map2: Map[Int, Long]): Map[Int, Long] = {
    {
      map1 ++ map2.map {
        case (key, value) => key -> (value + map1.getOrElse(key, 0L))
      }
    }
  }
}
