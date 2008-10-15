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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
public class JOTWebResponse implements HttpServletResponse
{

    /** The connection socket to the client*/
    private final Socket socket;
    private final static int DEFAULT_BUFFER_SIZE = 5000;
    /** Default: 5K */
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    /** Default: default java VM Loc.*/
    private Locale loc = Locale.getDefault();
    /** Default: ISO-8859-1*/
    private String encoding = "ISO-8859-1";
    private boolean isCommited = false;
    private boolean bufferUsed = false;
    /** default: unset*/
    private int contentLength = -1;
    /** default: unset*/
    private String contentType = null;    // user print and output streams : only one of the two can be used !
    private MyWriter print = null;
    private MyStream out = null;
    /** Defaults to 200: OK*/
    private int statusCode = SC_OK;
    /** headers (header name -> Vector of values(string)) **/
    Hashtable headers = new Hashtable();
    // vector of Cookie
    Vector cookies = new Vector();
    private String sessionID = null;
    // we need the request to encode URL's etc...
    private JOTWebRequest request;

    JOTWebResponse(Socket socket, JOTWebRequest request)
    {
        this.socket = socket;
        this.request = request;
    //TODO: read jsessionID from the request (if avail)
    }

    public void addCookie(Cookie cookie)
    {
        if (isCommited)
        {
            throw new IllegalStateException("We already have flushed the buffer, to late!");
        }
        cookies.add(cookie);
    }

    public boolean containsHeader(String headerName)
    {
        return headers.containsKey(headerName);
    }

    public String encodeURL(String url)
    {
        // TODO: what about relative URL's ??
        return addSessionIdToURL(url, sessionID);
    }

    public String encodeRedirectURL(String url)
    {
        // TODO: what about relative URL's ??
        // TODO: what is the diff with encodeURL ??
        return encodeURL(url);
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

    public void sendError(int statusCode, String err) throws IOException
    {
        if (isCommited)
        {
            throw new IllegalStateException("Response already commited !");
        }
        reset();
        setStatus(statusCode);
        sendText(err);
        flushBuffer();
    }

    public void sendError(int statusCode) throws IOException
    {
        sendError(statusCode, null);
    }

    public void sendRedirect(String url) throws IOException
    {
        url = absoluteURL(url);
        if (isCommited)
        {
            throw new IllegalStateException("Response already commited !");
        }
        setStatus(SC_MOVED_TEMPORARILY);
        setHeader("Location", url);
        isCommited = true;
    }

    public void setDateHeader(String key, long date)
    {
        // UTC/GMT style date
        String value = JOTTimezoneUtils.convertTimezone(new Date(date), "GMT + 0", JOTTimezoneUtils.FORMAT_HEADER) + " GMT";
        setHeader(key, "" + value);
    }

    public void addDateHeader(String key, long date)
    {
        // UTC/GMT style date
        String value = JOTTimezoneUtils.convertTimezone(new Date(date), "GMT + 0", JOTTimezoneUtils.FORMAT_HEADER) + " GMT";
        addHeader(key, "" + value);
    }

    public void setHeader(String key, String value)
    {
        headers.remove(key);
        addHeader(key, value);
    }

    public void addHeader(String key, String value)
    {
        if (isCommited)
        {
            throw new IllegalStateException("We already have flushed the buffer, to late!");
        }
        Vector v = new Vector();
        if (headers.containsKey(key))
        {
            v = (Vector) headers.get(key);
        }
        v.add(value);
        headers.put(key, v);
    }

    public void setIntHeader(String key, int value)
    {
        setHeader(key, "" + value);
    }

    public void addIntHeader(String key, int value)
    {
        addHeader(key, "" + value);
    }

    public void setStatus(int status)
    {
        if (isCommited)
        {
            throw new IllegalStateException("We already have flushed the buffer, to late!");
        }
        statusCode = status;
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
        } catch (IOException e)
        {
        }
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
        if (print != null)
        {
            throw new IllegalStateException("Can not gte both an outputstream and a printwriter !");
        }
        if (out == null)
        {
            out = new MyStream(socket.getOutputStream());
        }
        bufferUsed=true;
        return (ServletOutputStream) (OutputStream) out;
    }

    public PrintWriter getWriter() throws IOException
    {
        if (out != null)
        {
            throw new IllegalStateException("Can not get both an outputstream and a printwriter !");
        }
        if (print == null)
        {
            print = new MyWriter(socket.getOutputStream());
        }
        bufferUsed=true;
        return print;
    }

    public void setCharacterEncoding(String enc)
    {
        if (isCommited)
        {
            throw new IllegalStateException("We already have flushed the buffer, to late!");
        }
        encoding = enc;
    }

