/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.db;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModelMapping;

/**
 * Represents a Database Field(column).
 * @author tcolar
 */
public class JOTDBField
{
    // TODO: + validate max / min length, etc....
    public static Hashtable types = new Hashtable();
    public static final String TYPE_VARCHAR = "VARCHAR";
    public static final String TYPE_DECIMAL = "DECIMAL";
    public static final String TYPE_BIT = "BIT";
    public static final String TYPE_TINYINT = "TINYINT";
    public static final String TYPE_SMALLINT = "SMALLINT";
    public static final String TYPE_INTEGER = "INTEGER";
    public static final String TYPE_BIGINT = "BIGINT";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_TIMESTAMP = "TIMESTAMP";
    public static final String TYPE_DATE = "DATE";
    public static final String TYPE_TIME = "TIME";
    

    static
    {
        types.put(String.class.getName(), TYPE_VARCHAR);
        types.put(BigDecimal.class.getName(), TYPE_DECIMAL);
        types.put(Boolean.class.getName(), TYPE_BIT);
        types.put("boolean", TYPE_BIT);
        types.put(Byte.class.getName(), TYPE_TINYINT);
        types.put("byte", TYPE_TINYINT);
        types.put(Short.class.getName(), TYPE_SMALLINT);
        types.put("short", TYPE_SMALLINT);
        types.put(Integer.class.getName(), TYPE_INTEGER);
        types.put("int", TYPE_INTEGER);
        types.put(Long.class.getName(), TYPE_BIGINT);
        types.put("long", TYPE_BIGINT);
        types.put(Float.class.getName(), TYPE_FLOAT);
        types.put("float", TYPE_FLOAT);
        types.put(Double.class.getName(), TYPE_DOUBLE);
        types.put("double", TYPE_DOUBLE);
        types.put(Timestamp.class.getName(), TYPE_TIMESTAMP);
        types.put(Date.class.getName(), TYPE_DATE);
        types.put(Time.class.getName(), TYPE_TIME);
    }
    String fieldName = null;
    String fieldType = "VARCHAR";
    //  for varchar only
    int maxLength = -1;
    int minLength = -1;
    // for numeric only
    int minValue = -1;
    int maxValue = -1;
    int size = -1;
    private String defaultValue;

    /**
     * Returns the field size/length 
     * @return
     */
    public int getSize()
    {
        if (fieldType.equals(TYPE_VARCHAR) && size < 0)
        {
            return 100;
        }
        // some very big, very precise number (128 bits):-)
        if (fieldType.equals(TYPE_DECIMAL) && size < 0)
        {
            return 128;
        }
        return size;
    }

    /**
     * Hashtable of all defined field types
     * @return
     */
    public static Hashtable getTypes()
    {
        return types;
    }

    /**
     * Default value for when the field (database default value)
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * Sets the field size/length
     * @param size
     */
    public void setSize(int size)
    {
        this.size = size;
        if (maxLength == -1)
        {
            setMaxLength(size);
        }
    }

    public JOTDBField(String fieldType)
    {
        setFieldType(fieldType);
    }

    public JOTDBField(String fieldType, String fieldName)
    {
        setFieldName(fieldName);
        setFieldType(fieldType);
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        if (fieldName != null)
        {
            this.fieldName = getCleanFieldName(fieldName);
        }
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(String fieldType)
    {
        JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.DEBUG_LEVEL, this, "Setting field type for " + fieldName + " to " + fieldType);
        this.fieldType = fieldType;
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
        if (size < maxLength)
        {
            size = maxLength;
        }
    }

    public int getMaxValue()
    {
        return maxValue;
    }

    public void setMaxValue(int maxValue)
    {
        this.maxValue = maxValue;
    }

    public int getMinLength()
    {
        return minLength;
    }

    public void setMinLength(int minLength)
    {
        this.minLength = minLength;
    }

    public int getMinValue()
    {
        return minValue;
    }

    public void setMinValue(int minValue)
    {
        this.minValue = minValue;
    }

    /**
     * Prints field informations to logger, for debugging purposes
     */
    public void debug()
    {
        JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.DEBUG_LEVEL, this, "DB Field infos-> name:" + fieldName + ", type:" + fieldType + ", minLength:" + minLength + ", maxLength:" + maxLength + ", minValue:" + minValue + ", maxValue:" + maxValue);
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    private String getCleanFieldName(String fieldName)
    {
        return JOTModelMapping.createCleanTableName(fieldName);
    }
}
