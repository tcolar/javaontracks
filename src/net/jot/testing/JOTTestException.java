/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.testing;

/**
 * This is the Exception to be thrown by the jotTest() method, if a test fails.
 * 
 * @author Thibaut Colar http://jot.colar.net/
 *
 */
public class JOTTestException extends Exception 
{

	public JOTTestException(String string) 
	{
		super(string);
	}

}
