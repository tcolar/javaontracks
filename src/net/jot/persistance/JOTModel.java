/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.jot.db.JOTDBField;
import net.jot.db.JOTDBJDBCSetup;
import net.jot.logger.JOTLogger;
import net.jot.persistance.query.JOTDBQueryImpl;
import net.jot.persistance.query.JOTFSQueryImpl;
import net.jot.persistance.query.JOTQueryInterface;
import net.jot.persistance.query.JOTQueryManager;

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
public abstract class JOTModel
{
    // lowercase -> actual field name mapping
    private Hashtable fieldNames;
    /**
     *  The "dataId" of the object in the database (ie: primary key)
     */
    protected long dataId = -1;
    protected Class queryImplClass = null;
    private String storage;
    protected final static String DEFAULT_STORAGE = "default";
    
    private /*transient*/ JOTModelMapping mapping = null;

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
                mapping.defineTableName(getSimpleClassName(getClass().getName()));
                mapping.setStorageName(storage);
                mapping.setQueryClassName(queryImplClass.getName());
                loadFields(mapping);
                customize(mapping);
                createTableIfNecessary(mapping);
                if (!JOTPersistanceManager.isDBUpgradeRunning())
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
    public abstract void customize(JOTModelMapping mapping);

    /**
     * Returns the "storage" to be used by this Model.
     * default is to return JOTModel.DEFAULT_STORAGE (to use the "default" storage). 
     * @param storageName
     */
    public abstract String defineStorage();

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
            if (field.startsWith("data"))
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
                        mappedValue = field.toLowerCase();
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
        results.add(new Long(dataId));
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
                JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Could not get the value of " + fieldName , ex);
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
        if(fieldNames==null)
        {
            synchronized(this)
            {
                fieldNames=new Hashtable();
                Field[] fields=getClass().getFields();
                for(int i=0;i!=fields.length;i++)
                {
                    fieldNames.put(fields[i].getName().toLowerCase(),fields[i].getName());
                }
            }
        }
        Object value = null;
        try
        {
            String realName=(String)fieldNames.get(fieldName.toLowerCase());
            if(realName==null)
            {
                throw new Exception("Could not find real name of field: "+fieldName+"!!");
            }
            value = getClass().getField(realName).get(this);
        } catch (Exception ex)
        {
            JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Could not get the value of " + fieldName , ex);
        }
        return value;
    }

    public long getId()
    {
        return dataId;
    }

    public void setId(int id)
    {
        this.dataId = id;
    }

    /**
     * Deletes the coresponding record from the database
     */
    public void delete() throws Exception
    {
        JOTModelMapping mapping = JOTQueryManager.getMapping(getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.delete(this);
    }

    /**
     * Save/update the table in teh database.
     */
    public void save() throws Exception
    {
        JOTModelMapping mapping = JOTQueryManager.getMapping(getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.save(this);
    }

    /**
     * Creates the table in the DB, if it doesn't exists yet
     */
    protected void createTableIfNecessary(JOTModelMapping mapping) throws Exception
    {
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.createTable(mapping);
    }

    public boolean isNew()
    {
        return dataId == -1;
    }

    /**
     * Deletes the table in the DB
     */
    protected void deleteTable() throws Exception
    {
        JOTModelMapping mapping = JOTQueryManager.getMapping(getClass());
        JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
        impl.deleteTable(mapping);
    }

    public void initQueryImplClass() throws Exception
    {
        storage = defineStorage();
        Object db = JOTPersistanceManager.databases.get(storage);
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
                throw new Exception("Storage: " + storage + " is undefined !!");
            }
        }
    }

    public Class getQueryImplClass()
    {
        return queryImplClass;
    }

    private String getSimpleClassName(String fullname)
    {
        String name = null;
        if (fullname.lastIndexOf(".") != -1)
        {
            name = fullname.substring(fullname.lastIndexOf(".") + 1, fullname.length());
        } else
        {
            name = fullname;
        }
        return name.toLowerCase();
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
