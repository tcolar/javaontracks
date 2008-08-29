/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms;

import java.util.Properties;

import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.ui.JOTFormField;

/**
 * Generic class that provides easy mapping of a property file to a form
 * This allow for an easy "setup form" where as the setup options are stored in a property file.
 * Extend to map your own property file.
 * @author thibautc
 *
 */
public abstract class JOTPropertiesForm extends JOTGeneratedForm
{
	protected Properties props;
	
	public void addFormField(JOTFormField field)
	{
		String obj=props.getProperty(field.getName());
		if(obj!=null) field.setDefaultValue(obj);
		super.addFormField(field);
	}
	
        /**
Will be called when the form data changed (in the request / form submission)
To update the data values here.
@param request 
@throws java.lang.Exception 
*/
	public void refreshData(JOTFlowRequest request) throws Exception
	{
		updateProperties(request);
		super.refreshData(request);
	}
	
	/**
	 * Implement to set/load the properties.
         * This method should set the "props" variable (Properties object loaded from some property file)
         * 
	 * @param request
	 */
	public abstract void updateProperties(JOTFlowRequest request);
	/**
	 * Implement to save the properties back where they came from (your property file).
         * Note: save the "props" object to your property file.
	 * @throws Exception
	 */
	public abstract void saveProperties() throws Exception;
	
        /**
Call this to save the new form values to the property file 
This will save the values and then call your saveProperties implementation
@param request 
@throws java.lang.Exception 
*/
	public void save(JOTFlowRequest request) throws Exception
	{
		for(int i=0;i!=items.size();i++)
		{
			if(items.get(i) instanceof JOTFormField)
			{
				JOTFormField field=(JOTFormField)items.get(i);
				String name=field.getName();
				JOTFormElement el=get(name);
				if(el!=null && field.isSaveAutomatically())
				{
					Object value=el.getValue();
					props.setProperty(name, (String)value);
				}
			}
		}
		// then save the model
		saveProperties();
	}

}
