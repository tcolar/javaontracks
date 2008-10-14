/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Mini generic web server (regular- not j2ee)
 * Use together with JOTWebRequestHandlerBase for easy request processing
 * 
 * Ex: server=new JOTMiniWebServer();
 * server.start(8033,MYJOTConfiguratorHandler.class);
 * @author thibautc
 */
public class JOTMiniWebServer
{
    // Vector is synchronized.
    private Vector threads = new Vector();
    volatile boolean stop = false;
    ServerSocket socket = null;

    public JOTMiniWebServer()
    {
    }

    /**
     * Start the server on the given port
     * RequestHandler is the implem. to handle the requests
     * for simplicity, just provide a subclass impl. of JOTWebRequestHandlerBase
     * @param port
     * @param handler
     * @throws java.lang.Exception
     */
    public void start(int port, Class jOTServerRequestHandlerImplClass) throws Exception
    {
        socket = new ServerSocket(port);
        while (socket != null && !stop)
        {
            Socket client = socket.accept();
            System.out.println("New Connection from: " + client.getRemoteSocketAddress());
            JOTServerRequestHandler handler=(JOTServerRequestHandler)jOTServerRequestHandlerImplClass.newInstance();
            RequestThread c = new RequestThread(client, handler);
            threads.add(c);
            c.run();
        }

    }

    public void finalyze() throws Throwable
    {
        stop();
        super.finalize();
    }

    public void stop()
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
                handler.handle(socket);
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
