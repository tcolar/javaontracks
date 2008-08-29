/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.views;

import net.jot.web.view.*;

/**
 * Provides a plain, empty View.
 * Not much use besides a temporary place holder.
 * @author thibautc
 *
 */
public class JOTLazyView extends JOTView
{

	protected void getViewData() throws Exception
	{
		prepareViewData();
	}

	/**
	 * Just override in subclass if you want to add more things to the view
	 */
	public void prepareViewData() throws Exception
	{
	}

	public boolean validatePermissions()
	{
		return true;
	}

}
