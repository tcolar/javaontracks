/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * Dumps an whole object hierarchy (fileds, values.. recursively)
 * Useful for debugging.
 *
 * Note: this is all static.
 * @author thibautc
 */
public class JOTObjectDumper
{

    public static int maxRecursiveDepth = 30;
    public static final String TAB = "  ";

    public static int getMaxRecursiveDepth()
    {
        return maxRecursiveDepth;
    }

    /**
     * Default: 15
     * set to prevent infinite loops
     * @param maxRecursiveDepth
     */
    public static void setMaxRecursiveDepth(int maxRecursiveDepth)
    {
        JOTObjectDumper.maxRecursiveDepth = maxRecursiveDepth;
    }

    public static String dump(Object o)
    {
        StringBuffer buffer = new StringBuffer();
        dumpToSB(o, buffer, "", null);
        return buffer.toString();
    }

    private static void dumpToSB(Object o, StringBuffer buffer, String padding, String fieldName)
    {
        if (padding.length() > 0 && padding.length() / TAB.length() > maxRecursiveDepth)
        {
            buffer.append("\n ... More Avail - To deep recursion ... \n");
            return;
        }

        buffer.append(padding);
        if (fieldName != null)
        {
            buffer.append(fieldName).append(" = ");
        }

        if (o == null)
        {
            buffer.append("null").append("\n");
            return;
        }

        Class clazz = o.getClass();

        if (sameClass(clazz, Class.class) || sameClass(clazz, Object.class))
        {
            // we don't want to log all the Object/Class internals.
            buffer.append("\n");
            return;
        }

        boolean primitive = clazz.isPrimitive() || sameClass(clazz, String.class) ||
                sameClass(clazz, StringBuffer.class) || o instanceof Number;

        if (primitive)
        {
            buffer.append(o.toString()).append("\n");
            return;
        }

        if (o instanceof Collection)
        {
            Collection c = (Collection) o;
            buffer.append("Collection(").append(clazz.getName()).append(")\n");
            Object[] array = c.toArray();
            for (int i = 0; i != array.length; i++)
            {
                dumpToSB(array[i], buffer, padding + TAB, null);
            }
            return;
        }
        if (o instanceof Map)
        {
            Map m = (Map) o;
            buffer.append("Map(").append(clazz.getName()).append(")\n");
            Object[] array = m.keySet().toArray();
            for (int i = 0; i != array.length; i++)
            {
                buffer.append(padding + TAB).append(array[i].toString()).append(" => \n");
                dumpToSB(m.get(array[i]), buffer, padding + TAB + TAB, null);
            }
            return;
        }
        if (o instanceof Enumeration)
        {
            Enumeration e = (Enumeration) o;
            buffer.append("Enumeration(").append(clazz.getName()).append(")\n");
            while (e.hasMoreElements())
            {
                dumpToSB(e.nextElement(), buffer, padding + TAB, null);
            }
            return;
        }
        if (clazz.isArray())
        {
            buffer.append("Array(").append(clazz.getName()).append(")\n");
            for (int i = 0; i != Array.getLength(o); i++)
            {
                Object o2 = Array.get(o, i);
                dumpToSB(o2, buffer, padding + TAB, null);
            }
            return;
        }
        
        buffer.append("Object(").append(clazz.getName()).append(")\n");

        // Go through the class fields
        doFields(clazz, o, buffer, padding);

        // go through the object superclass(es) fields
        clazz=clazz.getSuperclass();
        while(clazz!=null)
        {
            doFields(clazz, o, buffer, padding);
            clazz=clazz.getSuperclass();
        }

    }

    private static void doFields(Class clazz, Object o, StringBuffer buffer, String padding)
    {
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i != fields.length; i++)
            {
                int modifiers = fields[i].getModifiers();
                fields[i].setAccessible(true);
                try
                {
                    if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) && !fields[i].getName().startsWith("this$"))
                    {
                        // do not bother listing constants
                        // also ignore inner classes whihc cause issues
                        Object o2 = fields[i].get(o);
                        System.out.println(fields[i].getName());
                        dumpToSB(o2, buffer, padding + TAB, fields[i].getName());
                    }
                } catch (IllegalAccessException e)
                {
                }
            }
    }
    
    private static boolean sameClass(Class clazz, Class clazz2)
    {
        return clazz.getName().equals(clazz2.getName());
    }
    
    class TestObject2
    {
        String innerVal="innerVal";
    }
}
