/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Hashtable;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.jot.logger.JOTLogger;
import net.jot.logger.JOTLoggerLocation;
import net.jot.utils.JOTUtilities;
import net.jot.web.server.JOTWebHelper;
import net.jot.web.server.JOTWebRequestHandlerBase;

/**
 *
 * @author tcolar
 */
public class JOTStaticServerHandler extends JOTWebRequestHandlerBase{
    private File rootFolder;
    private static final JOTLoggerLocation logger=new JOTLoggerLocation(JOTLogger.CAT_SERVER,JOTStaticServerHandler.class);

    public void handle() throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Received Request:  "+request.getRemoteHost()+" "+request.getRawRequestLine());
        }
        if(logger.isTraceEnabled())
            logger.trace("Received Request:  "+request.toString());
        
        if(request.getMethod().equalsIgnoreCase("GET"))
        {
            String path=request.getServletPath();
            File f=new File(rootFolder+path);
            // security check
            if( ! JOTUtilities.isWithinFolder(f, rootFolder))
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,"Forbidden path.");
                return;
            }
            if( ! f.exists())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,"File not found.");
                return;
            }
            if(f.isDirectory())
            {
                sendDirectoryListing(f);
            }
            else
            {
                sendFile(f);
            }
        }
        else
        {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"Only the GET method is supported.");
            return;
        }
    }

    public void init(Hashtable params)
    {
        rootFolder=new File((String)params.get(JOTStaticWebServer.ROOT_FOLDER));
        if(rootFolder==null || !rootFolder.isDirectory())
        {
            throw new IllegalArgumentException("Root folder is not a folder !! :"+rootFolder.getAbsolutePath());
        }
    }

    private void sendDirectoryListing(File f) throws Exception
    {
        response.setContentType("text/html");
        //response.setHeader("Location",request.getServletPath());
        File[] files=f.listFiles();
        files=JOTUtilities.sortFolderListing(files, JOTUtilities.SORT_BY_NAME_ASC);
        PrintWriter writer=response.getWriter();
        writer.println(JOTWebHelper.MSG_HEAD + "Directory Listing: ["+request.getServletPath()+"]" + JOTWebHelper.MSG_HEAD2+"<h5>");
        writer.println("<a href='../'>[..]</a>(UP)<br/>");
        for(int i=0;i!=files.length;i++)
        {
            File file=files[i];
            if(file.isDirectory())
            {
                writer.println("<a href='"+URLEncoder.encode(file.getName(),"UTF-8")+"/'>["+file.getName()+"]</a><br/>");
            }
            else
            {
                writer.println("<a href='"+URLEncoder.encode(file.getName(),"UTF-8")+"'>"+file.getName()+"</a><br/>");
            }
        }
        writer.println("</h5>"+JOTWebHelper.MSG_TAIL);
    }

    private void sendFile(File f) throws Exception
    {
        // not setting content type or anyhting, will let the browser deal with it.
        byte[] buffer=new byte[5000]; //5k
        ServletOutputStream stream=response.getOutputStream();
        FileInputStream fis=new FileInputStream(f);
        int read=-1;
        while((read=fis.read(buffer))!=-1)
        {
            stream.write(buffer,0,read);
        }
    }

}