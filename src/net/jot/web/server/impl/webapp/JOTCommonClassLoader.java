/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server.impl.webapp;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is the classloader for the "common" user class/libs of the jot server (common to all webapps)
 * Will search in standard java classloader first (delegation) and if not found,
 * in jotserver /common folder (classpath common to all webapp)
 * /common/classes/*
 * /common/lib/*.jar (incl. subfolders)
 * @author thibautc
 */
public class JOTCommonClassLoader extends URLClassLoader
{
    public JOTCommonClassLoader(URL[] urls)
    {
        super(urls);
    }
}
