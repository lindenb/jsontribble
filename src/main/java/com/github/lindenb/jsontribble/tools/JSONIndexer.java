package com.github.lindenb.jsontribble.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.broad.tribble.Tribble;
import org.broad.tribble.index.Index;
import org.broad.tribble.index.IndexFactory;
import org.broad.tribble.util.LittleEndianOutputStream;

import com.github.lindenb.jsontribble.JSONCodec;


import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.cmdline.Option;
import net.sf.picard.cmdline.StandardOptionDefinitions;

public class JSONIndexer extends CommandLineProgram
	{
    @Option(optional=false,doc="the json input file (default: stdin)",shortName=StandardOptionDefinitions.INPUT_SHORT_NAME)
    public File INPUT = null;
    @Option(optional=true,doc="index filename",shortName="X")
    public File INDEX = null;
    
    
	@Override
	protected int doWork()
		{
		
		try
			{
			JSONCodec codec=new JSONCodec();
			Index index=IndexFactory.createIntervalIndex(INPUT, codec);
			
			File indexFile=INDEX==null?Tribble.indexFile(INPUT):INDEX;
			
			LittleEndianOutputStream out=new LittleEndianOutputStream(new FileOutputStream(indexFile));
			index.write(out);
			out.flush();
			out.close();
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
