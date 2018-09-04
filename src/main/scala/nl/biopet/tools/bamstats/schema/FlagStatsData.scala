package nl.biopet.tools.bamstats.schema

case class FlagStatsData(flagStats: Map[String, Long],
                         crossCounts: CrossCounts) {

  def validate(): Unit = {
    require(
      flagStats.keySet == expectedKeys,
      "FlagStatsData incompatible. Missing and/or unknown names in flagstats.\n" +
        s"Missing: ${(expectedKeys -- flagStats.keySet).mkString(",")}\n" +
        s"Unknown: ${(flagStats.keySet -- expectedKeys).mkString(",")}"
    )
    require(
      crossCounts.keys.toSet == flagStats.keySet,
      "FlagStatsData incompatible. Internally corrupt. CrossCount keys do not match flagstats keys.")
    crossCounts.validate()

    // This requirement should be evaluated last. Otherwise it will mask other errors,
    // which will lead to confusion.
    require(
      crossCounts.toFlagStatsMap == flagStats,
      "Internally corrupt FlagStatsData. The CrossCounts table totals do not equal the flagstats.")
  }
}
