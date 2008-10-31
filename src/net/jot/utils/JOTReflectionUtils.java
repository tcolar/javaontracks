/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.utils;

import java.lang.reflect.Method;
import java.util.Hashtable;
import net.jot.logger.JOTLogger;
import net.jot.web.view.JOTView;

/**
 *
 * @author thibautc
 */
public class JOTReflectionUtils
{

    private static Hashtable methodCache = new Hashtable();

    /**
     * Try to find a pulic Method of the given name and given parameter values/types.
     * First calls Class.getMethod(), however this doesn't work unless args exactly match signature (no sperclass!!)
     * Try to manually find a match (including aparms superclasses)
     * Finds the first matching method - if there are many matches it will just return the first found !
     * If no match found try finding one recursively in the superclass(es)
     * @param obj (the object instance in which we are looking for a method)
     * @param method the name of the method
     * @param values the parameter values
     * @return the method, or null if no match found
     */
    public static Method findMethod(Object obj, String method, Object[] values)
    {
        Class objClass = obj.getClass();
        if (method == null || method.length() < 1)
        {
            return null;
        }

        Method m = null;
        Class[] classes = null;
        /**
         * This will loop through the class and superclasses
         */
        while (m == null && objClass != null && objClass != Object.class)
        {
            /**
             * Try standard getMethod()
             * This doesn't work unless args exactly match signature (no sperclass!!)
             */
            if (values != null)
            {
                classes = new Class[values.length];
                for (int i = 0; i != values.length; i++)
                {
                    classes[i] = values[i].getClass();
                }
            }
            try
            {
                m = objClass.getMethod(method, classes);
            } catch (Exception e)
            {
            }

            if (m == null)
            {
                /**
                 * Try to manually find a match
                 * Finds the first matching method - if there are many matches it will just return the first found !
                 */
                Method[] methods = objClass.getMethods();
                classes = null;
                if (m == null)
                {
                    for (int i = 0; i != methods.length && m == null; i++)
                    {
                        Method tmpMethod = methods[i];
                        if (tmpMethod.getName().equals(method))
                        {
                            classes = tmpMethod.getParameterTypes();
                            if (values == null || classes.length == values.length)
                            {
                                boolean matching = true;
                                for (int j = 0; j != classes.length && matching; j++)
                                {
                                    if (!classes[j].isInstance(values[j]))
                                    {
                                        matching = false;
                                    }
                                }
                                if (matching)
                                {
                                    m = tmpMethod;
                                }
                            }
                        }
                    }
                }
            }
            // if match not found, we will try the superclass(es)
            objClass = objClass.getSuperclass();
        }
        if (JOTLogger.isWarningEnabled())
        {
            String params = "[";
            for (int i = 0; values != null && i != values.length; i++)
            {
                params += values[i].getClass().getName() + ",";
            }
            params += "]";
            if (m == null)
            {
                JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.WARNING_LEVEL, JOTView.class, "No such method: " + method + params);
            }
        }
        if (JOTLogger.isTraceEnabled())
        {
            String params = "[";
            for (int i = 0; classes != null && i != classes.length; i++)
            {
                params += classes[i].getName() + ",";
            }
            params += "]";
            if (m != null)
            {
                JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.TRACE_LEVEL, JOTView.class, "Found mathing method: " + method + params);
            }
        }
        return m;
    }

    /**
     * Same as findCachedMethod(Object obj, String method, Object[] values), except you can force re-fetching the value.
     * @param forceRefetch force re-computing the method (reflection) instead of reading it from te cache
     */
    public static Method findCachedMethod(boolean forceRefetch, Object obj, String method, Object[] values)
    {
        // build a signature for the obj/method/value combo
        String signature = "" + obj.getClass().hashCode() + "_" + method + "_";
        for (int i = 0; values != null && i != values.length; i++)
        {
            signature += values[i].getClass().hashCode() + "-";
        }
        if (!methodCache.containsKey(signature))
        {
            Method m = findMethod(obj, method, values);
            methodCache.put(signature, m);
            JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.TRACE_LEVEL, JOTView.class, "Caching method as : " + signature);
        }
        Method m=(Method) methodCache.get(signature);
        if (m != null)
        {
            JOTLogger.log(JOTLogger.CAT_MAIN, JOTLogger.TRACE_LEVEL, JOTView.class, "Found method in cache: " + signature);
        }
        return m;
    }

    /**
     * Same as findMethod(Object obj, String method, Object[] values), but lazilly caches the results
     * If another call is made to find the same exact method (same name, on same obj class, with same values[] types)
     * then the cache method will be passed.
     * This is done because reflection is slow, and often classes methods won't chnage on the fly, so it makes sense to cache.
     * @param obj
     * @param method
     * @param values
     * @return
     */
    public static Method findCachedMethod(Object obj, String method, Object[] values)
    {
        return findCachedMethod(false, obj, method, values);
    }

    /**
     * Removes all entries from the method cache
     */
    public static void flushMethodCache()
    {
        methodCache.clear();
    }
}
