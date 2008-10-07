/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance.builders;

import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTTransaction;
import net.jot.persistance.query.JOTQueryManager;

/**
 * Query builder for delete type queries
 * Use through JOQueryBuilder
 * @author tcolar
 */
public class JOTDeleteQuery extends JOTQueryBase{
    protected JOTDeleteQuery(){}
    
    public void delete() throws Exception
    {
        delete(null);
    }

    public void delete(JOTTransaction transaction) throws Exception
    {
        String[] pms = null;
        if (params.size() > 0)
        {
            pms = (String[]) params.toArray(new String[0]);
        }
        JOTQueryManager.updateSQL(transaction, modelClass, sql.toString(), pms, flags);
    }

    public JOTDeleteQuery orWhere(JOTSQLCondition cond)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.orWhere(this,cond);
    }

    public JOTDeleteQuery where(JOTSQLCondition cond)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.where(this,cond);
    }

    public JOTDeleteQuery where(String where)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.where(this,where);
    }

    public JOTDeleteQuery withParams(String[] pms)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.withParams(this,pms);
    }

    public JOTDeleteQuery orWhere(String where)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.orWhere(this,where);
    }

    public JOTSelectQuery appendToSQL(String append)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.appendToSQL(this, append);
    }

}
