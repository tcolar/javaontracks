/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import net.jot.logger.JOTLogger;
import net.jot.web.filebrowser.JOTFileComparators;

/**
 * Collection of small utilities which aren't worth having their own class :-)
 *
 *@author     tcolar
 *@created    September 28, 2001
 */
public class JOTUtilities
{

    /**
     * Sorting types for the sort method
     */
    public static final int SORT_BY_NAME_ASC = 1;
    public static final int SORT_BY_NAME_DESC = -1;
    public static final int SORT_BY_SIZE_ASC = 2;
    public static final int SORT_BY_SIZE_DESC = -2;
    public static final int SORT_BY_TSTAMP_ASC = 3;
    public static final int SORT_BY_TSTAMP_DESC = -3;

    /**
     * endode a cell of data into CVS format.
     */
    public static String encodeCSVEntry(String s)
    {
        //quotes need to be doubled
        s = s.replaceAll("\"", "\"\"");
        // wrap in quotes to take care of line feeds and commas in the data.
        return "\"" + s + "\"";
    }

    /**
     * get a directory size (recursively)
     *
     *@param  folder  Description of the Parameter
     *@return         The folderSize value
     */
    public static int getFolderSize(File folder)
    {
        int size = 0;
        File[] content = folder.listFiles();
        if (content != null)
        {
            for (int i = 0; i != content.length; i++)
            {
                if (content[i].isDirectory())
                {
                    getFolderSize(content[i]);
                } else
                {
                    size += content[i].length();
                }
            }
        }
        return size;
    }

    /**
     * copy a file ...
     *
     *@param  dest                       Description of Parameter
     *@param  src                        Description of Parameter
     *@exception  FileNotFoundException  Description of Exception
     *@exception  IOException            Description of Exception
     *@since
     */
    public static void copyFile(java.io.File dest, java.io.File src)
            throws FileNotFoundException, IOException
    {
        FileInputStream input = null;
        FileOutputStream output = null;
        try
        {
            if (dest.getAbsolutePath().equals(src.getAbsolutePath()))
            {
                return;
            // avoid overwritting itself
            }

            input = new FileInputStream(src);
            output = new FileOutputStream(dest);

            int size = (int) src.length();
            byte buffer[] = new byte[size];
            input.read(buffer, 0, size);
            output.write(buffer, 0, size);

        } finally
        {
            if (input != null)
            {
                input.close();
            }

            if (output != null)
            {
                output.flush();
                output.close();
            }
        }
    }

    /**
     * Fornat a string to a specific length by truncating or padding with
     * spaces as needed.
     *
     *@param  str     Description of Parameter
     *@param  length  Description of Parameter
     *@return         Description of the Returned Value
     *@since
     */
    public static String formatString(String str, int length)
    {
        if (str == null)
        {
            str = "";
        }
        String result = "";
        if (str.length() >= length)
        {
            result = str.substring(0, length);
        } else
        {
            result = str;
            while (result.length() < length)
            {
                result += ' ';
            }
        }
        return result;
    }

    /**
     *Description of the Method
     *
     *@param  str  Description of Parameter
     *@return      Description of the Returned Value
     */
    public static String firstLine(String str)
    {
        int i = str.indexOf("\n");
        if (i != -1)
        {
            str = str.substring(0, i);
        }
        return str;
    }

    /**
     * Return a folder path with the trailing slah
     *
     *@param  str  Description of Parameter
     *@return      Description of the Returned Value
     */
    public static String endWithSlash(String str)
    {
        if (str != null && !str.endsWith(File.separator))
        {
            str += File.separator;
        }
        return str;
    }

    public static String endWithForwardSlash(String str)
    {
        if (str != null && !str.endsWith("/"))
        {
            str += "/";
        }
        return str;
    }

    /**
     * Move a filke to a new location
     * Note that is does a copy then a delete, as actaully "moving"
     * the file does not always work right in my  experience
     *
     *@param  dest                       Description of Parameter
     *@param  src                        Description of Parameter
     *@exception  FileNotFoundException  Description of Exception
     *@exception  IOException            Description of Exception
     */
    public static void moveFile(java.io.File dest, java.io.File src)
            throws FileNotFoundException, IOException
    {
        if (dest.getAbsolutePath().equals(src.getAbsolutePath()))
        {
            return;
        }
        // avoid overwritting itself

        copyFile(dest, src);
        src.delete();
    }

    /**
     *  Delete a folder recursively
     *
     *@param  folder  Description of the Parameter
     */
    public static void deleteFolder(File folder)
    {
        deleteFolderContent(folder);
        folder.delete();
    }

