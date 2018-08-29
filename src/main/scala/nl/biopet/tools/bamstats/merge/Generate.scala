package nl.biopet.tools.bamstats.merge

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

object Generate extends ToolCommand[Args] {
  def argsParser: AbstractOptParser[Args] = new ArgsParser(toolCommand = this)

  def emptyArgs: Args = Args()

  def main(args: Array[String]): Unit = {
  }

  def descriptionText: String = ???

  def manualText: String = ???

  def exampleText: String = ???
}
