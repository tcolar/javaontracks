/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.ctrl;

import java.util.Hashtable;

import net.jot.logger.JOTLogger;
import net.jot.web.forms.JOTForm;

/**
 * Generic class you can extend to create a formValidation Controller
 * @author thibautc
 */
public abstract class JOTFormValidationController extends JOTController
{
	private String result=RESULT_FAILURE;
	
        /**
         * Calls the form validate() method
         * @param form
         * @return
         */
	public Hashtable validateForm(JOTForm form)
	{
		Hashtable result=new Hashtable();
		try
		{
			result=form.validate(request);
			if(result.size()==0)
				this.result=RESULT_SUCCESS;
		}
		catch(Exception e)
		{
			JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Form validation failed! ",e);
		}
		return result;
	}

        /**
         * Returns the form validation results.
         * @return
         * @throws java.lang.Exception
         */
	public String process() throws Exception
	{
		return result;
	}
}
