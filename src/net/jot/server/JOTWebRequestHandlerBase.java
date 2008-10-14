/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.server;

import java.net.Socket;

/**
 * Base implementation of a request handler providing a parsed request object
 * giving easy access to parsed cookies, parameters, headers etc...
 * TODO: provide a response helper too
 * @author thibautc
 */
public abstract class JOTWebRequestHandlerBase implements JOTServerRequestHandler
{

    protected Socket socket;
    public JOTWebRequest request;

    public void handle(Socket socket)
    {
        this.socket = socket;
        try
        {
            request = JOTRequestParser.parseRequest(socket);
            handle();
        } catch (Exception e)
        {
        }
    }

    /**
     * Implement this to handle your requesthandler.
     * Note that you can/should make use of the provided "request" object. 
     */
    public abstract void handle();
}
