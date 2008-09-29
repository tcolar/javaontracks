/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance;

import net.jot.persistance.query.JOTQueryManager;
/**
 * This allow to build an SQL query manually.
 * It uses a builder pattern to make it more readable, failry close to ruby activeRecord's syntax
 * 
 * Example:
 * JOTQueryBuilder.select(User.class).where("name>a").where("name<d").orderBy("name").limit(2).execute();
 * Oftentimes it's best(safer) to use PreparedStatement form:
 * Example:
 * String[] params={"john","O'hara"}; // the ' could be dangerous if not using preparesStatement
 * JOTQueryBuilder.select(User.class).where("first=?").where("last=?").withParams(first,last).execute();
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
    private String sql="";
    private Class modelClass;
    private String[] params=null;
    private int nbWhere=0;
    
    private JOTQueryBuilder(){}
    
    /**
     * Factory method to create the queryBuilder
     * @param modelClass
     * @return
     */
    public static JOTQueryBuilder select(Class modelClass)
    {
        JOTQueryBuilder builder=new JOTQueryBuilder();
        builder.setModelClass(modelClass);
        builder.appendToSQL("select * from '"+modelClass+"'");
        return builder;
    }

    /**
     * If several where, we will be using AND
     * @param where
     */
    public void where(String where)
    {
        appendToSQL((nbWhere==0?"":"AND ")+"WHERE "+where);
        nbWhere++;
    }

    /**
     * If you want to do a OR where instead of and.
     * @param where
     */
    public void orWhere(String where)
    {
        appendToSQL((nbWhere==0?"":"OR ")+"WHERE "+where);
        nbWhere++;
    }

    /**
     * add a "limit" to the number of returned results
     * should only be  called once
     * @param limit
     */
    public void limit(int limit)
    {
        appendToSQL("LIMIT "+limit);
    }

    /**
     * Ascending orderBy
     * should only be called once
     * @param orderBy EX: "date"    "date,price" etc...
     */
    public void orderBy(String orderBy)
    {
        orderBy(orderBy,true);
    }
    
    /**
     * Add a orderBy to the query
     * should only be called once
     * @param orderBy EX: "date"    "date,price" etc...
     * @param ascending
     */
    public void orderBy(String orderBy, boolean ascending)
    {
        appendToSQL("ORDER BY "+orderBy+" "+(ascending?"":"DESC"));
    }

    private void appendToSQL(String append)
    {
        sql+=append+" ";
    }
    
    private void setModelClass(Class modelClass)
    {
        this.modelClass = modelClass;
    }
    
    /**
     * Provide PreparedStement params (optional)
     * @param params
     */
    public void withParams(String[] params)
    {
        this.params=params;
    }
    
    /**
     * Execute the query and return a Vector of "modelClass"(JOTModel).
     * @throws java.lang.Exception
     */
    public void execute() throws Exception
    {
        JOTQueryManager.findUsingSQL(modelClass, sql, null);
    }
}
