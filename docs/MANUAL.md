# Manual

BamStats requires a BAM file and an output directory for its stats. 
Optionally a reference fasta file can be added against which the BAM file will be validated.
There are also fllags to set the binsize of stats, the size of the region per thread, and whether 
to also output in TSV format.

Example:
```java -jar BamStats-version.jar \
-R reference.fa \
-o output_dir \
-b file.bam \
--binSize 200 \
--threadBinSize 200 \
--tsvOutputs```