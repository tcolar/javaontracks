/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.doclet;

import java.io.IOException;
import net.jot.exceptions.JOTException;

/**
 * Dummy class to test javadoc tags on
 *
 * @deprecated because
 * @author Thibaut Colar
 * @param myParam
 * @see java.util.Vector
 * @see java.util.Hashtable
 * @serial 12345
 * @since 1.4
 * @version 2.6
 * @author Bob Lee
 */
public class JOTDocletTestTags {

    /**
     * My stupid Test field.
     * @deprecated 
     * @see java.util.BitSet
     * @see java.util.Calendar
     * @serial 123
     * @serialField 45467
     * @since 2.3
     */
    public static final int myField=2;

    public static final String myField3="PRICE_ATTRIBUTE";

    public static final long myField4=35L;
    public static final Character myField5=new Character('a');
    public static final JOTDocletTestTags myField6=new JOTDocletTestTags();

    volatile String myField2;

    /**
     * Constructor that takes a string.
     * @deprecated
     * @exception NullPointerException all the time
     * @exception RuntimeException when it breaks
     * @throws NegativeArraySizeException sometimes
     * @throws IllegalArgumentException other times
     * @param param1 blah
     * @see java.util.Calendar
     * @see java.util.BitSet
     * @since 2.5
     */
    public JOTDocletTestTags(String param1) throws IOException
    {

    }
    public JOTDocletTestTags()
    {

    }
    /**
     * getStuff Method
     * @deprecated
     * @exception NullPointerException all the time
     * @exception RuntimeException when it breaks
     * @throws JOTException
     * @throws net.jot.exceptions.JOTTransactionCompletedException
     * @throws NegativeArraySizeException sometimes
     * @throws IllegalArgumentException other times
     * @param param1 blah
     * @see java.util.Calendar
     * @see java.util.BitSet
     * @see JOTDocletNavView
     * @since 2.5
     * @return a result of some sort
     * @param arg1 a thing
     * @param arg2 a string reprseneting stuff
     * @serialData 12345
     */
    public String getStuff(int arg1, int arg2) throws JOTException
    {
        return null;
    }

    /**
     * Cool inner class
     * @author thibaut  C.
     * @deprecated
     * @see java.util.BitSet
     * @see java.util.Calendar
     * @serial 123
     * @serialField 45467
     * @since 2.3     */
    class MyInnerClass
    {

    }
}
