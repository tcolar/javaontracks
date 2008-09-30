/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import net.jot.JOTInitializer;
import net.jot.db.JOTDBJDBCSetup;
import net.jot.db.JOTDBManager;
import net.jot.logger.JOTLogger;
import net.jot.prefs.JOTPreferenceInterface;
import net.jot.prefs.JOTPreferences;
import net.jot.prefs.JOTPropertiesPreferences;
import net.jot.scheduler.JOTClock;
import net.jot.utils.JOTUtilities;

/**
 * High level manager of all the JOT connections defined in db.properties
 * @author tcolar
 */
public class JOTPersistanceManager
{
  public final static String DBTYPE_JOTDB = "jotdb";
  public final static String DBTYPE_JDBC = "jdbc";
  // true while a db upgrade is in process
  private boolean upgradeRunning;

  public String jotdbFolder = null;
  public String versionFile = "versions.properties";
  //public static loglevels=null;

  public Hashtable databases = new Hashtable();

  // singleton
  private static final JOTPersistanceManager instance=new JOTPersistanceManager();

  public static JOTPersistanceManager getInstance()
  {
      return instance;
  }

  /**
         * Initializes from the db property file
         * @param prefs
         */
  public void init(JOTPreferenceInterface prefs) throws Exception
  {
    loadFromPrefs(prefs);
    upgradeDbs();
  }

  public Hashtable getDatabases()
  {
    return databases;
  }

  public void destroy()
  {
    JOTDBManager.getInstance().shutdown();
  }

  /**
   * return the database name for a storage
   * @param storageName
   * @return
   */
    public String getDbName(String storageName)
    {
        Object o=getDatabases().get(storageName);
        if(o instanceof JOTDBFSSetup)
        {
            return ((JOTDBFSSetup)o).getName();
        }
        else if(o instanceof JOTDBJDBCSetup)
        {
            return ((JOTDBJDBCSetup)o).getDbName();
        }
        return null;
    }

  boolean isDBUpgradeRunning()
  {
    return upgradeRunning;
  }

