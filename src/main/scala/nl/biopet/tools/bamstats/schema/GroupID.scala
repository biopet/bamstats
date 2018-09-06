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

import htsjdk.samtools.SAMReadGroupRecord

case class GroupID(sample: String, library: String, readgroup: String)
object GroupID {

  /**
    * Method to get a GroupID from a SAMReadgroupRecord
    * @param readgroup the SAMReadGroupRecord
    * @return a GroupID object
    */
  def fromSamReadGroup(readgroup: SAMReadGroupRecord): GroupID = {
    GroupID(
      sample = Option(readgroup.getSample)
        .getOrElse(
          throw new IllegalArgumentException(
            s"Sample not found on readgroup: $readgroup")),
      library = Option(readgroup.getLibrary)
        .getOrElse(
          throw new IllegalArgumentException(
            s"Library not found on readgroup: $readgroup")),
      readgroup = readgroup.getReadGroupId
    )
  }
}
