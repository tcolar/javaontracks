/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.utils.JOTTimezoneUtils;

/**
 * HttpServletRequest impl. (not fully impl. yet)
 * Use JOTRequestParser to parse a socket input into a request.
 * @author thibautc
 */
public class JOTWebRequest implements HttpServletRequest
{

    private static final DateFormat HEADER_FORMAT = new SimpleDateFormat(JOTTimezoneUtils.FORMAT_HEADER);
    private static final Enumeration EMPTY_ENUM = new Vector().elements();
    Socket socket = null;
    private String method = "GET";
    private String protocol = null;
    private String path;
    // requestLine as it came to us
    private String rawRequestLine;
    private String remoteHost = null;
    private int remotePort = -1;
    private String localHost = null;
    private int localPort = -1;
    /** headers (header name -> Vector of values(string)) **/
    private Hashtable headers = new Hashtable();
    // parameters (hash of Vector of strings)
    private Hashtable parameters = new Hashtable();
    // vector of Cookie
    private Vector cookies = new Vector();
    private String rawParams;
    private String sessionId;
    private String requestedSessionId;
    private String contextPath = "";
    private Hashtable attributes = new Hashtable();
    private String encoding = null;

    // TODO: basic authentication ?
    //String user;
    //String password;
    // body if any (ex: multipart)
    byte[] body;
    // local server name / host

    //those, lazy inited
    private String serverName = null;
    private StringBuffer url = new StringBuffer();
    private String scheme = null;
    static final JOTLoggerLocation logger = new JOTLoggerLocation(JOTLogger.CAT_SERVER, JOTWebRequest.class);
    private boolean requestedSIDFromCookies = true;
    private ServletInputStream in = null;
    private BufferedReader reader = null;
    private Vector locales;

    /**
     * should be retrieved through JOTRequestParser
     */
    JOTWebRequest()
    {
    }

    /*************************************************************************
     * HttpServletRequest  Impl. methods
     * @return
     */
    public String getScheme()
    {
        if (scheme != null)
        {
            return scheme;
        }
        //TODO
        scheme = "http";
        return scheme;
    }

    /**
     * 
     * @return
     */
    public StringBuffer getRequestURL()
    {
        if (url != null)
        {
            return url;
        }
        StringBuffer sb = new StringBuffer(getScheme()).append("://").append(getServerName());
        if (getLocalPort() != 80)
        {
            sb.append(":").append(getLocalPort());
        }
        sb.append(path);
        if (rawParams != null && rawParams.length() > 0)
        {
            sb.append("?").append(rawParams);
        }
        url = sb;
        // should we return a copy ?? according to spec probably not
        return url;
    }

    public Hashtable getHeaders()
    {
        return headers;
    }

    public String getRemoteHost()
    {
        return remoteHost;
    }

    public String getMethod()
    {
        return method;
    }

