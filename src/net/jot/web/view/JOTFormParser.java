/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.view;

import net.jot.utils.JOTPair;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jot.logger.JOTLogger;
import net.jot.web.forms.JOTForm;
import net.jot.web.forms.JOTFormConst;
import net.jot.web.forms.JOTFormElement;

/**
 * Whwn a form is found withina view, this parser will process it and set/adjust the values of fields etc... 
 * @author thibautc
 */
public class JOTFormParser extends JOTViewParser
{
        /**
         * Process the form
         * @param template
         * @param view
         * @param templateRoot
         * @return
         * @throws java.lang.Exception
         */
	public static String doForms(String template, JOTViewParserData view, String templateRoot) throws Exception
	{
	
		Hashtable forms=view.getForms();

		JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTViewParser.class, "Forms: "+forms);											
		
		Matcher m=null;
		while((m=FORM_PATTERN.matcher(template)).find())
		{
			String jotId=m.group(2);
			jotId=jotId.trim();
			String openingTag=m.group(1).trim();
			
			JOTForm form=(JOTForm)forms.get(jotId);
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTViewParser.class, "Found form:"+jotId);

			String closeTagString="</form>";
			Pattern closeTag=Pattern.compile(closeTagString,PATTERN_FLAGS);
			JOTPair pair=findMatchingClosingTag(m.end(), template, OPEN_TAG_PATTERN, closeTag);
            int index=pair.getX();
			
