/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.util.Hashtable;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;

/**
 *
 * @author tcolar
 */
public class JOTStaticServerHandler extends JOTWebRequestHandlerBase{
    private Object rootFolder;
    private static final JOTLoggerLocation logger=new JOTLoggerLocation(JOTLogger.CAT_SERVER,JOTStaticServerHandler.class);

    public void handle() throws Exception
    {
        if(logger.isDebugEnabled())
            logger.debug("Received Request:  "+request.getRemoteHost()+" "+request.getRawRequestLine());
        if(logger.isTraceEnabled())
            logger.trace("Received Request:  "+request.toString());
        
        if(request.getMethod().equalsIgnoreCase("GET"))
        {
            //response.sendError(response.SC_REQUESTED_RANGE_NOT_SATISFIABLE, "What kind of an error is that !");
            response.sendRedirect("http://www.gogle.com/");
        }
    }

    public void init(Hashtable params)
    {
        rootFolder=params.get(JOTStaticWebServer.ROOT_FOLDER);
    }

}