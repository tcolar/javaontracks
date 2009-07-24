package net.jot.utils.caching;

import java.util.Iterator;

/**
 * Central class to Manage of all the caches
 * It is a Cache of Cache items.
 * All the cache keys should be declared here, so it's simpler to keep track
 *
 * Example of use:
 * 		Cache cache = CacheManager.getInstance().getSubCache(CacheManager.SOME_CACHE_KEY, new CacheObjectProvider());
 * 		CacheProvider provider=new CacheProviderImpl(someRequiredArg);
 *		return cache.get("something", provider);
 *
 * @author thibautc
 */
public class CacheManager extends Cache
{

	private static final CacheManager instance = new CacheManager();
	//Cache keys
	// Ex: public static final String SOME_CACHE_KEY = "SOME_CACHE_KEY";

	public static CacheManager getInstance()
	{
		return instance;
	}

	private CacheManager()
	{
		setName("CacheManager");
	}

	public Cache getSubCache(String name, CacheProvider provider)
	{
		return (Cache)get(name, provider);
	}

	/**
	 * Overload to return the stats of all the included caches
	 * @return
	 */
	public String getStats()
	{
		StringBuffer sb = new StringBuffer(super.getStats());
		Iterator it = keys().iterator();
		while (it.hasNext())
		{
			String key = (String) it.next();
			Cache c = (Cache) get(key, null);
			// check for null, could have been deleted since or be null in value
			if (c != null)
			{
				sb.append("\n\t");
				sb.append(key);
				sb.append(" -> ");
				sb.append(c.getStats());
			}
		}
		return sb.toString();
	}

	public String getHtmlStats()
	{
		return getStats().replaceAll("\t", "&nbsp;&nbsp;").replaceAll("\n", "<br/>");
	}

}
