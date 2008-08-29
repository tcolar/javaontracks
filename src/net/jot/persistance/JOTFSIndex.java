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
import java.util.Hashtable;

import net.jot.logger.JOTLogger;

/**
 * Represents an index for an FSDB database
 * Used to find data faster and index/vacuum it.
 * 
 * Note that the data file contains all it needs on it's own.
 * The index can actually be rebuilt from the data file (ie: using JOTFSQueryImpl.vacuum())
 * @author tcolar
 */
public class JOTFSIndex
{
  /**
         * Stores the current highest ID found in the data file, so we can get the next unique higher value using nextval()
         */

  public long highestId = 0;
  /**
         * The index file.
         */
  private RandomAccessFile indexFile = null;
  /**
         * Use for fast find of a data record 
         * Key: ID / primarey key of the Indexed model.
         * Value: Offest of that ID data row in the data file.
         */
  private Hashtable dataIndex = new Hashtable();
  /**
         * This indexes the index file itself.
         * So that when a data offset is updated we can update the corresponsing "pointer" in the index file
         * This way when we add/delete a value in the table we can quicly update the index on file as well.
         */
  private Hashtable indexIndex = new Hashtable();

  /**
         * Returns the position od a data row given it's index
         * @param index
         * @return
         */
  public long getIndexValue(long index)
  {
    Long value = (Long) dataIndex.get(new Long(index));
    if (value == null)
    {
      value = new Long(-1);
    }
    return value.longValue();
  }

  /**
   * Updates the offset of a data file row in the index
         * 
         * @param index
         * @param value
         * @throws java.lang.Exception
         */
  public synchronized void setDataOffset(long id, long offset) throws Exception
  {
    // update dataIndex
    dataIndex.put(new Long(id), new Long(offset));
    if (id > highestId)
    {
      highestId = id;
    }
  }

  /**
         * When adding a new entry, call this method to get the next unique index value.
         * @return
         */
  public synchronized long nextVal()
  {
    highestId++;
    return highestId;
  }

  /**
         * Sets the index file
         * @param indexFile
         */
  public void setFile(RandomAccessFile indexFile)
  {
    this.indexFile = indexFile;

  }

  /**
         * hen a new entry is added to the data file(table), this should be called as well to update the index with it.
         * @param indexFile
         * @param id
         * @param offset
         * @throws java.lang.Exception
         */
  public synchronized void addEntry(/*RandomAccessFile indexFile, */long id, long offset) throws Exception
  {
    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, JOTFSIndexManager.class, "AddIndex " + id);
    long indexPos = indexFile.length();
    // append to index file
    indexFile.seek(indexFile.length());
    indexFile.writeLong(id);
    indexFile.writeLong(offset);
    // update indexIndex 
    setIndexIndexOffset(id, indexPos);
    // add to data index
    setDataOffset(id, offset);
  }

  public void closeFile()
  {
    try
    {
      indexFile.close();
    }
    catch (Exception e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, JOTFSIndexManager.class, "Failed to close index file.", e);
    }
  }

  /**
   * Call this after removing an entry from the data(table)
   * This will remove it from the index as well.
   * @param id
   * @throws java.lang.Exception
   */
  public void deleteEntry(long id) throws Exception
  {
    Long indexPtr = (Long) indexIndex.get(new Long(id));
    // remove from index on file
    indexFile.seek(indexPtr.longValue());
    indexFile.writeLong(0);
    indexFile.writeLong(0);
    // remove from memory
    dataIndex.remove(new Long(id));
    indexIndex.remove(new Long(id));
  }

  /**
   * Updates the IndexIndex
   * @param id
   * @param indexPos
   */
  public void setIndexIndexOffset(long id, long indexPos)
  {
    indexIndex.put(new Long(id), new Long(indexPos));
  }

  /*public void save() throws Exception
	{
		JOTLogger.log(JOTConstants.LOG_LVL_DEBUG,JOTFSIndexManager.class,"SaveIndex model");
		synchronized(JOTFSIndex.class)
		{
			indexFile.seek(0);
			Enumeration e=keys();
			while(e.hasMoreElements())
			{
				Long key=(Long)e.nextElement();
				long dataId=key.longValue();
				long value=getIndexValue(key.longValue());
				JOTLogger.log(JOTConstants.LOG_LVL_JDBC_TRACE,JOTFSIndexManager.class,"Writing to Index: "+dataId+" : "+value);						indexFile.writeLong(dataId);
				indexFile.writeLong(value);
			}
		}
	}*/

}
