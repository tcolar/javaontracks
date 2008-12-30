/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server.impl.webapp;

import java.net.URL;
import java.net.URLClassLoader;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;

/**
 * WebApp Classloader
 * Each webapp wil have it's own classloader.
 * Classes are looked up in this order:
 * - [jotserver]/webapp/WEB-INF/classes/ *.class
 * - [jotserver]/webapp/WEB-INF/lib/ *.jar
 * - Java classloader (java & classpath)
 * - [jotserver]/common/classes/ *.class
 * - [jotserver]/common/lib/ *.jar
 * @author thibautc
 */
public class JOTWebappClassLoader extends URLClassLoader
{
    private JOTLoggerLocation loc=new JOTLoggerLocation(JOTLogger.CAT_SERVER,getClass());
    /**
     * Parent is JOTCommonClassLoader
     * @param parent
     */
    public JOTWebappClassLoader(URL[] urls, ClassLoader parent)
    {
        super(urls, parent);
    }

    /**
     * [ovveride]
     * We look in webapp jars first, this does NOT follow the classloader spec (delegation)
     * Then Delegate to java std classloader
     * Then we look in the "common" appserver folders
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        // First, check if the class has already been loaded
        Class c = findLoadedClass(name);
        if (c == null)
        {
            c = findClass(name);
            if (c == null)
            {
                c = loadClass(name, resolve);
            }
        }
        if(c!=null)
            loc.trace("Loaded class: "+name);
        return c;
    }
}
