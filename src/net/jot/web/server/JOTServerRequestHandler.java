/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.net.Socket;
import java.util.Hashtable;

/**
 * Handle a request 
 * There will be one inctance of this per request/thread
 * @author thibautc
 */
public interface JOTServerRequestHandler 
{
    /**
     * Handle a client request
     * Each server request thread will be handed a new Instance of this by JOTMiniServ
     * So you should not have to worry about thread safety much in impl.
     * @param client
     */
    public void handle(Socket socket);
    
    /**
     * Provide optional parameters, will be called before handle() 
     * @param params
     */
    public void init(Hashtable params);
}
