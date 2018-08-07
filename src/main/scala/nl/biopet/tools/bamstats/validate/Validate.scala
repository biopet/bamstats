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

package nl.biopet.tools.bamstats.validate

import nl.biopet.tools.bamstats.BamStats
import nl.biopet.tools.bamstats.schema.Root
import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}


object Validate extends ToolCommand[Args] {


  def argsParser: AbstractOptParser[Args] = new ArgsParser(toolCommand = this)

  def emptyArgs: Args = Args()

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    Root.fromFile(cmdArgs.inputFile).validate()

    logger.info("Done")
  }

  def descriptionText: String =
    s"""
       |Validates a ${BamStats.toolName} file.
     """.stripMargin

  def manualText: String =
    s"""
       |
     """.stripMargin

  def exampleText: String =
    s"""
       |Default:
       |${BamStats.example("validate", "-i", "<input file>")}
     """.stripMargin
}
