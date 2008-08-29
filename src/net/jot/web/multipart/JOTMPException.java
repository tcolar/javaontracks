/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.multipart;

/**
Exception thrownm when parsing a multipart form request fails.
@author thibautc
*/
public class JOTMPException extends Exception
{
	public JOTMPException(String message)
	{
		super(message);
	}
}
