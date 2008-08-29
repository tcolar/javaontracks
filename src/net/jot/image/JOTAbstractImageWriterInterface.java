/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */

package net.jot.image;


import java.io.OutputStream;

/**
 * Implement this to provide an implementation that writes the abstractImage in a particular format.
 * 
 * @author thibautc
 */
public interface JOTAbstractImageWriterInterface 
{
    /**
     * Writes the abstractImage to a file according to impl. format
     * returns data length
     * @param abstractImage
     * @param file
     * @throws java.lang.Exception
     */
    public int writeToStream(JOTAbstractImage abstractImage, OutputStream out) throws Exception;

    /**
     * Return the image content type (to be sent to the browser)
     * @return
     */
    public String getContentType();
}
