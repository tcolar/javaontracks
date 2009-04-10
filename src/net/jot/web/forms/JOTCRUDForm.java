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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
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
 * This form helps into automatically generating/parsing an HTMl from a Database Table
 * It allows editing of the whole table(or a subset) and add/delete rows etc..
 *
 * This is basically a CRUD DB table editor (shows data as a grid)
 *
 * This is an extension of JOTGeneratedForm, see that for extra documentation.
 *
 * @author thibautc
 *
 */
public abstract class JOTCRUDForm extends JOTForm implements JOTGeneratedFormInterface
{

	/** set in subclass **/
	public String action = "";
	public String title = "Form";
	/** Vector of columns that will be displayed with associated properties**/
	private Vector columns = new Vector();
	/* hash of vectors(sorted) of items(form fields) by line. (hash key is id)*/
	private LinkedHashMap items = new LinkedHashMap();
	/** ID for JOT form*/
	public static final String JOT_GENERATED_FORM_ID = "JOT_GENERATED_FORM_ID";
	/** The token is here for security reasons, so that somebody can't fake the form request and change a different DB entry than the one he is suppose to be editing*/
	public static final String JOT_GENERATED_FORM_TOKEN = "JOT_GENERATED_FORM_TOKEN";
	/** stores the form/model java class */
	public static final String JOT_GENERATED_FORM_MODEL_CLASS = "JOT_GENERATED_FORM_MODEL_CLASS";
	/** You should set those value in your implementation in upatemodel()*/
	public Class modelClass = null;
	/** The entries to list / edit (results of a db query)**/
	public Vector dataEntries = null;
	public boolean allowDelete = true;
	/**
	 * 0 = none / disallow new
	 */
	public int nbNewLines = 3;

	/**
	 * Ovveride and make call to addColumn() to define which columns will be displayed.
	 * @param request
	 */
	public abstract void defineColumns(JOTFlowRequest request);

	/**
	 * this should set modelClass and dataEntries(from a DB query)
	 */
	public abstract void updateModel(JOTFlowRequest request) throws Exception;

	/**
	 * Add a column to be displayed on the page
	 * Notes:
	 * value will be ignored (replaced by actual value in model), except for new entries
	 * If filed is set as disabled, then the column won't be shown
	 * @param field
	 */
	protected void addColumn(JOTFormField field)
	{
		columns.add(new Column(field));
	}

	/**
	 * catId: a unique id for that category
	 * @param catId
	 * @param button
	 */
	public void addCategory(String catId, JOTFormCategory category)
	{
		Vector v = new Vector();
		v.add(category);
		items.put(catId, v);
	}

	/**
	 * buttonId: a unique id for that button
	 * @param buttonId
	 * @param button
	 */
	public void addSubmitButton(String buttonId, JOTFormSubmitButton button)
	{
		Vector v = new Vector();
		v.add(button);
		items.put(buttonId, v);
	}

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

	public void init(JOTFlowRequest request) throws Exception
	{
		//Hastable fields = model.getMapping().getFields()
		columns = new Vector();
		items = new LinkedHashMap();
		defineColumns(request);
		updateModel(request);
		// update the data
		if (dataEntries != null && dataEntries.size() > 0)
		{
			for (int i = 0; i != dataEntries.size(); i++)
			{
				JOTModel model = (JOTModel) dataEntries.get(i);
				Enumeration elms = columns.elements();
				while (elms.hasMoreElements())
				{
					Column col = (Column) elms.nextElement();
					JOTFormField field = col.getField();
					field.setDefaultValue(model.getFieldValue(field.getName()).toString());
					addFormField(new Long(model.getId()), field);
				}
			}
		}
		// fields for new entries
		if (nbNewLines > 0)
		{
			addCategory("new_cat", new JOTFormCategory("Add new entries:"));
			// line = -1.-2,-3 ??
			for (int i = 0; i != -nbNewLines; i--)
			{
				Enumeration elms = columns.elements();
				while (elms.hasMoreElements())
				{
					Column col = (Column) elms.nextElement();
					addFormField(new Long(i - 1), col.getField());
				}
			}
		}
		addSubmitButton("submit", new JOTFormSubmitButton("Save"));
	}

	public abstract Hashtable validateForm(JOTFlowRequest request) throws Exception;

	public final Hashtable validate(JOTFlowRequest request) throws Exception
	{
		return validateForm(request);
	}

	public void save(JOTFlowRequest request) throws Exception
	{
		// existing entries
		if (dataEntries != null && dataEntries.size() > 0)
		{
			for (int i = 0; i != dataEntries.size(); i++)
			{
				JOTModel model = (JOTModel) dataEntries.get(i);
				Vector v = (Vector) items.get(new Long(model.getId()));
				updateModelFromRequest(model, v, request);
				//model.save();
			}
		}
		// new entries
		for (int i = 0; i != nbNewLines; i++)
		{
			int index = -i - 1;
			Vector v = (Vector) items.get(new Long(index));
			JOTModel model=(JOTModel)modelClass.newInstance();
			updateModelFromRequest(model, v, request);
			if (isNewEntryValid(model))
				model.save();
		}
	}

