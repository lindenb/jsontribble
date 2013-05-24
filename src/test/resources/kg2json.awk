BEGIN	{
		FS="	";
		printf("{\"header\":{\"description\":\"UCSC  knownGenes: select * from knownGene where cdsStart<cdsEnd order by name\"},\"features\":[\n");
		}
		{
		if(NR>1) printf("\n,");
		printf("{\"chrom\":\"%s\"",$2);
		printf(",\"start\":%s",$4);
		printf(",\"end\":%s",$5);
		printf(",\"strand\":\"%s\"",$3);
		printf(",\"name\":\"%s\"",$1);
		printf(",\"cdsStart\":%s",$6);
		printf(",\"cdsEtart\":%s",$7);
		printf(",\"exonCount\":%s",$8);
		nExons=int($8);
		split($9,exonStarts,"[,]");
   		split($10,exonEnds,"[,]");
		
		printf(",\"exons\":[");
		for(i=1;i<=nExons;i++)
	        {
	        if(i>1) printf(",");
	        printf("{\"start\":%s,\"end\":%s,\"name\":\"Exon ",exonStarts[i],exonEnds[i]);
	        
	      
            if($6=="+")
                {
                printf("%d",i);
                }
            else
                {
             	 printf("%d",(nExons+1)-i);
                }
	            
	        printf("\"}");
	        }
		printf("]");
		
		
		if($11!="")
			{
			printf(",\"proteinID\":\"%s\"",$11);
			}
		if($12!="")
			{
			printf(",\"alignID\":\"%s\"",$12);
			}
		printf("}");
		}
	
END		{
		printf("\n]}\n");
		}
	