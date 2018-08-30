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

package nl.biopet.tools.bamstats.merge

import nl.biopet.tools.bamstats.{BamStats, Stats}
import nl.biopet.tools.bamstats.schema.Root
import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

object Merge extends ToolCommand[Args] {
  def argsParser: AbstractOptParser[Args] = new ArgsParser(toolCommand = this)

  def emptyArgs: Args = Args()

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    val statsList: List[Stats] =
      cmdArgs.inputFiles.flatMap(Root.fromFile(_).asStats)
    val root = Root.fromStats(statsList)
    root.validate()

    cmdArgs.outputFile.foreach(root.writeFile)
  }

  def descriptionText: String =
    """
      |This module will merge bamstats files together and keep the sample/library/readgroup structure.
      |It will also validate the resulting file.
      """.stripMargin

  def manualText: String =
    s"""
       |When merging the files ${BamStats.toolName} will validate the input files and the output files.
       |If aggregation values can not be regenerated the file is considered corrupt.
    """.stripMargin

  def exampleText: String =
    s"""
       |Merging multiple file
       |${BamStats.example("merge," +
                             "-i",
                           "<seqstat file>",
                           "-i",
                           "<seqstats file>",
                           "-o",
                           "<output file>")}
     """.stripMargin
}
