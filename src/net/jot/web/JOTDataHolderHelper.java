/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import net.jot.logger.JOTLogger;

/**
 * Implementation methods for JOTDataHolder, to keep JOTDataHolder small.
 * @author thibautc
 */
public class JOTDataHolderHelper
{
	public static void updateMarkers(JOTDataHolder data, Vector markersToKeep)
	{
		if(data!=null)
		{
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTDataHolderHelper.class, "Update markers, before: "+data.markers);
			// clear markers except keepers
			if(data.markers!=null)
			{
				Enumeration markers=data.markers.keys();
				while(markers.hasMoreElements())
				{
					String marker=(String)markers.nextElement();
					if(! markersToKeep.contains(marker))
					{
						data.markers.remove(marker);
					}
				}
			}
			else
			{
				data.markers=new Hashtable();
			}
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTDataHolderHelper.class, "Update markers, after: "+data.markers);
		}

	}
	
	public static void setMarker(JOTDataHolder data, String marker, String value)
	{
		data.markers.put(marker, value);
	}

	public static String findMarker(JOTDataHolder data, String marker)
	{
		String result=null;
		if(marker!=null && data!=null)
		{
			if(data.markers.containsKey(marker))
				result=(String)data.markers.get(marker);
		}
		return result;
	}


	public static JOTDataHolder getDataHolder(HttpSession session)
	{
		JOTDataHolder jotData= (JOTDataHolder)session.getAttribute(JOTDataHolder.SESSION_ID);
   	 	if(jotData==null)
   	 	{   	 		
   	 		jotData=new JOTDataHolder();
   	 		session.setAttribute(JOTDataHolder.SESSION_ID, jotData);
   	 	}
   	 	return jotData;
	}
}
