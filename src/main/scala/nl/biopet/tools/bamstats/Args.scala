package nl.biopet.tools.bamstats

import java.io.File

case class Args(outputDir: File = null,
                bamFile: File = null,
                referenceFasta: Option[File] = None,
                binSize: Int = 10000,
                threadBinSize: Int = 1000000,
                tsvOutputs: Boolean = false)
