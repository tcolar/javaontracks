/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */
package net.jot.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

/**
 *
 * @author thibautc
 */
public class JOTTimezoneUtils
{

    static Hashtable commonZones = new Hashtable();
    public final static String AMERICA_ID = "America";
    public final static String EUROPE_ID = "Europe";
    public final static String ASIA_ID = "Asia";
    public final static String PACIFIC_ID = "Pacific";
    public final static String AFRICA_ID = "Africa";
    public final static String OTHERS_ID = "Others";

    public static String[] getTimezoneList()
    {
        return TimeZone.getAvailableIDs();
    }

    /**
     * Get the timezone list for a specific offset from UTC(in hours)
     * @param hoursOffset
     * @return
     */
    public static String[] getTimezoneList(int hoursOffset)
    {
        ArrayList list = new ArrayList();
        list.add("");
        String[] s1 = TimeZone.getAvailableIDs(hoursOffset * 3600000);
        for (int i = 0; i != s1.length; i++)
        {
            list.add(s1[i]);
        }
        return (String[]) list.toArray(new String[0]);
    }

    /**
     * Add possible client list from it's current GMT offset in hours
     * We add the xones for the current offest and also the ones for offset-1 and offset+1 because
     * we have no way to know (or at leats that i know of) wether the client is currently in Daylight savings or not
     * by adding the 1hour before and after timezones this should cover the potential offsets at those times of the year
     * where the client is in DST but GMT is not yet.
     * Ex: client in the US during late march is -7 vs GMT(not swicthed to summer time yet) rather than usual -8.
     * @param hoursOffset
     * @return
     */
    public static ArrayList getPossibleUserTimezoneList(int userOffsetInHours)
    {
        ArrayList list = new ArrayList();
        String[] s1 = TimeZone.getAvailableIDs(userOffsetInHours * 3600000);
        for (int i = 0; i != s1.length; i++)
        {
            list.add(s1[i]);
        }
        s1 = TimeZone.getAvailableIDs((userOffsetInHours - 1) * 3600000);
        for (int i = 0; i != s1.length; i++)
        {
            list.add(s1[i]);
        }
        s1 = TimeZone.getAvailableIDs((userOffsetInHours + 1) * 3600000);
        for (int i = 0; i != s1.length; i++)
        {
            list.add(s1[i]);
        }
        return list;
    }

    public static String[] getCommonTimezoneList(String zoneId)
    {
        String[] result = null;
        if (commonZones.get(zoneId) == null)
        {
            synchronized (TimeZone.class)
            {
                Vector v = new Vector();
                v.add("");
                String[] zones = TimeZone.getAvailableIDs();
                for (int i = 0; i != zones.length; i++)
                {
                    if (zoneId.equals(AMERICA_ID) && (zones[i].startsWith("US/") || zones[i].startsWith("America/") || zones[i].startsWith("Canada/") || zones[i].startsWith("Mexico/")))
                    {
                        v.add(zones[i]);
                    } else if (zoneId.equals(PACIFIC_ID) && (zones[i].startsWith("Australia/") || zones[i].startsWith("Pacific/")))
                    {
                        v.add(zones[i]);
                    } else if (zoneId.equals(ASIA_ID) && (zones[i].startsWith("Asia/")))
                    {
                        v.add(zones[i]);
                    } else if (zoneId.equals(EUROPE_ID) && (zones[i].startsWith("Europe/")))
                    {
                        v.add(zones[i]);
                    } else if (zoneId.equals(AFRICA_ID) && (zones[i].startsWith("Africa/")))
                    {
                        v.add(zones[i]);
                    } else if (zoneId.equals(OTHERS_ID) && !(zones[i].startsWith("Africa/") || zones[i].startsWith("Europe/") || zones[i].startsWith("Asia/") || zones[i].startsWith("Australia/") || zones[i].startsWith("Pacific/") || zones[i].startsWith("US/") || zones[i].startsWith("America/") || zones[i].startsWith("Canada/") || zones[i].startsWith("Mexico/")))
                    {
                        v.add(zones[i]);
                    }
                }
                result = (String[]) v.toArray(new String[0]);
                commonZones.put(zoneId, result);
            }
        }
        return (String[])commonZones.get(zoneId);
    }

    public static void main(String args[])
    {
        System.out.println("====LOCAL====");
        String[] b = (String[]) getPossibleUserTimezoneList(-8).toArray(new String[0]);
        for (int i = 0; i != b.length; i++)
        {
            System.out.println("\t" + b[i].length());
        }

        System.out.println("====AMERICA====");
        String[] a = getCommonTimezoneList(AMERICA_ID);
        for (int i = 0; i != a.length; i++)
        {
            System.out.println("\t" + a[i].length());
        }
        System.out.println("====PACIFIC====");
        a = getCommonTimezoneList(PACIFIC_ID);
        for (int i = 0; i != a.length; i++)
        {
            System.out.println("\t" + a[i].length());
        }
        System.out.println("====EUROPE====");
        a = getCommonTimezoneList(EUROPE_ID);
        for (int i = 0; i != a.length; i++)
        {
            System.out.println("\t" + a[i].length());
        }
        System.out.println("====ASIA====");
        a = getCommonTimezoneList(ASIA_ID);
        for (int i = 0; i != a.length; i++)
        {
            System.out.println("\t" + a[i].length());
        }
        System.out.println("====AFRICA====");
        a = getCommonTimezoneList(AFRICA_ID);
        for (int i = 0; i != a.length; i++)
        {
            System.out.println("\t" + a[i].length());
        }
        System.out.println("===OTHERS====");
        a = getCommonTimezoneList(OTHERS_ID);
        for (int i = 0; i != a.length; i++)
        {
            System.out.println("\t" + a[i].length());
        }

    }
}
