/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.persistance.query.JOTFSQueryImpl;
import net.jot.persistance.query.JOTQueryManager;

/**
 * Manager for an FSDB index
 * @deprecated 
 * @author tcolar
 */
public class JOTFSIndexManager
{
        /**
	private static Hashtable indexes=new Hashtable();
		
	
	public static Hashtable getIndexes()
	{
		return indexes;
	}

       
	public static JOTFSIndex getIndex(JOTModelMapping mapping) throws Exception
	{
		String name=mapping.getTableName();
		JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.DEBUG_LEVEL,JOTFSIndexManager.class,"GetIndex model name:"+name);
		JOTFSIndex index=(JOTFSIndex)indexes.get(name);
		if(index==null)
		{
			// we need to load the index from the file.
			synchronized(JOTFSIndexManager.class)
			{
				if(index==null)
				{
					index=new JOTFSIndex();
					JOTFSQueryImpl impl=(JOTFSQueryImpl)JOTQueryManager.getImplementation(mapping.getQueryClassName());
					RandomAccessFile indexFile=impl.getIndexFile(mapping);
					indexFile.seek(0);
					index.setFile(indexFile);
					while(indexFile.getFilePointer()<indexFile.length())
					{
						long indexPos=indexFile.getFilePointer();
						long id=indexFile.readLong();
						long value=indexFile.readLong();
						if(id > 0)
						{
							index.setIndexIndexOffset(id,indexPos);
							//TODO: + make use of the metadata to see if data changed, maybe verify that the dataId is matching the dataId in the data file and if not, rebuild the index
							index.setDataOffset(id, value);
						}
					}
					indexes.put(name,index);
				}
			}
		}	
		return index;
	}

	public static void destroy()
	{
		Enumeration e=indexes.elements();
		while(e.hasMoreElements())
		{
			JOTFSIndex index=(JOTFSIndex)e.nextElement();
			if(index!=null)
				index.closeFile();
		}
	}
*/
}
