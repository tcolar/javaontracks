/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */
package net.jot.web.widget.pagebuilder;

import net.jot.web.widget.*;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.web.JOTFlowClassCache;
import net.jot.web.JOTFlowRequest;
import net.jot.web.ajax.JOTAjaxHelper;

/**
 *
 * @author thibautc
 */
public class JOTPagePreview extends JOTWidgetBase
{

    public static final String WIDGET_PAGE_ELEMENTS = "WIDGET_PAGE_ELEMENTS";

    public String renderWidget(Hashtable options)
    {
        // on page load init the elements .. for now flushing them
        session.removeAttribute(WIDGET_PAGE_ELEMENTS);

        String html = "";
        for (int i = 0; i != 7; i++)
        {
            for (int j = 0; j != 10; j++)
            {
                html += "<div class='grid' style='top:" + (j * 100) + "px;left:" + (i * 100) + "px'>"+(i==0?""+j*4:"")+" "+(j==0 && i!=0?""+i*4:"")+"</div>";
            }
        }
        html += "<div id='page_preview_" + uniqueId + "' style='position:relative;top:0px;left:0px;'>";
        html += renderInnerHtml();
        html += "</div>";
        return html;
    }

    public String renderInnerHtml()
    {
        String html = "";
        try
        {
        Vector elements = (Vector) session.getAttribute(WIDGET_PAGE_ELEMENTS);
        for (int i = 0; elements != null && i != elements.size(); i++)
        {
            JOTWidgetBase widget = (JOTWidgetBase)elements.get(i);
            widget.init(view);
            //String height=widget.getProperties().getProperty(JOTWidgetBaseProperties.PROP_HEIGHT).getDefaultValue();
            //String width=widget.getProperties().getProperty(JOTWidgetBaseProperties.PROP_WIDTH).getDefaultValue();
            //String bgcolor=widget.getProperties().getProperty(JOTWidgetBaseProperties.PROP_BG_COLOR).getDefaultValue();
            // TODO : get widget from hash
            html += "<div style='float:left;background-color:#"+getRandomColor()+"'>";
            html += widget.render(null);
            html += "</div>";
        }
        }
        catch(Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error rendering widget page preview pane.", e);
        }
        return html;
    }

    public String getWidgetList(int selectedId)
    {
        String html = "\n";
        Vector elements = (Vector) session.getAttribute(WIDGET_PAGE_ELEMENTS);
        for (int i = 0; elements != null && i != elements.size(); i++)
        {
            JOTWidgetBase widget = (JOTWidgetBase) elements.get(i);
            html += "<option value='"+i+"' "+(i==selectedId?"SELECTED":"")+">" + widget.getShortName() + "\n";
        }
        return html;
    }

    public boolean validatePermissions()
    {
        // no permissions needed.
        return true;
    }

    public String getAjaxCallbackJavascript()
    {
        // updating preview
        String js = JOTAjaxHelper.setJscriptVarValueFromAjaxVar("newHtml", "newHtml", uniqueId);
        js += JOTAjaxHelper.setElementInnerValue("page_preview", uniqueId, "newHtml");
        // updating widgets select box
        js += JOTAjaxHelper.setJscriptVarValueFromAjaxVar("widget_select", "widget_select", uniqueId);
        js += JOTAjaxHelper.setElementInnerValue("widget_select", -1, "widget_select");
        // updating widgets props box
        js += JOTAjaxHelper.setJscriptVarValueFromAjaxVar("props", "props", uniqueId);
        js += JOTAjaxHelper.setElementInnerValue("props", -1, "props");
        return JOTAjaxHelper.buildStandardCallbackJscript(js, uniqueId);
    }

    public String getAjaxAction()
    {
        return "pagebuilderajax.do";
    }

    public Hashtable widgetAjaxCall(JOTFlowRequest request)
    {
        Hashtable hash = new Hashtable();
        Vector elements = (Vector) session.getAttribute(WIDGET_PAGE_ELEMENTS);
        if (elements == null)
        {
            elements = new Vector();
        }

        String action = request.getParameter("action");
        if (action == null)
        {
            action = "";
        }

        else if(action.equalsIgnoreCase("Down"))
        {
           int id=new Integer(request.getParameterValues("widget_select")[0]).intValue();
           if(id < elements.size()-1)
           {
            // swapping with object before
            Object obj1=elements.get(id);
            Object obj2=elements.get(id+1);
            elements.set(id+1,obj1);
            elements.set(id,obj2);
            session.setAttribute(WIDGET_PAGE_ELEMENTS, elements);

            // Updating html
            hash.put("newHtml", renderInnerHtml());
            hash.put("widget_select", getWidgetList(id+1));
           } 
        }
        else if(action.equalsIgnoreCase("Up"))
        {
           int id=new Integer(request.getParameterValues("widget_select")[0]).intValue();
           if(id>=1)
           {
            // swapping with object before
            Object obj1=elements.get(id);
            Object obj2=elements.get(id-1);
            elements.set(id-1,obj1);
            elements.set(id,obj2);
            session.setAttribute(WIDGET_PAGE_ELEMENTS, elements);

            // Updating html
            hash.put("newHtml", renderInnerHtml());
            hash.put("widget_select", getWidgetList(id-1));
           } 
        }
        else if(action.equalsIgnoreCase("Remove"))
        {
            int id=new Integer(request.getParameterValues("widget_select")[0]).intValue();
                
            // adding new object
            elements.remove(id);
            session.setAttribute(WIDGET_PAGE_ELEMENTS, elements);

            // Updating html
            hash.put("newHtml", renderInnerHtml());
            hash.put("widget_select", getWidgetList(-1));
        }
        else if (action.equalsIgnoreCase("Add"))
        {
            JOTWidgetBase widget=getWidgetInstance("net.jot.web.widget.JOTExampleWidget");
            if(widget==null)
            {
                hash.put("props", "");                
            }
            else
            {
                hash.put("props", widget.renderPropertiesScreen(-1));
            }
            hash.put("newHtml", renderInnerHtml());
            hash.put("widget_select", getWidgetList(elements.size()-1));
        }
        
        else if (action.equalsIgnoreCase("Save"))
        {
            // Saving / Updating elements properties
            //int id=new Integer(request.getParameter("id")).intValue();
            JOTWidgetBase widget=getWidgetInstance("net.jot.web.widget.JOTExampleWidget");
            widget.parseProperties(request);
            elements.add(widget);
            session.setAttribute(WIDGET_PAGE_ELEMENTS, elements);
            hash.put("newHtml", renderInnerHtml());
            hash.put("widget_select", getWidgetList(elements.size()-1));
            hash.put("props", "");                
        }
        return hash;
    }

    public JOTWidgetBase getWidgetInstance(String widgetClass)
    {
        JOTWidgetBase widget=null;
        try
        {
            Class c=JOTFlowClassCache.getClass(widgetClass);
            widget=(JOTWidgetBase)c.newInstance();
            //widget.init(getMaster());
        }
        catch(Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed to instantiate widget: "+widgetClass, e);
        }
        return widget;
    }
    
    private String getRandomColor()
    {
        Random r = new Random();
        String color = "";
        for (int i = 0; i != 3; i++)
        {
            color += Integer.toHexString((Math.abs(r.nextInt()) % 150) + 16);
        }
        return color;
    }

    public JOTWidgetBaseProperties getWidgetProperties()
    {
        return new JOTWidgetBaseProperties();
    }

    public void customizeProperties()
    {
    }

    public String getShortName()
    {
       return "JOT Page Preview";
    }
}
