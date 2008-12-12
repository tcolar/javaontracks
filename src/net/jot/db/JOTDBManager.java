/**
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTStatementFlags;

/**
 * Main database manager object. Gives simple access to the loaded pooled databases.
 *@author     tcolar
 *@created    October 15, 2001
 */
public class JOTDBManager
{
    // singleton
    private final static JOTDBManager dbManager = new JOTDBManager();
    private Hashtable dbs = new Hashtable();

    /**
     *
     *@return    The instance value
     */
    public static final JOTDBManager getInstance()
    {
        return dbManager;
    }
    private JOTDBManager(){}
    /**
     * returns a connection to the 'default' database
     * @return
     * @throws java.lang.Exception
     */
    public JOTTaggedConnection getConnection() throws Exception
    {
        return getConnection("default");
    }

    /**
     * Gets a connection to a specified/named db.
     *
     *@param  dbName         Description of Parameter
     *@return                The connection value
     *@exception  Exception  Description of Exception
     */
    public JOTTaggedConnection getConnection(String dbName) throws Exception
    {
        JOTTaggedConnection con = null;
        try
        {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Trying to connect to database: " + dbName);

            JOTDBPool pool = (JOTDBPool) dbs.get(dbName);
            if (pool != null)
            {
                con = pool.retrieveConnection();
            } else
            {
                throw new Exception("No pool found for database: " + dbName + " !!!");
            }
            if (con == null)
            {
                throw new Exception("Connection is null");
            }
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Could not get connection  to database: " + dbName, e);
            throw (e);
        }
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Got Connection " + dbName + "/" + con.getId());

        return con;
    }

    /**
     * Release a connection to the DB pool
     *
     *@param  con     Description of Parameter
     */
    public void releaseConnection(JOTTaggedConnection con)
    {
        String name = con.getPoolName();
        JOTDBPool pool = (JOTDBPool) dbs.get(name);

        if (pool != null)
        {
            pool.releaseConnection(con);
        }
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Released Connection " + name + "/" + con.getId());
    }

