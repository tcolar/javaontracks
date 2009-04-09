/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms;

import com.sun.tools.jdi.LinkedHashMap;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import net.jot.persistance.JOTModel;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.forms.ui.JOTFormSelectField;

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
	protected Class modelClass = null;
	/** The entries to list / edit (results of a db query)**/
	protected Vector dataEntries = null;
	protected boolean allowDelete = true;
	/**
	 * 0 = none / disallow new
	 */
	protected int nbNewLines = 3;

	/**
	 * Ovveride and make call to addColumn() to define which columns will be displayed.
	 * @param request
	 */
	public abstract void defineColumns(JOTFlowRequest request);

	/**
	 * this should set modelClass and dataEntries(from a DB query)
	 */
	public abstract void updateModel(JOTFlowRequest request);

	public String getHtml(JOTFlowRequest request) throws Exception
	{
		// TODO
		// column headers
		// column data
		return "";
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
					addFormField(new Long(model.getId()), col.getField());
				}
			}
		}
		// fields for new entries
		if (nbNewLines > 0)
		{
			// line = -1.-2,-3 ??
			for (int i = 0; i != -nbNewLines; i--)
			{
				Enumeration elms = columns.elements();
				while (elms.hasMoreElements())
				{
					Column col = (Column) elms.nextElement();
					addFormField(new Long(i), col.getField());
				}
			}
		}
	}

	public Hashtable validate(JOTFlowRequest request) throws Exception
	{
		// TODO
		return null;
	}

	public void save(JOTFlowRequest request) throws Exception
	{
		// TODO
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

	private void addColumn(JOTFormField field)
	{
		columns.add(new Column(field));
	}

	/**
	Adds a form field
	@param field
	 */
	private void addFormField(Long line, JOTFormField field)
	{
		// make the name unique per line
		field.setName(field.getName()+"_#"+line);

		Vector v = (Vector) items.get(line);
		if (v == null)
		{
			v = new Vector();
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
