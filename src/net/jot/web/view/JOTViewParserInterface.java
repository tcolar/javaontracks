/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.view;

/**
 * Implement to create a view parser.
 * @author thibautc
 */
public interface JOTViewParserInterface
{
	/**
	 * Process the template and view and return processed html
	 * Can make callbacks to JOTViewParser.parse() if recusring parsing needed
	 * @param template
	 * @param view
	 * @param templateRoot
	 */
	public String process(String template, JOTViewParserData view, String templateRoot) throws Exception;
}
