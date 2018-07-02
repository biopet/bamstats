package nl.biopet.tools.bamstats.schema

case class Data(mappingQualityHistogram: Map[Int, Long],
                insertSizeHistogram: Map[Int, Long],
                clippingHistogram: Map[Int, Long],
                leftClippingHistogram: Map[Int, Long],
                rightClippingHistogram: Map[Int, Long],
                _5_ClippingHistogram: Map[Int, Long],
                _3_ClippingHistogram: Map[Int, Long]) {

  def +(other: Data): Data = {
    new Data(
      mappingQualityHistogram = addHistogram(this.mappingQualityHistogram,
                                             other.mappingQualityHistogram),
      insertSizeHistogram =
        addHistogram(this.insertSizeHistogram, other.insertSizeHistogram),
      clippingHistogram =
        addHistogram(this.clippingHistogram, other.clippingHistogram),
      leftClippingHistogram =
        addHistogram(this.leftClippingHistogram, other.leftClippingHistogram),
      rightClippingHistogram =
        addHistogram(this.rightClippingHistogram, other.rightClippingHistogram),
      _5_ClippingHistogram =
        addHistogram(this._5_ClippingHistogram, other.rightClippingHistogram),
      _3_ClippingHistogram =
        addHistogram(this._3_ClippingHistogram, other._3_ClippingHistogram)
    )
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
