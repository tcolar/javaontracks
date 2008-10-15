/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server.impl;

import java.util.Hashtable;
import net.jot.web.server.JOTWebRequestHandlerBase;

/**
 *
 * @author tcolar
 */
public class JOTStaticServerHandler extends JOTWebRequestHandlerBase{
    private Object rootFolder;

    public void handle() throws Exception
    {
        if(request.getMethod().equalsIgnoreCase("GET"))
        {
            System.out.println(request);
            response.sendError(response.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "What kind of an error is that !");
        }
    }

    public void init(Hashtable params)
    {
        rootFolder=params.get(JOTStaticWebServer.ROOT_FOLDER);
    }

}