    public void setContentLength(int length)
    {
        if (isCommited)
        {
            throw new IllegalStateException("We already have flushed the buffer, to late!");
        }
        contentLength = length;
    }

    public void setContentType(String type)
    {
        if (isCommited)
        {
            throw new IllegalStateException("We already have flushed the buffer, to late!");
        }
        contentType = type;
    }

    public void setBufferSize(int size)
    {
        if (bufferUsed)
        {
            throw new IllegalStateException("Can't change buffer size, already retrieved.");
        }
        bufferSize = size;
        clearBuffer();
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    public void flushBuffer() throws IOException
    {
        if (print != null)
        {
            print.flush();
        }
        if (out != null)
        {
            out.flush();
        // TODO: do the flush: write the headers etc.. (if first time) and then send the buffer content
        }
    }

    public void resetBuffer()
    {
        if (isCommited)
        {
            throw new IllegalStateException("The buffer was already flushed !");
        }
        clearBuffer();
    }

    public boolean isCommitted()
    {
        return isCommited;
    }

    public void reset()
    {
        clearBuffer();
        statusCode = SC_OK;
        headers.clear();
        cookies.clear();
    }

    public void setLocale(Locale locale)
    {
        loc = locale;
    }

    public Locale getLocale()
    {
        return loc;
    }

    private String absoluteURL(String url)
    {
        //TODO: make this work right somehow
        // no need to do jsessionid, user would call encoderedirurl first
        return url;
    }

    private void clearBuffer()
    {
        print = null;
        out = null;
    }

    private String addSessionIdToURL(String url, String sessionId)
    {
        //TODO: only encode if url is relvant to this webapp ? 
        if ((url == null) || (sessionId == null))
        {
            return (url);
        }
        String path = "";
        String args = "";
        String anchor = "";
        int urlIndex = url.indexOf('?');
        if (urlIndex != -1)
        {
            path = url.substring(0, urlIndex);
            args = url.substring(urlIndex);
        }
        int anchorIndex = path.indexOf('#');
        if (anchorIndex != -1)
        {
            anchor = path.substring(anchorIndex);
            path = path.substring(0, anchorIndex);
        }
        StringBuffer sb = new StringBuffer(path).append(";jsessionid=").append(sessionId).append(anchor).append(args);
        return (sb.toString());
    }

    public void destroy() throws Exception
    {
        Exception ex = null;
        try
        {
            flushBuffer();
        } catch (Exception e)
        {
            ex = e;
        }
        //TODO: close thestreams and socket etc ... ??
        try
        {
            if (out != null)
            {
                out.close();
            }
        } catch (Exception e)
        {
            ex = e;

        }
        try
        {
            if (print != null)
            {
                print.close();
            }
        } catch (Exception e)
        {
            ex = e;

        }
        try
        {
            if (socket != null && !socket.isClosed() && socket.getInputStream() != null)
            {
                socket.getInputStream().close();
            }
        } catch (Exception e)
        {
            ex = e;

        }
        try
        {
            if (socket != null && !socket.isClosed() && socket.getOutputStream() != null)
            {
                socket.getOutputStream().close();
            }
        } catch (Exception e)
        {
            ex = e;

        }
        if (ex != null)
        {
            throw (ex);
        }
    }

    /**
     * Send some message to the browser.
     * Used mostlt for error messages
     * @param err
     */
    private void sendText(String text)
    {
        setContentType("text/html");
        setContentLength(text.length());
        try
        {
            if (print != null)
            {
                print.print(text);
            } else if (out != null)
            {
                out.write(text.getBytes());
            }
        } catch (Exception e)
        {/* TODO */
            e.printStackTrace();
        }

    }

    void writePreamble()
    {
        if(!isCommited)
        {
            // TODO write response code, headers, content type etc...
        }
        isCommited=true;
    }

    class MyStream extends BufferedOutputStream
    {

        MyStream(OutputStream out)
        {
            super(out);
            setBufferSize(bufferSize);
        }

        /**
         * Override, once it's flushed the first time, setting to commited
         * @throws java.io.IOException
         */
        public synchronized void flush() throws IOException
        {
            //call the response flushbuffer (ie: commit)
            writePreamble();
            // then proceed with comitting the actual content.
            super.flush();
        }
    }

    class MyWriter extends PrintWriter
    {

        MyWriter(OutputStream out)
        {
            super(out);
            setBufferSize(bufferSize);
        }

        /**
         * Override, once it's flushed the first time, setting to commited
         * @throws java.io.IOException
         */
        public synchronized void flush()
        {
            //call the response flushbuffer (ie: commit)
            try
            {
                writePreamble();
            } catch (Exception e)
            {
                //throw new RuntimeException();
            }
            // then proceed with comitting the actual content.
            super.flush();
        }
    }
}
