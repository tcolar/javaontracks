/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.logger;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

import net.jot.prefs.JOTPreferenceInterface;
import net.jot.scheduler.JOTClock;
import net.jot.utils.JOTUtilities;

/**
 * Thi is a simple implementation of a logging system.
 * It is thread safe, and logs all to a single file (rotated at each restart)
 * Here are the existing levels:<br>
 * 	TRACE_LEVEL=0 <br>
 *	DEBUG_LEVEL=1 <br>
 *	INFO_LEVEL=2 <br>
 *	WARNING_LEVEL=3 <br>
 *	ERROR_LEVEL=4 <br>
 *	CRITICAL_LEVEL=5 <br>
 *
 * @author thibautc
 *
 */
public class JOTLogger
{
    // regexp to be used by JOTLoggerApp
    private static final String regexp = "^(\\S+)\\s+(\\d)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(.*)$";
    //                                    cat      level     date      servlet    user    text
    private static final String tstampRegexp = "(\\d+)/(\\d+)/(\\d+)_(\\d+):(\\d+):(\\d+)-(\\d+)";
    //                                       m       d       y      h     mn     s      ms
    public static final int TRACE_LEVEL = 0;
    public static final int DEBUG_LEVEL = 1;
    public static final int INFO_LEVEL = 2;
    public static final int WARNING_LEVEL = 3;
    public static final int ERROR_LEVEL = 4;
    public static final int CRITICAL_LEVEL = 5;
    public static String[] ALL_LEVELS={"0","1","2","3","4","5"};
    private static String file = "";
    private final static int NOT_INITED = 0;
    private final static int INITED_OK = 1;
    private final static int INITED_FAILED = 2;
    private static boolean printToConcole = false;
    private static boolean printStackTrace = true;
    private static int status = NOT_INITED;
    private static PrintStream printer = null;
    // Until the logger is properly initialized, we log to a in memory buffer, once inited, the buffer is flushed to the log filed and cleared.
    private static ByteArrayOutputStream tmpStream = null;
    private static Vector levels = null;
    private static String defaultCategory = "APP";
    private static Vector categories = null;
    public static final String CAT_MAIN = "JOT";
    public static final String CAT_DB = "JOT.DB";
    public static final String CAT_FLOW = "JOT.FLOW";

    /**
     * Sets the default logging category (ie: "Myapp")
     * @param cat
     */
    public static void setDefaultCategory(String cat)
    {
        defaultCategory = cat;
    }

    /**
     *Sets the log level to be enabled (other levels won't be logged)
     *levels can be like this:
     * [0,1,2]      0, 1 and 2
     * 0 is Trace, 5 is Critical
     *@param  levelString  The new level value
     */
    public static void setLevels(String[] levelString)
    {
        levels = new Vector();
        for (int i = 0; i != levelString.length; i++)
        {
            try
            {
                if (levelString[i].indexOf("-") != -1)
                {
                    //range
                    try
                    {
                        String[] edges = levelString[i].split("-");
                        int floor = new Integer(edges[0]).intValue();
                        int ceiling = new Integer(edges[1]).intValue();
                        if (floor < ceiling)
                        {
                            for (int j = floor; j <= ceiling; j++)
                            {
                                levels.add(new Integer(j));
                            }
                        }
                    } catch (Exception e2)
                    {
                        System.err.println("Log level range: " + levelString[i] + " is not valid " + e2);
                    }
                } else
                {
                    levels.add(new Integer(levelString[i]));
                }
            } catch (Exception e)
            {
                System.err.println("Log level: " + levelString[i] + " not recognized");
            }
        }
        JOTLogger.log(CAT_MAIN, DEBUG_LEVEL, "JOTLogger", "Log Levels: " + levels);
    }

