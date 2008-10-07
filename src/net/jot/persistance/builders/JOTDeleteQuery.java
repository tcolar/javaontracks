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
    /**
     * actually runs the delete action
     * @param transaction
     * @throws java.lang.Exception
     */
    public void delete(JOTTransaction transaction) throws Exception
    {
        Object[] pms = null;
        if (params.size() > 0)
        {
            pms = params.toArray();
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
    /**
     * It's much safer to use where(JOTSQLCondition cond)
     * @param where
     * @return
     */
    public JOTDeleteQuery where(String where)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.where(this,where);
    }
    /**
     * Pass the (prepared statement )parameters (ie: values)
     * @param pms
     * @return
     */
    public JOTDeleteQuery withParams(String[] pms)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.withParams(this,pms);
    }

    public JOTDeleteQuery orWhere(String where)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.orWhere(this,where);
    }

    /**
     * append generic SQL to the query, use with precautions !
     * @param append
     * @return
     */
    public JOTDeleteQuery appendToSQL(String append)
    {
        return (JOTDeleteQuery)JOTQueryBuilderHelper.appendToSQL(this, append);
    }

}
