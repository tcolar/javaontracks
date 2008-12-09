/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import net.jot.logger.JOTLogger;

/**
 * Helper to write a google sitemap
 * @author thibautc
 */
public class JOTGoogleSitemapBuilder
{

    String xml = "";

    public JOTGoogleSitemapBuilder()
    {
    }

    /** return te generated xml as a string; */
    public String toString()
    {
        return xmlWrap(xml);
    }

    /**
     * Save the generated xml to a file
     * @param file      file name
     * @param gzipped   whether to gzip the file
     */
    public void saveAs(File file, boolean gzipped)
    {
        OutputStream os = null;
        try
        {
            if(gzipped)
            {
                os=new FileOutputStream(file);
                GZIPOutputStream gos=new GZIPOutputStream(os);
                gos.write(xmlWrap(xml).getBytes());
                gos.finish();
            }
            else
            {
                os = new FileOutputStream(file);
                os.write(xmlWrap(xml).getBytes());
                os.flush();
            }
        } catch (Exception e)
        {
            JOTLogger.logException(this, "Failed writting sitemap to :" + file.getAbsolutePath(), e);
        } finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                } catch (Exception e2)
                {
                }
            }
        }
    }

    private String xmlWrap(String xml)
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" + xml + "</urlset>";
    }

    /**
     * url: full url http://www.sealybedding.com/something
     * chnageFreq: daily,weekly, hourly,monthly,always etc...
     * priority 0 - 1   (0.5= default, 1 is highest)
     * @param url
     * @param changeFreq
     * @param priority
     */
    public void addEntry(String url, String priority, String changeFreq)
    {
        url = url.replaceAll("&", "&amp;");
        String entry = "\t<url>\n";
        entry += "\t\t<loc>" + url + "</loc>\n";
        entry += "\t\t<changefreq>" + changeFreq + "</changefreq>\n";
        entry += "\t\t<priority>" + priority + "</priority>\n";
        entry += "\t</url>\n";

        xml += entry;
    }

    public void addEntry(String url, String priority)
    {
        addEntry(url, priority, "weekly");
    }

    public void addEntry(String url)
    {
        addEntry(url, "0.5");
    }

    /**
     * reset the xml (restart from scratch)
     */
    public void reset()
    {
        xml = "";
    }
}
