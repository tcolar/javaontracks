/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jot.logger.JOTLogger;
import net.jot.web.flowparams.JOTParamBase;

/**
 * This parses flow.conf into a JOTFlowConfig objects
 * It makes extensive use of Regular Expression to do this.
 * @author thibautc
 */
public class JOTFlowConfigParser 
{
	private static final Pattern P_GLOBAL_SET_ERROR_REQUEST=Pattern.compile("^SetErrorRequest\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_GLOBAL_SET_FLOW_EXTENSION=Pattern.compile("^SetFlowExtension\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_GLOBAL_SET_TEMPLATE_ROOT=Pattern.compile("^SetTemplateRoot\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_GLOBAL_SET_404_REQUEST=Pattern.compile("^Set404Request\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_GLOBAL_SET_FORBIDDEN_REQUEST=Pattern.compile("^SetForbiddenRequest\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	
	private static final Pattern P_DIRECTIVE_REQUEST=Pattern.compile("^Request\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_DIRECTIVE_CONTROLLER=Pattern.compile("^Controller\\s+(\\S+)\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_DIRECTIVE_CONTROLLERBUNDLE=Pattern.compile("^ControllerBundle\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	
	private static final Pattern P_PARAM_CALL=Pattern.compile("^Call\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PARAM_RENDERPAGE=Pattern.compile("^RenderPage\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PARAM_RENDERPAGE_2ARGS=Pattern.compile("^RenderPage\\s+(\\S+)\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PARAM_RENDER_STATIC_PAGE=Pattern.compile("^RenderStaticPage\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	//private static final Pattern P_PARAM_SET_MARKER=Pattern.compile("^SetMarker\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	//private static final Pattern P_PARAM_KEEP_MARKER=Pattern.compile("^KeepMarker\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	//private static final Pattern P_PARAM_RETURN_TO_MARKER=Pattern.compile("^ReturnToMarker\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PARAM_REDIRECT_TO=Pattern.compile("^RedirectTo\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PARAM_CONTINUE_TO=Pattern.compile("^ContinueTo\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_PARAM_PROCESS_FORM=Pattern.compile("^ProcessForm\\s+(\\S+)\\s+(\\S+)$",Pattern.CASE_INSENSITIVE);

	//Ifs
	private static final Pattern P_IF_RESULT=Pattern.compile("^IfResult\\s+(\\S+)(.*)$",Pattern.CASE_INSENSITIVE);
	private static final Pattern P_IF_NOT_RESULT=Pattern.compile("^IfNotResult\\s+(\\S+)(.*)$",Pattern.CASE_INSENSITIVE);

	
	/**
         * Parses floe.conf into a JOTFlowConfig object
         * @param flowFile
         * @return
         */
	public static JOTFlowConfig parseFlowFile(File flowFile) 
	{
		JOTFlowConfig config=new JOTFlowConfig();
		
		boolean result=false;
		JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL,JOTFlowManager.class, "Starting parse of flow File: "+flowFile.getAbsolutePath());
		try
		{
			BufferedReader reader=new BufferedReader(new FileReader(flowFile));
			String line;
			// describe which directive we are in
			JOTFlowDirective directive=null;
			int lineNb=0;
			while((line=reader.readLine())!=null)
			{
				lineNb++;
				String ifResult=null;
				boolean ifResultBoolean=true;
				String ifResultDebug="";
				
				JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL,JOTFlowManager.class, "Parsing line "+lineNb+" : "+line);
				if(line.trim().length()==0)
				{
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL,JOTFlowManager.class, "Line is empty "+line);					
					continue;
				}
				if(line.trim().startsWith("#") || line.trim().startsWith("//"))
				{
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL,JOTFlowManager.class, "Line is a comment, ignored");
					continue;
				}
				
				Matcher m=P_GLOBAL_SET_ERROR_REQUEST.matcher(line.trim());
				if(m.matches())
				{
					// this takes us out of any directive, since this is top-level
					if(directive!=null)
						config.addDirective(directive);
					directive=null;
					String request=m.group(1);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a DefaultErrorRequest: "+request);
					config.setErrorRequest(request);
					continue;
				}
				m=P_GLOBAL_SET_404_REQUEST.matcher(line.trim());
				if(m.matches())
				{
					// this takes us out of any directive, since this is top-level
					if(directive!=null)
						config.addDirective(directive);
					directive=null;
					String request=m.group(1);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a Default404Requeste: "+request);
					config.setNotFoundRequest(request);
					continue;
				}
				m=P_GLOBAL_SET_FORBIDDEN_REQUEST.matcher(line.trim());
				if(m.matches())
				{
					// this takes us out of any directive, since this is top-level
					if(directive!=null)
						config.addDirective(directive);
					directive=null;
					String request=m.group(1);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a DefaultForbiddenRequest: "+request);
					config.setForbiddenRequest(request);
					continue;
				}
				m=P_GLOBAL_SET_TEMPLATE_ROOT.matcher(line.trim());
				if(m.matches())
				{
					// this takes us out of any directive, since this is top-level
					if(directive!=null)
						config.addDirective(directive);
					directive=null;
					String root=m.group(1);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Template Root: "+root);
					config.setTemplateRoot(root);
					continue;
				}
				m=P_GLOBAL_SET_FLOW_EXTENSION.matcher(line.trim());
				if(m.matches())
				{
					// this takes us out of any directive, since this is top-level
					if(directive!=null)
						config.addDirective(directive);
					directive=null;
					String extension=m.group(1);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Flow extension: "+extension);
					config.setFlowExtension(extension);
					continue;
				}

				m=P_DIRECTIVE_REQUEST.matcher(line.trim());
				if(m.matches())
				{
					// new directive
					if(directive!=null)
						config.addDirective(directive);
					String[] args={m.group(1)};
					directive=new JOTFlowDirective(lineNb, JOTFlowDirective.DIRECTIVE_REQUEST,args);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a Request directive named: "+args[0]);
					continue;
				}
				m=P_DIRECTIVE_CONTROLLER.matcher(line.trim());
				if(m.matches())
				{
					// new directive
					if(directive!=null)
						config.addDirective(directive);
					String[] args={m.group(1),m.group(2)};
					directive=new JOTFlowDirective(lineNb, JOTFlowDirective.DIRECTIVE_CONTROLLER,args);		
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found an Action directive named: "+args[0]);
					continue;
				}
				m=P_DIRECTIVE_CONTROLLERBUNDLE.matcher(line.trim());
				if(m.matches())
				{
					// new directive
					if(directive!=null)
						config.addDirective(directive);
					String[] args={m.group(1)};
					directive=new JOTFlowDirective(lineNb,JOTFlowDirective.DIRECTIVE_CONTROLLER_BUNDLE,args);	
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a ActionBundle directive named: "+args[0]);
					continue;
				}

				m=P_IF_RESULT.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null)
					{
						throw new Exception("'IfResults' cannot be here");
					}
					ifResult=m.group(1);
					ifResultBoolean=true;
					ifResultDebug="(IfResult "+m.group(1)+")";
					line=m.group(2);
				}
				m=P_IF_NOT_RESULT.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null)
					{
						throw new Exception("'IfNotResults' cannot be here");
					}
					ifResult=m.group(1);
					ifResultBoolean=false;
					ifResultDebug="(IfNotResult "+m.group(1)+")";
					line=m.group(2);
				}
				/*m=P_PARAM_KEEP_MARKER.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || ifResult!=null || directive.getType()!=JOTFlowDirective.DIRECTIVE_REQUEST)
					{
						throw new Exception("'KEEP_MARKER' can not be here. Line:"+lineNb);
					}
					directive.keepMarker(m.group(1));
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a KEEP_MARKER "+m.group(1)+" "+ifResultDebug);
					continue;
				}*/				
				m=P_PARAM_PROCESS_FORM.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || directive.getType()!=JOTFlowDirective.DIRECTIVE_REQUEST)
					{
						throw new Exception("'PROCESS_FORM' can not be here. Line:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_PROCESS_FORM,m.group(1),m.group(2));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					continue;
				}										
				/*m=P_PARAM_SET_MARKER.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || directive.getType()!=JOTFlowDirective.DIRECTIVE_REQUEST)
					{
						throw new Exception("'SET_MARKER' can not be here. Line:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_SET_MARKER,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found SET_MARKER: "+m.group(1)+" "+ifResultDebug);
					continue;
				}*/
				m=P_PARAM_CALL.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || directive.getType()==JOTFlowDirective.DIRECTIVE_CONTROLLER)
					{
						throw new Exception("'Call' can not be here. Line:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb, JOTParamBase.FLOW_PARAM_CALL,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a Call param: "+m.group(1)+" "+ifResultDebug);
					continue;
				}
				/*m=P_PARAM_RETURN_TO_MARKER.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null)
					{
						throw new Exception("'ReturnToMarker' can not be here. Line:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.fLOW_PARAM_RETURN_TO_MARKER,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a ReturnToMarker param: "+m.group(1)+" "+ifResultDebug);
					continue;
				}*/
				m=P_PARAM_REDIRECT_TO.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null)
					{
						throw new Exception("'RedirectTo' can not be here. LINE:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_REDIRECT_TO,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a RedirectTo param: "+m.group(1)+" "+ifResultDebug);
					continue;
				}
				m=P_PARAM_CONTINUE_TO.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null)
					{
						throw new Exception("'ContinueTo' can not be here. LINE:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_CONTINUE_TO,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a ContinueTo param: "+m.group(1)+" "+ifResultDebug);
					continue;
				}
				m=P_PARAM_RENDERPAGE.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || directive.getType()!=JOTFlowDirective.DIRECTIVE_REQUEST)
					{
						throw new Exception("'RenderPage' can not be here. Line:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_RENDER_PAGE,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a RenderPage param: "+m.group(1)+" "+ifResultDebug);
					continue;
				}
				m=P_PARAM_RENDERPAGE_2ARGS.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || directive.getType()!=JOTFlowDirective.DIRECTIVE_REQUEST)
					{
						throw new Exception("'RenderPage' can not be here. Line: "+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_RENDER_PAGE,m.group(1),m.group(2));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a RenderPage param: "+m.group(1)+" "+m.group(2)+" "+ifResultDebug);
					continue;
				}
				m=P_PARAM_RENDER_STATIC_PAGE.matcher(line.trim());
				if(m.matches())
				{
					if(directive==null || directive.getType()!=JOTFlowDirective.DIRECTIVE_REQUEST)
					{
						throw new Exception("'RenderStaticPage' can not be here. Line:"+lineNb);
					}
					JOTParamBase param=new JOTParamBase(lineNb,JOTParamBase.FLOW_PARAM_RENDER_STATIC_PAGE,m.group(1));
					param.setIfResult(ifResult, ifResultBoolean);
					directive.add(param);
					JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL,JOTFlowManager.class, "Found a RenderStaticPage param: "+m.group(1)+" "+ifResultDebug);
					continue;
				}
				JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL,JOTFlowManager.class, "Failed to parse: '"+line.trim()+"'");
				throw new Exception("Failed to understand flow.conf at line "+lineNb);
			}
			// save the last "opened" directive.
			if(directive!=null)
				config.addDirective(directive);

			reader.close();
			config.setValid(true);
		}
		catch(Exception e)
		{
			JOTLogger.logException(JOTLogger.CAT_FLOW,JOTLogger.ERROR_LEVEL, JOTFlowManager.class, "Flow file parsing error ", e);			
                        config.setError(e.getMessage());
		}
		JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL,JOTFlowManager.class, "Completed parse of flow File");
		return config;
	}
	
	/**
	 * Runs a validation of the conf file using the actual parser.
	 * This allows to test your conf file before runtime in the app server.
	 * 
	 * You could call this from ant to verify your file automatically.
	 * 
	 * @param confFileFolder : the folder where flow.conf is located.
	 * @param verbose : Wether or not to display full debug infos.
	 * @return true if parsed ok, false otherwise
	 */
	public static boolean validateFlowConfig(String confFileFolder, boolean verbose)
	{
		String[] quietLevels={"3","4","5"};
		String[] verboseLevels={"0","1","2","3","4","5"};
		if(verbose)
			JOTLogger.setLevels(verboseLevels);
		else
			JOTLogger.setLevels(quietLevels);
			
		JOTFlowConfig config=parseFlowFile(new File(confFileFolder,JOTFlowManager.confFileName));
                
                config.runValidation();
		
		return config.isValid();
	}
	
	/**
         * Calls validateFlowConfig(arg[0],true) to validate your flow.conf file
         * @param args
         */
	public static void main(String[] args)
	{
          if(args.length==0)
            System.err.println("Pass the path to flow.conf as an argument.");
          else
            validateFlowConfig(args[0], true);
	}
}
