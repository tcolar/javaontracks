/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import net.jot.db.JOTDBField;
import net.jot.logger.JOTLogger;
import net.jot.prefs.JOTPreferences;


// TODO: ++ if the mapping in the class does not match the mapping in the database/FS we should probably crash with a severe error, rather that risquing messing up the data !
/**
 * Handles the mapping of a DB representaion (java object) to a DB table
 * @author tcolar
 */
public class JOTModelMapping
{

    public static final String META_FILE_EXTENSION = ".jotmeta";
    private static final String TABLE_FIELD_SEPARATOR = ",";
    private static final String TABLE_FIELD_TYPE_SEPARATOR = ":";
    private static final String TABLE_VERSION_STRING = "Version:";
    private static final String TABLE_DATA_ROW_LENGTH = "Row-Length:";
    public boolean createMissingTables = JOTPreferences.getInstance().getDefaultedBoolean("jot.db.create_missing_tables", Boolean.TRUE).booleanValue();
    private String storageName = "default";
    private String primaryKey = "id";
    private String tableName = null;
    private Hashtable mappedFields = new Hashtable();
    private String[] ignoredFields =
            {};
    private Hashtable fields = new Hashtable();
    private String queryClassName = null;
    private int dataSize = -1;

    // we cache those for speed
    private String updateString = null;
    private String insertString = null;

