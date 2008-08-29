/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import net.jot.logger.JOTLogger;

/**
 * This is a mutithreaded emailer program,
 * it uses raw connection (socket) to an smtp server to send mail.
 * It is mutlithread so that sending the email in the background.(which can take long if the mail
 * server is oveloaded)
 *
 *@author     tcolar
 *@created    May 6, 2003
 */
public class JOTEmailSender
{

    static String[] days = {"Sun", "Mon", "Tue", "Wen", "Thu", "Fri", "Sat"};
    static String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    String domain = "localhost";
    String host = null;
    int port = 25;
    String replyTo = "postmaster";
    String bounceTo = "postmaster";
    String from = "postmaster";
    boolean enabled = true;

    /**
     *Constructor for the Mailer object
     *
     *@param  prefs  Description of Parameter
     */
    public JOTEmailSender()
    {
    }

    /**
     *Sends an email using this emailer
     *
     *@param  to       Description of Parameter
     *@param  subject  Description of Parameter
     *@param  text     Description of Parameter
     */
    public void send(String to, String subject, String text)
    {
        if (isEnabled() && host != null)
        {
            new MailSender(to, subject, text).start();
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "sending email to: " + to);
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     *Description of the Method
     *
     *@param  i  Description of Parameter
     *@return    Description of the Returned Value
     */
    public String twoDigits(int i)
    {
        String result = "" + i;
        if (result.length() == 1)
        {
            result = "0" + result;
        }
        return result;
    }

    /**
     * Individual MailSender thread, responsible to handle a single "send mail" request.
     *
     *@author     tcolar
     *@created    May 6, 2003
     */
    protected class MailSender extends Thread
    {

        StringBuffer text;
        BufferedReader smtpReader = null;
        BufferedWriter smtpWriter = null;
        String to = "";
        String subject = "";
        String msg = null;

        /**
         *Constructor for the MailSender object
         *
         *@param  to       Description of Parameter
         *@param  subject  Description of Parameter
         *@param  message  Description of Parameter
         */
        public MailSender(String to, String subject, String message)
        {
            this.to = to;
            this.subject = subject;
            this.msg = format(message);
        }


        // sending the email
        /**
         *Main processing method for the Mailer object
         */
        public void run()
        {
            try
            {
                sendIt();
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed connecting to mailserver: " + host + ":" + port, e);
            }

        }

        public void sendIt() throws Exception
        {
            // constructing the date
            // in a format that mail server understands.
            String date = "";
            GregorianCalendar cal = new GregorianCalendar();
            date += days[cal.get(Calendar.DAY_OF_WEEK) - 1] + ", ";
            date += twoDigits(cal.get(Calendar.DAY_OF_MONTH)) + " ";
            date += months[cal.get(Calendar.MONTH)] + " ";
            date += cal.get(Calendar.YEAR) + " ";
            date += twoDigits(cal.get(Calendar.HOUR_OF_DAY)) + ":";
            date += twoDigits(cal.get(Calendar.MINUTE)) + ":";
            date += twoDigits(cal.get(Calendar.SECOND)) + " ";
            date += "-0800";

            Socket s = null;
            s = new Socket(host, port);
            // open mail server SMTP port
            smtpReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            smtpWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            smtpReader.readLine();
            smtpWriter.write("HELO " + domain);
            smtpWriter.newLine();
            smtpWriter.flush();
            smtpReader.readLine();
            smtpWriter.write("MAIL FROM: " + bounceTo+"@"+domain);
            smtpWriter.newLine();
            smtpWriter.flush();
            smtpReader.readLine();
            smtpWriter.write("RCPT TO: " + to);
            smtpWriter.newLine();
            smtpWriter.flush();
            smtpReader.readLine();
            smtpWriter.write("DATA");
            smtpWriter.newLine();
            smtpWriter.flush();
            smtpReader.readLine();
            smtpWriter.write("Date: " + date);
            smtpWriter.write("\r\n");
            smtpWriter.write("From: " + from + " <" + from + "@" + domain + ">");
            smtpWriter.write("\r\n");
            smtpWriter.write("Reply-To: " + replyTo+"@"+domain);
            smtpWriter.write("\r\n");
            smtpWriter.write("To: " + to);
            smtpWriter.write("\r\n");
            smtpWriter.write("Message-ID: " + new java.util.Date().getTime() + "@" + domain);
            smtpWriter.write("\r\n");
            smtpWriter.write("Subject: " + subject);
            smtpWriter.write("\r\n\r\n");
            smtpWriter.write("" + msg);
            smtpWriter.write("\r\n");
            smtpWriter.write(".");
            smtpWriter.write("\r\n");
            smtpWriter.flush();
            String code = smtpReader.readLine();
            if (code.charAt(0) != '2')
            {
                JOTLogger.log(JOTLogger.ERROR_LEVEL, this, "Error while sending email:" + code);
            }
            smtpWriter.write("QUIT");
            smtpWriter.newLine();
            smtpWriter.flush();
            smtpReader.readLine();

            s.close();

        }

        /**
         *Description of the Method
         *
         *@param  msgString  Description of Parameter
         *@return            Description of the Returned Value
         */
        public String format(String msgString)
        {
            StringBuffer msg = new StringBuffer(msgString);
            for (int i = 0; i != msg.length(); i++)
            {
                if (msg.charAt(i) == '\n')
                {
                    if (!(i != 0 && msg.charAt(i - 1) == '\r'))
                    {
                        msg.insert(i, '\r');
                    }
                }
            }
            return msg.toString();
        }
    }

    public void setBounceTo(String bounceTo)
    {
        this.bounceTo = bounceTo;
    }

    public static void setDays(String[] days)
    {
        JOTEmailSender.days = days;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public static void setMonths(String[] months)
    {
        JOTEmailSender.months = months;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
    }
}


