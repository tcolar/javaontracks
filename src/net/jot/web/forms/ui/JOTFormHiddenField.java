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
An "hidden" type text field.
@author thibautc
*/
public class JOTFormHiddenField extends JOTFormField
{
	public JOTFormHiddenField(String name, String defaultValue)
	{
		setType(JOTFormConst.INPUT_HIDDEN);
		setName(name);
		setDescription("");
		setDefaultValue(defaultValue);
	}

}
