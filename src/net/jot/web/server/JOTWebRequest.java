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
    String host=null;
    int port=-1;
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

    public void setHost(String host)
    {
        this.host = host;
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

    public void setPort(int port)
    {
        this.port = port;
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
        String str= ""+getClass().getSimpleName()+" [method:"+method+" proto:"+protocol+" path:"+path+" host:"+host+" port:"+port+"]";
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

    public String getHost()
    {
        return host;
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

    public int getPort()
    {
        return port;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getRawRequestLine()
    {
        return rawRequestLine;
    }
    
}
