/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.ajax;

import javax.servlet.http.HttpServletResponse;
import net.jot.web.JOTFlowRequest;

/**
 *
 * @author thibautc
 */
public interface JOTAjaxProvider 
{

    /**
     * Execute the ajax call and write to the response / flush it
     * ie: send the updated data that will be used by the javascript callback method
     * Params are in the request (getParameter)
     * @param request
     * @param response
     * @return
     */
    public void executeAjaxCall(JOTFlowRequest request, HttpServletResponse response) throws Exception;
    
    /**
     * return the path of the action(implementing JOTAjaxProvider) to be called when an ajax call is made:
     * Ex: "myAjax.do"
     * @return
     */
    public String getAjaxAction();

    /**
     * returns a name for the javascript function that will be created for this action
     * THIS SHOULD NOT CONTAIN ANY SPECIAL CHARS LIKE SPACES ETC....
     * (preferably unique ex: class.getSimpleName()
     * Ex: "ajax_exampleWidget"
     * @return
     */
    public String getJscriptFuncName();
    
    /**
     * returns the name of the javascript function you wrote, which will be called once data is returned from the ajax call
     * ie: updateData
     * @return
     */
    public String getJscriptCallbackFunctionName();


}
