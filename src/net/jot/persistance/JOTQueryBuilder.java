/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.persistance;

import java.util.Vector;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.persistance.query.JOTTransaction;

/**
 * This allow to build an SQL query manually.
 * It uses a builder pattern to make it more readable, failry close to ruby activeRecord's syntax
 * 
 * Example:
 * JOTQueryBuilder.select(User.class).where("name>a").where("name<d").orderBy("name").limit(2).execute();
 * Oftentimes it's best(safer) to use PreparedStatement form:
 * Example:
 * String[] params={"john","O'hara"}; // the ' could be dangerous if not using preparesStatement
 * JOTQueryBuilder.select(User.class).where("first=?").where("last=?").withParams(params).execute();
 * 
 * Notes: 
 * - multiple "Where" are ANDED unless you use OrWhere
 * - orderBy must be after the "where"
 * - limit must be after the "where" and "orderBy"
 * @author thibautc
 * 
 * TODO: test JOTQueryBuilder
 */
public final class JOTQueryBuilder
{

    private String sql = "";
    private Class modelClass;
    private String[] params = null;
    JOTStatementFlags flags=null;
    private int nbWhere = 0;

    private JOTQueryBuilder()
    {
    }

    /**
     * Factory method to create the queryBuilder
     * @param modelClass
     * @return
     */
    public static JOTQueryBuilder findAll(Class modelClass)
    {
        JOTQueryBuilder builder = new JOTQueryBuilder();
        builder.setModelClass(modelClass);
        builder.appendToSQL("select * from '" + modelClass + "'");
        return builder;
    }

    /**
     * If several where, we will be using AND
     * @param where
     */
    public JOTQueryBuilder where(String where)
    {
        appendToSQL((nbWhere == 0 ? "" : "AND ") + "WHERE " + where);
        nbWhere++;
        return this;
    }

    /**
     * If you want to do a OR where instead of and.
     * @param where
     */
    public JOTQueryBuilder orWhere(String where)
    {
        appendToSQL((nbWhere == 0 ? "" : "OR ") + "WHERE " + where);
        nbWhere++;
        return this;
    }

    /**
     * add a "limit" to the number of returned results
     * should only be  called once
     * NOTE: if does not use the SQL limit synatx has this is not the same on all DB's
     * Instead it's gonna call setMaxRows() on the statement
     * @param limit
     */
    public JOTQueryBuilder limit(int limit)
    {
        flags.setMaxRows(limit);
        return this;
    }

    /**
     * Ascending orderBy
     * should only be called once
     * @param orderBy EX: "date"    "date,price" etc...
     */
    public JOTQueryBuilder orderBy(String orderBy)
    {
        orderBy(orderBy, true);
        return this;
    }

    /**
     * Add a orderBy to the query
     * should only be called once
     * @param orderBy EX: "date"    "date,price" etc...
     * @param ascending
     */
    public JOTQueryBuilder orderBy(String orderBy, boolean ascending)
    {
        appendToSQL("ORDER BY " + orderBy + " " + (ascending ? "" : "DESC"));
        return this;
    }

    private void appendToSQL(String append)
    {
        sql += append + " ";
    }

    private void setModelClass(Class modelClass)
    {
        this.modelClass = modelClass;
    }

    /**
     * Provide PreparedStement params (optional)
     * @param params
     */
    public JOTQueryBuilder withParams(String[] params)
    {
        this.params = params;
        return this;
    }

    /**
     * Execute the query and return a Vector of "modelClass"(JOTModel).
     * @throws java.lang.Exception
     */
    public Vector execute() throws Exception
    {
        return execute(null);
    }

    public Vector execute(JOTTransaction transaction) throws Exception
    {
        return JOTQueryManager.findUsingSQL(transaction,modelClass, sql, params, flags);
    }

    public String showSQL()
    {
       return sql; 
    }
    
    /**
     * Show special statement flags (if any)
     * @return
     */
    public String showFlags()
    {
       return flags.toString();
    }
}
