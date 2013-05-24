MYSQLUCSC=mysql --user=genome --host=genome-mysql.cse.ucsc.edu -A -D hg19 -N
jars.folder=../../../dist
json.files=$(addsuffix .json, dbsnp knownGenes merge)
JAVA=java
.PHONY=all clean

%.json.idx: %.json
	$(JAVA) -jar ${jars.folder}/jsonindex.jar  I=$<

all: $(addsuffix .idx,$(json.files))


merge.json: dbsnp.json knownGenes.json
	$(JAVA) -jar ${jars.folder}/jsonsort.jar O=$@ $(foreach F, $^, I=${F})  > $@
	
dbsnp.json.idx: dbsnp.json
	$(JAVA) -jar ${jars.folder}/jsonindex.jar  I=$<

dbsnp.json: dbsnp2json.awk
	$(MYSQLUCSC) -e 'select * from snp137 where FIND_IN_SET(func,"missense")>0 and avHet>0.1 order by name' |\
	awk -F '	' -f $<  |\
	$(JAVA) -jar ${jars.folder}/jsonsort.jar MAX_RECORDS_IN_RAM=500 > $@


knownGenes.json.idx: knownGenes.json
	$(JAVA) -jar ${jars.folder}/jsonindex.jar I=$<

knownGenes.json: kg2json.awk
	$(MYSQLUCSC) -e 'select * from knownGene where cdsStart<cdsEnd order by name' |\
	awk -F '	' -f $<  |\
	$(JAVA) -jar ${jars.folder}/jsonsort.jar MAX_RECORDS_IN_RAM=500 > $@


clean:
	rm -f $(foreach F,$(json.files), $F $F.idx ) 