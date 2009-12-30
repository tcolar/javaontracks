/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance.builders;

import net.jot.persistance.JOTSQLCondition;

/**
 * Common static methods shared by all query builders
 * @author tcolar
 */
public final class JOTQueryBuilderHelper {
    private JOTQueryBuilderHelper(){}
    
            /**
     * Note: It is much safer to use where(JOTSQLCondition) ..,
     * If several where, we will be using AND
     * @param where
     */
    public static JOTQueryBase where(JOTQueryBase builder,String where)
    {
        appendToSQL(builder,(builder.nbWhere == 0 ? "WHERE " : "AND ") + where);
        builder.nbWhere++;
        return builder;
    }

    /**
     * Note: It is much safer to use orWere(JOTSQLCondition) ..,
     * If you want to do a OR where instead of and.
     * @param where
     */
    public static JOTQueryBase orWhere(JOTQueryBase builder, String where)
    {
        appendToSQL(builder,(builder.nbWhere == 0 ? "WHERE " : "OR ") + where);
        builder.nbWhere++;
        return builder;
    }
    /**
     * Provide PreparedStement params (optional)
     * @param params
     */
    public static JOTQueryBase withParams(JOTQueryBase builder, String[] pms)
    {
        for (int i = 0; i != pms.length; i++)
        {
            builder.params.add(pms[i]);
        }
        return builder;
    }
    
    
    /**
     * If several where, we will be using AND
     * @param where
     */
    public static JOTQueryBase where(JOTQueryBase builder, JOTSQLCondition cond)
    {
        builder.params.add(cond.getValue());
        return where(builder,cond.getSqlString());
    }

    /**
     * If you want to do a OR where instead of and.
     * @param where
     */
    public static JOTQueryBase orWhere(JOTQueryBase builder, JOTSQLCondition cond)
    {
        builder.params.add(cond.getValue());
        return orWhere(builder,cond.getSqlString());
    }
    
    /**
     * Ascending orderBy
     * should only be called once
     * @param orderBy EX: "date"    "date,price" etc...
     */
    public static JOTQueryBase orderBy(JOTQueryBase builder, String orderBy)
    {
        builder=orderBy(builder, orderBy, true);
        return builder;
    }

    /**
     * Add a orderBy to the query
     * should only be called once
     * @param orderBy EX: "date"    "date,price" etc...
     * @param ascending
     */
    public static JOTQueryBase orderBy(JOTQueryBase builder, String orderBy, boolean ascending)
    {
        appendToSQL(builder,"ORDER BY " + orderBy + (ascending ? "" : " DESC"));
        return builder;
    }

    public static JOTQueryBase distinct(JOTQueryBase builder, String column)
    {
        appendToSQL(builder,"DISTINCT " + column);
        return builder;
    }
        /**
     * Manually append whatever you like to the query.
     * @param where
     * @return
     */
    public static JOTQueryBase appendToSQL(JOTQueryBase builder,String append)
    {
        builder.sql.append(append).append(" ");
        return builder;
    }


}
