/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.persistance.builders;

import net.jot.persistance.*;
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
public class JOTQueryBuilder
{

    private JOTQueryBuilder()
    {
    }

    /**
     * Factory method to create the queryBuilder
     * @param modelClass
     * @return
     */
    public static JOTSelectQuery selectQuery(Class modelClass)
    {
        JOTSelectQuery builder = new JOTSelectQuery();
        builder.setModelClass(modelClass);
        builder.appendToSQL("SELECT * FROM");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        return builder;
    }

    public static JOTInsertQuery insertQuery(Class modelClass)
    {
        JOTInsertQuery builder = new JOTInsertQuery();
        builder.setModelClass(modelClass);
        builder.appendToSQL("INSERT INTO");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        return builder;
    }

    public static JOTUpdateQuery updateQuery(Class modelClass)
    {
        JOTUpdateQuery builder = new JOTUpdateQuery();
        builder.setModelClass(modelClass);
        builder.appendToSQL("UPDATE");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        builder.appendToSQL("SET");
        return builder;
    }

    public static JOTDeleteQuery deleteQuery(Class modelClass)
    {
        JOTDeleteQuery builder = new JOTDeleteQuery();
        builder.setModelClass(modelClass);
        builder.appendToSQL("DELETE FROM");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        return builder;
    }
    
    public static JOTModel findByID(Class modelClass, int id) throws Exception
    {
        JOTSelectQuery builder = selectQuery(modelClass);
        JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, new Integer(id));
        builder.where(cond);
        return builder.findOne();
    }

    public static void deleteByID(Class modelClass, int id) throws Exception
    {
        JOTDeleteQuery builder = deleteQuery(modelClass);
        JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, new Integer(id));
        builder.where(cond);
        builder.delete();
    }

}
