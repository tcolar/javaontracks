/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.webapp;

import java.io.File;
import net.jot.logger.JOTLogger;
import net.jot.prefs.JOTPreferences;
import net.jot.web.server.JOTMiniServer;

/**
 *
 * @author thibautc
 */
public class JOTwebappServer
{

    private final static Integer DEFAULT_PORT = new Integer(8033);
    private final static String DEFAULT_LOG_FILE = "server.log";
    private static final String PREFS_FILE="jotserver.properties";

    /**
     * creates/completes a basic server dir structure as needeed
     * @param root
     */
    private static void createServerRootAsNeeded(String root) throws Exception
    {
        File dir = new File(root);
        dir.mkdirs();
        new File(root, "webapps").mkdirs();
        new File(root, "logs").mkdirs();
        File props = new File(root, "jotserver.properties");
        if (!props.exists())
        {
            props.createNewFile();
        }
    }
    private JOTMiniServer server = null;

    public JOTwebappServer(File root, JOTPreferences prefs)
    {
        Integer port=prefs.getDefaultedInt("server.port", DEFAULT_PORT);
        File log=new File(root+File.separator+"logs", DEFAULT_LOG_FILE);
        System.out.println("Will log to console & "+log.getAbsolutePath());
        JOTLogger.init(log.getAbsolutePath(), JOTLogger.ALL_LEVELS, null);
        JOTLogger.setPrintToConcole(true);
        JOTLogger.setPrintStackTrace(true);
        JOTLogger.info(this,"Starting server on port: "+port);
        server = new JOTMiniServer();
        try
        {
            server.start(port.intValue(), JOTWebappRequestProcessor.class, null);
        } catch (Exception e)
        {
            System.err.println("Failed starting server: " + e);
            e.printStackTrace();
            shutdown();
        }
    }

    public void shutdown()
    {
        server.stop();
    }

    /**
     * TODO: option to accept connection only from localhost
     * TODO: provide/use a password ?
     * @param args
     */
    public static void main(String[] args)
    {
        // Testing
        args=new String[1];
        args[0]="/tmp/server/";
        
        JOTwebappServer server=null;
        try
        {
            if (args.length < 1)
            {
                System.out.println("Please provide server root folder as argument.");
                System.exit(-1);
            }
            createServerRootAsNeeded(args[0]);
            JOTPreferences prefs=JOTPreferences.getInstance();
            prefs.loadFrom(new File(args[0],PREFS_FILE));
            server=new JOTwebappServer(new File(args[0]),prefs);
        } catch (Exception e)
        {
            JOTLogger.logException(JOTwebappServer.class, "Failed starting JOT server", e);
            e.printStackTrace();
            if(server!=null)
                server.shutdown();
        }
    }

    public void finalyze() throws Throwable
    {
        shutdown();
        super.finalize();
    }

}
