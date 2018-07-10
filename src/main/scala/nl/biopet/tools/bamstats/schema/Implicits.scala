package nl.biopet.tools.bamstats.schema

import play.api.libs.json.{Json, Reads, Writes}
import nl.biopet.utils.Counts.Implicits._

object Implicits {
  implicit val singleFlagStatsReads: Reads[SingleFlagStats] =
    Json.reads[SingleFlagStats]
  implicit val combinedFlagStatsReads: Reads[CombinedFlagStats] =
    Json.reads[CombinedFlagStats]
  implicit val flagStatsReads: Reads[FlagStats] = Json.reads[FlagStats]
  implicit val dataReads: Reads[Data] = Json.reads[Data]
  implicit val readgroupReads: Reads[Readgroup] = Json.reads[Readgroup]
  implicit val libraryReads: Reads[Library] = Json.reads[Library]
  implicit val sampleReads: Reads[Sample] = Json.reads[Sample]
  implicit val rootReads: Reads[Root] = Json.reads[Root]

  implicit val singleFlagStatsWrites: Writes[SingleFlagStats] =
    Json.writes[SingleFlagStats]
  implicit val combinedFlagStatsWrites: Writes[CombinedFlagStats] =
    Json.writes[CombinedFlagStats]
  implicit val flagStatsWrites: Writes[FlagStats] = Json.writes[FlagStats]
  implicit val dataWrites: Writes[Data] = Json.writes[Data]
  implicit val readgroupWrites: Writes[Readgroup] = Json.writes[Readgroup]
  implicit val libraryWrites: Writes[Library] = Json.writes[Library]
  implicit val sampleWrites: Writes[Sample] = Json.writes[Sample]
  implicit val rootWrites: Writes[Root] = Json.writes[Root]
}
