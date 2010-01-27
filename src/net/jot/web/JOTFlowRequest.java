/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import net.jot.logger.JOTLogger;
import net.jot.web.multipart.JOTMultiPartItem;
import net.jot.web.multipart.JOTMultiPartParser;

/**
 * This is a custom extension of the standard HttpServletRequestWrapper with added functionality.
 * In particular it supports a setParameter() method that allows to overide/manipulates parameter values.
 * It also provides basic for multipartRequest support (ie: file uploads from forms)
 * @author thibautc
 *
 */
public class JOTFlowRequest extends HttpServletRequestWrapper
{

	Hashtable customParams = new Hashtable();
	// possible data about uploaded files from multipart request
	Hashtable files = new Hashtable();
	File tempMultiPartFile = null;
	boolean isMultiPartParsed = false;

	public JOTFlowRequest(HttpServletRequest request)
	{
		super(request);
		try
		{
			setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			JOTLogger.logException(this, "Encoding exception", e);
		}
	}

	public Hashtable getCustomParams()
	{
		return customParams;
	}

	public void setCustomParams(Hashtable customParams)
	{
		this.customParams = customParams;
	}

	/**
	 * SetParameter will save the new parameter value (valid through the java request.)
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value)
	{
		customParams.put(name, value);
	}

	/**
	 * Overload of getParameter, to take into account our custom setRequest feature
	 */
	public String getParameter(String name)
	{
		String param = super.getParameter(name);
		if (customParams.containsKey(name))
		{
			// If there is a custom value, return that one.
			param = (String) customParams.get(name);
		}
		return param;
	}

	/**
	 * Call this if you want to parse the request as a multipart content.
	 * ie: data is coming from a form with enctype="multipart/form-data"
	 * Usually this is a file upload form.
	 * By default it is not done automatically for performance/security reasons.
	 * So you will want to call this method manually to parse the multipart form.
	 * This will:
	 * - add the multipart variables as request parameters, so you can read them with getParameter()
	 * - uplload the files data to a temporary folder, so you can get/save the files  getFileItem().xxx()
	 *
	 *  @tempDataFolder: is the folder the temp data will be stored temporarely (raw multipart data)
	 *  @maxSize: maximum data size in bytes to accept.
	 */
	public void parseMultiPartContent(String tempDataFolder, long maxSize) throws Exception
	{
		// this should only be done once.
		if (!isMultiPartParsed)
		{
			isMultiPartParsed = true;
			try
			{
				tempMultiPartFile = File.createTempFile("mpart", null, new File(tempDataFolder));
				tempMultiPartFile.deleteOnExit();

				JOTMultiPartParser.parse(this, tempMultiPartFile, maxSize);
			} catch (Exception e)
			{
				getInputStream().close();
				tempMultiPartFile.delete();
				throw e;
			}
		}
	}

	/**
	 * When the request is completed/terminated tries to delete the temporary multipart data (uploaded files)
	 * @throws java.lang.Throwable
	 */
	public void finalize() throws Throwable
	{
		// removes the temp data when request ends.
		if (tempMultiPartFile != null && tempMultiPartFile.exists())
		{
			JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Removing temporary multipart data: " + tempMultiPartFile.getAbsolutePath());
			tempMultiPartFile.delete();
		}
		super.finalize();
	}

	/**
	 * Called internally by JOTMultiPartParser, if a file was uploaded from the form.
	 * @param item
	 */
	public void addFile(JOTMultiPartItem item)
	{
		JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Adding a file to the request: " + item.getName());
		files.put(item.getName(), item);
	}

	/**
	 * Return a file handle(JOTMultiPartItem) from the request(multipart request) for an uploaded file, which you can then save somewhere
	 * NOTE: parseMultiPartContent() MUST have been called first otherwise it will return nothing(null).
	 * @param name : the name of the file (HTML input name)
	 * @return : Handle to the uplaoded file. 
	 * see javadoc of net.jot.web.multipart.JOTMultiPartParser for an example
	 */
	public JOTMultiPartItem getFile(String name)
	{
		return (JOTMultiPartItem) files.get(name);
	}
}