    /**
     * "Tail" the log file to get the latest entries
     * @param levels
     * @param max
     * @return
     * @throws java.lang.Throwable
     */
    public static Vector getLastLogEntries(Vector levels, int max) throws Throwable
    {
        Vector lines = null;
        JOTFileTailer tailer = new JOTFileTailer();
        tailer.loadFile(file);
        JOTLogFilter filter = new JOTLogFilter(levels);
        lines = tailer.tail(max, filter);

        return lines;
    }

    /**
     * Log a message
     * @param level mesaage level, ie: 0 for debug
     * @param o Object, the object name (ie: Myclass) will be logged as the "location"
     *          If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message 
     */
    public static void log(int level, Object o, String message)
    {
        log(defaultCategory, level, o, "", message);
    }

    /**
     * Log a message
     * @param cat   log category (ie: Myapp")
     * @param level log level
     * @param o     Object, the object name (ie: Myclass) will be logged as the "location"
     * If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message 
     */
    public static void log(String cat, int level, Object o, String message)
    {
        log(cat, level, o, "", message);
    }

    /**
     * Log a message
     * @param level    log level
     * @param o        Object, the object name (ie: Myclass) will be logged as the "location"
     * If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user	   If you want to track which user/ip the request came from pass it here.
     * @param message
     */
    public static void log(int level, Object o, String user, String message)
    {
        log(defaultCategory, level, o, user, message);
    }

    /**
     * Log a message
     * @param cat	   log category (ie: Myapp")
     * @param level    log level
     * @param o        Object, the object name (ie: Myclass) will be logged as the "location"
     * If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user	   If you want to track which user/ip the request came from pass it here.
     * @param message
     */
    public static void log(String cat, int level, Object o, String user, String message)
    {
        if (o instanceof String)
        {
            log(cat, level, (String) o, user, message);
        } else if (o instanceof Class)
        {
            log(cat, level, ((Class) o).getName(), user, message);
        } else
        {
            log(cat, level, o.getClass().getName(), user, message);
        }
    }

    /**
     * Log a message
     * @param cat		log category (ie: Myapp")
     * @param level			log level
     * @param location  	Where we logged that from (ie: class/servlet name)
     * @param user			If you want to track which user/ip the request came from pass it here.
     * @param message
     */
    public static void log(String cat, int level, String servletName, String user, String message)
    {
        boolean catMatch = categories == null || categories.contains(cat);
        boolean levelMatch = levels == null || levels.contains(new Integer(level));
        if (levelMatch && catMatch)
        {
            if (cat == null)
            {
                cat = defaultCategory;
            }
            cat = cat.replaceAll("\\s+", "_");
            user = user.replaceAll("\\s+", "_");
            servletName = servletName.replaceAll("\\s+", "_");

            String time = JOTClock.getDateStringWithMs();
            addLogEntry(cat + " " + level + " " + time + " " + servletName + " " + user + " " + message);


            if (printToConcole && status == NOT_INITED)
            {
                System.out.println(cat + level + " " + time + " " + servletName + " " + user + " " + message);
            }
        }
    }

    /**
     * Initializes the logger
     * @param prefs    file with the logger prefs
     * @param logFile  where to log to
     */
    public static synchronized void init(JOTPreferenceInterface prefs, String logFile)
    {
        String folder = null;

        if (JOTUtilities.isWindowsOS())
        {
            folder = prefs.getString("jot.logger.folder.windows");
        } else
        {
            folder = prefs.getString("jot.logger.folder.others");
        }
        init(prefs, folder, logFile);
    }

    /**
     * Initializes the logger manually
     * @param prefs 
     * @param folder
     * @param logFile
     */
    public static synchronized void init(JOTPreferenceInterface prefs, String folder, String logFile)
    {
        //creating the folder if it does not exist yet
        new File(folder).mkdirs();
        folder = JOTUtilities.endWithSlash(folder);
        folder += logFile;
        String levelsString = prefs.getString("jot.logger.levels");
        if (levelsString == null)
        {
            levelsString = "0-5";
        }
        String[] levels = levelsString.split(",");
        String catString = prefs.getString("jot.logger.categories");
        if (catString == null || catString.equals("") || catString.startsWith("*"))
        {
            catString = null;
        }
        init(folder, levels, catString);

    }

