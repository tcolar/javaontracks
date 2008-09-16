/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jottools.actions;

import java.util.Hashtable;

/**
 *
 * @author thibautc
 */
public class ConfigHome extends ConfigView
{

    public String getPageContent(Hashtable parameters)
    {
        return "<html><body>" + getJotHeader()+"<h5>"+
                "- <a href='project?action=create'/>Create a new JavaOnTracks project</a>" +
               "</h5></body></html>";
    }
    
}
