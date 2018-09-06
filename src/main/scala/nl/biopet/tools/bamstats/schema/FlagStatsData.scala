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
