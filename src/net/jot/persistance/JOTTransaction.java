/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.persistance;

import java.util.Vector;
import net.jot.exceptions.JOTTransactionCompletedException;
import net.jot.db.JOTDBManager;
import net.jot.db.JOTTaggedConnection;
import net.jot.logger.JOTLogger;

/**
 * This is used to run multiple DB queries in a transaction
 *
 * Note that once either commit() or roollback() have been called, this transaction is considered completet
 * and can't be used anymore (will throw JOTTransactionCompltedException)
 * @author thibautc
 */
public final class JOTTransaction
{

    private JOTTaggedConnection con;
    private boolean completed = false;
	Vector createdTables = new Vector();

    // do not use
    private JOTTransaction()
    {
        con = null;
    }

    public JOTTransaction(String storageName) throws Exception
    {
        con = JOTDBManager.getInstance().getConnection(storageName);
        con.getConnection().setAutoCommit(false);
    }

    public JOTTaggedConnection getConnection()
    {
        return con;
    }

    public void rollBack() throws Exception
    {
        if (completed)
        {
            throw new JOTTransactionCompletedException();
        }
        con.getConnection().rollback();
        // if no exception, this transaction is completed
        terminate();
    }

    /*public void rollBack(Savepoint savept) throws Exception
    {
        if (completed)
        {
            throw new JOTTransactionCompletedException();
        }
        con.getConnection().rollback(savept);
        // if no exception, this tranmsaction is completed
        terminate();
    }*/

    /*public Savepoint setSavepoint() throws Exception
    {
        if (completed)
        {
            throw new JOTTransactionCompletedException();
        }
        return con.getConnection().setSavepoint();
    }

    public Savepoint setSavepoint(String savept) throws Exception
    {
        if (completed)
        {
            throw new JOTTransactionCompletedException();
        }
        return con.getConnection().setSavepoint(savept);
    }*/

    public void setTransactionIsolation(int isolation) throws Exception
    {
        if (completed)
        {
            throw new JOTTransactionCompletedException();
        }
        con.getConnection().setTransactionIsolation(isolation);
    }

    public void commit() throws Exception
    {
        if (completed)
        {
            throw new JOTTransactionCompletedException();
        }
        con.getConnection().commit();
        // if no exception, this tranmsaction is completed
        terminate();
    }

    /**
     * This will terminate the transaction and free resources
     * In theory you should not need to call this, since commit() and rollback() will call it.
     * However if you want to ensure release of the connection you can call it to be safe.
     * @throws java.lang.Exception
     */
    public void terminate()
    {
        if (!completed)
        {
            completed = true;
            try
            {
                con.getConnection().setAutoCommit(true);
            } catch (Exception e)
            {
                JOTLogger.logException(this, "Failed to set the connection back to autocommit", e);
            }
            try
            {
                JOTDBManager.getInstance().releaseConnection(con);
            } catch (Exception e)
            {
                JOTLogger.logException(this, "Failed to release the connection to the pool", e);
            }
            con = null;
        }
    }

    public void finalyze() throws Throwable
    {
        // if the transaction wasn't completed correctly, we will try to cleanup here
        // and try to release the connection to the pool.
        terminate();
        super.finalize();
    }

	public boolean hasCreatedTable(String tableName)
	{
		return createdTables.contains(tableName);
	}

	public void addCreatedTable(String tableName)
	{
		createdTables.add(tableName);
	}
}
