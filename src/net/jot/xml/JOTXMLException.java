/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.xml;

/**
 *
 * @author thibautc
 */
class JOTXMLException extends Exception {

    public JOTXMLException(String title)
    {
        super(title);
    }
    public JOTXMLException(String title, Throwable cause)
    {
        super(title, cause);
    }
}
