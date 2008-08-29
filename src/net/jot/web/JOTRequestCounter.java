/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.util.Date;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;

/**
 *Use to "throttle" requests
 * You create a counter with a  unique name and how many minutes it stores data for (in memory)
 * then call addRequest() to count how many request for this IP, in the time span.
 * @author tcolar
 */
public class JOTRequestCounter
{
    int minutes=60;
    long tableDate=new Date().getTime();
    Hashtable table=new Hashtable();
    
    public JOTRequestCounter(int minutes)
    {
        this.minutes=minutes;
    }

    /**
     * Count a request and returns how many from this IP in the timespan(including this one)
     * @param request
     * @return
     */
    public int countRequest(HttpServletRequest request)
    {
        Date now=new Date();
        if(now.getTime()>tableDate+minutes*60000)
        {
            table.clear();
            tableDate=now.getTime();
        }
        
        String ip=request.getRemoteAddr();
        int value=1;
        Object o=table.get(ip);
        if(o!=null)
        {
            value=((Integer)o).intValue()+1;;
        }
            
        table.put(ip, new Integer(value));
        return value;
    }
}
