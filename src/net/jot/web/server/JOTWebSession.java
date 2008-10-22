/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 *
 * @author thibautc
 */
public class JOTWebSession implements HttpSession{

    public long getCreationTime()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getId()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLastAccessedTime()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServletContext getServletContext()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMaxInactiveInterval(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxInactiveInterval()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HttpSessionContext getSessionContext()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getAttribute(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getValue(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getAttributeNames()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getValueNames()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAttribute(String arg0, Object arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void putValue(String arg0, Object arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAttribute(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeValue(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void invalidate()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isNew()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
