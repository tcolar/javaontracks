/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.utils.caching.providers;

import net.jot.utils.caching.Cache;
import net.jot.utils.caching.CacheProvider;

/**
 * A provider for a cache object
 * Used to store a Cache object itself inside a Cache.
 * @author thibautc
 */
public class CacheObjectProvider implements CacheProvider
{

    public Object readCacheItem(Object key) throws Exception
    {
		return new Cache();
    }

}
