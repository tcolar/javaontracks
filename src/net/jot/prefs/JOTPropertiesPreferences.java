/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

/**
 * Represents/manage preferences stored in a propert file.
 * @author tcolar
 */
public class JOTPropertiesPreferences implements JOTPreferenceInterface
{

	protected Properties props = new Properties();

	public Boolean getBoolean(String key)
	{
		String val = getString(key);
		return val == null ? null : new Boolean(val.equalsIgnoreCase("true") || val.equalsIgnoreCase("CHECKED"));
	}

	public Boolean getDefaultedBoolean(String key, Boolean defaultValue)
	{
		Boolean b = getBoolean(key);
		return b == null ? defaultValue : b;
	}

	public Long getDefaultedLong(String key, Long defaultValue)
	{
		Long l = (Long) props.get(key);
		return l == null ? defaultValue : l;

	}

	public String getDefaultedString(String key, String defaultValue)
	{
		String s = (String) props.get(key);
		return s == null ? defaultValue : s;
	}

	public String getString(String key)
	{
		return (String) props.get(key);
	}

	public Integer getInt(String key)
	{
		return new Integer((String) props.get(key));
	}

	public Integer getDefaultedInt(String key, Integer defaultValue)
	{
		Integer i = null;
		try
		{
			i = getInt(key);
		} catch (Exception e)
		{
		}
		if (i == null)
		{
			i = defaultValue;
		}
		return i;
	}

	/**
	 * load prefs from given props file.
	 * @param f
	 * @throws java.io.IOException
	 */
	public void loadFrom(File f) throws IOException
	{
		IOException exception = null;
		FileInputStream input = null;
		input = new FileInputStream(f);
		try
		{
			loadFrom(input);
		} catch (IOException e)
		{
			exception = e;
		} finally
		{
			if (input != null)
			{
				input.close();
			}
		}

		if (exception != null)
		{
			throw (exception);
		}
	}

	public void loadFrom(InputStream input) throws IOException
	{
		props.clear();
		props.load(input);
	}

	/**
	 * Save prefs to stream
	 * @param output
	 * @throws java.io.IOException
	 */
	public void saveTo(OutputStream output) throws IOException
	{
		props.store(output, null);
	}

	public void saveTo(File f) throws IOException
	{
		IOException exception = null;
		FileOutputStream output = null;
		output = new FileOutputStream(f);
		try
		{
			saveTo(output);
		} catch (IOException e)
		{
			exception = e;
		} finally
		{
			if (output != null)
			{
				output.close();
			}
		}

		if (exception != null)
		{
			throw (exception);
		}
	}

	public void setBoolean(String key, Boolean value)
	{
		props.setProperty(key, value.toString());
	}

	public void setString(String key, String value)
	{
		props.setProperty(key, value);
	}
}
