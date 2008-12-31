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

    /**
     * ordered list of items found in the page
     * such as root xml element, definition, comments etc...
     * Types: JOTXMLElement, JOTXMLTextElement
     */
    protected Vector items;

    /**
     * return this element attributes
     */
    public String[] getAttributes()
    {
        return null;
    }

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
}
