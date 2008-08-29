/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.filebrowser;


import net.jot.utils.JOTConstants;
import net.jot.web.ctrl.JOTController;

/**
 * Generic File Browsing support controller, used to handle browsing the file system, uploading/picking files.
 * Extend this to create your own file browsing implementation
 * JOTFileBrowserHelper contains the actual actions such as deeting a file, picking one etc...
 * @author thibautc
 *
 */
public abstract class JOTFileBrowserController extends JOTController 
{
	public static final String JUST_RESET = "_JOT_JUST_RESET";
	// the resulting selected files(File[]) will be store in the trequest attribute with this name
	public static final String RESULTS_ATTRIBUTE = "_JOT_FM_SELECTED_FILES";
	
        /**
         * Process the browser request, ie: browse around/pcik a file etc...
         * @return
         * @throws java.lang.Exception
         */
	public String process() throws Exception 
	{
		if(JOTFileBrowserHelper.getFbSession(request)==null)
		{
			JOTFileBrowserSession fbSession=createFbSession();
			// if the session timedout we just cancel any actions and go back to just displaying default.
			if(request.getParameter("JOTConstants.SESSION_FB_ACTION") != null || (request.getContentType()!=null && request.getContentType().indexOf("multipart/form-data")!=-1))
			{
				// add a warning
				fbSession.setCurrentWarning(fbSession.getSessionTimeoutWarning());
			}
			// mark it was reset
			request.setParameter(JUST_RESET, "Yes");
			// clear the action
			request.setParameter(JOTConstants.SESSION_FB_ACTION, "NONE");

			JOTFileBrowserHelper.setFbSession(request,fbSession );
		}
		// this contains the meat of processing the action.
		String result=JOTFileBrowserHelper.updateFileManagerView(request, response);
		return result;
	}
	
        /**
         * Your implementation should create return a JOTFileBrowserSession, 
         * which basically tells the FileBrowser how to behave, what to allow and what is expected of the user.
         * @return
         */
	public abstract JOTFileBrowserSession createFbSession();

}
