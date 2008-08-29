/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */
package net.jot.web.widget;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import net.jot.logger.JOTLogger;
import net.jot.web.JOTFlowRequest;
import net.jot.web.ajax.JOTAjaxHelper;
import net.jot.web.ajax.JOTAjaxProvider;
import net.jot.web.ctrl.JOTController;

/**
 * Widget Base (Ajax enabled).
 * @author thibautc
 */
public abstract class JOTWidgetBase  extends JOTController implements JOTAjaxProvider
{

    protected static final Pattern ARG_PATTERN = Pattern.compile("\\s*(\\S+)\\s*=\\s*'([^']*)'\\s*");
    /*
     * If the same widget is added several times to the same view (render called several time on this object)
     * id will be incremented each time, to ensure unique function names etc...
     */
    public int uniqueId = 0;
    public JOTWidgetBaseProperties properties = new JOTWidgetBaseProperties();

    /**
     * Standard widdget constructor, calls customizeProperties
     */
    public JOTWidgetBase()
    {
        super();
        customizeProperties();
    }

    public void parseProperties(JOTFlowRequest request)
    {
        JOTWidgetBaseProperties props = getProperties();
        Enumeration e = props.getAllProperties().keys();
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            if (request.getParameter(key) != null)
            {
                props.updatePropertyDefaultValue(key, request.getParameter(key));
            }
        }
    }

    /**
     * Will be called by jot, when a widget is found in a view.
     * @param options
     * @return
     */
    public String render(String args)
    {
        int refreshEvery = properties.getRefreshEvery();
        uniqueId = getNextUniqueId();
        Hashtable parsedArgs = parseArgs(args);
        String widgetHtml = renderWidget(parsedArgs);

        String html = "";
        html += "\n<!-- ---- Begin widget: " + getClass().getName() + " : " + uniqueId + "---- -->\n";
        html += "<div style='float:left;'>\n";
        if (isAjaxEnabled())
        {
            html += "<script language='javascript'>\n";
            // Add the ajax calling method.
            html += JOTAjaxHelper.getAjaxJavascript(this, uniqueId);
            // Add widget ajax callback method
            html += "\n// AJAX Callback method";
            html += "\nfunction " + getJscriptCallbackFunctionName() + "(){";
            html += getAjaxCallbackJavascript();
            html += "}\n";
            if (refreshEvery != -1)
            {
                //we will call it (almost) right away
                html += "setTimeout('" + getJscriptFuncName() + "()',100);\n";
                // and then at every "refreshEvery" seconds intervals. 
                html += "setInterval('" + getJscriptFuncName() + "()'," + refreshEvery * 1000 + ");\n";
            }
            html += "</script>\n";
        }
        // get widget HTML code
        html += widgetHtml;
        html += "\n</div>\n<!-- ---- End widget: " + getClass().getName() + " : " + uniqueId + "---- -->\n";
        return html;
    }

    public void executeAjaxCall(JOTFlowRequest request, HttpServletResponse response) throws Exception
    {
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        Hashtable hash = widgetAjaxCall(request);
        Enumeration e = hash.keys();
        String xml = "<root>";
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            String value = (String) hash.get(key);
            xml += "<" + key + "><![CDATA[" + value + "]]></" + key + ">";
        }
        xml += "</root>";
        response.setContentLength(xml.length());
        response.getWriter().write(xml);
        response.flushBuffer();
    }

    public String getJscriptCallbackFunctionName()
    {
        return "jot_widget_callback_" + getClass().getSimpleName() + "_" + uniqueId;
    }

    public String getJscriptFuncName()
    {
        return JOTAjaxHelper.getStandardAjaxJscriptFuncName(this, uniqueId);
    }

    /**
     * Can be ovveriden to disable ajax
     * @return
     */
    public boolean isAjaxEnabled()
    {
        return true;
    }

    /**
     * Renders the HTML code content for the widget
     * @param options
     * @return
     */
    public abstract String renderWidget(Hashtable args);

    /**
     * javascript code (without javascript tags) to be run when Ajax call returns.
     * IE: code to update the HTML view of the data.
     * **Note**: be careful to use the uniqueId when retrieving the XMLHttpRequest element
     * XMLHttpRequest variablke name: (req_"+uniqueId)
     * see JOTExampleWidget for example 
     * @return
     */
    public abstract String getAjaxCallbackJavascript();

    /**
     * Parse the args String
     * @param options
     * @return
     */
    protected Hashtable parseArgs(String args)
    {
        Hashtable hash = new Hashtable();
        if (args != null)
        {
            String[] elems = args.split(",");
            for (int i = 0; i != elems.length; i++)
            {
                String arg = elems[i];
                Matcher m = ARG_PATTERN.matcher(arg);
                if (m.matches())
                {
                    String key = m.group(1);
                    String value = decodeCommas(m.group(2));
                    hash.put(key, value);
                } else
                {
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Couldn't parse Widget arg(skipping): " + arg);
                }
            }
        }
        return hash;
    }

    /**
     * When passing arguments to a widget they are comma separated, do if an arg value contains a comma
     * this causes problems, so use encodeCommas(argValue) to encode the commas in your args value.
     * @param value
     * @return
     */
    public static String encodeCommas(String value)
    {
        return value.replaceAll(",", "[[COMMA]]");
    }
    
    /**
     * Put the commas back after parsing the args.
     * @param value
     * @return
     */
    public static String decodeCommas(String value)
    {
        return value.replaceAll("\\[\\[COMMA\\]\\]",",");      
    }
    
    /**
     * Process the ajax call and return updated data (key/value) to be processed by the callbackJavacsript
     * @param request
     * @return
     */
    public abstract Hashtable widgetAjaxCall(JOTFlowRequest request);

    /**
     * Will be called when the ajax call is made.
     * @return
     * @throws java.lang.Exception
     */
    public String process() throws Exception
    {
        executeAjaxCall(request, response);
        return RESULT_SUCCESS;
    }

    public int getId()
    {
        return uniqueId;
    }

    /**
     * We want each widget in the "page" to have a unique id, to avoid issues with same widget several time in same page
     * So we create a unique id in the request scope which shoudl provide what we wat here.
     * @return
     */
    protected synchronized int getNextUniqueId()
    {
        Integer id = (Integer) request.getAttribute("JOT_WIDGET_ID");
        if (id != null)
        {
            id = new Integer(id.intValue() + 1);
        } else
        {
            id = new Integer(1);
        }
        request.setAttribute("JOT_WIDGET_ID", id);
        return id.intValue();
    }

    /** 
     * Add/Update widget options to this in customizeProperties()
     * Ex:  properties.addProperty(PROP_BG_COLOR,new JOTWidgetProperty(JOTWidgetProperty.TYPE_TEXT,null,"#FF0000"));
     */
    public abstract void customizeProperties();

    /**
     * Returns a shortname for your widget (<25 chars)
     * @return
     */
    public abstract String getShortName();

    public void dumpWidgetProperties()
    {
        System.out.println("Properties for widget:");
        Enumeration e = properties.getAllProperties().keys();
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            JOTWidgetProperty prop = properties.getProperty(key);
            System.out.println("\t" + key + " -> " + prop.getDefaultValue());
        }
    }

    public String renderPropertiesScreen(int id)
    {
        String html = "<b/>" + getShortName() + "(" + id + ")</b><hr/><form id='propsform' action='pagebuilderajax.do'><input type='hidden' name='id' value='" + id + "'>";
        Hashtable props = properties.getAllProperties();
        Enumeration e = props.keys();
        while (e.hasMoreElements())
        {
            String key = (String) e.nextElement();
            JOTWidgetProperty prop = (JOTWidgetProperty) props.get(key);
            html += "" + key + ":";
            if (prop.getType() == JOTWidgetProperty.TYPE_TEXT)
            {
                html += "<input name='" + key + "' type='text' size='10' value='" + prop.getDefaultValue() + "'><br/>";
            } else if (prop.getType() == JOTWidgetProperty.TYPE_CHECKBOX)
            {
                html += "<input name='" + key + "' type='checkbox' " + (prop.getDefaultValue().equals(JOTWidgetProperty.CHECKBOX_TRUE) ? "SELECTED" : "") + "><br/>";
            } else if (prop.getType() == JOTWidgetProperty.TYPE_SELECT)
            {
                html += "<select name='" + key + "'>";
                String[] values = prop.getPossibleValues();
                for (int i = 0; i != values.length; i++)
                {
                    html += "<option value='" + values[i] + "' " + (values[i].equals(prop.getDefaultValue()) ? "SELECTED" : "") + ">" + values[i] + "\n";
                }
                html += "</select>";
            }
            html += "&nbsp;&nbsp;&nbsp;";
        }
        html += "<br/><input type='button' name='' value='Close' onclick=\"document.getElementById('props').innerHTML='Properties';\">";
        html += "<input type='button' name='action' value='Save' onclick=\"jot_ajax_JOTPagePreview_1_form('propsform');\"></form>";
        return html;
    }

    public JOTWidgetBaseProperties getProperties()
    {
        return properties;
    }

}
