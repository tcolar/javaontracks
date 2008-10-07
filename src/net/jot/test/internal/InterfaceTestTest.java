/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.internal;

import net.jot.test.*;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 * Internal Test case for interface method of unit testing
 * @author thibautc
 */
public class InterfaceTestTest implements JOTTestable
{
    private static int add(int i1,int i2)
    {
        return i1+i2;
    }
    
    public void jotTest() throws Throwable
    {
        TestTest test=new TestTest();
        JOTTester.checkIf("Test Interface Test", add(1,3)==4);
    }
}
