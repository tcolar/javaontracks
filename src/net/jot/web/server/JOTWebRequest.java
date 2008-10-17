/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

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
    
    String method="GET";
    String protocol=null;
    String path;
    // requestLine as it came to us
    String rawRequestLine;
    String remoteHost=null;
    int remotePort=-1;
    String localHost=null;
    int localPort=-1;
    /** headers (header name -> Vector of values(string)) **/
    Hashtable headers=new Hashtable();
    // parameters (hash of strings)
    Hashtable parameters=new Hashtable();
    // vector of Cookie
    Vector cookies=new Vector();
    // TODO: basic authentication ?
    //String user;
    //String password;
    
    // body if any (ex: multipart)
    byte[] body;

    
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

    
    /*
     *
    public String getHost()
    {
        // Return already determined host
        if (_host!=null)
            return _host;

        // Return host from absolute URI
        _host=_uri.getHost();
        _port=_uri.getPort();
        if (_host!=null)
            return _host;

        // Return host from header field
        _hostPort=_header.get(HttpFields.__Host);
        _host=_hostPort;
        _port=0;
        if (_host!=null && _host.length()>0)
        {
            int colon=_host.lastIndexOf(':');
            if (colon>=0)
            {
                if (colon<_host.length())
                {
                    try{
                        _port=TypeUtil.parseInt(_host,colon+1,-1,10);
                    }
                    catch(Exception e)
                    {Code.ignore(e);}
                }
                _host=_host.substring(0,colon);
            }

            return _host;
        }

        // Return host from connection
        if (_connection!=null)
        {
            _host=_connection.getServerName();
            _port=_connection.getServerPort();
            if (_host!=null && !InetAddrPort.__0_0_0_0.equals(_host))
                return _host;
        }

        // Return the local host
        try {_host=InetAddress.getLocalHost().getHostAddress();}
        catch(java.net.UnknownHostException e){Code.ignore(e);}
        return _host;
    }
    */
}
