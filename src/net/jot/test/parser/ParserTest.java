/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.parser;

import java.util.regex.Pattern;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;
import net.jot.utils.JOTPair;
import net.jot.web.view.JOTViewParser;

/**
 *
 * @author tcolar
 */
public class ParserTest implements JOTTestable
{
    public final static Pattern openTag=Pattern.compile("<test-tag>");
    public final static Pattern closeTag=Pattern.compile("</test-tag>");
    public final static String test1="</test-tag>";
    public final static String test2="</test-tag";
    public final static String test3="/test-tag>";
    public final static String test4="<test-tag>sdfddsfsds</test-tag>";
    public final static String test5="<test-tag><test-tag></test-tag></test-tag>";
    public final static String test6="<test-tag>ddd<test-tag>ddd</test-tag>sdfddsfsds</test-tag>";

    public void jotTest() throws Throwable
    {
        JOTTester.tag("Checking findMatchingTag()");
        JOTPair pair=JOTViewParser.findMatchingClosingTag(0, test1, openTag, closeTag);
        JOTTester.checkIf("test1",pair.getX()==0 && pair.getY()==11,""+pair.getY());
        JOTPair pair2=JOTViewParser.findMatchingClosingTag(3, test1, openTag, closeTag);
        JOTTester.checkIf("test1b",pair2.getX()==-1 && pair2.getY()==-1);
        JOTTester.tag("Checking view parser");
        pair=JOTViewParser.findMatchingClosingTag(0, test2, openTag, closeTag);
        JOTTester.checkIf("test2",pair.getX()==-1 && pair.getY()==-1);
        pair=JOTViewParser.findMatchingClosingTag(0, test3, openTag, closeTag);
        JOTTester.checkIf("test3",pair.getX()==-1 && pair.getY()==-1);
        pair=JOTViewParser.findMatchingClosingTag(1, test4, openTag, closeTag);
        JOTTester.checkIf("test4",pair.getX()==20 && pair.getY()==31,""+pair.getX());
        pair=JOTViewParser.findMatchingClosingTag(8, test4, openTag, closeTag);
        JOTTester.checkIf("test4b",pair.getX()==20 && pair.getY()==31);
        pair=JOTViewParser.findMatchingClosingTag(25, test4, openTag, closeTag);
        JOTTester.checkIf("test4c",pair.getX()==-1 && pair.getY()==-1);
        pair=JOTViewParser.findMatchingClosingTag(0, test4, openTag, closeTag);
        JOTTester.checkIf("test4d",pair.getX()==-1 && pair.getY()==-1);
        pair=JOTViewParser.findMatchingClosingTag(1, test5, openTag, closeTag);
        JOTTester.checkIf("test5",pair.getX()==31 && pair.getY()==42,""+pair.getX());
        pair=JOTViewParser.findMatchingClosingTag(10, test5, openTag, closeTag);
        JOTTester.checkIf("test5b",pair.getX()==31 && pair.getY()==42);
        pair=JOTViewParser.findMatchingClosingTag(14, test5, openTag, closeTag);
        JOTTester.checkIf("test5b",pair.getX()==20 && pair.getY()==31);
        pair=JOTViewParser.findMatchingClosingTag(1, test6, openTag, closeTag);
        JOTTester.checkIf("test6",pair.getX()==47 && pair.getY()==58,""+pair.getX());
        pair=JOTViewParser.findMatchingClosingTag(20, test6, openTag, closeTag);
        JOTTester.checkIf("test6b",pair.getX()==26 && pair.getY()==37,""+pair.getX());
    }

}
