/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.web.view.JOTViewTag;

/**
 * A form element is basically equivalent to an HTML form field.
 * It stores that field data (ie: value, type etc..)
 * @author thibautc
 *
 */
public class JOTFormElement extends JOTViewTag
{
	private int type=JOTFormConst.UNDEFINED;

        /**
        Some Elements (ie select) might have subelements (ie: option)
        */
	private Hashtable subElements=new Hashtable();
	
	/**
	 * Creates a formelement of the given type
         * ie: type=JOTFormConst.INPUT_TEXT
	 * @param elementType
	 */
	public JOTFormElement(int elementType)
	{
		type=elementType;
		switch(type)
		{
		case JOTFormConst.INPUT_TEXT:
			setTagProperty("type", "text");
			break;
		case JOTFormConst.INPUT_HIDDEN:
			setTagProperty("type", "hidden");
			break;
		case JOTFormConst.INPUT_BUTTON:
			setTagProperty("type", "button");
			break;
		case JOTFormConst.INPUT_CHECKBOX:
			setTagProperty("type", "checkbox");
			break;
		case JOTFormConst.INPUT_IMAGE:
			setTagProperty("type", "image");
			break;
		case JOTFormConst.INPUT_PASSWORD:
			setTagProperty("type", "password");
			break;
		case JOTFormConst.INPUT_RADIO:
			setTagProperty("type", "radio");
			break;
		case JOTFormConst.INPUT_SUBMIT:
			setTagProperty("type", "submit");
			break;
		case JOTFormConst.INPUT_RESET:
			setTagProperty("type", "reset");
			break;
		}
	}
	
	/**
	 * Easy way to set the value of an element
	 * for example for most fields it's the value="" property
	 * but for a textarea it will be the content
	 * and for a checkbox a SELECTED flag etc...
	 * @param htmlValue
	 */
	public void setValue(String htmlValue)
	{
		JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, this, "HTML value:"+htmlValue);
		switch(type)
		{
		case JOTFormConst.TEXTAREA:
			setContent(htmlValue);
			break;
		case JOTFormConst.SELECT:
                        Hashtable subs=getSubElements();
                        String[] values=htmlValue.split(",");
                        Vector list= new Vector(Arrays.asList(values));
                        Enumeration e=subs.keys();
                        while(e.hasMoreElements())
                        {
                            String key=(String)e.nextElement();
                            JOTFormElement el=(JOTFormElement)subs.get(key);
                            if(list.contains(key))
                            {
                                el.setFlag(JOTFormConst.VALUE_SELECTED);
                            }
                            else
                            {
                                el.removeFlag(JOTFormConst.VALUE_SELECTED);                                
                            }
                        }
			//setContent(htmlValue);
			break;
		case JOTFormConst.INPUT_CHECKBOX:
			if(htmlValue.equalsIgnoreCase("on") || htmlValue.equalsIgnoreCase(JOTFormConst.VALUE_CHECKED))
				setFlag(JOTFormConst.VALUE_CHECKED);
			else
				removeFlag(JOTFormConst.VALUE_CHECKED);
			break;
		case JOTFormConst.INPUT_RADIO:
			if(htmlValue.equalsIgnoreCase("on") || htmlValue.equalsIgnoreCase(JOTFormConst.VALUE_CHECKED))
				setFlag(JOTFormConst.VALUE_CHECKED);
			else
				removeFlag(JOTFormConst.VALUE_CHECKED);
			break;
		default:
			setTagProperty("value", htmlValue);
		}		
	}
	
	/**
	 * Shortcut to get the value of a field, see setValue()
	 * @return
	 */
	public String getValue()
	{
		String value=null;
		switch(type)
		{
		case JOTFormConst.TEXTAREA:
			value=getContent();
			break;
		case JOTFormConst.SELECT:
			Hashtable subs=getSubElements();
                        Enumeration keys=subs.keys();
                        while(keys.hasMoreElements())
                        {
                            String key=(String)keys.nextElement();
                            JOTFormElement option=(JOTFormElement)subs.get(key);
                            Boolean val2=(Boolean)option.getFlags().get(JOTFormConst.VALUE_SELECTED);
			    if(val2!=null && val2==Boolean.TRUE)
                            {
                                if(value==null)
                                    value="";
                                value+=key+",";
                            }
                        }
                        if(value!=null && value.endsWith(","))
                            value=value.substring(0,value.length()-1);
			break;
		case JOTFormConst.INPUT_CHECKBOX:
			Boolean val=(Boolean)getFlags().get(JOTFormConst.VALUE_CHECKED);
			if(val!=null && val==Boolean.TRUE)
				value=JOTFormConst.VALUE_CHECKED;
			else
				value=JOTFormConst.VALUE_UNCHECKED;
			break;
		case JOTFormConst.INPUT_RADIO:
			val=(Boolean)getFlags().get(JOTFormConst.VALUE_CHECKED);
			if(val!=null && val==Boolean.TRUE)
				value=JOTFormConst.VALUE_CHECKED;
			else
				value=JOTFormConst.VALUE_UNCHECKED;
			break;
		default:
			value=(String)getTagProperties().get("value");
		}	
		return value;
	}
	
	public void set(String htmlId, JOTFormElement element)
	{
		subElements.put(htmlId, element);
	}

	public JOTFormElement get(int type, String name)
	{
		JOTFormElement el=(JOTFormElement)subElements.get(name);
		if(el==null)
		{
			el=new JOTFormElement(type);
			subElements.put(name,el);
		}
		return el;
	}
	
	public int getType() 
	{
		return type;
	}

	/**
	 * Manually set/update an element type
	 * @param type
	 */
	public void setType(int type) 
	{
		this.type = type;
	}

	public Hashtable getSubElements() {
		return subElements;
	}

}