    public Hashtable getParameters()
    {
        return parameters;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public Cookie[] getCookie(String nameetc)
    {
        return (Cookie[]) cookies.toArray(new Cookie[0]);
    }

    protected void addHeader(String key, String value)
    {
        Vector v = new Vector();
        if (headers.containsKey(key))
        {
            v = (Vector) headers.get(key);
        }
        v.add(value);
        headers.put(key, v);
        if (key.equalsIgnoreCase("cookie"))
        {
            parseCookieLine(value);
        }
    }

    public int getLocalPort()
    {
        return localPort;
    }

    public String getServerName()
    {
        // lazy inited
        if (serverName != null)
        {
            return serverName;
        }

        // Return host from absolute URI
        /*serverName=_uri.getHost();
        if (serverName!=null)
        return serverName;
         */
        // Return host from header field
        String host = (String) headers.get("Host");
        if (host != null)
        {
            int column = host.indexOf(":");
            if (column == -1)
            {
                serverName = host;
            } else
            {
                serverName = host.substring(0, column);
                try
                {
                    localPort = new Integer(host.substring(column + 1, host.length())).intValue();
                } catch (Exception e)
                {
                    logger.exception("Malformed Host HTTP Header: " + host, e);
                }
            }
        }
        if (serverName != null)
        {
            return serverName;
        }

        // Try from socket host
        if (socket != null)
        {
            //TODO: use canonial or regular host name ??
            serverName = socket.getLocalAddress().getCanonicalHostName();
            if (serverName != null)
            {
                return serverName;
            }
        }

        // Fallback to local host
        try
        {
            serverName = InetAddress.getLocalHost().getHostAddress();
        } catch (java.net.UnknownHostException e)
        {/*How could that fail ?? */

        }
        return serverName;
    }

    public String getAuthType()
    {
        // TODO: support this ??
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Cookie[] getCookies()
    {
        return (Cookie[]) cookies.toArray(new Cookie[0]);
    }

    public long getDateHeader(String headerName)
    {
        String header = getHeader(headerName);
        if (header == null)
        {
            return -1;
        }
        Date d = null;
        try
        {
            d = HEADER_FORMAT.parse(header);
        } catch (ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
        return d.getTime();
    }

    public String getHeader(String headerName)
    {
        Vector v = (Vector) headers.get(headerName);
        return (v == null || v.size() < 1) ? null : (String) v.get(0);
    }

    public Enumeration getHeaders(String headerName)
    {
        Vector v = (Vector) headers.get(headerName);
        return v == null ? EMPTY_ENUM : v.elements();
    }

    public Enumeration getHeaderNames()
    {
        return headers.keys();
    }

    public int getIntHeader(String headerName)
    {
        String header = getHeader(headerName);
        return header == null ? -1 : new Integer(header).intValue();
    }

    public String getPathInfo()
    {
        // TODO: unsure about impl.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPathTranslated()
    {
        // TODO: unsure about impl.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getQueryString()
    {
        return rawParams;
    }

    public String getRemoteUser()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isUserInRole(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Principal getUserPrincipal()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRequestedSessionId()
    {
        return requestedSessionId;
    }

    public String getRequestURI()
    {
        return path;
    }

    public String getServletPath()
    {
        return path;
    }

    public HttpSession getSession(boolean createIfNew)
    {
        String id = requestedSessionId;
        if (id == null || !isRequestedSessionIdValid())
        {
            id = sessionId;
        }
        JOTWebSession session = null;
        if (createIfNew)
        {
            session = JOTSessionManager.getInstance().getSession(sessionId);
        } else
        {
            JOTSessionManager.getInstance().getOrCreateSession(sessionId);
        }
        sessionId = session.getId();
        return session;
    }

    public HttpSession getSession()
    {
        return getSession(true);
    }

    public boolean isRequestedSessionIdValid()
    {
        return JOTSessionManager.getInstance().getSession(requestedSessionId) != null;
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return requestedSIDFromCookies;
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return !requestedSIDFromCookies;
    }

    /**
     * @deprecated
     * @return
     */
    public boolean isRequestedSessionIdFromUrl()
    {
        return isRequestedSessionIdFromURL();
    }

    public Object getAttribute(String key)
    {
        return attributes.get(key);
    }

    public Enumeration getAttributeNames()
    {
        return attributes.keys();
    }

    public String getCharacterEncoding()
    {
        return encoding;
    }

    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException
    {
        //TODO: check that encoding is valid
        this.encoding = encoding;
    }

    public int getContentLength()
    {
        try
        {
            return getInputStream().available();
        } catch (IOException e)
        {
        }
        return -1;
    }

    public String getContentType()
    {
        //TODO: how to determine this ??
        return null;
    }

    public ServletInputStream getInputStream() throws IOException
    {
        if (reader != null)
        {
            throw new IllegalStateException("Can't use both getInputStream() and getReader()");
        }
        if (in == null)
        {
            in = (ServletInputStream) socket.getInputStream();
        }
        return in;
    }

    public String getParameter(String name)
    {
        Vector v = (Vector) parameters.get(name);
        if (v == null)
        {
            return null;
        }
        return (String) v.get(0);
    }

    public Enumeration getParameterNames()
    {
        return parameters.keys();
    }

    public String[] getParameterValues(String name)
    {
        Vector v = (Vector) parameters.get(name);
        if (v == null)
        {
            return null;
        }
        return (String[]) v.toArray(new String[0]);
    }

    public Map getParameterMap()
    {
        return parameters;
    }

    public int getServerPort()
    {
        return localPort;
    }

    public BufferedReader getReader() throws IOException
    {
        if (in != null)
        {
            throw new IllegalStateException("Can't use both getInputStream() and getReader()");
        }
        if (reader == null)
        {
            if (encoding == null)
            {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } else
            {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));
            }
        }
        return reader;
    }

    public String getRemoteAddr()
    {
        return remoteHost;
    }

    public void setAttribute(String key, Object value)
    {
        if (value == null)
        {
            removeAttribute(key);
        } else
        {
            attributes.put(key, value);
        }
    }

    public void removeAttribute(String key)
    {
        attributes.remove(key);
    }

    public Locale getLocale()
    {
        if (locales == null)
        {
            getLocales();
        }
        return (Locale) locales.get(0);
    }

    public Enumeration getLocales()
    {
        if (locales == null)
        {
            locales = new Vector();
            Enumeration e = getHeaders("Accept-Language");
            while (e.hasMoreElements())
            {
                String s = (String) e.nextElement();
                if (s != null)
                {
                    String[] vals = s.split(",");
                    for (int i = 0; i != vals.length; i++)
                    {
                        Locale locale = getIndividualLocale(vals[i]);
                        if (locale != null)
                        {
                            locales.add(locale);
                        }
                    }
                }
            }
            if (locales.size() == 0)
            {
                // if no valid locale, then return default server local as per spec
                locales.add(Locale.getDefault());
            }
        }
        return locales.elements();
    }

    public boolean isSecure()
    {
        //TODO: how do i know that ??
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RequestDispatcher getRequestDispatcher(String arg0)
    {
        //TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @deprecated
     * @param arg0
     * @return
     */
    public String getRealPath(String paht)
    {
        // need to be call with ServletContext.getRealPath
        return JOTSessionManager.getInstance().getServletContext().getRealPath(path);
    }

    public String getLocalAddr()
    {
        return localHost;
    }

    public String getLocalName()
    {
        return getServerName();
    }

    /**********************************************************
     * Others
     */
    void setRawParameters(String params)
    {
        rawParams = params;
    }

    /**
     *
     * @param id
     * @param fromCookies - if flase : from url
     */
    void setRequestedSessionId(String id, boolean fromCookies)
    {
        requestedSessionId = id;
        requestedSIDFromCookies = fromCookies;
        if (isRequestedSessionIdValid())
        {
            sessionId = id;
        }
    }

    void setSessionId(String id)
    {
        sessionId = id;
    }

    String getSessionId()
    {
        return sessionId;
    }

    void setRawParams(String rawParams)
    {
        this.rawParams = rawParams;
    }

    void setRemoteHost(String host)
    {
        this.remoteHost = host;
    }

    void setMethod(String method)
    {
        this.method = method;
    }

    void setParameters(Hashtable parameters)
    {
        this.parameters = parameters;
    }

    void setPath(String path)
    {
        this.path = path;
    }

    void setRemotePort(int port)
    {
        this.remotePort = port;
    }

    void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    void setRawRequestLine(String rawRequestLine)
    {
        this.rawRequestLine = rawRequestLine;
    }

    public void parseCookieLine(String value)
    {
        //TODO: look at cookie spec some more
        String[] cooks = value.split(";");
        for (int i = 0; i != cooks.length; i++)
        {
            String cookie = cooks[i];
            int index = cookie.indexOf("=");
            if (index != -1)
            {
                String key = cookie.substring(0, index).trim();
                String val = "";
                // Is trim safe in this case ??
                if (cookie.length() > index)
                {
                    val = cookie.substring(index + 1, cookie.length()).trim();
                }
                cookies.add(new Cookie(key, val));
                if (key.equals("JSESSIONID"))
                {
                    setRequestedSessionId(val, true);
                }

            }
        }
    }

    /**
     * Print detailed infos about the request
     * @return
     */
    public String toString()
    {
        String str = "" + getClass().getSimpleName() + " [method:" + method + " proto:" + protocol + " path:" + path + " host:" + remoteHost + " port:" + remotePort + "]";
        str += "\n\tHeaders:";
        Enumeration e = headers.keys();
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            str += "\n\t\t" + key + " -> ";
            Vector v = (Vector) headers.get(key);
            for (int i = 0; i != v.size(); i++)
            {
                str += v.get(i) + " | ";
            }
        }
        str += "\n\tCookies:";
        for (int i = 0; i != cookies.size(); i++)
        {
            Cookie cookie = (Cookie) cookies.get(i);
            str += "\n\t\t" + cookie.getName() + " : " + cookie.getValue();
        }
        str += "\n\tParams:";
        Enumeration e2 = parameters.keys();
        while (e2.hasMoreElements())
        {
            String key = (String) e2.nextElement();
            String val = (String) parameters.get(key);
            str += "\n\t\t" + key + " -> " + val;
        }
        str += "\n";
        return str;
    }

    String getRawRequestLine()
    {
        return rawRequestLine;
    }

    String getLocalHost()
    {
        return localHost;
    }

    void setLocalHost(String localHost)
    {
        //TODO: return servername instead ??
        this.localHost = localHost;
    }

    void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

    void setContextPath(String path)
    {
        contextPath = path;
    }

    private Locale getIndividualLocale(String str)
    {
        //We might have something like en-us;q=0.5

        // remove qvalue stuff
        int i = str.indexOf(';');
        if (i != -1)
        {
            str = str.substring(0, i);
        }
        str = str.trim();

        //We might have something like en-us
        String language = str;
        String country = "";

        // there might be a "-" followed by a countryname
        int countrySeparator = language.indexOf('-');
        Locale locale = null;

        if (countrySeparator != -1)
        {
            country = language.substring(countrySeparator + 1, language.length());
            language = language.substring(0, countrySeparator);
        }
        try
        {
            locale = new Locale(language, country);
        } catch (Exception e)
        {
            //if parsing failed, this will be null ?
        }
        return locale;
    }
}
