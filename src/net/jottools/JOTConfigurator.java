/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jottools;

import net.jot.utils.JOTUtilities;
import net.jot.web.server.impl.JOTMiniServer;
import net.jottools.actions.ConfigView;

/**
 *
 * @author thibautc
 */
public class JOTConfigurator
{
    private final static int DEFAULT_PORT=8033;
    private JOTMiniServer server=null;
    
    public JOTConfigurator(Integer port)
    {
        server=new JOTMiniServer();
        try
        {
            server.start(port.intValue(), JOTConfiguratorHandler.class,null);        
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
        new JOTConfigurator(port);
    }

    public void finalyze() throws Throwable
    {
        if(server!=null)
        {
            server.finalyze();
        }
        super.finalize();
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
