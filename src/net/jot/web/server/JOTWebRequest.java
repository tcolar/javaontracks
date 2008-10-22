/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.io.BufferedReader;
import java.io.IOException;
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
 * HttpServletRequest impl.
 * Use JOTRequestParser to parse a socket input into a request.
 * @author thibautc
 */
public class JOTWebRequest implements HttpServletRequest
{
    private static final DateFormat HEADER_FORMAT = new SimpleDateFormat(JOTTimezoneUtils.FORMAT_HEADER);
    private static final Enumeration EMPTY_ENUM=new Vector().elements();

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
    // parameters (hash of strings)
    private Hashtable parameters = new Hashtable();
    // vector of Cookie
    private Vector cookies = new Vector();
    private String rawParams;
    private String sessionId;
    private String requestedSessionId;
    private String contextPath="";

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
        scheme = "http://";
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
        StringBuffer sb = new StringBuffer(getScheme()).append(getServerName());
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
        return (Cookie[])cookies.toArray(new Cookie[0]);
    }

    public long getDateHeader(String headerName)
    {
        String header=getHeader(headerName);
        if(header==null) return -1;
        Date d=null;
        try
        {
            d=HEADER_FORMAT.parse(header);
        }
        catch(ParseException e)
        {
            throw new IllegalArgumentException(e);
        }
        return d.getTime();
    }

    public String getHeader(String headerName)
    {
        Vector v=(Vector)headers.get(headerName);
        return (v==null || v.size()<1)?null:(String)v.get(0);
    }

    public Enumeration getHeaders(String headerName)
    {
        Vector v=(Vector)headers.get(headerName);
        return v==null?EMPTY_ENUM:v.elements();
    }

    public Enumeration getHeaderNames()
    {
        return headers.keys();
    }

    public int getIntHeader(String headerName)
    {
        String header=getHeader(headerName);
        return header==null?-1:new Integer(header).intValue();
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

    public HttpSession getSession(boolean arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public HttpSession getSession()
    {
        return getSession(true);
    }

    public boolean isRequestedSessionIdValid()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRequestedSessionIdFromURL()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isRequestedSessionIdFromUrl()
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

    public String getCharacterEncoding()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getContentLength()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getContentType()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServletInputStream getInputStream() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getParameter(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getParameterNames()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String[] getParameterValues(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map getParameterMap()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getServerPort()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BufferedReader getReader() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRemoteAddr()
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

    public Locale getLocale()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Enumeration getLocales()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSecure()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RequestDispatcher getRequestDispatcher(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getRealPath(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLocalName()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLocalAddr()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**********************************************************
     * Others
     */
    void setRawParameters(String params)
    {
        rawParams = params;
    }

    void setRequestedSessionId(String id)
    {
        requestedSessionId = id;
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
        contextPath=path;
    }


}
