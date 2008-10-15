/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpResponse impl.
 * @author thibautc
 */
public class JOTWebResponse implements HttpServletResponse{

    /** The connection socket to the client*/
    private Socket socket;
    
    private final static int DEFAULT_BUFFER_SIZE=5000;

    /** Default: 5K */
    private int bufferSize=DEFAULT_BUFFER_SIZE;
    /** Default: efault java VM Loc.*/
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
    
    private JOTWebResponse()
    {
    }
    /**
     * Create the response from the client socket.
     * @param socket
     */
    public JOTWebResponse(Socket socket)
    {
        this.socket=socket;
    }
    
    public void addCookie(Cookie arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsHeader(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String encodeURL(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String encodeRedirectURL(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String encodeUrl(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String encodeRedirectUrl(String arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public void setDateHeader(String arg0, long arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addDateHeader(String arg0, long arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHeader(String arg0, String arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addHeader(String arg0, String arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setIntHeader(String arg0, int arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addIntHeader(String arg0, int arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setStatus(int arg0)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setStatus(int arg0, String arg1)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLocale(Locale locale)
    {
        loc=locale;
    }

    public Locale getLocale()
    {
        return loc;
    }

}
