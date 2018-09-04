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

package nl.biopet.tools.bamstats.generate

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('R', "reference") valueName "<file>" action { (x, c) =>
    c.copy(referenceFasta = Some(x))
  } text "Fasta file of reference"
  opt[File]('o', "outputDir") required () valueName "<directory>" action {
    (x, c) =>
      c.copy(outputDir = x)
  } text "Output directory"
  opt[File]('b', "bam") required () valueName "<file>" action { (x, c) =>
    c.copy(bamFile = x)
  } text "Input bam file"
  opt[File]("bedFile") valueName "<file>" action { (x, c) =>
    c.copy(bedFile = Some(x))
  } text "Extract information for the regions specified in the bedfile."
  opt[Unit]("scatterMode") action { (_, c) =>
    c.copy(scatterMode = true)
  } text "Exclude reads from which the start originates from another region. " +
    "This is useful for running multiple instances of bamstats each on a different region. " +
    "The files can be merged afterwards without duplicates."
  // How should this option be adequately named?
  opt[Unit]('u', "onlyUnmapped")
    .action((_, c) => c.copy(onlyUnmapped = true))
    .text(
      "Only returns stats on unmapped reads. (This is excluding singletons.")
  opt[Unit]("tsvOutputs") action { (_, c) =>
    c.copy(tsvOutputs = true)
  } text "Also output tsv files, default there is only a json"
  opt[String]("defaultSample")
    .valueName("<name>")
    .action((x, c) => c.copy(defaultSample = Some(x)))
    .text(
      "Fallback value in case sample names are not defined for each readgoup in the SAM/BAM file." +
        " If the fallback is not specified the program will throw an error when it finds a readgroup without a sample name.")
  opt[String]("library")
    .valueName("<name>")
    .required()
    .action((x, c) => c.copy(defaultLibrary = Some(x)))
    .text("Fallback value in case library names are not defined for each readgoup in the SAM/BAM file." +
      " If the fallback is not specified the program will throw an error when it finds a readgroup without a library name.")
}
