package com.github.lindenb.jsontribble.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.CloseableTribbleIterator;
import org.broad.tribble.Tribble;
import org.broad.tribble.index.Index;
import org.broad.tribble.index.IndexFactory;

import com.github.lindenb.jsontribble.JSONCodec;
import com.github.lindenb.jsontribble.JSONFeature;


import net.sf.picard.cmdline.CommandLineProgram;
import net.sf.picard.cmdline.Option;
import net.sf.picard.cmdline.StandardOptionDefinitions;
import net.sf.picard.cmdline.Usage;
import net.sf.picard.io.IoUtil;

public class VCFAnnot extends CommandLineProgram
	{
	@Usage
	public String USAGE = getStandardUsagePreamble() + "Annot a VCF with JSON + javascript(rhino engine)\n";
	@Option(optional=true,doc="JSON file indexed with tribble",shortName="J")
	public String JSON = null;
	@Option(optional=true,doc="VCF file",shortName=StandardOptionDefinitions.INPUT_SHORT_NAME)
	public File INPUT = null;
	@Option(optional=true,doc="VCF file annotated",shortName=StandardOptionDefinitions.OUTPUT_SHORT_NAME)
	public File OUTPUT = null;
	@Option(optional=true,doc="Add this extra header to the VCF header",shortName="XH",minElements=0)
	public List<String> EXTRA_HEADERS = new ArrayList<String>();
	
	
	@Option(optional=true,doc="javascript code",shortName="JS")
	public String SCRIPT = null;
	@Option(optional=true,doc="javascript file",shortName="JF")
	public File SCRIPTF = null;
	
	int shift=0;
	
	public class Context
		{
		private String chrom;
		private int pos;
		private String ref;
		private String alt;
	
		private String id=null;
		private Set<String> filters=new LinkedHashSet<String>();
		private Map<String,String> info=new LinkedHashMap<String, String>();
	
		
		
		
		public String getChrom()
		{
			return chrom;
		}
	
	
		public int getPos()
		{
			return pos;
		}
	
	
		public String getRef()
		{
			return ref;
		}
	
		public String getAlt()
		{
			return alt;
		}
	
		public String getId()
		{
			return id;
		}
	
		public void setId(String id)
		{
			this.id = id;
		}
		public Set<String> getFilterSet()
		{
			return filters;
		}
	
		public Map<String, String> getInfoMap()
		{
			return info;
		}
	}
	
	
	private CompiledScript script=null;
	private ScriptEngine engine=null;
	
	private static boolean isEmpty(String s)
		{
		return s.trim().isEmpty() || s.trim().equals(".");
		}
	
	@Override
	protected int doWork() {
		try
			{
			if(SCRIPT==null && SCRIPTF==null)
				{
				System.err.println("undefined script");
				return -1;
				}
	
			ScriptEngineManager manager = new ScriptEngineManager();
			this.engine = manager.getEngineByName("js");
			if(this.engine==null)
				{
				System.err.println("not available: javascript. Use the SUN/Oracle JDK ?");
				return -1;
				}
	
			Compilable compilingEngine = (Compilable)this.engine;
			this.script = null;
			if(SCRIPTF!=null)
				{
				FileReader r=new FileReader(SCRIPTF);
				this.script=compilingEngine.compile(r);
				r.close();
				}
			else
				{
				this.script=compilingEngine.compile(SCRIPT);
				}
			final JSONCodec codec=new JSONCodec();
			String indexFile=Tribble.indexFile(JSON);
			Index index=IndexFactory.loadIndex(indexFile);
	
			@SuppressWarnings("unchecked")
			AbstractFeatureReader<JSONFeature> jsonReader = AbstractFeatureReader.getFeatureReader(
					JSON,
					codec,
					index
					);
			
			Pattern tab=Pattern.compile("[\t]");
			String line;
			Bindings bindings = this.engine.createBindings();
			PrintWriter w=new PrintWriter(OUTPUT);
			BufferedReader r= IoUtil.openFileForBufferedReading(INPUT);
			while((line=r.readLine())!=null)
				{
				if(line.startsWith("#"))
					{
					if(line.startsWith("#CHROM"))
						{
						for(String s:EXTRA_HEADERS)
							{
							while(!s.startsWith("##")) s="#"+s;
							w.println(s);
							}
						}
					w.println(line);
					continue;
					}
				String tokens[]=tab.split(line);
				Context ctx=new Context();
				ctx.chrom=tokens[0];
				ctx.pos=Integer.parseInt(tokens[1]);
				ctx.id=tokens[2];
				ctx.ref=tokens[3];
				ctx.alt=tokens[4];
				for(String f:tokens[6].split("[;]"))
					{
					if(isEmpty(f)) continue;
					ctx.filters.add(f);
					}
				for(String f:tokens[7].split("[;]"))
					{
					if(isEmpty(f)) continue;
					String k=f;
					String v="";
					int eq=f.indexOf("=");
					if(eq==-1)
						{
						k=f.substring(0,eq);
						v=f.substring(eq+1);
						}
					ctx.info.put(k,v);
					}
				
				bindings.put("variant",ctx);
				
				List<Object> annotations=new ArrayList<Object>();
		    	CloseableTribbleIterator<JSONFeature> iter=null;
		    	iter=jsonReader.query(ctx.chrom, ctx.pos+shift, ctx.pos+shift);
		    	while(iter.hasNext())
		    		{	
		    		JSONFeature f=iter.next();
		    		annotations.add(f.getContent());
		    		}
		    	iter.close();
	
				
				this.script.eval(bindings);
				ctx.filters.remove(".");
				
				
				for(int i=0;i< tokens.length;++i)
					{
					if(i>0) w.print('\t');
					switch(i)
						{
						case 2: w.print(isEmpty(ctx.id)?".":ctx.id);break;
						case 6: 
							{
							if(ctx.filters.isEmpty())
								{
								w.print('.');	
								}
							else
								{
								boolean first=true;
								for(String f:ctx.filters)
									{
									if(!first) w.print(";");
									first=false;
									w.print(f);	
									}
								}
							break;
							}
						case 7: 
							{
							if(ctx.info.isEmpty())
								{
								w.print('.');	
								}
							else
								{
								boolean first=true;
								for(String f:ctx.info.keySet())
									{
									if(!first) w.print(";");
									first=false;
									w.print(f);
									String v=ctx.info.get(f);
									if(!v.isEmpty())
										{
										w.print("=");
										w.print(f);
										}
									}
								}
							break;
							}
						default:  w.print(tokens[i]); break;
						}
					}
				w.println();
				}
			
			r.close();
			w.close();
			jsonReader.close();
			} 
		catch (Exception e) {
			e.printStackTrace();
			return -1;
			}
		return 0;
		}
	
	public static void main(String[] args)
		{
		new VCFAnnot().instanceMainWithExit(args);
		}
}
