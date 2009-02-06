/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.views;

import net.jot.JOTInitializer;
import net.jot.utils.JOTHTMLUtilities;
import net.jot.web.view.JOTView;

/**
 * Standard error page View, leave as-is to use the bultin template, or cuse with yor own template.
 * @author thibautc
 */
public class JOTErrorView extends JOTView
{
	public static final String EXCEPTION_ATTRIB="JOT_Exception";
	
	public void prepareViewData() throws Exception
	{	
		setBuiltinTemplate(JOTBuiltinTemplates.ERROR_TEMPLATE);
		
		addVariable("version",JOTInitializer.VERSION);
		Throwable t=(Throwable)request.getAttribute(EXCEPTION_ATTRIB);
        
		if(t!=null)
		{
			addVariable("title",htmlEncode(t.toString()));
			//JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTViewParser.class, "###getmessage:"+t.getMessage());
			addVariable("exception", t);
			if(t.getCause()!=null)
			{
				addVariable("cause", (Throwable)t.getCause());
				addVariable("causeTitle",htmlEncode(t.getCause().toString()));
			}
		}
	}

    public StackTraceElement[] getStackTrace(Throwable t)
    {
        return t.getStackTrace();
    }

	// Here we want html tags to be shown, not interpreted.
	public String htmlEncode(String s)
	{
		return JOTHTMLUtilities.textToHtml(s);
	}
	public String htmlEncode(StackTraceElement e)
	{
		return htmlEncode(e.toString());
	}

	public boolean validatePermissions()
	{
		return true;
	}
}
