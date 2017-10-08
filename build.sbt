organization := "com.github.biopet"
name := "bamstats"

scalaVersion := "2.11.11"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.github.biopet" %% "biopet-tool-utils" % "0.1.0-SNAPSHOT" changing()
libraryDependencies += "com.github.biopet" %% "biopet-ngs-utils" % "0.1.0-SNAPSHOT" changing()
libraryDependencies += "com.github.biopet" %% "biopet-config-utils" % "0.1.0-SNAPSHOT" changing()

libraryDependencies += "com.github.biopet" %% "biopet-test-utils" % "0.1.0-SNAPSHOT" % Test changing()
libraryDependencies += "com.google.guava" % "guava" % "18.0" % Test

mainClass in assembly := Some("nl.biopet.tools.bamstats.BamStats")

useGpg := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
