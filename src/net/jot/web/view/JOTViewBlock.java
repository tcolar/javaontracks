/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.view;



/**
 * Represent a View block.
 * A block is simply a piece of html code marked as a block, which data can be replaced on the fly.
 * @author thibautc
 */
public class JOTViewBlock
{
	protected boolean visible=true;
	protected String content=null;
	
	public String getContent()
	{
		return content;
	}
	/**
	 * If a content is defined(not null), then the "HTML element" block content will be replaced by 
	 * the value of newConetnt.
	 * ie: <div jotid="toto">xyzxyz</div>
	 * If newParam is set to "aaaaa", the rendered HTML page will show: aaaaaa
	 * If newParam is set to null, the rendered HTML page will show: xyzxyz
	 * @param newContent default:null
	 */
	public void setContent(String newContent)
	{
		content = newContent;
	}
	public boolean isVisible()
	{
		return visible;
	}
	/**
	 * If set to false, the elemnt will be hidden "removed" from the generated HTML.
	 * @param visible default:true
	 */
	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}
	
	
}
