/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import net.jot.web.ctrl.JOTController;
import net.jot.web.flowparams.JOTParamBase;
import net.jot.web.forms.JOTForm;
import net.jot.web.view.JOTView;

/**
 * Object representation of the flow config file: flow.conf
 * Also provide validation methods to cross-check the flow.conf.
 * @author thibautc
 */
public class JOTFlowConfig 
{

	private String errorRequest=null;
	private String notFoundRequest=null;
	private String forbiddenRequest=null;
        /**
         * Usually only one template root, but can be multiples to keep searchign through several until object is found.
         */
	private String[] templateRoots=null;
	private String flowExtension=".do";
	private Hashtable requests=new Hashtable();
	private Hashtable actions=new Hashtable();
	private Hashtable bundles=new Hashtable();
	private Hashtable forms=new Hashtable();
        
        private String error="Parsing error.";

	private boolean valid=false;
	private String confPath;
	
        /**
         * Returns wether the parsing of flow.conf fails
         * Note: this does not run the extra validations in runValidation(), so usually would be called after runValidation()
         * @return
         */
	public boolean isValid() 
	{
		return valid;
	}

  public String getError()
  {
    return error;
  }

  public void setError(String error)
  {
    this.error = error;
  }


	public void setValid(boolean valid) 
	{
		this.valid = valid;
	}


	public String getNotFoundRequest()
	{
		return notFoundRequest;
	}


	public void setNotFoundRequest(String request)
	{
		notFoundRequest=request;
	}

        /**
         * Adds a directive found in flow.conf
         * @param directive
         * @throws java.lang.Exception
         */
	public void addDirective(JOTFlowDirective directive) throws Exception
	{
		if(directive.getType()==JOTFlowDirective.DIRECTIVE_REQUEST)
		{
			String name=directive.getArgs()[0];
			if(requests.get(name)!=null)
			{
				throw new Exception("Duplicated Request name '"+name+"' in flow.conf");
			}
			requests.put(name,directive);
		}
		else if(directive.getType()==JOTFlowDirective.DIRECTIVE_CONTROLLER)
		{			
			String name=directive.getArgs()[0];
			if(actions.get(name)!=null || bundles.get(name)!=null )
			{
				throw new Exception("Duplicated Action/ActionBundle name '"+name+"' in flow.conf");
			}
			
			actions.put(name,directive);			
		}
		else if(directive.getType()==JOTFlowDirective.DIRECTIVE_CONTROLLER_BUNDLE)
		{
			String name=directive.getArgs()[0];
			if(actions.get(name)!=null || bundles.get(name)!=null)
			{
				throw new Exception("Duplicated Controller name '"+name+"' in flow.conf");
			}
			bundles.put(name,directive);			
		}
		/*else if(directive.getType()==JOTFlowDirective.DIRECTIVE_FORM)
		{			
			String name=directive.getArgs()[0];
			String code=directive.getArgs()[1];
			if(forms.get(name)!=null)
			{
				throw new Exception("Duplicated Form name '"+name+"' in flow.conf");
			}
			
			forms.put(name,code);			
		}*/
		else
		{
			throw new Exception("Directive as no type ??");
		}
	}


	public void setErrorRequest(String request)
	{
		errorRequest=request;
	}


	public String getErrorRequest()
	{
		return errorRequest;
	}


	public String getFlowExtension()
	{
		return flowExtension;
	}


	public void setFlowExtension(String flowExtension)
	{
		this.flowExtension = flowExtension;
	}


	public String getForbiddenRequest()
	{
		return forbiddenRequest;
	}


	public void setForbiddenRequest(String request)
	{
		forbiddenRequest=request;
	}


	public String[] getTemplateRoots()
	{
		return templateRoots;
	}


	public void setTemplateRoots(String templateRoots[])
	{
		this.templateRoots = templateRoots;
	}
        
	public void setTemplateRoot(String templateRoot)
	{
            templateRoots=new String[1];
            templateRoots[0] = templateRoot;
	}


	public void setConfigPath(String confPath)
	{
		this.confPath=confPath;
	}
	public String getConfigPath()
	{
		return confPath;
	}


	public Hashtable getActions()
	{
		return actions;
	}


	public Hashtable getBundles()
	{
		return bundles;
	}


	public Hashtable getRequests()
	{
		return requests;
	}


	public Hashtable getForms()
	{
		return forms;
	}
        