    /**
     * Recursively delete all the content of a folder (but not the folder itself)
     *
     *@param  root  Description of Parameter
     */
    public static void deleteFolderContent(File root)
    {
        File[] content = root.listFiles();
        if (content != null)
        {
            for (int i = 0; i != content.length; i++)
            {
                if (content[i].isDirectory())
                {
                    deleteFolderContent(content[i]);
                } else
                {
                    content[i].delete();
                }
            }
        }
        root.delete();
    }

    /**
     * recursively copy the content of oldfolder into a newfolder
     *
     *@param  newFolder      Description of Parameter
     *@param  oldFolder      Description of Parameter
     *@exception  Exception  Description of the Exception
     */
    public static void copyFolderContent(File newFolder, File oldFolder, boolean recurse)
            throws Exception
    {
        File[] content = oldFolder.listFiles();
        if (content != null)
        {
            for (int i = 0; i != content.length; i++)
            {
                String targetPath;
                File newFile;

                targetPath = newFolder.getAbsolutePath() + File.separator + content[i].getName();
                newFile = new File(targetPath);

                if (content[i].isDirectory())
                {
                System.out.println("DIR "+content[i].getAbsolutePath()+" -> "+newFile.getAbsolutePath());
                    if (recurse)
                    {
                        newFile.mkdirs();
                        copyFolderContent(newFile, content[i], recurse);
                    }
                } else
                {
                System.out.println(content[i].getAbsolutePath()+" -> "+newFile.getAbsolutePath());
                    copyFile(newFile, content[i]);
                }
            }
        }
    }

    /**
     * Recursively moving all the content of oldfolder into newfolder
     * It does a copy then delete rather than a real move
     *
     *@param  newFolder  Description of Parameter
     *@param  oldFolder  Description of Parameter
     */
    public static void moveFolderContent(File newFolder, File oldFolder)
    {
        File[] content = oldFolder.listFiles();
        if (content != null)
        {
            for (int i = 0; i != content.length; i++)
            {
                if (content[i].isDirectory())
                {
                    File folder = new File(newFolder.getAbsolutePath() + File.separator + content[i].getName());
                    folder.mkdirs();
                    moveFolderContent(folder, content[i]);
                } else
                {
                    //System.out.println("moving "+content[i].getAbsolutePath()+" to "+newFolder.getAbsolutePath()+File.separator+content[i].getName());
                    content[i].renameTo(new File(newFolder.getAbsolutePath() + File.separator + content[i].getName()));
                }
            }
        }
    }

