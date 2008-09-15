/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.server;

import java.net.Socket;
import java.util.Hashtable;

/**
 *
 * @author thibautc
 */
public interface JOTServerRequestHandler 
{
    /**
     * Handle a client GET request
     * Keep in mind this can be called from multiple threads.
     * So be careful / SYNCHRONIZE if needed
     * @param client
     */
    public void handleGetRequest(Socket client, String path, Hashtable Parameters);

}