    /**
     * Load a database configuration from a DBSetup object
     *
     *@param  setup  Description of Parameter
     */
    public void loadDb(String name, JOTDBJDBCSetup setup) throws Exception
    {
        // use the dbName as the pool identifier
        if (dbs.contains(name))
        {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Flushing pool of " + name + "to load new db");
            JOTDBPool pool = (JOTDBPool) dbs.get(name);
            pool.dropAll();
            pool.shutdown();
            dbs.remove(name);
        } else
        {
            // load the driver
            try
            {
                if (Class.forName(setup.getDriver()) == null)
                {
                    throw new Exception("Failed to get driver class: null");
                }
                JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Adding db in pool: " + name);
                dbs.put(name, new JOTDBPool(name, setup));
                JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Loaded the db driver succesfully: " + setup.getDriver());
                if (!tableExists(name, "jotcounters"))
                {
                    JOTTaggedConnection con = getConnection(name);
                    try
                    {
                        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Creating jotcounters table.");
                        update(con, "CREATE TABLE jotcounters(name varchar(40), val varchar(10))");
                        update(con, "ALTER TABLE jotcounters ADD PRIMARY KEY (name)");
                    } catch (SQLException e)
                    {
                        JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "JOTDBModel", "Error creating jotcounters", e);
                        throw (new Exception("Error creating jotcounters", e));
                    } finally
                    {
                        releaseConnection(con);
                    }

                }

            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, this, "Loading the db failed: " + setup.getDriver(), e);
                throw (new Exception("Loading the driver failed: " + setup.getDriver(), e));
            }
        }
    }

    /**
     * Quick DB query, without having to open/close the connection manually.
     * Opens a connection, makes the querry and then close it.
     * It can look like a waste to open and close the connection
     * for each query, but since the connection are pooled they are in fact
     * not open  /closed but retrieved/released from the pool.
     *
     * The query is gonna be something like "get * from toto where id=?"
     * Parameters: [5]
     * 
     *@param  query          Description of Parameter
     *@param  params         Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    public ResultSet query(JOTTaggedConnection con, String query, Object[] params, JOTStatementFlags flags) throws
            Exception
    {
        return execute(con, query, params, false, flags);
    }

    /**
     * It's is best/much safer to use query(con,query,params) as the parameters will be safely formatted for you (for example quotes in parameter values issues etc...)
     * 
     * The query is gonna be something like "get * from toto where id=5"
     *
     *@param  query          Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    public ResultSet query(JOTTaggedConnection con, String query, JOTStatementFlags flags) throws
            Exception
    {
        return execute(con, query, false, flags);
    }

    /**
     * Similar to query(con,query,params) but for the sql "update"
     * Update is NOT used to make an "SQL update" (confusing name :-)
     * But usually used to make a query which no result are expected such as an
     * SQL "insert" command.
     * Using query() for such a query might result in a backendexception/no results.
     *
     *@param  query          Description of Parameter
     *@param  params         Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    public ResultSet update(JOTTaggedConnection con, String query, Object[] params, JOTStatementFlags flags) throws
            Exception
    {
        return execute(con, query, params, true, flags);
    }

    public ResultSet update(JOTTaggedConnection con, String query, Object[] params) throws
            Exception
    {
        return execute(con, query, params, true, null);
    }

    /**
     * It's is best/much safer  to use update(con,query,params) as the parameters will be safely formatted for you (for example quotes in parameter values issues etc...)
     *
     *@param  query          Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    public ResultSet update(JOTTaggedConnection con, String query, JOTStatementFlags flags) throws
            Exception
    {
        return execute(con, query, true, flags);
    }

    public ResultSet update(JOTTaggedConnection con, String query) throws Exception
    {
        return execute(con, query, true, null);
    }

    /**
     * This is the equivalent of the nextval function on many database (ex:postgresql sequence)
     * This is use to have a safe incremental counter(usually a primary key / ID).
     * It's value is read and then incremented to the next value (for creating unique identifiers).
     * This function is synchronized/atomic, since it should execute at once to ensure the same value can't be read twice.
     *
     *@param  dataId             Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    public synchronized int nextVal(JOTTaggedConnection con, String id) throws Exception
    {
        // doing this in a transaction ans synchronized should be safe
        con.getConnection().setAutoCommit(false);
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Getting nextval of : " + id);
        int curval = 0;
        try
        {
            String[] params =
                    {
                id
            };
            ResultSet rs = query(con, "select * from jotcounters where name=?", params, null);
            if (rs.next())
            {
                curval = rs.getInt("val");
                String nextval = "" + (curval + 1);
                String[] params2 =
                        {
                    nextval, id
                };
                update(con, "update jotcounters set val=? where name=?", params2, null);
            } else
            {
                //new counter, creating it
                curval = 1;
                String[] params3 =
                        {
                    "2", id
                };
                update(con, "insert into jotcounters (val, name) values(?,?)", params3, null);
            }
        } catch (Exception e)
        {
            throw (e);
        } finally
        {
            con.getConnection().commit();
            con.getConnection().setAutoCommit(true);
        }
        return curval;
    }

    /**
     *  disconnects all the open DB's and release resources
     */
    public void shutdown()
    {
        // for all open dbs
        for (Enumeration e = dbs.keys(); e.hasMoreElements();)
        {

            JOTDBPool pool = (JOTDBPool) dbs.get(e.nextElement());
            // stopping the running thread
            pool.shutdown();
        }
    }

    /**
     * Does the query job itself.
     * Uses a preparedStement becasue it's safer
     * (takes care of special charcaters like quotes and such)
     *
     *@param  update         Description of Parameter
     *@param  squeleton      Description of Parameter
     *@param  params         Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    private ResultSet execute(JOTTaggedConnection con, String squeleton, Object[] params, boolean update, JOTStatementFlags flags) throws Exception
    {

        // empty args list causes an sql exception
        if (params == null || params.length == 0)
        {
            return execute(con, squeleton, update, flags);
        }

        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Executing query: " + squeleton);
        if (params != null && params.length > 0)
        {

            String str = "";
            for (int i = 0; i != params.length; i++)
            {
                str += (params[i]==null?"!!NULL!!":params[i].toString()) + ", ";
            }
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.TRACE_LEVEL, this, "With Parameters: " + str);
        }
        updateAccessTime(con);
        ResultSet rs = null;
        PreparedStatement st = con.getConnection().prepareStatement(squeleton);
        if (flags != null)
        {
            if (flags.getMaxRows() != -1)
            {
                st.setMaxRows(flags.getMaxRows());
            }
        }
        if (params != null)
        {
            for (int i = 0; i != params.length; i++)
            {
                st.setObject(i + 1, params[i]);
            }
        }
        if (update)
        {
            st.executeUpdate();
        } else
        {
            rs = st.executeQuery();
        }

        return rs;
    }

    /**
     * Esay way to do the query, however this is to use only on direct
     * querries
     * Querries constructed from user inout shouldn't use this method
     * It is not safe (can be hacked), does not use preparedStatement
     *
     *@param  query          Description of Parameter
     *@param  update         Description of Parameter
     *@param  con            Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    private ResultSet execute(JOTTaggedConnection con, String query, boolean update, JOTStatementFlags flags) throws Exception
    {
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Executing query: " + query);

        updateAccessTime(con);
        ResultSet rs = null;

        Connection stdCon = con.getConnection();
        if (stdCon == null)
        {
            throw (new Exception("Failed to get a connection !"));
        }
        Statement st = stdCon.createStatement();
        if (flags != null)
        {
            if (flags.getMaxRows() != -1)
            {
                st.setMaxRows(flags.getMaxRows());
            }
        }

        if (update)
        {
            st.executeUpdate(query);
        } else
        {
            rs = st.executeQuery(query);
        }

        return rs;
    }

    /**
     *  Updates the last time a connectin was used, information used by the DBPool Manager
     *
     *@param  con  Description of the Parameter
     */
    private void updateAccessTime(JOTTaggedConnection con)
    {
        JOTDBPool pool = (JOTDBPool) dbs.get(con.getPoolName());
        pool.updateAccessTime(con.getId());

    }

    /**
     * Checks wether a table already exists or not in a DB
     * @param storageName
     * @param table
     * @return
     * @throws java.lang.Exception
     */
    public boolean tableExists(String storageName, String table) throws Exception
    {
        JOTTaggedConnection con = getInstance().getConnection(storageName);
        boolean result = true;
        try
        {
            JOTDBManager.getInstance().query(con, "SELECT COUNT(0) from " + table, null);
        } catch (Exception e)
        {
            result = false;
        } finally
        {
            getInstance().releaseConnection(con);
        }
        return result;
    }
}

