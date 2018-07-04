package nl.biopet.tools.bamstats.schema

import nl.biopet.utils.Counts

case class Data(mappingQualityHistogram: Counts.DoubleArray[Int],
                insertSizeHistogram: Counts.DoubleArray[Int],
                clippingHistogram: Counts.DoubleArray[Int],
                leftClippingHistogram: Counts.DoubleArray[Int],
                rightClippingHistogram: Counts.DoubleArray[Int],
                _5_ClippingHistogram: Counts.DoubleArray[Int],
                _3_ClippingHistogram: Counts.DoubleArray[Int]) {

//  def +(other: Data): Data = {
//    new Data(
//      mappingQualityHistogram = addHistogram(this.mappingQualityHistogram,
//                                             other.mappingQualityHistogram),
//      insertSizeHistogram =
//        addHistogram(this.insertSizeHistogram, other.insertSizeHistogram),
//      clippingHistogram =
//        addHistogram(this.clippingHistogram, other.clippingHistogram),
//      leftClippingHistogram =
//        addHistogram(this.leftClippingHistogram, other.leftClippingHistogram),
//      rightClippingHistogram =
//        addHistogram(this.rightClippingHistogram, other.rightClippingHistogram),
//      _5_ClippingHistogram =
//        addHistogram(this._5_ClippingHistogram, other.rightClippingHistogram),
//      _3_ClippingHistogram =
//        addHistogram(this._3_ClippingHistogram, other._3_ClippingHistogram)
//    )
//  }
  def validate(): Unit = ???
  def addHistogram(map1: Map[Int, Long],
                   map2: Map[Int, Long]): Map[Int, Long] = {
    {
      map1 ++ map2.map {
        case (key, value) => key -> (value + map1.getOrElse(key, 0L))
      }
    }
  }
}
