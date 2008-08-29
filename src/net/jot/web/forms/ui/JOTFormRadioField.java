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
RadioButton type field
Note than "radioName" is the name of the radio button in html(used to group radios together):
Ex input type="radio" name="radioName"
@author thibautc
*/
public class JOTFormRadioField extends JOTFormField
{
	// ENABLED / ON
	protected String radioName="";

	public JOTFormRadioField(String name, String description, String radioName, String defaultValue)
	{
		setType(JOTFormConst.INPUT_RADIO);
		setName(name);
		setDescription(description);
		setDefaultValue(defaultValue);
		this.radioName=radioName;
	}
}
