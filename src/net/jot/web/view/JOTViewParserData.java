/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.view;

import java.util.Hashtable;

/**
 * Lightweight interface through which the view parser data can be retrieved
 * @author thibautc
 */
public interface JOTViewParserData {

    /**
     * Custom JOT element block (can be added/removed easily)
     * @return
     */
    public Hashtable getBlocks();

    /**
     * Custom tags added to a standrard HTML element
     * @return
     */
    public Hashtable getTags();

    /**
     * This returns a Hashtable of variables to use in the template
     * This is the most commonly use way to store data
     * @return
     */
    public Hashtable getVariables();

    /**
     * Any custom forms
     * @return
     */
    public Hashtable getForms();

    /**
     * Return the full view object (JOTView)
     * or null if not available (Ex: JOTLightweightView)
     * @return
     */
    public JOTView getFullView();


}
