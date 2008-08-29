/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms.ui;

/**
Form submit button
@author thibautc
*/
public class JOTFormSubmitButton
{
	protected String title="Sumit Form";

	public JOTFormSubmitButton(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}
	
}
