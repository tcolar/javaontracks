/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.web.view.JOTViewParser;

/**
 * Allow the caching in memory of the text files.
 * Rather than having to read the template from file continously, we will cache them in memory..
 * If the file is updated (changed timestamp), then reload it
 * @author thibautc
 *
 */
public class JOTTextFileCache
{

	public static Hashtable files = new Hashtable();

	/**
	 * Retrieve a template (as a string String) from the cache.
	 * Loads it first if not in cache yet
	 * @param templatePath
	 * @return
	 * @throws Exception if reading the template file fails.
	 */
	public static String getFileText(String filePath) throws Exception
	{
		if (needCaching(filePath))
		{
			// we need to load it first
			synchronized (JOTTextFileCache.class)
			{
					loadTextFile(filePath);
			}
		}

		return ((JOTTextFileCacheItem) files.get(filePath)).getTemplate();
	}

	public static boolean needCaching(String filePath)
	{
		JOTTextFileCacheItem item = (JOTTextFileCacheItem) files.get(filePath);
		File f = new File(filePath);

		return item==null || item.getTimestamp() != f.lastModified();
	}
	/**
	 * Load a user template from file
	 * @param templatePath
	 * @throws Exception
	 */
	private static void loadTextFile(String filePath) throws Exception
	{
		BufferedReader reader = null;
		String fileString = "";
		long timestamp = 0;
		try
		{
			JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTViewParser.class, "Caching text file: " + filePath);
			File f = new File(filePath);
			timestamp = f.lastModified();
			reader = new BufferedReader(new FileReader(filePath));
			String s = null;
			while ((s = reader.readLine()) != null)
			{
				fileString += s + "\n";
			}
			reader.close();
		} catch (Exception e)
		{
			if (reader != null)
			{
				reader.close();
			}
			throw (e);
		}
		fileString = JOTViewParser.doRemoveTags(fileString);
		JOTTextFileCacheItem item = new JOTTextFileCacheItem(timestamp, fileString);
		files.put(filePath, item);
	}

	static class JOTTextFileCacheItem
	{

		private long timestamp;
		private String template;

		public JOTTextFileCacheItem(long timestamp, String template)
		{
			this.timestamp = timestamp;
			this.template = template;
		}

		public String getTemplate()
		{
			return template;
		}

		public long getTimestamp()
		{
			return timestamp;
		}
	}
}
