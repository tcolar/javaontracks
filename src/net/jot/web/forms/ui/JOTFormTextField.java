/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms.ui;

import net.jot.web.forms.JOTFormConst;

/**
Html text field
@author thibautc
*/
public class JOTFormTextField extends JOTFormField
{
	protected int size=10;
	
	public JOTFormTextField(String name, String description, int size, String defaultValue)
	{
		setType(JOTFormConst.INPUT_TEXT);
		setName(name);
		setDescription(description);
		setDefaultValue(defaultValue);
		this.size=size;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}
}
