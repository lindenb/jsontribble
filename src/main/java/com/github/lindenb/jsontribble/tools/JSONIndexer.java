package com.github.lindenb.jsontribble.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.broad.tribble.Tribble;
import org.broad.tribble.index.Index;
import org.broad.tribble.index.IndexFactory;
import org.broad.tribble.util.LittleEndianOutputStream;

import com.github.lindenb.jsontribble.JSONCodec;


import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.cmdline.Option;
import net.sf.picard.cmdline.StandardOptionDefinitions;
import net.sf.picard.cmdline.Usage;

public class JSONIndexer extends CommandLineProgram
	{
	@Usage
    public String USAGE = getStandardUsagePreamble() +
    		"Creates a Tribble Index for a JSON-file. Features must have been sorted on chrom/start/end.\n"+
    		"The tribble indexes are created in the same folder of the input as : (input.json)"+Tribble.STANDARD_INDEX_EXTENSION
    		;

    @Option(optional=true,minElements=0,doc="the json input file. The json file must be indexed with tribble.  (default: stdin)",shortName=StandardOptionDefinitions.INPUT_SHORT_NAME)
    public List<File> INPUT = new ArrayList<File>();
    
    
	@Override
	protected int doWork()
		{
		for(File jsonFile: INPUT)
			{
			File indexFile=null;
			try
				{
				JSONCodec codec=new JSONCodec();
				
				Index index=IndexFactory.createIntervalIndex(jsonFile, codec);
				indexFile=Tribble.indexFile(jsonFile);
				LittleEndianOutputStream out=new LittleEndianOutputStream(new FileOutputStream(indexFile));
				index.write(out);
				out.flush();
				out.close();
				}
			catch(IOException err)
				{
				if(indexFile!=null) indexFile.delete();
				err.printStackTrace();
				return -1;
				}
			}
		return 0;
		}
	public static void main(String[] args)
		{
		new JSONIndexer().instanceMainWithExit(args);
		}
	}
