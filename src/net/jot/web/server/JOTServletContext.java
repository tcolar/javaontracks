/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * TBD --- maybe 
 * @author thibautc
 */
public class JOTServletContext implements ServletContext
{

    public ServletContext getContext(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getContextPath()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMajorVersion()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMinorVersion()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getMimeType(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set getResourcePaths(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getResource(String arg0) throws MalformedURLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InputStream getResourceAsStream(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RequestDispatcher getRequestDispatcher(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RequestDispatcher getNamedDispatcher(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Servlet getServlet(String arg0) throws ServletException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getServlets()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getServletNames()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void log(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void log(Exception arg0, String arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void log(String arg0, Throwable arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRealPath(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getServerInfo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getInitParameter(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getInitParameterNames()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getAttribute(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getAttributeNames()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttribute(String arg0, Object arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAttribute(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getServletContextName()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
