/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import java.util.Vector;

/**
 * Represents the parameters for an SQL query
 * such as a set of conditins, orderBy statement etc...
 * @author tcolar
 */
public class JOTSQLQueryParams
{
	private Vector conditions=new Vector();
	private Vector orderBys=new Vector();
	private int limit=0;
	
	public void addCondition(JOTSQLCondition cond)
	{
		conditions.add(cond);
	}
	
	public void addOrderBy(JOTSQLOrderBy orderBy)
	{
		orderBys.add(orderBy);
	}
	
	public void clearAll()
	{
		conditions.clear();
		orderBys.clear();
		limit=0;		
	}
	
	public JOTSQLCondition[] getConditions()
	{
		return (JOTSQLCondition[]) conditions.toArray(new JOTSQLCondition[0]);
	}
	
	public JOTSQLOrderBy[] getOrderBys()
	{
		return (JOTSQLOrderBy[]) orderBys.toArray(new JOTSQLOrderBy[0]);		
	}
	
	public int getLimit()
	{
		return limit;
	}
        /**
         * Specify a limit to the number of rows returned by the query.
         * @param limit
         */
	public void setLimit(int limit)
	{
		this.limit = limit;
	}
}
