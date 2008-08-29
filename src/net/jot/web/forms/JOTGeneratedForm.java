/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.jot.logger.JOTLogger;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.ui.JOTFormCaptchaField;
import net.jot.web.forms.ui.JOTFormCategory;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormPasswordField;
import net.jot.web.forms.ui.JOTFormSelectField;
import net.jot.web.forms.ui.JOTFormSubmitButton;
import net.jot.web.forms.ui.JOTFormTextField;
import net.jot.web.forms.ui.JOTFormTextareaField;
import net.jot.web.view.JOTFormParser;

/**
 * This is a generic object you can implement/subclass to have a simple HTML form generated from data.
 * You can subclass it as-is or reimplement/copy methods to modify the generated Html
 * Or implement one of the subclasses such as JOTDBForm
 * You will want to add the needed css classes to your css file.
 * see bellow getCss()
 * @author thibautc
 *
 */
public abstract class JOTGeneratedForm extends JOTForm
{

    public static final String HAD_SUCCESS = "_GeneratedForm_success";
    Vector items = new Vector();
    String title = "Form";
    String action = "";
    Hashtable lastResults = null;

    /**
    @param request 
     */
    public abstract void layoutForm(JOTFlowRequest request);

    /**
     * Ovveride this to change the look. if you don't like the default.
     */
    public String getHtml(JOTFlowRequest request) throws Exception
    {
        String html = getFormBowells(request);
        // process it like a regular form would.
        try
        {
            html = JOTFormParser.doElements(getAll(), html, null, null);
        } catch (Exception e)
        {
            JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "error parsing generated form.", e);
        }
        html = getJavascript() + getCss() + "<form jotclass=\"" + getClass().getName() + "\" action=\"" + action + "\" method=\"post\">" + html;
        html += "</table></form>";
        return html;
    }

    /**
     * Ovveride this if you want custom javascript
     */
    protected String getJavascript()
    {
        return "\n<script type='text/javascript'>\n" +
                "function gen_form_toggle(objName){\n" +
                " var obj=document.getElementById(objName);\n" +
                " obj.style.display=(obj.style.display!='block')? 'block' : 'none';}\n" +
                "</script>\n";
    }

    /**
     * Ovveride this to change the form CSS
     * It's probably best to have this css into your template and have this return an empty string.
     * This here are just default that would work independently of the template.
     */
    protected String getCss()
    {
        return "<!-- add those style definitions to your css (example):\n\n " +
                ".gen_form_content_td{text-align:center}" +
                ".gen_form_title{text-align:center;background-color: rgb(18, 52, 86);font-family: \"Times New Roman\",\"Times\",serif;font-weight: bold;font-size: 140%}\n" +
                ".gen_form_section{text-align:center;background-color: rgb(68, 102, 136);font-family: \"Times New Roman\",\"Times\",serif;font-weight: bold;font-size: 120%}\n" +
                ".gen_form_content{overflow-x: hidden;overflow-y: hidden;background-color: rgb(52, 81, 120);font-size: 100%;padding-left-value: 0.1em;padding-right-value: 0.1em;padding-top: 0.1em;padding-bottom: 0.5em;border-left-style: solid;border-bottom-style: solid;border-right-style: solid;border-top-style: solid;border-bottom-color: black;border-right-color: black;color: rgb(244, 240, 216)}\n" +
                ".gen_form_item{padding-bottom:1em;}\n" +
                ".gen_form_error{background-color:#ffdddd;border-color:#ff0000;color:#ff0000}\n" +
                ".gen_form_success{background-color:#ddffdd;border-color:#00ff00;color:#005500}\n" +
                "input.gen_form_edit[type=\"text\"], input.gen_form_edit[type=\"password\"], select.gen_form_edit{font-size: 100%;border-top-width: 1px;border-right-width: 1px;border-bottom-width: 1px;border-left-width: 1px;border-top-style: solid;border-right-style: solid;border-bottom-style: solid;border-left-style: solid;border-top-color: #8cacbb;border-right-color: #8cacbb;border-bottom-color: #8cacbb;border-left-color: #8cacbb;min-height: 22px;color: black;vertical-align: middle;display: inline;background-color: #dee7ec;}\n" +
                "input.gen_form_edit[type=\"text\"], input.gen_form_edit[type=\"password\"]{height: 18px;max-height: 22px;}\n" +
                "input.gen_form_button{font-weight:bold;color: #c8d4bc;background-color: #123456;border-top-width: 1px;border-right-width: 1px;border-bottom-width: 1px;border-left-width: 1px;border-top-style: solid;border-right-style: solid;border-bottom-style: solid;border-left-style: solid;border-top-color: black;border-right-color: black;border-bottom-color: black;border-left-color: black;vertical-align: middle;text-decoration: none;font-size: 100%;cursor: pointer;height: 22px;max-height: 22px;min-height: 22px;display: inline;}\n" +
                ".gen_form_help{position:absolute;z-layer:99;border-width:2px;border-style:solid;background-color:#fafbc6;display:none;color:#000000;max-width:400px;min-width:100px;min-height:50px;padding: 5px;}\n" +
                ".gen_form_help_link{text-decoration: none;border-bottom-style: none;color: #77cccc;}\n" +
                ".gen_form_help_link:hover{cursor: help;text-decoration: none;border-bottom-style: none;color: #99ffff;}\n" +
                "-->";
    }

    /**
     * Ovveride this to change the look of the description (a.k.a form field help CSS popup)
     * @param field
     * @param spanCpt
     * @return
     */
    protected String getDescription(JOTFormField field, int spanCpt)
    {
        String desc = "";
        if (field.getHelp() != null)
        {
            String spanId = "_help_span_" + spanCpt;
            desc += "<a class='gen_form_help_link' href='#' onClick=\"gen_form_toggle('" + spanId + "');\">" + field.getDescription() + "</a>";
            desc += "<br><div id='" + spanId + "' class='gen_form_help'>" + field.getHelp() + "</div>";
        } else
        {
            desc = field.getDescription();
        }
        return desc;
    }

    /**
     * Generates HTMl for the inside of the form, ovveride for cutom html
     */
    protected String getFormBowells(JOTFlowRequest request) throws Exception
    {
        String html = "<table class='gen_form_content'><tr><td colspan='2'  style='text-align:center' class='gen_form_title'>" + title + "</td></tr>\n";

        // success

        if (hasValidated())
        {
            html += "<tr><td colspan='2' class='gen_form_success'>Saved Successfully!</td><tr>\n";
        }
        // errors	
        if (getErrors() != null && getErrors().size() > 0)
        {
            html += "<tr><td colspan='2' class='gen_form_error'>\n";
            Iterator errors = getErrors().values().iterator();
            while (errors.hasNext())
            {
                html += "- " + (String) errors.next() + "<br>";
            }
            html += "</td><tr>\n";
        }

        int spanCpt = 0;
        for (int i = 0; i != items.size(); i++)
        {

            Object o = items.get(i);
            if (o instanceof JOTFormCategory)
            {
                String title = ((JOTFormCategory) o).getTitle();
                html += "<tr><td colspan='2' class='gen_form_section'>" + title + "</td><tr>\n";
            } else if (o instanceof JOTFormSubmitButton)
            {
                String title = ((JOTFormSubmitButton) o).getTitle();
                html += "<tr><td colspan='2' class='gen_form_content_td'><input type='submit' class='gen_form_button' value='" + title + "'></td></tr>\n";
            } else
            {
                JOTFormField field = (JOTFormField) o;
                String value = get(field.getName()).getValue();
                if (value == null)
                {
                    value = field.getDefaultValue();
                }
                String description = getDescription(field, spanCpt);
                spanCpt++;
                // must be a field
                String head = "<tr><td class='gen_form_content_td' style='text-align:left'>" + description + "</td><td>\n";
                String tail = "\n</td></tr>\n";
                switch (field.getType())
                {
                    case JOTFormConst.INPUT_TEXT:
                        int size = ((JOTFormTextField) o).getSize();
                        html += head + "<input name=\"" + field.getName() + "\" type=\"text\" class=\"gen_form_edit\" size=\"" + size + "\"  value=\"" + value + "\">" + tail;
                        break;
                    case JOTFormConst.INPUT_CAPTCHA:
                        int size2 = ((JOTFormCaptchaField) o).getSize();
                        html += head + "<img src='" + ((JOTFormCaptchaField) o).getCaptchcaUrl() + "' ALT='Too many attempts.'><br/>";
                        html += "Enter Code:<input name=\"" + field.getName() + "\" type=\"text\" class=\"gen_form_edit\" size=\"" + size2 + "\"  value=\"" + value + "\">" + tail;
                        break;
                    case JOTFormConst.TEXTAREA:
                        int cols = ((JOTFormTextareaField) o).getCols();
                        int rows = ((JOTFormTextareaField) o).getRows();
                        html += head + "<textarea name=\"" + field.getName() + "\" cols=\"" + cols + "\" class=\"gen_form_edit\" rows=\"" + rows + "\" >" + value + "</textarea>" + tail;
                        break;
                    case JOTFormConst.INPUT_CHECKBOX:
                        String checked = field.getDefaultValue().equals("true") ? "CHECKED" : "";
                        html += head + "<input name=\"" + field.getName() + "\" type=\"checkbox\" class=\"gen_form_edit\" " + checked + ">" + tail;
                        break;
                    case JOTFormConst.INPUT_HIDDEN:
                        html += head + "<input name=\"" + field.getName() + "\" type=\"hidden\" value=\"" + value + "\">" + tail;
                        break;
                    case JOTFormConst.INPUT_PASSWORD:
                        int psize = ((JOTFormPasswordField) o).getSize();
                        html += head + "<input name=\"" + field.getName() + "\" type=\"password\" size=\"" + psize + "\" class=\"gen_form_edit\" value=\"" + value + "\">" + tail;
                        break;
                    case JOTFormConst.INPUT_RADIO:
                        html += head + "<input name=\"" + field.getName() + "\" type=\"radio\" class=\"gen_form_edit\" " + field.getDefaultValue() + ">" + tail;
                        break;
                    case JOTFormConst.SELECT:
                        JOTFormSelectField select = (JOTFormSelectField) o;
                        html += head + "<select name=\"" + field.getName() + "\" class=\"gen_form_edit\" size=\"" + select.getSize() + "\"" + (select.getAllowMultiples() ? " MULTIPLE" : "") + ">";
                        String[] possibles = select.getPossibleValues();
                        String[] descriptions = select.getPossibleDescriptions();
                        String[] selected = value.split(",");
                        List selectedList = Arrays.asList(selected);
                        for (int j = 0; j != select.getPossibleValues().length; j++)
                        {
                            String val = possibles[j];
                            String desc = descriptions[j];
                            String selectString = selectedList.contains(val) ? JOTFormConst.VALUE_SELECTED : JOTFormConst.VALUE_UNSELECTED;
                            html += "<option value=\"" + val + "\" " + selectString + ">" + desc + "\n";
                        }
                        html += "</select>" + tail;
                        break;
                }
            }

        }
        return html;
    }

    /**
    Adds a form field
    @param field 
     */
    public void addFormField(JOTFormField field)
    {
        items.add(field);
        defineField(field.getName(), field.getType());
        if (field.getType() == JOTFormConst.SELECT)
        {
            JOTFormSelectField select = (JOTFormSelectField) field;
            JOTFormElement el = get(field.getName());
            String[] options = select.getPossibleValues();
            if (options != null)
            {
                for (int i = 0; i != options.length; i++)
                {
                    el.set(options[i], new JOTFormElement(JOTFormConst.SELECT_OPTION));
                }
            }
        }
    }

    /**
    Adds a form category (form section sperator with title)
    @param cat 
     */
    public void addCategory(JOTFormCategory cat)
    {
        items.add(cat);
    }

    /**
    The form submit button
    @param button 
     */
    public void addSubmitButton(JOTFormSubmitButton button)
    {
        items.add(button);
    }

    /**
    Mai title for the form, that appears at the top of the form
    @param title 
     */
    public void setFormTitle(String title)
    {
        this.title = title;
    }

    public void init(JOTFlowRequest request) throws Exception
    {
        items = new Vector();
        refreshData(request);
    }

    public void refreshData(JOTFlowRequest request) throws Exception
    {
        items = new Vector();
        layoutForm(request);
    }

    public String getFormAction()
    {
        return action;
    }

    /**
    Sets where the form should go to "action"
    Ex: setFormAction("submitform.do");
    @param action 
     */
    public void setFormAction(String action)
    {
        this.action = action;
    }

    /**
    Implement this to validate the form
    Return a hashtable of errors, or null/empty hashtable if no errors
    @return 
    @throws java.lang.Exception 
     */
    public abstract Hashtable validateForm(JOTFlowRequest request) throws Exception;

    public final Hashtable validate(JOTFlowRequest request) throws Exception
    {
        return validateForm(request);
    }

    /**
    Returns all the items (fields,categories,buttons etc..) composing this form.
    @return 
     */
    public Vector getItems()
    {
        return items;
    }
}
