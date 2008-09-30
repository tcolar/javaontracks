/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test;

import net.jot.JOTInitializer;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 *
 * @author thibautc
 */
public class TransactionTest implements JOTTestable
{

    public void jotTest() throws Throwable
    {
        // we need this to have the Prefs, Logger etc.. ready.
        
        JOTTester.tag("Test1");
    }

}
