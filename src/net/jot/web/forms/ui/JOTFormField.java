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
Generic form field, use subclasses instead such as JOTFormTextField
@author thibautc
*/
public abstract class JOTFormField
{

  protected String name = "";
  protected String description = "";
  protected int type = JOTFormConst.UNDEFINED;
  protected String defaultValue = "";
  /**
	 * If set to false, the save() method will NOT save this field
	 * Use to have a form field that you don't want to save (or not as part of the save() method at least)
	 */
  protected boolean saveAutomatically = true;
  /**
	 * set this String to some (html format)text to be chown when help about this formElement is requested.
         * If not null, a "help" icon will show next to the field (for generated forms), which when clicked shows the help text.
	 */
  private String help = null;

  public String getHelp()
  {
    return help;
  }

  public void setHelp(String help)
  {
    this.help = help;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

/**
The form value if non exists yet
@param defaultValue 
*/
  public void setDefaultValue(String defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int fieldType)
  {
    this.type = fieldType;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public boolean isSaveAutomatically()
  {
    return saveAutomatically;
  }

  /**
If false, then that particular field won't be saved.
@param saveAutomatically 
*/
  public void setSaveAutomatically(boolean saveAutomatically)
  {
    this.saveAutomatically = saveAutomatically;
  }
}
