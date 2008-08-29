/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import net.jot.utils.JOTUtilities;

/**
 * Extension of JOTFlowRequest allowing rewriting of the request URL "on the fly" without loosing the original request and associated parameters/attributes
 * The original request is "wrapped" in a new request with the url, manipulated
 * @author thibautc
 */
public class JOTRewrittenRequest extends JOTFlowRequest 
{

    private StringBuffer url;
    private String uri=null;
    private String path=null;
    
    public JOTRewrittenRequest(HttpServletRequest request, String newPath, Hashtable attributes) 
    {
        super(request);
        url = new StringBuffer();
        url.append(request.getScheme()).append("://").append(request.getServerName());
        int port = request.getServerPort();
        if(port!=80)
            url.append(":" + port);
        url.append(JOTUtilities.endWithForwardSlash(request.getContextPath())).append(newPath);
        uri=JOTUtilities.endWithForwardSlash(request.getContextPath())+newPath;
        path=newPath;
        Enumeration e=attributes.keys();
        while(e.hasMoreElements()) 
        {
        	String key=(String)e.nextElement();
        	setParameter(key, (String)attributes.get(key));
        }    

   }

    public String getServletPath() 
    {
        return path;
    }

    public String getRequestURI() 
    {
        return uri;
    }

    public StringBuffer getRequestURL() 
    {
        return url;
    }


}