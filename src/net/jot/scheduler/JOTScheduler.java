/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.scheduler;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.jot.logger.JOTLogger;

/**
 * Generic scheduler, that can run scheduled task/items.
 * any task can be registered into the scheduler using registerItem()
 * Works like a cron mostly.
 * It is started by JOT at startup (by JOTInitializer)
 * Note that new jobs are looked for every minute, so the "granularity" is about 1 minute.
 * @author thibautc
 *
 */
public class JOTScheduler extends Thread
{
	private static JOTScheduler instance=null;
	private int lastMinute=-1;
	private boolean stop=false;
	private boolean done=false;
	
	// Hashtables are synchronized so they are good here.
	private Hashtable items=new Hashtable();
	private Hashtable itemOptions=new Hashtable();
	private Hashtable itemThreads=new Hashtable();
	private Hashtable itemLastRun=new Hashtable();
	
	private JOTScheduler()
	{
	}
	
	public static JOTScheduler getInstance()
	{
		synchronized(JOTScheduler.class)
		{
			if(instance==null)
			{
				instance=new JOTScheduler();
			}
		}
		return instance;
	}
	
	/**
	 * Shuts down the scheduler and release resources
	 * @throws Exception
	 */
	public void shutdown() throws Exception
	{
		JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Scheduler shutdown");
		stop=true;
		while(! done)
		{
			Thread.sleep(10000);
		}
		
		//TODO: = what about child process (scheduled items cuurently running)
		Enumeration e=itemThreads.elements();
		while(e.hasMoreElements())
		{
			Vector v=(Vector)e.nextElement();
			for(int i=0;i!=v.size();i++)
			{
				JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Scheduler shutdown, killing subprocess.");
				Thread t=(Thread)v.get(i);
				t.interrupt();
				//t.destroy();
                                t=null;
			}
		}
		instance.interrupt();
		//instance.finalize();
		instance=null;
		JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Scheduler shutdown completed.");
	}
	
        /**
         * Register an item(ie: a job) in the scheduler.
         * 
        @param item 
        @param options 
        */
	public void registerItem(JOTScheduledItem item, JOTSchedulingOptions options)
	{
		String objectName=item.getClass().getName();
		JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Registering scheduled item: "+objectName);
		if(items.get(objectName)!=null)
                    JOTLogger.log(JOTLogger.WARNING_LEVEL, JOTScheduler.class, " ! Duplicated scheduled item: "+objectName);
		items.put(objectName,item);
		itemOptions.put(objectName,options);
                
                if(!options.isStartNow())
                {
                    Calendar now=JOTClock.getNow();
                    itemLastRun.put(objectName,new Long(now.getTime().getTime()));
                }
	}
        /**
         * Unregisters a job from the sheculer, ie: remove it.
         * @param item
         */
	public void unregisterItem(JOTScheduledItem item)
	{
		String objectName=item.getClass().getName();
		JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Unregistering scheduled item: "+objectName);
		items.remove(objectName);
		itemOptions.remove(objectName);
	}
	
	/**
         * Main scheduler loop.
         * Once scheduler.start() is call, it should run forever.
         */
	public void run()
	{
		try
		{
			JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Scheduler started.");
			while(!stop)
			{
				Calendar now=JOTClock.getNow();
				int minute=now.get(Calendar.MINUTE);
				if(minute!=lastMinute)
				{
					// start child thread to process potential tasks in the current minute.
					new JOTSchedulerRunner(now).start();
					lastMinute=minute;
				}
				sleep(5000);
			}
		}
		catch(Exception e)
		{
			JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Error in scheduler.", e);
		}
		done=true;
	}
	
        
	/**
	 * Inner class
	 * Individual "minute" cron runner, will process all scheduled items for the given "minute"
	 * @author thibautc
	 *
	 */
	private class JOTSchedulerRunner extends Thread
	{
		private Calendar now=null;
		
		JOTSchedulerRunner(Calendar now)
		{
			this.now=now;
		}
		
