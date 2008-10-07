/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget.builtin;

import java.util.Hashtable;

/**
 * A widget that renders a google map for one address
 * Expectes the following widgte args:
 * - address - address need to be encoded using JOTWidgetBase.encodeCommas()
 * - gkey: your google Map API key
 * @author thibautc
 */
public class JOTGoogleMapWidget extends JOTTitledBoxWidget
{

    public String getShortName()
    {
        return "Google Map";
    }

    public boolean validatePermissions()
    {
        return true;
    }

    public String getTitle()
    {
        return "Google Map";
    }

    public String getGoogleScriptString(String googleKey)
    {
       return "<script src='http://maps.google.com/maps?file=api&amp;v=2.x&amp;key="+googleKey+"' type='text/javascript'></script>";
    }
    public String getGoogleInit()
    {
       // center on north pole by default.
       return "<script type='text/javascript'>var map = null;var geocoder = null;if (GBrowserIsCompatible()) {map = new GMap2(document.getElementById('map_canvas')); map.setCenter(new GLatLng(90, 0), 13);geocoder = new GClientGeocoder();map.addControl(new GLargeMapControl());map.addControl(new GMapTypeControl());}</script>";
    }

    public String addGoogleMarker(String address, String letter)
    {
        String marker="<script type='text/javascript'>if (geocoder) {geocoder.getLatLng('"+address+"',function(point) {if (point) {";
        
        if(letter==null)
            marker+="map.setCenter(point, 13);var marker = new GMarker(point);";
        else
            marker+="var myIcon = new GIcon(G_DEFAULT_ICON);myIcon.image ='http://www.google.com/mapfiles/marker" + letter + ".png';markerOptions={icon:myIcon };var marker=new GMarker(point, markerOptions);bounds.extend(point);";
        
        marker+="map.addOverlay(marker);}});}</script>";
        return marker;
    }
    public String getGoogleMapCanvas(String width, String height)
    {
        return "<div id='map_canvas' style='width: "+width+"px; height: "+height+"px'></div>";
    }
    public String getLinkToGoogle(String address)
    {
        if(response!=null)
            address=response.encodeURL(address);
        return "<center><a target='_blank' href='http://maps.google.com/maps?hl=en&q="+address+"'>Click here for larger Map & Directions</a></center>";
    }
    
    public String renderBoxContent(Hashtable args)
    {
        String address=(String)args.get("address");
        String googleKey=(String)args.get("gkey");
        String html = "";
        //html+=address;
        html+=getGoogleMapCanvas("350", "350");
        html+=getGoogleScriptString(googleKey);
        html+=getGoogleInit();
        html+=addGoogleMarker(address, null);
        html+=getLinkToGoogle(address);
        return html;
    }

}
