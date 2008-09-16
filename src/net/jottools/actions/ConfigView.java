/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jottools.actions;

import java.io.OutputStream;
import java.util.Hashtable;
import net.jot.JOTInitializer;

/**
 *
 * @author thibautc
 */
public abstract class ConfigView
{
    Hashtable attributes=new Hashtable();
    
    public void writePage(OutputStream out, Hashtable parameters)
    {
        //String workspace=getWorkspace();
        //if(workspace!=null)
        //    attributes.put("workspace",workspace);
        
        String content=getPageContent(parameters);
        try
        {
            out.write("HTTP/1.0 200 OK\r\n".getBytes());
            //out.write(("Content-Length: "+content.length()+"\n").getBytes());
            out.write(("Content-Type: text/html; charset=utf-8\r\n").getBytes());
            out.write(("\r\n").getBytes());
                        
            out.write(content.getBytes());
            out.flush();
        }
        catch(Exception e){}
    }
    
    public String getJotHeader()
    {
        return "<center><a href='/home'>JavaOnTracks "+JOTInitializer.VERSION+" config tool.</a></center><br>Workspace(Projects root): <input type='text' size='40' name='workspace'/><input type='submit' value='Update'/><hr/>";
    }
    
    /**
     *  get the html inner page
     *  
     */
    public abstract String getPageContent(Hashtable parameters);

    /*private String getWorkspace()
    {
         
    }*/
    
}
