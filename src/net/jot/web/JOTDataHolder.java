/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.util.Hashtable;


/**
 * JOT user data will be stored in this object
 * The getter and setters are in JOTDataHolderHelper too minimize this object footprint in memory. 
 * @author thibautc
 *
 */
public class JOTDataHolder
{
	public static final String SESSION_ID = "JOT_DATA_HOLDER";
	// Flow markers
	Hashtable markers=new Hashtable();
	// stores user attributes until the action completes
	//Hashtable actionStore=new Hashtable();
	// form data
	//Hashtable formStore=new Hashtable();
}
