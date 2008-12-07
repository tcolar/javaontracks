/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.captcha;

import java.io.File;
import javax.servlet.http.HttpServletResponse;
import net.jot.image.JOTAbstractImageWriterInterface;

/**
 *
 * @author thibautc
 */
public interface JOTCaptchaGeneratorInterface 
{
    /**
     * Creates a captcha image
     * Return captcha code
     * @param writer
     * @param imageFile
     * @throws java.lang.Exception
     */
    public String writeToFile(JOTAbstractImageWriterInterface writer, File imageFile) throws Exception;

    /**
     * Return a string contatining all the possible chars that can might be in the captcha
     */
    public String getChars();
    
    /**
     * Sends the captchca straight to the browser (sets headers etc..).
     * Note that it will close(commit) the response.
     * ex: writeBrowser(writer,responset,"CAPTCHA_CODE");
     * returns the captcha code.
     */
    public String writeToBrowser(JOTAbstractImageWriterInterface writer, HttpServletResponse response) throws Exception;
}
