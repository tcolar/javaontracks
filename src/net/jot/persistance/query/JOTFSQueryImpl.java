/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance.query;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import net.jot.db.JOTDBField;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTFSIndex;
import net.jot.persistance.JOTFSIndexManager;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTPersistanceManager;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLOrderBy;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.JOTStatementFlags;
import net.jot.utils.JOTUtilities;

//TODO: +++ make sure db files permissions are safe: ie: 600 ?

/**
 * Implementation of The Query Interface for an the JOT FSDB database.<br>
 * Handle all the CRUD functions
 * 
 * We use an "index" file to speed up things and cleanup data file during vacuum
 *
 * Does not support transactions.
 *
 * @author thibautc
 *
 */
public class JOTFSQueryImpl implements JOTQueryInterface
{

  private static final String TABLE_FILE_EXTENSION = ".jotdb";
  private static final String BACKUP_FILE_EXTENSION = ".back";
  private static final String INDEX_FILE_EXTENSION = ".jotindex";
  private static final String TEMP_TABLE_FILE_EXTENSION = ".jotdb~";
  private static final String TEMP_INDEX_FILE_EXTENSION = ".jotindex~";
  private static final String META_FILE_EXTENSION = JOTModelMapping.META_FILE_EXTENSION;

  //private RandomAccessFile tableFile=null;
	//private RandomAccessFile indexFile=null;

  private Hashtable tableFiles = new Hashtable();
  private Hashtable indexFiles = new Hashtable();

  public JOTModel findByID(JOTTransaction transaction, JOTModelMapping mapping, Class objectClass, long id) throws Exception
  {
    warnTransaction(transaction);
    JOTFSIndex index = JOTFSIndexManager.getIndex(mapping);
    long ptr = index.getIndexValue((long) id);

    if (ptr >= 0)
    {
      RandomAccessFile dataFile = getTableFile(mapping);
      dataFile.seek(ptr);
      byte[] data = new byte[mapping.getDataSize()];
      dataFile.readFully(data);
      return parseData(mapping, objectClass, data);
    }
    return null;
  }

  public Vector find(JOTTransaction transaction, JOTModelMapping mapping, Class objectClass, JOTSQLQueryParams params) throws Exception
  {
    warnTransaction(transaction);
    Vector results = new Vector();
    int ptr = 0;
    RandomAccessFile dataFile = getTableFile(mapping);
    JOTModel model = null;
    while (ptr + mapping.getDataSize() <= dataFile.length())
    {
      dataFile.seek(ptr);
      byte[] data = new byte[mapping.getDataSize()];
      dataFile.readFully(data);
      model = parseData(mapping, objectClass, data);
      if (params == null || match(model, params.getConditions()))
      {
          // id=0 is a deleted record
          if(model.getId()!=0)
          {
            addResult(results, model, params == null ? null : params.getOrderBys());
          }
      }
      ptr += mapping.getDataSize();
    //if we found 'limit' items, we are done.
    }
    model = null;
    /*if (params != null && params.getLimit() > 0 && results.size() >= params.getLimit())
    {
      results = new Vector(results.subList(0, params.getLimit()));
    }*/
    return results;
  }

