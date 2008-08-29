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

/**
 * Represents an individual HTML tags
 * So we can "customize" it on the fly: ie: redefine/add a property (class="myclass") or a flag (DISABLED) etc...
 * @author thibautc
 */
public class JOTViewTag extends JOTViewBlock
{
	/**
	 *  HTML TAG property
	 *  ie: class=mycssclass   border=1   etc ...
	 */
	Hashtable properties=new Hashtable();
	/**
	 * HTML FLags
	 * ie: DISABLED    CHECKED etc ....
	 */
	Hashtable flags=new Hashtable();
	
	public Hashtable getTagProperties()
	{
		return properties;
	}
	
	public Hashtable getFlags()
	{
		return flags;
	}
	/**
	 * This allows to redefine (or add new) HTML tags to the block.
	 * For example say you have this block: <div class="default" jotid="block1"/>
	 * You have added the following htmlTags using setHtmlTags: ["class","css1"] ["border","2"]
	 * The htmnl output will look like this:
	 * <div class="css1" border="2"/>
	 */
	public void setTagProperty(String name, String value)
	{
		properties.put(name, value);
	}
	
	/**
	 * Unset a tag property, so that it will be left alone.
	 * @param name
	 * @param value
	 */
	public void unsetTagProperty(String name)
	{
		properties.remove(name);
	}

	/**
	 * Used to set a FLAG on an html element.
	 * A flag is a property that as no value: for example things like "DISABLED" or "CHECKED"
	 * @param flack
	 */
	public void setFlag(String flag)
	{
		flags.put(flag,Boolean.TRUE);
	}
	/**
	 * Remove a flag from the HTML (if found)
	 * @param flag
	 */
	public void removeFlag(String flag)
	{
		flags.put(flag,Boolean.FALSE);
	}
	
}
