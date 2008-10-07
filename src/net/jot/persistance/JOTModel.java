/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import net.jot.persistance.builders.JOTQueryBuilder;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.util.logging.LogManager;
import net.jot.JOTInitializer;
import net.jot.db.JOTDBField;
import net.jot.db.JOTDBJDBCSetup;
import net.jot.logger.JOTLogger;
import net.jot.persistance.query.JOTDBQueryImpl;
import net.jot.persistance.query.JOTFSQueryImpl;
import net.jot.persistance.query.JOTQueryInterface;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.persistance.JOTTransaction;

/**
 * Generic Persistance model<br>
 * Implementation for example include DB, filesystem etc ...<br>
 * If the table doesn't exists yet, it will be created automatically.
 * Note that if you change your model (add fields, rename fields etc.. you will have to handle changes in the upgrade() method.
 * @author Thibaut Colar http://jot.colar.net/
 *
 * 
 * TODO: support/use transactionnal db queries
 * TODO: support join ?
 * TODO: if field is transient or starts with __ then ignore as a data field
 * TODO: hasOne hasMany belongTo etc ... ?
 */
public abstract class JOTModel extends JOTModelAddons
{
    /**
     *  The "id" of the object in the database (ie: primary key)
     */
    protected long id = -1;

    // lowercase -> actual field name mapping
    private transient Hashtable fieldNames;
    protected transient Class queryImplClass = null;
    private transient String storage;
    public transient final static String DEFAULT_STORAGE = "default";
    private transient JOTModelMapping mapping = null;

    /**
     * Called to get the table mapping.
     * This calls all the customization methods in the process.
     * Also creates the table if it hasn't been done yet.
     * @return
     * @throws Exception
     */
    public final JOTModelMapping getMapping() throws Exception
    {
        if (mapping == null)
        {
            synchronized (this)
            {
                initQueryImplClass();
                mapping = new JOTModelMapping();
                mapping.defineTableName(getClass().getSimpleName());
                mapping.setStorageName(storage);
                mapping.setQueryClassName(queryImplClass.getName());
                loadFields(mapping);
                customize(mapping);
                createTableIfNecessary(mapping);
                if (!JOTPersistanceManager.getInstance().isDBUpgradeRunning())
                {
                    // during an upgrade this would fail.
                    validateMetadata(mapping);
                }
            }
        }
        return mapping;
    }

    /**
     * Implements the customization (User defined tables modifications, ie: fields mappings, etc ...)
     * @param mapping
     */
    protected abstract void customize(JOTModelMapping mapping);

    /**
     * Returns the "storage" to be used by this Model.
     * default is to return JOTModel.DEFAULT_STORAGE (to use the "default" storage).
     * Ovveride if needed 
     * @param storageName
     */
    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    private void injectHasManys()
    {
        Field[] fields=getClass().getFields();
        for(int i=0;i!=fields.length;i++)
        {
            if(fields[i].getType().equals(HasMany.class))
            {
                try
                {
                HasMany hasMany=(HasMany)fields[i].get(this);
                if(hasMany==null)
                    hasMany=new HasMany(getClass());
                //TODO: set id or where close etc...
                hasMany.init(this);
                fields[i].set(this, hasMany);
                }
                catch(IllegalAccessException e)
                {
                    JOTLogger.logException(this, "Failed to 'inject' hasMany !", e);
                }
            }
        }
    }

    
    /**
     * get the list of fields from the class 
     */
    private void loadFields(JOTModelMapping mapping)
    {
        Hashtable fieldsHash = new Hashtable();
        String[] ignoredFields = mapping.getIgnoredFields();
        Hashtable mappedFields = mapping.getMappedFields();

        Field[] fields = getClass().getFields();
        for (int i = 0; i != fields.length; i++)
        {
            String fieldClass = fields[i].getType().getName();
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Field " + fields[i].getName() + " is " + fieldClass);
            String type = (String) JOTDBField.getTypes().get(fieldClass);
            if (type == null)
            {
                JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Unrecognized field type: " + fields[i].getName() + " -> Using VARCHAR");
                type = "VARCHAR";
            }
            String field = fields[i].getName();
            
            boolean skip=Modifier.isTransient(fields[i].getModifiers());
            skip=skip || field.startsWith("__");
            
            if (!skip)
            {
                boolean ignore = false;
                for (int j = 0; j != ignoredFields.length; j++)
                {
                    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Field: " + field + " VS " + ignoredFields[j]);
                    if (ignoredFields[j].equalsIgnoreCase(field))
                    {
                        ignore = true;
                        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Ignoring field: " + field);
                    }
                }

                if (!ignore)
                {
                    String mappedValue = (String) mappedFields.get(field);
                    String defaultValue = null;
                    try
                    {
                        defaultValue = (String) fields[i].get(null);
                    } catch (Exception e)
                    {
                    }
                    if (mappedValue == null)
                    {
                        mappedValue = field;
                    }
                    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Adding field: " + field + " mapped as: " + mappedValue);
                    JOTDBField dbField = new JOTDBField(type, mappedValue);
                    dbField.setDefaultValue(defaultValue);
                    fieldsHash.put(field, dbField);
                }
            }
        }
        mapping.setFields(fieldsHash);

    }

