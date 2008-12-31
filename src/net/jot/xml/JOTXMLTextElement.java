/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.xml;

/**
 * Represent "non-data" elements in the xml file (ex: empty lines, comments etc...)
 * @author thibautc
 */
public class JOTXMLTextElement {
    private String text=null;

    public JOTXMLTextElement(String content)
    {
        text=content;
    }
}
