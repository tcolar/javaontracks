/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.filebrowser;

import java.io.File;
import java.util.Comparator;

/**
 * Static comparators objects used to order file listings.
 * Used by JotFileBrowser
 * @author thibautc
 *
 */
public class JOTFileComparators
{
	public static final Comparator NAME_ASC_COMPARATOR = new JOTFileNameAscComparator();
	public static final Comparator NAME_DESC_COMPARATOR = new JOTFileNameDescComparator();
	public static final Comparator SIZE_ASC_COMPARATOR = new JOTFileSizeAscComparator();
	public static final Comparator SIZE_DESC_COMPARATOR = new JOTFileSizeDescComparator();
	public static final Comparator TSTAMP_ASC_COMPARATOR = new JOTFileTimestampAscComparator();
	public static final Comparator TSTAMP_DESC_COMPARATOR = new JOTFileTimestampDescComparator();
	
        /** Compares by file Name alphabetical*/
	static class JOTFileNameAscComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1)
		{
			File f1=(File)arg0;
			File f2=(File)arg1;
			return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
		}
	}
        /** Compares by file Name reversed alphabetical*/
	static class JOTFileNameDescComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1)
		{
			return NAME_ASC_COMPARATOR.compare(arg1, arg0);
		}
	}
        /** Compares by file size small to large*/
	static class JOTFileSizeAscComparator implements Comparator
	{
		public  int compare(Object arg0, Object arg1)
		{
			File f1=(File)arg0;
			File f2=(File)arg1;
			long l1=f1.isDirectory()?0:f1.length();
			long l2=f2.isDirectory()?0:f2.length();
			if(l1==l2) return 0;
				else
			return l1>l2?1:-1;
		}
	}
        /** Compares by file size large to small*/
	static class JOTFileSizeDescComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1)
		{
			return SIZE_ASC_COMPARATOR.compare(arg1,arg0);
		}
	}
        /** Compares by file timestamp(last change) oldest to newest*/
	static class JOTFileTimestampAscComparator implements Comparator
	{
		public  int compare(Object arg0, Object arg1)
		{
			File f1=(File)arg0;
			File f2=(File)arg1;
			if(f1.lastModified()==f2.lastModified()) return 0;
				else
			return f1.lastModified()>f2.lastModified()?1:-1;
		}
	}
        /** Compares by file timestamp(last change) newest to oldest*/
	static class JOTFileTimestampDescComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1)
		{
			return TSTAMP_ASC_COMPARATOR.compare(arg1,arg0);
		}
	}
}
