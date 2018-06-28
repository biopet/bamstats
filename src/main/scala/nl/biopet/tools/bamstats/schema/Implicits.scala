package nl.biopet.tools.bamstats.schema

import play.api.libs.json.{Json, Reads, Writes}

object Implicits {

  implicit val dataReads: Reads[Data] = Json.reads[Data]
  implicit val aggregationReads: Reads[Aggregation] = Json.reads[Aggregation]
  implicit val readgroupReads: Reads[Readgroup] = Json.reads[Readgroup]
  implicit val libraryReads: Reads[Library] = Json.reads[Library]
  implicit val sampleReads: Reads[Sample] = Json.reads[Sample]
  implicit val rootReads: Reads[Root] = Json.reads[Root]

  implicit val dataWrites: Writes[Data] = Json.writes[Data]
  implicit val aggregationWrites: Writes[Aggregation] = Json.writes[Aggregation]
  implicit val readgroupWrites: Writes[Readgroup] = Json.writes[Readgroup]
  implicit val libraryWrites: Writes[Library] = Json.writes[Library]
  implicit val sampleWrites: Writes[Sample] = Json.writes[Sample]
  implicit val rootWrites: Writes[Root] = Json.writes[Root]
}
