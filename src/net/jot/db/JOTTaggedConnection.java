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

/**
 * A TaggedConnection is basically a Standard DB connection with a name(tag), that allows us to know 
 * which pool it is part of.
 *@author     tcolar
 *@created    May 6, 2003
 */
public class JOTTaggedConnection
{
    private int id = -1;
    private Connection con;
    private String poolName = "";


    /**
     *Constructor for the TaggedConnection object
     *
     *@param  i     Description of Parameter
     *@param  con   Description of Parameter
     *@param  name  Description of the Parameter
     */
    JOTTaggedConnection(String name, int i, Connection con)
    {
        id = i;
        this.con = con;
        poolName = name;
    }


    /**
     *Gets the connection attribute of the TaggedConnection object
     *
     *@return    The connection value
     */
    public Connection getConnection()
    {
        return con;
    }


    /**
     *  Gets the poolName attribute of the TaggedConnection object
     *
     *@return    The poolName value
     */
    public String getPoolName()
    {
        return poolName;
    }


    /**
     *Gets the ID of the connection.
     *
     *@return    The dataId value
     */
    int getId()
    {
        return id;
    }
}

