/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jottools;

import java.net.Socket;
import java.util.Hashtable;
import net.jot.server.JOTMiniWebServer;
import net.jot.server.JOTServerRequestHandler;

/**
 *
 * @author thibautc
 */
public class Configurator implements JOTServerRequestHandler
{
    private final static int DEFAULT_PORT=8033;
    private JOTMiniWebServer server=null;
    
    public Configurator(Integer port)
    {
        server=new JOTMiniWebServer();
        try
        {
            server.start(port.intValue(), this);        
        }
        catch(Exception e)
        {
            System.err.println("Failed starting server: "+e);
            e.printStackTrace();
        }
    }
    
    /**
     * TODO: option to accept connection only from localhost
     * TODO: provide/use a password ?
     * @param args
     */
    public static void main(String[] args)
    {
        Integer port=new Integer(DEFAULT_PORT);
        if (args.length < 1)
        {
            System.out.println("Using default port: "+DEFAULT_PORT);
        }
        else
        {
            String p = args[1];
            port = new Integer(p.trim());
        }
        new Configurator(port);
    }

    public void finalyze() throws Throwable
    {
        if(server!=null)
        {
            server.finalyze();
        }
        super.finalize();
    }
    
    public void handleGetRequest(Socket socket, String path, Hashtable Parameters)
    {
        System.out.println("Request: "+path);
    }    
}
