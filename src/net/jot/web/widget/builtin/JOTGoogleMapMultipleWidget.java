/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget.builtin;

import java.util.Hashtable;

/**
 * A widget that renders a google map with a bunch of markers on it (a to z)
 * Expectes the following widgte args:
 * - addresses: pipe(|) separated list of addresses - Each address need to be encoded using JOTWidgetBase.encodeCommas()
 * - gkey : your google Map API key
 * @author thibautc
 */
public class JOTGoogleMapMultipleWidget extends JOTGoogleMapWidget
{
    // call after all the addoverlay to zoom out until all markers can be seen
    public String getShowAllMarkers()
    {
        return "<script type='text/javascript'>map.setCenter(bounds.getCenter(),map.getBoundsZoomLevel(bounds));</script>";
    }
    
    // override
    public String renderBoxContent(Hashtable args)
    {
        String addresses=(String)args.get("addresses");
        if(addresses==null)
            addresses="";
        String[] adds=addresses.split("\\|");
        String googleKey=(String)args.get("gkey");
        String html = "";
        html+=getGoogleMapCanvas("350", "350");
        html+=getGoogleScriptString(googleKey);
        html+=getGoogleInit();
        html+="<script type='text/javascript'>var bounds = new GLatLngBounds();</script>";
        char c='A';
        for(int i=0;i!=adds.length;i++)
        {
            html+=addGoogleMarker(adds[i],""+c);
            c++;
            if(c > 'Z')
            {
                c='A';
            }
        }
        html+=getShowAllMarkers();
        return html;
    }
}
