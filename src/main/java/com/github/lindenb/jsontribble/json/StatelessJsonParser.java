package com.github.lindenb.jsontribble.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broad.tribble.readers.PositionalBufferedStream;


public class StatelessJsonParser
	{
	private static final ObjectFactory<List<Object>> DEFAULT_LIST_FACTORY=new ObjectFactory<List<Object>>()
			{
			@Override
			public List<Object> createNewObject()
				{
				return new ArrayList<Object>();
				}
			};
	private ObjectFactory<List<Object>> listFactory=DEFAULT_LIST_FACTORY;
	
	private static final ObjectFactory<Map<String,Object>> DEFAULT_OBJECT_FACTORY=new ObjectFactory<Map<String,Object>>()
			{
			@Override
			public Map<String,Object> createNewObject()
				{
				return new LinkedHashMap<String,Object>();
				}
			};		
	private  ObjectFactory<Map<String,Object>> mapFactory=DEFAULT_OBJECT_FACTORY;

	private static final NumberParser DEFAULT_NUMBER_PARSER=new NumberParser()
		{
		@Override
		public Number parseNumber(String lit) throws IllegalArgumentException
			{
			try {
				return Integer.parseInt(lit);
				}
			catch (Exception e)
				{
				}
			try {
				return Long.parseLong(lit);
				}
			catch (Exception e)
				{
				}
			
			try {
				return new BigInteger(lit);
				}
			catch (Exception e)
				{
				}
			
			try {
				return Double.parseDouble(lit);
				}
			catch (Exception e)
				{
				}
			
			try {
				return new BigDecimal(lit);
				}
			catch (Exception e)
				{
				}
			throw new IllegalArgumentException("Cannot parse number "+lit);
			}
		};
	private NumberParser numberParser=DEFAULT_NUMBER_PARSER;
			
	protected int readSkipWs(PositionalBufferedStream stream)
		throws IOException
		{
		int c;
		while((c=stream.read())!=-1 && Character.isWhitespace(c));
		return c;
		}
	
	protected int peekSkipWs(PositionalBufferedStream stream)
		throws IOException
		{
		int c;
		while((c=stream.peek())!=-1)
			{
			if(!Character.isWhitespace(c)) return c;
			stream.read();
			}
		return -1;
		}
	
	
	private void mustConsumme(PositionalBufferedStream stream,String s)
			throws IOException
		{
		for(int i=0;i< s.length();++i)
			{
			int c=stream.read();
			if(c==-1) throw new IOException();
			if(c!=s.charAt(i) ) throw new IOException();
			}
		}
	
	public boolean bool(PositionalBufferedStream stream)
			throws IOException
			{
			int c=readSkipWs(stream);
			switch(stream.read())
				{
				case 't': mustConsumme(stream,"rue");return true;
				case 'f': mustConsumme(stream,"alse");return false;
				default: throw new IOException("Expected 't'rue of 'f'alse but got "+c);
				}
			}
	
	public Object nil(PositionalBufferedStream stream)
			throws IOException
			{
			int c;
			if((c=readSkipWs(stream))!='n') throw new IOException("Expected 'n'ull but got "+c);
			mustConsumme(stream,"ull");
			return null;
			}
	
	private String jsonString(PositionalBufferedStream in,int quote)
			throws IOException
			{
			StringBuilder b=new StringBuilder();
			int c;
			while(((c=in.read()))!=-1)
				{
				if(c=='\\')
					{
					switch(c=in.read())
						{
						case -1:throw new IOException("\\ followed by EOF");
						case 'n': b.append("\n"); break;
						case 't': b.append("\t"); break;
						case 'r': b.append("\r"); break;
						case '\'': b.append("\'"); break;
						case '\"': b.append("\""); break;
						default: b.append((char)c);break;
						}
					}
				else if(c==quote)
					{
					return b.toString();
					}
				else
					{
					b.append((char)c);
					}
				}
			
			throw new IOException("cannot parse string");
			}
	
	public String string(PositionalBufferedStream in)
		throws IOException
		{
		int c=this.readSkipWs(in);
		switch(c)
			{
			case '\'':
			case '\"': return jsonString(in,c);
			default: throw new IOException("Expected '\"' or '\'' but got '"+c+"'");
			}
		}
	
	private Number jsonNumber(PositionalBufferedStream in,int first)
			throws IOException
		{
		StringBuilder b=new StringBuilder();
		b.append((char)first);
		int c;
		while((c=in.peek())!=-1 && (Character.isDigit(c) || "eE.+-".indexOf(c)!=-1))
			{
			b.append((char)in.read());
			}
		return this.numberParser.parseNumber(b.toString());
		}
	
	
	public Object number(PositionalBufferedStream in)
		throws IOException
		{
		int c=this.readSkipWs(in);
		switch(c)
			{
			case '+':case '-': case '.': 
			case '0': case '1': case '2': case '3': case '4': 
			case '5': case '6': case '7': case '8': case '9':
				{
				return jsonNumber(in, c);
				}
			default: throw new IOException("bad number starting with '"+(char)c+"'");
			}
		}
	
	public int commaOrEndObject(PositionalBufferedStream in)
			throws IOException
		{
		int c=this.readSkipWs(in);
		switch(c)
			{
			case ',':
			case '}': return c;
			default: throw new IOException("Expected '}' or ',' but got '"+c+"'");
			}
		}

	protected Map.Entry<String, Object> jsonObjectEntry(PositionalBufferedStream in)
		throws IOException
		{
		String key=string(in);
		int c=readSkipWs(in);
		if(c!=':') throw new IOException("Expected ':' but got '"+c+"'");
		Object value=any(in);
		return new AbstractMap.SimpleEntry<String,Object>(key,value);
		}
	
	/** assume '{' was already found */
	protected Map<String, Object> jsonObject(PositionalBufferedStream in)
		throws IOException
		{
		Map<String, Object> object=this.mapFactory.createNewObject();
		int c;
		c=peekSkipWs(in);
		if(c=='}')
			{
			in.read();
			return object;
			}
		
		for(;;)
			{
			Map.Entry<String, Object> entry=this.jsonObjectEntry(in);
			if(object.containsKey(entry.getKey())) throw new IOException("duplicate key: "+entry.getKey());
			object.put(entry.getKey(),entry.getValue());
			if(commaOrEndObject(in)=='}') return object;
			}
		}
	
	public Map<String, Object> object(PositionalBufferedStream in)
		throws IOException
		{
		int c=this.readSkipWs(in);
		if(c!='{') throw new IOException("Expected '{' but got '"+c+"'");
		return jsonObject(in);
		}
	
	
	public int commaOrEndArray(PositionalBufferedStream in)
			throws IOException
		{
		int c=this.readSkipWs(in);
		switch(c)
			{
			case ',':
			case ']': return c;
			default: throw new IOException("Expected ']' or ',' but got '"+c+"'");
			}
		}
	
	/* assume '[' seen */
	private List<Object> jsonArray(PositionalBufferedStream in)
		throws IOException
		{
		List<Object> array=this.listFactory.createNewObject();
		int c=peekSkipWs(in);		
		if(c==']')
			{
			in.read();
			return array;
			}
		for(;;)
			{
			array.add(any(in));
			if(commaOrEndArray(in)==']') return array;
			}

		}
	
	public List<Object> array(PositionalBufferedStream in)
		throws IOException
		{
		int c=this.readSkipWs(in);
		if(c!='[') throw new IOException("Expected '[' but got '"+c+"'");
		return jsonArray(in);
		}
	
	
	public Object any(PositionalBufferedStream stream)
		throws IOException
		{
		int c=readSkipWs(stream);
		switch(c)
			{
			case '{': return jsonArray(stream);
			case '[': return jsonObject(stream);
			case 't': mustConsumme(stream, "rue"); return Boolean.TRUE;
			case 'f': mustConsumme(stream, "alse"); return Boolean.FALSE;
			case 'n': mustConsumme(stream, "ull");return null;
			case '\'': case '\"': return jsonString(stream,c);
			case '+':case '-': case '.': 
			case '0': case '1': case '2': case '3': case '4': 
			case '5': case '6': case '7': case '8': case '9':
				{
				return jsonNumber(stream,c);
				}
			case -1: throw new IOException("EOF met");
			default:
				{
				throw new IOException("Bad json. found "+(char)c);
				}
			}
		}
	}