    public void defineFieldMaxValue(String field, int value)
    {
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setMaxValue(value);
        }
    }

    public void defineFieldMinValue(String field, int value)
    {
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setMinValue(value);
        }
    }

    public void defineFieldSize(String field, int value)
    {
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setSize(value);
        }
    }

    public void defineFieldMinlength(String field, int value)
    {
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setMinLength(value);
        }
    }

    public String getDBName()
    {
        return JOTPersistanceManager.getInstance().getDbName(storageName);
    }

    public String getInsertString()
    {
        Hashtable fields = getFields();
        //we only need to compute this once.
        if (insertString == null)
        {
            String result = "\"" + getPrimaryKey() + "\"";
            Enumeration e = fields.elements();
            while (e.hasMoreElements())
            {
                result += ", \"" + ((JOTDBField) e.nextElement()).getFieldName() + "\"";
            }
            insertString = result;
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.TRACE_LEVEL, this, "Request params: " + insertString);
        }
        return insertString;
    }

    public String getUpdateString()
    {
        Hashtable fields = getFields();
        if (updateString == null)
        {
            String result = "\"" + getPrimaryKey() + "\"=?";
            Enumeration e = fields.elements();
            while (e.hasMoreElements())
            {
                JOTDBField field = (JOTDBField) e.nextElement();
                result += ", \"" + field.getFieldName() + "\"=?";
            }
            updateString = result;
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.TRACE_LEVEL, this, "Request params: " + updateString);
        }
        return updateString;
    }

    public void setPrimaryKey(String primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    public void setStorageName(String storageName)
    {
        this.storageName = storageName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public void defineFieldMaxlength(String field, int value)
    {
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setMaxLength(value);
        }
    }

    public void defineFieldType(String field, String type)
    {
        field = field.toLowerCase();
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setFieldType(type);
        }
    }

    public void defineFieldDBName(String field, String name)
    {
        JOTDBField f = (JOTDBField) fields.get(field);
        if (f != null)
        {
            f.setFieldName(name);
        }
    }

    public void defineFieldsToIgnore(String[] ignore)
    {
        ignoredFields = ignore;
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Setting Fields to be ignored to: " + ignoredFields);
        for (int i = 0; i != ignoredFields.length; i++)
        {
            if (fields != null && fields.contains(ignoredFields[i]))
            {
                fields.remove(ignoredFields[i]);
                JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Removing previously added field to be ignored: " + ignoredFields[i]);
            }
        }
    }

    public Hashtable getFields()
    {
        return fields;
    }

    public void setFields(Hashtable fields)
    {
        this.fields = fields;
    }

    public String[] getIgnoredFields()
    {
        return ignoredFields;
    }

    public void setIgnoredFields(String[] ignoredFields)
    {
        this.ignoredFields = ignoredFields;
    }

    public Hashtable getMappedFields()
    {
        return mappedFields;
    }

    public void setMappedFields(Hashtable mappedFields)
    {
        this.mappedFields = mappedFields;
    }

    public String getPrimaryKey()
    {
        return primaryKey;
    }

    public void definePrimaryKey(String primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    /**
     * Storage name is the name of the storage definition
     * can be different than the db name defined in the prop file itself.
     * @return
     */
    public String getStorageName()
    {
        return storageName;
    }
    /*public void defineStorageName(String storageName) {
    this.storageName = storageName;
    }*/

    public String getTableName()
    {
        return tableName;
    }

    public void defineTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getQueryClassName()
    {
        return queryClassName;
    }

    public void setQueryClassName(String className)
    {
        queryClassName = className;
    }

    /**
     * Returns the size(bytes) of a "row" of data.
     * @return
     */
    public int getDataSize()
    {
        if (dataSize == -1)
        {
            // primary key takes 8(long)
            int size = 8;
            Enumeration e = fields.elements();
            while (e.hasMoreElements())
            {
                JOTDBField field = (JOTDBField) e.nextElement();
                size += getFieldSize(field);
            }
            dataSize = size;
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Computed data size :" + dataSize);

        }
        return dataSize;
    }

    public int getFieldSize(JOTDBField field)
    {
        int size = 0;

        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Looking for field size for :" + field.getFieldName() + " -> " + field.getFieldType() + " (" + field.getSize() + ")");

        if (field.getFieldType().equals(JOTDBField.TYPE_BIT))
        // store in a byte.
        {
            size += 1;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_TINYINT))
        {
            size += 1;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_SMALLINT))
        {
            size += 2;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_INTEGER))
        {
            size += 4;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_FLOAT))
        {
            size += 4;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_TIME))
        {
            size += 8;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_DOUBLE))
        {
            size += 8;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_BIGINT))
        {
            size += 8;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_DECIMAL))
        {
            size += 8;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_TIMESTAMP))
        {
            size += 8;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_DATE))
        {
            size += 8;
        } else if (field.getFieldType().equals(JOTDBField.TYPE_VARCHAR))
        // For a string, we write the string size first (4 bytes) then the string.
        {
            size += 4 + field.getSize();
        } else if (field.getFieldType().equals(JOTDBField.TYPE_DECIMAL))
        // For a big decimal, we write the decimal size first (4 bytes) then the string.
        {
            size += 4 + field.getSize();
        } else
        {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.WARNING_LEVEL, this, "Field type unknown :" + field.getClass());
        }
        return size;
    }

    /**
     * Returns the Metadata respresentation of the table columns
     * ie: "id":int4, "name":varchar(100) ....
     * @param mapping
     * @return
     */
    protected String getMeta()
    {
        String header = "\"" + getPrimaryKey() + "\"" + TABLE_FIELD_TYPE_SEPARATOR + "BIGINT" + TABLE_FIELD_SEPARATOR;
        Enumeration e = getFields().elements();
        while (e.hasMoreElements())
        {
            JOTDBField field = (JOTDBField) e.nextElement();
            header += "\"" + field.getFieldName() + "\"" + TABLE_FIELD_TYPE_SEPARATOR + field.getFieldType();
            if (field.getSize() > -1)
            {
                header += "(" + field.getSize() + ")";
            }
            if (e.hasMoreElements())
            {
                header += TABLE_FIELD_SEPARATOR;
            }
        }
        return header;
    }

    /**
     * Writes the current mapping (table metadata) to the metadata file (.jotmeta)
     * @param mapping
     * @throws java.lang.Exception
     */
    public static synchronized void writeMetaFile(JOTModelMapping mapping) throws Exception
    {
        // create the meta file.
        File dbFolder = new File(JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName()));
        dbFolder.mkdirs();
        File metaFile = new File(dbFolder, mapping.getTableName() + META_FILE_EXTENSION);
        String header = mapping.getMeta();
        PrintWriter writer = new PrintWriter(new FileWriter(metaFile));
        writer.println(TABLE_VERSION_STRING + JOTPersistanceManager.getInstance().getDbVersion(mapping.getStorageName()));
        writer.println(TABLE_DATA_ROW_LENGTH + mapping.getDataSize());
        writer.println(header);
        writer.flush();
        writer.close();
    }

    public static synchronized JOTModelMeta readMetaFile(JOTModelMapping mapping) throws Exception
    {
        // create the meta file.
        File metaFile = new File(JOTPersistanceManager.getInstance().getDbFolder(mapping.getDBName()), mapping.getTableName() + META_FILE_EXTENSION);
        BufferedReader reader = new BufferedReader(new FileReader(metaFile));
        String s = null;
        JOTModelMeta meta = new JOTModelMeta();
        while ((s = reader.readLine()) != null)
        {
            if (s.startsWith(TABLE_VERSION_STRING))
            {
                meta.setVersion(s.substring(TABLE_VERSION_STRING.length(), s.length()));
            } else if (s.startsWith(TABLE_DATA_ROW_LENGTH))
            {
                meta.setRowSize(s.substring(TABLE_DATA_ROW_LENGTH.length(), s.length()));
            } else if (s.startsWith("\""))
            {
                meta.setFields(s);
            }

        }
        reader.close();
        return meta;
    }

    public static void deleteMetaFile(JOTModelMapping mapping)
    {
        String meta = mapping.getTableName() + META_FILE_EXTENSION;
        File f = new File(JOTPersistanceManager.getInstance().getDbFolder(mapping.getStorageName()), meta);
        if (f.exists())
        {
            f.delete();
        }
    }
}
