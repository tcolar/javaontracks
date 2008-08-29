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
Password type field
@author thibautc
*/
public class JOTFormPasswordField extends JOTFormTextField
{
	public JOTFormPasswordField(String name, String description, int size, String defaultValue)
	{
		super(name, description, size, defaultValue);
		setType(JOTFormConst.INPUT_PASSWORD);
	}
}