  /**
         * loads the db property file
         * @param prefs
         */
  private void loadFromPrefs(JOTPreferenceInterface prefs) throws Exception
  {
    databases = new Hashtable();
    // Parses the prefs and load the DB list.
    String databaseList = prefs.getString("jot.db.properties_files");

    jotdbFolder = JOTUtilities.isWindowsOS() ? prefs.getString("db.fs.root_folder.windows") : prefs.getString("db.fs.root_folder.others");
    if (jotdbFolder == null)
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "No jot.fs.root_folder.xxx defined in: " + prefs);
    }
    else
    {
        new File(jotdbFolder).mkdirs();
    }


    if (databaseList != null)
    {
      String[] dbs = databaseList.split(",");
      for (int i = 0; i != dbs.length; i++)
      {
        String dbProps = dbs[i];
        File props = ((JOTPreferences) prefs).findAssociatedPropsFile(dbProps);
        if (props != null)
        {
          loadDbs(props);
        }

      }
    }
    else
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "No DB(s) defined (jot.db.properties_files) in: " + prefs);
    }
  }

  /**
         * Loads the databases according to the property file
         * @param props
         */
  private void loadDbs(File props) throws Exception
  {
    String dbHandle = null;
    JOTPropertiesPreferences dbPrefs = new JOTPropertiesPreferences();
    try
    {
      dbPrefs.loadFrom(new FileInputStream(props));
      String type = dbPrefs.getString("db.type");
      if (type == null)
      {
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "db.type missing from property file: " + props);
      }
      else
      {
        dbHandle = dbPrefs.getString("db.name");
        if (dbHandle == null)
        {
          JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "db.type missing from property file: " + props);
        }
        else
        {
          // during test we want only the "test" db loaded as "default"
          String dbKey=dbHandle;
          if(JOTInitializer.getInstance().isTestMode())
          {
              if(! dbHandle.equalsIgnoreCase("test"))
                  return;
              else
                  dbKey="default";
          }
          // load db
          if (type.equalsIgnoreCase(DBTYPE_JDBC))
          {
            JOTDBJDBCSetup setup = new JOTDBJDBCSetup();
            setup.setDbName(dbHandle);
            String upgrader = dbPrefs.getString("db.upgrader.class");
            setup.setUpgraderClass(upgrader);
            String url = dbPrefs.getString("db.jdbc.url");
            setup.setURL(url);
            String password = dbPrefs.getDefaultedString("db.jdbc.password", "");
            setup.setPassword(password);
            String driver = dbPrefs.getString("db.jdbc.driver");
            setup.setDriver(driver);
            String user = dbPrefs.getString("db.jdbc.user");
            setup.setUser(user);
            String maxCons = dbPrefs.getDefaultedString("db.jdbc.max_connections", "10");
            setup.setMaxConnections(new Integer(maxCons).intValue());
            Boolean unicode = dbPrefs.getDefaultedBoolean("db.jdbc.is_unicode", Boolean.TRUE);
            setup.setUnicode(unicode.booleanValue());
            String encoding = dbPrefs.getString("db.jdbc.encoding");
            if (encoding != null && encoding.length() == 0)
            {
              encoding = null;
            }
            setup.setEncoding(encoding);

            databases.put(dbHandle, setup);
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "JOTPersistanceManager", "Added jdbc Database: " + dbHandle);
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, "JOTPersistanceManager", "Jdbc Database setup: " + setup);

            // loading this DB pool
            JOTDBManager.getInstance().loadDb(dbHandle, setup);

          }
          else if (type.equalsIgnoreCase(DBTYPE_JOTDB))
          {
            if (jotdbFolder == null)
            {
              JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "Cannot use jotdb, jot.fs.root_folder.xxx undefined in master property file !");
            }
            else
            {
              if(! new File(getDbFolder(dbHandle)).exists())
                      new File(getDbFolder(dbHandle)).mkdirs();
              if(! new File(getDbBackupFolder(dbHandle)).exists())
                      new File(getDbBackupFolder(dbHandle)).mkdirs();
              JOTDBFSSetup setup = new JOTDBFSSetup();
              setup.setName(dbHandle);
              String upgrader = dbPrefs.getString("db.upgrader.class");
              setup.setUpgraderClass(upgrader);
              //	other options to read ??
              databases.put(dbKey, setup);
              JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Added jotdb Database: " + dbHandle);
            }
          }
          else
          {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "Unrecognized DB type: " + type);
          }
        }

      }
    }
    catch (Exception e)
    {
      JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "Failed to load DB Properties File: " + props);
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "Failed loading db property file.", e);
      throw(new Exception("Failed to load the database(s): ",e));
    }
  }

  public int getDbVersion(String db)
  {
    int version = 1;
    JOTDBUpgrader upgrader=getUpgrader(db);
    if(upgrader!=null)
    {
      version=upgrader.getLatestVersion();
    }
    return version;
  }

  
  public JOTDBUpgrader getUpgrader(String db)
  {
    Object o = databases.get(db);
    Class upgrader = null;
    if (o instanceof JOTDBJDBCSetup)
    {
      upgrader = ((JOTDBJDBCSetup) o).getUpgraderClass();
    }
    else if (o instanceof JOTDBFSSetup)
    {
      upgrader = ((JOTDBFSSetup) o).getUpgraderClass();
    }
    JOTDBUpgrader up=null;
    try
    {
      up=(JOTDBUpgrader)upgrader.newInstance();
    }
    catch(Exception e){}
    return up;
  }

  /**
   * Whole method is synchronized to ensure the DB's are untouched during upgrade.
   */
  private synchronized void upgradeDbs() throws Exception
  {
    Enumeration keys = databases.keys();
    while (keys.hasMoreElements())
    {
      String dbName = (String) keys.nextElement();
      
      JOTDBUpgrader up=getUpgrader(dbName);
      if (up != null)
      {
        int oldVersion = getCurrentDBVersion(dbName);
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Old Version: " +oldVersion+", new Version:"+up.getLatestVersion());
        if (oldVersion != up.getLatestVersion())
        {
          upgradeRunning=true;
          JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Starting upgrade of DB: " + dbName);
          // backup first
          String backupFolder=null;
          try
          {
            backupFolder=backupDb(dbName);
          }
          catch (Exception e)
          {
            String message = "Backing up the DB: " + dbName + " failed! won't run the upgrade without a good backup !";
            JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", message, e);
            // exit
             upgradeRunning=false;
             throw new Exception(message);          
          }
          try
          {
            //backup went ok, calling the upgrader impl.
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Backup complete, Starting DBUpgrader impl: " + up.getClass().getName());           
            up.upgradeDb(oldVersion);
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Completed DBUpgrader impl (no errors): " + up.getClass().getName());           
          }
          catch (Exception e)
          {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "Upgdrading the DB threw an exception !!");           
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "Restoring db from latest known working backup: "+backupFolder);           
            // restoring latest good backup
            try
            {
              restoreDb(dbName, backupFolder);
            }
            catch(Exception e2)
            {
             JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "Restoring the Backup failed as well !!", e2);              
            }
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "The DB was fully restored to the latest stable version, now you have a few options:");           
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "1) Maybe the error was temporary, so you can try to restart the app so it will retry the upgrade.");           
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "2) You could go back to the previous version of the application which should work with this version of the DB.");           
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", "3) If neither of those options are OK, then you should backup your WHOLE DB foler(including backups) and contact technical support.");           
            // exit
            String message = "Upgrading the DB: " + dbName + " failed! see logs for more infos.";
            JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, "DBManager", message, e);
             upgradeRunning=false;
             throw new Exception(message);
          }
          int newVersion = up.getLatestVersion();
          setCurrentDBVersion(dbName, newVersion);

          JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Completed upgrade of DB " + dbName + " from version:" + oldVersion + " to Version: " + newVersion);
          upgradeRunning=false;
        }
        else
        {
          JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "No upgrade needed(already at latest version) for: " + dbName);
        }
      }
      else
      {
        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "No upgrader defined for: " + dbName);
      }
    }
  }

  private int getCurrentDBVersion(String dbName)
  {
    // if the db does not exist yet, then it will be created with the current version #
    int version = getUpgrader(dbName).getLatestVersion();
    File f = new File(jotdbFolder, versionFile);
    JOTPropertiesPreferences versions = new JOTPropertiesPreferences();
    try
    {
      if (!f.exists())
      {
        f.createNewFile();
      }
      versions.loadFrom(f);
      Integer versionInt = versions.getInt(dbName);
      if(versionInt!=null)
      {
          version=versionInt.intValue();
      }
      else
      {
          // does not exist yet, set to current version
          setCurrentDBVersion(dbName, version);
      }
    }
    catch (Exception e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "Problem finding DB version for: " + dbName, e);
      setCurrentDBVersion(dbName, version);
    }
    return version;
  }

  private void setCurrentDBVersion(String dbName, int version)
  {
    File f = new File(jotdbFolder, versionFile);
    JOTPropertiesPreferences versions = new JOTPropertiesPreferences();
    try
    {
      if (!f.exists())
      {
        f.createNewFile();
      }
      versions.loadFrom(f);
      versions.setString(dbName, "" + version);
      FileOutputStream out = new FileOutputStream(f);
      versions.saveTo(out);
      out.close();
    }
    catch (Exception e)
    {
      JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.ERROR_LEVEL, "DBManager", "Problem writing DB version for: " + dbName, e);
    }
  }

  public String getDbFolder(String dbName)
  {
    return JOTUtilities.endWithSlash(JOTUtilities.endWithSlash(jotdbFolder)+dbName);
  }
  
  public String getDbBackupFolder(String dbName)
  {
    return JOTUtilities.endWithSlash(getDbFolder(dbName)+"__BACKUPS__");
  }
  
  public String backupDb(String dbName) throws Exception
  {
     JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Starting backup of DB: " + dbName);
     String dbFolder=getDbFolder(dbName);
     String backupFolder=getDbBackupFolder(dbName)+"_"+JOTClock.getDateString();
     new File(backupFolder).mkdirs();
     JOTUtilities.copyFolderContent(new File(backupFolder), new File(dbFolder), false);
     JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Completed backup of DB: " + dbName+" under:"+backupFolder);
     JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "This is the last stable backup, should the upgrade fail, you could restore from this backup.");
     JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "ie: copy * from:"+backupFolder+" to:"+dbFolder);
     return backupFolder;
  }
  
  private void restoreDb(String dbName, String backupFolder) throws Exception
  {
    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Starting restore of DB: " + dbName+" from"+backupFolder);
    JOTUtilities.copyFolderContent(new File(getDbFolder(dbName)), new File(backupFolder), false);
    JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, "DBManager", "Completed restore of DB: " + dbName);    
  }

  /**
   * Used by test module to temporarely set the "test" db as the default one.
   */
  public void setTestDbAsDefault() throws Exception
  {
      Hashtable dbs=getDatabases();
      if(dbs.containsKey("test"))
      {
          dbs.remove("default");
          dbs.put("default",dbs.get("test"));
      }
      else
      {
          throw new Exception("no DB called 'test' defined in jot.properties, only found"+dbs.keys());
      }
  }
}
