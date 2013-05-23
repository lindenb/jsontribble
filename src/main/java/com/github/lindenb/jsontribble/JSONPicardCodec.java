package com.github.lindenb.jsontribble;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.samtools.util.SortingCollection.Codec;

@SuppressWarnings("unchecked")
public class JSONPicardCodec implements Codec<JSONFeature>
	{
	private enum DataType{
		STRING,BOOLEAN,NIL,LIST,OBJECT,BYTE,SHORT,INTEGER,LONG,FLOAT,DOUBLE,BIGINT,BIGDEC
		
		};
	
	private DataInputStream in;
	private DataOutputStream out;
	@Override
	public JSONFeature decode()
		{
		try {
			
			Map<String,Object> m=(Map<String,Object>)this.read();
			if(m==null) return null;
			return new JSONFeature(m);
			} 
		catch (IOException e)
			{
			return null;
			}
		}

	@Override
	public void encode(JSONFeature feat)
		{
		try
			{
			this.write(feat.getContent());
			}
		catch(IOException err)
			{
			throw new RuntimeException(err);
			}
		}

	@Override
	public void setInputStream(InputStream in)
		{
		this.in=new DataInputStream(in);
		}

	@Override
	public void setOutputStream(OutputStream out)
		{
		this.out=new DataOutputStream(out);
		}
	@Override
	public JSONPicardCodec clone() 
		{
		return new JSONPicardCodec();
		}
	
	private void writeDataType(DataType t)throws IOException
		{
		this.out.write((byte)t.ordinal());
		}
	
	
	private Object read() throws IOException
		{
		int c=this.in.read();
		if(c==-1) return null;
		switch(DataType.values()[c])
			{
			case NIL: return null;
			case BYTE: return in.readByte();
			case BOOLEAN: return in.readBoolean();
			case SHORT: return in.readShort();
			case INTEGER: return in.readInt();
			case LONG: return in.readLong();
			case FLOAT: return in.readFloat();
			case DOUBLE: return in.readDouble();
			case BIGINT: return new BigInteger(in.readUTF());
			case BIGDEC: return new BigDecimal(in.readUTF());
			case LIST:
				{
				int n=in.readInt();
				List<Object> L=new ArrayList<Object>(n);
				for(int i=0;i< n;++i)
					{
					L.add(read());
					}
				return L;
				}
			case OBJECT:
				{
				int n=in.readInt();
				Map<String,Object> m=new LinkedHashMap<String, Object>(n);
				for(int i=0;i< n;++i)
					{
					String k=in.readUTF();
					m.put(k,read());
					}
				return m;
				}
			case STRING:
				{
				return in.readUTF();
				}
			}
		return null;
		}

	
	private void write(Object o) throws IOException
		{
		if(o==null)
			{
			writeDataType(DataType.NIL);
			}
		else if(o instanceof Byte)
			{
			writeDataType(DataType.BYTE);
			out.writeByte(Byte.class.cast(o));
			}
		else if(o instanceof Short)
			{
			writeDataType(DataType.SHORT);
			out.writeShort(Short.class.cast(o));
			}
		else if(o instanceof Integer)
			{
			writeDataType(DataType.INTEGER);
			out.writeInt(Integer.class.cast(o));
			}
		else if(o instanceof Long)
			{
			writeDataType(DataType.LONG);
			out.writeLong(Long.class.cast(o));
			}
		else if(o instanceof BigInteger)
			{
			writeDataType(DataType.BIGINT);
			out.writeUTF(BigInteger.class.cast(o).toString());
			}
		else if(o instanceof Float)
			{
			writeDataType(DataType.FLOAT);
			out.writeFloat(Float.class.cast(o));
			}
		else if(o instanceof Double)
			{
			writeDataType(DataType.DOUBLE);
			out.writeDouble(Double.class.cast(o));
			}
		else if(o instanceof BigDecimal)
			{
			writeDataType(DataType.BIGDEC);
			out.writeUTF(BigDecimal.class.cast(o).toPlainString());
			}
		else if(o instanceof Boolean)
			{
			writeDataType(DataType.BOOLEAN);
			out.writeBoolean(Boolean.class.cast(o));
			}
		else if( o instanceof List)
			{
			List<Object> L=(List<Object>)o;
			writeDataType(DataType.LIST);
			out.writeInt(L.size());
			for(int i=0;i< L.size();++i)
				{
				write(L.get(i));
				}
			}
		else if(o.getClass().isArray())
			{
			Object L[]=(Object[])o;
			writeDataType(DataType.LIST);
			out.writeInt(L.length);
			for(int i=0;i< L.length;++i)
				{
				write(L[i]);
				}
			}
		else if(o instanceof Map)
			{
			Map<String,Object> m=(Map<String,Object>)o;
			writeDataType(DataType.OBJECT);
			out.writeInt(m.size());
			for(String k:m.keySet())
				{
				out.writeUTF(k);
				write(m.get(k));
				}
			}
		else
			{
			writeDataType(DataType.STRING);
			out.writeUTF(String.valueOf(o));
			}
		}
}
