/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.testing.JOTTestable;

/**
 * Dumps an whole object hierarchy (fileds, values.. recursively)
 * Useful for debugging.
 *
 * @author thibautc
 */
public class JOTObjectDumper implements JOTTestable
{

    public static String dump(Object o)
    {
        StringBuffer buffer = new StringBuffer();
        dumpToSB(o, buffer, "");
        return buffer.toString();
    }

    public static void dumpToSB(Object o, StringBuffer buffer, String padding)
    {
        Class oClass = o.getClass();
        if (oClass.isArray())
        {
            buffer.append(padding).append("[").append(oClass.getName()).append("\n");
            for (int i = 0; i != Array.getLength(o); i++)
            {
                Object value = Array.get(o, i);
                dumpToSB(value, buffer, padding + "  ");
            }
            buffer.append(padding).append("]").append("\n");
        } else
        {
            buffer.append(padding).append("{").append(oClass.getName()).append("\n");
            while (oClass != null)
            {
                Field[] fields = oClass.getDeclaredFields();
                for (int i = 0; i != fields.length; i++)
                {
                    fields[i].setAccessible(true);
                    buffer.append(padding).append(fields[i].getName());
                    buffer.append(" = ");
                    try
                    {
                        Object value = fields[i].get(o);
                        if (value != null)
                        {
                            if (value.getClass().isArray())
                            {
                                dumpToSB(value, buffer, padding + "  ");
                            } else
                            {
                                buffer.append(value).append("\n");
                            }
                        }
                    } catch (IllegalAccessException e)
                    {
                    }
                }
                oClass = oClass.getSuperclass();
            }
            buffer.append(padding).append("}").append("\n");
        }
    }

    public void jotTest() throws Throwable
    {
        TestObject t=new TestObject();
        System.out.println(dump(t));
    }
    
    class TestObject
    {
        int field1=5;
        Integer field2=new Integer(2);
        String field3="field3";
        float field4=2.25f;
        byte[] b={1,2,3,4};
        Vector v=new Vector();
        
        Hashtable hash=new Hashtable();
        
        TestObject()
        {
            hash.put("blah",field3);
            hash.put("bloh",field2);
            hash.put("blut",b);
            
            v.add(field3);
            v.add(field2);
            v.add(b);
            v.add(hash);
        }
    }
}
