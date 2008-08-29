/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.web.view.JOTViewParser;


/**
 * Allow the caching in memory of the templates.
 * Rather than having to read the template from file continously, we will cache them in memory..
 * @author thibautc
 *
 */
public class JOTTemplateCache
{
	public static Hashtable templates=new Hashtable();
	
	/**
	 * Retrieve a template (as a string String) from the cache.
         * Loads it first if not in cache yet
	 * @param templatePath
	 * @return
	 * @throws Exception if reading the template file fails.
	 */
	public static String getTemplate(String templatePath) throws Exception
	{
		if(!templates.contains(templatePath))
		{
			// we need to load it first
			synchronized(JOTTemplateCache.class)
			{
				if(!templates.contains(templatePath))
				{
					loadTemplate(templatePath);
				}
			}
		}
		
		return (String)templates.get(templatePath);
	}
	
	/**
	 * Load a user template from file
	 * @param templatePath
	 * @throws Exception
	 */
	private static void loadTemplate(String templatePath) throws Exception
	{
		BufferedReader reader=null;
		String templateString="";
		try
		{
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTViewParser.class, "Caching template: "+templatePath);						
			reader=new BufferedReader(new FileReader(templatePath));
			String s=null;
			while((s=reader.readLine())!=null)
			{
				templateString+=s+"\n";
			}
			reader.close();
		}
		catch(Exception e)
		{
			if(reader!=null)
				reader.close();
			throw(e);
		}
		templateString=JOTViewParser.doRemoveTags(templateString);
		templates.put(templatePath,templateString);
	}
	

}
