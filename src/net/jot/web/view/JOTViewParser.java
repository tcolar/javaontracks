/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.view;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jot.logger.JOTLogger;
import net.jot.utils.JOTReflectionUtils;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowClassCache;
import net.jot.web.JOTMainFilter;
import net.jot.web.JOTTemplateCache;

/*
Rather than parse the template each time, preload a "programatic view of it"
-text blahblah
- jot:block jotid=blah
-text value="blah blah"
-jot:var jotid=blah
-text value=blah blah ...
- jot:if eval="blah"
text value="blah"
etc ....
then  use that during rendering, should be MUCH faster
re-parse the template on the fly it timestamp changed ? (option)
 */
//the parser will choke on <div prop=">"> .. does it matter ??
import net.jot.web.widget.JOTWidgetBase;

/**
 * This is the main parser that takes a View object and a Template and spits out HTML code to the browser.
 * It makes heavy use of headache-inducing Regular expressions.
 * 
 */
public class JOTViewParser
{

    protected static final String MISSING_VALUE = "MISSING_VALUE !";
    protected static final String COUNTER_NAME = "cpt";

    // we want to ignore the case of the tag, ".*" to allow multiple lines and allow the html to be multilines.  canon=unicode insensitive
    protected static final int PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE | Pattern.CANON_EQ;
    // Main patterns
    //TODO: jot:wrap
    protected static final Pattern FORM_PATTERN = Pattern.compile("(<form\\s+jotclass=\"([^\"]+)\"[^>]*>)(.*)", PATTERN_FLAGS);
    protected static final Pattern REMOVE_PATTERN = Pattern.compile("<jot:remove>.*</jot:remove>", PATTERN_FLAGS);
    protected static final Pattern BLOCK_PATTERN = Pattern.compile("(<jot:block\\s+dataId=\"([^\"]+)\"\\s*>)(.*)", PATTERN_FLAGS);
    protected static final Pattern BLOCK_PATTERN_1LINE = Pattern.compile("<jot:block\\s+dataId=\"([^\"]+)\"\\s*/>", PATTERN_FLAGS);
    protected static final Pattern TAG_PATTERN_1LINE = Pattern.compile("<[^> ]*\\s+jotid=\"([^\"]+)\"([^/][^>])*/>", PATTERN_FLAGS);
    protected static final Pattern TAG_PATTERN = Pattern.compile("(<([^> ]*)\\s+jotid=\"([^\"]+)\"[^>]*>)(.*)", PATTERN_FLAGS);
    protected static final Pattern LOOP_PATTERN = Pattern.compile("<jot:loop\\s+over=\"([^\"]+)\"\\s+as=\"([^\"]+)\"\\s*(counter=\"([^\"]+)\")?\\s*>", PATTERN_FLAGS);
    protected static final Pattern VAR_PATTERN = Pattern.compile("<jot:var\\s+value=\"([^\"]+)\"\\s*(default=\"([^\"]+)\")?\\s*/>", PATTERN_FLAGS);
    protected static final Pattern URL_PATTERN = Pattern.compile("<jot:url\\s+path=\"([^\"]+)\"\\s*/>", PATTERN_FLAGS);
    protected static final Pattern INCLUDE_PATTERN = Pattern.compile("<jot:include\\s+file=\"([^\"]+)\"\\s*/>", PATTERN_FLAGS);
    protected static final Pattern RANGE_LOOP_PATTERN = Pattern.compile("<jot:loop\\s+from=\"([^\"]+)\"\\s+to=\"([^\"]+)\"\\s*(counter=\"([^\"]+)\")?\\s*>", PATTERN_FLAGS);
    protected static final Pattern GENERIC_LOOP_PATTERN = Pattern.compile("<jot:loop[^>]*>", PATTERN_FLAGS);
    protected static final Pattern IF_PATTERN = Pattern.compile("(<jot:if\\s+eval=\"([^\"]+)\"\\s*>)(.*)", PATTERN_FLAGS);
    protected static final Pattern WIDGET_PATTERN = Pattern.compile("<jot:widget\\s+class=\"([^\"]+)\"\\s*(args=\"([^\"]+)\")?\\s*/>", PATTERN_FLAGS);
    // match a variable pattern members
    protected static final Pattern VAR_MEMBER_PATTERN = Pattern.compile("(([a-zA-Z0-9\\(\\)_,\"-]*('[^']*')*)+)", PATTERN_FLAGS);

