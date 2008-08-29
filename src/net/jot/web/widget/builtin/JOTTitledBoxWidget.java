/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget.builtin;

import java.util.Hashtable;
import net.jot.web.widget.JOTWidgetBaseProperties;
import net.jot.web.widget.JOTWidgetNoAjaxBase;
import net.jot.web.widget.JOTWidgetProperty;

/**
 * "Box"(with border) with title and content widget
 * @author thibautc
 */
public abstract class JOTTitledBoxWidget extends JOTWidgetNoAjaxBase
{
    public String renderWidget(Hashtable options)
    {
        String bgColor=properties.getPropertyDefaultValue(JOTWidgetBaseProperties.PROP_BG_COLOR, options, "#FFFFFF");
        String borderType=properties.getPropertyDefaultValue(JOTWidgetBaseProperties.PROP_BORDER_TYPE, options, JOTWidgetBaseProperties.VAL_BORDER_TYPE_SOLID);
        String borderThickness=properties.getPropertyDefaultValue(JOTWidgetBaseProperties.PROP_BORDER_WIDTH, options, JOTWidgetBaseProperties.VAL_BORDER_THICKNESS_MEDIUM);
        String borderColor=properties.getPropertyDefaultValue("borderColor", options, "#000000");
        String titleBgColor=properties.getPropertyDefaultValue("titleBgColor", options, "#CCCCCC");
        String titleColor=properties.getPropertyDefaultValue("titleColor", options, "#000000");        
        
        String width=properties.getPropertyDefaultValue(JOTWidgetBaseProperties.PROP_WIDTH, options, getWidth());        
        
        String html="<STYLE type='text/css'>.widget_button_link{font-weight:bold;color: #FFFF86;background-color: #2e5d89;border-width: 1px;border-style: solid;border-color: black;vertical-align: middle;text-decoration: none;cursor: pointer;padding-left:3px;padding-right:3px} .widget_default_line{border-width:1px;border-color:#ffffff;border-style:solid;background-color:#eeeeee} .table_cell{background-color:#eeeeee;border-style: solid; border-width: 0px;padding:0px;} .table_header{background-color:#dddddd;}</STYLE>";
        html+="<div style='padding:2px;margin:2px;border-style:"+borderType+";border-width:"+borderThickness+";border-color:"+borderColor+";background-color:"+bgColor+";width:"+width+";max-width:"+width+";overflow:"+getOverflow()+(getHeight()==null?"":";height:"+getHeight())+"'>";
        html+="<div style='text-align:center;background-color:"+titleBgColor+";color:"+titleColor+";font-weight:bold'>"+getTitle()+"</div>";
        html+=renderBoxContent(options);
        html+="</div>";
        
            
        return html;
    }

    public void customizeProperties()
    {
        properties.addProperty(JOTWidgetBaseProperties.PROP_BG_COLOR, new JOTWidgetProperty(JOTWidgetProperty.TYPE_TEXT,new String[0],"#FFFFFF"));
        properties.addProperty(JOTWidgetBaseProperties.PROP_BORDER_WIDTH, new JOTWidgetProperty(JOTWidgetProperty.TYPE_TEXT,new String[0],"2px"));
        properties.addProperty(JOTWidgetBaseProperties.PROP_WIDTH, new JOTWidgetProperty(JOTWidgetProperty.TYPE_TEXT,new String[0],getWidth()));
        properties.addProperty(JOTWidgetBaseProperties.PROP_BORDER_TYPE, new JOTWidgetProperty(JOTWidgetProperty.TYPE_TEXT,new String[0],JOTWidgetBaseProperties.VAL_BORDER_TYPE_SOLID));
    }

    //return box title;
    public abstract String getTitle();
    
    // returns the HTML code for the insides of the box.
    public abstract String renderBoxContent(Hashtable options);

    
    // can ovveride in subclass: either: hidden, scroll, auto, visible
    public String getOverflow(){return "hidden";}
    public String getWidth(){return  "350px";}
    public String getHeight(){return null;}
     
}
