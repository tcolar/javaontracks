/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

/**
 * Object representation of a page/view in flow.conf
 * @author thibautc
 */
public class JOTFlowPage
{
        /**
         * code is the class name. ie: net.jotwiki.something.SomeClass
         */
	private Object code;
	private Object name;

	public JOTFlowPage(String code, String name)
	{
		this.code=code;
		this.name=name;
	}

	public Object getCode()
	{
		return code;
	}

	public void setCode(Object code)
	{
		this.code = code;
	}

	public Object getName()
	{
		return name;
	}

	public void setName(Object name)
	{
		this.name = name;
	}
}
