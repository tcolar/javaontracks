/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.captcha;

import java.util.Date;
import java.util.Vector;
import javax.servlet.http.HttpServletResponse;
import net.jot.captcha.JOTCaptchaGeneratorInterface;
import net.jot.captcha.generators.JOTSTDCaptchaGenerator;
import net.jot.image.JOTAbstractImageWriterInterface;
import net.jot.image.writers.JOTBMPImageWriter;
import net.jot.logger.JOTLogger;
import net.jot.web.JOTRequestCounter;
import net.jot.web.view.JOTView;

/**
 * Note, that it defaults to allow a maximum of 30 captcha per IP per 10 mn, after that it disallows for 1 hour.
 * This might cause issue if behind a proxy (all users show with proxy IP)
 * @author tcolar
 */
public class JOTSendCaptchaView extends JOTView
{
    public final static String CAPTCHA_SESSION_ID="_JOT_CAPTCHA_ID";
    public static JOTRequestCounter counter = new JOTRequestCounter(10);
    public static boolean enableSecurity = true;
    public static JOTAbstractImageWriterInterface writer = new JOTBMPImageWriter();
    public static JOTCaptchaGeneratorInterface gen = new JOTSTDCaptchaGenerator();
    public static int maxRequestPerIPPer10Mn = 30;
    public static int blockIPForMn = 60;
    Vector blockedIps = new Vector();
    long blockedTime = new Date().getTime();

    public void prepareViewData() throws Exception
    {
        if (enableSecurity)
        {
            String ip = request.getRemoteAddr();
            int value = counter.countRequest(request);
            if(blockedTime>new Date().getTime()+blockIPForMn*60000)
            {
                blockedIps.clear();
            }
            if (value > maxRequestPerIPPer10Mn)
            {
                blockedIps.add(ip);
                JOTLogger.log(JOTLogger.WARNING_LEVEL, this, "Blocking Capctcha request for: "+ip);
            }
            if (blockedIps.contains(ip))
            {
                session.removeAttribute(CAPTCHA_SESSION_ID);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                response.flushBuffer();
            }
        }
        String captcha=gen.writeToBrowser(writer, response);
        session.setAttribute(CAPTCHA_SESSION_ID,captcha);
    }

    public boolean validatePermissions()
    {
        return true;
    }

    /**
     * call when initializing your app if you want to enable security
     * @param enableSecurity: enable or not
     */
    public static void setEnableSecurity(boolean enableSecurity)
    {
        JOTSendCaptchaView.enableSecurity = enableSecurity;
    }

    /**
     * call during your app initialization if you want to use a cutom generator rather than the default one
     * @param gen
     */
    public static void setGenerator(JOTCaptchaGeneratorInterface gen)
    {
        JOTSendCaptchaView.gen = gen;
    }

    /**
     *  call during your app initialization if you want to use a cutom imageWriter rather than the default one
     *
     * @param writer
     */
    public static void setImageWriter(JOTAbstractImageWriterInterface writer)
    {
        JOTSendCaptchaView.writer = writer;
    }
    
    
    public static void setBlockIPForMn(int blockIPForMn)
    {
        JOTSendCaptchaView.blockIPForMn = blockIPForMn;
    }

    public static void setMaxRequestPerIPPer10Mn(int maxRequestPerIPPer10Mn)
    {
        JOTSendCaptchaView.maxRequestPerIPPer10Mn = maxRequestPerIPPer10Mn;
    }

    public static JOTCaptchaGeneratorInterface getGenerator()
    {
        return gen;
    }

    
}

