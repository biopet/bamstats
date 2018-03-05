organization := "com.github.biopet"
organizationName := "Biopet"

startYear := Some(2014)

name := "BamStats"
biopetUrlName := "bamstats"

biopetIsTool := true

mainClass in assembly := Some("nl.biopet.tools.bamstats.BamStats")

developers := List(
  Developer(id = "ffinfo",
            name = "Peter van 't Hof",
            email = "pjrvanthof@gmail.com",
            url = url("https://github.com/ffinfo"))
)

scalaVersion := "2.11.12"

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.3.1"
libraryDependencies += "com.github.biopet" %% "ngs-utils" % "0.3.1"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.2.1" % Test
libraryDependencies += "com.google.guava" % "guava" % "18.0" % Test
