/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test;

import net.jot.testing.JOTTester;

/**
 * Test case for static method of unit testing
 * @author thibautc
 */
public class JOTTestTest 
{
    private static int add(int i1,int i2)
    {
        return i1+i2;
    }
    
    public static void jotTest() throws Throwable
    {
        JOTTestTest test=new JOTTestTest();
        JOTTester.checkIf("Silly math Test", test.add(1,3)==4);
    }
}
