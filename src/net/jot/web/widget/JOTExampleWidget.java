/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget;

import java.util.Hashtable;
import net.jot.web.JOTFlowRequest;
import net.jot.web.ajax.JOTAjaxHelper;

/**
 * Example widget that shows the ammount of memory available on the server
 * Uses ajax to autorefresh
 * @author thibautc
 */
public class JOTExampleWidget extends JOTWidgetBase
{
    /*
     * <jot:widget class="net.jot.web.widget.JOTExampleWidget" width="28" style="7" refreshEvery="5"/> ...
    */
    
    public String renderWidget(Hashtable options)
    {
        int refreshEvery=properties.getRefreshEvery();
        String color=properties.getPropertyDefaultValue(JOTWidgetBaseProperties.PROP_BG_COLOR, options, "#ff0000");
        String ftSize=properties.getPropertyDefaultValue("fontSize", options, "12");
        
        String html="";
        html+="<span style='background-color:"+color+";font-size:"+ftSize+"' id='example_widget_"+uniqueId+"'>Mem: N/A %</span>";
        if(refreshEvery==-1)
        {
            // manual refresh button
            html+="<button onclick=\""+getJscriptFuncName()+"('');\">Update</button>";
        }
        html+="<br/><br/>";
        return html;
    }

    public boolean validatePermissions()
    {
        // no permissions needed.
        return true;
    }

    
    public String getAjaxCallbackJavascript()
    {
        String  js=JOTAjaxHelper.setJscriptVarValueFromAjaxVar("mem","mem",uniqueId);
        js +=      "mem = 'Mem: '+mem+'%';\n"; 
        js+=       JOTAjaxHelper.setElementInnerValue("example_widget",uniqueId,"mem");
        
        return JOTAjaxHelper.buildStandardCallbackJscript(js, uniqueId);
    }

    public String getAjaxAction()
    {
        return "examplewidget.do";
    }

    public Hashtable widgetAjaxCall(JOTFlowRequest request)
    {
        Runtime r = Runtime.getRuntime();
        float freemem=r.freeMemory();
        float totalmem= r.totalMemory();
        long memPct = (long)(freemem*100/totalmem);
        
        Hashtable hash=new Hashtable();
        hash.put("mem",""+memPct);
        
        return hash;
    }

    public JOTWidgetBaseProperties getWidgetProperties()
    {
        return new JOTWidgetBaseProperties();
    }

    public void customizeProperties()
    {
        properties.removeProperty(JOTWidgetBaseProperties.PROP_BORDER_TYPE);
        properties.removeProperty(JOTWidgetBaseProperties.PROP_BORDER_WIDTH);
        properties.addProperty("fontSize",new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT, new String[]{"5","6","7","8","12","15","20","30"},"12"));
    }

    public String getShortName()
    {
        return "JOT Example widget";
    }
}
