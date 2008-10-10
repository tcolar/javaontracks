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
        //JOTGoogleMapWidget wg=new JOTGoogleMapWidget();
        //Hashtable args=new Hashtable();
        //args.put("address","3531  canterbury ln, kent wa");
        //args.put("gkey","ABQIAAAAr7MMhUq-UD2S0-q5Dm13ARRi_j0U6kJrkFvY4-OX2XYmEAa76BR9FkLr7KJ57j9i0k7J8NhSc9VPWw");
        return "<html><body>" + getJotHeader()+"<h5>"+
                "- <a href='project?action=create'/>Create a new JavaOnTracks project</a>" +
               "</h5></body></html>";
    }
    
}
