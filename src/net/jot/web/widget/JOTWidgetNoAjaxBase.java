/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */
package net.jot.web.widget;

import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.jot.web.JOTFlowRequest;
import net.jot.web.view.JOTView;

/**
 * Widget Base but without Ajaxa support / javascript
 * @author thibautc
 */
public abstract class JOTWidgetNoAjaxBase extends JOTWidgetBase
{

    // Disabling ajax
    public boolean isAjaxEnabled()
    {
        return false;
    }
    
    public String getAjaxCallbackJavascript()
    {
        // not ajax enabled
        return "";
    }

    public Hashtable widgetAjaxCall(JOTFlowRequest request)
    {
        // not ajax enabled
        return null;
    }

    public String getAjaxAction()
    {
        // not ajax enabled
        return null;
    }
    
}
