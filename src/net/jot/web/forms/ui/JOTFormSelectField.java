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
Html Select
@author thibautc
*/
public class JOTFormSelectField extends JOTFormField
{

  protected int size = 1;
  protected String[] possibleValues =  {};
  protected String[] possibleDescriptions =  {};

  protected boolean multiples = false;
        /**

@param name 
@param description 
@param size 
@param possibleValues list of value(s) (option) for the select to show in the html
@param defaultValues  list of value(s) should be selected/highlighted.
*/
public JOTFormSelectField(String name, String description, int size, String[] possibleValues, String[] defaultValues)
  {
    this(name, description, size, possibleValues, possibleValues, defaultValues);
  }

  /**
         * 
         * @param name
         * @param description
         * @param size
         * @param possibleValues list of value(s) (option) for the select to show in the html
         * @param possibleDescriptions the name/description to be chown in the select box.
         * @param defaultValues  list of value(s) should be selected/highlighted.
         */
  public JOTFormSelectField(String name, String description, int size, String[] possibleValues, String[] possibleDescriptions, String[] defaultValues)
  {
    setType(JOTFormConst.SELECT);
    setName(name);
    setDescription(description);
    this.size = size;
    this.possibleValues = possibleValues;
    this.possibleDescriptions = possibleDescriptions;
    if (defaultValues != null)
    {
      this.defaultValue = "";
      for (int i = 0; i != defaultValues.length; i++)
      {
        defaultValue += (i == 0 ? "" : ",") + defaultValues[i];
      }
    }
  }

  public String[] getPossibleDescriptions()
  {
    return possibleDescriptions;
  }

  public String[] getPossibleValues()
  {
    return possibleValues;
  }

  public int getSize()
  {
    return size;
  }

  public void setAllowMultiples(boolean b)
  {
      multiples=b;
  }
  
  public boolean getAllowMultiples()
  {
      return multiples;
  }
}
