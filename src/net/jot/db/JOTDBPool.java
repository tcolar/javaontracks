/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import net.jot.logger.JOTLogger;

/**
 * This is an implementation of a database connection pooling system
 * It manages connections, adds some when needed, and periodically
 * check there status and renew them is they appear corrupt/hung.
 * It also release connections if they aren't used for a while.
 *
 *
 *@author     tcolar
 *@created    August 19, 2003
 */
public class JOTDBPool extends Thread
{
    volatile boolean shutdown = false;
    int statusGap = 15 * 60 * 1000;
    //15 mn

    boolean[] status;
    long[] lastAccess;
    Hashtable cons = new Hashtable();
    JOTDBJDBCSetup setup;
    // 3 a least for maximum, the VF NEEDS at least 3 concurrent connection to work properly (at least 2 aniway :-)
    int maxSize = 10;
    int lastIndex = 0;
    int minSize = 1;
    int initSize = minSize;
    long lastStatus = -1;
    String name="";


    /**
     * Create appol for a particular DB
     *
     *@param  setup  Description of Parameter
     */
    public JOTDBPool(String name, JOTDBJDBCSetup setup) throws Exception
    {
    	this.name=name;
        this.setup = setup;
        maxSize = setup.getMaxConnections();
        status = new boolean[maxSize];
        lastAccess = new long[maxSize];

        int initSize = 2;
        lastIndex = initSize;
        for (int i = 0; i != maxSize; i++)
        {
            status[i] = false;
        }
        for (int i = 0; i <= initSize; i++)
        {
            renewConnection(i);
        }
        JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this, "Initializing DB Pool for "+name);
        start();
    }


    /**
     *The run thread method is there as a whatchdog
     *It will try to find out when connections aren't needed anymore
     *And free them if possible.
     * It also frees "locked" connections.
     * This should be threadsafe.
     */
    public void run()
    {
    	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.DEBUG_LEVEL, this, "Starting Pool: "+name);
        while (true && !shutdown)
        {
            boolean done = false;
            long now = new Date().getTime();
            try
            {
                sleep(25000);
            } catch (Exception e)
            {
            }
            //check every 1 mn
            //log(1, "DBPool", "Cleaning up db connections");
            int released = 0;
            int locked = 0;
            synchronized (this)
            {
                // downsizing pool if possible
                for (int i = lastIndex; i >= minSize && !done; i--)
                {
                    // cleaning connections unused fro over 5 hours
                    if (now - lastAccess[i] > 60000 * 60 * 5)
                    {
                        released++;
                        try
                        {
                            dropConnection(i);
                        } catch (Exception e)
                        {
                        	JOTLogger.logException(JOTLogger.CAT_DB,JOTLogger.ERROR_LEVEL, this, "Failed releasing connection for "+name , e);
                        }
                        lastIndex--;
                    } else
                    {
                        done = true;
                    }

                }
                //renew locked connection
                for (int i = lastIndex; i >= 0; i--)
                {
                    if (now - lastAccess[i] > 60000 * 60 * 5)
                    {
                        if (status[i] == true)
                        {
                            locked++;
                            try
                            {
                                renewConnection(i);
                            }
                            catch(Exception e)
                            {
                                JOTLogger.logException(JOTLogger.CAT_DB,JOTLogger.ERROR_LEVEL,this,"Failed reneweing a DB Connection !!", e);
                            }
                        }
                    }
                }
            }

            if (released != 0)
            {
            	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this, name+":" + released + " db connections where released because unused down to " + lastIndex);
            }
            if (locked != 0)
            {
            	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this, name+":"  + locked + " db connections where renewed, because they were locked down to" + lastIndex);
            }

            if (now - lastStatus > statusGap)
            {
            	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this, name+": Pool size at " + new Date().toString() + " : " + lastIndex);
                lastStatus = now;
            }
        }
        JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this, "Stopping Pool: "+name);
	
    }


    /**
     * free a connection when not usefull anymore.
     *
     *@param  con  Description of Parameter
     */
    public void releaseConnection(JOTTaggedConnection con)
    {
        releaseConnection(con.getConnection(), con.getId());
    }


    /**
     *  Description of the Method
     *
     *@param  con  Description of the Parameter
     *@param  i    Description of the Parameter
     */
    public void releaseConnection(Connection con, int i)
    {
    	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.DEBUG_LEVEL, this,name+": Freing connection: " + i);
        status[i] = false;
    }



    /**
     * This get a connection from the pool
     * Returns the "first available".
     * This is synchronized since multithread.
     *
     *@return    The connection value
     */
    public synchronized JOTTaggedConnection retrieveConnection() throws Exception
    {
    	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.DEBUG_LEVEL, this,name+": Retrieving a connection");
        if (shutdown)
        {
            // this should not happen but we protect against problems anyway.
        	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.WARNING_LEVEL, this,name+": Apparently i got shutdown even though i should not, restarting");
            shutdown = false;
            start();
        }

        int maxDelay = 30000;
        int gap = 1500;
        int i;

        int delay = 0;
        do
        {
            i = 0;
            while (i < maxSize && status[i] != false)
            {
                i++;
            }
            if (i >= maxSize)
            {
            	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.WARNING_LEVEL, this,name+": Couldn't get a connection to the db, waiting " + gap + " ms");
                try
                {
                    Thread.sleep(gap);
                } catch (Exception e)
                {
                }
                delay += gap;
            }
        } while (i >= maxSize && delay < maxDelay);
        if (i >= maxSize)
        {
        	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.ERROR_LEVEL, this,name+": Reached max tries to get a connection to the db");
            return null;
        }
        if (i > lastIndex)
        {
            renewConnection(i);
            lastIndex = i;
        }
        status[i] = true;
        Connection con = null;
        con = (Connection) cons.get("" + i);
        boolean ok = true;
        try
        {
            if (con == null || con.isClosed())
            {
                ok = false;
            }
        } catch (Exception e)
        {
            ok = false;
        }
        if (!ok)
        {
        	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.WARNING_LEVEL, this, name+": A db connection seems to have died (" + i + "), renewing it");
            //trying to renew it
            renewConnection(i);
            con = (Connection) cons.get("" + i);
        }
        updateAccessTime(i);
        return new JOTTaggedConnection(name, i, con);
    }


    /**
     * Renew a connection when it got corrupted.
     *
     *@param  index  Description of Parameter
     *@return        Description of the Returned Value
     */
    public synchronized Connection renewConnection(int index) throws Exception
    {
    	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this,name+": Renewing connection: " + index);

        Connection con = null;
        Object obj = cons.get("" + index);
        if (obj != null)
        {
            con = (Connection) obj;
            try
            {
                con.close();
            } catch (Exception e)
            {
            	JOTLogger.logException(JOTLogger.CAT_DB,JOTLogger.ERROR_LEVEL, this,name+": Couldn't close a db connection: " , e);
            }
            con = null;
            cons.remove("" + index);
        }

        try
        {
        	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this,name+": Trying to open a connection to :" + setup.getURL() + " / " + setup.getUser());

            Properties prop = new java.util.Properties();
            prop.put("user", setup.getUser());
            prop.put("password", setup.getPassword());
            prop.put("useUnicode", "" + setup.getUseUnicode());
            if (setup.getEncoding() != null)
            {
                prop.put("characterEncoding", setup.getEncoding());
            }

            con = DriverManager.getConnection(setup.getURL(), prop);
            cons.put("" + index, con);
            updateAccessTime(index);
            status[index] = false;
        } catch (Exception e)
        {
            status[index] = false;
            JOTLogger.logException(JOTLogger.CAT_DB,JOTLogger.ERROR_LEVEL, this,name+": Couldn't renew a connection to the db: " + setup.getURL(), e);
            throw(new Exception("Couldn't renew a connection to the db: " + setup.getURL(),e));
        }
        return con;
    }


    /**
     *  Release/removes a connection
     *
     *@param  i  Description of the Parameter
     */
    public synchronized void dropConnection(int i)
    {
    	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this,name+": Dropping connection" + i + " (last:" + lastIndex + ")");
        status[i] = false;
        try
        {
            Connection con = (Connection) cons.get("" + i);
            con.close();
        } catch (Exception e)
        {
        	JOTLogger.logException(JOTLogger.CAT_DB,JOTLogger.ERROR_LEVEL, this,name+": Error dropping a connection: " , e);
        }
        cons.remove("" + i);
    }


    /**
     * Release/closes all the connections
     */
    public void dropAll()
    {
        for (int i = 0; i != lastIndex; i++)
        {
            dropConnection(i);
        }
        lastIndex = 0;
    }


    /**
     *  Terminates a pool and releases resources
     */
    public void shutdown()
    {
    	JOTLogger.log(JOTLogger.CAT_DB,JOTLogger.INFO_LEVEL, this,name+": Shutting down !");
        dropAll();
        shutdown = true;
        interrupt();
    }


    /**
     *  will automatically call shutdown when java app is terminated, to avoid hung DB connections.
     */
    public void finalize() throws Throwable
    {
        shutdown();
        super.finalize();
    }


    /**
     *  Updates the last time a pooled connection was used, so we can manage it better.
     *
     *@param  dataId  Description of the Parameter
     */
    public synchronized void updateAccessTime(int id)
    {
        lastAccess[id] = new Date().getTime();
    }
}


