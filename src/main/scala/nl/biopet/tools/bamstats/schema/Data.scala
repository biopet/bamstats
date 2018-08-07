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
import nl.biopet.utils.{io, DoubleArray}
import play.api.libs.json.{JsValue, Json}

case class Data(flagStats: FlagStats,
                mappingQualityHistogram: DoubleArray[Int],
                insertSizeHistogram: DoubleArray[Int],
                clippingHistogram: DoubleArray[Int],
                leftClippingHistogram: DoubleArray[Int],
                rightClippingHistogram: DoubleArray[Int],
                _5_ClippingHistogram: DoubleArray[Int],
                _3_ClippingHistogram: DoubleArray[Int]) {

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

  def validate(): Unit = {
    val groupStats = asGroupStats
    require(groupStats._3_ClippingHistogram.toDoubleArray == this._3_ClippingHistogram)
    require(groupStats._5_ClippingHistogram.toDoubleArray == this._5_ClippingHistogram)
    require(groupStats.clippingHistogram.toDoubleArray == this.clippingHistogram)
    require(groupStats.insertSizeHistogram.toDoubleArray == this.insertSizeHistogram)
    require(groupStats.leftClippingHistogram.toDoubleArray == this.leftClippingHistogram)
    require(groupStats.rightClippingHistogram.toDoubleArray == this.rightClippingHistogram)
    require(groupStats.mappingQualityHistogram.toDoubleArray == this.mappingQualityHistogram)
    //TODO: Add functions for flagstat here
  }
}
