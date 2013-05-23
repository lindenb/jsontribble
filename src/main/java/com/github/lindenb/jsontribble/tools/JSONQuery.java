package com.github.lindenb.jsontribble.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.cmdline.Option;
import net.sf.picard.cmdline.StandardOptionDefinitions;
import net.sf.picard.io.IoUtil;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.CloseableTribbleIterator;
import org.broad.tribble.Tribble;
import org.broad.tribble.index.Index;
import org.broad.tribble.index.IndexFactory;

import com.github.lindenb.jsontribble.JSONCodec;
import com.github.lindenb.jsontribble.JSONFeature;
import com.github.lindenb.jsontribble.json.JsonPrinter;

public class JSONQuery extends CommandLineProgram
	{
    @Option(optional=false,doc="the json input file",shortName=StandardOptionDefinitions.INPUT_SHORT_NAME)
    public String INPUT = null;
    @Option(optional=true,doc="bed regions",shortName="B")
    public File BED = null;
    @Option(optional=true,doc="regions (chrom:start-end)",shortName="L")
    public List<String> REGION = new ArrayList<String>();
  
    private long query(
    		long count,
    		PrintWriter pw,
    		AbstractFeatureReader<JSONFeature> reader,String chrom,int start,int end
    		)
    	throws IOException
    	{
    	CloseableTribbleIterator<JSONFeature> iter=null;
    	iter=reader.query(chrom, start, end);
    	while(iter.hasNext())
    		{	
    		JSONFeature f=iter.next();
    		if(count>0) pw.print(',');
    		++count;
    		JsonPrinter.print(pw, f.getContent());
    		}
    	iter.close();
    	return count;
    	}
    
	@Override
	protected int doWork()
		{
		if(BED==null && REGION.isEmpty())
			{
			System.err.println("No BED ot Region");
			return -1;
			}
		try
			{
			PrintWriter out=new PrintWriter(System.out);
			JSONCodec codec=new JSONCodec();
			String indexFile=Tribble.indexFile(INPUT);
			Index index=IndexFactory.loadIndex(indexFile);

			
			@SuppressWarnings("unchecked")
			AbstractFeatureReader<JSONFeature> reader = AbstractFeatureReader.getFeatureReader(
						INPUT,
						codec,
						index
						);

			
			
			long count=0L;
			for(String R: REGION)
				{
				for(String r:R.split("[ ;,]+"))
					{
					int start;
					int end;
					if(r.isEmpty()) continue;
					int colon=r.indexOf(':');
					if(colon<1) continue;
					int hyph=r.indexOf('-',colon);
					if(hyph==-1)
						{
						start=Integer.parseInt(r.substring(colon+1));
						end=start;
						}
					else
						{
						start=Integer.parseInt(r.substring(colon+1,hyph));
						end=Integer.parseInt(r.substring(hyph+1));
						}
					count=query(count,out,reader,r.substring(0,colon).trim(),start,end);
					}
				}
			if(BED !=null)
				{
				Pattern tab=Pattern.compile("[\t]");
				BufferedReader in=IoUtil.openFileForBufferedReading(BED);
				String line;
				while((line=in.readLine())!=null)
					{
					if(line.isEmpty() || line.startsWith("#")) continue;
					String tokens[]=tab.split(line,4);
					if(tokens.length<3) throw new IOException("bad bed line "+line);
					count=query(count,out,reader,tokens[0].trim(),
							Integer.parseInt(tokens[1]),
							Integer.parseInt(tokens[2])
							);
					}
				}
			out.print("\n]}\n");

			}
		catch(IOException err)
			{
			err.printStackTrace();
			return -1;
			}
			
		return 0;
		}
	public static void main(String[] args)
		{
		new JSONIndexer().instanceMainWithExit(args);
		}
	}