			if(index==-1)
			{
				JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.ERROR_LEVEL, JOTViewParser.class, "View Parsing error, Could not find closing tag for:"+openingTag+" ("+jotId+")");									
				throw new Exception("View Parsing error, Could not find closing tag for:"+openingTag+" ("+jotId+")");
			}
			
			String tagContent=template.substring(m.end(),index);

			if(form!=null)
			{
				if(! form.isVisible())
				{
					JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTViewParser.class, "Removing  invisible form:"+jotId);
					// keeping what's is Before & AFTER the tag.
                    template=template.substring(0,m.start())+template.substring(pair.getY());
				}
				else
				{
					// keep form itself (but remove jotid="" from it)
					Matcher m2=OPEN_TAG_JOTCLASS_PATTERN.matcher(openingTag);
					openingTag=m2.replaceFirst("");
					openingTag=replaceTagProperties(openingTag, form.getTagProperties(),false);
					
					String replacement=openingTag;					
					
					if(form.getContent()!=null)
					{
						JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTViewParser.class, "Replacing form content of:"+jotId);
						// replace old content by newContent
						//replacement+=parse(view,form.getContent(),templateRoot);
						replacement+=form.getContent();
					}
					else
					{
						// keep existing tag content
						Hashtable elements=form.getAll();
						tagContent=doElements(elements, tagContent, view, templateRoot);
						//replacement+=parse(view,tagContent,templateRoot);
						replacement+=tagContent;
					}
					// keep tag closing and rest of template
					replacement+=closeTagString;					
                    template=template.substring(0,m.start())+replacement+template.substring(pair.getY());
				}
			}
			else
			{
				// keep unchanged except remove jotid=""
				Matcher m2=OPEN_TAG_JOTCLASS_PATTERN.matcher(openingTag);
				openingTag=m2.replaceFirst("");
                template=template.substring(0,m.start())+openingTag+template.substring(m.end());
				JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTViewParser.class, "Form defined in html but not found in the View: "+jotId);
			}
			
			
		}//end while
		
		return template;
	}

        /**
         * Process a form piece (field)
         * @param elements
         * @param content
         * @param view
         * @param templateRoot
         * @return
         * @throws java.lang.Exception
         */
	public static String doElements(Hashtable elements, String content, JOTViewParserData view, String templateRoot) throws Exception
	{
		Enumeration keys=elements.keys();
		while(keys.hasMoreElements())
		{
			String key=(String)keys.nextElement();
			JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTFormParser.class, "Processing form element:"+key);			
			// parse for this key (html element)
			JOTFormElement element=(JOTFormElement)elements.get(key);
			int type=element.getType();
			String pattern=null;
			String closePattern=null;
			if(type==JOTFormConst.INPUT || type>=20)
			{
				pattern="<input\\s+name=\""+key+"\"[^>]*>";
				if(type==JOTFormConst.INPUT_RADIO)
					pattern="<input\\s+dataId=\""+key+"\"[^>]*>";					
			}
			else if(type==JOTFormConst.LABEL)
			{
				pattern="(<label\\s+name=\""+key+"\"[^>]*>)";
				closePattern="</label>";
			}
			else if(type==JOTFormConst.OBJECT)
			{
				pattern="(<object\\s+name=\""+key+"\"[^>]*>)";
				closePattern="</object>";
			}
			else if(type==JOTFormConst.TEXTAREA)
			{
				pattern="(<textarea\\s+name=\""+key+"\"[^>]*>)";
				closePattern="</textarea>";
			}
			else if(type==JOTFormConst.BUTTON)
			{
				pattern="(<button\\s+name=\""+key+"\"[^>]*>)";
				closePattern="</button>";
			}
			else if(type==JOTFormConst.OBJECT)
			{
				pattern="(<object\\s+name=\""+key+"\"[^>]*>)";
				closePattern="</object>";
			}
			else if(type==JOTFormConst.SELECT)
			{
				pattern="(<select\\s+name=\""+key+"\"[^>]*>)";
				closePattern="</select>";
			}
			else if(type==JOTFormConst.OBJECT_PARAM)
			{
				pattern="<param\\s+name=\""+key+"\"[^>]*>";
			}
			else if(type==JOTFormConst.SELECT_OPTGRP)
			{
				pattern="(<optgroup\\s+dataId=\""+key+"\"[^>]*>)";
				closePattern="</optgroup>";
			}
			else if(type==JOTFormConst.SELECT_OPTION)
			{
				pattern="(<option\\s+dataId=\""+key+"\"[^>]*>)";
				closePattern="</option>";
			}

			content=handleTag(view, templateRoot, pattern, closePattern, content, element);
		}
		return content;
	}

        /**
         * Handles field tags: custom HTML properties / flags susch as:
         * - class="myclass"
         * - DISABLED
         * etc...
         * @param view
         * @param templateRoot
         * @param openPattern
         * @param closePattern
         * @param content
         * @param element
         * @return
         * @throws java.lang.Exception
         */
	protected static String handleTag(JOTViewParserData view, String templateRoot, String openPattern, String closePattern, String content, JOTFormElement element) throws Exception
	{
		if(openPattern!=null)
		{
			Pattern p=Pattern.compile(openPattern,JOTViewParser.PATTERN_FLAGS);
			Matcher m;
			String tag;
			String tagInside="";
			if((m=p.matcher(content)).find())
			{
				int closeIndex=m.end();
				if(closePattern==null)
				{
					tag=m.group(0);
					JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTFormParser.class, "Tag : "+tag);			
				}
				else
				{
					tag=m.group(1);
					Pattern p2=Pattern.compile(closePattern, JOTViewParser.PATTERN_FLAGS);
					JOTPair pt=findMatchingClosingTag(m.end(), content, null, p2);
                    int index=pt.getX();
                    if(index==-1)
                        throw new Exception("Failed findind matching tag for: "+m.group());
					tagInside=content.substring(m.end(),index);
					closeIndex=pt.getY();
 				}
				String newTag="";
				if(!element.isVisible())
				{
					JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTFormParser.class, "Found INVISIBLE element in html code, removing it : "+tag);			
				}
				else
				{
					newTag=JOTViewParser.replaceTagProperties(tag, element.getTagProperties(), false);					
					newTag=JOTViewParser.replaceFlags(newTag,element.getFlags(),false);					
					if(closePattern!=null)
					{
						String tagContent="";
						Hashtable subelements=element.getSubElements();
						if(element.getContent()==null)
						{
							if(subelements!=null)
							{
								tagContent=doElements(subelements, tagInside, view, templateRoot);
							}
							if(view!=null)
							{
								tagContent=JOTViewParser.parse(view,tagContent,templateRoot);
							}
						}
						else
						{
							tagContent=element.getContent();
							if(subelements!=null)
							{
								tagContent=doElements(subelements, tagContent, view, templateRoot);
							}
						}
						newTag+=tagContent+closePattern;
					}
				}

				content=content.substring(0,m.start())+newTag+content.substring(closeIndex);
									
			}
			else
			{
				JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTFormParser.class, "Element not found in html code, skipped : "+openPattern);			
			}
		}
		return content;
	}




}
