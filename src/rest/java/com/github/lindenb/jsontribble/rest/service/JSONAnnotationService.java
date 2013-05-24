package com.github.lindenb.jsontribble.rest.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;


import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.CloseableTribbleIterator;
import org.broad.tribble.FeatureCodecHeader;
import org.broad.tribble.Tribble;
import org.broad.tribble.index.Index;
import org.broad.tribble.index.IndexFactory;
import org.broad.tribble.readers.PositionalBufferedStream;
import org.broad.tribble.util.ParsingUtils;


import com.github.lindenb.jsontribble.JSONCodec;
import com.github.lindenb.jsontribble.JSONFeature;
import com.github.lindenb.jsontribble.json.JsonPrinter;
import com.github.lindenb.jsontribble.json.JsonUtil;
import com.github.lindenb.jsontribble.json.StatelessJsonParser;

@Path("tribble/")
public class JSONAnnotationService 
	{
	public static final String CTX_JSON_FILE_PATH="tribble.json.file";
	
	private JSONCodec codec=new JSONCodec();
	private Map<String,MappingResource> name2resource=new HashMap<String,MappingResource>();
	public JSONAnnotationService(@Context  ServletContext context) throws IOException,ServletException
		{
		if(context==null) throw new ServletException("context is null");
		String configFile=context.getInitParameter(CTX_JSON_FILE_PATH);
		if(configFile==null) throw new ServletException("undefined "+CTX_JSON_FILE_PATH);
		//load config
		PositionalBufferedStream fin=new PositionalBufferedStream(new FileInputStream(configFile));
		StatelessJsonParser parser=new StatelessJsonParser();
		Map<String,Object> m=parser.object(fin);
		fin.close();
		List<Object> L=JsonUtil.getArray(m,"resources");
		if(L==null) throw new ServletException("No 'resources' defined in "+configFile);
		for(Object rsrc:L)
			{
			MappingResource mr=new MappingResource();
			mr.name=JsonUtil.getString(rsrc,"name");
			mr.jsonFilePath=JsonUtil.getString(rsrc,"path");
			mr.description=JsonUtil.getString(rsrc,"description");
			if(mr.name==null || mr.jsonFilePath==null) continue;
			name2resource.put(mr.name, mr);
			mr.loadHeader();
			}
		
		}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/resources/{rsrc}/chromosomes")
	public StreamingOutput getSequenceNames(
			@PathParam("rsrc")final String rsrcName
			)
		{
		return new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				MappingResource mr=name2resource.get(rsrcName);
				if(mr==null) throw new WebApplicationException(Status.NOT_FOUND);
				JsonPrinter.print(out,mr.getIndex().getSequenceNames());
				}
			};
		}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/resources")
	public StreamingOutput getResources()
		{
		return new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				PrintWriter w=new PrintWriter(out);
				w.print("[");
				boolean first=true;
				for(String k:name2resource.keySet())
					{
					if(!first) w.print(';');
					first=false;
					MappingResource rsrc=name2resource.get(k);
					w.print("{\"name\":");
					JsonPrinter.print(w, rsrc.name);
					if(rsrc.description!=null)
						{
						w.print(",\"description\":");
						JsonPrinter.print(w, rsrc.description);
						}
					w.print("}");
					}
				w.print("]");
				w.flush();
				}
			};
		}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/resources/{rsrc}/annotations.json")
	public StreamingOutput getAnnotationsJson(
			@PathParam("rsrc")final String rsrcName,
			@QueryParam("chrom")final String chrom,
			@QueryParam("start")final int start,
			@QueryParam("end")final int end
			) throws WebApplicationException
		{
		return new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				MappingResource mr=name2resource.get(rsrcName);
				if(mr==null) throw new WebApplicationException(Status.NOT_FOUND);
				mr.json(out, chrom, start, end);
				}
			};
		}
	
	@GET
	@Produces(MediaType.TEXT_XML)
	@Path("/resources/{rsrc}/annotations.xml")
	public StreamingOutput getAnnotationsXml(
			@PathParam("rsrc")final String rsrcName,
			@QueryParam("chrom")final String chrom,
			@QueryParam("start")final int start,
			@QueryParam("end")final int end
			) throws WebApplicationException
		{
		return new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				MappingResource mr=name2resource.get(rsrcName);
				if(mr==null) throw new WebApplicationException(Status.NOT_FOUND);
				mr.xml(out, chrom, start, end);
				}
			};
		}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/resources/{rsrc}/annotations.{ext :(bed|txt|text)}")
	public StreamingOutput getAnnotationsBed(
			@PathParam("rsrc")final String rsrcName,
			@QueryParam("chrom")final String chrom,
			@QueryParam("start")final int start,
			@QueryParam("end")final int end
			) throws WebApplicationException
		{
		return new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				MappingResource mr=name2resource.get(rsrcName);
				if(mr==null) throw new WebApplicationException(Status.NOT_FOUND);
				mr.bed(out, chrom, start, end);
				}
			};
		}
	
	
	private class MappingResource
		{
		String name;
		String description;
		String jsonFilePath;
		FeatureCodecHeader header=null;
		
		void loadHeader() throws IOException
			{
			InputStream inputStream = ParsingUtils.openInputStream(this.jsonFilePath);
			PositionalBufferedStream in=new PositionalBufferedStream(inputStream);
			this.header=codec.readHeader(in);
			in.close();
			}
		
		private Index getIndex() throws IOException
			{
			Index index= IndexFactory.loadIndex(
					Tribble.indexFile(this.jsonFilePath)
					);

			return index;
			}
		
		@SuppressWarnings("unchecked")
		private void xml(
				OutputStream out,
				String chrom,
				int start,
				int end
				) throws WebApplicationException
			{	
			AbstractFeatureReader<JSONFeature> reader = null;
			CloseableTribbleIterator<JSONFeature> iter=null;
			XMLStreamWriter w;
			try
				{
				XMLOutputFactory xmlfactory= XMLOutputFactory.newInstance();
				
				Index index=this.getIndex();
				reader=AbstractFeatureReader.getFeatureReader(
						this.jsonFilePath,
						codec,
						index
						);
				w= xmlfactory.createXMLStreamWriter(out,"UTF-8");
				w.writeStartDocument("UTF-8","1.0");
				w.writeStartElement("annotations");
				w.writeAttribute("chrom", chrom);
				w.writeAttribute("start", String.valueOf(start));
				w.writeAttribute("end", String.valueOf(end));
				
				iter=reader.query(chrom, start, end);
				JsonPrinter.write(w,"header",this.header.getHeaderValue());
				
				w.writeStartElement("features");
				while(iter.hasNext())
					{
					JSONFeature feat=iter.next();
					JsonPrinter.write(w,"feature",feat.getContent());
					}
				w.writeEndElement();
				
				w.writeEndElement();
				w.writeEndDocument();
				w.flush();
				index=null;
				}
			catch(Exception err)
				{
				throw new WebApplicationException(err, Status.INTERNAL_SERVER_ERROR);
				}
			finally
				{
				if(iter!=null) iter.close();
				if(reader!=null) try {reader.close();}catch(IOException err){}
				}

			}
		
		
		@SuppressWarnings("unchecked")
		private void json(
				OutputStream out,
				String chrom,
				int start,
				int end
				) throws WebApplicationException
			{	
			AbstractFeatureReader<JSONFeature> reader = null;
			CloseableTribbleIterator<JSONFeature> iter=null;
			PrintWriter w=null;
			try
				{
				Index index=this.getIndex();
				reader=AbstractFeatureReader.getFeatureReader(
						this.jsonFilePath,
						codec,
						index
						);
				iter=reader.query(chrom, start, end);
				w=new PrintWriter(out);
				w.print("{\"header\":");
				JsonPrinter.print(w,this.header.getHeaderValue());
				w.print("\n,\"features\":[");
				boolean first=true;
				while(iter.hasNext())
					{
					JSONFeature feat=iter.next();
					if(!first) w.print(',');
					w.print('\n');
					JsonPrinter.print(w,feat.getContent());
					}
				w.print("\n]}");
				index=null;
				}
			catch(Exception err)
				{
				throw new WebApplicationException(err, Status.INTERNAL_SERVER_ERROR);
				}
			finally
				{
				if(w!=null) w.flush();
				if(iter!=null) iter.close();
				if(reader!=null) try {reader.close();}catch(IOException err){}				
				}
			}

		@SuppressWarnings("unchecked")
		private void bed(
				OutputStream out,
				String chrom,
				int start,
				int end
				) throws WebApplicationException
			{	
			AbstractFeatureReader<JSONFeature> reader = null;
			CloseableTribbleIterator<JSONFeature> iter=null;
			PrintWriter w=null;
			try
				{
				Index index=this.getIndex();
				reader=AbstractFeatureReader.getFeatureReader(
						this.jsonFilePath,
						codec,
						index
						);
				iter=reader.query(chrom, start, end);
				w=new PrintWriter(out);
				while(iter.hasNext())
					{
					JSONFeature feat=iter.next();
					w.print(feat.getChr()+"\t"+feat.getStart()+"\t"+feat.getEnd()+"\t");
					w.println(JsonPrinter.toString(feat.getContent()).replaceAll("[\n]", ""));
					}
				index=null;
				}
			catch(Exception err)
				{
				throw new WebApplicationException(err, Status.INTERNAL_SERVER_ERROR);
				}
			finally
				{
				if(w!=null) w.flush();
				if(iter!=null) iter.close();
				if(reader!=null) try {reader.close();}catch(IOException err){}				
				}
			}		
		
		}
	}
