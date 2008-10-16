package net.jot.web.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.utils.JOTTimezoneUtils;

/**
 * HttpServletResponse impl.
 * @author thibautc
 */
public class JOTWebResponse implements HttpServletResponse
{
    JOTLoggerLocation logger=new JOTLoggerLocation(JOTLogger.CAT_SERVER,getClass());
    public final static String INFOS="JavaOnTracks Server 1.0";
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
    private static final String MSG_HEAD="<html><body><table width=100%><tr height=25 bgcolor='#eeeeff'><td><b>ERROR ";
    private static final String MSG_HEAD2="</b></td></tr><tr><td>";
    private static final String MSG_TAIL="</td></tr><tr height=15 bgcolor='#eeeeff'><td>"+INFOS+"</td></tr></table></body></html>";
    private static final Pattern PROTOCOL_PATTERN=Pattern.compile("^\\w+\\://.*");

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
        return addSessionIdToURL(url, sessionID);
    }

    public String encodeRedirectURL(String url)
    {
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
        sendMessage(""+statusCode,err);
        reset();
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
        return (ServletOutputStream)out;
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
        else if (out != null)
        {
            out.flush();
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
        if(url.startsWith("/"))
        {
            //request.getProtocol()+":"+request.get
            // URLrelative to server root
        }
        else if(PROTOCOL_PATTERN.matcher(url).matches())
        {
            // TODO: test this pattern
            // already absolute URL .. do nothing
        }
        else
        {
            // URL relative to current request
        }
        // no need to do jsessionid, user would call encoderedirurl first ??
        return url;
    }

    private void clearBuffer()
    {
        print = null;
        out = null;
    }

    private String addSessionIdToURL(String url, String sessionId)
    {
        //TODO: only encode if url is relevant to this webapp ? 
        // TODO: what about relative URL's ??

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

    public void destroy()
    {
        Exception ex = null;
        try
        {
            flushBuffer();
        } catch (Exception e)
        {
            ex = e;
        }
        //TODO: close the streams and socket etc ... ??
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
            logger.exception("Errors closing request socket.", ex);
        }
    }
    /**
     * internal message page (errorrs etc..)
     * Does not set status_code, do that beforehand
     * @param string
     * @param err
     */
    private void sendMessage(String title, String message) throws IOException
    {
        setStatus(statusCode);
        sendText(MSG_HEAD+title+MSG_HEAD2+message+MSG_TAIL);
        flushBuffer();
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
            }else
            {
                getOutputStream().write(text.getBytes());
            }
        } catch (Exception e)
        {/* TODO */
            logger.exception("Failed adding text to response.", e);
        }

    }

    void writePreamble()
    {
        if(!isCommited)
        {
            try
            {
            PrintWriter p=new PrintWriter(socket.getOutputStream());
            p.println("HTTP/1.1 "+statusCode);
            p.println("Location: "+"TODO");
            //content-type
            p.println("Content-encoding: "+encoding);
            if(contentType!=null)
                p.println("Content-type: "+contentType);
            if(contentLength!=-1)
                p.println("Content-length: "+contentLength);
            //headers
            p.println("Status: "+statusCode);
            if(!headers.containsKey("Server"))
            {
                p.println("Server: "+INFOS);
            }
            if(!headers.containsKey("Date"))
            {
                String now=JOTTimezoneUtils.convertTimezone(new Date(), "GMT + 0", JOTTimezoneUtils.FORMAT_HEADER);
                p.println("Date: "+now+" GMT");
            }
            Enumeration e=headers.keys();
            while(e.hasMoreElements())
            {
                String key=(String)e.nextElement();
                StringBuffer header=new StringBuffer(key).append(": ");
                Vector values=(Vector)headers.get(key);
                for(int i=0;i!=values.size();i++)
                {
                    header.append((String)(values.get(i)));
                    if(i<values.size()-1)
                        header.append(";");
                }
                p.println(header);
            }
            //cookies
            if(cookies.size()>0)
            {
                StringBuffer header=new StringBuffer("Cookies: ");
                for(int i=0;i!=cookies.size();i++)
                {
                    Cookie cookie=(Cookie)cookies.get(i);
                    header.append(cookie.getName()).append("=").append(cookie.getValue());
                    if(i<cookies.size()-1)
                        header.append(";");
                }
                p.println(header);
            }
            //end headers, add empty line
            p.println();
            p.flush();
            }
            catch(Exception e)
            {
                logger.exception("Failure while writing headers to response.", e);
            }
        }
        isCommited=true;
    }

    class MyStream extends ServletOutputStream
    {
        BufferedOutputStream stream;
        MyStream(OutputStream out)
        {
            stream=new BufferedOutputStream(out,bufferSize);
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
            stream.flush();
        }

        public void write(int b) throws IOException
        {
            stream.write(b);
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
            writePreamble();
            super.flush();
        }
    }
}