    /**
     * Recursively zip the content of a folder into a stream / file
     *
     *@param  zos              Description of Parameter
     *@param  root             Description of Parameter
     *@param  folder           Description of Parameter
     *@exception  IOException  Description of Exception
     */
    public static void zipFolder(ZipOutputStream zos, String root, File folder) throws IOException
    {
        //System.out.println("zipFolder(" + folder.getAbsolutePath() + ")");
        File[] content = folder.listFiles();
        if (content != null)
        {
            for (int i = 0; i != content.length; ++i)
            {
                if (content[i].isDirectory())
                {
                    zipFolder(zos, root, content[i]);
                } else
                {
                    zos.putNextEntry(new ZipEntry(content[i].getAbsolutePath().substring(root.length())));
                    int fileLength = (int) content[i].length();
                    InputStream input = new FileInputStream(content[i]);
                    byte buffer[] = new byte[fileLength];
                    input.read(buffer, 0, fileLength);
                    zos.write(buffer, 0, fileLength);
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     *  Unzip a zip file in the given folder (recursively);
     *  Retrieve is use as this
     * if retrieve is set to ".swf", the last file found
     * ending by .swf will be returned.
     *
     *@param  tmpdir         Description of the Parameter
     *@param  retrieve       Description of the Parameter
     *@param  filename       Description of the Parameter
     *@return                Description of the Return Value
     *@exception  Exception  Description of the Exception
     */
    public static String unzip(String filename, String tmpdir, String retrieve) throws Exception
    {
        File f = new File(filename);
        String result = null;
        ZipFile zip = new ZipFile(f);

        for (Enumeration e = zip.entries(); e.hasMoreElements();)
        {
            ZipEntry ze = (ZipEntry) e.nextElement();
            if (ze.isDirectory())
            {
                String entryName = ze.getName();
                entryName = entryName.replace('\\', '/');
                String dir = tmpdir + entryName;
                File dirfile = new File(dir);
                if (!dirfile.exists())
                {
                    dirfile.mkdir();
                }
            }
        }

        for (Enumeration e = zip.entries(); e.hasMoreElements();)
        {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String entryName = tmpdir + ze.getName();
            entryName = entryName.replace('\\', '/');
            if (!ze.isDirectory())
            {
                int end = 0;
                int start = 0;
                while (end >= 0)
                {
                    String tmp = "";
                    end = entryName.indexOf("/", end);
                    if (end >= 0)
                    {
                        tmp = entryName.substring(start, end);
                        end = end + 1;
                    }
                    File tmpf = new File(tmp);
                    if (!tmpf.exists())
                    {
                        tmpf.mkdir();
                    }
                }
                FileOutputStream fos = new FileOutputStream(entryName);
                InputStream is = zip.getInputStream(ze);
                byte[] buf = new byte[5 * 1024];
                // small buffer
                int bytesRead;
                while ((bytesRead = is.read(buf)) != -1)
                {
                    fos.write(buf, 0, bytesRead);
                }
                fos.close();

                if (entryName != null && entryName.toLowerCase().endsWith(retrieve.toLowerCase()))
                {
                    result = entryName;
                }
            }
        }

        return result;
    }

    /**
     *  Replace all occurence of "pattern" by "replacement" in the string "src"
     *  This does not use the "replaceAll()" method of the String class, which often leads to issues since it interprets
     * some charcaters such as "$".
     * Rather it does a plain search/replace loop.
     *
     *@param  src          Description of the Parameter
     *@param  pattern      Description of the Parameter
     *@param  replacement  Description of the Parameter
     *@return              Description of the Return Value
     */
    public static String replaceAll(String src, String pattern, String replacement)
    {
        int i = src.indexOf(pattern);
        if (i >= 0)
        {
            src = src.substring(0, i) + replacement + src.substring(i + pattern.length(), src.length());
            return replaceAll(src, pattern, replacement);
        }
        return src;
    }

    /**
     *  Description of the Method
     *
     *@param  prefix  Description of the Parameter
     *@return         Description of the Return Value
     */
    public static File createTempFile(String prefix, String path)
    {
        if (prefix == null)
        {
            prefix = "temp";
        }

        //Preferences prefs = CheckerManager.getInstance().getPreferences();
        //String path = prefs.getString("server.path.tmp");

        path = "/tmp/" + path;
        String folder = prefix + new java.util.Date().getTime() + "_" + new Random().nextInt(9999);

        return new File(path, folder);
    }

    /**
     * Formats a date into 
     * - a user friendly format: 01/19/1977 04:08 
     * - an SQL format: 77-01-19 04:08
     * @param d
     * @param sqlFormat
     * @return
     */
    public static String formatDate(Date d, boolean sqlFormat)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(d);
        String date = "";
        if (!sqlFormat)
        {
            date = sizeIt(cal.get(Calendar.MONTH) + 1, 2) + "/" + sizeIt(cal.get(Calendar.DAY_OF_MONTH), 2) + "/" + cal.get(Calendar.YEAR) + " " + sizeIt(cal.get(Calendar.HOUR_OF_DAY), 2) + ":" + sizeIt(cal.get(Calendar.MINUTE), 2);
        } else
        {
            date = sizeIt(cal.get(Calendar.YEAR), 2) + "-" + sizeIt(cal.get(Calendar.MONTH) + 1, 2) + "-" + sizeIt(cal.get(Calendar.DAY_OF_MONTH), 2) + " " + sizeIt(cal.get(Calendar.HOUR_OF_DAY), 2) + ":" + sizeIt(cal.get(Calendar.MINUTE), 2);
        }
        return date;
    }

    /**
     * Pads a number with zeroes to make it requested length
     * @param i
     * @param length
     * @return
     */
    public static String sizeIt(int i, int length)
    {
        return sizeIt("" + i, length);
    }

    /**
     * Pads a string(number) with zeroes to make itt requested length
     * @param s
     * @param length
     * @return
     */
    public static String sizeIt(String s, int length)
    {
        while (s.length() < length)
        {
            s = "0" + s;
        }
        return s;
    }

    /**
     * Checks wether s string is at least minSize long
     * @param field
     * @param minSize
     * @return
     */
    public static boolean checkFieldLength(String field, int minSize)
    {
        if (field == null)
        {
            return false;
        }
        return field.length() >= minSize;
    }

    /**
     * Validates wether an email address seem to be valid (ie: ~ aa@bb.cc)
     * @param email
     * @return
     */
    public static boolean checkEmail(String email)
    {
        if (email == null || email.length() < 4)
        {
            return false;
        }
        int i1 = email.indexOf("@");
        int i2 = email.indexOf(".", i1);
        if (i1 == -1 || i2 == -1 || i1 < 1 || i2 < i1)
        {
            return false;
        }
        return true;
    }

    public static String upperCase(String s)
    {
        if (s == null)
        {
            return s;
        }
        return s.toUpperCase();
    }

    public static String lowerCase(String s)
    {
        if (s == null)
        {
            return s;
        }
        return s.toLowerCase();
    }

    /**
     * Uppercase the first letter of a string (ie: name) 
     * @param s
     * @return
     */
    public static String upperFirst(String s)
    {
        if (s == null || s.length() == 0)
        {
            return s;
        }
        if (s.length() == 1)
        {
            return s.toUpperCase();
        } else
        {
            return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
        }

    }

    public static boolean isWindowsOS()
    {
        String os = System.getProperty("os.name");
        return os.startsWith("Windows");
    }

    /**
     * Padds an umber with 0's to make it correct length
     * @param nb
     * @param length
     * @return
     */
    public static String formatNumber(int nb, int length)
    {
        return sizeIt(nb, length);
    }

    /**
     * The Standard java response.sendredirect tries to rebuild the full URL using the servlet path
     * This will not work with java behind a proxy (since it will use the proxy path rather than vanity URL)
     * So here we just send a simple (possibly relative) URL.
     * @param res
     * @param newLocation
     * @param permanent
     */
    public static void sendRedirect(HttpServletResponse res, String newLocation, boolean permanent, boolean encodeIt)
    {
        try
        {
            /*
             * Not that encoding add the jsessionid and thta can can issues: SEO and others.
             */
            if (encodeIt)
            {
                /**
                 * Note: when using "localhost" on firefox, it doesn;t accept the cookie
                 * and instead use a url jsessionid
                 * strange !
                 */
                newLocation = res.encodeRedirectURL(newLocation);
            }
            res.setStatus(permanent ? HttpServletResponse.SC_MOVED_PERMANENTLY : HttpServletResponse.SC_MOVED_TEMPORARILY);
            res.setHeader("Location", newLocation);
            res.flushBuffer();
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, JOTUtilities.class, "sendRedirect failed for:" + newLocation, e);
        }
    }

    /**
     * Computes an SHA1 hash as an Hex format string.
     * @param input
     * @return
     * @throws java.lang.Exception
     */
    public static String getSHA1Hash(String input) throws Exception
    {
        //calculate hash
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(input.getBytes());
        // convert to Hexadecimal
        BigInteger bi = new BigInteger(hash);
        return bi.toString(16);
    }

    /**
     * Checks if "f" is within "folder" hierarchy (recursively)
     * ie: if folder is an ancestor of f.
     * @param f
     * @param folder
     * @return
     */
    public static boolean isWithinFolder(File f, File folder)
    {
        // we reached file system root without success
        if (f == null)
        {
            if (folder == null)
            {
                return true;
            } else
            {
                return false;
            }
        }
        // keep browsing from "f" up until we find "folder"
        if (f != null && f.equals(folder))
        {
            return true;
        } else
        {
            return isWithinFolder(f.getParentFile(), folder);
        }
    }

    /**
     * Returns java standrad tmp dir (java.io.tmpdir)
     * @return
     */
    public static File getStandardTmpDir()
    {
        // Usually /tmp on unix    c:\\temp on windows
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Computes the MD5 hash of a file's content
     * @param f
     * @return
     * @throws java.lang.Exception
     */
    public static String getFileMd5(File f) throws Exception
    {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        InputStream is = new FileInputStream(f);
        byte[] buffer = new byte[50000];
        int read = 0;
        String md5 = null;
        try
        {
            while ((read = is.read(buffer)) != -1)
            {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            md5 = bigInt.toString(16);
        } catch (IOException e)
        {
            throw new Exception("Failed to compute MD5: ", e);
        } finally
        {
            try
            {
                is.close();
            } catch (IOException e)
            {
            }
        }
        return md5;
    }

    /**
     * Sorts the file listing
     * @return the sorted files
     * @param sortOrder: use one of the constants here such as SORT_BY_NAME_DESC
     */
    public static File[] sortFolderListing(File[] files, int sortOrder)
    {
        Comparator comp = JOTFileComparators.NAME_ASC_COMPARATOR;
        switch (sortOrder)
        {
            case SORT_BY_NAME_DESC:
                comp = JOTFileComparators.NAME_DESC_COMPARATOR;
                break;
            case SORT_BY_SIZE_ASC:
                comp = JOTFileComparators.SIZE_ASC_COMPARATOR;
                break;
            case SORT_BY_SIZE_DESC:
                comp = JOTFileComparators.SIZE_DESC_COMPARATOR;
                break;
            case SORT_BY_TSTAMP_ASC:
                comp = JOTFileComparators.TSTAMP_ASC_COMPARATOR;
                break;
            case SORT_BY_TSTAMP_DESC:
                comp = JOTFileComparators.TSTAMP_DESC_COMPARATOR;
                break;
        }
        Arrays.sort(files, comp);
        Collection coll = Arrays.asList(files);
        Vector v = new Vector(coll);
        return (File[])v.toArray(files);
    }

	public static String getShortClassname(Class clazz)
	{
		if(clazz==null) return "null";
		String name=clazz.getName();
		if(name!=null && name.indexOf(".")!=-1)
			name=name.substring(name.lastIndexOf(".")+1);
		return name;
	}
}

