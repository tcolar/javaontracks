/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mini generic web server (minimal feature set)
 * It doesn't support much of anything beside get requests with parameters
 * no cookies,sessions, etc... at this point
 * @author thibautc
 */
public class JOTMiniWebServer
{
    // Get, followed by some spaces, followed by a path, possibly followed by a ?, followed by parameters/values 
    private static final Pattern RequestPattern = Pattern.compile("^GET\\s+([^\\? ]*)\\??(\\S*).*");
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

    private Hashtable parseParameters(String parameters) throws Exception
    {
        Hashtable hash = new Hashtable();
        String[] params=parameters.split("&");
        for(int i=0;i!=params.length;i++)
        {
            String p=params[i];
            int index=p.indexOf("=");
            if(index!=-1)
            {
                hash.put(URLDecoder.decode(p.substring(0,index),"UTF-8"),URLDecoder.decode(p.substring(index+1,p.length()),"UTF-8"));
            }
            else
            {
                hash.put(URLDecoder.decode(p,"UTF-8"),"");                
            }
        }
        return hash;
    }

    
    
    /**
     * Individual request thread
     */
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
                    Matcher m = RequestPattern.matcher(line);
                    if (m.matches())
                    {
                        String path = URLDecoder.decode(m.group(1), "UTF-8");
                        Hashtable params = new Hashtable();
                        if (m.groupCount() > 1)
                        {
                            params = parseParameters(m.group(2));
                        }
                        handler.handleGetRequest(socket, path, params);
                    }
                }

                reader.close();
                if(!socket.isClosed())
                {
                    try
                    {
                        socket.getOutputStream().close();
                    }
                    catch(Exception e2){/*already closed .. big deal*/}
                }
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

        public void stop()
        {
            try
            {
                if (socket != null && !socket.isClosed())
                {
                    socket.getInputStream().close();
                    socket.close();
                }
                super.finalize();
            } catch (Throwable t)
            {
                // not important
            }
        }
    }

}
