/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.views;

import net.jot.logger.JOTLogger;
import net.jot.web.forms.JOTGeneratedForm;
import net.jot.web.view.JOTView;

/**
 * Generic form display View for generatedForm using the builtin form template.
 */
public abstract class JOTGeneratedFormView extends JOTView
{

  public static final String GENERATED_FORM = "generatedForm";

  public void prepareViewData() throws Exception
  {
    JOTGeneratedForm form = (JOTGeneratedForm) request.getAttribute(GENERATED_FORM);
    
    String htmlForm = "";
    if(form!=null)
    {
        htmlForm=form.getHtml(request);
    }
    else
    {
        JOTLogger.log(JOTLogger.INFO_LEVEL, this, "GeneratedForm request attribute is missing ! not adding any form to the HTML.");
    }
    addVariable(GENERATED_FORM, htmlForm);
  }
}
