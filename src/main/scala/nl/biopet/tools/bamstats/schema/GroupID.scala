package nl.biopet.tools.bamstats.schema

import htsjdk.samtools.SAMReadGroupRecord

case class GroupID(sample: String, library: String, readgroup: String)
object GroupID {

  /**
    * Method to get a GroupID from a SAMReadgroupRecord
    * @param record the SAMReadGroupRecord
    * @return a GroupID object
    */
  def fromSamReadGroup(record: SAMReadGroupRecord): GroupID = {
    GroupID(sample = record.getSample,
            library = record.getLibrary,
            readgroup = record.getReadGroupId)
  }
}
