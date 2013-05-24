BEGIN	{
		FS="	";
		printf("{\"header\":{\"description\":\"UCSC  snp137: select count(*) from snp137 where FIND_IN_SET(func,\\\"missense\\\")>0 and avHet>0.1\"},\"features\":[\n");
		}
		{
		if(NR>1) printf("\n,");
		printf("{\"chrom\":\"%s\"",$2);
		printf(",\"start\":%s",$3);
		printf(",\"end\":%s",$4);
		printf(",\"name\":\"%s\"",$5);
		
		printf(",\"score\":%s",$6);
		printf(",\"strand\":\"%s\"",$7);
		printf(",\"refNCBI\":\"%s\"",$8);
		printf(",\"refUCSC\":\"%s\"",$9);
		printf(",\"observed\":\"%s\"",$10);
		printf(",\"class\":\"%s\"",$12);
		
		
		
		gsub("[,]+$","",$13)
		n=split($13,a,"[,]");
		if(n>0)
			{
			printf(",\"valid\":[");
			for(i=1;i<=n;++i) {if(i>1) printf(",");printf("\"%s\"",a[i]);}
			printf("]");
			}
		printf(",\"avHet\":%s",$14);
		
		gsub("[,]+$","",$16)
		n=split($16,a,"[,]");
		if(n>0)
			{
			printf(",\"func\":[");
			for(i=1;i<=n;++i) {if(i>1) printf(",");printf("\"%s\"",a[i]);}
			printf("]");
			}
			
		gsub("[,]+$","",$21)
		n=split($21,a,"[,]");
		if(n>0)
			{
			printf(",\"submitters\":[");
			for(i=1;i<=n;++i) {if(i>1) printf(",");printf("\"%s\"",a[i]);}
			printf("]");
			}
		
		gsub("[,]+$","",$26)
		n=split($26,a,"[,]");
		if(n>0)
			{
			printf(",\"bitfields\":[");
			for(i=1;i<=n;++i) {if(i>1) printf(",");printf("\"%s\"",a[i]);}
			printf("]");
			}
		printf("}");
		}
	
END		{
		printf("\n]}\n");
		}
	