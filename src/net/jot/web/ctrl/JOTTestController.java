/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.ctrl;

import net.jot.logger.JOTLogger;

public class JOTTestController extends JOTController 
{

	/**
         * This is a dummy controller that just does nothing but print a debug message to the log file when called
         * Used for testing or as atemporary controller until a real one is written.
         * @return
         * @throws java.lang.Exception
         */
	public String process() throws Exception 
	{
		JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, this, "*** Test controller was called! ***");
		return RESULT_SUCCESS;
	}

	public boolean validatePermissions()
	{
		return true;
	}

}
