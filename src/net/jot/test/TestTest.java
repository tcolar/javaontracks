/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test;

import net.jot.testing.JOTTester;

/**
 * Internal Test case for static method of unit testing
 * @author thibautc
 */
public class TestTest
{
    private static int add(int i1,int i2)
    {
        return i1+i2;
    }
    
    public static void jotTest() throws Throwable
    {
        TestTest test=new TestTest();
        JOTTester.checkIf("Silly math Test", test.add(1,3)==4);
    }
}
