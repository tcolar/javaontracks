/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.views;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.jot.web.filebrowser.JOTFileBrowserHelper;
import net.jot.web.filebrowser.JOTFileBrowserSession;
import net.jot.web.view.JOTView;

/**
 * This is the generic/default implementation of a fileManager view.
 * This use is the builtin default template.
 * Use as-is and/or extend/copy to create your own view/template.
 * @author thibautc
 *
 */
public class JOTGenericFileBrowserView extends JOTView
{

	// Use the builtin generic filemanager template.
	static String tpl=JOTBuiltinTemplates.FILEMANAGER_TEMPLATE;

	public static final DateFormat DEFAULT_TSTAMP_FORMAT=new SimpleDateFormat("MM/dd/yy HH:mm:ss");
	public static final NumberFormat DEFAULT_NUMBER_FORMATTER=new DecimalFormat("######,##0.##");
	
	public static final long ONEKB=1024;
	public static final long ONEMB=1024*ONEKB;
	public static final long ONEGB=1024*ONEMB;	
	
	public void prepareViewData() throws Exception
	{
		setBuiltinTemplate(tpl);

		JOTFileBrowserSession fbSession=JOTFileBrowserHelper.getFbSession(request);
		if(fbSession!=null)
		{
			addVariable("fbSession", fbSession);
		}
	}

	public boolean validatePermissions()
	{
		return true;
	}


	// View methods, called from generic template.
	
	/**
	 * return the CSS classs name for a specific file. allows to display different file types in different ways
	 */
	public String getFileCssClass(File f)
	{
		long now=new Date().getTime();
		long recently=now-(1000*60*2);
		boolean isNewer=f.lastModified()>recently;
		if(f.isDirectory())
			return isNewer?"newFolder":"folder";
		else
			return isNewer?"newFile":"file";			
	}
	
	/**
	 *  returns a nice looking file timestamp
	 */
	public String getTimeStamp(File f)
	{
		if(f==null) return " ";
		long tstamp=f.lastModified();
		Date d=new Date(tstamp);
		return DEFAULT_TSTAMP_FORMAT.format(d);
	}
	
	/**
	 * return s a nice looking file size
	 */
	public String getFileSize(File f)
	{
		if(f==null || f.isDirectory())
			return " ";
		float size=f.length();
		String s=null;
		if(size>ONEGB)
			s=DEFAULT_NUMBER_FORMATTER.format(size/ONEGB)+" Gb";
		else if(size>ONEMB)
			s=DEFAULT_NUMBER_FORMATTER.format(size/ONEMB)+" Mb";
		else if(size>ONEKB)
			s=DEFAULT_NUMBER_FORMATTER.format(size/ONEKB)+" Kb";
		else s=DEFAULT_NUMBER_FORMATTER.format(size);
		return s;	
	}
}
