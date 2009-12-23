/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance.builders;

import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTTransaction;
import net.jot.persistance.query.JOTQueryManager;

/**
 * Query builder for update type queries
 * Use through JOQueryBuilder
 * !! Note: does not delete anyhting until you call executeUpdate() !!
 * @author tcolar
 */
public class JOTUpdateQuery extends JOTQueryBase{
	private final JOTTransaction transaction;
    
    protected JOTUpdateQuery(JOTTransaction transaction){this.transaction=transaction;}
    /**
     * set the fields and values to be updated.
     * @param fields
     * @param values
     * @return
     */
    public JOTUpdateQuery update(String[] fields, Object[] values)
    {
        for (int i = 0; i != fields.length; i++)
        {
            if (i > 0)
            {
                appendToSQL(",");
            }
            appendToSQL(fields[i]);
            appendToSQL("=?");
        }
        for (int i = 0; i != values.length; i++)
        {
            params.add(values[i]);
        }
        return this;
    }
    /**
     * actually runs the update
     * @throws java.lang.Exception
     */
    public void executeUpdate() throws Exception
    {
        JOTQueryManager.updateSQL(transaction, modelClass, sql.toString(), params.toArray(), flags);
    }
    
    public JOTUpdateQuery orWhere(JOTSQLCondition cond)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.orWhere(this,cond);
    }

    public JOTUpdateQuery where(JOTSQLCondition cond)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.where(this,cond);
    }

    /**
     * It's much safer to use where(JOTSQLCondition cond)
     * @param where
     * @return
     */
    public JOTUpdateQuery where(String where)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.where(this,where);
    }

   /**
     * Pass the (prepared statement )parameters (ie: values)
     * @param pms
     * @return
     */
     public JOTUpdateQuery withParams(String[] pms)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.withParams(this,pms);
    }

    public JOTUpdateQuery orWhere(String where)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.orWhere(this,where);
    }
    
    /**
     * append generic SQL to the query, use with precautions !
     * @param append
     * @return
     */
    public JOTUpdateQuery appendToSQL(String append)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.appendToSQL(this, append);
    }

}
