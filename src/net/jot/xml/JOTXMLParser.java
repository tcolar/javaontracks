/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.xml;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jot.utils.Pair;
import net.jot.web.view.JOTViewParser;

/**
 * http://en.wikipedia.org/wiki/XML
 * http://en.wikipedia.org/wiki/CDATA#CDATA_sections_in_XML
 * TODO deal with <blah>toto<blah>
 * TODO deal with <blah name="5">toto</blah>
 * TODO deal with special chars: &amp etc...
 * TODO deal with numbers: &#38;&#x38;
 * TODO deal with CDATA (<![CDATA[   ...     ]]>)
 * etc...
 *
 *
 * Used by JOTXML to parse xml text into XML text/elements
 * Kept separated for clarity.
 * We completely ignore declarative stuff (and DTD etc...)
 * http://en.wikipedia.org/wiki/XML
 * @author thibautc
 */
public class JOTXMLParser
{

    protected static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE | Pattern.CANON_EQ;
    protected static final Pattern PATTERN_TAG = Pattern.compile("(<([^> ]*)[^>]*>)(.*)", PATTERN_FLAGS);
    protected static final Pattern OPEN_TAG_PATTERN = Pattern.compile("[^> ]*[^>]*>", PATTERN_FLAGS);
    protected static final Pattern PATTERN_COMMENT = Pattern.compile("<\\!--.*?-->", PATTERN_FLAGS);
    protected static final Pattern PATTERN_DECLARATION = Pattern.compile("<\\?.*?\\?>", PATTERN_FLAGS);
    private final static Pattern PATTERN_PIECES = Pattern.compile("__JOT_XML_PIECE_\\d+__");
    private final static Pattern PATTERN_ATTRIB = Pattern.compile("(\\S+)\\s*=['\"]+(.*?)['\"]+");

    private Hashtable pieces = new Hashtable();
    private Vector elements = new Vector();
    private boolean rootFound=false;

    private JOTXMLParser()
    {
    }

    protected static Vector parse(StringBuffer xmlBuf) throws JOTXMLException
    {
        JOTXMLParser parser = new JOTXMLParser();

        // remove comments
        xmlBuf = parser.doTextItem(PATTERN_COMMENT, xmlBuf);
        // and declarations (we ignore declarative stuff (and DTD etc...))
        xmlBuf = parser.doTextItem(PATTERN_DECLARATION, xmlBuf);

        String xml = xmlBuf.toString();

        parser.doTags(xml, null);

        // TODO: put back the comments etc...

        // TODO

        parser.dumpElements(parser.elements, "");

        return parser.elements;
    }

    private void addElement(JOTXMLElement parent, JOTXMLElement element)
    {
        rootFound=true;
        if (parent != null)
        {
            parent.addItem(element);
        } else
        {
            elements.add(element);
        }
    }

    private void addText(JOTXMLElement parent,String text)
    {
        if(text.length()>0)
        {
            JOTXMLTextElement element=new JOTXMLTextElement(reinsertPieces(text));
        if (parent != null)
        {
            parent.addItem(element);
        } else
        {
            elements.add(element);
        }
        }
    }

    private void doAttributes(JOTXMLElement parent, String openingTag)
    {
        Matcher m=PATTERN_ATTRIB.matcher(openingTag);
        while(m.find())
        {
            String name=m.group(1);
            String val=m.group(2);
            //TODO: numbers ?
            JOTXMLElement el=new JOTXMLElement(name);
            el.setValue(val);
            parent.addItem(el);
        }
    }

    private StringBuffer doTextItem(Pattern pattern, StringBuffer xml)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = pattern.matcher(xml);
        while (m.find())
        {
            String key = getNextKey();
            pieces.put(key, m.group(0));
            JOTViewParser.safeAppendReplacement(m, newBuf, key);
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private String getNextKey()
    {
        String key = "__JOT_XML_PIECE_" + pieces.size() + "__";
        return key;
    }

    /**
     *
     * @param buffer
     * @param curElem null = first - root
     * @return
     * @throws java.lang.Exception
     */
    private int doTags(String xml, JOTXMLElement parent) throws JOTXMLException
    {
        Matcher m = PATTERN_TAG.matcher(xml);
        int lastElemIndex=0;
        int index = 0;
        while (m.find(index))
        {
            index=m.start();
            String openingTag = m.group(1);
            String tagName = m.group(2).trim();
            String restOfTemplate = m.group(3);
            String closeTagString = "</\\s*" + /*Pattern.quote(*/ tagName/*)*/ + "\\s*>";
            Pattern closeTag = Pattern.compile(closeTagString, PATTERN_FLAGS);
            Pattern open = Pattern.compile(/*Pattern.quote(*/"<" + tagName/*)*/ + "\\s+");

            // a closing tag
            if (openingTag.startsWith("</"))
            {
                index += openingTag.length();
                lastElemIndex=index;
                continue;
            }

            if (parent==null && rootFound)
            {
                throw (new JOTXMLException("There should be only one ROOT element in the xml file. Found second one: " + openingTag));
            }

            addText(parent,xml.substring(lastElemIndex,index));

            boolean oneLiner = openingTag.endsWith("/>");

            int i = 0;
            if (!oneLiner)
            {
                Pair pair=JOTViewParser.findMatchingClosingTag(0, restOfTemplate, open, closeTag);
                i=pair.getX();

                if (i == -1)
                {
                    throw new JOTXMLException("Couldn't find closing tag (" + closeTag + ") for:" + open + " in " + restOfTemplate);
                }
                index += pair.getY();
            } else
            {
                index += openingTag.length();
            }

            JOTXMLElement element = new JOTXMLElement(tagName);

            //String desc = openingTag.length() > 40 ? openingTag.substring(0, 40) : openingTag;
            //System.out.println((oneLiner ? "1" : "") + "TAG: " + desc);

            if (!oneLiner)
            {
                String tagContent = restOfTemplate.substring(0, i);

                doTags(tagContent, element);
            }

            lastElemIndex=index;
            addElement(parent, element);
            doAttributes(element,openingTag);
        }
        addText(parent,xml.substring(lastElemIndex));
        return index;
    }

    private void dumpElements(Vector elements, String curPath)
    {
        for (int i = 0; i != elements.size(); i++)
        {
            Object o = elements.get(i);
            if(o instanceof JOTXMLTextElement)
            {
                JOTXMLTextElement el=(JOTXMLTextElement)o;
                System.out.println(el.getText());
            }
            else if(o instanceof JOTXMLElement)
            {
                JOTXMLElement el = (JOTXMLElement) elements.get(i);
                String newPath = curPath + (curPath.length() > 0 ? "." : "") + el.getName();
                String val=el.getValue()!=null?" = '"+el.getValue()+"'":"";
                System.out.println(newPath+val);
                dumpElements(el.getAllItems(), newPath);
            }
        }
    }

    private String reinsertPieces(String buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_PIECES.matcher(buf);
        while (m.find())
        {
            String value = (String) pieces.get(m.group());
            JOTViewParser.safeAppendReplacement(m, newBuf, value);
        }
        m.appendTail(newBuf);
        return newBuf.toString();
    }

    /**
     * For testing
     * @param args
     */
    public static void main(String[] args)
    {
        String[] files =
        {
            "/tmp/xml/config.xml"
        };
        for (int i = 0; i != files.length; i++)
        {
            File f = new File(files[i]);

            try
            {
                StringBuffer xml = JOTXML.readXmlFrom(f);
                JOTXMLParser.parse(xml);
            } catch (Exception e)
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
