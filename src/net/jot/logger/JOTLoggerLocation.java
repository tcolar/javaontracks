/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.logger;

/**
 * A logger location - similar to log4j
 * Uses JOTLogger under the covers
 * 
 * JOTLoggerLocation loc=new JOTLogerLocation(getClass());
 * @author thibautc
 */
public class JOTLoggerLocation 
{
    public static final String CAT_MAIN = JOTLogger.CAT_MAIN;
    public static final String CAT_DB = JOTLogger.CAT_DB;
    public static final String CAT_FLOW = JOTLogger.CAT_FLOW;
    private String area=JOTLogger.CAT_MAIN;
    private String cat="";
    
    private JOTLoggerLocation(){}
    public JOTLoggerLocation(Class clazz)
    {
        this(clazz.getName());
    }
    public JOTLoggerLocation(String clazz)
    {
        this(JOTLogger.CAT_MAIN,clazz);
    }
    public JOTLoggerLocation(String category, Class clazz)
    {
        this(category,clazz.getName());
    }
    public JOTLoggerLocation(String category, String clazz)
    {
        this.cat=category;
        this.area=clazz;
    }

    public void exception(String message, Throwable e)
    {
        JOTLogger.logException(cat,JOTLogger.ERROR_LEVEL, area, message, e);
    }
    public void exception(String user, String message, Throwable e)
    {
        JOTLogger.logException(cat,JOTLogger.ERROR_LEVEL, user, area, message, e);
    }
    public void trace(String message)
    {
        JOTLogger.log(cat, JOTLogger.TRACE_LEVEL, area, message);
    }
    public void trace(String user, String message)
    {
        JOTLogger.log(cat, JOTLogger.TRACE_LEVEL, area, user, message);
    }
    public void debug(String message)
    {
        JOTLogger.log(cat, JOTLogger.DEBUG_LEVEL, area, message);
    }
    public void debug(String user, String message)
    {
        JOTLogger.log(cat, JOTLogger.DEBUG_LEVEL, area, user, message);
    }
    public void info(String message)
    {
        JOTLogger.log(cat, JOTLogger.INFO_LEVEL, area, message);
    }
    public void info(String user, String message)
    {
        JOTLogger.log(cat, JOTLogger.INFO_LEVEL, area, user, message);
    }
    public void error(String message)
    {
        JOTLogger.log(cat, JOTLogger.ERROR_LEVEL, area, message);
    }
    public void error(String user, String message)
    {
        JOTLogger.log(cat, JOTLogger.ERROR_LEVEL, area, user, message);
    }
    public void critical(String message)
    {
        JOTLogger.log(cat, JOTLogger.CRITICAL_LEVEL, area, message);
    }
    public void critical(String user, String message)
    {
        JOTLogger.log(cat, JOTLogger.CRITICAL_LEVEL, area, user, message);
    }
    
    public static boolean isDebugEnabled()
    {
        return JOTLogger.isDebugEnabled();
    }
    public static boolean isTraceEnabled()
    {
        return JOTLogger.isTraceEnabled();
    }
    public static boolean isInfoEnabled()
    {
        return JOTLogger.isInfoEnabled();
    }
    public static boolean isWarningEnabled()
    {
        return JOTLogger.isWarningEnabled();
    }
    public static boolean isErrorEnabled()
    {
        return JOTLogger.isErrorEnabled();
    }
    public static boolean isCriticalEnabled()
    {
        return JOTLogger.isCriticalEnabled();
    }
}
