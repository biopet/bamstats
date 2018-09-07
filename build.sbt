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
            url = url("https://github.com/ffinfo")),
  Developer(id = "rhpvorderman",
            name = "Ruben Vorderman",
            email = "r.h.p.vorderman@lumc.nl",
            url = url("https://github.com/rhpvorderman"))
)

biocondaTestCommands := Seq(
  s"${biocondaCommand.value} merge --help",
  s"${biocondaCommand.value} merge --version",
  s"${biocondaCommand.value} validate --help",
  s"${biocondaCommand.value} validate --version",
  s"${biocondaCommand.value} generate --help",
  s"${biocondaCommand.value} generate --version"
)
scalaVersion := "2.11.12"

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.6"
libraryDependencies += "com.github.biopet" %% "ngs-utils" % "0.6"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.3"
libraryDependencies += "com.google.guava" % "guava" % "18.0" % Test