    /**
     * Return the fields values
     * @param mapping
     * @param conds
     * @return
     */
    public Object[] getFieldValues(JOTModelMapping mapping, JOTSQLCondition[] conds)
    {
        Vector results = new Vector();
        results.add(new Long(id));
        Enumeration e = mapping.getFields().keys();
        while (e.hasMoreElements())
        {
            String fieldName = (String) e.nextElement();
            try
            {
                Object value = getFieldValue(fieldName);
                results.add(value);
            } catch (Exception ex)
            {
                JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Could not get the value of " + fieldName, ex);
            }
        }
        if (conds != null && conds.length > 0)
        {
            for (int i = 0; i != conds.length; i++)
            {
                JOTSQLCondition cond = conds[i];
                results.add(cond.getValue());
            }
        }

        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.TRACE_LEVEL, this, "Request values: " + results);
        return (Object[]) results.toArray(new Object[0]);
    }

    public Object getFieldValue(String fieldName)
    {
        if (fieldNames == null)
        {
            synchronized (this)
            {
                fieldNames = new Hashtable();
                Field[] fields = getClass().getFields();
                for (int i = 0; i != fields.length; i++)
                {
                    fieldNames.put(fields[i].getName(), fields[i].getName());
                }
            }
        }
        Object value = null;
        try
        {
            String realName = (String) fieldNames.get(fieldName);
            if (realName == null)
            {
                throw new Exception("Could not find real name of field: " + fieldName + "!!");
            }
            value = getClass().getField(realName).get(this);
        } catch (Exception ex)
        {
            JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Could not get the value of " + fieldName, ex);
        }
        return value;
    }

    public long getId()
    {
        return id;
    }

    public final void setId(int id)
    {
        this.id = id;
        injectHasManys();
    }

    /**
     * Deletes the coresponding record from the database
     */
    public void delete(JOTTransaction transaction) throws Exception
    {
        JOTQueryBuilder.deleteQuery(getClass()).delete();
    }

    public void delete() throws Exception
    {
        delete(null);
    }

    /**
     * Save/update the table in teh database.
     */
    public void save(JOTTransaction transaction) throws Exception
    {
        JOTQueryManager.save(transaction, this);
    }

    public void save() throws Exception
    {
        save(null);
    }

    /**
     * Creates the table in the DB, if it doesn't exists yet
     */
    private void createTableIfNecessary(JOTModelMapping mapping) throws Exception
    {
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.createTable(mapping);
    }

    public boolean isNew()
    {
        return id == -1;
    }

    /**
     * Deletes the table in the DB
     */
    /*protected void deleteTable() throws Exception
    {
        JOTModelMapping mapping = JOTQueryManager.getMapping(getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.deleteTable(mapping);
    }*/

    public void initQueryImplClass() throws Exception
    {
        storage = defineStorage();
        Object db = JOTPersistanceManager.getInstance().getDatabases().get(storage);
        if (db != null)
        {
            if (db instanceof JOTDBFSSetup)
            {
                queryImplClass = JOTFSQueryImpl.class;
            } else if (db instanceof JOTDBJDBCSetup)
            {
                queryImplClass = JOTDBQueryImpl.class;
            } else
            {
                throw new Exception("DB type unsuppported: " + storage + " !");
            }
        } else
        {
            String name=JOTInitializer.getInstance().isTestMode()?"test":storage;
            throw new Exception("Storage(DB): '" + name + "' is not defined in jot.properties!!");
        }
    }

    public Class getQueryImplClass()
    {
        return queryImplClass;
    }

    private void validateMetadata(JOTModelMapping mapping) throws Exception
    {
        JOTModelMeta meta = JOTModelMapping.readMetaFile(mapping);
        if (meta.getRowSize() != -1 && meta.getRowSize() != mapping.getDataSize())
        {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Model RowLength: " + mapping.getDataSize() + " Metadata RowLength:" + meta.getRowSize());
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "You probably changed your model and forgot to handle it in your JOTDBUpdater impl.");
            throw new Exception("Model mapping does not match metdata for table: " + mapping.getTableName());
        }
    }
 
}
