package com.github.lindenb.jsontribble;

import java.util.Map;

import org.broad.tribble.Feature;

import com.github.lindenb.jsontribble.json.JsonPrinter;

public class JSONFeature
	implements Feature
	{

	private Map<String,Object> content;
	public JSONFeature(Map<String,Object> content)
		{
		this.content=content;
		}
	@Override
	public String getChr()
		{
		Object o=content.get("chrom");
		if(o==null)throw new IllegalStateException("Not chrom in "+this.content);
		return String.valueOf(o);
		}
	
	
	@Override
	public int getStart() {
		Object o=this.content.get("start");
		if(o==null) o= this.content.get("pos");
		if(o==null) throw new IllegalStateException("Not start or pos in "+this.content);
		if(o.getClass()!=Integer.class) throw new IllegalStateException("Not an integer "+o+" in "+this.content);
		return Integer.class.cast(o);
		}
	
	@Override
	public int getEnd()
		{
		Object o=this.content.get("end");
		if(o==null) o= this.content.get("pos");
		if(o==null) throw new IllegalStateException("Not end or pos in "+this.content);
		if(o.getClass()!=Integer.class) throw new IllegalStateException("Not an integer "+o+" in "+this.content);
		return Integer.class.cast(o);
		}
	public Map<String, Object> getContent()
		{
		return content;
		}
	
	@Override
	public String toString() {
		return JsonPrinter.toString(getContent());
		}
	
	boolean valid()
		{
		try	{
			getChr();
			getStart();
			getEnd();
			}
		catch(Exception err)
			{
			return false;
			}
		return true;
		}
	}
