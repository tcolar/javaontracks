/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.prefs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import net.jot.logger.JOTLogger;
import net.jot.utils.JOTUtilities;

/**
 * JOT prefrences handler
 * Uses the jot.properties file.
 * @author tcolar
 */
public class JOTPreferences extends JOTPropertiesPreferences
{
    // singleton
    private static JOTPreferences prefs = null;
    private URL prefsFile = null;
    // the web serverwill pass us that at start time.
    private static String webConfPath = null;

    private JOTPreferences(){}

    // Will be called by JOTMainFilter during webapp server start (if we are running as a webapp)
    public static void setWebConfPath(String path)
    {
        webConfPath = path;
    }

    public final static JOTPreferences getInstance()
    {
        if (prefs == null)
        {
            synchronized (JOTPreferences.class)
            {
                    prefs = new JOTPreferences();
                    prefs.initPrefs();
            }
        }
        return prefs;
    }

    /**
     * Loads the props file and init the prefs.
     */
    public void initPrefs()
    {
        // log the used file wherever possible (system out)
        String prefsFile = findPrefsFile();
        JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.INFO_LEVEL, this, "Using Pref file: " + prefsFile);
        File f = new File(prefsFile);
        if (f.exists())
        {
            try
            {
                setPrefsFile(f.toURI().toURL());
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.CAT_MAIN, JOTLogger.CRITICAL_LEVEL, this, "Failed to load Preference file: " + f.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Sets the InputStream to fetch that contains the preferences.
     * @param input
     */
    public void setPrefsFile(URL u) throws IOException
    {
        InputStream input = u.openStream();
        loadFrom(input);
        prefsFile = u;
    }

    /**
     * Search for the prefs file in several places.
     * @return
     */
    public String findPrefsFile()
    {
        // - Djot.prefs=
        try
        {
            String jotPrefs = System.getProperty("jot.prefs");
            if (jotPrefs != null && jotPrefs.trim().length() > 0)
            {
                JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Found property jot.prefs: " + jotPrefs);
                return jotPrefs.trim();
            } else
            {
                JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "No jot.prefs property defined, keep looking for pref file ... ");
            }
        } catch (Exception e)
        {
            JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Could not read system property: jot.prefs");
        }
        // - site WEB-INF/  for webapp
        // would require "request" , should we do ??
        if (webConfPath != null)
        {
            File f = new File(webConfPath, "jot.properties");
            if (f.exists())
            {
                return f.getAbsolutePath();
            }
        }

        // - runtime dir
        String runDir = System.getProperty("user.dir");
        File f = new File(runDir, "jot.properties");
        if (f.exists())
        {
            return f.getAbsolutePath();
        }

        // - ~
        String userDir = System.getProperty("user.home");
        f = new File(userDir, "jot.properties");
        if (f.exists())
        {
            return f.getAbsolutePath();
        }

        // /etc/    ||   c:\etc\
        boolean isWindows = JOTUtilities.isWindowsOS();
        String path = isWindows ? "c:\\etc\\" : "/etc/";
        f = new File(path, "jot.properties");
        if (f.exists())
        {
            return f.getAbsolutePath();
        }


        // not good if we got here !!
        JOTLogger.logException(JOTLogger.CAT_DB, JOTLogger.CRITICAL_LEVEL, this, "", new Exception("No preference file found !!"));
        System.err.println("CRITICAL ERROR: No preference file found !");

        return null;
    }

    public void save()
    {
        try
        {
			File f = new File(prefsFile.toURI());
			if(f.exists() && f.canWrite())
			{
				saveTo(f);
			}
        } catch (Exception e)
        {
        }
    }

    /**
     * Once the main pref file is found, other associated files can be found in the same location(folder)
     * by this method.
     * @param props
     * @return
     */
    public URL findAssociatedPropsItem(String props)
    {
        URL u = null;
        /*if (props.indexOf('/') != -1 || props.indexOf('\\') != -1)
        {
            // try an absolute path
            u = new File(props);
        } else*/
        {
            // try a relative path
			try
			{
				u = prefsFile.toURI().resolve(props).toURL();
			}catch(Exception e){e.printStackTrace();}
        }
        return u;
    }
}
