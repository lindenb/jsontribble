package com.github.lindenb.jsontribble.rest.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

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

@Path("tribble/")
public class JSONAnnotationService
	{
	public static final String CTX_JSON_FILE_PATH="tribble.json.file";
	@Context 
	private ServletContext context;
	private String jsonFile;
	private FeatureCodecHeader header=null;
	private Index index;
	private JSONCodec codec=new JSONCodec();
	
	public JSONAnnotationService() throws IOException,ServletException
		{
		this.jsonFile=context.getInitParameter(CTX_JSON_FILE_PATH);
		if(this.jsonFile==null) throw new ServletException("undefined "+CTX_JSON_FILE_PATH);
		InputStream inputStream = ParsingUtils.openInputStream(this.jsonFile);
		PositionalBufferedStream in=new PositionalBufferedStream(inputStream);
		this.header=codec.readHeader(in);
		inputStream.close();
		this.index= IndexFactory.loadIndex(Tribble.indexFile(JSONAnnotationService.this.jsonFile));
		}
	
	private Index getIndex()
		{
		return index;
		}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/chromosomes")
	public StreamingOutput getSequencesNames()
		{
		return new StreamingOutput()
			{
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				JsonPrinter.print(out,getIndex().getSequenceNames());
				}
			};
		}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/annotations")
	public StreamingOutput getAnnotations(
			@FormParam("chrom")final String chrom,
			@FormParam("start")final int start,
			@FormParam("end")final int end
			)
		{
		return new StreamingOutput()
			{
			@SuppressWarnings("unchecked")
			@Override
			public void write(OutputStream out) throws IOException,
					WebApplicationException
				{
				AbstractFeatureReader<JSONFeature> reader = null;
				CloseableTribbleIterator<JSONFeature> iter=null;
				PrintWriter w=null;
				try
					{
					reader=AbstractFeatureReader.getFeatureReader(jsonFile,codec,index);
					iter=reader.query(chrom, start, end);
					w=new PrintWriter(out);
					w.print("{\"header\":");
					JsonPrinter.print(w,header.getHeaderValue());
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
					w.flush();
					}
				catch(Exception err)
					{
					throw new WebApplicationException(err, Status.INTERNAL_SERVER_ERROR);
					}
				finally
					{
					if(w!=null) w.flush();
					if(iter!=null) iter.close();
					if(reader!=null) reader.close();
					}
				}
			};
		}
	}
