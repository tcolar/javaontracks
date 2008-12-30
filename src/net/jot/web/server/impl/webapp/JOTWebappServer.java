/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server.impl.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.prefs.JOTPreferences;
import net.jot.utils.JOTUtilities;
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
    private static JOTLoggerLocation logger = new JOTLoggerLocation(JOTLogger.CAT_SERVER, JOTWebappServer.class);
    private File serverRoot = null;
    private File webappFolder = null;
    // the common classloader, parent of webapploaders
    private JOTCommonClassLoader commonLoader;
    private Hashtable webapps = new Hashtable();

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
        webappFolder=new File(root, "webapps");
    }
    private JOTMiniServer server = null;

    public JOTWebappServer(File root, JOTPreferences prefs)
    {
        logger.info("Starting app server");
        try
        {
            createServerRootAsNeeded(root);
        } catch (Exception e)
        {
            System.err.println("Failed starting server: " + e);
            logger.exception("Failed creating app server root: " + root.getAbsolutePath(), e);
            throw new RuntimeException("Failed starting server: " + e);
        }

        initCommonLoader(root);

        Integer port = prefs.getDefaultedInt("server.port", DEFAULT_PORT);
        boolean autoReload = prefs.getDefaultedBoolean("server.webapps.autoreload", Boolean.TRUE).booleanValue();
        File log = new File(root + File.separator + "logs", DEFAULT_LOG_FILE);
        System.out.println("Will log to console & " + log.getAbsolutePath());
        //JOTLogger.init(log.getAbsolutePath(), JOTLogger.ALL_LEVELS, null);
        JOTLogger.initIfNecessary(log.getAbsolutePath(), JOTLogger.ALL_LEVELS, null);
        JOTLogger.setPrintToConcole(true);
        JOTLogger.setPrintStackTrace(true);
        JOTLogger.info(this, "Starting server on port: " + port);
        Hashtable params = new Hashtable();

        server = new JOTMiniServer();

        startWebapps(autoReload);
        
        try
        {
            logger.info("Server is running and ready to accept requests");
            server.start(port.intValue(), JOTWebappRequestProcessor.class, params);
        } catch (Exception e)
        {
            logger.exception("Failed starting app server", e);
            shutdown();
            throw new RuntimeException("Failed starting server: " + e);
        }
    }

    public void shutdown()
    {
        server.stop();
    }

    public void finalize() throws Throwable
    {
        shutdown();
        super.finalize();
    }

    /**
     * return vector of jar files.
     * @param libs
     * @return
     */
    protected static Vector findAllJars(File libs)
    {
        Vector results = new Vector();
        File[] files = libs.listFiles();
        for (int i = 0; i != files.length; i++)
        {
            File f = files[i];
            // for directory, recurse in
            if (f.isDirectory())
            {
                results.add(findAllJars(f));
            }
            if (f.isFile())
            {
                if (f.getName().toLowerCase().endsWith(".jar"))
                {
                    results.add(f);
                }
            }
        }
        return results;
    }

    /**
     * Initializes the common loader
     * @param root
     */
    private void initCommonLoader(File root)
    {
        Vector paths = new Vector();
        File classes = new File(root, "common" + File.separator + "classes");
        if (classes.exists() && classes.isDirectory())
        {
            try
            {
                logger.info("Adding " + classes.getAbsolutePath() + " to common classpath");
                paths.add(new File(root, "common" + File.separator + "classes" + File.separator).toURL());
            } catch (Exception e)
            {
                logger.exception("Malformed Classpath URL: " + classes.getAbsolutePath(), e);
            }
        }
        File libs = new File(root, "common" + File.separator + "lib");
        if (libs.exists() && libs.isDirectory())
        {
            Vector jars = findAllJars(libs);
            for (int i = 0; i != jars.size(); i++)
            {
                File jar = (File) jars.get(i);
                try
                {
                    logger.info("Adding " + jar.getAbsolutePath() + " to common classpath");
                    paths.add(jar.toURL());
                } catch (Exception e)
                {
                    logger.exception("Malformed Classpath URL: " + classes.getAbsolutePath(), e);
                }
            }
        }
        URL[] urls = (URL[]) paths.toArray(new URL[0]);
        // init loader
        commonLoader = new JOTCommonClassLoader(urls);
    }

    private void startWebapps(boolean autoReload)
    {
        logger.info("Starting webapps");
        try
        {
            File[] files = webappFolder.listFiles();
            for (int i = 0; i != files.length; i++)
            {
                File f = files[i];
                if (f.isFile() && f.getName().toLowerCase().endsWith(".war"))
                {
                    logger.info("Extracting web archive: " + f.getAbsolutePath());
                    File folder = new File(f.getParentFile(), f.getName());
                    folder.mkdirs();
                    JOTUtilities.deleteFolder(folder);
                    try
                    {
                        JOTUtilities.unzip(f.getAbsolutePath(), folder.getAbsolutePath(), null);
                    } catch (Exception e)
                    {
                        logger.exception("Extraction of war failed! :" + f.getAbsolutePath(), e);
                    }
                    f = folder;
                }
                if (f.isDirectory())
                {
                    String context = f.getName();
                    logger.info("Starting loading of webapp with context: /" + context + " at:" + f.getAbsolutePath());
                    JOTWebappHolder webapp = new JOTWebappHolder(f, context, commonLoader);
                    logger.info("Finished loading of webapp with context: /" + context);
                    webapps.put(context, webapp);
                }
            }
        } catch (Exception e)
        {
            logger.exception("Failed starting webapp ", e);
        }
        logger.info("Done starting webapps");
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
    /**
     * TODO: option to accept connection only from localhost
     * TODO: provide/use a password ?
     * @param args
     */
    public static void main(String[] args)
    {
        File f = new File("/tmp/server/");
        JOTPreferences prefs = JOTPreferences.getInstance();
        try
        {
            prefs.loadFrom(new File(f, PREFS_FILE));
        } catch (IOException ex)
        {
            logger.error("No prefs file found at: "+f+File.separator+PREFS_FILE+" will use default settings.");
        }

        JOTWebappServer serv = new JOTWebappServer(f, prefs);
    }
}
