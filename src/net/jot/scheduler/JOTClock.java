/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.scheduler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

/**
 *Collection of utilities related to Date/Time
 *@author     tcolar
 *@created    September 25, 2001
 */
public class JOTClock
{
        private static Calendar calendar = null;
        public static final DateFormat TIMESTAMP_FORMAT=new SimpleDateFormat("M_d_yyyy_H_m_s");
        public static final DateFormat TIMESTAMP_WITH_MS_FORMAT=new SimpleDateFormat("M_d_yyyy_H_m_s-S");
        public static final Pattern TIMESTAMP_PATTERN=Pattern.compile(".*_(\\d+_\\d+_\\d+_\\d+_\\d+_\\d+(\\-\\d+)?).*");

        /**
         * Gets the current Date
         *
         *@return    The currentDate value
         *@since
         */
        public static Date getCurrentDate()
        {
                calendar = new GregorianCalendar();		
                return calendar.getTime();
        }

        public static int getField(int field)
        {
                calendar = new GregorianCalendar();		
                return calendar.get(field);
        }
        
        public static Calendar getNow()
        {
                calendar = new GregorianCalendar();
                return calendar;
        }
        
        
        /**
         *Gets the current Time in ms since 1970
         *
         *@return    The currentTime value
         *@since
         */
        public static long getCurrentTime()
        {
                return getCurrentDate().getTime();
        }
        
        /**
         * return the date in a fomat containing no spaces, mainly used as part of file names
         * @return
         */
    	public static String getDateStringWithMs()
    	{
    		Calendar cal=JOTClock.getNow();
                return TIMESTAMP_WITH_MS_FORMAT.format(cal.getTime());
    	}
        /**
         * Same, but without milliseconds
         * @return
         */
       	public static String getDateString()
    	{
    		Calendar cal=JOTClock.getNow();
                return TIMESTAMP_FORMAT.format(cal.getTime());
    	}
        
        /**
         * parse a date string created by getDateString of getDateStringWithMs back into a Date object
         * @param s
         * @return
         * @throws java.text.ParseException
         */
        public static Date parseDateString(String s) throws ParseException
        {
            if(s.indexOf("-")>0)
                return TIMESTAMP_WITH_MS_FORMAT.parse(s);
            else
                return TIMESTAMP_FORMAT.parse(s);                
        }
}

