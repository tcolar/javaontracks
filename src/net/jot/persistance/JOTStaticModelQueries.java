/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.db.JOTDBField;
import net.jot.logger.JOTLogger;
import net.jot.persistance.query.JOTQueryInterface;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.utils.JOTUtilities;

/**
 *
 * @author tcolar
 */
abstract class JOTStaticModelQueries 
{
    /**
     * Simply use a manually built query (JOTQueryBuilder)
     * In my be simpler to directly call JOTQueryBuilder, which is what this does anyhow.
     * JOTQueryBuilder.findAll(User.class).where("name>'a'").exeute();
     * @param transaction
     * @param queryBuilder
     * @return
     * @throws java.lang.Exception
     */
  /*  public static Vector findByQuery(JOTTransaction transaction, JOTQueryBuilder queryBuilder) throws Exception
    {
        return JOTQueryManager.findByQuery(transaction, queryBuilder);
    }
    public static Vector findByQuery(JOTQueryBuilder queryBuilder) throws Exception
    {
        return JOTQueryManager.findByQuery(queryBuilder);
    }*/
    
    /**
     * Returns the record whith the given ID <br>
     * @param dataId
     * @return a JOTModel object or null if none found
     */
    /*public static JOTModel findByID(JOTTransaction transaction, Class modelClass, long id) throws Exception
    {
       return JOTQueryManager.findByID(transaction, modelClass, id); 
    }
    public static JOTModel findByID(Class modelClass, long id) throws Exception
    {
       return JOTQueryManager.findByID(modelClass, id);
    }*/
    /**
     * This is here, if you want to make manual custom SQL calls not covered by the other methods<br>
     * 
     * NOTE: your request MUST return records matching your model.<br>
     * 
     * @param sql   ie: "select * from 'users' where first=?, last=? order by name" ... etc ...
     * @param params ie: ['John','Doe']
     * @return a Vector of JOTModel objects 
     */
    /*public static Vector findUsingSQL(JOTTransaction transaction, Class modelClass, String sql, String[] params, JOTStatementFlags flags) throws Exception
    {
        return JOTQueryManager.findUsingSQL(transaction, modelClass, sql, params, flags);
    }
    public static Vector findUsingSQL(JOTTransaction transaction, Class modelClass, String sql, String[] params) throws Exception
    {
        return JOTQueryManager.findUsingSQL(transaction, modelClass, sql, params);
    }
    public static Vector findUsingSQL(Class modelClass, String sql, String[] params) throws Exception
    {
        return JOTQueryManager.findUsingSQL(modelClass, sql, params);
    }*/
    /**
     * Returns all the records matching the parameters<br>
     * @return a Vector of JOTModel objects
     */
    /*public static Vector find(JOTTransaction transaction, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return JOTQueryManager.find(transaction, modelClass, params);
    }
    public static Vector find(Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return JOTQueryManager.find(modelClass, params);
    }*/
    /**
     * Returns the first records matching the parameters<br>
     * @return a JOTModel object or null if none found
     */
    /*public static JOTModel findOne(JOTTransaction transaction, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return JOTQueryManager.findOne(transaction, modelClass, params);
    }
    public static JOTModel findOne(Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return JOTQueryManager.findOne(modelClass, params);
    }*/
    /**
     * Returns a record if it is found in the database
     * If it is not found then create a blank new one and return it.
     * @param modelClass
     * @param params
     * @return
     * @throws java.lang.Exception
     */
    /*public static JOTModel findOrCreateOne(JOTTransaction transaction, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return JOTQueryManager.findOrCreateOne(transaction, modelClass, params);
    }
    public static JOTModel findOrCreateOne(Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return JOTQueryManager.findOrCreateOne(modelClass, params);
    }

    public static void dumpAllToCSV(OutputStream out, Class modelClass) throws Exception
    {
        JOTQueryManager.dumpToCSV(out, modelClass);
    }*/

    /**
     * Dump a whole table (model) data into a stream(ie file) in CSV format
     * @param out
     * @param modelClass
     * @param params
     * @throws java.lang.Exception
     */
    /*public static void dumpAllToCSV(OutputStream out, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        JOTQueryManager.dumpToCSV(out, modelClass, params);
    }
*/
    public static void deleteWholeTable(Class modelClass) throws Exception
    {        
        JOTModelMapping mapping = JOTQueryManager.getMapping(modelClass);
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.deleteTable(mapping);
    }
    
        /**
     * Creates the table in the DB, if it doesn't exists yet
     */
    protected static void createTableIfNecessary(Class modelClass) throws Exception
    {
        JOTModelMapping mapping = JOTQueryManager.getMapping(modelClass);
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.createTable(mapping);
    }

    protected class HasMany
    {
        private Class modelClass=null;
        public HasMany(Class clazz) 
        { 
            this.modelClass = (Class)clazz; 
            System.out.println("Created a hasmany for class: "+modelClass.getName());
        }
    }
}
