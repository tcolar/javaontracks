/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.servlet.http.Cookie;

/**
 * Simple web request (non-j2ee) support
 * provide acess to parameters, cookies, etc...
 * @author thibautc
 */
public class JOTWebRequest {
    Socket socket=null;
    private String method="GET";
    private String protocol=null;
    private String path;
    // requestLine as it came to us
    private String rawRequestLine;
    private String remoteHost=null;
    private int remotePort=-1;
    private String localHost=null;
    private int localPort=-1;
    /** headers (header name -> Vector of values(string)) **/
    private Hashtable headers=new Hashtable();
    // parameters (hash of strings)
    private Hashtable parameters=new Hashtable();
    // vector of Cookie
    private Vector cookies=new Vector();
    private String rawParams;

    public String getRawParameters()
    {
        return rawParams;
    }

    public void setRawParams(String rawParams)
    {
        this.rawParams = rawParams;
    }
    // TODO: basic authentication ?
    //String user;
    //String password;
    
    // body if any (ex: multipart)
    byte[] body;
    // local server name / host

    //those, lazy inited
    private String serverName=null;
    private String url=null;
    private String scheme=null;

    /**
     * should be retrieved through JOTRequestParser
     */
    JOTWebRequest()
    {
    }
    
    public Cookie[] getCookie(String nameetc)
    {
        return (Cookie[])cookies.toArray(new Cookie[0]);
    }
    
    protected void addHeader(String key, String value)
    {
        Vector v=new Vector();
        if(headers.containsKey(key))
        {
            v=(Vector)headers.get(key);
        }
        v.add(value);
        headers.put(key,v);
        if(key.equalsIgnoreCase("cookie"))
        {
            parseCookieLine(value);
        }
    }

    public void setRemoteHost(String host)
    {
        this.remoteHost = host;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public void setParameters(Hashtable parameters)
    {
        this.parameters = parameters;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setRemotePort(int port)
    {
        this.remotePort = port;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public void setRawRequestLine(String rawRequestLine)
    {
        this.rawRequestLine = rawRequestLine;
    }

    public void parseCookieLine(String value)
    {
        //TODO: look at cookie spec some more
        String[] cooks=value.split(";");
        for(int i=0;i!=cooks.length;i++)
        {
            String cookie=cooks[i];
            int index=cookie.indexOf("=");
            if(index!=-1)
            {
                String key=cookie.substring(0,index).trim();
                String val="";
                // Is trim safe in this case ??
                if(cookie.length()>index)
                    val=cookie.substring(index+1,cookie.length()).trim();
               cookies.add(new Cookie(key,val)); 
            }
        }
    }

    /**
     * Print detailed infos about the request
     * @return
     */
    public String toString()
    {
        String str= ""+getClass().getSimpleName()+" [method:"+method+" proto:"+protocol+" path:"+path+" host:"+remoteHost+" port:"+remotePort+"]";
        str+="\n\tHeaders:";
        Enumeration e=headers.keys();
        while(e.hasMoreElements())
        {
            String key=(String)e.nextElement();
            str+="\n\t\t"+key+" -> ";
            Vector v=(Vector)headers.get(key);
            for(int i=0 ; i!=v.size(); i++)
                str+=v.get(i)+" | ";
        }
        str+="\n\tCookies:";
        for(int i=0 ; i!=cookies.size(); i++)
        {
            Cookie cookie=(Cookie)cookies.get(i);
            str+="\n\t\t"+cookie.getName()+" : "+cookie.getValue();
        }
        str+="\n\tParams:";
        Enumeration e2=parameters.keys();
        while(e2.hasMoreElements())
        {
            String key=(String)e2.nextElement();
            String val=(String)parameters.get(key);
            str+="\n\t\t"+key+" -> "+val;
        }
        str+="\n";
        return str;
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

    public String getPath()
    {
        return path;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getRawRequestLine()
    {
        return rawRequestLine;
    }

    public String getLocalHost()
    {
        return localHost;
    }

    public void setLocalHost(String localHost)
    {
        this.localHost = localHost;
    }

    public int getLocalPort()
    {
        return localPort;
    }

    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

    public String getServerName()
    {
        // lazy inited
        if (serverName!=null)
            return serverName;

        // Return host from absolute URI
        /*serverName=_uri.getHost();
        if (serverName!=null)
            return serverName;
*/
        // Return host from header field
        String host=(String)headers.get("Host");
        if (host!=null)
        {
            try
            {
                URL url=new URL(host);
                serverName=url.getHost();
                return serverName;
            }
            catch(MalformedURLException e){/*try something else*/}
        }

        // Try from socket host
        if (socket!=null)
        {
            //TODO: use canonial or regular host name ??
            serverName=socket.getLocalAddress().getCanonicalHostName();
            if(serverName!=null)
                return serverName;
        }

        // Fallback to local host
        try 
        {
            serverName=InetAddress.getLocalHost().getHostAddress();
        }
        catch(java.net.UnknownHostException e){/*How could that fail ?? */}
        return serverName;
    }

    public String getScheme()
    {
        if(scheme!=null)
            return scheme;
        //TODO
        scheme="http://";
        return scheme;
    }

    public String getURL()
    {
        if(url!=null)
            return url;
        StringBuffer sb=new StringBuffer(getScheme()).append(getServerName());
        if(getLocalPort() != 80)
            sb.append(":").append(getLocalPort());
        sb.append(getPath());
        if(rawParams!=null && rawParams.length()>0)
            sb.append("?").append(rawParams);
        url=sb.toString();
        System.out.println(url);
        return url;
    }

    void setRawParameters(String params)
    {
        rawParams=params;
    }
    
}
