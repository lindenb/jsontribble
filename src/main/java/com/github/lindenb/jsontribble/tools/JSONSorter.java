package com.github.lindenb.jsontribble.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.broad.tribble.FeatureCodecHeader;
import org.broad.tribble.readers.PositionalBufferedStream;

import com.github.lindenb.jsontribble.JSONCodec;
import com.github.lindenb.jsontribble.JSONFeature;
import com.github.lindenb.jsontribble.JSONPicardCodec;
import com.github.lindenb.jsontribble.json.JsonPrinter;

import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.cmdline.Option;
import net.sf.picard.cmdline.StandardOptionDefinitions;
import net.sf.samtools.util.CloseableIterator;
import net.sf.samtools.util.SortingCollection;

public class JSONSorter extends CommandLineProgram
	{
    @Option(optional=true,doc="the json input file(s) (default: stdin)",shortName=StandardOptionDefinitions.INPUT_SHORT_NAME)
    public List<File> INPUT = new ArrayList<File>();
    @Option(optional=true,doc="the json output file (default: stdout)",shortName=StandardOptionDefinitions.OUTPUT_SHORT_NAME)
    public File OUPUT = null;
    
    
	@Override
	protected int doWork()
		{
		Comparator<JSONFeature> comparator=new Comparator<JSONFeature>()
			{
			@Override
			public int compare(JSONFeature a, JSONFeature b)
				{
				int i=a.getChr().compareTo(b.getChr());
				if(i!=0) return i;
				i=a.getStart()-b.getStart();
				if(i!=0) return i;
				return a.getEnd()-b.getEnd();
				}
			};
		try
			{
			PrintWriter pw=OUPUT==null?new PrintWriter(System.out):new PrintWriter(OUPUT);
	    	SortingCollection.Codec<JSONFeature> codec=new JSONPicardCodec();
			SortingCollection<JSONFeature> sorted=SortingCollection.newInstance(
					JSONFeature.class,
					codec,
					comparator,
					super.MAX_RECORDS_IN_RAM
					);
			int index=-1;
			for(;;)
				{
				++index;
				PositionalBufferedStream in=
						INPUT.isEmpty()?
						new PositionalBufferedStream(System.in):
						new PositionalBufferedStream(new FileInputStream(INPUT.get(index)));
				
				JSONCodec jsCodec=new JSONCodec();
				
				FeatureCodecHeader header=jsCodec.readHeader(in);
				if(index==0)
					{
					pw.print("{\"header\":");
					JsonPrinter.print(pw, header.getHeaderValue());
					pw.print(",\"features\":[\n");
					}
				
				while(!in.isDone())
					{
					JSONFeature feat = jsCodec.decode(in);
					if(feat==null) continue;
					sorted.add(feat);
					}
				
				in.close();
				if(index==0 && INPUT.isEmpty()) break;
				}
			
			sorted.doneAdding();
			boolean first=true;
			CloseableIterator<JSONFeature> iter=sorted.iterator();
			while(iter.hasNext())
				{
				JSONFeature feat = iter.next();
				if(!first) pw.print(',');
				first=false;
				JsonPrinter.print(pw,feat);
				}
			sorted.cleanup();
			iter.close();
			pw.print("\n]}\n");
			
			pw.flush();
			pw.close();
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
		new JSONSorter().instanceMainWithExit(args);
		}
	}
