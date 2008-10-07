/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance.query;

import net.jot.persistance.JOTTransaction;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTQueryResult;
import net.jot.persistance.JOTStatementFlags;

/**
 * Low(medium) level database access
 * Usually you would rather use JOTMOdel methods or the JOTQueryBuilder
 * unless you want to make a completely manual SQL query
 * @author thibautc
 * 
 *
 */
public final class JOTQueryManager
{

    private static Hashtable modelMappings = new Hashtable();
    private static Hashtable implementations = new Hashtable();

    /*public static void delete(JOTTransaction transaction,JOTModel model) throws Exception
    {        
        JOTModelMapping mapping = getMapping(model.getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.delete(transaction, model);
    }*/
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
     * This is here, if you want to make manual custom SQL calls not covered by the other methods<br>
     * 
     * NOTE: your request MUST return records matching your model.<br>
     * 
     * @param sql   ie: "select * from 'users' where first=?, last=? order by name" ... etc ...
     * @param params ie: ['John','Doe']
     * @return a Vector of JOTModel objects 
     */
    public static JOTQueryResult executeSQL(JOTTransaction transaction, Class modelClass, String sql, String[] params, JOTStatementFlags flags) throws Exception
    {
        JOTModelMapping mapping = getMapping(modelClass);
        JOTQueryInterface impl = getImplementation(mapping.getQueryClassName());
        return impl.executeSQL(transaction,mapping, modelClass, sql, params, flags);
    }
    public static void updateSQL(JOTTransaction transaction, Class modelClass, String sql, Object[] params, JOTStatementFlags flags) throws Exception
    {
        JOTModelMapping mapping = getMapping(modelClass);
        JOTQueryInterface impl = getImplementation(mapping.getQueryClassName());
        impl.updateSQL(transaction,mapping, sql, params, flags);
    }

    public static String getTableName(Class modelClass)
    {
        String name=null;
        try
        {
            JOTModelMapping mapping = getMapping(modelClass);
            name=mapping.getTableName();
        }
        catch(Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_DB, JOTQueryManager.class, "SQL Safe Table name is missing ??",e);
        }
        return name;
    }
    
}
