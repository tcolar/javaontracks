/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.io.IOException;
import java.io.PrintStream;
import java.util.GregorianCalendar;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jot.JOTInitializer;
import net.jot.logger.JOTLogger;
import net.jot.prefs.JOTPreferences;
import net.jot.web.ctrl.JOTMasterController;

/*
 * Options:
 * TODO: enable monitoring of request time per item
 * monitor: "all" "*.do, *.jsp" etc ...
 * 
 * TODO: - enable monitoring of request per user (cookies or IP)
 * warning level, emails etc ...
 * 
 * TODO: log last X user requests (paths)
 * 
 * TODO: - detect infinite loops 
 * 
 * TODO: - debug object (dump object)
 * TODO: method to find an object size in memory ?
 */

/*
 * Created on Oct 3, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**
 * This is the Main entry point for JOT when used in a webapp
 * This is the main object, that will catch all the requests and process them.
 * This filter MUST be regsitered in web.xml for requests to be processed by JOT
 * @author thibautc
 *
 */
public class JOTMainFilter extends HttpServlet implements Filter
{

    private static final long serialVersionUID = 1020265679050704054L;
    protected FilterConfig filterConfig;
    protected String confPath;
    protected JOTFlowConfig flowConfig;
    protected static String context = null;
    protected boolean sendToContainer = true;
    Exception jotInitError = null;

    /**
     * Initialize the filter, calls the JOT initialization as well.
     * Loads and validate the jot.conf file right away.
     * If this succeeds, JOT should be ready to go.
     * If there is an error, fail right away so the use knows there is a problem right of the bat.
     */
    public void init(FilterConfig filterConfig)
    {
        try
        {
            this.filterConfig = filterConfig;
            // we will pass the jotconf folder path to the prefs
            confPath = filterConfig.getServletContext().getRealPath("/jotconf");
            JOTPreferences.setWebConfPath(confPath);
            // initialize JOT (and the prefs)
            JOTInitializer.getInstance().init();
            // load the flow config ...
            flowConfig = JOTFlowManager.init(confPath);
            if (flowConfig.getTemplateRoots() == null)
            {
                flowConfig.setTemplateRoot(filterConfig.getServletContext().getRealPath(""));
            }
            flowConfig.setConfigPath(confPath);

            flowConfig.runValidation();

            context = filterConfig.getServletContext().getServletContextName();
        } catch (Exception e)
        {
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "*********************************");
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "- Initialization failed! ", e);
            jotInitError = e;
        }
    }

    /**
     * Main request processing method, process a user web request.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        JOTMasterController master = new JOTMasterController();
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
    }

    protected String getURI(ServletRequest request)
    {
        if (request instanceof HttpServletRequest)
        {
            return ((HttpServletRequest) request).getRequestURI();
        } else
        {
            return "Not an HttpServletRequest";
        }
    }

    /**
     * try to cleanup all used resources at shutdown.
     */
    public void destroy()
    {
        filterConfig = null;
        JOTInitializer.getInstance().destroy();
    }

    /**
     * Returns the servlet context name: ie /mywebapp/
     * @return
     */
    public static String getContextName()
    {
        return context;
    }
}
