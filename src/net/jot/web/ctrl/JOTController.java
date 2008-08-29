/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.ctrl;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.jot.logger.JOTLogger;
import net.jot.web.JOTDataHolder;
import net.jot.web.JOTDataHolderHelper;
import net.jot.web.JOTFlowClassCache;
import net.jot.web.JOTFlowConfig;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.JOTForm;
import net.jot.web.view.JOTView;

/**
 * Extends this generic class to implement a Controller
 * The Controller is here to process a request (do the logic required to process it)
 * It gives you easy access to the following objects:
 * request,response,session. filterConfig, filterChain
 * Note that request in a JOTFlowRequest that gives you setParameter as well as form parsing features(MultiPartRequest)
 * @author thibautc
 */
public abstract class JOTController
{
  /**
   * Return this if the controller processed succesfully (generic success message)
   */

  public static final String RESULT_SUCCESS = "success";
  /**
   * Return this if the controller failed (Exception/error etc...)
   */
  public static final String RESULT_FAILURE = "failure";
  /**
   * Special return code used when a user calls a controller he has no permission to.
   */
  public static final String RESULT_FORBIDDEN = "forbidden";
  /**
   * Special return code used for Forms(JOTForm), if the form data validation failed.
   */
  public static final String RESULT_VALIDATION_FAILURE = "validation_failure";
  /**
   * Special return code used for for a controller that will be called many times until ity his considered "completed"
   * For example the Filemanager where you can browse around until you pick a file(completed)
   */
  public static final String RESULT_COMPLETED = "completed";
  private JOTMasterController master;
  private JOTFlowConfig flowConfig;
  private JOTDataHolder data;
  // the things that will be useful to the user:

  /**
   * Gives you easy access to the request
   */
  public JOTFlowRequest request;
  /**
   * Gives you easy access to the Response
   */
  public HttpServletResponse response;
  /**
   * Gives you easy acess to the "Session" object
   */
  public HttpSession session;
  /**
   * Gives you easy acess to filterConfig should you need it
   */
  public FilterConfig filterConfig;
  /**
   * Gives you easy acess to filterChain should you need it
   */
  public FilterChain filterChain;

  protected JOTController()
  {
  }

  /**
   * Will be called by the mastercontroller.
   * @param master
   */
  public void init(JOTMasterController master)
  {
    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTFlowClassCache.class, "Initializing controller.");

    this.master = master;
    filterChain = master.getFilterChain();
    filterConfig = master.getFilterConfig();
    flowConfig = master.getFlowConfig();
    request = master.getRequest();
    response = master.getResponse();
    session = request.getSession();
    data = JOTDataHolderHelper.getDataHolder(session);
  }

  /**
   * "Hack" controller to be initialized from a view.
   * Used by widgets
   * @param view
   */
  public void init(JOTView view)
  {
    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTFlowClassCache.class, "Initializing controller.");
    filterChain = view.filterChain;
    filterConfig = view.filterConfig;
    request = view.request;
    response = view.response;
    session = view.session;
    data = JOTDataHolderHelper.getDataHolder(session);
  }


  /**
  * This is the implementaion of the "meat" of your controller, where the controller does it's stuff (logic)
   *Thta should return a result code, either a standard one. ie: RESULT_SUCCESS) or your custom one: "ie: goToHomePage"
  * @return The result of the process (possibly tested in flow.conf)
  * @throws Exception
  */
  public abstract String process() throws Exception;

  protected JOTDataHolder getDataHolder()
  {
    return JOTDataHolderHelper.getDataHolder(request.getSession());
  }

  /**
  * use this method to get/create a form object
  * Will provide either :
  * - a new form object if this form is newly used
  * - the existing form if used earlier in this request
  * (ie: validation failed)
   * This allows you to get the form and put it in the View, or to populate/validate it in your controller.
  * @param formClass
  * @return
  */
  public JOTForm getForm(Class formClass)
  {
    return master.getForm(formClass);
  }

  /**
  * Implements this to check wether the user is allowed to use your controller.
  * This is the simplest way to secure the application.
  * If this returns false, the controller won't be ran and a "RESULT_FORBIDDEN" will be sent.
  * The user can then be sent to a 'forbidden' page.
  * @return
  */
  public abstract boolean validatePermissions();
  
  public JOTMasterController getMaster()
  {
      return master;
  }
}
