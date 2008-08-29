/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

/**
 * Reprsents an SQL OrderBy condition to be added to an SQL query.
 * @author thibautc
 *
 */
public class JOTSQLOrderBy
{
	public static final int ASCENDING=0;
	public static final int DESCENDING=1;
	
	private String field;
	private int direction=ASCENDING;

	public String getField() 
	{
		return field;
	}

	public int getDirection() 
	{
		return direction;
	}

	/**
	 * Create an SQL orderBy (used in orderBy statement)
	 * 
	 * @param field    ie: 'firstname'
	 * @param direction (sort order): ASCENDING or DESCENDING
	 */
	public JOTSQLOrderBy(String field, int direction)
	{
		this.field=field;
		this.direction=direction;
	}
        
	/**
	 * Create an SQL orderBy (used in orderBy statement) order ASCENDING
	 */ 
	public JOTSQLOrderBy(String field)
	{
		this.field=field;
		this.direction=ASCENDING;
	}
}
