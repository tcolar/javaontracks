/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.utils.caching;

/**
 *
 * @author thibautc
 */
public interface CacheProvider
{

    /**
     * If an object is not cached yet, this will be called to get it.
     * This will be called synchronized.
     * @param key
     * @return
     */
    public abstract Object readCacheItem(Object key) throws Exception;
}

