/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance.query;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTQueryBuilder;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.JOTStatementFlags;
import net.jot.utils.JOTUtilities;

/**
 * This is the "Main" Object for retrieving records from the persistance/database.<br>
 * Ie: Vector users=JOTQueryManager.findUsingSQL(User.class, "SELECT * FROM mytable", null);
 * 
 * @author thibautc
 * 
 *
 */
public final class JOTQueryManager
{

    private static Hashtable modelMappings = new Hashtable();
    private static Hashtable implementations = new Hashtable();

    public static void delete(JOTTransaction transaction,JOTModel model) throws Exception
    {
        JOTModelMapping mapping = getMapping(model.getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.delete(transaction, model);
    }

    public static void save(JOTTransaction transaction, JOTModel model) throws Exception
    {
        JOTModelMapping mapping = getMapping(model.getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.save(transaction, model);
    }

    /**
     * 
     * @param modelClass
     * @return
     * @throws java.lang.Exception
     */
    public static JOTModelMapping getMapping(Class modelClass) throws Exception
    {
        return getMapping(modelClass, true, true);
    }

    /**
     * Internal Method, that usually would not be called by end-user.<br>
     * Get/Loads a Model mapping.<br>
     * modelClass MUST be of type JOTModel or subclass
     * @param modelClass
     * @return
     * @throws Exception
     */
    public static JOTModelMapping getMapping(Class modelClass, boolean runValidation, boolean createMissingTables) throws Exception
    {
        String className = modelClass.getName();
        if (!modelMappings.containsKey(className))
        {
            synchronized (JOTQueryManager.class)
            {
                if (!modelMappings.containsKey(className))
                {
                    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, JOTModelMapping.class, "will create a new instance of: " + modelClass.getName());
                    Object modelImpl = modelClass.newInstance();
                    JOTModel model = (JOTModel) modelImpl;
                    model.initQueryImplClass();
                    Class impl = model.getQueryImplClass();
                    if (!implementations.containsKey(impl.getName()))
                    {
                        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, JOTModelMapping.class, "Adding impl: " + impl.getName());
                        implementations.put(impl.getName(), (JOTQueryInterface) impl.newInstance());
                    }
                    JOTModelMapping mapping = model.getMapping();
                    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, JOTModelMapping.class, "Adding mapping for : " + className);
                    modelMappings.put(className, mapping);
                }
            }

        }
        return (JOTModelMapping) modelMappings.get(className);
    }

    /**
     * Internal Method, Not to be used by end-user.<br>
     * Returns the QueryImplementation object, which deals with raw CRUD implementation.
     * @param className
     * @return
     * @throws Exception
     */
    public static JOTQueryInterface getImplementation(String className) throws Exception
    {
        // If this is called before the getmapping, it will return null.
        // this should never happen, so return null if it ever does.
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.TRACE_LEVEL, JOTModelMapping.class, "Retrieving impl : " + className);
        return (JOTQueryInterface) implementations.get(className);
    }

    /**
     * Simply use a manually built query (JOTQueryBuilder)
     * In my be simpler to directly call JOTQueryBuilder, which is what this does anyhow.
     * JOTQueryBuilder.findAll(User.class).where("name>'a'").exeute();
     * @param transaction
     * @param queryBuilder
     * @return
     * @throws java.lang.Exception
     */
    public static Vector findByQuery(JOTTransaction transaction, JOTQueryBuilder queryBuilder) throws Exception
    {
        return queryBuilder.execute(transaction);
    }
    public static Vector findByQuery(JOTQueryBuilder queryBuilder) throws Exception
    {
        return findByQuery(null, queryBuilder);
        //TODO
    }
    
    /**
     * Returns the record whith the given ID <br>
     * @param dataId
     * @return a JOTModel object or null if none found
     */
    public static JOTModel findByID(JOTTransaction transaction, Class modelClass, long id) throws Exception
    {
        JOTModelMapping mapping = getMapping(modelClass);
        JOTQueryInterface impl = getImplementation(mapping.getQueryClassName());
        return impl.findByID(transaction,mapping, modelClass, id);
    }
    public static JOTModel findByID(Class modelClass, long id) throws Exception
    {
       return findByID(null,modelClass, id);
    }
    /**
     * This is here, if you want to make manual custom SQL calls not covered by the other methods<br>
     * 
     * NOTE: your request MUST return records matching your model.<br>
     * 
     * @param sql   ie: "select * from 'users' where first=?, last=? order by name" ... etc ...
     * @param params ie: ['John','Doe']
     * @return a Vector of JOTModel objects 
     */
    public static Vector findUsingSQL(JOTTransaction transaction, Class modelClass, String sql, String[] params, JOTStatementFlags flags) throws Exception
    {
        JOTModelMapping mapping = getMapping(modelClass);
        JOTQueryInterface impl = getImplementation(mapping.getQueryClassName());
        return impl.findUsingSQL(transaction,mapping, modelClass, sql, params, flags);
    }
    public static Vector findUsingSQL(JOTTransaction transaction, Class modelClass, String sql, String[] params) throws Exception
    {
        return findUsingSQL(transaction, modelClass, sql, params, null);
    }
    public static Vector findUsingSQL(Class modelClass, String sql, String[] params) throws Exception
    {
        return findUsingSQL(null, modelClass, sql, params, null);
    }
    /**
     * Returns all the records matching the parameters<br>
     * @return a Vector of JOTModel objects
     */
    public static Vector find(JOTTransaction transaction, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        JOTModelMapping mapping = getMapping(modelClass);
        JOTQueryInterface impl = getImplementation(mapping.getQueryClassName());
        return impl.find(transaction, mapping, modelClass, params);
    }
    public static Vector find(Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return find(null, modelClass, params);
    }
    /**
     * Returns the first records matching the parameters<br>
     * @return a JOTModel object or null if none found
     */
    public static JOTModel findOne(JOTTransaction transaction, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        JOTModelMapping mapping = getMapping(modelClass);
        JOTQueryInterface impl = getImplementation(mapping.getQueryClassName());
        return impl.findOne(null,mapping, modelClass, params);
    }
    public static JOTModel findOne(Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return findOne(null, modelClass, params);
    }
    /**
     * Returns a record if it is found in the database
     * If it is not found then create a blank new one and return it.
     * @param modelClass
     * @param params
     * @return
     * @throws java.lang.Exception
     */
    public static JOTModel findOrCreateOne(JOTTransaction transaction, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        JOTModel model = findOne(null,modelClass, params);
        if (model == null)
        {
            model = (JOTModel) modelClass.newInstance();
        }
        return model;
    }
    public static JOTModel findOrCreateOne(Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        return findOrCreateOne(null, modelClass, params);
    }

    public static void dumpToCSV(OutputStream out, Class modelClass) throws Exception
    {
        dumpToCSV(out, modelClass, null);
    }

    /**
     * Dump a table (model) data into a stream(ie file) in CSV format
     * @param out
     * @param modelClass
     * @param params
     * @throws java.lang.Exception
     */
    public static void dumpToCSV(OutputStream out, Class modelClass, JOTSQLQueryParams params) throws Exception
    {
        Vector results = find(modelClass, params);
        PrintWriter p = new PrintWriter(out);
        JOTModelMapping mapping = getMapping(modelClass);
        // write the meata on the first line
        Hashtable fields = mapping.getFields();
        Enumeration fieldNames = fields.keys();
        String header = "\"" + mapping.getPrimaryKey() + "\",";
        while (fieldNames.hasMoreElements())
        {
            header += JOTUtilities.encodeCSVEntry((String) fieldNames.nextElement()) + ",";
        }
        header = header.substring(0, header.length() - 1);
        p.println(header);
        // writes the data in csv format
        for (int i = 0; i != results.size(); i++)
        {
            // handle a line of data
            JOTModel model = (JOTModel) results.get(i);
            String line = "\"" + model.getId() + "\",";
            fieldNames = fields.keys();
            while (fieldNames.hasMoreElements())
            {
                String value = model.getFieldValue((String) fieldNames.nextElement()).toString();
                line += JOTUtilities.encodeCSVEntry(value) + ",";
            }
            line = line.substring(0, line.length() - 1);
            p.println(line);
        }
        p.flush();
    }

}
