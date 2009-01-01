/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.xml;

import java.util.Vector;

/**
 *
 * @author thibautc
 */
public class JOTXMLElement {

    private String name=null;
    private String value=null;

    public JOTXMLElement(String name)
    {
        this.name=name;
    }

    public String getName()
    {
        return name;
    }

    /**
     * ordered list of items found in the page
     * such as root xml element, definition, comments etc...
     * Types: JOTXMLElement, JOTXMLTextElement
     */
    protected Vector items=new Vector();

    /**
     * Return this element sub-elements (childs)
     */
    public JOTXMLElement[] getChilds()
    {
        return null;
    }

    /**
     * Return all the items included in this element
     * (including comments, spaces etc...)
     */
    public Vector getAllItems()
    {
        return items;
    }

    protected void addItem(Object item)
    {
        items.add(item);
    }

    void setValue(String val)
    {
        value=val;
    }

    public String getValue()
    {
        return value;
    }

}
