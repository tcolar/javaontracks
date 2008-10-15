/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server.impl;

import java.util.Hashtable;
import net.jot.web.server.JOTMiniServer;

/**
 * Implements a minimalistic web server (static content)
 * @author tcolar
 */
public class JOTStaticWebServer
{

    public final JOTMiniServer server = new JOTMiniServer();
    public final static String ROOT_FOLDER = "ROOT_FOLDER";

    public void start(int port, String rootFolder)
    {
        try
        {
            Hashtable params=new Hashtable();
            params.put(ROOT_FOLDER,rootFolder);
            System.out.println("Server started on port "+port);
            server.start(port, JOTStaticServerHandler.class, params);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        server.stop();
    }

    public static void main(String args[])
    {
        new JOTStaticWebServer().start(8033,"/tmp/");
    }
}
