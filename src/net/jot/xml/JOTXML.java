/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * Does not deal with "entity" (ignored)
 * @author thibautc
 */
public class JOTXML extends JOTXMLElement
{

    /**
     * The index in "items" of the XML root element
     */
    private int rootIndex = -1;

    /**
     * Create the XML object from the given raw xml(text)
     * You can get this xml from a file using (JOTXML.readXML())
     * @param xml
     * @throws net.jot.xml.JOTXMLException
     */
    public JOTXML(StringBuffer xml) throws JOTXMLException
    {
        items=new Vector();
    }

    /**
     * Create a new xml file with the given root element.
     */
    public JOTXML(JOTXMLElement rootElement)
    {
        items=new Vector();
        items.add(rootElement);
        rootIndex = 0;
    }

    public JOTXMLElement getRoot()
    {
        return (JOTXMLElement) items.get(rootIndex);
    }

    public static StringBuffer readXmlFrom(InputStream stream)
    {
        //TODO: TBD
        return null;
    }
    public static StringBuffer readXmlFrom(File f)
    {
        //TODO: TBD
        return null;
    }

    public void writeTo(OutputStream out) throws IOException
    {
        //TBD
    }
    public void writeTo(File out) throws IOException
    {
        FileOutputStream o =new FileOutputStream(out);
        try
        {
            writeTo(out);
        }
        catch(IOException e)
        {
            throw(e);
        }
        finally
        {
            o.close();
        }
    }
}
