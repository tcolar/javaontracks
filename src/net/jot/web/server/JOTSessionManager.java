/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.math.BigInteger;
import java.util.Date;
import java.util.Hashtable;
import java.security.SecureRandom;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import net.jot.logger.JOTLogger;

/**
 * Manage Web sessions
 * TODO: session cleanup thread
 * @author thibautc
 */
public class JOTSessionManager
{

    private static final JOTSessionManager instance = new JOTSessionManager();
    private final SecureRandom random = new SecureRandom();
    private int uniq = 1;
    private final JOTServletContext context = new JOTServletContext();
    private JOTSessionManagerThread thread = new JOTSessionManagerThread();
    // cleanup every 5mn
    private final static long CLEANUP_INTERVAL = 1000 * 300;
    /**
     * TODO: synchronized, faster to use hashmap instead(safe?) ? -> research later
     */
    private Hashtable sessions = new Hashtable();
    private boolean inUse=false;
    
    public static JOTSessionManager getInstance()
    {
        if(!instance.inUse && !instance.thread.isAlive())
        {
            instance.thread.start();
        }
        instance.inUse=true;
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
        throw (new UnsupportedOperationException("Not supported yet."));
    }

    public void finalize() throws Throwable
    {
        shutdown();
        super.finalize();
    }

    public static void shutdown()
    {
        if (instance != null)
        {
            getInstance().thread.shutdown();
        }
    }

    /**
     * session cleanup thread
     */
    private class JOTSessionManagerThread extends Thread implements Runnable
    {

        volatile boolean stop = false;

        public JOTSessionManagerThread()
        {
        }

        public void run()
        {
            while (!stop)
            {
                try
                {
                    sleep(CLEANUP_INTERVAL);
                    if (inUse)
                    {
                        //if nobody is using this, no point wasting time.
                        long start = new Date().getTime();
                        int startSize = sessions.size();
                        cleanup();
                        long end = new Date().getTime();
                        int endSize = sessions.size();
                        if (JOTLogger.isDebugEnabled())
                        {
                            JOTLogger.debug(this, "Session cleanup from:" + startSize + " to:" + endSize + " took:" + (end - start) + "ms.");
                        }
                    }
                } catch (InterruptedException ie)
                {
                    // stop was set.
                }
            }
        }

        private void cleanup()
        {
            Enumeration e = sessions.keys();
            while (e.hasMoreElements())
            {
                String key = (String) e.nextElement();
                JOTWebSession session = (JOTWebSession) sessions.get(key);
                if (session.isInvalidated() || session.isExpired())
                {
                    sessions.remove(key);
                    session = null;
                }
            }
        }

        private void shutdown()
        {
            stop = true;
            // stop sleeping.
            interrupt();
        }
    }
}