  /**
         * adds a found result and sort it as requested.
         * @param results
         * @param model
         * @param orderBys
         */
  private void addResult(Vector results, JOTModel model, JOTSQLOrderBy[] orderBys) throws Exception
  {
    if (orderBys == null || orderBys.length == 0 || results.size() == 0)
    {
      results.add(model);
    }
    else
    {
      // we gotta insert it so it's sorted as requested
      int direction = orderBys[0].getDirection();
      String field = orderBys[0].getField();
      int start = 0;
      int end = results.size();
      int incr = 1;
      if (direction == JOTSQLOrderBy.DESCENDING)
      {
        start = results.size() - 1;
        end = -1;
        incr = -1;
      }
      Object object = null;
      String primaryKey = "dataid";
      try
      {
        primaryKey = model.getMapping().getPrimaryKey();
      }
      catch (Exception e)
      {
      }
      if (field.equalsIgnoreCase(primaryKey))
      {
        object = new Long(model.getId());
      }
      else
      {
        object = model.getFieldValue(field);
      }
      boolean done = false;
      int i = 0;
      for (i = start; i != end && !done; i += incr)
      {
        Object tmpVal = null;
        if (field.equalsIgnoreCase(primaryKey))
        {
          tmpVal = new Long(((JOTModel) results.get(i)).getId());
        }
        else
        {
          tmpVal = ((JOTModel) results.get(i)).getFieldValue(field);
        }
        if(object==null)
        {
            throw new Exception("We where asked to sort table '"+model.getMapping().getTableName()+"' by '"+field+"', but that field does not exists !");
        }
        if (object!=null && object.equals(tmpVal))
        {
        // go to the next orderby if there is one, otherwise "continue"
        }
        // byte,short,integer,boolean?,time,date,timestamp,string
				// not comparable: boolean, 
        if (object instanceof Byte ||
                object instanceof Short ||
                object instanceof Integer ||
                object instanceof Long ||
                object instanceof Float ||
                object instanceof Double ||
                object instanceof Timestamp ||
                object instanceof Date ||
                object instanceof Time ||
                object instanceof BigInteger ||
                object instanceof String)
        {
          // comparable objects
          Comparable objCmp = (Comparable) object;
          Comparable valCmp = (Comparable) tmpVal;
          int cmp = objCmp.compareTo(valCmp);
          if (direction == JOTSQLOrderBy.ASCENDING && cmp < 0)
          {
            // we should keep going
            continue;
          }
          if (direction == JOTSQLOrderBy.DESCENDING && cmp > 0)
          {
            // we should keep going
            continue;
          }
          if (direction == JOTSQLOrderBy.DESCENDING && cmp < 0)
          {
            // we found the spot
            done = true;
            continue;
          }
          if (direction == JOTSQLOrderBy.DESCENDING && cmp < 0)
          {
            // we found the spot
            done = true;
            continue;
          }
          if (cmp == 0)
          {
            // TODO: + we should go on the next orderBy here ...
						// keep going
            done = true;
            continue;
          }
        }
        else if (object instanceof Boolean)
        {
          if (((Boolean) object).booleanValue() != ((Boolean) tmpVal).booleanValue())
          {
            done = true;
          }
        }
      }
      if (direction == JOTSQLOrderBy.DESCENDING)
      {
        i++;
      }
      // inserting at correct location
      if (i > results.size())
      {
        results.add(model);
      }
      else
      {
        results.add(i, model);
      }
    }
  }

  public JOTModel findOne(JOTTransaction transaction, JOTModelMapping mapping, Class objectClass, JOTSQLQueryParams params) throws Exception
  {
    warnTransaction(transaction);
    if (params == null)
    {
      params = new JOTSQLQueryParams();
    }
    Vector result = find(transaction, mapping, objectClass, params);
    if (result == null || result.size() < 1)
    {
      return null;
    }
    return (JOTModel) result.get(0);
  }