    // util patterns
    protected static final Pattern OPEN_BLOCK_PATTERN = Pattern.compile("<jot:block\\s+dataId=\"[^\"]+\"\\s*>", PATTERN_FLAGS);
    protected static final String CLOSE_BLOCK_STRING = "</jot:block>";
    protected static final Pattern CLOSE_BLOCK_PATTERN = Pattern.compile(CLOSE_BLOCK_STRING, PATTERN_FLAGS);
    protected static final Pattern OPEN_TAG_PATTERN = Pattern.compile("[^> ]*\\s+jotid=\"[^\"]+\"[^>]*>", PATTERN_FLAGS);
    protected static final Pattern OPEN_TAG_JOTID_PATTERN = Pattern.compile("jotid=\"[^\"]+\"", PATTERN_FLAGS);
    protected static final Pattern OPEN_TAG_JOTCLASS_PATTERN = Pattern.compile("jotclass=\"[^\"]+\"", PATTERN_FLAGS);
    protected static final String CLOSE_LOOP_STRING = "</jot:loop>";
    protected static final Pattern CLOSE_LOOP_PATTERN = Pattern.compile(CLOSE_LOOP_STRING, PATTERN_FLAGS);
    protected static final Pattern OPEN_IF_PATTERN = Pattern.compile("<jot:if\\s+eval=\"[^\"]+\"\\s*>", PATTERN_FLAGS);
    protected static final String CLOSE_IF_STRING = "</jot:if>";
    protected static final Pattern CLOSE_IF_PATTERN = Pattern.compile(CLOSE_IF_STRING, PATTERN_FLAGS);
    protected static final Pattern PARAMS_PATTERN = Pattern.compile("([^,]*)[,]?", PATTERN_FLAGS);

    /**
     * Parse the whole template file as a String.
     * @param view
     * @param template
     * @return
     * @throws Exception
     */
    public static String parse(JOTViewParserData view, String template, String templateRoot) throws Exception
    {
        // done as part of caching now, since static.
        //template=doRemoveTags(template);		
        template = doIncludes(template, view, templateRoot);
        template = doWidgets(template, view);
        template = doUrls(template, view);
        template = do1LineBlocks(template, view);
        template = do1LineTags(template, view);
        template = doBlocks(template, view, templateRoot);
        template = doTags(template, view, templateRoot);
        template = doLoops(template, view, templateRoot);
        template = doIfs(template, view, templateRoot);
        template = doRangeLoops(template, view, templateRoot);
        template = doVariables(template, view);
        template = JOTFormParser.doForms(template, view, templateRoot);
        return template;
    }

