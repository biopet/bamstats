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
import nl.biopet.utils.tool.ToolCommand
import nl.biopet.utils.tool.multi.MultiToolCommand
import nl.biopet.tools.bamstats.generate.Generate
import nl.biopet.tools.bamstats.merge.Merge

object BamStats extends MultiToolCommand {
  def subTools: Map[String, List[ToolCommand[_]]] = Map(
    "Mode" -> List(Generate, Merge)
  )

  def descriptionText: String =
    s"""$toolName is a package that contains tools
       |to generate stats from a BAM file,
       |merge those stats for multiple samples,
       |and validate the generated stats files.
       |
     """.stripMargin + extendedDescriptionText
  def manualText: String = extendedManualText

  def exampleText: String = extendedExampleText
}
