/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mini generic web server (minimal feature set)
 * It doesn't support much of anyhting beside get requests with parameters
 * no cookies,sessions, etc... at this point
 * @author thibautc
 */
public class JOTMiniWebServer
{
    // Get, followed by some spaces, followed by a path, possibly followed by a ?, followed by parameters/values 
    private static final Pattern RequestPattern=Pattern.compile("^GET\\s+([^\\?]*)\\??(\\S*)");
    
    private Vector threads = new Vector();
    boolean stop = false;
    ServerSocket socket = null;

    public JOTMiniWebServer()
    {
    }

    /**
     * Start the server on the given port
     * RequestHandler implementaion to handle teh requests
     * @param port
     * @param handler
     * @throws java.lang.Exception
     */
    public void start(int port, JOTServerRequestHandler handler) throws Exception
    {
        socket = new ServerSocket(port);
        while (socket != null && !stop)
        {
            Socket client = socket.accept();
            System.out.println("New Connection from: " + client.getRemoteSocketAddress());
            RequestThread c = new RequestThread(client, handler);
            threads.add(c);
            c.run();
        }

    }

    class RequestThread implements Runnable
    {

        private Socket socket;
        private JOTServerRequestHandler handler;

        public RequestThread(Socket socket, JOTServerRequestHandler handler)
        {
            this.socket = socket;
            this.handler = handler;
        }

        public void run()
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = "";
                while (reader.ready() && line != null)
                {
                    line = reader.readLine();
                    Matcher m=RequestPattern.matcher(line);
                    if (m.matches())
                    {
                        handleGet(line);
                    }
                }

                reader.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    socket.close();
                } catch (Exception e2)
                {
                }
                threads.remove(this);
            }
        }

        private void handleGet(String line)
        {
            //TODO: parse the GET request
            handler.handleGetRequest(socket, line, null);
            
            
        }

        public void stop()
        {
            try
            {
                if (socket != null)
                {
                    socket.getInputStream().close();
                    socket.close();
                }
                super.finalize();
            } catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
    }

    public void finalyze() throws Throwable
    {
        stop = true;
        for (int i = 0; i != threads.size(); i++)
        {
            RequestThread c = (RequestThread) threads.get(i);
            if (c != null)
            {
                c.stop();
            }
        }
        super.finalize();
    }
}
