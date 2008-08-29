/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.util.Hashtable;

import net.jot.logger.JOTLogger;

/**
 * Cache that stores "Class" objects requested by flow.conf (ie: controllers and views)
 * Such as class.forName won't have to be called repeatedly.
 * @author thibautc
 */
public class JOTFlowClassCache
{

	private static Hashtable cache=new Hashtable();
	
	public static Class getClass(String className) throws Exception
	{
		try
		{
		if(!cache.containsKey(className))
		{
			synchronized(JOTFlowClassCache.class)
			{
				if(!cache.containsKey(className))
				{
					Class theClass=Class.forName(className);
					cache.put(className, theClass);
				}
			}
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTFlowClassCache.class, "Loaded class in cache: "+className);
		}
		
		JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTFlowClassCache.class, "Fetched class from cache: "+className);
		}
		catch(ClassNotFoundException e)
		{
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.ERROR_LEVEL, JOTFlowClassCache.class, "!! Could not find class: "+className);
			Exception e2=new Exception("!! Could not find class: "+className,e);
			throw(e2);
		}
		
		
		return (Class)cache.get(className);
	}
}
