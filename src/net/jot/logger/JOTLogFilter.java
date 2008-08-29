/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */

package net.jot.logger;

import java.util.Vector;

/**
 * An implementation of a TailFilter to retrieve only the "valid" log lines
 * It only accept the lines coreponding to the given log level.
 *
 *@author     tcolar
 *@created    May 6, 2003
 */
public class JOTLogFilter implements JOTTailFilter
{
    Vector levels;


    /**
     *@param  logLevels  Description of Parameter
     */
    public JOTLogFilter(Vector logLevels)
    {
        levels = logLevels;
    }


    /**
     *Description of the Method
     *
     *@param  s  Description of Parameter
     *@return    Description of the Returned Value
     */
    public boolean acceptLine(String s)
    {
    	boolean result=false;
        // check if this line's level matches the wanted levels.
        if (s != null && s.length() > 0)
        {
            String level = s.split(" ")[0];
            //System.out.println("line level: '"+level+"'  VS  "+levels+" "+s);
            result=levels.contains(new Integer(level));
        }
       	//System.out.println("Testing line: "+s+" -> "+result);
        return result;
    }

}

