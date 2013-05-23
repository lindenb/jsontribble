package test.github.lindenb.jsontribble;


import java.io.File;
import java.io.FileOutputStream;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.CloseableTribbleIterator;
import org.broad.tribble.Tribble;
import org.broad.tribble.index.Index;
import org.broad.tribble.index.IndexFactory;
import org.broad.tribble.util.LittleEndianOutputStream;
import com.github.lindenb.jsontribble.JSONCodec;
import com.github.lindenb.jsontribble.JSONFeature;

public class TestJsonTribble {
public static void main(String[] args) throws Exception
	{
	
	File dbSnpJs=new File("/home/lindenb/src/jsontribble/dbsnp.json");
	File indexFile=Tribble.indexFile(dbSnpJs);
	JSONCodec codec=new JSONCodec();
	
	for(int indexType=0;indexType<3;++indexType)
		{
		Index index=null;
		switch(indexType)
			{
			case 0: index=IndexFactory.createDynamicIndex(dbSnpJs, codec);break;
			case 1: index=IndexFactory.createIntervalIndex(dbSnpJs, codec);break;
			case 2: index=IndexFactory.createLinearIndex(dbSnpJs, codec);break;
			}
		
		LittleEndianOutputStream out=new LittleEndianOutputStream(new FileOutputStream(indexFile));
		index.write(out);
		out.close();
		
		index=IndexFactory.loadIndex(indexFile.toString());
		System.out.println("Chromosome:"+index.getSequenceNames());
		System.out.println("Properties:"+index.getProperties());
       
		@SuppressWarnings("unchecked")
		AbstractFeatureReader<JSONFeature> reader = AbstractFeatureReader.getFeatureReader(
        		dbSnpJs.getAbsolutePath(), codec, index);
        
        CloseableTribbleIterator<JSONFeature> iter = reader.query("chr1",26602,26802);
        while (iter.hasNext()) {
        	JSONFeature feature = iter.next();
        	System.err.println(feature.getContent());
        }

        iter.close();
        reader.close();
		}
	System.out.println("Done");
	}
}
