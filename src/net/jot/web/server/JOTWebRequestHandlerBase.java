/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.net.Socket;

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

    public void handle(Socket socket) throws Exception
    {
        this.socket = socket;
        request = JOTRequestParser.parseRequest(socket);
        response = new JOTWebResponse(socket, request);
        handle();
        // note: that wil cleanup the request too
        response.destroy();
    }

    /**
     * Implement this to handle your requesthandler.
     * Note that you can/should make use of the provided "request" object. 
     */
    public abstract void handle() throws Exception;
}
