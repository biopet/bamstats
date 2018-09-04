package nl.biopet.tools.bamstats.schema

import htsjdk.samtools.SAMReadGroupRecord

case class GroupID(sample: String, library: String, readgroup: String)
object GroupID {

  /**
    * Method to get a GroupID from a SAMReadgroupRecord
    * @param readgroup the SAMReadGroupRecord
    * @return a GroupID object
    */
  def fromSamReadGroup(readgroup: SAMReadGroupRecord,
                       defaultSample: Option[String] = None,
                       defaultLibrary: Option[String] = None): GroupID = {
    GroupID(
      sample = Option(readgroup.getSample)
        .getOrElse(
          defaultSample.getOrElse(throw new IllegalArgumentException(
            s"Sample not found on readgroup: $readgroup"))),
      library = Option(readgroup.getLibrary)
        .getOrElse(
          defaultLibrary.getOrElse(throw new IllegalArgumentException(
            s"Library not found on readgroup: $readgroup"))),
      readgroup = readgroup.getReadGroupId
    )
  }
}
