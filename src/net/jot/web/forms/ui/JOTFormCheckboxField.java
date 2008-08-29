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
A Form checkbox
@author thibautc
*/
public class JOTFormCheckboxField extends JOTFormField
{

	public JOTFormCheckboxField(String name, String description, boolean checked)
	{
		setType(JOTFormConst.INPUT_CHECKBOX);
		setName(name);
		setDefaultValue(""+checked);
		setDescription(description);
	}
        //ovveride
        public void setDefaultValue(String defaultValue)
        {
            if(defaultValue.equalsIgnoreCase("checked"))
                defaultValue="true";
            if(defaultValue.equalsIgnoreCase("unchecked"))
                defaultValue="false";
            this.defaultValue = defaultValue;
        }
	
}
