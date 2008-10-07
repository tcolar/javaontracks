/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance.builders;

import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTTransaction;
import net.jot.persistance.query.JOTQueryManager;

/**
 *
 * @author tcolar
 */
public class JOTInsertQuery extends JOTQueryBase
{
    public void insert(String[] fields, String[] values) throws Exception
    {
        insert(null, fields, values);
    }
    public void insert(JOTTransaction transaction, String[] fields, String[] values) throws Exception
    {
        appendToSQL("(");
        for (int i = 0; i != fields.length; i++)
        {
            if (i > 0)
            {
                appendToSQL(",");
            }
            appendToSQL(fields[i]);
        }
        appendToSQL(") VALUES (");
        for (int i = 0; i != fields.length; i++)
        {
            if (i > 0)
            {
                appendToSQL(",");
            }
            appendToSQL("?");
            params.add(fields[i]);
        }
        appendToSQL(")");
        JOTQueryManager.updateSQL(transaction, modelClass, sql.toString(), values, flags);
    }

    public JOTInsertQuery orWhere(JOTSQLCondition cond)
    {
        return (JOTInsertQuery)JOTQueryBuilderHelper.orWhere(this,cond);
    }

    public JOTInsertQuery where(JOTSQLCondition cond)
    {
        return (JOTInsertQuery)JOTQueryBuilderHelper.where(this,cond);
    }

    public JOTInsertQuery where(String where)
    {
        return (JOTInsertQuery)JOTQueryBuilderHelper.where(this,where);
    }

    public JOTInsertQuery withParams(String[] pms)
    {
        return (JOTInsertQuery)JOTQueryBuilderHelper.withParams(this,pms);
    }

    public JOTInsertQuery orWhere(String where)
    {
        return (JOTInsertQuery)JOTQueryBuilderHelper.orWhere(this,where);
    }

    public JOTInsertQuery appendToSQL(String append)
    {
        return (JOTInsertQuery)JOTQueryBuilderHelper.appendToSQL(this, append);
    }
}
