/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test;

import net.jot.testing.JOTTester;

/**
 *
 * @author thibautc
 */
public class TesterDebugger
{
/**
     * For debugging JOTTester
     * @param args
     */
    public static void main(String[] args)
    {
        String[] args2 =
        {
            "classes", "-selfTest"
        };
        int breakpoint = 1;
        JOTTester.main(args2);
    }
}