	// setters / getters
	public void setAllowDelete(boolean allowDelete)
	{
		this.allowDelete = allowDelete;
	}

	public void setNbNewLines(int nbNewLines)
	{
		this.nbNewLines = nbNewLines;
	}

	/**
	Adds a form field
	@param field
	 */
	private void addFormField(Long line, JOTFormField fieldDef)
	{
		// make the name unique per line
		JOTFormField field = null;
		// the fiel passed is a "definition", so we need to make clones with their own names
		try
		{
			field = (JOTFormField) fieldDef.clone();
			field.setName(field.getName() + "_#" + line);
		} catch (CloneNotSupportedException e)
		{
			JOTLogger.logException(this, "Failed cloning field !!", e);
			return;
		}
		Vector v = (Vector) items.get(line);
		if (v == null)
		{
			v = new Vector();
			items.put(line, v);
		}
		v.add(field);
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
				".gen_form_th{text-align:center;font-weight:bold;background-color:#CCCCCC}" +
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
	 * Generates HTMl for the inside of the form, ovveride for cutom html
	 */
	protected String getFormBowells(JOTFlowRequest request) throws Exception
	{
		String html = "<table class='gen_form_content'><tr><td colspan='" + columns.size() + "'  style='text-align:center' class='gen_form_title'>" + title + "</td></tr>\n";

		// success
		if (hasValidated())
		{
			html += "<tr><td colspan='" + columns.size() + "' class='gen_form_success'>Saved Successfully!</td><tr>\n";
		}
		// errors
		if (getErrors() != null && getErrors().size() > 0)
		{
			html += "<tr><td colspan='" + columns.size() + "' class='gen_form_error'>\n";
			Iterator errors = getErrors().values().iterator();
			while (errors.hasNext())
			{
				html += "- " + (String) errors.next() + "<br>";
			}
			html += "</td><tr>\n";
		}
		//Header
		html += "<tr>";
		for (int i = 0; i != columns.size(); i++)
		{
			Column col = (Column) columns.get(i);
			JOTFormField field = col.getField();
			if (field.getHelp() == null)
			{
				html += "<th class='gen_form_th'>" + field.getDescription() + "</th>";
			} else
			{
				String spanId = "_help_span_" + i;
				html += "<th class='gen_form_th'><a class='gen_form_help_link' href='#' onClick=\"gen_form_toggle('" + spanId + "');\">" + field.getDescription() + "</a>";
				html += "<br><div id='" + spanId + "' class='gen_form_help'>" + field.getHelp() + "</div></th>";
			}
		}
		html += "</tr>";
		//existing data
		Iterator it = items.keySet().iterator();
		while (it.hasNext())
		{
			Object key = (Object) it.next();
			Vector line = (Vector) items.get(key);
			html += "<tr>\n";
			for (int i = 0; i != line.size(); i++)
			{
				Object o = line.get(i);
				if (o instanceof JOTFormCategory)
				{
					String title = ((JOTFormCategory) o).getTitle();
					html += "</tr><tr><td colspan='" + columns.size() + "' class='gen_form_section'>" + title + "</td>\n";
				} else if (o instanceof JOTFormSubmitButton)
				{
					// submit button on it's own line (must be last)
					String title = ((JOTFormSubmitButton) o).getTitle();
					html += "</tr><tr><td colspan='" + columns.size() + "' class='gen_form_content_td'><input type='submit' class='gen_form_button' value='" + title + "'></td>\n";
				} else
				{
					JOTFormField field = (JOTFormField) o;
					String value = get(field.getName()).getValue();
					if (value == null)
					{
						value = field.getDefaultValue();
					}
					//String description = getDescription(field, spanCpt);
					// must be a field
					String head = "<td class='gen_form_content_td'>\n";
					String tail = "\n</td>\n";
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
			html += "</tr>\n";
		}
		return html;
	}

	/**
	 * When there are fields to enter a new DB entry, the created db model
	 * will be passed to this method and you can check if you want to save it or not.
	 * Ex: check that the user DID enter some data.
	 * Return false to discard, or true to dave this model.
	 * @param model
	 * @return
	 */
	protected abstract boolean isNewEntryValid(JOTModel model);

	/**
	 * Inner utility class
	 */
	class Column
	{

		JOTFormField field = null;

		Column(JOTFormField field)
		{
			this.field = field;
		}

		public JOTFormField getField()
		{
			return field;
		}
	}
}
