/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.db;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTDBUpgrader;


/**
 * Object representation of a JDBC database setup options
 * such as url, name ,password etc...
 *@author     tcolar
 *@created    May 6, 2003
 */
public class JOTDBJDBCSetup
{
    String url = "";
    String name = "";
    String password = "";
    String driver = "";
    int max = 10;
    String user = "";
    boolean useUnicode = false;
    String encoding = null;
    Class upgrader=null;

    /**
     *Sets the user attribute of the DbSetup object
     *
     *@param  user  The new user value
     */
    public void setUser(String user)
    {
        this.user = user;
    }


    /**
     *Sets the uRL attribute of the DbSetup object
     *
     *@param  url  The new uRL value
     */
    public void setURL(String url)
    {
        this.url = url;
    }


    /**
     *  Sets the unicode attribute of the DBSetup object
     *
     *@param  b  The new unicode value
     */
    public void setUnicode(boolean b)
    {
        useUnicode = true;
    }


    /**
     *  Sets the encoding attribute of the DBSetup object
     *
     *@param  s  The new encoding value
     */
    public void setEncoding(String s)
    {
        encoding = s;
    }


    /**
     *Sets the dbName attribute of the DbSetup object
     *
     *@param  name  The new dbName value
     */
    public void setDbName(String name)
    {
        this.name = name;
    }


    /**
     *Sets the Password attribute of the DbSetup object
     *
     *@param  Password  The new Password value
     */
    public void setPassword(String password)
    {
        this.password = password;
    }


    /**
     *Sets the driver attribute of the DbSetup object
     *
     *@param  driver  The new driver value
     */
    public void setDriver(String driver)
    {
        this.driver = driver;
    }


    /**
     *Sets the maxConnections attribute of the DbSetup object
     *
     *@param  max  The new maxConnections value
     */
    public void setMaxConnections(int max)
    {
        this.max = max;
    }


    /**
     *Gets the user attribute of the DbSetup object
     *
     *@return    The user value
     */
    public String getUser()
    {
        return user;
    }


    /**
     *Gets the uRL attribute of the DbSetup object
     *
     *@return    The uRL value
     */
    public String getURL()
    {
        return url;
    }


    /**
     *Gets the dbName attribute of the DbSetup object
     *
     *@return    The dbName value
     */
    public String getDbName()
    {
        return name;
    }


    /**
     *Gets the Password attribute of the DbSetup object
     *
     *@return    The Password value
     */
    public String getPassword()
    {
        return password;
    }


    /**
     *Gets the driver attribute of the DbSetup object
     *
     *@return    The driver value
     */
    public String getDriver()
    {
        return driver;
    }


    /**
     *Gets the maxConnections attribute of the DbSetup object
     *
     *@return    The maxConnections value
     */
    public int getMaxConnections()
    {
        return max;
    }


    /**
     *  Gets the useUnicode attribute of the DBSetup object
     *
     *@return    The useUnicode value
     */
    public boolean getUseUnicode()
    {
        return useUnicode;
    }

  public Class getUpgraderClass()
  {
    return upgrader;
  }

  public void setUpgraderClass(String updaterClass)
  {
    if(updaterClass==null) return;
    try
    {
      upgrader=Class.forName(updaterClass);
      if( ! (upgrader.newInstance() instanceof JOTDBUpgrader))
        throw new Exception(updaterClass+" is not of type JOTDBUpgrader !!");
    }
    catch(Exception e)
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "DB upgrader class not found: "+updaterClass);
      upgrader=null;
    }
  }

    /**
     *  Gets the encoding attribute of the DBSetup object
     *
     *@return    The encoding value
     */
    public String getEncoding()
    {
        return encoding;
    }
    
    public String toString()
    {
    	return "Driver:"+driver+" Url:"+url+" User:"+user+" Unicode:"+useUnicode+" Encoding:"+encoding+" MaxCons:"+max;
    }



}

