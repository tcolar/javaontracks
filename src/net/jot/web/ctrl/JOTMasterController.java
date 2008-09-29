/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.ctrl;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jot.logger.JOTLogger;
import net.jot.utils.JOTConstants;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTDataHolder;
import net.jot.web.JOTDataHolderHelper;
import net.jot.web.JOTFlowClassCache;
import net.jot.web.JOTFlowConfig;
import net.jot.web.JOTFlowDirective;
import net.jot.web.JOTFlowRequest;
import net.jot.web.JOTTemplateCache;
import net.jot.web.flowparams.JOTParamBase;
import net.jot.web.forms.JOTForm;
import net.jot.web.forms.JOTGeneratedForm;
import net.jot.web.views.JOTLazyView;
import net.jot.web.view.JOTView;
import net.jot.web.view.JOTViewParser;
import net.jot.web.views.JOTErrorView;
import net.jot.web.views.JOTForbiddenView;

/**
 * This is the master controller, it handles a complete "user request" comiming from the browser.
 * This might includes multiple controllers, validations, internal redirtect and view rendering.
 * @author thibautc
 *
 */
public class JOTMasterController
{

    private static final String NO_RESULT_YET = "__JOT_NO_RESULT_YET";
    private JOTFlowRequest request;
    private HttpServletResponse response;
    private FilterConfig filterConfig;
    private FilterChain filterChain;
    private JOTFlowConfig flowConfig;
    private String lastResult = NO_RESULT_YET;
    private static JOTFlowDirective ERROR_DIRECTIVE = null;
    private static JOTFlowDirective FORBIDDEN_DIRECTIVE = null;

    static
    {
        String[] params = {JOTErrorView.class.getName(), null};
        ERROR_DIRECTIVE = new JOTFlowDirective(0, JOTFlowDirective.DIRECTIVE_REQUEST, null);
        ERROR_DIRECTIVE.add(new JOTParamBase(0, JOTParamBase.FLOW_PARAM_RENDER_PAGE, params));
        String[] params2 = {JOTForbiddenView.class.getName(), null};
        FORBIDDEN_DIRECTIVE = new JOTFlowDirective(0, JOTFlowDirective.DIRECTIVE_REQUEST, null);
        FORBIDDEN_DIRECTIVE.add(new JOTParamBase(0, JOTParamBase.FLOW_PARAM_RENDER_PAGE, params2));
    }

    public final JOTFlowConfig getFlowConfig()
    {
        return flowConfig;
    }

    public final void setFlowConfig(JOTFlowConfig flowConfig) throws Exception
    {
        this.flowConfig = flowConfig;
    }

    public final FilterConfig getFilterConfig()
    {
        return filterConfig;
    }

    public final JOTFlowRequest getRequest()
    {
        return request;
    }

    public final HttpServletResponse getResponse()
    {
        return response;
    }

    public final void setRequest(ServletRequest request) throws Exception
    {
        this.request = new JOTFlowRequest((HttpServletRequest) request);
    }

    public final void setResponse(ServletResponse response) throws Exception
    {
        this.response = (HttpServletResponse) response;
    }

    public final void setFilterConfig(FilterConfig filterConfig) throws Exception
    {
        this.filterConfig = filterConfig;
    }

    public final void setFilterChain(FilterChain filterChain)
    {
        this.filterChain = filterChain;
    }

    public final FilterChain getFilterChain()
    {
        return filterChain;
    }

