# BamStats

BamStats is a package that contains tools
to generate stats from a BAM file,
merge those stats for multiple samples,
and validate the generated stats files.

     
#### Mode - Generate

Generate reports clipping stats, flag stats, insert size and mapping quality on a BAM file. It outputs
a JSON file, but can optionally also output in TSV format.

The output of the JSON file is organized in a sample - library - readgroup tree structure.
If readgroups in the BAM file are not annotated with sample (`SM`) and library (`LB`) tags
an error will be thrown.
This can be fixed by using `samtools addreplacerg` or `picard AddOrReplaceReadGroups`.
     
        

#### Mode - Merge

This module will merge bamstats files together and keep the sample/library/readgroup structure.
Values for the same readgroups will be added.
It will also validate the resulting file.
      
        

#### Mode - Validate

Validates a BamStats file.
If aggregation values can not be regenerated the file is considered corrupt.
This should only happen when the file has been manually edited.
     
        

# Documentation

For documentation and manuals visit our [github.io page](https://biopet.github.io/bamstats).

# About


BamStats is part of BIOPET tool suite that is developed at LUMC by [the SASC team](http://sasc.lumc.nl/).
Each tool in the [BIOPET tool suite](https://github.com/biopet/) is meant to offer a standalone function that can be used to perform a
dedicate data analysis task or added as part of a pipeline, for example the SASC team's [biowdl pipelines](https://github.com/biowdl).

All tools in the BIOPET tool suite are [Free/Libre](https://www.gnu.org/philosophy/free-sw.html) and
[Open Source](https://opensource.org/osd) Software.
    

# Contact


<p>
  <!-- Obscure e-mail address for spammers -->
For any question related to BamStats, please use the
<a href='https://github.com/biopet/bamstats/issues'>github issue tracker</a>
or contact
 <a href='http://sasc.lumc.nl/'>the SASC team</a> directly at: <a href='&#109;&#97;&#105;&#108;&#116;&#111;&#58;&#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;'>
&#115;&#97;&#115;&#99;&#64;&#108;&#117;&#109;&#99;&#46;&#110;&#108;</a>.
</p>

     

