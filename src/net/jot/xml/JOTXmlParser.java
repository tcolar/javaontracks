/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.xml;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jot.web.view.JOTViewParser;

/**
 * Used by JOTXML to parse xml text into XML text/elements
 * Kept separated for clarity.
 * We completely ignore declarative stuff (and DTD etc...)
 * http://en.wikipedia.org/wiki/XML
 * @author thibautc
 */
public class JOTXmlParser {
    protected static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE | Pattern.CANON_EQ;
    protected static final Pattern PATTERN_COMMENT = Pattern.compile("<\\!--.*-->", PATTERN_FLAGS);
    protected static final Pattern PATTERN_DECLARATION = Pattern.compile("<\\?*\\?>", PATTERN_FLAGS);

    private Hashtable pieces = new Hashtable();

    private JOTXmlParser(){}

    protected static Enumeration parse(StringBuffer xml) throws JOTXMLException
    {
        StringBuffer copy=new StringBuffer(xml.toString());
        JOTXmlParser parser=new JOTXmlParser();
        copy=parser.doTextItem(PATTERN_COMMENT, copy);
        // we completely ignore declarative stuff (and DTD etc...)
        copy=parser.doTextItem(PATTERN_DECLARATION, copy);
        copy=parser.doTextItem(PATTERN_COMMENT, copy);

        //TODO: actual xml tags


        // TODO: all remaining should be blank etc... -> textItems
        // TODO: group together "text" elements (in a row)


        // TODO
        return parser.pieces.elements();
    }

    private StringBuffer doTextItem(Pattern pattern,StringBuffer xml)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = pattern.matcher(xml);
        while (m.find())
        {
            String key = getNextKey();
            pieces.put(key,new JOTXMLTextElement(m.group(0)));
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

}
