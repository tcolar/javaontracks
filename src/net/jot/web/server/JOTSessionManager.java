/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.math.BigInteger;
import java.util.Date;
import java.util.Hashtable;
import java.security.SecureRandom;
import javax.servlet.ServletContext;

/**
 * Manage Web sessions
 * @author thibautc
 */
public class JOTSessionManager
{

    private static final JOTSessionManager instance = new JOTSessionManager();
    // seeding with now time and avail memory value combo ~ random seed
    //private static final String seed=""+Runtime.getRuntime().freeMemory()+""+new Date().getTime();
    // self seeding should be safer
    private static final SecureRandom random = new SecureRandom();
    private static int uniq = 1;
    private static final JOTServletContext context=new JOTServletContext();

    /**
     * TODO: synchronized, safe faster to use hashmap instead ? -> research later
     * 
     * TODO: when is it safe to automatically remove an expired session (not still used)?? 
     */
    private Hashtable sessions = new Hashtable();

    public static JOTSessionManager getInstance()
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
        JOTWebSession session = getSession(sessionId);
        if (session == null)
        {
            session = createSession();
        }
        return session;
    }

    /**
     * returns null if does not exists or invalid / expired
     * @param sessionId
     * @return
     */
    public JOTWebSession getSession(String sessionId)
    {
        JOTWebSession session = (JOTWebSession) sessions.get(sessionId);
        if (session != null && (isExpired(sessionId) || session.isInvalidated()))
        {
            session = null;
        }
        if (session != null)
        {
            session.setLastAccessTime(new Date().getTime());
            /** TODO: this is probably not the right way to set isNew
             * If the user calls craete and then get again it would set to new where it should noit
             * Probably OK though
             * */
            session.setNew(false);
        }
        return session;
    }

    /**
     * Create a new session witha  new unique ID
     * @return
     */
    public JOTWebSession createSession()
    {
        String sessionId = getUniqueSessionID();
        JOTWebSession session = new JOTWebSession(sessionId);
        return session;
    }

    private synchronized String getUniqueSessionID()
    {
        String sessionId = null;
        do
        {
            byte[] id = new byte[32];
            random.nextBytes(id);
            id[31] = (byte) uniq;
            id[30] = (byte) (uniq << 8);
            BigInteger bi = new BigInteger(id);
            sessionId = bi.toString(16);
        // still check for collision just in case.
        } while (sessions.containsKey(sessionId));
        // not used as a unique identifier, just to avoid random number collisions
        uniq++;
        if (uniq < 0)
        {
            uniq = 0;
        }
        return sessionId;
    }

    public boolean isExpired(String sessionId)
    {
        JOTWebSession session = (JOTWebSession) sessions.get(sessionId);
        boolean expired = false;
        if (session != null)
        {
            expired = new Date().getTime() - session.getLastAccessedTime() > session.getMaxInactiveInterval() * 1000;
        }
        return expired;
    }

    ServletContext getServletContext()
    {
        throw( new UnsupportedOperationException("Not supported yet."));
    }
}
