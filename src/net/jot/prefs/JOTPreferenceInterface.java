/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Main interface for reading a preference file (implementaion takes care of the format)
 * Preferences are key/values
 * ie: xml file, property file etc ...
 * @author Thibaut Colar http://jot.colar.net/
 *
 */
public interface JOTPreferenceInterface
{
	/**
	 * Loads the prefrence from this input/file into memory (cache)
	 * @param input
	 * @throws IOException
	 */
	public void loadFrom(InputStream input) throws IOException;
	
	/**
	 * Saves the preference from the cache into an output/file
	 * @param output
	 * @throws IOException
	 */
	public void saveTo(OutputStream output) throws IOException;
	
	/**
	 * Loads a String value
	 * Returns null if the value is not defined
	 * @param key
	 * @return
	 */
	public String getString(String key);
	
	/**
	 * Loads a boolean value (true or false 1 or 0) etc.. depends of implementation
	 * Returns null if not defined or not a boolean
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key);
	
	/**
	 * Loads a String value
	 * Returns 'defaultValue' if not defined or not a boolean
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getDefaultedString(String key, String defaultValue);
	
	/**
	 * Loads a boolean value (true or false 1 or 0) etc.. depends of implementation
	 * Returns 'defaultValue' if not defined or not a boolean
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public Boolean getDefaultedBoolean(String key, Boolean defaultValue);

	/**
	 * Sets a string value
	 * @param key
	 * @param value
	 */
	public void setString(String key, String value);

	/**
	 * Sets a boolean value
	 * @param key
	 * @param value
	 */
	public void setBoolean(String key, Boolean value);
	
}
