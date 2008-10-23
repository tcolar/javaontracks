/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server.impl.webapp;

import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import net.jot.web.server.JOTRequestParser;
import net.jot.web.server.JOTServerRequestHandler;
import net.jot.web.server.JOTWebRequest;
import net.jot.utils.JOTUtilities;
import net.jottools.actions.ConfigView;

/**
 * Impl. to handle a request processing.
 * There will be one inctance of this per request/thread
 * @author thibautc
 */
public class JOTWebappRequestProcessor  implements JOTServerRequestHandler{

    public void handle(Socket socket)
    {
        try
        {
            JOTWebRequest request=JOTRequestParser.parseRequest(socket);
        }
        catch(Exception e){e.printStackTrace();}
        /*System.out.println("Request: " + path);
        Enumeration e = Parameters.keys();
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            System.out.println("Param: " + key + " -> " + (String) Parameters.get(key));
        }
        try
        {
            ConfigView view = getViewClass(path);
            if (view != null)
            {
                view.writePage(socket.getOutputStream(), Parameters);
            } else
            {
                socket.getOutputStream().write("Page Not Found !".getBytes());
            }
        } catch (Exception e2)
        {
            e2.printStackTrace();
        }*/
    }

    private ConfigView getViewClass(String path)
    {
        Object view = null;
        try
        {
            if (path.startsWith("/"))
            {
                path = path.substring(1, path.length());
            }
            if (path.length() == 0)
            {
                path = "home";
            }
            Class c = Class.forName("net.jottools.actions.Config" + JOTUtilities.upperFirst(path.toLowerCase()));
            if (c != null)
            {
                view = c.newInstance();
                if (!(view instanceof ConfigView))
                {
                    view = null;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            view = null;
        }
        return (ConfigView) view;
    }

    public void init(Hashtable params)
    {
    }
}
