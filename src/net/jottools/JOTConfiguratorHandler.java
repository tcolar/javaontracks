/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jottools;

import java.util.Hashtable;
import net.jot.web.server.JOTWebRequestHandlerBase;

/**
 *
 * @author thibautc
 */
public class JOTConfiguratorHandler extends JOTWebRequestHandlerBase
{

    /*
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
    }*/

    public void handle()
    {
            System.out.println(request);
    }

    public void init(Hashtable params)
    {
    }
}
