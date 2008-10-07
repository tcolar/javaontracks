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
    
    protected JOTUpdateQuery(){}
    
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

    public void executeUpdate() throws Exception
    {
        executeUpdate(null);
    }
    public void executeUpdate(JOTTransaction transaction) throws Exception
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

    public JOTUpdateQuery where(String where)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.where(this,where);
    }

    public JOTUpdateQuery withParams(String[] pms)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.withParams(this,pms);
    }

    public JOTUpdateQuery orWhere(String where)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.orWhere(this,where);
    }
    
    public JOTUpdateQuery appendToSQL(String append)
    {
        return (JOTUpdateQuery)JOTQueryBuilderHelper.appendToSQL(this, append);
    }

}
