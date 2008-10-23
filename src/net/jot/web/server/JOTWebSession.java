/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

/**
 * Represent a web session
 * @author thibautc
 */
public class JOTWebSession implements HttpSession
{
    private String id;
    private long creationTime;
    private long lastAccessTime;
    /** 30 mn default*/
    private int maxInactiveInterval=30*60;
    boolean invalidated=true;
    private Hashtable attributes=new Hashtable();
    private boolean isNew=true;
    
    /**
     * use JOTWEbSession(uniqueId) instead
     */
    private JOTWebSession(){}
    /**
     * Instead, retrieve a session using request.getSession() (JOTSessionManager)
     * @param id
     */
    JOTWebSession(String uniqueId)
    {
        this.id=uniqueId;
        creationTime=new Date().getTime();
        lastAccessTime=creationTime;
        invalidated=false;
    }

    /**
     * return creation time as long (ms since jan 1 1970)
     * @see HttpSession
     */
    public long getCreationTime()
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        return creationTime;
    }

    /**
     * return the session ID
     * @see HttpSession
     */
    public String getId()
    {
        return id;
    }

    /**
     * return the last time the session was accessed
     * @see HttpSession
     */
    public long getLastAccessedTime()
    {
        return lastAccessTime;
    }

    /**
     * @see HttpSession
     * @return
     */
    public ServletContext getServletContext()
    {
        return JOTSessionManager.getInstance().getServletContext();
    }

    /**
     * Set the session expiration time (in Seconds) of inactivity
     * @param intervalInSec
     * @see HttpSession
     */
    public void setMaxInactiveInterval(int intervalInSec)
    {
        maxInactiveInterval=intervalInSec;
    }

    /**
     * Get the session expiration time (in Seconds) of inactivity
     * @param intervalInSec
     * @see HttpSession
     */
    public int getMaxInactiveInterval()
    {
        return maxInactiveInterval;
    }

    /**
     * Unsafe and deprecated .. not implemented
     * @see HttpSession
     * @deprecated 
     * @return
     */
    public HttpSessionContext getSessionContext()
    {
        throw new UnsupportedOperationException("This is not supported - Deprecated anyhow.");
    }

    /**
     * return a session attribute
     * @see HttpSession
     * @param name
     * @return
     */
    public Object getAttribute(String name)
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        return attributes.get(name);
    }

    /**
     * @deprecated 
     * @param name
     * @see HttpSession
     * @return
     */
    public Object getValue(String name)
    {
        return getAttribute(name);
    }

    /**
     * @see HttpSession
     * @return
     */
    public Enumeration getAttributeNames()
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        return attributes.keys();
    }

    /**
     * @see HttpSession
     * @return
     */
    public String[] getValueNames()
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        return (String[])attributes.keySet().toArray(new String[0]);
    }

    /**
     * @see HttpSession
     * @return
     */
    public void setAttribute(String key, Object value)
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        attributes.put(key, value);
        if(value instanceof HttpSessionBindingListener)
        {
            HttpSessionBindingEvent evt=new HttpSessionBindingEvent(this, key, value);
            ((HttpSessionBindingListener)value).valueBound(evt);
        }
    }

    /**
     * @see HttpSession
     * @return
     */
    public void putValue(String key, Object value)
    {
        setAttribute(key, value);
    }

    /**
     * @see HttpSession
     * @return
     */
    public void removeAttribute(String key)
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        Object value=attributes.get(key);
        if(value!=null && value instanceof HttpSessionBindingListener)
        {
            HttpSessionBindingEvent evt=new HttpSessionBindingEvent(this, key, value);
            ((HttpSessionBindingListener)value).valueUnbound(evt);
            // helps GC
            value=null;
        }
        attributes.remove(key);
    }

    /**
     * @see HttpSession
     * @return
     */
    public void removeValue(String key)
    {
        removeAttribute(key);
    }

    /**
     * @see HttpSession
     * @return
     */
    public void invalidate()
    {
        if(invalidated)
            throw new IllegalStateException("Session was already invalidated.");
        Enumeration e=getAttributeNames();
        while(e.hasMoreElements())
        {
            removeAttribute((String)e.nextElement());
        }
        //help GC
        attributes=null;
        
        invalidated=true;
    }

    /**
     *
     * @see HttpSession
     * When the server creates a session in response to the first request,
     * the session is in a new state. So, HttpSession.isNew() will return true.
     * On the next request, which must include the session ID received from the server (cookies or rewritten url)
     * the server will associate the request with the session.
     * The client will then have joined the session, meaning that the session is no longer
     * in the new state. So, HttpSession.isNew() will return false.
     * @return
     */
    public boolean isNew()
    {
        if(invalidated)
            throw new IllegalStateException("Session was invalidated.");
        return isNew;
    }

    boolean isInvalidated()
    {
        return invalidated;
    }

    void setLastAccessTime(long time)
    {
        lastAccessTime=time;
    }

    void setNew(boolean value)
    {
        isNew=value;
    }
}