  /**
         * checks wether a record macthes the query
         * @param model
         * @param conditions
         * @return
         */
  private boolean match(JOTModel model, JOTSQLCondition[] conditions)
  {
    try
    {
      for (int i = 0; i != conditions.length; i++)
      {
        JOTSQLCondition cond = conditions[i];
        String fieldName = cond.getField();
        Object object = model.getFieldValue(fieldName);

        // compare value and condition
        if (object instanceof Byte ||
                object instanceof Short ||
                object instanceof Integer ||
                object instanceof Long ||
                object instanceof Float ||
                object instanceof Double ||
                object instanceof Timestamp ||
                object instanceof Date ||
                object instanceof Time ||
                object instanceof BigInteger)
        {
          // comparable objects
          Comparable value = (Comparable) object;
          Comparable comp = (Comparable) cond.getValue();
          switch (cond.getComparaison())
          {
            case JOTSQLCondition.IS_EQUAL:
              if (!(value.compareTo(comp) == 0))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_NOT_EQUAL:
              if (!((value.compareTo(comp) != 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_GREATER:
              if (!((value.compareTo(comp) > 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_GREATER_OR_EQUAL:
              if (!((value.compareTo(comp) >= 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_LOWER:
              if (!((value.compareTo(comp) < 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_LOWER_OR_EQUAL:
              if (!((value.compareTo(comp) <= 0)))
              {
                return false;
              }
              break;
            default:
              throw new Exception("Cannot interpret '" + cond.getSQLComparator() + "' condition on type: " + object.getClass());
          }

        }
        else if (object instanceof Boolean)
        {
          //  Boolean does not implement comparable
          Boolean value = (Boolean) object;
          Boolean comp = (Boolean) cond.getValue();
          switch (cond.getComparaison())
          {
            case JOTSQLCondition.IS_EQUAL:
              if (!(value == comp))
              {
                return false;
              }
              break;
            default:
              throw new Exception("Cannot interpret '" + cond.getSQLComparator() + "' condition on type: " + object.getClass());
          }
        }
        else if (object instanceof String)
        { // String is comparable but we have to deal with "is_like"
          String value = (String) object;
          String comp = (String) cond.getValue();
          if(value==null)
              return false;
          switch (cond.getComparaison())
          {
            case JOTSQLCondition.IS_EQUAL:
              if (!(value.compareTo(comp) == 0))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_NOT_EQUAL:
              if (!((value.compareTo(comp) != 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_GREATER:
              if (!((value.compareTo(comp) > 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_GREATER_OR_EQUAL:
              if (!((value.compareTo(comp) >= 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_LOWER:
              if (!((value.compareTo(comp) < 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_LOWER_OR_EQUAL:
              if (!((value.compareTo(comp) <= 0)))
              {
                return false;
              }
              break;
            case JOTSQLCondition.IS_LIKE:
              // is this gonna work right ??
              comp = comp.replaceAll("%", ".*");
              //System.out.println("match: "+value+" vs "+comp);
              if (!(value.matches(comp)))
              {
                return false;
              }
              break;

            default:
              throw new Exception("Cannot interpret '" + cond.getSQLComparator() + "' condition on type: " + object.getClass());
          }
        }
      }
    }
    catch (Exception e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Exception during 'where' cause.", e);
      return false;
    }
    // no matching failed, so we have a match.
    return true;
  }

  public void createTable(JOTModelMapping mapping) throws Exception
  {
    String dbFolder = JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName());
    String table = mapping.getTableName() + TABLE_FILE_EXTENSION;
    File f = new File(dbFolder, table);

    if (!f.exists())
    {
      f.getParentFile().mkdirs();
      // create the meta file.
      JOTModelMapping.writeMetaFile(mapping);
    }
  }

  public void deleteTable(JOTModelMapping mapping) throws Exception
  {
    String dbFolder = JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName());
    String table = mapping.getTableName() + TABLE_FILE_EXTENSION;
    File f = new File(dbFolder, table);
    if (f.exists())
    {
      f.delete();
    }
    String index = mapping.getTableName() + INDEX_FILE_EXTENSION;
    f = new File(dbFolder, index);
    if (f.exists())
    {
      f.delete();
    }
    JOTModelMapping.deleteMetaFile(mapping);
  }

  public RandomAccessFile getTableFile(JOTModelMapping mapping) throws Exception
  {
    if (tableFiles.get(mapping.getTableName()) == null)
    {
      synchronized (this)
      {
        if (tableFiles.get(mapping.getTableName()) == null)
        {
          String dbFolder = JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName());
          String table = mapping.getTableName() + TABLE_FILE_EXTENSION;
          File f = new File(dbFolder, table);
          RandomAccessFile rf = new RandomAccessFile(f, "rwd");
          tableFiles.put(mapping.getTableName(), rf);
        }
      }
    }
    return (RandomAccessFile) tableFiles.get(mapping.getTableName());
  }

  public RandomAccessFile getIndexFile(JOTModelMapping mapping) throws Exception
  {
    if (indexFiles.get(mapping.getTableName()) == null)
    {
      synchronized (this)
      {
        if (indexFiles.get(mapping.getTableName()) == null)
        {
          String dbFolder = JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName());
          String table = mapping.getTableName() + INDEX_FILE_EXTENSION;
          File f = new File(dbFolder, table);
          // rwd means read and write synchronously
          RandomAccessFile rf = new RandomAccessFile(f, "rwd");
          indexFiles.put(mapping.getTableName(), rf);
        }
      }
    }
    return (RandomAccessFile) indexFiles.get(mapping.getTableName());
  }

  public synchronized void delete(JOTTransaction transaction, JOTModel model) throws Exception
  {
    warnTransaction(transaction);
    JOTModelMapping mapping = model.getMapping();
    JOTFSIndex index = JOTFSIndexManager.getIndex(mapping);
    long dataPtr = index.getIndexValue(model.getId());
    if (dataPtr >= 0)
    {
      int dataSize = mapping.getDataSize();
      // wipeout the data from disk
      RandomAccessFile dataFile = getTableFile(mapping);
      dataFile.seek(dataPtr);
      // blanks the entry (zeroes)
      byte[] blankData = new byte[dataSize];
      dataFile.write(blankData);
      // wipeout from the index
      index.deleteEntry(model.getId());
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Deleting entry: :" + mapping.getTableName() + " : " + model.getId());

    }
    else
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Cannot Delete entry: :" + mapping.getTableName() + " : " + model.getId() + " (not found)");
    }
  }

  /**
	 * Save/update the table in the database.
	 */
  public void save(JOTTransaction transaction, JOTModel model) throws Exception
  {
    warnTransaction(transaction);
    JOTModelMapping mapping = model.getMapping();
    if (model.getId() == -1)
    {
      JOTFSIndex index = JOTFSIndexManager.getIndex(mapping);
      model.setId((int) index.nextVal());
      RandomAccessFile dataFile = getTableFile(mapping);
      RandomAccessFile indexFile = getIndexFile(mapping);
      byte[] data = buildData(mapping, model);
      //prepare data row
      synchronized (this)
      {
        long value = dataFile.length();
        dataFile.seek(dataFile.length());
        dataFile.write(data);
        index.addEntry(/*indexFile,*/(long) model.getId(), value);
      }
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Saved " + mapping.getTableName() + " : " + model.getId() + " into " + JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName()) + "/" + mapping.getTableName() + TABLE_FILE_EXTENSION);
    }
    else
    {
      JOTFSIndex index = JOTFSIndexManager.getIndex(mapping);
      long dataPtr = index.getIndexValue(model.getId());
      RandomAccessFile dataFile = getTableFile(mapping);
      byte[] data = buildData(mapping, model);
      //prepare data row
      synchronized (this)
      {
        dataFile.seek(dataPtr);
        dataFile.write(data);
      }
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Updated " + mapping.getTableName() + " : " + model.getId());
    }
  }

  private JOTModel parseData(JOTModelMapping mapping, Class objectClass, byte[] data) throws Exception
  {
    ByteBuffer buf = ByteBuffer.wrap(data);
    Hashtable fields = mapping.getFields();
    Enumeration e = fields.keys();
    JOTModel model = (JOTModel) objectClass.newInstance();

    long id = buf.getLong();
    model.setId((int) id);

    while (e.hasMoreElements())
    {
      String fieldName = (String) e.nextElement();
      JOTDBField field = (JOTDBField) fields.get(fieldName);
      String type = field.getFieldType();
      Field javaField = model.getClass().getField(fieldName);

      if (type.equals(JOTDBField.TYPE_BIGINT))
      {
        Long value = new Long(buf.getLong());
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_INTEGER))
      {
        Integer value = new Integer(buf.getInt());
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_BIT))
      {
        byte value = buf.get();
        Boolean bool = new Boolean(value == 0 ? false : true);
        javaField.set(model, bool);
      }
      else if (type.equals(JOTDBField.TYPE_TINYINT))
      {
        Byte value = new Byte(buf.get());
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_SMALLINT))
      {
        Short value = new Short(buf.getShort());
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_FLOAT))
      {
        Float value = new Float(buf.getFloat());
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_DOUBLE))
      {
        Double value = new Double(buf.getDouble());
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_TIMESTAMP))
      {
        long time = buf.getLong();
        Timestamp value = new Timestamp(time);
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_TIME))
      {
        long time = buf.getLong();
        Time value = new Time(time);
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_DATE))
      {
        long time = buf.getLong();
        java.sql.Date value = new java.sql.Date(time);
        javaField.set(model, value);
      }
      else if (type.equals(JOTDBField.TYPE_VARCHAR))
      {
        // read the actual string length
        int stringLength = buf.getInt();
        // read the String data
        byte[] stringData = new byte[stringLength];
        buf.get(stringData);
        String value = new String(stringData);
        javaField.set(model, value);
        // 'skip' the String padding.
        if (field.getSize() > stringLength)
        {
          byte[] padding = new byte[field.getSize() - stringLength];
          buf.get(padding);
        }
      }
      else if (type.equals(JOTDBField.TYPE_DECIMAL))
      {
        // read the data length
        int unscaledDataLength = buf.getInt();
        // read the scale value
        int scale = buf.getInt();
        byte[] unscaledData = new byte[unscaledDataLength];
        buf.get(unscaledData);
        BigInteger unscaledValue = new BigInteger(unscaledData);
        BigDecimal value = new BigDecimal(unscaledValue, scale);
        javaField.set(model, value);
        // 'skip' the String padding.
        if (field.getSize() > unscaledDataLength)
        {
          byte[] padding = new byte[field.getSize() - unscaledDataLength];
          buf.get(padding);
        }
      }
      else
      {
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.WARNING_LEVEL, this, "Unknown field type: " + type);
      }
    }
    return model;
  }

  /**
         * Transforms a Model object data into a bytearray to be saved in the DB file.
         * @param mapping
         * @param model
         * @return
         */
  private byte[] buildData(JOTModelMapping mapping, JOTModel model)
  {
    ByteBuffer buf = ByteBuffer.allocate(mapping.getDataSize());
    Object[] values = model.getFieldValues(mapping, null);
    Hashtable fields = mapping.getFields();
    Enumeration e = fields.elements();
    buf.putLong(model.getId());
    int cpt = 1;
    while (e.hasMoreElements())
    {
      JOTDBField field = (JOTDBField) e.nextElement();
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Building data, field: :" + field.getFieldName() + " -> " + values[cpt].getClass());
      Object object = values[cpt];
      buildFieldData(buf, field, object);
      cpt++;
    }
    return buf.array();
  }

  public byte[] getSingleFieldData(JOTModelMapping mapping, JOTDBField field, Object fieldValue)
  {
    ByteBuffer buf = ByteBuffer.allocate(mapping.getFieldSize(field));
    buildFieldData(buf, field, fieldValue);
    return buf.array();
  }

  private void buildFieldData(ByteBuffer buf, JOTDBField field, Object object)
  {
    if (object instanceof Boolean)
    {
      byte value = (byte) (((Boolean) object).booleanValue() ? 1 : 0);
      buf.put(value);
    }
    else if (object instanceof Byte)
    {
      byte value = ((Byte) object).byteValue();
      buf.put(value);
    }
    else if (object instanceof Short)
    {
      short value = ((Short) object).shortValue();
      buf.putShort(value);
    }
    else if (object instanceof Integer)
    {
      int value = ((Integer) object).intValue();
      buf.putInt(value);
    }
    else if (object instanceof Long)
    {
      long value = ((Long) object).longValue();
      buf.putLong(value);
    }
    else if (object instanceof Float)
    {
      float value = ((Float) object).floatValue();
      buf.putFloat(value);
    }
    else if (object instanceof Double)
    {
      double value = ((Double) object).doubleValue();
      buf.putDouble(value);
    }
    else if (object instanceof String)
    {
      // write the length
      String value = (String) object;
      int stringLength = value.length();
      if (stringLength > field.getSize())
      {
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "String is too long, will be truncated: " + value);
        value = value.substring(0, field.getSize());
        stringLength = value.length();
      }
      buf.putInt(stringLength);
      // write the string
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.TRACE_LEVEL, this, "String value: " + value);
      buf.put(value.getBytes());
      // write the padding
      if (value.getBytes().length < field.getSize())
      {
        byte[] padding = new byte[field.getSize() - value.getBytes().length];
        buf.put(padding);
      }
    }
    else if (object instanceof java.sql.Date)
    {
      long value = ((java.sql.Date) object).getTime();
      buf.putLong(value);
    }
    else if (object instanceof java.sql.Time)
    {
      long value = ((Time) object).getTime();
      buf.putLong(value);
    }
    else if (object instanceof Timestamp)
    {
      long value = ((Timestamp) object).getTime();
      buf.putLong(value);
    }
    else if (object instanceof BigDecimal)
    {
      /* for a bigdecimal we will write:
				 - 4 bytes: length of unscaledvalue(biginteger) data
				 - 4 bytes: scale of the bigdecimal
				 - n bytes: data of the unscaledvalue(bigdecimal)
				 */
      BigDecimal decimal = (BigDecimal) object;
      BigInteger unscaled = decimal.unscaledValue();
      byte[] data = unscaled.toByteArray();
      if (data.length > field.getSize())
      {
        JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "BigDecimal is too long, can't be stored: " + data.length + " / " + field.getSize(), new Exception("BigDecimalTooLong"));
        data = new byte[0];
      }
      int scale = decimal.scale();
      buf.putInt(data.length);
      buf.putInt(scale);
      buf.put(data);
      //write the padding
      if (data.length < field.getSize())
      {
        byte[] padding = new byte[field.getSize() - data.length];
        buf.put(padding);
      }
    }
    else if (object instanceof java.util.Date)
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.WARNING_LEVEL, this, "Use java.sql.Date instead of java.util.Date: ");
    }
    else
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.WARNING_LEVEL, this, "Unknown field type: " + field.getClass());
    }

  }

  public Vector findUsingSQL(JOTTransaction transaction, JOTModelMapping mapping, Class objectClass, String sql, Object[] params, JOTStatementFlags flags) throws Exception
  {
    throw(new Exception("FindUsingSQL is not supported by jotfs !"));
    //return null;
  }

  // TODO: + provide a config entry to vacuum dbs dusing startup ie: db.fs.vacuum=user,toto,dada
	/**
         * Tries to vacuum the database
         * This reindexes the database and compacts it, resulting in smaller DB size and faster performance.
         * @param objectClass
         * @param simulate
         * @throws java.lang.Exception
         */

  public static synchronized void vacuum(Class objectClass, boolean simulate) throws Exception
  {
    JOTModelMapping mapping = JOTQueryManager.getMapping(objectClass);
    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.WARNING_LEVEL, JOTFSQueryImpl.class, "Starting vacuuming of: " + mapping.getTableName());


    // remove empty data entries and create new data file
    int keptData = 0;
    int droppedData = 0;
    String dbFolder = JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName());
    String table = mapping.getTableName() + TABLE_FILE_EXTENSION;
    File dataF = new File(dbFolder, table);
    RandomAccessFile dataFile = new RandomAccessFile(dataF, "rwd");

    dataFile.seek(0);
    long dataSize = mapping.getDataSize();

    String index = mapping.getTableName() + INDEX_FILE_EXTENSION;
    File indexF = new File(dbFolder, index);

    String newTable = mapping.getTableName() + TEMP_TABLE_FILE_EXTENSION;
    File newDataF = new File(dbFolder, newTable);
    RandomAccessFile newDataFile = new RandomAccessFile(newDataF, "rwd");
    String newIndex = mapping.getTableName() + TEMP_INDEX_FILE_EXTENSION;
    File newIndexF = new File(dbFolder, newIndex);
    RandomAccessFile newIndexFile = new RandomAccessFile(newIndexF, "rwd");
    newDataFile.seek(0);
    newIndexFile.seek(0);
    byte[] dataHolder = new byte[(int) dataSize - 8];

    if (dataFile.length() > 0)
    {
      for (int i = 0; i != dataFile.length() / dataSize; i++)
      {
        long id = dataFile.readLong();
        dataFile.readFully(dataHolder);

        if (id > 0)
        {
          long dataPtr = newDataFile.getFilePointer();
          // write the data
          newDataFile.writeLong(id);
          newDataFile.write(dataHolder);
          // write the index
          newIndexFile.writeLong(id);
          newIndexFile.writeLong(dataPtr);

          keptData++;
        }
        else
        {
          droppedData++;
        }
      }
    }
    else
    {
    // the data file is empty.
    }

    newIndexFile.close();
    newDataFile.close();

    if (!simulate)
    {
      // if both succeeded, make temp files, the active ones (except if simulate)
      dataFile.close();
      JOTUtilities.moveFile(new File(dataF.getAbsolutePath() + BACKUP_FILE_EXTENSION), dataF);
      JOTUtilities.moveFile(dataF, newDataF);
      JOTUtilities.moveFile(new File(indexF.getAbsolutePath() + BACKUP_FILE_EXTENSION), indexF);
      JOTUtilities.moveFile(indexF, newIndexF);
      // Forcing relaod of updated index/data files.
      JOTFSQueryImpl impl = (JOTFSQueryImpl) JOTQueryManager.getImplementation(JOTFSQueryImpl.class.getName());
      impl.reset();
    }

    System.out.println("Data records kept: " + keptData);
    System.out.println("Data records dropped: " + droppedData);
    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.WARNING_LEVEL, JOTFSQueryImpl.class, "Completed vacuuming of: " + mapping.getTableName() + " -> Rows removed:" + droppedData + "  Rows Kept: " + keptData);

  }

