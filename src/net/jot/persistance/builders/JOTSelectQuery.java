/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance.builders;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.db.JOTDBField;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTQueryResult;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.utils.JOTUtilities;

/**
* Query builder for select type queries
* Use through JOQueryBuilder
* @author tcolar
*/
public class JOTSelectQuery extends JOTQueryBase{
    
    protected JOTSelectQuery(){}
    /**
     * Execute the query and return a Vector of "modelClass"(JOTModel).
     * Find all matches - ifno conditions -> all.
     * Will not be null for selects (might be empty)
     * or null for "Update type queries".
     * @throws java.lang.Exception
     */
    public JOTQueryResult find() throws Exception
    {
        Object[] pms = null;
        if (params.size() > 0)
        {
            pms = params.toArray();
        }
        JOTQueryResult result = JOTQueryManager.executeSQL(null, modelClass, sql.toString(), pms, flags);
        return result;
    }

    public JOTModel findOne() throws Exception
    {
        limit(1);
        return find().getFirstResult();
    }

    /**
     * Find one, and if none found, then create a new instance (not saved yet)
     * @param transaction
     * @return
     * @throws java.lang.Exception
     */
    public JOTModel findOrCreateOne() throws Exception
    {
        JOTModel model = findOne();
        if (model == null)
        {
            model = (JOTModel) modelClass.newInstance();
        }
        return model;
    }

 

    /**
     * add a "limit" to the number of returned results
     * should only be  called once
     * NOTE: if does not use the SQL limit synatx has this is not the same on all DB's
     * Instead it's gonna call setMaxRows() on the statement
     * @param limit
     */
    public JOTSelectQuery limit(int limit)
    {
        flags.setMaxRows(limit);
        return this;
    }

     /**
     * Dump matching data into a stream(ie file) in CSV format with column names
     * @param out
     * @param modelClass
     * @param params
     * @throws java.lang.Exception
     */
    public void dumpToCSV(OutputStream out) throws Exception
    {
        Vector results = find().getAllResults();
        PrintWriter p = new PrintWriter(out);
        JOTModelMapping mapping = JOTQueryManager.getMapping(null, modelClass);
        // write the metadata on the first line
        Hashtable fields = mapping.getFields();
        Enumeration fieldNames = fields.keys();
        String header = "ID" + ",";
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

    public JOTSelectQuery orWhere(JOTSQLCondition cond)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.orWhere(this,cond);
    }

    public JOTSelectQuery where(JOTSQLCondition cond)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.where(this,cond);
    }

    /**
     * It's much safer to use where(JOTSQLCondition cond)
     * @param where
     * @return
     */
    public JOTSelectQuery where(String where)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.where(this,where);
    }

   /**
     * Pass the (prepared statement )parameters (ie: values)
     * @param pms
     * @return
     */
     public JOTSelectQuery withParams(String[] pms)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.withParams(this,pms);
    }

    public JOTSelectQuery orderBy(String orderBy)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.orderBy(this,orderBy);
    }

    public JOTSelectQuery orderBy(String orderBy, boolean ascending)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.orderBy(this,orderBy, ascending);
    }

    public JOTSelectQuery orWhere(String where)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.orWhere(this,where);
    }
    /**
     * append generic SQL to the query, use with precautions !
     * @param append
     * @return
     */
    public JOTSelectQuery appendToSQL(String append)
    {
        return (JOTSelectQuery)JOTQueryBuilderHelper.appendToSQL(this, append);
    }

}
