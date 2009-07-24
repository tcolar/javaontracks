/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.utils.caching;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.jot.logger.JOTLogger;
import net.jot.utils.JOTUtilities;

/**
 * Lazy caching base
 * Stores data in a Synchronized Hashmap(so it can store null)
 * @author thibautc
 */
public class Cache
{

	public String name = JOTUtilities.getShortClassname(getClass());
	private Map cache = Collections.synchronizedMap(new HashMap());
	public int hits = 0;
	public Date flushTime = new Date();

	/**
	 * Gets an item from the cache (lazilly cached)
	 * Key should be an immutable (ie: String)
	 * Infos: will be passed to read, can be anyhting that help you creation the value in read()
	 * @param key
	 * @return
	 */
	public Object get(Object key, CacheProvider provider)
	{
		hits++;
		if (!cache.containsKey(key))
		{
			synchronized (this)
			{
				if (!cache.containsKey(key))
				{
					Object value = null;
					try
					{
						if (provider != null)
						{
							value = provider.readCacheItem(key);
						}
					} catch (Exception e)
					{
						JOTLogger.logException(this, "Cache[" + name + "]Failed reading entry for'" + key + "' with provider:" + provider.getClass().getName(), e);
					}
					cache.put(key, value);
					JOTLogger.debug(JOTLogger.CAT_MAIN,this,"Cache", "[" + name + "]Lazyly Cached : " + key + " -> " + value);
				}
			}
		}
		JOTLogger.debug(JOTLogger.CAT_MAIN,this,"Cache", "[" + name + "]Read: " + key + " -> " + cache.get(key));
		return cache.get(key);
	}

	public synchronized void flushCache()
	{
		JOTLogger.debug(JOTLogger.CAT_MAIN,this,"Cache", "[" + name + "]Flushing :" + cache.size() + " elements");
		cache.clear();
		flushTime = new Date();
		hits = 0;
	}

	/**
	 * Set a name for this cache to be used in the stats
	 * Defaults to the class name
	 * @param n
	 */
	public void setName(String n)
	{
		name = n;
	}

	/**
	 * Return a stat string about this cache
	 * @return
	 */
	public String getStats()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(name);
		sb.append("] Hits=");
		sb.append(hits);
		sb.append(", Size=");
		sb.append(cache.size());
		sb.append(", Time=");
		String time = flushTime.toString();
		time = JOTUtilities.formatDate(flushTime,false);
		sb.append(time);
		return sb.toString();
	}

	public Set keys()
	{
		return cache.keySet();
	}

	public boolean containsKey(Object key)
	{
		return cache.containsKey(key);
	}

	/**
	 * Force a value (put())
	 * @param urlKey
	 * @param url
	 */
	public void setValue(Object key, Object value)
	{
		JOTLogger.debug(JOTLogger.CAT_MAIN,this,"Cache", "[" + name + "]Set: " + key + " -> " + value);
		cache.put(key, value);
	}
}
