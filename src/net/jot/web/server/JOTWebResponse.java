/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * 
Server: Sun-Java-System-Web-Server-6.1
Date: Wed, 15 Oct 2008 18:36:27 GMT
Content-Type: text/html
Last-Modified: Mon, 17 Dec 2007 23:57:43 GMT
Etag: "6153-47670cf7"
Accept-Ranges: bytes
Transfer-Encoding: chunked

200 OK
 * ---
 * 
Date: Wed, 15 Oct 2008 18:38:52 GMT
Server: Apache/2.2.8 (Ubuntu) PHP/5.2.4-2ubuntu5.3 with Suhosin-Patch
X-Powered-By: PHP/5.2.4-2ubuntu5.3
Expires: Thu, 19 Nov 1981 08:52:00 GMT
Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0
Pragma: no-cache
Content-Length: 6609
Keep-Alive: timeout=15, max=100
Connection: Keep-Alive
Content-Type: text/html; charset=UTF-8

200 OK
 * ----
 * 
Date: Wed, 15 Oct 2008 18:39:11 GMT
Server: Apache
Accept-Ranges: bytes
Cache-Control: max-age=60, private, private
Expires: Wed, 15 Oct 2008 18:40:03 GMT
Content-Type: text/html
Vary: User-Agent,Accept-Encoding
Content-Encoding: gzip
Content-Length: 20420
Keep-Alive: timeout=5, max=63
Connection: Keep-Alive

200 OK
 */

package net.jot.web.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import net.jot.utils.JOTTimezoneUtils;

/**
 * HttpServletResponse impl.
 * @author thibautc
 */
public class JOTWebResponse implements HttpServletResponse{

    /** The connection socket to the client*/
    private final Socket socket;
    
    private final static int DEFAULT_BUFFER_SIZE=5000;

    /** Default: 5K */
    private int bufferSize=DEFAULT_BUFFER_SIZE;
    /** Default: default java VM Loc.*/
    private Locale loc=Locale.getDefault();
    /** Default: ISO-8859-1*/
    private String encoding="ISO-8859-1";

    private boolean isCommited=false;
    private boolean anythingWritten=false;

    /** default: unset*/
    private int contentLength=-1;
    /** default: unset*/
    private String contentType=null;
    
    // user print and output streams : only one of the two can be used !
    private PrintWriter print=null;
    private ServletOutputStream out=null;
    
    private byte[] buffer=new byte[bufferSize];
    /** Defaults to 200: OK*/
    private int statusCode=SC_OK;
    /** headers (header name -> Vector of values(string)) **/
    Hashtable headers=new Hashtable();
    // vector of Cookie
    Vector cookies=new Vector();
    
    /**
     * Create the response from the client socket.
     * @param socket
     */
    public JOTWebResponse(Socket socket)
    {
        this.socket=socket;
    }
    
    public void addCookie(Cookie cookie)
    {
        cookies.add(cookie);
    }

    public boolean containsHeader(String headerName)
    {
        return headers.containsKey(headerName);
    }

    public String encodeURL(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String encodeRedirectURL(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @deprecated
     * @param arg0
     * @return
     */
    public String encodeUrl(String arg0)
    {
        return encodeURL(arg0);
    }
    /**
     * @deprecated
     * @param arg0
     * @return
     */
    public String encodeRedirectUrl(String arg0)
    {
        return encodeRedirectURL(arg0);
    }

    public void sendError(int arg0, String arg1) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendError(int arg0) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void sendRedirect(String arg0) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setDateHeader(String key, long date)
    {
        String value= JOTTimezoneUtils.convertTimezone(new Date(date), "GMT + 0", JOTTimezoneUtils.FORMAT_HEADER)+" GMT";
        setHeader(key, ""+value);
    }

    public void addDateHeader(String key, long date)
    {
        String value= JOTTimezoneUtils.convertTimezone(new Date(date), "GMT + 0", JOTTimezoneUtils.FORMAT_HEADER)+" GMT";
        addHeader(key, ""+value);
    }

    public void setHeader(String key, String value)
    {
        headers.remove(key);
        addHeader(key,value);
    }

    public void addHeader(String key, String value)
    {
        Vector v=new Vector();
        if(headers.containsKey(key))
        {
            v=(Vector)headers.get(key);
        }
        v.add(value);
        headers.put(key,v);
    }

    public void setIntHeader(String key, int value)
    {
        setHeader(key, ""+value);
    }

    public void addIntHeader(String key, int value)
    {
        addHeader(key, ""+value);
    }

    public void setStatus(int status)
    {
        statusCode=status;
    }

    /**
     * @deprecated use sendError() instead
     * @param status
     * @param sm
     */
    public void setStatus(int status, String sm)
    {
        // deprecated, uses sendError instead
        try
        {
            sendError(status, sm);
        }
        catch(IOException e){}
    }

    public String getCharacterEncoding()
    {
        return encoding;
    }

    public String getContentType()
    {
        return contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
        if(print!=null)
            throw new IllegalStateException("Can not gte both an outputstream and a printwriter !");
        if(out==null)
            out=(ServletOutputStream)socket.getOutputStream();
        return out;
    }

    public PrintWriter getWriter() throws IOException
    {
        if(out!=null)
            throw new IllegalStateException("Can not gte both an outputstream and a printwriter !");
        if(print==null)
            print=new PrintWriter(socket.getOutputStream());
        return print;
    }

    public void setCharacterEncoding(String enc)
    {
        encoding=enc;
    }

    public void setContentLength(int length)
    {
        contentLength=length;
    }

    public void setContentType(String type)
    {
        contentType=type;
    }

    public void setBufferSize(int size)
    {
        if(anythingWritten)
            throw new IllegalStateException("Can't change buffer size, after data was written.");
        bufferSize=size;
        clearBuffer();
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public void flushBuffer() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetBuffer()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCommitted()
    {
        return isCommited;
    }

    public void reset()
    {
        clearBuffer();
        statusCode=SC_OK;
        headers.clear();
    }

    public void setLocale(Locale locale)
    {
        loc=locale;
    }

    public Locale getLocale()
    {
        return loc;
    }

    private void clearBuffer()
    {
        buffer=new byte[bufferSize];
    }
    
    /*public static void main(String[] args)
    {
        System.out.println(JOTTimezoneUtils.convertTimezone(new Date(), "GMT + 0", JOTTimezoneUtils.FORMAT_HEADER));
    }*/
}
