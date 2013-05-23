package com.github.lindenb.jsontribble;

import java.io.IOException;
import java.util.Map;

import org.broad.tribble.Feature;
import org.broad.tribble.FeatureCodecHeader;
import org.broad.tribble.readers.PositionalBufferedStream;

import com.github.lindenb.jsontribble.json.StatelessJsonParser;

public class JSONCodec extends StatelessJsonParser
	implements org.broad.tribble.FeatureCodec<JSONFeature>
	{
	@Override
	public boolean canDecode(String f) {
		return f.endsWith(".json");
	}

	@Override
	public JSONFeature decode(PositionalBufferedStream in) throws IOException
		{
		for(;;)
			{
			Map<String,Object> m=null;
			int c=super.readSkipWs(in);
			switch(c)
				{
				case -1 : return null;
				case '{': m= super.jsonObject(in);break;
				default: throw new IOException("Illegal JSON: got "+(char)c);
				}
			JSONFeature f=new JSONFeature(m);
			c=super.commaOrEndArray(in);
			if(c==']') while((c=in.read())!=-1);//consumme to the end
			if(!f.valid()) continue;
			return f;
			}
		}

	@Override
	public Feature decodeLoc(PositionalBufferedStream in) throws IOException
		{
		return decode(in);
		}

	@Override
	public Class<JSONFeature> getFeatureType()
		{
		return JSONFeature.class;
		}

	@Override
	public FeatureCodecHeader readHeader(PositionalBufferedStream in)
			throws IOException
		{
		int c=super.readSkipWs(in);
		Object header=null;
		if(c!='{') throw new IOException("Expected '{ but got \""+(char)c+"\"");
		for(;;)
			{
			String key=string(in);
			c=readSkipWs(in);
			if(c!=':') throw new IOException("Expected ':' but got '"+c+"'");

			if(key.equals("header"))
				{
				header=any(in);
				}
			else if(key.equals("features"))
				{
				c=super.readSkipWs(in);
				if(c!='[') throw new IOException("reading \"features\" expected a '[' found "+(char)c);
				break;
				}
			else
				{
				any(in);
				}
			c=super.readSkipWs(in);
			if(c!=',') throw new IOException("expected a comma found "+(char)c);
			}
		return new FeatureCodecHeader(header,in.getPosition());
		}
	
	}
