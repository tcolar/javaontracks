/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.scheduler;

import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Pattern;

import net.jot.logger.JOTLogger;

/**
 * This reprsents a scheduled job "schedule".
 * This is used to set schedule options
 * 
 */

public class JOTSchedulingOptions
{
	public final static String scheduleHelp="Schedule format: <br>" +
			"Month(s) Day(s) WeekDay(s) Hour(s) Minute(s)    : Space separated.<br>" +
			"Month: 0=january, 11=December<br>" +
			"Days of week: 0=Sunday, 6=Saturday<br>" +
			"Day of month: 1 = 1st of the month.<br>" +
			"Hours: 0=Midnight, 23=11PM<br>" +
			" <b>Examples:</b><br>" +
			"'* * * 5,14 0' any day at 5:00 AM and 2:00 PM<br>" +
			"'* * * 5,14 8' any day at 5:08 AM and 2:08 PM<br>" +
			"'* * 0 0 0' any sunday at 00:00 AM<br>" +
			"'* 1 * 0 0' any 1st of the month at 00:00 AM<br>" +
			"'* 13 5 0 0' any Friday the 13th at 00:00AM<br>" +
			"'* * * * *'  any minute of any day.<br>" +
			"'0 * * * *' any minute of any day in January<br>" +
			"'2 13 5 0 0' any Friday the 13th in March at 00:00 AM"; 

	public final static Pattern validator=Pattern.compile("[0-9,*]+ [0-9,*]+ [0-9,*]+ [0-9,*]+ [0-9,*]+");
	
	public final long EVERY_MINUTE=1;
	public final long EVERY_5_MINUTES=EVERY_MINUTE*5;
	public final long EVERY_15_MINUTES=EVERY_MINUTE*15;
	public final long EVERY_30_MINUTES=EVERY_MINUTE*30;
	public final long EVERY_HOUR=EVERY_MINUTE*60;
	public final long EVERY_3_HOURS=EVERY_HOUR*3;
	public final long EVERY_6_HOURS=EVERY_HOUR*6;
	public final long EVERY_12_HOURS=EVERY_HOUR*12;
	public final long EVERY_24_HOUR=EVERY_HOUR*24;
	
        /**
         * Only allow one call to run at a time
         * If one is already running will just skip the new call
         */
	public static final int DROP_NEW_CALLS_IF_PREVIOUS_NOT_COMPLETED=1;
        /**
         * Allow multiple concurrent calls
         */
	public static int START_NEW_THREAD_IF_PREVIOUS_NOT_COMPLETED=2;
	// TBD
	//final int QUEUE_NEW_CALLS_IF_PREVIOUS_NOT_COMPLETED=3;
	
	// Vectors: null=any
        private boolean useSchedule=false;
        private Vector runAtMonth=null;
	private Vector runAtDays=null;
	private Vector runAtWeekDays=null;
	private Vector runAtHours=null;
	private Vector runAtMinutes=null;
	
	private long runEvery=-1;
	private boolean startNow=false;
	
	private int threadingScheme=DROP_NEW_CALLS_IF_PREVIOUS_NOT_COMPLETED;
	
        /**
         * Set the threading scheme: ie:
         * - DROP_NEW_CALLS_IF_PREVIOUS_NOT_COMPLETED
         * - START_NEW_THREAD_IF_PREVIOUS_NOT_COMPLETED
         * @param scheme
         */
	public void setThreadingScheme(int scheme)
	{
		threadingScheme=scheme;
	}
	
	/**
	 * run the job at an interval (in milliseconds)
	 * ie: runEvery(EVERY_5_MINUTE);
	 * 
	 * minutes: interval in minutes.
	 * startingNow: if true then first run right away, otherwise, first run after "minutes"
	 */
	public void setRunEvery(long minutes, boolean startingNow)
	{
		runEvery=minutes;
		startNow=startingNow;
	}
	
        /**
         * Request a run right away
         * @param runNow
         */
 	public void setRunNow(boolean runNow)
	{
		startNow=runNow;
	}
	
	
       /**
         * Request a run "right now"
         * Very much like an unix cron line
         * But the order is different:<br>
         * Month(o=january) Day(1=1st) DayOfWeek(0=sunday) Hour Minute<br>
         * '* * * 5,14 0' any day at 5:00 AM and 2:00 PM<br>
         * '* * * 5,14 8' any day at 5:08 AM and 2:08 PM<br>
	 * '* * 0 0 0' any sunday at 00:00 AM<br>
	 * '* 1 * 0 0' any 1st of the month at 00:00 AM<br>
	 * '* 13 5 0 0' any Friday the 13th at 00:00AM<br>
	 * '* * * * *'  any minute of any day.<br>
	 * '0 * * * *' any minute of any day in January<br>
	 * '2 13 5 0 0' Friday the 13th in March at 00:00 AM";        
         * @param runNow
         */
	public void setRunAt(String cronEntry)
	{
            useSchedule=true;
		runAtMonth=null;
		runAtDays=null;
		runAtWeekDays=null;
		runAtMinutes=null;
		runAtHours=null;
		String[] entries=cronEntry.split(" ");
		if(entries.length<5)
		{
			JOTLogger.log(JOTLogger.ERROR_LEVEL, this, "Invalid CronType entry: "+cronEntry);
		}
		else
		{
			if(! entries[0].equals("*"))
				runAtMonth=new Vector(Arrays.asList(entries[0].split(",")));
			if(! entries[1].equals("*"))
				runAtDays=new Vector(Arrays.asList(entries[1].split(",")));
			if(! entries[2].equals("*"))
				runAtWeekDays=new Vector(Arrays.asList(entries[2].split(",")));
			if(! entries[3].equals("*"))
				runAtHours=new Vector(Arrays.asList(entries[3].split(",")));
			if(! entries[4].equals("*"))
				runAtMinutes=new Vector(Arrays.asList(entries[4].split(",")));
			JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Added cronTye: "+cronEntry);
		}
	}
	
	public Vector getRunAtDays()
	{
		return runAtDays;
	}

	public Vector getRunAtHours()
	{
		return runAtHours;
	}

	public Vector getRunAtMinutes()
	{
		return runAtMinutes;
	}

	public Vector getRunAtMonth()
	{
		return runAtMonth;
	}

	public Vector getRunAtWeekDays()
	{
		return runAtWeekDays;
	}

	public long getRunEvery()
	{
		return runEvery;
	}

	public boolean isStartNow()
	{
		return startNow;
	}

	public int getThreadingScheme()
	{
		return threadingScheme;
	}
	
	public static String getScheduleHelp()
	{
		return  scheduleHelp;
	}

	// check wether a schedule string format(cron like) looks valid.
	public static boolean isValid(String schedule)
	{
		return validator.matcher(schedule).matches();
	}

        /**
         * Wether a schedule item was added (ie: setRunAt() was called)
         * @return
         */
        boolean isScheduleEnabled()
        {
          return useSchedule;
        }
}
