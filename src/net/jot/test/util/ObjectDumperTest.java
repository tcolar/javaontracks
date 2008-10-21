/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.util;

import java.math.BigInteger;
import net.jot.logger.JOTObjectDumper;
import net.jot.testing.JOTTestable;

/**
 *
 * @author thibautc
 */
public class ObjectDumperTest implements JOTTestable{



   public void jotTest() throws Throwable
    {
        DumperTestObject t = new DumperTestObject();
        t.v.add(new MyInteger());
        t.v.add(new MyObject());
        t.filIn=new MyInteger();
        t.filIn2=new MyObject2();
        System.out.println(JOTObjectDumper.dump(t));
    }

   class MyInteger extends BigInteger
   {

        public MyInteger()
        {
            super("123");
        }

   }
   class MyObject
   {
       String myval="blah";
   }
   class MyObject2 extends MyObject
   {
       String myval2="bloh";
   }
}
