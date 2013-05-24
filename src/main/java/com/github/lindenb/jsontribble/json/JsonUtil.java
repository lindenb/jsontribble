package com.github.lindenb.jsontribble.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonUtil
	{
	public static Map<String,Object> asObject(Object o)
		{
		if(o==null || !(o instanceof Map)) return null;
		return (Map<String,Object>)o;
		}

	public static List<Object> asArray(Object o)
		{
		if(o==null) return null;
		if(o instanceof List) return (List<Object>)o;
		if(o.getClass().isArray()) return Arrays.asList((Object[])o);
		return null;
		}

	
	
	public static String asString(Object o)
		{
		if(o==null) return null;
		return String.valueOf(o);
		}
	
	public static Integer asInt(Object o)
		{
		if(o==null || !(o instanceof Number)) return null;
		return Number.class.cast(o).intValue();
		}

	public static Long asLong(Object o)
		{
		if(o==null || !(o instanceof Number)) return null;
		return Number.class.cast(o).longValue();
		}
	
	
	public static Integer getInt(Object o,String key)
		{
		Map<String,Object> m=asObject(o);
		return m==null?null:asInt(m.get(key));
		}

	
	public static String getString(Object o,String key)
		{
		Map<String,Object> m=asObject(o);
		return m==null?null:asString(m.get(key));
		}
	
	public static List<Object> getArray(Object o,String key)
		{
		Map<String,Object> m=asObject(o);
		return m==null?null:asArray(m.get(key));
		}

	public static Map<String,Object> getObject(Object o,String key)
		{
		Map<String,Object> m=asObject(o);
		return m==null?null:asObject(m.get(key));
		}

	
	}
