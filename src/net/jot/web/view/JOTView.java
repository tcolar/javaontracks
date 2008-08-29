/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.view;

import java.util.Hashtable;

import net.jot.web.ctrl.JOTController;
import net.jot.web.forms.JOTForm;

/**
 * Abstract Base of a View (provides an empty view) 
 * The View is where you add variables to be used by the view.
 * A view is responsible for providing a set of variable to be merged with a template.
 * @author thibautc
 *
 */
public abstract class JOTView extends JOTController
{
	private boolean provideRequestParameters=false;
	private Hashtable variables=new Hashtable();
	private Hashtable blocks=new Hashtable();
	private Hashtable tags=new Hashtable();
	private Hashtable forms=new Hashtable();
	private String bTemplate=null;
	
        /**
         * You can call this method to use a "hardcode" template rather tha use one loaded from a template as usual.
         * @param template
         */
	public void setBuiltinTemplate(String template)
	{
		bTemplate = template;
	}

	/**
	 * Adds a variable to this View
         * Then you can use the varibale in the view:
         * Ex: jot:var value="id"
	 * @param name
	 * @param value
	 */
	public void addVariable(String id, Object value)
	{
		variables.put(id,value);
	}

	/**
	 * Adds a Form to this View. The form can then be used/rendered in the template.
	 * @param name
	 * @param value
	 */
	protected void addForm(JOTForm form)
	{
		forms.put(form.getClass().getName(),form);
	}
	/**
	 * Defines a page block to be "defined" on the fly at runtime
	 * For example <jot:block dataId="toto">blah</jot:block> will be replaced by the data provided
	 * in the view element with dataId "toto" if it exists.
         * You can also use blocks to easily show/hide whole html parts
	 * @param dataId
	 * @param element
	 */
	protected void addBlock(String id,JOTViewBlock element)
	{
		blocks.put(id, element);
	}

	/**
	 * Defines an HTML tag to be "redefined" on the fly at runtime
	 * For example <div jotid="toto"></div>, content will be replaced by the data provided
	 * in the view element with dataId "toto" if it exists.
	 * @param dataId
	 * @param element
	 */
	protected void addTag(String id,JOTViewBlock element)
	{
		tags.put(id, element);
	}
	
	/**
	 * Wether to automatically provide all the request parameters & attributes to the view
         * TODO: is that implemented ?
	 * Defaults to false.
	 * @param b
	 */
	protected void setProvideRequestParameters(boolean b)
	{
		provideRequestParameters=b;
	}
	
	
	public final String process() throws Exception
	{
		prepareViewData();
		return RESULT_SUCCESS;
	}
	
	
	/**
	 * To be implemented by subclass
	 * Loads the View data by calling add* etc ...
	 * @throws Exception
	 */
	public abstract void prepareViewData() throws Exception;

	public Hashtable getBlocks() {
		return blocks;
	}
	
	public Hashtable getTags() {
		return tags;
	}

	public boolean isProvideRequestParameters() {
		return provideRequestParameters;
	}

	public Hashtable getVariables() {
		return variables;
	}
	
	public Hashtable getForms()
	{
		return forms;
	}

	public String getBuiltinTemplate()
	{
		return bTemplate;
	}
	

}
