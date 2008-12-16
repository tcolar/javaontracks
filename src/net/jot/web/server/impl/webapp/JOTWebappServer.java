/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server.impl.webapp;

import java.io.File;
import java.util.Hashtable;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.prefs.JOTPreferences;
import net.jot.web.server.impl.JOTMiniServer;

/**
 *
 * @author thibautc
 */
public class JOTWebappServer
{

    private final static Integer DEFAULT_PORT = new Integer(8033);
    private final static String DEFAULT_LOG_FILE = "server.log";
    private static final String PREFS_FILE = "jotserver.properties";
    JOTLoggerLocation logger = new JOTLoggerLocation(JOTLogger.CAT_SERVER, getClass());
    private File serverRoot = null;

    /**
     * creates/completes a basic server dir structure as needeed
     * @param root
     */
    private void createServerRootAsNeeded(File root) throws Exception
    {
        if (!root.exists())
        {
            root.mkdirs();
            new File(root, "webapps").mkdirs();
            new File(root, "logs").mkdirs();
            File props = new File(root, "jotserver.properties");
            if (!props.exists())
            {
                props.createNewFile();
            }
        }
        serverRoot = root;
    }
    private JOTMiniServer server = null;

    public JOTWebappServer(File root, JOTPreferences prefs)
    {
        try
        {
            createServerRootAsNeeded(root);
        } catch (Exception e)
        {
            System.err.println("Failed starting server: " + e);
            logger.exception("Failed creating app server root: "+root.getAbsolutePath(), e);
            throw new RuntimeException("Failed starting server: " + e);
        }

        Integer port = prefs.getDefaultedInt("server.port", DEFAULT_PORT);
        boolean autoReload = prefs.getDefaultedBoolean("server.webapps.autoreload", Boolean.TRUE).booleanValue();
        File log = new File(root + File.separator + "logs", DEFAULT_LOG_FILE);
        System.out.println("Will log to console & " + log.getAbsolutePath());
        JOTLogger.init(log.getAbsolutePath(), JOTLogger.ALL_LEVELS, null);
        JOTLogger.setPrintToConcole(true);
        JOTLogger.setPrintStackTrace(true);
        JOTLogger.info(this, "Starting server on port: " + port);
        Hashtable params = new Hashtable();

        server = new JOTMiniServer();
        
        try
        {
            server.start(port.intValue(), JOTWebappRequestProcessor.class, params);
        } catch (Exception e)
        {
            logger.exception("Failed creating app server root: "+root.getAbsolutePath(), e);
            shutdown();
            throw new RuntimeException("Failed starting server: " + e);
        }
        startWebapps(autoReload);
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
        /*File f = new File()"/tmp/server/");

        JOTWebappServer server = new JOTWebappServer(f, prefs);
        try
        {
            if (args.length < 1)
            {
                System.out.println("Please provide server root folder as argument.");
                System.exit(-1);
            }
            createServerRootAsNeeded(args[0]);
            JOTPreferences prefs = JOTPreferences.getInstance();
            prefs.loadFrom(new File(args[0], PREFS_FILE));
            server = new JOTWebappServer(new File(args[0]), prefs);
        } catch (Exception e)
        {
            JOTLogger.logException(JOTWebappServer.class, "Failed starting JOT server", e);
            e.printStackTrace();
            if (server != null)
            {
                server.shutdown();
            }
        }*/
    }

    public void finalize() throws Throwable
    {
        shutdown();
        super.finalize();
    }

    private void startWebapps(boolean autoReload)
    {
        try
        {

        } catch (Exception e)
        {
            logger.exception("Failed starting webapp ", e);
        }
    }

    /*
     *         JOTMasterController master = new JOTMasterController();
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        try
        {
            if (jotInitError != null)
            {
                response.setContentType("text/html");
                response.getOutputStream().println("<b>" + jotInitError.getMessage() + "</b><br><pre>");
                jotInitError.printStackTrace(new PrintStream(response.getOutputStream()));
                response.getOutputStream().println("</pre>");
                response.flushBuffer();
                return;
            }


            boolean isFlowRequest = (req.getServletPath().endsWith(flowConfig.getFlowExtension()));

            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Request: " + ((HttpServletRequest) request).getServletPath());

            String realPath = filterConfig.getServletContext().getRealPath(req.getServletPath());

            if (realPath.toLowerCase().startsWith(flowConfig.getConfigPath().toLowerCase()))
            {
                master.renderForbidden(res);
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "User: " + request.getRemoteAddr() + " tried to access: " + realPath + " Blocked (Forbidden)");
                return;
            }

            if (isFlowRequest)
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Starting flow request: " + ((HttpServletRequest) request).getServletPath());
                long startTime = new GregorianCalendar().getTime().getTime();
                master.setRequest(request);
                master.setResponse(response);
                master.setFilterConfig(filterConfig);
                master.setFilterChain(filterChain);
                master.setFlowConfig(flowConfig);
                master.process();
                long endTime = new GregorianCalendar().getTime().getTime();
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Completed flow request: " + ((HttpServletRequest) request).getServletPath() + " took:" + (endTime - startTime) + " ms");
            }
        } catch (Throwable t)
        {
            master.renderError(res, t);
        }

        if (!response.isCommitted() && sendToContainer)
        {
            // sends to the servlet container
            filterChain.doFilter(request, response);
        }
*/
}