        /**
         * Runs a validation of the loaded flow.conf, calls setValidationError if an error is found
         * Checks that view/controller/forms classes exists and are of right type etc...
         */
        public void runValidation()
        {
          // if parsing the file failed, no need to validate any further.
          if(isValid())
          {
            //default pages
            if(errorRequest!=null && ! requests.containsKey(errorRequest))
            {
              setValidationError("The default error request is not defined in flow.conf: "+errorRequest);
              return;
            }
            if(notFoundRequest!=null && ! requests.containsKey(notFoundRequest))
            {
              setValidationError("The default 404 not found request is not defined in flow.conf: "+notFoundRequest);
              return;
            }
            if(forbiddenRequest!=null && ! requests.containsKey(forbiddenRequest))
            {
              setValidationError("The default forbidden request is not defined in flow.conf: "+forbiddenRequest);
              return;
            }
            //templateRoot
            for(int i=0;i!=templateRoots.length;i++)
            {
                if(templateRoots[i]!=null && !new File(templateRoots[i]).exists())
                {
                    setValidationError("The provided template root does not exist: "+templateRoots[i]);
                    return;              
                }
            }
            //actions
            Enumeration keys=actions.keys();
            while(keys.hasMoreElements())
            {
              String key=(String)keys.nextElement();
              if(!validateDirective((JOTFlowDirective)actions.get(key)))
                return;
            }
            //requests
            keys=requests.keys();
            while(keys.hasMoreElements())
            {
              String key=(String)keys.nextElement();
              if(!validateDirective((JOTFlowDirective)requests.get(key)))
                return;
            }
            //bundles
            keys=bundles.keys();
            while(keys.hasMoreElements())
            {
              String key=(String)keys.nextElement();
              if(!validateDirective((JOTFlowDirective)bundles.get(key)))
                return;
            }
            //forms
            keys=forms.keys();
            while(keys.hasMoreElements())
            {
              String key=(String)keys.nextElement();
              if(!validateDirective((JOTFlowDirective)forms.get(key)))
                return;
            }
          }
        }
 
        /**
         * Validates a particular directive
         * @param dir
         * @return
         */
        private boolean validateDirective(JOTFlowDirective dir)
        {
              if(dir.getType()==JOTFlowDirective.DIRECTIVE_CONTROLLER && !validateClass(dir.getArgs()[1],JOTController.class))
              {
                  setValidationError("Flow.conf("+dir.getLineNumber()+") error. Invalid action class not found: "+dir.getArgs()[1]);
                  return false;                  
              }
              
              for(int i=0;i!=dir.size();i++)
              {
                JOTParamBase param=(JOTParamBase)dir.get(i);
                
                if(param.getType()==JOTParamBase.FLOW_PARAM_CALL && (!actions.containsKey(param.getParams()[0]) && !bundles.containsKey(param.getParams()[0])))
                {
                  setValidationError("Flow.conf("+param.getLineNumber()+") error. Call action/bundle not found: "+param.getParams()[0]);
                  return false;
                }
                if(param.getType()==JOTParamBase.FLOW_PARAM_CONTINUE_TO && !requests.containsKey("/"+param.getParams()[0]))
                {
                  setValidationError("Flow.conf("+param.getLineNumber()+") error. ContinueTo not found: /"+param.getParams()[0]);
                  return false;
                }
                if(param.getType()==JOTParamBase.FLOW_PARAM_PROCESS_FORM && !validateClass(param.getParams()[0],JOTForm.class))
                {
                  setValidationError("Flow.conf("+param.getLineNumber()+") error. Invalid ProcessForm class: /"+param.getParams()[0]);
                  return false;
                }
                if(param.getType()==JOTParamBase.FLOW_PARAM_RENDER_PAGE)
                {
                  if(param.getParams().length>1)
                  {
                    if(!validateClass(param.getParams()[0], JOTView.class))
                    {
                      setValidationError("Flow.conf("+param.getLineNumber()+") error. Invalid RenderPage class: "+param.getParams()[0]);
                      return false;                     
                    }
                  }
                  //check that the page exists ?? -> no.
                }
                  
              }
              return true;
        }
        
        /**
         * Sets a validation error message (can be retrieved by getError()) and setValid(false);
         * @param error
         */
        private void setValidationError(String error)
        {
          setError(error);
          setValid(false);
        }
        
        /**
         * Validates that a class can be loaded (forName) and that it's an instance of classType(if classType!=null)
         * @param code
         * @param classType
         * @return
         */
        private boolean validateClass(String code, Class classType)
        {
          boolean result=false;
          if(code!=null)
          {
          try
          {
            Class c=Class.forName(code);
            if(c!=null)
            {
              return classType==null || (classType.isInstance(c.newInstance()));
            }
          }
          catch(Exception e)
          {
          }
          }
          return result;
        }
}
