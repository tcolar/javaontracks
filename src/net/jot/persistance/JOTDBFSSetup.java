/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import net.jot.logger.JOTLogger;

/**
 * Setting for an FSDB database
 * @author tcolar
 */
public class JOTDBFSSetup 
{

  private Class upgrader=null;

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
}
