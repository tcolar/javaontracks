/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms.ui;

import net.jot.captcha.JOTCaptchaGeneratorInterface;
import net.jot.web.forms.JOTFormConst;

/**
 *
 * @author tcolar
 */
public class JOTFormCaptchaField extends JOTFormTextField
{

    private String captchcaUrl = "";

    public JOTFormCaptchaField(String name, String description, String captchcaServletUrl)
    {
        this(name, description, captchcaServletUrl,null);
    }
    
    /**
     * Generator: the generator that will be used to generate the image, when captchcaUrl is called
     * This is used to show in the help which chars might be used in the captcha
     * @param name
     * @param description
     * @param captchcaServletUrl
     * @param captchaGenerator
     */
    public JOTFormCaptchaField(String name, String description, String captchcaServletUrl, JOTCaptchaGeneratorInterface captchaGenerator)
    {
        super(name, description, 10, "");
        setType(JOTFormConst.INPUT_CAPTCHA);
        this.captchcaUrl = captchcaServletUrl;
        String help="Enter the Letters / Numbers (Left to Right) you see in the image into the text field.";
        
        if(captchaGenerator!=null)
            help+="<br>The code might contain the following Numbers or letters:<br>"+captchaGenerator.getChars();
        setHelp(help);
    }

    public String getCaptchcaUrl()
    {
        return captchcaUrl;
    }

    public void setCaptchcaUrl(String captchcaUrl)
    {
        this.captchcaUrl = captchcaUrl;
    }
}
