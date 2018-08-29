package nl.biopet.tools.bamstats.merge

import nl.biopet.tools.bamstats.BamStats
import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

object Merge extends ToolCommand[Args] {
  def argsParser: AbstractOptParser[Args] = new ArgsParser(toolCommand = this)

  def emptyArgs: Args = Args()

  def main(args: Array[String]): Unit = {}

  def descriptionText: String =
    """
      |This module will merge bamstats files together and keep the sample/library/readgroup structure.
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
