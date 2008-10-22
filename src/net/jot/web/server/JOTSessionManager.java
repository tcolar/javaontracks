/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.util.Date;
import java.util.Hashtable;
import java.security.SecureRandom;

/**
 *
 * @author thibautc
 */
public class JOTSessionManager {

    private static final JOTSessionManager instance=new JOTSessionManager();
    // seeding with now time and avail memory value combo ~ random seed
    //private static final String seed=""+Runtime.getRuntime().freeMemory()+""+new Date().getTime();
    // self seeding should be safer
    private static final SecureRandom random=new SecureRandom();
    /**
     * TODO: synchronized, safe faster to use hashmap instead ? -> research later
     * 
     * TODO: when is it safe to automatically remove an expired session (not still used)?? 
     */
    private Hashtable sessions=new Hashtable();
    
    public JOTSessionManager getInstance()
    {
        return instance;
    }
    
    /**
     * if session does not exist yet, then create one (with a new sessionId, not the one passed)
     * @param sessionId
     * @return
     */
    public JOTWebSession getOrCreateSession(String sessionId)
    {
        JOTWebSession session=getSession(sessionId);
        if(session==null)
        {
            session=createSession();
        }
        return session;
    }
    /**
     * returns nullif does not exists
     * @param sessionId
     * @return
     */
    public JOTWebSession getSession(String sessionId)
    {
        JOTWebSession session=(JOTWebSession)sessions.get(sessionId);
        if(session!=null && isExpired(sessionId))
        {
            session.invalidate();
            session=null;
            sessions.remove(sessionId);
        }
        return session;   
    }

    /**
     * Create a new session witha  new unique ID
     * @return
     */
    public JOTWebSession createSession()
    {
        byte[] id=new byte[32];
        random.nextBytes(id);
        String sessionId=new String(id);
        // TODO: create a JOTWebSessionObject
        // TODO: add it to sessions.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean isExpired(String sessionId)
    {
        JOTWebSession session=(JOTWebSession)sessions.get(sessionId);
        boolean expired=false;
        if(session!=null)
        {
            expired=new Date().getTime()-session.getLastAccessedTime()>session.getMaxInactiveInterval();
        }
        return expired;
    }
}
