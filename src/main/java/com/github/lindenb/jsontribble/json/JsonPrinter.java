package com.github.lindenb.jsontribble.json;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

@SuppressWarnings("rawtypes")
public class JsonPrinter extends JsonUtil
	{
	public static String toString(Object o)
		{
		StringWriter sw=new StringWriter();
		PrintWriter pw=new PrintWriter(sw);
		print(pw,o);
		pw.flush();
		return sw.toString();
		}
	public static void print(OutputStream w,Object o)
		{
		PrintWriter pw=new PrintWriter(w);
		print(pw,o);
		pw.flush();
		}
	
	public static void print(PrintWriter w,Object o)
		{	
		if(o==null)
			{
			System.out.print("null");
			return;
			}
		else if(o instanceof Number || o instanceof Boolean)
			{
			w.print(String.valueOf(o));
			}
		else if(o instanceof List)
			{
			List L=(List)o;
			w.print('[');
			for(int i=0;i< L.size();++i)
				{
				if(i>0) w.print(',');
				print(w,L.get(i));
				}
			w.print(']');
			}
		else if(o.getClass().isArray())
			{
			Object L[]=(Object[])o;
			w.print('[');
			for(int i=0;i< L.length;++i)
				{
				if(i>0) w.print(',');
				print(w,L[i]);
				}
			w.print(']');
			}
		else if(o instanceof Map)
			{
			boolean first=true;
			Map m=(Map)o;
			w.print('{');
			for(Object k:m.keySet())
				{
				if(!first) w.print(',');
				first=false;
				print(w,String.valueOf(k));
				w.print(':');
				print(w,m.get(k));
				}
			w.print('}');
			}
		else
			{
			String s=String.valueOf(o);
			w.print('"');
			for(int i=0;i< s.length();++i)
				{
				char c=s.charAt(i);
				switch(c)
					{
					case '\"': w.print("\\\""); break;
					case '\n': w.print("\\n"); break;
					case '\t': w.print("\\t"); break;
					default:
						{
						w.print(c);
						}
					}
				}
			w.print('"');
			}
		}
	
	private static String localName(String key)
		{
		key=key.trim().replaceAll("[^A-Za-z_0-9]+", "_");
		if(key.isEmpty() || !(Character.isLetter(key.charAt(0)) || key.startsWith("_")))
			{
			key="_"+key;
			}
		return key;
		}
	public static void write(XMLStreamWriter w,String key,Object value) throws XMLStreamException
		{
		String name=localName(key);
		if(value==null)
			{
			w.writeEmptyElement(name);
			return;
			}
		w.writeStartElement(name);
		if(value instanceof Number || value instanceof Boolean)
			{
			w.writeAttribute("type", value.getClass().getSimpleName().toLowerCase());
			w.writeCharacters(String.valueOf(value));
			}
		else if(value instanceof List || value.getClass().isArray())
			{
			List L=asArray(value);
			for(int i=0;i< L.size();++i)
				{
				write(w,key,L.get(i));
				}
			}
		else if(value instanceof Map)
			{
			Map m=asObject(value);
			for(Object k:m.keySet())
				{
				write(w,String.valueOf(k),m.get(k));
				}
			}
		else
			{
			w.writeCharacters(String.valueOf(value));
			}
		w.writeEndElement();
		}
	}
