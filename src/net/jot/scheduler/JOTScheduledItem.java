/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.scheduler;

/**
 * A scheduledItem is an object that should run an action on a specific schedule
 * Implement this interface to create your own scheduled object.
 * 
*/
public interface JOTScheduledItem
{
	/**
	 * Implement here what should be run at the scheduled time.
	 * Note: you should not call it from your own code run() but instead use forceRun() if you want to force a run.
	 */
	public void run();
	
	/**
	 * Implement if you want to skip run in particular cases (say while you do something else)
	 * Or just return false to never skip.
	 */
	public boolean skipRun();
	
	/**
	 * Implement if you want to FORCE a run right away (well within a few ms anyway)
	 * Returns false by default.
	 */
	public boolean forceRun();
	
	/**
	 * Will be called once a run has completed.
	 * Use this if you nedd to wait for a run to complete.
	 * Or if you want to fire some sort of event once the job is complete.
	 */
	public void runCompleted();
}
