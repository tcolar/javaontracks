/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jottools;

import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import net.jot.server.JOTMiniWebServer;
import net.jot.server.JOTServerRequestHandler;
import net.jot.utils.JOTUtilities;
import net.jottools.actions.ConfigView;

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
        Enumeration e=Parameters.keys();
        while(e.hasMoreElements())
        {
            String key=(String)e.nextElement();
            System.out.println("Param: "+key+" -> "+(String)Parameters.get(key));
        }
        try
        {
            ConfigView view=getViewClass(path);
            if(view!=null)
            {
                view.writePage(socket.getOutputStream(),Parameters);
            }
            else
            {
                socket.getOutputStream().write("Page Not Found !".getBytes());
            }
        }
        catch(Exception e2)
        {
            e2.printStackTrace();
        }
    }

    private ConfigView getViewClass(String path)
    {
        Object view=null;
        try
        {
        if(path.startsWith("/"))
            path=path.substring(1,path.length());
        if(path.length()==0)
            path="home";
        Class c=Class.forName("net.jottools.actions.Config"+JOTUtilities.upperFirst(path.toLowerCase()));
        if(c!=null)
        {
            view=c.newInstance();
            if (! (view instanceof ConfigView))
            {
                view=null;
            }
        }
        }
        catch(Exception e){e.printStackTrace();view=null;}
        return (ConfigView)view;
    }
}
