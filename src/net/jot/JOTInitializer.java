/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTFSIndexManager;
import net.jot.persistance.JOTPersistanceManager;
import net.jot.prefs.JOTPreferenceInterface;
import net.jot.prefs.JOTPreferences;

/**
 * This handles the initialization(and shutdown) of JOT.
 * The init methos initialzes all the required objects for JOT to work, such as thr preferences manager, logger etc...
 * 
 * In the case of a webapp, this will be called for you by JOTFilter (defined in web.xml)
 * 
 * In the case of a Java app(not webapp), you HAVE TO call this manually:
 * Call init() when you start your app (very first thing, befor you use any other JOT features)
 * Call destroy() when you stop your app (last thing)
 * @author tcolar
 */
public class JOTInitializer
{

    private static boolean destroyed = false;
    public static final String VERSION = "0.1.2";

    // singleton
    private static final JOTInitializer initializer = new JOTInitializer();

    public final static JOTInitializer getInstance()
    {
        return initializer;
    }
    private JOTInitializer(){}
    
    private boolean testMode = false;

    /**
     * Initializes JOT
     *
     */
    public void init() throws Exception
    {
        // give time to debugger to start
        //Thread.sleep(5000);        
        destroyed = false;
        // Initializing the prefs (we need the prefs to initialized the logger)
        JOTPreferenceInterface prefs = JOTPreferences.getInstance();
        // Initializing the Logger
        JOTLogger.init(prefs, "jot.log");
        // Initialize the persistance / databases(s).
        JOTPersistanceManager.getInstance().init(prefs);
    }

    /**
     * To be called on program exit
     * Cleans up all resources (close open files, stop threads etc ...)
     *
     */
    public void destroy()
    {
        if (!destroyed)
        {
            destroyed = true;
            JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.INFO_LEVEL, JOTInitializer.class, "Destroying");
            try
            {
                JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.DEBUG_LEVEL, JOTInitializer.class, "Stopping PersistanceManager");
                JOTPersistanceManager.getInstance().destroy();
                JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.DEBUG_LEVEL, JOTInitializer.class, "Stopping FSIndexManager");
                //JOTFSIndexManager.destroy();
                JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.DEBUG_LEVEL, JOTInitializer.class, "Stopping Logger");
                JOTLogger.destroy();
                System.out.println("Shutdown complete");
            } catch (Exception e)
            {
                System.err.println(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * On finalize we call destroy so that even if the user forgot to call it, we try to cleanup the resources anyhow.
     */
    protected void finalize() throws Throwable
    {
        destroy();
        super.finalize();
    }

    public void setTestMode(boolean b)
    {
        testMode = b;
    }

    public boolean isTestMode()
    {
        return testMode;
    }
}
