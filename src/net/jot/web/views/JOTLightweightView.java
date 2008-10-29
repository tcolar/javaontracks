/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.views;

import net.jot.web.view.*;
import java.util.Hashtable;
import net.jot.web.forms.JOTForm;

/**
 * This is a lightweight view object, which is decoupled from the webcontainer
 * whereas JOTVIew is tightly coupled.
 * This is mianly used to take advantage/use the  template mechanism/parser of JOT
 * without needing a webapp server running.
 * As in JOTVIew you can subclass this and add your own custom method and call them from the template
 * ex: getUserName(){} ->  in template: %lt;jot:var value="userName"/&gt;
 *
 * @author thibautc
 */
public abstract class JOTLightweightView implements JOTViewParserData
{

    private Hashtable variables = new Hashtable();
    private Hashtable blocks = new Hashtable();
    private Hashtable tags = new Hashtable();
    private Hashtable forms = new Hashtable();

    public Hashtable getBlocks()
    {
        return variables;
    }

    public Hashtable getTags()
    {
        return tags;
    }

    public Hashtable getVariables()
    {
        return variables;
    }

    public Hashtable getForms()
    {
        return forms;
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
        variables.put(id, value);
    }

    /**
     * Adds a Form to this View. The form can then be used/rendered in the template.
     * @param name
     * @param value
     */
    protected void addForm(JOTForm form)
    {
        forms.put(form.getClass().getName(), form);
    }

    /**
     * Defines a page block to be "defined" on the fly at runtime
     * For example <jot:block dataId="toto">blah</jot:block> will be replaced by the data provided
     * in the view element with dataId "toto" if it exists.
     * You can also use blocks to easily show/hide whole html parts
     * @param dataId
     * @param element
     */
    protected void addBlock(String id, JOTViewBlock element)
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
    protected void addTag(String id, JOTViewBlock element)
    {
        tags.put(id, element);
    }
    
    public JOTView getFullView()
    {
        // not avail for lightweight view
        return null;
    }
}
