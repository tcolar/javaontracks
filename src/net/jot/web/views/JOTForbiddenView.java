/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.views;

import net.jot.web.view.JOTView;

/**
 * Default forbidden error page using builtin template
 * @author thibautc
 */
public class JOTForbiddenView extends JOTView
{
	public void prepareViewData() throws Exception
	{	
		setBuiltinTemplate(JOTBuiltinTemplates.FORBIDDEN_TEMPLATE);
	}

	public boolean validatePermissions()
	{
		return true;
	}
}