    /**
     * Main method executing the request
     * @throws Exception
     */
    public final void process() throws Exception
    {
        lastResult = NO_RESULT_YET;
        // show an error message if parsing the flow file failed.
        if (!flowConfig.isValid())
        {
            renderError(response, new Exception(flowConfig.getError()));
            return;
        }

        String servletName = request.getServletPath();
        servletName = servletName.replaceAll(flowConfig.getFlowExtension(), "");

        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Request: " + servletName);

        if (flowConfig.getRequests().containsKey(servletName))
        {
            JOTFlowDirective directive = (JOTFlowDirective) flowConfig.getRequests().get(servletName);
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "Matching Request: " + directive);
            // deal with markers
            handleMarkers(directive);
            // Call the user action ....
            processDirective(directive);
        } else
        {
            render404(response, "Page not found.");
            return;
        }
    }

    /**
     * Process an actual directive fromthe flow definition (ie: Call blah)
     * @param directive
     * @return
     * @throws Exception
     */
    private boolean processDirective(JOTFlowDirective directive) throws Exception
    {

        if (directive.getType() == JOTFlowDirective.DIRECTIVE_CONTROLLER)
        {
            lastResult = callController(directive.getArgs()[1]);
        }
        boolean exit = false;
        // process the params
        for (int i = 0; i != directive.size() && !exit; i++)
        {
            JOTParamBase param = (JOTParamBase) directive.get(i);
            boolean ifPassed = true;
            if (param.getIfResultValue() != null)
            {
                ifPassed = param.isIfResultCheckIfTrue() == param.getIfResultValue().equals(lastResult);
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Will evaluate param (if matched): " + lastResult + " vs " + param.getIfResultValue() + " => " + ifPassed);
            }
            if (!ifPassed)
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Skipping param (if NOT matched): " + lastResult + " vs " + param.getIfResultValue());
                continue;
            } else
            {
                switch (param.getType())
                {
                    case JOTParamBase.FLOW_PARAM_CALL:
                        //action or action bundle
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "Call: " + param.getParams()[0]);
                        if (flowConfig.getActions().containsKey(param.getParams()[0]))
                        {
                            JOTFlowDirective ctrlDirective = (JOTFlowDirective) flowConfig.getActions().get(param.getParams()[0]);
                            exit = processDirective(ctrlDirective);
                        } else if (flowConfig.getBundles().containsKey(param.getParams()[0]))
                        {
                            JOTFlowDirective ctrlDirective = (JOTFlowDirective) flowConfig.getBundles().get(param.getParams()[0]);
                            exit = processDirective(ctrlDirective);
                        } else
                        {
                            throw new Exception("Controller/ControllerBundle '" + param.getParams()[0] + "' not found in flow.conf.");
                        }
                        break;
                    case JOTParamBase.FLOW_PARAM_REDIRECT_TO:
                        String redirect = param.getParams()[0];
                        redirect = redirect + ".do";
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "redirectTo: " + redirect);
                        JOTUtilities.sendRedirect(response, redirect, false, false);
                        exit = true;
                        return true;
                    // TODO: maybe add an EXTERNAL_REDIRECT option to redirect to absolute location

                    case JOTParamBase.FLOW_PARAM_CONTINUE_TO:
                        String forward = param.getParams()[0];
                        forward += ".do";
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "continueTo: " + forward);
                        RequestDispatcher dispatch = request.getRequest().getRequestDispatcher(forward);
                        dispatch.forward(request, response);
                        exit = true;
                        return true;
                    case JOTParamBase.FLOW_PARAM_SET_MARKER:
                        String name = param.getParams()[0];
                        String to = request.getServletPath();
                        setMarker(name, to);
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "SetMarker: " + name + " -> " + to);
                        break;
                    case JOTParamBase.FLOW_PARAM_PROCESS_FORM:
                        String formClass = param.getParams()[0];
                        String errorsVariable = param.getParams()[1];
                        lastResult = callForm(formClass, errorsVariable);
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "validate Form: " + formClass + " -> " + errorsVariable);
                        break;
                    case JOTParamBase.FLOW_PARAM_RENDER_PAGE:
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "renderPage: " + param.getParams()[0]);
                        String page = param.getParams()[0];
                        JOTView view = new JOTLazyView();
                        if (param.getParams().length > 1)
                        {
                            page = param.getParams()[1];
                            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "renderPage: " + param.getParams()[0] + " with " + param.getParams()[1]);
                            // calling the view controller
                            view = callView(param.getParams()[0]);
                        } else
                        {
                            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "renderPage: " + param.getParams()[0]);
                        }
                        if (view == null)
                        {
                            renderForbidden(response);
                        }
                        if (!response.isCommitted())
                        {
                            // might be commited by the controller in special cases (ie: serving images)
                            renderPage(view, page);
                        }
                        exit = true;
                        return true;
                    case JOTParamBase.FLOW_PARAM_RENDER_STATIC_PAGE:
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "renderStaticPage: " + param.getParams()[0]);
                        renderStaticPage(param.getParams()[0]);
                        exit = true;
                        return true;
                    case JOTParamBase.fLOW_PARAM_RETURN_TO_MARKER:
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "returnToMarker: " + param.getParams()[0]);
                        String marker = findMarker(param.getParams()[0]);
                        if (marker != null)
                        {
                            //TODO: - should we save the request parameters too, on a redirect to marker ??
                            marker = request.getContextPath() + marker;
                            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "returning To Marker: " + marker);
                            response.sendRedirect(marker);
                            exit = true;
                            return true;
                        }
                        break;
                }
            }
        }
        return exit;
    }

    /**
     * Calls a form and validate it.
     * @param formClass
     * @param errorsVariable
     * @return
     * @throws java.lang.Exception
     */
    private String callForm(String formClass, String errorsVariable) throws Exception
    {
        //TODO: check that flow.conf tells us what to do for all three results possible ?

        Class theClass = JOTFlowClassCache.getClass(formClass);
        JOTForm form = getForm(theClass);

        String result = JOTController.RESULT_SUCCESS;
        try
        {
            if (validatePermissions(form) == false)
            {
                result = JOTController.RESULT_FORBIDDEN;
            }
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Form processing forbidden! ", e);
            result = JOTController.RESULT_FORBIDDEN;
        }
        if (result.equals(JOTController.RESULT_SUCCESS))
        {
            // validate
            try
            {
                Hashtable errors = validateForm(form);
                if (errors.size() > 0)
                {
                    form.setErrors(errors);
                    result = JOTController.RESULT_VALIDATION_FAILURE;
                } else
                {
                    form.setHasValidated(true);
                    request.setAttribute(JOTGeneratedForm.HAD_SUCCESS, Boolean.TRUE);
                }
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Form validation failed! ", e);
                Hashtable err = new Hashtable();
                err.put("EXCEPTION", "Form validation failed");
                form.setErrors(err);
                result = JOTController.RESULT_VALIDATION_FAILURE;
            }
            // save if validation passed
            try
            {
                if (form.hasValidated())
                {
                    saveForm(form);
                    result = form.getResult();
                }
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Saving form failed! ", e);
                result = JOTController.RESULT_FAILURE;
                throw (e);
            }
        }
        //    	save in request
        request.setAttribute(JOTForm.REQUEST_ID, form);
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "End Processing formValidator: " + formClass + " result:" + result);
        return result;
    }

    /**
     * Render a static page from a template
     * The template won't be parsed, so basically just returns a pure HTML template "as-is"
     * This is faster than parsing obviously, so should be used for "static" pages.
     * @param template
     * @throws Exception
     */
    private void renderStaticPage(String template) throws Exception
    {
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Rendering static page: " + template);
        String[] templateRoots = flowConfig.getTemplateRoots();

        if (request.getSession().getAttribute(JOTConstants.CUSTOM_TEMPLATE_ROOTS) != null)
        {
            templateRoots = (String[]) request.getSession().getAttribute(JOTConstants.CUSTOM_TEMPLATE_ROOTS);
        }
        
        boolean found=false;
        for(int i=0; !found && i!=templateRoots.length;i++)
        {
            File templateFile = new File(templateRoots[i], template);
            String templateString = "";
            templateString = JOTTemplateCache.getTemplate(templateFile.getAbsolutePath());
            found=true;
            sendResponse(response, templateString);
        }
        if(!found)
        {
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Static page missing: " + template);
        }
    }

    /**
     * Render a page using a template (process it first with a View)
     * @param view
     * @param template
     * @throws Exception
     */
    private void renderPage(JOTView view, String template) throws Exception
    {
        String[] templateRoots = flowConfig.getTemplateRoots();

        if (request.getSession().getAttribute(JOTConstants.CUSTOM_TEMPLATE_ROOTS) != null)
        {
            templateRoots = (String[]) request.getSession().getAttribute(JOTConstants.CUSTOM_TEMPLATE_ROOTS);
        }

        String templateFile = "";
        try
        {
            if (view.getBuiltinTemplate() != null)
            {
                templateFile = "-BULITIN-";
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "Template: BUILT-IN");
                template = JOTViewParser.parse(view, view.getBuiltinTemplate(), null);
            } else
            {
                boolean found=false;
                for (int i = 0; !found && i != templateRoots.length; i++)
                {
                    templateFile = templateRoots[i] + template;
                    try
                    {
                        template = JOTViewParser.parseTemplate(view, templateRoots[i], template);
                        found=true;
                    }
                    catch(Exception e2)
                    {
                        JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "Faile parsing template : " + JOTUtilities.endWithSlash(templateRoots[i]) + template, e2);
                    }
                }
                if(!found)
                {
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Template not found in any of the template Root(s) : " + template);
                }
            }
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Template error in " + templateFile, e);
            throw (e);
        }
        sendResponse(response, template);
    }

    private void setMarker(String name, String to)
    {
        JOTDataHolderHelper.setMarker(JOTDataHolderHelper.getDataHolder(request.getSession()), name, to);
    }

    private String findMarker(String marker)
    {
        return JOTDataHolderHelper.findMarker(JOTDataHolderHelper.getDataHolder(request.getSession()), marker);
    }

    /**
     * "Execute" a view (view type controller)
     * @param className
     * @return
     * @throws Exception
     */
    private JOTView callView(String className) throws Exception
    {
        Class viewClass = JOTFlowClassCache.getClass(className);
        JOTView view = (JOTView) viewClass.newInstance();
        view.init(this);
        // calling the actual controller action code.
        if (!view.validatePermissions())
        {
            return null;
        }
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Start Processing View: " + className);
        view.process();
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "End Processing View: " + className);
        return view;
    }

    /**
     * Executes a controller (non view)
     * @param className
     * @return
     * @throws Exception
     */
    private String callController(String className) throws Exception
    {
        Class controllerClass = JOTFlowClassCache.getClass(className);
        JOTController controller = (JOTController) controllerClass.newInstance();
        controller.init(this);

        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "Start Processing controller: " + className);
        String result = JOTController.RESULT_FAILURE;

        if (!controller.validatePermissions())
        {
            //throw new Exception("User is not allowed to run this controller !");
            return JOTController.RESULT_FORBIDDEN;
        }
        // calling the actual controller action code.
        result = controller.process();
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, this, "End Processing controller: " + className + " result:" + result);
        return result;
    }

    /**
     * Set/Unset the markers as defined in flow file.
     * @param directive
     */
    private void handleMarkers(JOTFlowDirective directive)
    {
        // find markers to keeep
        Vector markersToKeep = directive.getMarkersToKeep();
        // clear markers
        JOTDataHolder jotData = JOTDataHolderHelper.getDataHolder(request.getSession());
        JOTDataHolderHelper.updateMarkers(jotData, markersToKeep);
    }

    /**
     * Render an error page (Unexpected Exception)
     * @param response
     * @param t
     */
    public void renderError(HttpServletResponse response, Throwable t)
    {
        try
        {
            JOTFlowDirective directive = ERROR_DIRECTIVE;
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.CRITICAL_LEVEL, this, "JOT Error: ", t);
            request.setAttribute(JOTErrorView.EXCEPTION_ATTRIB, t);

            if (flowConfig.getErrorRequest() != null)
            {
                directive = (JOTFlowDirective) flowConfig.getRequests().get(flowConfig.getErrorRequest());
            }
            processDirective(directive);
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.CRITICAL_LEVEL, this, "Error rendering error ! ", e);
            renderPlainError(response, t);
        }
    }

    /**
     * Sends the processed HTML page to the browser
     * @param template
     * @throws Exception
     */
    public void sendResponse(HttpServletResponse response, String template) throws Exception
    {
        response.setContentType("text/html");
        response.setContentLength(template.getBytes().length);
        response.getOutputStream().write(template.getBytes());
        response.flushBuffer();
    }

    /**
     * Renders a forbidden page
     * @param response
     * @param txt
     */
    public void renderForbidden(HttpServletResponse response) throws Exception
    {
        try
        {
            JOTFlowDirective directive = FORBIDDEN_DIRECTIVE;
            if (flowConfig.getForbiddenRequest() != null)
            {
                directive = (JOTFlowDirective) flowConfig.getRequests().get(flowConfig.getForbiddenRequest());
            }
            processDirective(directive);
        } catch (Exception e)
        {
            renderError(response, e);
        }
    }

    /**
     * Renders a "not found" page
     * @param response
     * @param txt
     */
    public void render404(HttpServletResponse response, String txt) throws Exception
    {
        try
        {
            if (flowConfig.getNotFoundRequest() != null)
            {
                JOTFlowDirective dir = (JOTFlowDirective) flowConfig.getRequests().get(flowConfig.getNotFoundRequest());
                processDirective(dir);
            }
        //standard = do nothing, let app server handle it.
        } catch (Exception e)
        {
            renderError(response, e);
        }
    }

    /**
     * Renders a page in plain text (no template/decoration), this is a failover if "nice" forbidden/404... pages fail.
     * @param response
     * @param text
     */
    public void renderPlain(ServletResponse response, String text)
    {
        try
        {
            response.setContentType("text/html");
            response.getWriter().println(text);
            response.flushBuffer();
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Failed to render the plain page !", e);
        }
    }

    /**
     * Renders an unexpected error, this is a failover if renderError did not succeed
     * @param response
     * @param t
     */
    public void renderPlainError(ServletResponse response, Throwable t)
    {
        try
        {
            response.setContentType("text/html");
            t.printStackTrace(response.getWriter());
            response.flushBuffer();
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Failed to render the plainerror page !", e);
            JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Original error: !", t);
        }
    }

    /**
     * Calls the form validation
     * @param form
     * @return
     * @throws java.lang.Exception
     */
    public Hashtable validateForm(JOTForm form) throws Exception
    {
        Hashtable result = new Hashtable();
        form.reparseForm(request);
        form.preValidate();
        result = form.validate(request);
        return result;
    }

    /**
     * Calls a form permission validation
     * @param form
     * @return
     * @throws java.lang.Exception
     */
    protected boolean validatePermissions(JOTForm form) throws Exception
    {
        boolean result = form.validatePermissions(request);
        return result;
    }

    /**
     * Save the processed form
     * @param form
     * @throws java.lang.Exception
     */
    private void saveForm(JOTForm form) throws Exception
    {
        form.save(request);
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
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "get Form before parse");
        JOTForm form = (JOTForm) request.getAttribute(JOTForm.REQUEST_ID);
        if (form == null || form.getClass() != formClass)
        {
            try
            {
                form = (JOTForm) formClass.newInstance();
                form.init(request);
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, this, "Error retrieving this form from the request: " + formClass.getName(), e);
                form = null;
            }
        }
        return form;
    }
    
    
}
