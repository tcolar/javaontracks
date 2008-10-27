/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.view;

import java.util.Hashtable;

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
import net.jot.web.ctrl.JOTController;
import net.jot.web.ctrl.JOTMasterController;
import net.jot.web.forms.JOTForm;

/**
 * Abstract Base of a View (provides an empty view) 
 * The View is where you add variables to be used by the view.
 * A view is responsible for providing a set of variable to be merged with a template.
 * Used to extend controller, but that wasn't a clean design so split.
 * @author thibautc
 *
 */
public abstract class JOTView implements JOTViewParserData
{

    private boolean provideRequestParameters = false;
    private Hashtable variables = new Hashtable();
    private Hashtable blocks = new Hashtable();
    private Hashtable tags = new Hashtable();
    private Hashtable forms = new Hashtable();
    private String bTemplate = null;
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
     * You can call this method to use a "hardcode" template rather tha use one loaded from a template as usual.
     * @param template
     */
    public void setBuiltinTemplate(String template)
    {
        bTemplate = template;
    }

    /**
     * Adds a variable to this View
     * Then you can use the varibale in the view:
     * Ex: jot:var value="id"
     * @param name
     * @param value
     */
    public void addVariable(String id, Object value)
    {
        variables.put(id, value);
    }

    /**
     * Adds a Form to this View. The form can then be used/rendered in the template.
     * @param name
     * @param value
     */
    protected void addForm(JOTForm form)
    {
        forms.put(form.getClass().getName(), form);
    }

    /**
     * Defines a page block to be "defined" on the fly at runtime
     * For example <jot:block dataId="toto">blah</jot:block> will be replaced by the data provided
     * in the view element with dataId "toto" if it exists.
     * You can also use blocks to easily show/hide whole html parts
     * @param dataId
     * @param element
     */
    protected void addBlock(String id, JOTViewBlock element)
    {
        blocks.put(id, element);
    }

    /**
     * Defines an HTML tag to be "redefined" on the fly at runtime
     * For example <div jotid="toto"></div>, content will be replaced by the data provided
     * in the view element with dataId "toto" if it exists.
     * @param dataId
     * @param element
     */
    protected void addTag(String id, JOTViewBlock element)
    {
        tags.put(id, element);
    }

    /**
     * Wether to automatically provide all the request parameters & attributes to the view
     * TODO: is that implemented ?
     * Defaults to false.
     * @param b
     */
    protected void setProvideRequestParameters(boolean b)
    {
        provideRequestParameters = b;
    }

    /**
     * called  by mastercontroller
     * @return
     * @throws java.lang.Exception
     */
    public final String process() throws Exception
    {
        prepareViewData();
        return JOTController.RESULT_SUCCESS;
    }

    /**
     * To be implemented by subclass
     * Loads the View data by calling add* etc ...
     * @throws Exception
     */
    public abstract void prepareViewData() throws Exception;

    public Hashtable getBlocks()
    {
        return blocks;
    }

    public Hashtable getTags()
    {
        return tags;
    }

    public boolean isProvideRequestParameters()
    {
        return provideRequestParameters;
    }

    public Hashtable getVariables()
    {
        return variables;
    }

    public Hashtable getForms()
    {
        return forms;
    }

    public String getBuiltinTemplate()
    {
        return bTemplate;
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

    /**
     * Use this method to get/create a form object
     * Will provide either :
     * - a new form object if this form is newly used
     * - the existing form if used earlier in this request
     * (ie: validation failed)
     * This allows you to get the form and put it in the View(using addForm), or to populate/validate it in your controller.
     * @param formClass
     * @return
     */
    public JOTForm getForm(Class formClass)
    {
        return master.getForm(formClass);
    }

    protected JOTDataHolder getDataHolder()
    {
        return JOTDataHolderHelper.getDataHolder(request.getSession());
    }
    
    public JOTView getFullView()
    {
        return this;
    }
}
