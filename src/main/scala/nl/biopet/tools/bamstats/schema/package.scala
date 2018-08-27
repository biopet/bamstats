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

package nl.biopet.tools.bamstats

package object schema {
  case class Sample(libraries: Map[String, Library])
  case class Library(readgroups: Map[String, Readgroup])
  case class Readgroup(data: Data)
  case class FlagStatsData(flagStats: Map[String, Long],
                           crossCounts: CrossCounts) {
    def validate(): Unit = {
      require(
        flagStats.keySet == FlagMethods.values.map(_.name),
        "FlagStatsData incompatible. Missing or unknown names in flagstats")
      require(
        crossCounts.keys.toSet == flagStats.keySet,
        "FlagStatsData incompatible. Internally corrupt. CrossCount keys do not match flagstats keys.")
      crossCounts.validate()
    }
  }
  case class CrossCounts(keys: List[String], counts: List[List[Long]]) {
    def validate(): Unit = {
      // Test whether the keys match keys known by te program
      val totalIndex = keys.indexOf(FlagMethods.total.name)

      // Test whether the matrix of counts is a true square.
      val numberOfMethods = keys.length
      require(counts.length == numberOfMethods,
              "Number of rows not equal to number of methods.")
      counts.zipWithIndex.foreach {
        case (row, index) =>
          require(
            row.length == numberOfMethods,
            s"Number of columns not equal to number of methods on row $index")
      }

      // Test whether the crossline (condition A and condition A is true)
      // Matches with the total value (condition A and always true is true)
      // These values should always match. Otherwise the matrix is wrongly constructed.
      val totalsColumn: List[Long] = counts.map(_(totalIndex))
      for (index <- 0 until keys.length) {
        require(totalsColumn(index) == counts(index)(index),
                s"Crossline at ($index,$index) is not equal to" +
                  s" the value in the totals column at ($index,$totalIndex)")
      }
    }
  }
}