  private void reset()
  {
    indexFiles.clear();
    tableFiles.clear();
  }

  /**
         * Releases resources and open files.
         */
  public void destroy()
  {
    try
    {
      Collection c = indexFiles.values();
      Iterator i = c.iterator();
      while (i.hasNext())
      {
        RandomAccessFile rf = (RandomAccessFile) i.next();
        rf.close();
      }
      Collection c2 = tableFiles.values();
      Iterator i2 = c.iterator();
      while (i2.hasNext())
      {
        RandomAccessFile rf = (RandomAccessFile) i.next();
        rf.close();
      }
    }
    catch (Exception e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Could not close the tables/indexes : ", e);
    }
  }

  public void alterAddField(JOTModelMapping mapping, JOTDBField field, Object defaultValue) throws Exception
  {
    byte[] append = getSingleFieldData(mapping, field, defaultValue);

    // Same code as vacuum() except we add the extra field (and use the old rowSize)
    String dbFolder = JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName());
    String table = mapping.getTableName() + TABLE_FILE_EXTENSION;
    File dataF = new File(dbFolder, table);
    RandomAccessFile dataFile = new RandomAccessFile(dataF, "rwd");

    dataFile.seek(0);
    long oldDataSize=JOTModelMapping.readMetaFile(mapping).getRowSize();
    // hack for old versiuon that did not have row size in the metadata
    if(oldDataSize==-1)
    {
      oldDataSize=mapping.getDataSize()-append.length;
    }