    /** process URL's*/
    static String doUrls(String template, JOTViewParserData view)
    {
        Matcher m = URL_PATTERN.matcher(template);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            //String htmlTag=m.group(0);
            String url = m.group(1);
            String newUrl = JOTMainFilter.getContextName() + url;
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "New URL:" + newUrl);
            safeAppendReplacement(m, buf, newUrl);
        }
        m.appendTail(buf);
        template = buf.toString();
        return template;
    }

    static String doIfs(String template, JOTViewParserData view, String templateRoot) throws Exception
    {
        Matcher m = null;
        while ((m = IF_PATTERN.matcher(template)).find())
        {
            StringBuffer buf = new StringBuffer();
            String var = m.group(2);
            String openingTag = m.group(1);
            String restOfTemplate = m.group(3);
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Found If " + openingTag);
            boolean negated = false;
            var = var.trim();
            if (var.startsWith("!"))
            {
                var = var.substring(1, var.length()).trim();
                negated = true;
            }

            int index = findMatchingClosingTag(0, restOfTemplate, OPEN_IF_PATTERN, CLOSE_IF_PATTERN, 1);

            if (index == -1)
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, JOTView.class, "View Parsing error, Could not find closing if tag for:" + openingTag + " (" + var + ")");
                throw new Exception("View Parsing error, Could not find closing if tag for:" + openingTag + " (" + var + ")");
            }

            Vector varHash = getVariableHash(var);
            Object obj = view.getVariables().get(varHash.get(0));
            Object val = getVariableValue(view, varHash, obj, null);

            boolean result = false;
            if (val != null)
            {
                if (val instanceof Boolean)
                {
                    result = ((Boolean) val).booleanValue();
                } else
                {
                    result = val != null;
                }
            }

            if (negated)
            {
                result = !result;
            }


            if (!result)
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing if content:" + openingTag);
                // keeping what's is AFTER the tag.
                safeAppendReplacement(m, buf, restOfTemplate.substring(index, restOfTemplate.length()));
            } else
            {
                String newTemplate = restOfTemplate.substring(0, index - CLOSE_IF_STRING.length()) + restOfTemplate.substring(index, restOfTemplate.length());
                newTemplate = parse(view, newTemplate, templateRoot);
                //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "Replacement without tag:"+newTemplate);
                safeAppendReplacement(m, buf, newTemplate);
            }
            template = buf.toString();
        //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "New template:"+template);
        }//end while

        return template;
    }

    static String doIncludes(String template, JOTViewParserData view, String templateRoot) throws Exception
    {
        Hashtable variables = view.getVariables();
        Matcher m = INCLUDE_PATTERN.matcher(template);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String htmlTag = m.group(0);
            String file = m.group(1);
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Include:" + file);
            // id the argument exists as a variable, uses that, otherwise expect a file name.
            Vector hash = getVariableHash(file);
            if (hash.size() > 0)
            {
                Object hash0 = variables.get((String) hash.get(0));
                if (hash0 != null)
                {
                    file = (String) getVariableValue(view, hash, hash0, file);
                }
            }

            File f = new File(JOTUtilities.endWithSlash(templateRoot) + file);
            
            String include = JOTTemplateCache.getTemplate(f.getAbsolutePath());
            try
            {
                include = parse(view, include, templateRoot);
            } catch (Exception e)
            {
                Exception e2 = new Exception("Error while parsing included template in: " + file, e);
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, JOTView.class, "Error parsing template !", e);
                e2.fillInStackTrace();
                throw (e2);
            }

            safeAppendReplacement(m, buf, include);

        }
        m.appendTail(buf);

        return buf.toString();
    }

    static String doRangeLoops(String template, JOTViewParserData view, String templateRoot) throws Exception
    {
        Hashtable variables = view.getVariables();
        Matcher m = null;
        while ((m = RANGE_LOOP_PATTERN.matcher(template)).find())
        {
            String newLoopContent = "";
            Object obj = null;
            Vector hash = getVariableHash(m.group(1));
            Vector hash2 = getVariableHash(m.group(2));
            if (hash.size() > 0)
            {
                obj = variables.get(hash.get(0));
            }
            Integer from = (Integer) getVariableValue(view, hash, obj, "0");
            obj = null;
            if (hash.size() > 0)
            {
                obj = variables.get(hash2.get(0));
            }
            Integer to = (Integer) getVariableValue(view, hash2, obj, "0");
            String counter = m.group(4);
            if (counter == null)
            {
                counter = COUNTER_NAME;
            }
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Loop found from:" + from + " to:" + to + " counter:" + counter);

            int loopStartIndex = m.start();

            int loopEndIndex = findMatchingClosingTag(m.end(), template, GENERIC_LOOP_PATTERN, CLOSE_LOOP_PATTERN, 1);

            if (loopEndIndex == -1)
            {
                throw new Exception("Could not find matching end of loop tag for loop" + m.group(0));
            }

            // what is inside the loop.
            String loopContent = template.substring(m.end(), loopEndIndex - CLOSE_LOOP_STRING.length());

            int start = (from).intValue();
            int stop = (to).intValue();
            int step = 1;
            if (stop < start)
            {
                step = -1;
            }

            for (int i = start; i != stop; i += step)
            {
                variables.put(counter, new Integer(i));
                // recurse to deal with builtin loops/variables
                newLoopContent += parse(view, loopContent, templateRoot);
                //remove the temp. "As" value
                variables.remove(counter);
            }

            template = template.substring(0, loopStartIndex) + newLoopContent + template.substring(loopEndIndex, template.length());
        }
        return template;
    }

    /**
     * Process loops
     * 
     * Loopable variabale must be of 1 of those types: 
     * - Collection:
     *   AbstractCollection, AbstractList, AbstractSet, ArrayList, BeanContextServicesSupport, 
     *	BeanContextSupport, HashSet, LinkedHashSet, LinkedList, TreeSet, Vector
     * - Object[]
     * - Hashtable() (keys are ignored)
     * 
     * @param template
     * @param view
     * @return
     * @throws Exception
     */
    static String doLoops(String template, JOTViewParserData view, String templateRoot) throws Exception
    {
        Hashtable variables = view.getVariables();
        Matcher m = null;
        while ((m = LOOP_PATTERN.matcher(template)).find())
        {
            String newLoopContent = "";
            String loopObjectName = m.group(1);
            String as = m.group(2);
            String counter = m.group(4);
            loopObjectName = loopObjectName.trim();
            if (counter == null)
            {
                counter = COUNTER_NAME;
            }
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Loop found :" + loopObjectName + " loop as:" + as + " counter:" + counter);

            int loopStartIndex = m.start();
            int loopEndIndex = findMatchingClosingTag(m.end(), template, GENERIC_LOOP_PATTERN, CLOSE_LOOP_PATTERN, 1);

            if (loopEndIndex == -1)
            {
                throw new Exception("Could not find matching end of loop tag for loop:" + loopObjectName);
            }

            // what is inside the loop.
            String loopContent = template.substring(m.end(), loopEndIndex - CLOSE_LOOP_STRING.length());

            Vector varHash = getVariableHash(loopObjectName);
            if (varHash.size() > 0)
            {
                Object o = variables.get(varHash.get(0));
                Object loop = getVariableValue(view, varHash, o, null);
                Object[] objects = new Object[0];

                if (loop == null)
                {
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL, JOTView.class, "Loop object is null! :" + loopObjectName);
                } else if (loop instanceof Collection)
                {
                    objects = ((Collection) loop).toArray();
                } else if (loop instanceof Object[])
                {
                    objects = (Object[]) loop;
                } else if (loop instanceof Hashtable)
                {
                    objects = ((Hashtable) loop).values().toArray();
                } else
                {
                    throw new Exception("Loop value must be of one of those types: [Collection, Object[], Hashtable], found:" + loop.getClass().getName());
                }

                for (int i = 0; i != objects.length; i++)
                {
                    // loop values
                    Object obj = objects[i];
                    //store the "As" value temp.
                    //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTView.class, "Setting loop var :"+as+" -> "+obj);
                    variables.put(as, obj);
                    // and the counter value 
                    variables.put(counter, new Integer(i));
                    // recurse to deal with builtin loops/variables
                    newLoopContent += parse(view, loopContent, templateRoot);
                    //remove the temp. "As" value
                    variables.remove(as);
                    variables.remove(counter);
                }
            }
            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTView.class, "Loop content :"+loopContent);
            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.DEBUG_LEVEL, JOTView.class, "New Loop content :"+newLoopContent);

            template = template.substring(0, loopStartIndex) + newLoopContent + template.substring(loopEndIndex, template.length());
        }
        return template;
    }

    /**
     * Same as doTags but for 1liner tags
     * @param template
     * @param view
     * @return
     */
    static String do1LineTags(String template, JOTViewParserData view)
    {
        Hashtable tags = view.getTags();
        Matcher m = TAG_PATTERN_1LINE.matcher(template);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String htmlTag = m.group(0);
            String jotId = m.group(1);
            jotId = jotId.trim();
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Tag(1liner):" + htmlTag + " with jotid found, jotid:" + jotId);
            JOTViewTag tag = (JOTViewTag) tags.get(jotId);

            if (tag == null)
            {
                // keep unchanged except remove jotid=""
                Matcher m2 = OPEN_TAG_JOTID_PATTERN.matcher(htmlTag);
                htmlTag = m2.replaceFirst("");
                safeAppendReplacement(m, buf, htmlTag);
            } else
            {
                if (!tag.isVisible())
                {
                    // removing the block
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing invisible 1liner tag:" + jotId);
                    safeAppendReplacement(m, buf, "");
                } else
                {
                    Matcher m2 = OPEN_TAG_JOTID_PATTERN.matcher(htmlTag);
                    String replacement = m2.replaceFirst("");

                    replacement = replaceTagProperties(replacement, tag.getTagProperties(), true);
                    replacement = replaceFlags(replacement, tag.getFlags(), true);

                    if (tag.getContent() != null)
                    {
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL, JOTView.class, "Cannot replace content of 1liner tags." + jotId);
                    }

                    safeAppendReplacement(m, buf, replacement);

                }
            }
        }
        m.appendTail(buf);

        return buf.toString();
    }

    /**
     * Handle the tags that are marked with a jotid property
     * ie: makes them invisible, replace their content, replace some of the tag properties etc ...
     * @param template
     * @param view
     * @return
     * @throws Exception
     */
    static String doTags(String template, JOTViewParserData view, String templateRoot) throws Exception
    {
        Hashtable tags = view.getTags();

        Matcher m = null;
        while ((m = TAG_PATTERN.matcher(template)).find())
        {
            StringBuffer buf = new StringBuffer();
            String jotId = m.group(3);
            jotId = jotId.trim();
            String openingTag = m.group(1);
            String tagName = m.group(2);
            String restOfTemplate = m.group(4);

            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Found Tag: " + openingTag);

            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "Tag Name: "+tagName);
            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "jotId: "+jotId);

            String closeTagString = "</" + tagName + ">";
            Pattern closeTag = Pattern.compile(closeTagString, PATTERN_FLAGS);
            int index = findMatchingClosingTag(0, restOfTemplate, OPEN_TAG_PATTERN, closeTag, 1);

            if (index == -1)
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, JOTView.class, "View Parsing error, Could not find closing tag for:" + openingTag + " (" + jotId + ")");
                throw new Exception("View Parsing error, Could not find closing tag for:" + openingTag + " (" + jotId + ")");
            }

            String tagContent = restOfTemplate.substring(0, index);
            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "Tag content:"+restOfTemplate.substring(0,index));

            JOTViewTag tag = (JOTViewTag) tags.get(jotId);

            if (tag != null)
            {
                if (!tag.isVisible())
                {
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing  invisible block:" + jotId);
                    // keeping what's is AFTER the tag.
                    safeAppendReplacement(m, buf, restOfTemplate.substring(index, restOfTemplate.length()));
                } else
                {
                    // keep tag itself (but remove jotid="" from it)
                    Matcher m2 = OPEN_TAG_JOTID_PATTERN.matcher(openingTag);
                    openingTag = m2.replaceFirst("");
                    openingTag = replaceTagProperties(openingTag, tag.getTagProperties(), false);

                    String replacement = openingTag;
                    replacement = replaceFlags(replacement, tag.getFlags(), false);

                    if (tag.getContent() != null)
                    {
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing block content of:" + jotId);
                        // replace old content by newContent
                        replacement += parse(view, tag.getContent(), templateRoot);
                    } else
                    {
                        // keep existing tag content
                        replacement += parse(view, tagContent, templateRoot);
                    }
                    // closing the tag
                    replacement += closeTagString;
                    // keeping what's is AFTER the tag.
                    replacement += restOfTemplate.substring(index, restOfTemplate.length());

                    safeAppendReplacement(m, buf, replacement);
                }
            } else
            {
                // keep unchanged except remove jotid=""
                Matcher m2 = OPEN_TAG_JOTID_PATTERN.matcher(openingTag);
                openingTag = m2.replaceFirst("");
                safeAppendReplacement(m, buf, openingTag + restOfTemplate);
            }


            template = buf.toString();
        //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "New template:"+template);
        }//end while

        return template;
    }

    /**
     * Replaces an html tag properties with the ones provided
     * ie: change the value of the class property in <a class="toto"> ...
     * @param openingTag
     * @param tagProperties
     * @param oneLiner
     * @return
     */
    static String replaceTagProperties(String openingTag, Hashtable tagProperties, boolean oneLiner)
    {
        Enumeration keys = tagProperties.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String value = (String) tagProperties.get(key);

            Pattern quoted = Pattern.compile(key + "\\s*=\\s*\"([^\"]*)\"", PATTERN_FLAGS);
            Pattern singlequoted = Pattern.compile(key + "\\s*=\\s*'([^\"]*)'", PATTERN_FLAGS);
            Pattern unquoted = Pattern.compile(key + "\\s*=\\s*(\\S+)", PATTERN_FLAGS);

            Matcher m = quoted.matcher(openingTag);
            if (m.find())
            {
                // leave in quotes
                openingTag = m.replaceFirst(key + "=\"" + value + "\"");
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing tag property:" + key);
            } else
            {
                m = unquoted.matcher(openingTag);
                if (m.find())
                {
                    // put in quotes for safety
                    openingTag = m.replaceFirst(key + "=\"" + value + "\"");
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing tag property:" + key);
                } else
                {
                    m = singlequoted.matcher(openingTag);
                    if (m.find())
                    {
                        // leave in single quotes
                        openingTag = m.replaceFirst(key + "=\"" + value + "\"");
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing tag property:" + key);
                    } else
                    {
                        int offset = 1;
                        String closingTag = ">";
                        if (oneLiner)
                        {
                            offset = 2;
                            closingTag = "/>";
                        }

                        openingTag = openingTag.substring(0, openingTag.length() - offset) + " " + key + "=\"" + value + "\"" + closingTag;
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Adding tag property:" + key);
                    }
                }
            }
        }
        return openingTag;
    }

    /**
     * Deal with 1liner blocks
     * @param template
     * @param view
     * @return
     * @throws Exception
     */
    static String do1LineBlocks(String template, JOTViewParserData view) throws Exception
    {
        StringBuffer buf = new StringBuffer();
        Hashtable blocks = view.getBlocks();

        Matcher m = BLOCK_PATTERN_1LINE.matcher(template);
        while (m.find())
        {
            String jotId = m.group(1);
            jotId = jotId.trim();
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Found block (1 liner), jotid:" + jotId);
            JOTViewBlock block = (JOTViewBlock) blocks.get(jotId);

            if (block == null || !block.isVisible())
            {
                // removing the block
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing invisible block:" + jotId);
                safeAppendReplacement(m, buf, "");
            } else
            {
                if (block.getContent() != null)
                {
                    // replacing by content
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing block content of:" + jotId);
                    safeAppendReplacement(m, buf, block.getContent());
                }
            }
        }
        m.appendTail(buf);
        return buf.toString();
    }

    /**
     * Deal with jot:block tags
     * ie: make sthen visible or not, replace their content etc ...
     * @param template
     * @param view
     * @return
     * @throws Exception
     */
    static String doBlocks(String template, JOTViewParserData view, String templateRoot) throws Exception
    {
        Hashtable blocks = view.getBlocks();

        Matcher m = null;
        while ((m = BLOCK_PATTERN.matcher(template)).find())
        {
            StringBuffer buf = new StringBuffer();
            String jotId = m.group(2);
            jotId = jotId.trim();
            String openingTag = m.group(1);
            String restOfTemplate = m.group(3);
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Found block " + openingTag);

            int index = findMatchingClosingTag(0, restOfTemplate, OPEN_BLOCK_PATTERN, CLOSE_BLOCK_PATTERN, 1);

            if (index == -1)
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, JOTView.class, "View Parsing error, Could not find closing tag for:" + openingTag + " (" + jotId + ")");
                throw new Exception("View Parsing error, Could not find closing block tag for:" + openingTag + " (" + jotId + ")");
            }

            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "Block content:"+restOfTemplate.substring(0,index));

            JOTViewBlock block = (JOTViewBlock) blocks.get(jotId);

            if (block == null)
            {
                // keeping all but the opening/closing tags
                String newTemplate = restOfTemplate.substring(0, index - CLOSE_BLOCK_STRING.length()) + restOfTemplate.substring(index, restOfTemplate.length());
                newTemplate = parse(view, newTemplate, templateRoot);
                //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "Replacement without tag:"+newTemplate);
                safeAppendReplacement(m, buf, newTemplate);
            } else
            {
                if (!block.isVisible())
                {
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing invisible block:" + jotId);
                    // keeping what's is AFTER the tag.
                    safeAppendReplacement(m, buf, restOfTemplate.substring(index, restOfTemplate.length()));
                } else
                {
                    if (block.getContent() != null)
                    {
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing block content of:" + jotId);
                        // replace old content by newContent
                        // keeping what's is AFTER the tag.
                        String newContent = parse(view, block.getContent(), templateRoot);
                        safeAppendReplacement(m, buf, newContent + restOfTemplate.substring(index, restOfTemplate.length()));
                    }
                }
            }
            template = buf.toString();
        //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "New template:"+template);
        }//end while

        return template;
    }

    /**
     * Replaces the template variables par their values
     * @param template
     * @param view
     * @return
     * @throws Exception
     */
    static String doVariables(String template, JOTViewParserData view) throws Exception
    {
        Hashtable variables = view.getVariables();

        Matcher m = VAR_PATTERN.matcher(template);
        StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String defaultVal = MISSING_VALUE;
            String varName = m.group(1).trim();
            if (m.groupCount() > 2 && m.group(3) != null)
            {
                defaultVal = m.group(3).trim();
            }
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Var found:" + varName + " default:" + defaultVal);
            Vector varHash = getVariableHash(varName);

            if (varHash.size() > 0)
            {
                Object obj = variables.get(varHash.get(0));
                Object value = getVariableValue(view, varHash, obj, defaultVal);
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Var value: " + value);
                safeAppendReplacement(m, buf, value.toString());
            }

        }

        m.appendTail(buf);
        return buf.toString();
    }

    /**
     * Standard java appendReplacement() use the $sign to do block replace stuff.
     * Anyhow i don't use that, but if my replacement string ass $ sign(or bacquote) in it will mess things up
     * and throw an exception.
     * So this method here is made to backquote the $ signs so they don't get interpreted.
     * As well as bacquoting the bacquotes ! so don't cause trouble either.
     * @param string
     * @return
     */
    public static void safeAppendReplacement(Matcher m, StringBuffer sb, String replacement)
    {
        replacement = replacement.replaceAll("\\\\", "\\\\\\\\");
        replacement = replacement.replaceAll("\\$", "\\\\\\$");
        //replacement=replacement.replaceAll("\\\\", "\\\\\\\\");
        m.appendReplacement(sb, replacement);
    }

    /**
     * Parse a template variable into pieces
     * ie: toto.tata.titi().tutu  becomes [toto,tata,titi,tutu]
     * @param varName
     * @return
     * @throws Exception
     */
    static Vector getVariableHash(String varName) throws Exception
    {
        Matcher memberMatcher = VAR_MEMBER_PATTERN.matcher(varName);
        Vector varHash = new Vector();
        if (varName.trim().startsWith("'") && varName.trim().endsWith("'"))
        {
            varHash.add(varName);
        } else
        {
            while (memberMatcher.find())
            {
                if (memberMatcher.group(1).length() > 0)
                {
                    varHash.add(memberMatcher.group(1));
                }
            }
        }
        return varHash;
    }

    /**
     * Removes the <jot:remove> tags
     * @param template
     * @return
     */
    public static String doRemoveTags(String template)
    {
        Matcher m = REMOVE_PATTERN.matcher(template);
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing all <jot:remove> tags content.");
        template = m.replaceAll("");
        return template;
    }

    /**
     * Evaluate a template variable and finds its value
     * @param varHash
     * @param obj
     * @param defaultVal
     * @return
     * @throws Exception
     */
    static Object getVariableValue(JOTViewParserData view, Vector varHash, Object obj, String defaultVal) throws Exception
    {
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "hash: " + varHash + " obj:" + obj);

        if (obj == null)
        {
            if (varHash.size() > 0)
            {
                String call = ((String) varHash.get(0)).trim();
                //maybe we have a string
                if (call.startsWith("'") && call.endsWith("'"))
                {
                    obj = call.substring(1, call.length() - 1);
                }
                //maybe we have an integer
                if (obj == null)
                {
                    try
                    {
                        obj = new Integer((String) varHash.get(0));
                    } catch (Exception e)
                    {
                    }
                }

                if (obj == null)
                {
                    //maybe we have a user function call

                    obj = evaluateObject(view, view, call);
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "call: " + call);
                }
            }
        }

        if (obj != null)
        {
            //JOTLogger.log(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing variable:"+varHash.get(0));
            for (int i = 1; i < varHash.size(); i++)
            {
                String name = ((String) varHash.get(i)).trim();

                Field field = null;
                Object prevObject = obj;
                // look for a method
                obj = evaluateObject(view, obj, name);
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "call2: " + name + " obj:" + obj);

                if (obj == null && name.indexOf("(") == -1)
                {
                    field = lookForField(prevObject, name);
                    if (field != null)
                    {
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Replacing var, Retrieveing Field: " + field.toString());
                        obj = field.get(prevObject);
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "call3: " + name + " obj:" + obj);
                    } else
                    {
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "NO Field: " + name);
                    }
                }
                if (obj == null)
                {
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL, JOTView.class, "Replacing var, no such field/method: " + name + " (" + varHash + ")");
                    break;
                }

            }
        }
        if (obj == null)
        {
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "Variable value could not be retrieved for: " + varHash + " , will use default:" + defaultVal);
            obj = defaultVal;
        }

        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "Variable value: " + obj);

        return obj;
    }

    /**
     * Once a variable is split into individual objects/fields, we call this to get each "piece" value
     * @param view
     * @param parent
     * @param call
     * @return
     * @throws java.lang.Exception
     */
    static Object evaluateObject(JOTViewParserData view, Object parent, String call) throws Exception
    {
        Object obj = null;
        if (call != null)
        {
            Object[] values = null;
            String method = call;
            int start = call.indexOf("(");
            int end = call.lastIndexOf(")");
            if (start > -1 && end > -1)
            {
                method = call.substring(0, start);
                String params = call.substring(start + 1, end);
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "PARAMS: " + params);
                Matcher matcher = PARAMS_PATTERN.matcher(params);
                Vector parameters = new Vector();
                while (matcher.find())
                {
                    String param = matcher.group(1);
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "PARAM: " + param);
                    if (param != null && param.length() > 0)
                    {
                        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "Found method param:" + param);
                        Vector hash = getVariableHash(param);
                        Object o = view.getVariables().get(hash.get(0));
                        Object value = getVariableValue(view, hash, o, param);
                        parameters.add(value);
                    }
                }
                values = new Object[parameters.size()];
                for (int i = 0; i != parameters.size(); i++)
                {
                    values[i] = parameters.get(i);
                }
            }
            if (JOTLogger.isTraceEnabled())
            {
                String params = "[";
                for (int i = 0; values != null && i != values.length; i++)
                {
                    params += values[i].getClass().getName() + ",";
                }
                params += "]";
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Looking for Method:" + method + params);
            }
            Method m = lookForMethod(parent, method, values);
            if (m != null)
            {
                obj = m.invoke(parent, values);
            }
        }
        return obj;
    }

    /**
     * Try to find a public Field with the given name in the object or null if it is not defined in the object
     * @param obj
     * @param field
     * @return
     */
    static Field lookForField(Object obj, String field)
    {
        Field f = null;
        try
        {
            f = obj.getClass().getField(field);
        } catch (Exception e)
        {
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "No such field: " + e.getMessage());
        //JOTLogger.logException(JOTLogger.CAT_FLOW,JOTLogger.TRACE_LEVEL, JOTView.class, "No such field: "+field,e);
        }
        return f;
    }

    /**
     * Try to find a Method of the given name in the object or null if it is not defined in the object
     * @param obj
     * @param method
     * @return
     */
    protected static Method lookForMethod(Object obj, String method, Object[] values)
    {
        if (method == null || method.length() < 1)
        {
            return null;
        }
        if (method.endsWith("()"))
        {
            method = method.substring(0, method.length() - 2);
        }
        Method m = null;

        m = JOTReflectionUtils.findCachedMethod(obj, method, values);
        if (m == null)
        {
            String method2 = "get" + method.substring(0, 1).toUpperCase() + method.substring(1, method.length());
            m = JOTReflectionUtils.findCachedMethod(obj, method2, values);
        }
        return m;
    }

    /**
     * Find the matching(balanced) closing html tag to the tag provided
     * Note that if the HTML is broken (unbalanced tags) this might break.
     * @param pos
     * @param template
     * @param openTag
     * @param closeTag
     * @param depth
     * @return
     */
    public static int findMatchingClosingTag(int pos, String template, Pattern openTag, Pattern closeTag, int depth)
    {
        // we came here with one tag opened, so starting with -1
        depth--;
        Matcher m2 = closeTag.matcher(template.substring(pos, template.length()));
        if (!m2.find())
        {
            // huho, no matching closing tag found .. broken html ??
            return -1;
        }
        int index = pos + m2.start();
        if (openTag != null)
        {
            Matcher m = openTag.matcher(template.substring(pos, index));
            while (m.find())
            {
                depth++;
            }
        }
        if (depth == 0)
        {
            return index + closeTag.pattern().length();
        } else
        {
            return findMatchingClosingTag(index + closeTag.pattern().length(), template, openTag, closeTag, depth);
        }
    }

    /**
     * Parse a whole template file
     * @param view
     * @param templateFile
     * @return
     * @throws Exception
     */
    public static String parseTemplate(JOTViewParserData view, String templateRoot, String templateFile) throws Exception
    {
        File f = new File(JOTUtilities.endWithSlash(templateRoot) + templateFile);
        long startTime = new GregorianCalendar().getTime().getTime();
        // Get template from cache
        String templateString = JOTTemplateCache.getTemplate(f.getAbsolutePath());
        String template = parse(view, templateString, templateRoot);
        long endTime = new GregorianCalendar().getTime().getTime();
        JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.DEBUG_LEVEL, JOTView.class, "Parsing of template: " + f.getAbsolutePath() + " took " + (endTime - startTime) + "ms");
        return template;
    }

    /**
     * For testing purposes.
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("cal: " + new GregorianCalendar());
        Pattern p2 = Pattern.compile("(<jot:block\\s+dataId=\"(\\S+)\"\\s*>)(.*)", PATTERN_FLAGS);
        String s = "Block1: (invisible, nothing should display after this)<jot:block dataId=\"block1\">This text<jot:block dataId=\"block2\">bl2</jot:block>is Block1 Dummy</jot:block><hr>One liner block (block2), should be replaced by a green array:<jot:block dataId=\"block2\"/><hr>Tag Block 3:<div jotid=\"block3\">This text is Block3 Dummy (in a div tag)</div><hr>Tag block 4 (One liner):<div class=\"default\" jotid=\"block4\"/><hr><table border=1>Loop1:<jot:loop dataId=\"loop1\"><tr><td>This is a loop iteration #<jot:var value=\"cpt\"/>: <jot:Var  value=\"iter\"	/> <br></td></tr></jot:loop><jot:remove><tr><td>Dummy loop iteration, that WILL NOT show at runtime, but will show in dreamweaver</td></tr></jot:remove></table><hr></body>";

        Matcher m = p2.matcher(s);
        while (m.find())
        {
            //block tag
            System.out.println("tag: " + m.group(1));
            //jotid
            System.out.println("jotid: " + m.group(2));
            //rest
            String rest = m.group(3);
            System.out.println("rest: " + rest);

            //int l=a.indexOf("<jot:loop dataId")+1;
            int i = findMatchingClosingTag(0, rest, OPEN_BLOCK_PATTERN, CLOSE_BLOCK_PATTERN, 1);
            System.out.println("tag reminder: " + rest.substring(0, i));
        //replace: remove the tag + content if invisible			
        //m.reset();
        }

    }

    /**
     * Replaces the flags in the HTML, such as "DISABLED" or "CHECKED"
     * @param openingTag
     * @param flags
     * @param oneLiner
     * @return
     */
    static String replaceFlags(String openingTag, Hashtable flags, boolean oneLiner)
    {
        Enumeration keys = flags.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            Boolean value = (Boolean) flags.get(key);

            Pattern quoted = Pattern.compile(key, PATTERN_FLAGS);

            Matcher m = quoted.matcher(openingTag);
            if (m.find())
            {
                if (!value.booleanValue())
                {
                    //we need to remove the flag
                    openingTag = m.replaceFirst("");
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Removing flag:" + key);
                }
            } else
            {
                if (value.booleanValue())
                {
                    //we need to ADD the flag
                    int offset = 1;
                    String closingTag = ">";
                    if (oneLiner)
                    {
                        offset = 2;
                        closingTag = "/>";
                    }
                    openingTag = openingTag.substring(0, openingTag.length() - offset) + " " + key + closingTag;
                    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Adding flag:" + key);
                }
            }
        }
        return openingTag;
    }

    private static String doWidgets(String template, JOTViewParserData view)
    {
        StringBuffer buf = new StringBuffer();

        Matcher m = WIDGET_PATTERN.matcher(template);
        while (m.find())
        {
            String code = m.group(1);
            String args = null;
            if (m.groupCount() > 2)
            {
                args = m.group(3);
            }
            JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, JOTView.class, "Found widget: " + code);

            Object instance;
            try
            {
                Class clazz = JOTFlowClassCache.getClass(code);
                instance = clazz.newInstance();
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, JOTView.class, "Failed to instantiate widget class: " + code, e);
                // do nothing
                return buf.toString();
            }
            if (instance instanceof JOTWidgetBase)
            {
                JOTWidgetBase widget = (JOTWidgetBase) instance;
                widget.init(view.getFullView());
                String widgetHtml = widget.render(args);
                safeAppendReplacement(m, buf, widgetHtml);
            } else
            {
                JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.ERROR_LEVEL, JOTView.class, "Widget instance failed ! not a JOTWidegetView instance !: " + code);
            }
        }
        m.appendTail(buf);
        return buf.toString();
    }
}
