/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.net.Socket;
import java.util.Date;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.web.server.impl.JOTStaticServerHandler;

/**
 * Base implementation of a request handler providing a parsed request object
 * giving easy access to request and response
 * TODO: provide a response helper too
 * @author thibautc
 */
public abstract class JOTWebRequestHandlerBase implements JOTServerRequestHandler
{

    private Socket socket;
    public JOTWebRequest request;
    public JOTWebResponse response;
    private static final JOTLoggerLocation logger=new JOTLoggerLocation(JOTLogger.CAT_SERVER,JOTWebRequestHandlerBase.class);

    public void handle(Socket socket) throws Exception
    {
        long startTime=-1;
        if(logger.isDebugEnabled())
        {
            startTime=new Date().getTime();
        }

        this.socket = socket;
        request = JOTRequestParser.parseRequest(socket);
        response = new JOTWebResponse(socket, request);
        handle();
        // note: that wil cleanup the request too
        response.destroy();

        if(logger.isDebugEnabled())
        {
            long time=new Date().getTime()-startTime;
            logger.debug("Handled the request "+request.getServletPath()+" in "+time+" ms");
        }
    }

    /**
     * Implement this to handle your requesthandler.
     * Note that you can/should make use of the provided "request" object. 
     */
    public abstract void handle() throws Exception;
}
