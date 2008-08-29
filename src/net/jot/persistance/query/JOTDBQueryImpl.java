/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance.query;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.jot.db.JOTDBField;
import net.jot.db.JOTDBManager;
import net.jot.db.JOTTaggedConnection;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLOrderBy;
import net.jot.persistance.JOTSQLQueryParams;

/**
 * Implementation of The Query Interface for an SQL database.<br>
 * Handle all the SQL CRUD functions
 * Transforms DB java objects into actual SQL queries string and back.
 * @author thibautc
 *
 */
public class JOTDBQueryImpl implements JOTQueryInterface
{

  public JOTModel findByID(JOTModelMapping mapping, Class objectClass, long id) throws Exception
  {
    JOTSQLQueryParams params = new JOTSQLQueryParams();
    params.addCondition(new JOTSQLCondition(mapping.getPrimaryKey(), JOTSQLCondition.IS_EQUAL, new Long(id)));
    Vector v = find(mapping, objectClass, params);
    if (v != null && v.size() > 0)
    {
      return (JOTModel) v.get(0);
    }
    return null;
  }

  public Vector findUsingSQL(JOTModelMapping mapping, Class objectClass, String sql, Object[] params) throws Exception
  {
    ResultSet rs = null;
    JOTTaggedConnection con = JOTDBManager.getInstance().getConnection(mapping.getStorageName());
    try
    {
      rs = JOTDBManager.getInstance().query(con, sql, params);
    }
    catch (SQLException e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "JOTDBModel", "Error", e);
      throw (e);
    }
    finally
    {
      JOTDBManager.getInstance().releaseConnection(con);
    }
    return LoadFromRS(mapping, objectClass, rs);
  }

  public Vector find(JOTModelMapping mapping, Class objectClass, JOTSQLQueryParams params) throws Exception
  {
    if (params == null)
    {
      params = new JOTSQLQueryParams();
    }
    String sql = buildSelectString(mapping, params);
    // creating the parameters
    JOTSQLCondition[] conds = params.getConditions();
    Object[] sqlParams = new Object[conds.length];
    for (int i = 0; i != conds.length; i++)
    {
      sqlParams[i] = conds[i].getValue();
    }
    return findUsingSQL(mapping, objectClass, sql, sqlParams);
  }

  public JOTModel findOne(JOTModelMapping mapping, Class objectClass, JOTSQLQueryParams params) throws Exception
  {
    if (params == null)
    {
      params = new JOTSQLQueryParams();
    }
    params.setLimit(1);
    Vector v = find(mapping, objectClass, params);
    if (v != null && v.size() > 0)
    {
      return (JOTModel) v.get(0);
    }
    return null;
  }

  /**
	 * Trandform a db resultset into an JOTModel object array
	 * @param rs
	 * @return
	 */
  protected static Vector LoadFromRS(JOTModelMapping mapping, Class objectClass, ResultSet rs) throws Exception
  {
    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, "JOTDBModel", "*** loadFromRS() ***");
    Vector v = new Vector();
    if (rs != null)
    {
      ResultSetMetaData meta = rs.getMetaData();
      Vector columns = new Vector();
      for (int i = 1; i <= meta.getColumnCount(); i++)
      {
        columns.add(meta.getColumnName(i));
      }
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, "JOTDBModel", "Columns list from DB metadata: " + columns);
      while (rs.next())
      {
        JOTModel model = (JOTModel) objectClass.newInstance();
        // dealing with the 'dataId' first
        if (columns.contains(mapping.getPrimaryKey()))
        {
          model.setId((int) rs.getLong(mapping.getPrimaryKey()));
        }
        else
        {
          // Not good !
          throw (new Exception("Primary key missing from database !! :" + mapping.getPrimaryKey()));
        }
        // then dealing with all other user fields
        Hashtable fields = mapping.getFields();
        //fields.put("dataId",new JOTDBField("BIGINT",primaryKey));
        Enumeration e = fields.keys();
        while (e.hasMoreElements())
        {
          String javaField = (String) e.nextElement();
          JOTDBField dbField = (JOTDBField) fields.get(javaField);
          if (columns.contains(dbField.getFieldName()))
          {
            Object value = rs.getObject(dbField.getFieldName());
            Field f = model.getClass().getField(javaField);
            f.set(model, value);
          }
          else
          {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, "JOTDBModel", "Field " + dbField.getFieldName() + " was not found in the java object, ignored.");
          }
        }
        v.add(model);
      }
    }
    return v;
  }

  /**
	 * Create the full SELECT SQL query String
	 * @param mapping
	 * @param params
	 * @return
	 */
  public String buildSelectString(JOTModelMapping mapping, JOTSQLQueryParams params)
  {
    String query = "SELECT * FROM \"" + mapping.getTableName() + "\"";
    if (params != null)
    {
      query += getConditionsString(params.getConditions());
      query += getOrderByString(params.getOrderBys());
      query += getLimitString(params.getLimit());
    }
    return query;
  }

  public String getLimitString(int limit)
  {
    String s = "";
    if (limit > 0)
    {
      s = " LIMIT " + limit;
    }
    return s;
  }

  /**
	 * Creates the "orderBy" part of the SQL string.
	 * @param orderBys
	 * @return
	 */
  public String getOrderByString(JOTSQLOrderBy[] orderBys)
  {
    String orderByString = "";
    if (orderBys.length > 0)
    {
      orderByString += " ORDER BY ";
    }
    for (int i = 0; i != orderBys.length; i++)
    {
      orderByString += "\"" + orderBys[i].getField() + "\"";
      if (orderBys[i].getDirection() == JOTSQLOrderBy.DESCENDING)
      {
        orderByString += " DESC";
      }
      if (orderBys.length > i + 1)
      {
        orderByString += ", ";
      }
    }
    return orderByString;
  }

  /**
	 * Creates the condition part of the SQL String (WHERE ...)
	 * @param conds
	 * @return
	 */
  public String getConditionsString(JOTSQLCondition[] conds)
  {
    String condString = "";
    if (conds != null && conds.length > 0)
    {
      condString += " WHERE ";
      for (int i = 0; i != conds.length; i++)
      {
        JOTSQLCondition cond = conds[i];
        condString += "\"" + cond.getField() + "\" " + cond.getSQLComparator();
        if (conds.length > i + 1)
        {
          condString += " AND ";
        }
      }
    }
    return condString;
  }

  /**
	 * Create the full INSERT SQL query String
	 * @param mapping
	 * @return
	 */
  public String buildInsertString(JOTModelMapping mapping)
  {
    // no conditions on an insert
    return "INSERT INTO \"" + mapping.getTableName() + "\" (" + getInsertString(mapping) + ") VALUES(" + buildQmarksString(mapping) + ")";
  }

  /**
	 * Create the full UPDATE SQL query String
	 * @param mapping
	 * @param conds
	 * @return
	 */
  public String buildUpdateString(JOTModelMapping mapping, JOTSQLCondition[] conds)
  {
    String query = "UPDATE \"" + mapping.getTableName() + "\" SET " + getUpdateString(mapping);
    query += getConditionsString(conds);
    return query;
  }

  /**
	 * Create the full DELETE SQL query String
	 * @param mapping
	 * @param conds
	 * @return
	 */
  public String buildDeleteString(JOTModelMapping mapping, JOTSQLCondition[] conds)
  {
    String query = "DELETE FROM \"" + mapping.getTableName() + "\"";
    query += getConditionsString(conds);
    return query;
  }

  /**
	 * Creates the SQL UPDATE string
	 * @param mapping
	 * @return
	 */
  public String getUpdateString(JOTModelMapping mapping)
  {
      return mapping.getUpdateString();
  }

  /**
	 * Creates the question marks (paramters of the createSatement) String
	 * @param mapping
	 * @return
	 */
  public String buildQmarksString(JOTModelMapping mapping)
  {
    int nbParams = mapping.getFields().size();
    String qmarks = "?,";
    for (int i = 0; i != nbParams; i++)
    {
      qmarks += "?,";
    }
    if (qmarks.length() > 0)
    {
      qmarks = qmarks.substring(0, qmarks.length() - 1);
    }

    return qmarks;
  }

  public void createTable(JOTModelMapping mapping) throws Exception
  {
    JOTTaggedConnection con = JOTDBManager.getInstance().getConnection(mapping.getStorageName());
    try
    {
      if (!JOTDBManager.getInstance().tableExists(mapping.getStorageName(), mapping.getTableName()))
      {
        String columns = getColumnsDefinition(mapping);
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Trying to create the table in the DB if missing)");
        JOTDBManager.getInstance().update(con, "CREATE TABLE \"" + mapping.getTableName() + "\"(" + columns + ")");
        JOTDBManager.getInstance().update(con, "ALTER TABLE \"" + mapping.getTableName() + "\" ADD PRIMARY KEY (\"" + mapping.getPrimaryKey() + "\")");
        JOTModelMapping.writeMetaFile(mapping);
    }
    }
    catch (Exception e)
    {
      throw (e);
    }
    finally
    {
      JOTDBManager.getInstance().releaseConnection(con);
    }
  }

  public void deleteTable(JOTModelMapping mapping) throws Exception
  {
    JOTTaggedConnection con = JOTDBManager.getInstance().getConnection(mapping.getStorageName());
    try
    {
      if (JOTDBManager.getInstance().tableExists(mapping.getStorageName(), mapping.getTableName()))
      {
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Trying to delete the table in the DB)");
        JOTDBManager.getInstance().update(con, "DROP TABLE \"" + mapping.getTableName() + "\"");
        JOTModelMapping.deleteMetaFile(mapping);
    }
    }
    catch (Exception e)
    {
      throw (e);
    }
    finally
    {
      JOTDBManager.getInstance().releaseConnection(con);
    }
  }
  /**
	 * Creates the SQL INSERT string
	 * @param mapping
	 * @return
	 */

  public String getInsertString(JOTModelMapping mapping)
  {
    return mapping.getInsertString();
  }

  /**
	 * Creates the "column definition" string, used to create the table<br>
	 * ie:  "dataId BIGINT, name varchar(80)"
	 * @param mapping
	 * @return
	 */
  public String getColumnsDefinition(JOTModelMapping mapping)
  {
    String columns = "\"" + mapping.getPrimaryKey() + "\" BIGINT";
    Enumeration e = mapping.getFields().elements();
    while (e.hasMoreElements())
    {
      JOTDBField field = (JOTDBField) e.nextElement();
      columns += ", ";
      columns += getColumnDefinition(field);
    }

    return columns;
  }

  public String getColumnDefinition(JOTDBField field)
  {
      String column = "\"" + field.getFieldName() + "\"";
      column += " ";
      column += field.getFieldType();
      if (field.getSize() > -1)
      {
        column += "(" + field.getSize() + ")";
      }   
      return column;
  }
  
  public void delete(JOTModel model) throws Exception
  {
    JOTModelMapping mapping = model.getMapping();
    JOTTaggedConnection con = JOTDBManager.getInstance().getConnection(mapping.getStorageName());
    try
    {
      JOTSQLQueryParams params = new JOTSQLQueryParams();
      params.addCondition(new JOTSQLCondition(mapping.getPrimaryKey(), JOTSQLCondition.IS_EQUAL, new Integer((int) model.getId())));
      String deleteString = buildDeleteString(mapping, params.getConditions());
      Object[] paramValues = {new Integer((int) model.getId())};
      JOTDBManager.getInstance().update(con, deleteString, paramValues);
    }
    catch (SQLException e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Error", e);
      throw (e);
    }
    finally
    {
      JOTDBManager.getInstance().releaseConnection(con);
    }

  }

  /**
	 * Save/update the table in the database.
	 */
  public void save(JOTModel model) throws Exception
  {
    JOTModelMapping mapping = model.getMapping();
    JOTTaggedConnection con = JOTDBManager.getInstance().getConnection(mapping.getStorageName());
    try
    {
      if (model.getId() == -1)
      {
        // create
        try
        {
          model.setId(JOTDBManager.getInstance().nextVal(con, mapping.getTableName() + "_cpt"));
        }
        catch (SQLException e)
        {
          JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Error getting NEXTVAL()", e);
          JOTDBManager.getInstance().releaseConnection(con);
          throw (e);
        }
        try
        {
          Object[] paramValues = model.getFieldValues(mapping, null);
          String insertString = buildInsertString(mapping);
          JOTDBManager.getInstance().update(con, insertString, paramValues);
        }
        catch (SQLException e)
        {
          JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Error during INSERT", e);
          JOTDBManager.getInstance().releaseConnection(con);
          throw (e);
        }
      // if failure revert the dataId to -1 ?	
      }
      else
      {
        // update
        try
        {
          JOTSQLQueryParams params = new JOTSQLQueryParams();
          params.addCondition(new JOTSQLCondition(mapping.getPrimaryKey(), JOTSQLCondition.IS_EQUAL, new Integer((int) model.getId())));
          String updateString = buildUpdateString(mapping, params.getConditions());
          Object[] paramValues = model.getFieldValues(mapping, params.getConditions());
          JOTDBManager.getInstance().update(con, updateString, paramValues);
        }
        catch (SQLException e)
        {
          JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Error during UPDATE", e);
          JOTDBManager.getInstance().releaseConnection(con);
          throw (e);
        }
      }
    }
    catch (SQLException e)
    {
      throw (e);
    }
    finally
    {
      JOTDBManager.getInstance().releaseConnection(con);
    }
  }

  public void alterAddField(JOTModelMapping mapping, JOTDBField field, Object defaultValue) throws Exception
  {
    String[] params={mapping.getTableName()};   
    sqlExecute(mapping, "ALTER TABLE ? ADD "+getColumnDefinition(field), params);
  }

  /*public void alterRemoveField(String fieldName) throws Exception
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }*/
  private void sqlExecute(JOTModelMapping mapping, String sql, Object[] params) throws Exception
  {
    JOTTaggedConnection con = JOTDBManager.getInstance().getConnection(mapping.getStorageName());
    try
    {
      JOTDBManager.getInstance().update(con, sql, params);
    }
    catch (SQLException e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "JOTDBModel", "Error", e);
      throw (e);
    }
    finally
    {
      JOTDBManager.getInstance().releaseConnection(con);
    }

  }

 
}