    /**
     * log the exception & trace with level: ERROR  and default Category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     * @param e
     */
    public static void logException(Object o, String message, Throwable e)
    {
        logException(ERROR_LEVEL, o.getClass(), message, e);
    }
    
    /**
     * log the exception & trace with level: ERROR
     * @param category log category (ie: Myapp")
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     * @param e
     */
    public static void logException(String category, Object o, String message, Throwable e)
    {
        logException(category, ERROR_LEVEL, o.getClass(), message, e);
    }
    
    /**
     * Log an exception
     * @param level			log level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     * @param e				Throwable/Exception that occured
     */
    public static void logException(int level, Object o, String message, Throwable e)
    {
        logException(defaultCategory, level, o.getClass(), message, e);
    }

    /**
     * Log an exception (with the whole stack)
     * @param cat		log category (ie: Myapp")
     * @param level			log level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     * @param e				Throwable/Exception that occured
     */
    public static void logException(String category, int level, Object o, String message, Throwable e)
    {
        if (o instanceof String)
        {
            logException(category, level, (String) o, message,"", e);
        } else
        {
            logException(category, level, o.getClass().getName(), message,"", e);
        }
    }

    /**
     * Log an exception (with the whole stack)
     * @param cat		log category (ie: Myapp")
     * @param level			log level
     * @param location  	Where we logged that from (ie: class/servlet name)
     * @param user			If you want to track which user/ip the request came from pass it here.
     * @param message
     * @param e				Throwable/Exception that occured
     */
    public static void logException(String category, int level, String servletName, String user, String message, Throwable e)
    {
        // outputting the message itself
        log(category, level, servletName, user, message);
        boolean catMatch = categories == null || categories.contains(category);

        if (printStackTrace && catMatch)
        {
            ByteArrayOutputStream b = new ByteArrayOutputStream(50000);
            e.printStackTrace(new PrintStream(b));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(b.toByteArray())));
            String s = "";
            try
            {
                while ((s = reader.readLine()) != null)
                {
                    log(category, level, "trace", "", s);
                }
            } catch (Exception e2)
            {
                log(category, ERROR_LEVEL, "JOTlogger", "", "Exception logging an exception !!  :" + e2);
            }
        }
    }

    /**
     * Manually init the logger
     *
     *@param  fileName  path to the log file
     *@param  logLevel  log levels to enable
     *@param categories categories to enable (null means all). ie: "JOT,MYAPP"
     */
    public static synchronized void init(String fileName, String levels[], String categories)
    {
        JOTLogger.log(CAT_MAIN, INFO_LEVEL, "JOTLogger", "Initializing Logger");
        setLevels(levels);
        setCategories(categories);
        file = fileName;
        log(CAT_MAIN, INFO_LEVEL, "JOTLogger", "Log file is:" + file);
        try
        {
            File f = new File(file);
            if (f.exists())
            {
                // backup previous log file.
                String nowString = JOTClock.getDateString();
                File backupFile = new File(file + "_" + nowString);
                log(CAT_MAIN, INFO_LEVEL, "JOTLogger", "Backing up old log file: " + file + "  To: " + backupFile);
                // rename to seems to fail, use copy/delete instead
                JOTUtilities.copyFile(backupFile, f);
                f.delete();
            }
            f = new File(file);
            if (tmpStream != null)
            {
                try
                {
                    // copy the content of the temp log data.
                    FileOutputStream out = new FileOutputStream(f);
                    // TODO: - filter the stream to only show specific levels
                    out.write(tmpStream.toByteArray());
                    out.flush();
                    out.close();
                    tmpStream = null;
                } catch (Exception e)
                {
                }
            }
            printer = new PrintStream(new FileOutputStream(file, true));
            status = INITED_OK;
        } catch (Exception e)
        {
            System.err.println("Couldn't initialyze the log file (" + file + ")!! " + e);
            status = INITED_FAILED;
        }
    }

    /**
     * Set the categories to enable(null means all). ie: "JOT,MYAPP"
     * @param cat
     */
    public static void setCategories(String cat)
    {
        if (cat == null)
        {
            categories = null;
        } else
        {
            String[] cats = cat.split(",");
            categories = new Vector();
            for (int i = 0; i != cats.length; i++)
            {
                categories.add(cats[i]);
            }
        }

        JOTLogger.log(CAT_MAIN, DEBUG_LEVEL, "JOTLogger", "Log Categories: " + categories + " (null means ALL)");

    }

    /**
     * Log to the temporary log file, if the real log path isn't known yet.
     * Log to the real lof file if path known
     * If both failed write to standard error stream.
     *
     *@param  entry  The feature to be added to the LogEntry attribute
     *@since
     */
    private static synchronized void addLogEntry(String entry)
    {

        if (status == NOT_INITED)
        {
            try
            {
                printer.println(entry);
                printer.flush();
            } catch (Exception e)
            {
                System.err.println(entry);
            }
        } else
        {
            if (status == INITED_OK)
            {
                try
                {
                    printer.println(entry);
                    printer.flush();
                } catch (Exception e)
                {
                    System.err.println(entry);
                }
            } else
            {
                System.err.println(entry);
            }
        }
        if (printToConcole)
        {
            System.out.println(entry);
        }
    }

    /**
     * Initialize the default logger on instance.
     */
    static
    {
        try
        {
            tmpStream = new ByteArrayOutputStream();
            printer = new PrintStream(tmpStream);
            printer.println(JOTClock.getDateStringWithMs() + "*** Restarting log System ***");
        } catch (Exception e)
        {
            System.err.println("Error: Couldn't create Temporary log file ");
        }
    }

    /**
     * Wether to dump all errors to the console as well as the log file.
     * @param printToConcole   default false
     */
    public static void setPrintToConcole(boolean printToConcole)
    {
        JOTLogger.printToConcole = printToConcole;
    }

    /**
     * Wether to dump the exception stacktrace when logException is used
     * @param printStackTrace  default true
     */
    public static void setPrintStackTrace(boolean printStackTrace)
    {
        JOTLogger.printStackTrace = printStackTrace;
    }

    /**
     * Cleanup resources on exit.
     *
     */
    public static void destroy()
    {
        if (printer != null)
        {
            printer.close();
            printer = null;
        }
    }

    // ###############################
    // wapper methods for simplicity
    // ###############################
    
    // TRACE
    
    /**
     * Logs a message with the TRACE level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void trace(Object o, String message)
    {
        log(TRACE_LEVEL, o, message);
    }
    /**
     * Logs a message with the TRACE level
     * @param cat   category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void trace(String cat, Object o, String message)
    {
        log(cat, TRACE_LEVEL, o, "", message);
    }
    /**
     * Logs a message with the TRACE level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user   ex: Ip or user name
     * @param message
     */
    public static void trace(Object o, String user, String message)
    {
        log(TRACE_LEVEL, o, user, message);
    }
    /**
     * Logs a message with the TRACE level
     * @param cat category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user  ex: Ip or user name
     * @param message
     */
    public static void trace(String cat, Object o, String user, String message)
    {
        log(cat, TRACE_LEVEL, o, user, message);
    }
    
    // DEBUG
    
    /**
     * Logs a message with the DEBUG level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void debug(Object o, String message)
    {
        log(DEBUG_LEVEL, o, message);
    }
    /**
     * Logs a message with the DEBUG level
     * @param cat   category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void debug(String cat, Object o, String message)
    {
        log(cat, DEBUG_LEVEL, o, "", message);
    }
    /**
     * Logs a message with the DEBUG level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user   ex: Ip or user name
     * @param message
     */
    public static void debug(Object o, String user, String message)
    {
        log(DEBUG_LEVEL, o, user, message);
    }
    /**
     * Logs a message with the DEBUG level
     * @param cat category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user  ex: Ip or user name
     * @param message
     */
    public static void debug(String cat, Object o, String user, String message)
    {
        log(cat, DEBUG_LEVEL, o, user, message);
    }

        // INFO
    
    /**
     * Logs a message with the INFO level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void info(Object o, String message)
    {
        log(INFO_LEVEL, o, message);
    }
    /**
     * Logs a message with the INFO level
     * @param cat   category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void info(String cat, Object o, String message)
    {
        log(cat, INFO_LEVEL, o, "", message);
    }
    /**
     * Logs a message with the INFO level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user   ex: Ip or user name
     * @param message
     */
    public static void info(Object o, String user, String message)
    {
        log(INFO_LEVEL, o, user, message);
    }
    /**
     * Logs a message with the INFO level
     * @param cat category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user  ex: Ip or user name
     * @param message
     */
    public static void info(String cat, Object o, String user, String message)
    {
        log(cat, INFO_LEVEL, o, user, message);
    }

        // WARNING
    
    /**
     * Logs a message with the WARNING level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void warning(Object o, String message)
    {
        log(WARNING_LEVEL, o, message);
    }
    /**
     * Logs a message with the WARNING level
     * @param cat   category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void warning(String cat, Object o, String message)
    {
        log(cat, WARNING_LEVEL, o, "", message);
    }
    /**
     * Logs a message with the WARNING level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user   ex: Ip or user name
     * @param message
     */
    public static void warning(Object o, String user, String message)
    {
        log(WARNING_LEVEL, o, user, message);
    }
    /**
     * Logs a message with the WARNING level
     * @param cat category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user  ex: Ip or user name
     * @param message
     */
    public static void warning(String cat, Object o, String user, String message)
    {
        log(cat, WARNING_LEVEL, o, user, message);
    }

    // ERROR
    
    /**
     * Logs a message with the ERROR level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void error(Object o, String message)
    {
        log(ERROR_LEVEL, o, message);
    }
    /**
     * Logs a message with the ERROR level
     * @param cat   category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void error(String cat, Object o, String message)
    {
        log(cat, ERROR_LEVEL, o, "", message);
    }
    /**
     * Logs a message with the ERROR level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user   ex: Ip or user name
     * @param message
     */
    public static void error(Object o, String user, String message)
    {
        log(ERROR_LEVEL, o, user, message);
    }
    /**
     * Logs a message with the ERROR level
     * @param cat category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user  ex: Ip or user name
     * @param message
     */
    public static void error(String cat, Object o, String user, String message)
    {
        log(cat, ERROR_LEVEL, o, user, message);
    }

    // CRITICAL
    
    /**
     * Logs a message with the CRITICAL level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void critical(Object o, String message)
    {
        log(CRITICAL_LEVEL, o, message);
    }
    /**
     * Logs a message with the CRITICAL level
     * @param cat   category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param message
     */
    public static void critical(String cat, Object o, String message)
    {
        log(cat, CRITICAL_LEVEL, o, "", message);
    }
    /**
     * Logs a message with the CRITICAL level
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user   ex: Ip or user name
     * @param message
     */
    public static void critical(Object o, String user, String message)
    {
        log(CRITICAL_LEVEL, o, user, message);
    }
    /**
     * Logs a message with the CRITICAL level
     * @param cat category
     * @param o If o is a String then the string will be displayed, otherwise o.getClass().getName()
     * @param user  ex: Ip or user name
     * @param message
     */
    public static void critical(String cat, Object o, String user, String message)
    {
        log(cat, CRITICAL_LEVEL, o, user, message);
    }

    

}
