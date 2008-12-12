/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import net.jot.db.JOTDBField;

/**
 * Defines a single DB condition, as used in an SQL 'where' closed <br>
 * ie: 'firstname' IS_EQUAL 'John'
 * 
 * @author Thibaut Colar http://jot.colar.net/
 *
 */
public class JOTSQLCondition 
{
	public static final int IS_EQUAL=0;
	public static final int IS_NOT_EQUAL=1;
	public static final int IS_GREATER=2;
	public static final int IS_LOWER=3;
	public static final int IS_GREATER_OR_EQUAL=5;
	public static final int IS_LOWER_OR_EQUAL=6;
	public static final int IS_LIKE=7;
	
	private String field;
	private int comparaison;
	private Object value;
	
	public int getComparaison() 
	{
		return comparaison;
	}

	public String getField() 
	{
		return field;
	}

	public Object getValue() 
	{
		return value;
	}

	/**
	 * Create an SQL condition(used in where statement)
	 * 
	 * @param field    ie: 'firstname'
	 * @param comparaison ie: IS_EQUAL
	 * @param value ie: 'John'
	 */
	public JOTSQLCondition(String field, int comparaison, Object value)
	{
                field=JOTDBField.getCleanFieldName(field);

		this.value=value;
		this.comparaison=comparaison;
		this.field=field;
	}
	
        /**
         * Return the comparator in SQL form
         * example: getSQLComparator() for IS_GREATER will return " > ? "
         * @return
         */
	public String getSQLComparator()
	{
		String s=" = ?";
		switch(comparaison)
		{
			case IS_GREATER: s=" > ?";break;
			case IS_GREATER_OR_EQUAL:s=" >= ?";break;
			case IS_LOWER:s=" < ?";break;
			case IS_LOWER_OR_EQUAL:s=" <= ?";break;
			case IS_LIKE:s=" like ?";break;
			case IS_NOT_EQUAL:s=" <> ?";break;
		}
		return s;
	}

        public String getSqlString()
        {
            return new StringBuffer(field).append(getSQLComparator()).toString();
        }
}
