JSON-TRIBBLE
============

**Tribble** ( http://code.google.com/p/tribble/ ) is a java library for indexing and querying genomic file formats. It was originally developed by members of the Integrative Genomics Viewer (IGV) and Genome Analysis Toolkit (GATK) at the Broad Institute. 

The following project contains a Tribble Codec for the **JSON** format.

It also contains a **REST**-application (Jersey) to query some json-based genomic files.

Project status: **beta**

Author: Pierre Lindenbaum PhD. @yokofakun

Dependencies
------------

Tested with:

* Java 1.7
* Picard ( > 1.91)

The REST/web application was tested with :

* Glassfish
* apache ant

Install
-------
clone the project from github:

```bash
git clone "https://github.com/lindenb/jsontribble.git"
cd ngsproject.git
```

Edit the file *build.properties* and set the informations about the location of the picard library: http://picard.sourceforge.net/

```
picard.version=1.91
picard.dir=/home/lindenb/package/picard-tools-${picard.version}
```

compile the tools with apache ant

```bash
$ ant
Buildfile: /home/lindenb/src/jsontribble/build.xml

sort:
    [mkdir] Created dir: /home/lindenb/src/jsontribble/tmp
    [javac] Compiling 1 source file to /home/lindenb/src/jsontribble/tmp
      [jar] Building jar: /home/lindenb/src/jsontribble/dist/jsonsort.jar
   [delete] Deleting directory /home/lindenb/src/jsontribble/tmp

(...)

all:

BUILD SUCCESSFUL
Total time: 1 second
```


The JSON parser
---------------
I wrote a simple JSON parser. I'm not sure it will handle correctly the non-standard characters (unicode, etc...).
Numbers are casted to java primitives in that order: int, long, BigInteger, float, double, BigDecimal.


The JSONFormat
--------------
structure of a JSON document:
```json
{
"header":(header),
"features":[ (features)* ]
}
```

a *header* must be a JSON-Object. You can put whatever you want in it.

a *feature*   must be a JSON-Object. You can put whatever you want in it but it must contains the following fields:

  * "chrom" : an integer or a string
  * "start" : an integer
  * "end" : an integer

you can use

  * "pos":  an integer
  
instead of start/end is start==end


A valid JSON-feature:
```json
{
    "chrom": "chr1",
    "start": 11873,
    "end": 14409,
    "strand": "+",
    "name": "uc010nxq.1",
    "cdsStart": 12189,
    "cdsEtart": 13639,
    "exonCount": 3,
    "exons": [
        {
            "start": 11873,
            "end": 12227,
            "name": "Exon 3"
        },
        {
            "start": 12594,
            "end": 12721,
            "name": "Exon 2"
        },
        {
            "start": 13402,
            "end": 14409,
            "name": "Exon 1"
        }
    ],
    "proteinID": "B7ZGX9",
    "alignID": "uc010nxq.1"
}
```

minimal document:
```json
{"header":{},"features":[]}

```



The Tools
---------

<a name="JSONSorter"/>
<h3>JSONSorter</h3>
<p>USAGE: JSONSorter [options]
<p>
<p>Sorts the JSON-based features.
<p></p>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>INPUT=File</td><td>the json input file(s) (default: stdin). The header is token from the first file  This option may be specified 0 or more times. </td></tr>
<tr><td>OUPUT=File</td><td>the json output file (default: stdout)  Default value: null. </td></tr>
</table>
Example:
```bash
java -jar jsonsort.jar INPUT=unsorted.json > dbsnp.json
```
<br/>

<a name="JSONIndexer"/>
<h3>JSONIndexer</h3>
<p>USAGE: JSONIndexer [options]
<p>
<p>Creates a Tribble Index for a JSON-file. Features must have been sorted on chrom/start/end.
<p>The tribble indexes are created in the same folder of the input as : (input.json).idx</p>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>INPUT=File</td><td>the json input file (default: stdin)  This option may be specified 0 or more times. </td></tr>
</table>

Example:
```bash
java -jar jsonindex.jar INPUT=sorted.json
```
<br/>

<a name="JSONQuery"/>
<h3>JSONQuery</h3>
<p>USAGE: JSONQuery [options]
<p>
<p>Query genomic locations from a JSON-based genomic file.
<p></p>
<table>
<tr><th>Option</th><th>Description</th></tr>
<tr><td>INPUT=String</td><td>the json input file.  The json file must be indexed with tribble.  Required. </td></tr>
<tr><td>BED=File</td><td>bed regions  Default value: null. </td></tr>
<tr><td>REGION=String</td><td>regions (chrom:start-end)  This option may be specified 0 or more times. </td></tr>
</table>

Example:
```bash
java -jar jsonquery.jar L=chr1:12048-1204800 I=merge.json | fold -w 80 | head -n 20

{"header":{"description":"UCSC  snp137: select count(*) from snp137 where FIND_I
N_SET(func,\"missense\")>0 and avHet>0.1"},"features":[{"chrom":"chr1","start":1
1873,"end":14409,"strand":"+","name":"uc010nxq.1","cdsStart":12189,"cdsEtart":13
639,"exonCount":3,"exons":[{"start":11873,"end":12227,"name":"Exon 3"},{"start":
12594,"end":12721,"name":"Exon 2"},{"start":13402,"end":14409,"name":"Exon 1"}],
"proteinID":"B7ZGX9","alignID":"uc010nxq.1"},{"chrom":"chr1","start":69090,"end"
:70008,"strand":"+","name":"uc001aal.1","cdsStart":69090,"cdsEtart":70008,"exonC
ount":1,"exons":[{"start":69090,"end":70008,"name":"Exon 1"}],"proteinID":"Q8NH2
1","alignID":"uc001aal.1"},{"chrom":"chr1","start":322036,"end":326938,"strand":
"+","name":"uc009vjk.2","cdsStart":324342,"cdsEtart":325605,"exonCount":3,"exons
":[{"start":322036,"end":322228,"name":"Exon 3"},{"start":324287,"end":324345,"n
ame":"Exon 2"},{"start":324438,"end":326938,"name":"Exon 1"}],"proteinID":"C9J4L
2","alignID":"uc009vjk.2"},{"chrom":"chr1","start":323891,"end":328581,"strand":
"+","name":"uc001aau.3","cdsStart":324342,"cdsEtart":325605,"exonCount":3,"exons
":[{"start":323891,"end":324060,"name":"Exon 3"},{"start":324287,"end":324345,"n
ame":"Exon 2"},{"start":324438,"end":328581,"name":"Exon 1"}],"proteinID":"C9J4L
```
<br/>



Running the REST Application
----------------------------

The rest application was tested with the glassfish server.


Run the tests (see above) to create a few json files.

Edit *src/rest/webapp/config.json* and set the full path to the indexed json files. Example:
```json
{
    "resources": [
        {
            "name": "dbsnp",
            "path": "/home/lindenb/src/jsontribble/src/test/resources/dbsnp.json"
        },
        {
            "name": "knownGenes",
            "path": "/home/lindenb/src/jsontribble/src/test/resources/knownGenes.json"
        },
        {
            "name": "merge",
            "path": "/home/lindenb/src/jsontribble/src/test/resources/merge.json"
        }
    ]
}
```




Add the correct information to the file 'build.properties'
```
glassfish.dir=/home/lindenb/package/glassfish3
glassfish.admin.port=4848
webapp.json.config=/home/lindenb/src/jsontribble/src/rest/webapp/config.json
```
Deploy the web application:
```bash

$ ant deploy.webapp

Buildfile: /home/lindenb/src/jsontribble/build.xml

dist/jsontribble.war:
    [mkdir] Created dir: /home/lindenb/src/jsontribble/tmp
    [javac] Compiling 1 source file to /home/lindenb/src/jsontribble/tmp
     [copy] Copying 1 file to /home/lindenb/src/jsontribble/tmp
      [war] Building war: /home/lindenb/src/jsontribble/dist/jsontribble.war
   [delete] Deleting directory /home/lindenb/src/jsontribble/tmp

deploy.webapp:
     [exec] Application deployed with name jsontribble.
     [exec] Command deploy executed successfully.

BUILD SUCCESSFUL

```

#### Listing the available resources  ####

URL : http://localhost:8080/jsontribble/rest/tribble/resources

```json
[{"name":"knownGenes"};{"name":"dbsnp"};{"name":"merge"}]
```
#### Listing the chromosomes of a resource  ####

URL : http://localhost:8080/jsontribble/rest/tribble/resources/{resource-name}/chromosomes

Example: http://localhost:8080/jsontribble/rest/tribble/resources/dbsnp/chromosomes

```json
["chr1","chr10","chr11","chr12","chr13","chr14","chr15","chr16","chr17","chr18","chr19",(...),"chrX","chrY"]
```

####  query a resource  ####
URL: http://localhost:8080/jsontribble/rest/tribble/resources/{resource-name}/annotations.{format}?chrom={chrom}&start={start}&end={end}

#####  JSON Format #####

Example: http://localhost:8080/jsontribble/rest/tribble/resources/dbsnp/annotations.json?chrom=chr1&start=881826&end=981826

```json
{"header":{"description":"UCSC  snp137: select count(*) from snp137 where FIND_IN_SET(func,\"missense\")>0 and avHet>0.1"}
,"features":[
{"chrom":"chr1","start":881826,"end":881827,"name":"rs112341375","score":0,"strand":"+","refNCBI":"G","refUCSC":"G","observed":"C/G","class":"single","valid":["by-frequency"],"avHet":0.5,"func":["missense"],"submitters":["BUSHMAN"]}
{"chrom":"chr1","start":897119,"end":897120,"name":"rs28530579","score":0,"strand":"+","refNCBI":"G","refUCSC":"G","observed":"C/G","class":"single","valid":["unknown"],"avHet":0.375,"func":["missense"],"submitters":["ABI","ENSEMBL","SSAHASNP"]}
{"chrom":"chr1","start":907739,"end":907740,"name":"rs112235940","score":0,"strand":"+","refNCBI":"G","refUCSC":"G","observed":"A/G","class":"single","valid":["unknown"],"avHet":0.5,"func":["missense"],"submitters":["COMPLETE_GENOMICS"]}
{"chrom":"chr1","start":949607,"end":949608,"name":"rs1921","score":0,"strand":"+","refNCBI":"G","refUCSC":"G","observed":"A/C/G","class":"single","valid":["by-cluster","by-frequency","by-1000genomes"],"avHet":0.464348,"func":["missense"],"submitters":["1000GENOMES","AFFY","BGI","BUSHMAN","CGAP-GAI","CLINSEQ_SNP","COMPLETE_GENOMICS","CORNELL","DEBNICK","EXOME_CHIP","GMI","HGSV","ILLUMINA","ILLUMINA-UK","KRIBB_YJKIM","LEE","MGC_GENOME_DIFF","NHLBI-ESP","SC_JCM","SC_SNP","SEATTLESEQ","SEQUENOM","UWGC","WIAF","YUSUKE"],"bitfields":["maf-5-some-pop","maf-5-all-pops"]}
]}
```

#####  BED/TEXT Format #####

Example:  "http://localhost:8080/jsontribble/rest/tribble/resources/merge/annotations.bed?chrom=chr1&start=897119&end=981826"

```
chr1	895966	901099	{"chrom":"chr1","start":895966,"end":901099,"strand":"+","name":"uc001aca.2","cds...
chr1	896828	897858	{"chrom":"chr1","start":896828,"end":897858,"strand":"+","name":"uc001acb.1","cds...
chr1	897008	897858	{"chrom":"chr1","start":897008,"end":897858,"strand":"+","name":"uc010nya.1","cds...
chr1	897119	897120	{"chrom":"chr1","start":897119,"end":897120,"name":"rs28530579","score":0,"strand...
chr1	897734	899229	{"chrom":"chr1","start":897734,"end":899229,"strand":"+","name":"uc010nyb.1","cds...
chr1	901876	910484	{"chrom":"chr1","start":901876,"end":910484,"strand":"+","name":"uc001acd.3","cds...
chr1	901876	910484	{"chrom":"chr1","start":901876,"end":910484,"strand":"+","name":"uc001ace.3","cds...
chr1	901876	910484	{"chrom":"chr1","start":901876,"end":910484,"strand":"+","name":"uc001acf.3","cds...
chr1	907739	907740	{"chrom":"chr1","start":907739,"end":907740,"name":"rs112235940","score":0,"stran...
chr1	910578	917473	{"chrom":"chr1","start":910578,"end":917473,"strand":"-","name":"uc001ach.2","cds...
chr1	934341	935552	{"chrom":"chr1","start":934341,"end":935552,"strand":"-","name":"uc001aci.2","cds...
chr1	934341	935552	{"chrom":"chr1","start":934341,"end":935552,"strand":"-","name":"uc010nyc.1","cds...
chr1	948846	949919	{"chrom":"chr1","start":948846,"end":949919,"strand":"+","name":"uc001acj.4","cds...
chr1	949607	949608	{"chrom":"chr1","start":949607,"end":949608,"name":"rs1921","score":0,"strand":"+...
chr1	955502	991499	{"chrom":"chr1","start":955502,"end":991499,"strand":"+","name":"uc001ack.2","cds...
```

#####  XML Format #####

Example: http://localhost:8080/jsontribble/rest/tribble/resources/dbsnp/annotations.xml?chrom=chr1&start=897119&end=981826

```XML
<?xml version="1.0" encoding="UTF-8"?>
<annotations chrom="chr1" start="897119" end="981826">
  <header>
    <description>UCSC  snp137: select count(*) from snp137 where FIND_IN_SET(func,"missense")&gt;0 and avHet&gt;0.1</description>
  </header>
  <features>
    <feature>
      <chrom>chr1</chrom>
      <start type="integer">897119</start>
      <end type="integer">897120</end>
      <name>rs28530579</name>
      <score type="integer">0</score>
      <strand>+</strand>
      <refNCBI>G</refNCBI>
      <refUCSC>G</refUCSC>
      <observed>C/G</observed>
      <class>single</class>
      <valid>
        <valid>unknown</valid>
      </valid>
      <avHet type="double">0.375</avHet>
      <func>
        <func>missense</func>
      </func>
      <submitters>
        <submitters>ABI</submitters>
        <submitters>ENSEMBL</submitters>
        <submitters>SSAHASNP</submitters>
      </submitters>
    </feature>
    <feature>
      <chrom>chr1</chrom>
      <start type="integer">907739</start>
      <end type="integer">907740</end>
      <name>rs112235940</name>
      <score type="integer">0</score>
      <strand>+</strand>
      <refNCBI>G</refNCBI>
      <refUCSC>G</refUCSC>
      <observed>A/G</observed>
      <class>single</class>
      <valid>
        <valid>unknown</valid>
      </valid>
      <avHet type="double">0.5</avHet>
      <func>
        <func>missense</func>
      </func>
      <submitters>
        <submitters>COMPLETE_GENOMICS</submitters>
      </submitters>
    </feature>
    <feature>
      <chrom>chr1</chrom>
      <start type="integer">949607</start>
      <end type="integer">949608</end>
      <name>rs1921</name>
      <score type="integer">0</score>
      <strand>+</strand>
      <refNCBI>G</refNCBI>
      <refUCSC>G</refUCSC>
      <observed>A/C/G</observed>
      <class>single</class>
      <valid>
        <valid>by-cluster</valid>
        <valid>by-frequency</valid>
        <valid>by-1000genomes</valid>
      </valid>
      <avHet type="double">0.464348</avHet>
      <func>
        <func>missense</func>
      </func>
      <submitters>
        <submitters>1000GENOMES</submitters>
        <submitters>AFFY</submitters>
        <submitters>BGI</submitters>
        <submitters>BUSHMAN</submitters>
        <submitters>CGAP-GAI</submitters>
        <submitters>CLINSEQ_SNP</submitters>
        <submitters>COMPLETE_GENOMICS</submitters>
        <submitters>CORNELL</submitters>
        <submitters>DEBNICK</submitters>
        <submitters>EXOME_CHIP</submitters>
        <submitters>GMI</submitters>
        <submitters>HGSV</submitters>
        <submitters>ILLUMINA</submitters>
        <submitters>ILLUMINA-UK</submitters>
        <submitters>KRIBB_YJKIM</submitters>
        <submitters>LEE</submitters>
        <submitters>MGC_GENOME_DIFF</submitters>
        <submitters>NHLBI-ESP</submitters>
        <submitters>SC_JCM</submitters>
        <submitters>SC_SNP</submitters>
        <submitters>SEATTLESEQ</submitters>
        <submitters>SEQUENOM</submitters>
        <submitters>UWGC</submitters>
        <submitters>WIAF</submitters>
        <submitters>YUSUKE</submitters>
      </submitters>
      <bitfields>
        <bitfields>maf-5-some-pop</bitfields>
        <bitfields>maf-5-all-pops</bitfields>
      </bitfields>
    </feature>
  </features>
</annotations>
```


Running the Tests
-----------------
compile the tools with ant and then
```bash
src/test/resources
make
```