                /**
                 * Loops through all the items/jobs and run them if their shedule matches the current time.
                 */
		public void run()
		{
                    try
                    {
                        Enumeration keys=items.keys();
			while(keys.hasMoreElements())
			{
				String key=(String)keys.nextElement();
				JOTLogger.log(JOTLogger.CAT_MAIN,JOTLogger.DEBUG_LEVEL, JOTScheduler.class, "** Start Key: "+key);						
				JOTScheduledItem item=(JOTScheduledItem)items.get(key);
				JOTSchedulingOptions options=(JOTSchedulingOptions)itemOptions.get(key);
				Vector threads=(Vector)itemThreads.get(key);
				if(threads==null)
                                {
					threads=new Vector();
                                }
				if(item!=null && options!=null)
				{
                                   	boolean multipleAllowed=options.getThreadingScheme()==JOTSchedulingOptions.START_NEW_THREAD_IF_PREVIOUS_NOT_COMPLETED;
					
					// in those case no need to bother any further
                                        boolean shouldSkip=item.skipRun() || (! multipleAllowed && threads.size()>0);
					if(shouldSkip)
					{
						if((options.isStartNow() && itemLastRun.get(key)==null) ||
						 item.forceRun() ||
						 isMatchingRunEvery(key, options) ||
						 isMatchingSchedule(options))
						{
							String reason=item.skipRun()?"Item told us to skip this scheduled run.":"item already running.";
							JOTLogger.log(JOTLogger.CAT_MAIN,JOTLogger.DEBUG_LEVEL, JOTScheduler.class, "Scheduler: skipping item: "+item+" : "+reason);						
						}
					}
                                        if(!shouldSkip)
                                        {
					synchronized(this)
					{
						boolean run= (options.isStartNow() && itemLastRun.get(key)==null) ||
								 item.forceRun() ||
								 isMatchingRunEvery(key, options) ||
								 isMatchingSchedule(options)
								 ;
					
						if(run)
						{
							// saving lastrun stamp
							itemLastRun.put(key,new Long(now.getTime().getTime()));
							// starting scheduledItem in new Thread and saving in itemThread
							// Note: thread will "remove itself" from itemThread once done.
							JOTSchedulerItemThread thread=new JOTSchedulerItemThread(key,item);
							threads.add(thread);
							itemThreads.put(key,threads);
							thread.start();
						}
					}
                                        }
                                    JOTLogger.log(JOTLogger.CAT_MAIN,JOTLogger.DEBUG_LEVEL, JOTScheduler.class, "** End Key: "+key);						
				}
			}
                    } 
                    catch(Exception e)
                    {
				JOTLogger.logException(JOTLogger.CAT_MAIN,JOTLogger.ERROR_LEVEL, JOTScheduler.class, "Error in main scheduler thread: ",e);						                        
                    }
		}

		// Compare "now" with lastrun to see if it's time to run again
		private boolean isMatchingRunEvery(String key,  JOTSchedulingOptions options)
		{
			long mn=options.getRunEvery();
                        Long lastRun=(Long)itemLastRun.get(key);
                        if(mn==-1)
                        {
				return false;
                        }
                        long howLongAgo=now.getTime().getTime()-lastRun.longValue();
			boolean result=lastRun!=null && howLongAgo>mn*60000;
 			return result;
		}

		// Compare "now" with the item schedule
		private boolean isMatchingSchedule( JOTSchedulingOptions o)
		{
			boolean match=o.isScheduleEnabled()
                        && (o.getRunAtMonth()==null || o.getRunAtMonth().contains(""+now.get(Calendar.MONTH))) 
                        && (o.getRunAtWeekDays()==null || o.getRunAtWeekDays().contains(""+now.get(Calendar.DAY_OF_WEEK))) 
                        &&(o.getRunAtDays()==null || o.getRunAtDays().contains(""+now.get(Calendar.DAY_OF_MONTH)))
                        && (o.getRunAtHours()==null || o.getRunAtHours().contains(""+now.get(Calendar.HOUR_OF_DAY))) 
                        && (o.getRunAtMinutes()==null || o.getRunAtMinutes().contains(""+now.get(Calendar.MINUTE)));
                        //JOTLogger.log(JOTLogger.CAT_MAIN,JOTLogger.INFO_LEVEL,JOTScheduler.class,"howlong: "+howLongAgo+" "+result);
                       return match;
                }
	}
	
	/**
	 * This thread handles running a particular item/job
	 * @author thibautc
	 *
	 */
	private class JOTSchedulerItemThread extends Thread
	{
		private String name;
		private JOTScheduledItem item;
		
		JOTSchedulerItemThread(String name, JOTScheduledItem item)
		{
			this.name=name;
			this.item=item;
		}
		
                /**
                 * Calls the actual item run() method.
                 */
		public void run()
		{
                    try
                    {
			long start=JOTClock.getCurrentTime();
			JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Starting scheduled item: "+name);
			
			// running item
			item.run();
			item.runCompleted();
			
			// done: removing myself from itemThread
			Vector threads=(Vector)itemThreads.get(name);
			threads.remove(this);
			itemThreads.put(name,threads);
			
			long end=JOTClock.getCurrentTime();
			long time=end-start;
			JOTLogger.log(JOTLogger.INFO_LEVEL, JOTScheduler.class, "Completed scheduled item: "+name+" in "+time+"ms.");
                    } 
                    catch(Exception e)
                    {
				JOTLogger.logException(JOTLogger.CAT_MAIN,JOTLogger.ERROR_LEVEL, JOTScheduler.class, "Error in scheduler item thread: ",e);						                        
                    }
		}
	}
}