    String index = mapping.getTableName() + INDEX_FILE_EXTENSION;
    File indexF = new File(dbFolder, index);

    String newTable = mapping.getTableName() + TEMP_TABLE_FILE_EXTENSION;
    File newDataF = new File(dbFolder, newTable);
    RandomAccessFile newDataFile = new RandomAccessFile(newDataF, "rwd");
    String newIndex = mapping.getTableName() + TEMP_INDEX_FILE_EXTENSION;
    File newIndexF = new File(dbFolder, newIndex);
    RandomAccessFile newIndexFile = new RandomAccessFile(newIndexF, "rwd");
    newDataFile.seek(0);
    newIndexFile.seek(0);
    byte[] dataHolder = new byte[(int) oldDataSize - 8];

    if (dataFile.length() > 0)
    {
      for (int i = 0; i != dataFile.length() / oldDataSize; i++)
      {
        long id = dataFile.readLong();
        dataFile.readFully(dataHolder);

        if (id > 0)
        {
          long dataPtr = newDataFile.getFilePointer();
          // write the old data
          newDataFile.writeLong(id);
          newDataFile.write(dataHolder);
          // write the new field
          newDataFile.write(append);
          // write the index
          newIndexFile.writeLong(id);
          newIndexFile.writeLong(dataPtr);
        }
      }
    }

    newIndexFile.close();
    newDataFile.close();

    JOTUtilities.moveFile(new File(dataF.getAbsolutePath() + BACKUP_FILE_EXTENSION), dataF);
    JOTUtilities.moveFile(dataF, newDataF);
    JOTUtilities.moveFile(new File(indexF.getAbsolutePath() + BACKUP_FILE_EXTENSION), indexF);
    JOTUtilities.moveFile(indexF, newIndexF);

    // Forcing relaod of updated index/data files.
    JOTFSQueryImpl impl = (JOTFSQueryImpl) JOTQueryManager.getImplementation(JOTFSQueryImpl.class.getName());
    impl.reset();
  }

  private void warnTransaction(JOTTransaction transaction)
  {
      if(transaction!=null)
          JOTLogger.info(this, "WARNING: Transactions not supported by JOTDB, will be ignored !");
  }

/*public void alterRemoveField(String fieldName) throws Exception
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }*/
}
