/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.persistance.builders;

import java.io.OutputStream;
import net.jot.persistance.*;
import net.jot.persistance.query.JOTQueryManager;

/**
 * This allow to build an SQL queries(insert,select,delete,update) manually.
 * It uses a builder pattern to make it more readable, failry close to ruby activeRecord's syntax
 * 
 * Example:
 * JOTQueryBuilder.selectQuery(User.class).where("name>a").where("name<d").orderBy("name").limit(2).find();
 * Oftentimes it's best(safer) to use PreparedStatement form:
 * Example:
 * String[] params={"john","O'hara"}; // the ' could be dangerous if not using preparesStatement
 * JOTQueryBuilder.selectQuery(User.class).where("first=?").where("last=?").withParams(params).find();
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
     * Builds a select query
     * @param modelClass
     * @return
     */
    public static JOTSelectQuery selectQuery(JOTTransaction transaction, Class modelClass)
    {
		return selectQuery(transaction, "*", modelClass);
    }

	public static JOTSelectQuery selectQuery(JOTTransaction transaction, String selectWhat, Class modelClass)
    {
        JOTSelectQuery builder = new JOTSelectQuery();
        builder.setModelClass(modelClass);
        builder.appendToSQL("SELECT "+selectWhat+" FROM");
        builder.appendToSQL(JOTQueryManager.getTableName(transaction, modelClass));
        return builder;
    }
    /**
     * builds an insert query
     * @param modelClass
     * @return
     */
    public static JOTInsertQuery insertQuery(JOTTransaction transaction, Class modelClass)
    {
        JOTInsertQuery builder = new JOTInsertQuery(transaction);
        builder.setModelClass(modelClass);
        builder.appendToSQL("INSERT INTO");
        builder.appendToSQL(JOTQueryManager.getTableName(transaction, modelClass));
        return builder;
    }

    public static JOTUpdateQuery updateQuery(JOTTransaction transaction, Class modelClass)
    {
        JOTUpdateQuery builder = new JOTUpdateQuery(transaction);
        builder.setModelClass(modelClass);
        builder.appendToSQL("UPDATE");
        builder.appendToSQL(JOTQueryManager.getTableName(transaction, modelClass));
        builder.appendToSQL("SET");
        return builder;
    }

    public static JOTDeleteQuery deleteQuery(JOTTransaction transaction, Class modelClass)
    {
        JOTDeleteQuery builder = new JOTDeleteQuery(transaction);
        builder.setModelClass(modelClass);
        builder.appendToSQL("DELETE FROM");
        builder.appendToSQL(JOTQueryManager.getTableName(transaction, modelClass));
        return builder;
    }
    /**
     * shortcut to find an item by it's id
     * @param modelClass
     * @param id
     * @return
     * @throws java.lang.Exception
     */
    public static JOTModel findByID(JOTTransaction transaction, Class modelClass, long id) throws Exception
    {
        JOTSelectQuery builder = selectQuery(transaction, modelClass);
        JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, new Long(id));
        builder.where(cond);
        return builder.findOne();
    }
    /**
     * shortcut to delete an item by it's id
     * @param modelClass
     * @param id
     * @throws java.lang.Exception
     */
    public static void deleteByID(JOTTransaction transaction, Class modelClass, long id) throws Exception
    {
        JOTDeleteQuery builder = deleteQuery(transaction, modelClass);
        JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, new Long(id));
        builder.where(cond);
        builder.delete();
    }
    /**
     * Shortcut to return ALL the items of a table.
     * @param modelClass
     * @throws java.lang.Exception
     */
    public static JOTQueryResult findAll(JOTTransaction transaction, Class modelClass) throws Exception
    {
        JOTSelectQuery builder = selectQuery(transaction, modelClass);
        return builder.find();
    }

    /**
     * Dump a whole table in CSV  format to out.
     * @param out
     * @param modelClass
     * @throws java.lang.Exception
     */
    public static void dumpToCSV(OutputStream out, Class modelClass) throws Exception
    {
        selectQuery(null, modelClass).dumpToCSV(out);
    }
}
