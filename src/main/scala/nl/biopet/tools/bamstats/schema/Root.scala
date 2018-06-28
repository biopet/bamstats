package nl.biopet.tools.bamstats.schema

import java.io.File

import nl.biopet.utils.{conversions, io}
import play.api.libs.json._

case class Root(samples: Map[String, Sample], bamStats: Option[Aggregation]) {}
object Root {}
