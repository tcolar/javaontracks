/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.util.Hashtable;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.web.server.JOTMiniServer;

/**
 * Implements a minimalistic web server (static content)
 * @author tcolar
 */
public class JOTStaticWebServer
{

    public final JOTMiniServer server = new JOTMiniServer();
    public final static String ROOT_FOLDER = "ROOT_FOLDER";
    JOTLoggerLocation logger=new JOTLoggerLocation(JOTLogger.CAT_SERVER,getClass());

    public void start(int port, String rootFolder)
    {
        try
        {
            Hashtable params=new Hashtable();
            params.put(ROOT_FOLDER,rootFolder);
            logger.info("Server started on port "+port);
            System.out.println("Server started on port "+port);
            server.start(port, JOTStaticServerHandler.class, params);
        } catch (Exception e)
        {
            logger.exception("Failed starting the server.", e);
            System.out.println("Failed starting the server.");
            e.printStackTrace();
        }
    }

    public void stop()
    {
        server.stop();
    }

    public static void main(String args[])
    {
        new JOTStaticWebServer().start(8066,"/tmp/");
    }
}
