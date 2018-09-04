package nl.biopet.tools.bamstats.schema

import nl.biopet.tools.bamstats.FlagMethods

case class CrossCounts(keys: List[String], counts: List[List[Long]]) {
  val totalIndex: Int = keys.indexOf(FlagMethods.total.name)
  val totalNumberOfReads: Long = counts(totalIndex)(totalIndex)
  val totalsColumn: List[Long] = counts.map(_(totalIndex))

  /**
    * Returns the total values zipped with the keys. This should equal the flagstats data.
    * @return A map with names and counts
    */
  def toFlagStatsMap: Map[String, Long] = {
    keys.zip(totalsColumn).toMap
  }

  /**
    * Validate the crosscounts table.
    */
  def validate(): Unit = {
    // Test whether the keys match keys known by te program
    // If the tests from FlagstatsData have succeeded, this should always succeed.
    require(
      keys.toSet == expectedKeys,
      "FlagStatsData incompatible. Missing and/or unknown names in crosscounts.\n" +
        s"Missing: ${(expectedKeys -- keys.toSet).mkString(",")}\n" +
        s"Unknown: ${(keys.toSet -- expectedKeys).mkString(",")}"
    )

    // Test whether the matrix of counts is a true square.
    val numberOfMethods = keys.length
    require(
      counts.length == numberOfMethods,
      s"Number of rows (${counts.length}) not equal to number of methods ($numberOfMethods).")
    counts.zipWithIndex.foreach {
      case (row, index) =>
        require(
          row.length == numberOfMethods,
          // Added +1 to the index because the fifth row is "5" for humans, not "4".
          s"Number of columns (${row.length}) not equal to number of methods ($numberOfMethods) on row ${index + 1}"
        )
    }

    // Test whether the crossline (condition A and condition A is true)
    // Matches with the total value (condition A and always true is true)
    // These values should always match. Otherwise the matrix is wrongly constructed.
    // Use index +1 for human readable matrix notation.
    for (index <- keys.indices) {
      require(
        totalsColumn(index) == counts(index)(index),
        s"Crossline at (${index + 1},${index + 1})" +
          s" with value '${counts(index)(index)}' is not equal to" +
          s" the value in the totals column at (${index + 1},${totalIndex + 1})" +
          s" with value '${counts(index)(totalIndex)}'"
      )
    }
  }

}
