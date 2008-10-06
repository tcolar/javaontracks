/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.persistance;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.db.JOTDBField;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.utils.JOTUtilities;

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

    private StringBuffer sql = new StringBuffer();
    private Class modelClass;
    private Vector params = new Vector();
    JOTStatementFlags flags = new JOTStatementFlags();
    private int nbWhere = 0;

    private JOTQueryBuilder()
    {
    }

    /**
     * Factory method to create the queryBuilder
     * @param modelClass
     * @return
     */
    public static JOTQueryBuilder selectQuery(Class modelClass)
    {
        JOTQueryBuilder builder = new JOTQueryBuilder();
        builder.setModelClass(modelClass);
        builder.appendToSQL("SELECT * FROM");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        return builder;
    }

    public static JOTQueryBuilder insertQuery(Class modelClass)
    {
        JOTQueryBuilder builder = new JOTQueryBuilder();
        builder.setModelClass(modelClass);
        builder.appendToSQL("INSERT INTO");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        return builder;
    }

    public static JOTQueryBuilder updateQuery(Class modelClass)
    {
        JOTQueryBuilder builder = new JOTQueryBuilder();
        builder.setModelClass(modelClass);
        builder.appendToSQL("UPDATE");
        builder.appendToSQL(JOTQueryManager.getTableName(modelClass));
        builder.appendToSQL("SET");
        return builder;
    }

    public static JOTQueryBuilder deleteQuery(Class modelClass)
    {
        JOTQueryBuilder builder = new JOTQueryBuilder();
        builder.setModelClass(modelClass);
        builder.appendToSQL("DELETE FROM").appendToSQL(JOTQueryManager.getTableName(modelClass));
        return builder;
    }

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
        String[] pms = null;
        if (params.size() > 0)
        {
            pms = (String[]) params.toArray(new String[0]);
        }
        JOTQueryManager.updateSQL(transaction, modelClass, sql.toString(), pms, flags);
    }

    public void update(String[] fields, String[] values) throws Exception
    {
        update(null, fields, values);
    }
    public void update(JOTTransaction transaction, String[] fields, String[] values) throws Exception
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
        for (int i = 0; i != fields.length; i++)
        {
            params.add(fields[i]);
        }
        String[] pms = null;
        if (params.size() > 0)
        {
            pms = (String[]) params.toArray(new String[0]);
        }
        JOTQueryManager.updateSQL(transaction, modelClass, sql.toString(), pms, flags);
    }

    /**
     * Note: It is much safer to use where(JOTSQLCondition) ..,
     * If several where, we will be using AND
     * @param where
     */
    public JOTQueryBuilder where(String where)
    {
        appendToSQL((nbWhere == 0 ? "WHERE " : "AND ") + where);
        nbWhere++;
        return this;
    }

    /**
     * Manullay append whatever you like to the query.
     * @param where
     * @return
     */
    public JOTQueryBuilder append(String sqlText)
    {
        appendToSQL(sqlText);
        return this;
    }

    /**
     * Note: It is much safer to use orWere(JOTSQLCondition) ..,
     * If you want to do a OR where instead of and.
     * @param where
     */
    public JOTQueryBuilder orWhere(String where)
    {
        appendToSQL((nbWhere == 0 ? "WHERE " : "OR ") + where);
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
        appendToSQL("ORDER BY " + orderBy + (ascending ? "" : " DESC"));
        return this;
    }

    public void delete() throws Exception
    {
        delete(null);
    }

    void delete(JOTTransaction transaction) throws Exception
    {
        String[] pms = null;
        if (params.size() > 0)
        {
            pms = (String[]) params.toArray(new String[0]);
        }
        JOTQueryManager.updateSQL(transaction, modelClass, sql.toString(), pms, flags);
    }

    private JOTQueryBuilder appendToSQL(String append)
    {
        sql.append(append).append(" ");
        return this;
    }

    private void setModelClass(Class modelClass)
    {
        this.modelClass = modelClass;
    }

    /**
     * Provide PreparedStement params (optional)
     * @param params
     */
    public JOTQueryBuilder withParams(String[] pms)
    {
        for (int i = 0; i != pms.length; i++)
        {
            params.add(pms[i]);
        }
        return this;
    }

    /**
     * Execute the query and return a Vector of "modelClass"(JOTModel).
     * Will not be null for selects (might be empty)
     * or null for "Update type queries".
     * @throws java.lang.Exception
     */
    public JOTQueryResult find() throws Exception
    {
        return find(null);
    }

    public JOTQueryResult find(JOTTransaction transaction) throws Exception
    {
        String[] pms = null;
        if (params.size() > 0)
        {
            pms = (String[]) params.toArray(new String[0]);
        }
        JOTQueryResult result = JOTQueryManager.executeSQL(transaction, modelClass, sql.toString(), pms, flags);
        return result;
    }

    public JOTModel findOne() throws Exception
    {
        return findOne(null);
    }

    public static JOTModel findByID(Class modelClass, int id) throws Exception
    {
        JOTQueryBuilder builder = selectQuery(modelClass);
        JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, id);
        builder.where(cond);
        return builder.findOne();
    }

    public static void deleteByID(Class modelClass, int id) throws Exception
    {
        JOTQueryBuilder builder = deleteQuery(modelClass);
        JOTSQLCondition cond = new JOTSQLCondition("id", JOTSQLCondition.IS_EQUAL, id);
        builder.where(cond);
        builder.delete();
    }

    /**
     * Return the first result only
     * or null if no results
     * @param transaction
     * @return
     * @throws java.lang.Exception
     */
    public JOTModel findOne(JOTTransaction transaction) throws Exception
    {
        return find(transaction).getFirstResult();
    }

    /**
     * Find one, and if none found, then create a new instance (not saved yet)
     * @param transaction
     * @return
     * @throws java.lang.Exception
     */
    public JOTModel findOrCreateOne(JOTTransaction transaction) throws Exception
    {
        JOTModel model = findOne(transaction);
        if (model == null)
        {
            model = (JOTModel) modelClass.newInstance();
        }
        return model;
    }

    public JOTModel findOrCreateOne() throws Exception
    {
        return findOrCreateOne(null);
    }

    public String showSQL()
    {
        return sql.toString();
    }

    /**
     * Show special statement flags (if any)
     * @return
     */
    public String showFlags()
    {
        return flags.toString();
    }

    /**
     * If several where, we will be using AND
     * @param where
     */
    public JOTQueryBuilder where(JOTSQLCondition cond)
    {
        params.add(cond.getValue());
        return where(cond.getSqlString());
    }

    /**
     * If you want to do a OR where instead of and.
     * @param where
     */
    public JOTQueryBuilder orWhere(JOTSQLCondition cond)
    {
        params.add(cond.getValue());
        return orWhere(cond.getSqlString());
    }

    /**
     * Dump a whole table (model) data into a stream(ie file) in CSV format
     * @param out
     * @param modelClass
     * @param params
     * @throws java.lang.Exception
     */
    public void dumpToCSV(OutputStream out) throws Exception
    {
        Vector results = find().getAllResults();
        PrintWriter p = new PrintWriter(out);
        JOTModelMapping mapping = JOTQueryManager.getMapping(modelClass);
        // write the metadata on the first line
        Hashtable fields = mapping.getFields();
        Enumeration fieldNames = fields.keys();
        String header = mapping.getPrimaryKey() + ",";
        while (fieldNames.hasMoreElements())
        {
            String name = ((JOTDBField) fields.get(fieldNames.nextElement())).getFieldName();
            header += JOTUtilities.encodeCSVEntry(name) + ",";
        }
        header = header.substring(0, header.length() - 1);
        p.println(header);
        // writes the data in csv format
        for (int i = 0; i != results.size(); i++)
        {
            // handle a line of data
            JOTModel model = (JOTModel) results.get(i);
            String line = model.getId() + ",";
            fieldNames = fields.keys();
            while (fieldNames.hasMoreElements())
            {
                String value = model.getFieldValue((String) fieldNames.nextElement()).toString();
                line += JOTUtilities.encodeCSVEntry(value) + ",";
            }
            line = line.substring(0, line.length() - 1);
            p.println(line);
        }
        p.flush();
    }
}
