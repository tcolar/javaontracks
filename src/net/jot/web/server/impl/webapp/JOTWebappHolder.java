/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server.impl.webapp;

import java.io.File;
import java.net.URL;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;

/**
 *
 * @author thibautc
 */
public class JOTWebappHolder
{

    private JOTLoggerLocation loc = new JOTLoggerLocation(JOTLogger.CAT_SERVER, getClass());
    private JOTWebappClassLoader loader;

    /**
     *
     * @param war : might be a war file or an expanded war (folder)
     * @param parentClassLoader
     */
    public JOTWebappHolder(File war, String context, ClassLoader parentClassLoader)
    {
        if (war.isDirectory())
        {
            Vector paths = new Vector();
            File libs = findFolder(war, "/web-inf/libs/");
            File classes = findFolder(war, "/web-inf/classes/");
            if (classes != null)
            {
                try
                {
                    loc.debug("Adding " + classes.getAbsolutePath() + " to webapp[" + context + "] classpath");
                    paths.add(classes.toURL());
                } catch (Exception e)
                {
                    loc.exception("Malformed Classpath URL: " + classes.getAbsolutePath(), e);
                }
            }
            if (libs.exists())
            {
                Vector jars = JOTWebappServer.findAllJars(libs);
                for (int i = 0; i != jars.size(); i++)
                {
                    File jar = (File) jars.get(i);
                    try
                    {
                        loc.debug("Adding " + jar.getAbsolutePath() + " to webapp[" + context + "] classpath");
                        paths.add(jar.toURL());
                    } catch (Exception e)
                    {
                        loc.exception("Malformed Classpath URL: " + classes.getAbsolutePath(), e);
                    }
                }
            }
            URL[] urls = (URL[]) paths.toArray(new URL[0]);
            // init loader
            loader = new JOTWebappClassLoader(urls, parentClassLoader);
        }

    }

    private File findFolder(File war, String file)
    {
        File[] files = war.listFiles();
        for (int i = 0; i != files.length; i++)
        {
            File f = files[i];
            if (f.getAbsolutePath().replaceAll("\\", "/").equalsIgnoreCase(war.getAbsolutePath() + "/" + file))
            {
                return f;
            }
            if (f.isDirectory())
            {
                File found = findFolder(f, file);
                if (found != null)
                {
                    return f;
                }
            }
        }
        return null;
    }
}
