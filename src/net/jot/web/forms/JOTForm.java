/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.Vector;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.web.JOTFlowRequest;
import net.jot.web.ctrl.JOTController;
import net.jot.web.forms.ui.JOTFormField;
import net.jot.web.view.JOTViewTag;

/**
 * Generic class for handling HTML forms (validate, save the values when validation fail etc...)
 * You probably will want to use one of the subclass rather than this directly: ie: JOTGeneratedForm, JOTDBForm etc...
 * Or you can Subclass this directly for more control
 * Form objects provide easy parsing/validation of web forms
 * @author thibautc
 *
 */
public abstract class JOTForm extends JOTViewTag
{

	public static final String REQUEST_ID = "__JOT__SUBMITTED_FORM";
	private Map requestParams = null;
	;
	/**
	 * Stores the defined form elements (~form fields)
	 */
	private Hashtable elements = new Hashtable();
	/** store validation error messages*/
	private Hashtable errors = null;
	/** store validation success status*/
	private boolean hasValidated = false;
	/**
	End result, can be set by the user to branch to different places.
	by defaut if the form saved ok it's success
	in case of validation failure it would be  JOTController.RESULT_VALIDATION_FAILURE
	 */
	private String result = JOTController.RESULT_SUCCESS;

	/**
	 * Implement this and return true if user is authorized to use this form
	 * @param request
	 * @return
	 */
	public abstract boolean validatePermissions(JOTFlowRequest request);

	/**
	 * You can use this method to set initial form values (Called the first time the form is created)
	 * Ex:     defineField("name", JOTFormConst.TEXTAREA);    get("fname").setValue("toto");
	 */
	public abstract void init(JOTFlowRequest request) throws Exception;

	/**
	 * You need to define this method to you want to validate your form.
	 * The returned a hashtable or error messages (String error_id, String error_text)
	 * The hashtable should be empty/null if no errors occured.
	 * Ex: Hashtable h=new Hashtable(); h.put("error1","Password invalid");
	 */
	public abstract Hashtable validate(JOTFlowRequest request) throws Exception;

	/**
	 * This will be called after a succesful validation
	 * This is where you save your form data .. wherever you want to save it like a database etc ...
	 * Implementation note: if the field is saveAutomatically=false, then we won't save that field
	 * @return
	 */
	public abstract void save(JOTFlowRequest request) throws Exception;

	/**
	 * Override this function if you want to do things before validation runs
	 *
	 */
	public void preValidate()
	{
	}

	/**
	 * Define a field of the form (name/field type)
	 * You should define all the types you want to use / see in the html form
	 * Typically you make calls to this method from within init()
	 * @param name
	 * @param type
	 */
	public void defineField(String name, int type)
	{
		elements.put(name, new JOTFormElement(type));
	}

	/**
	 * Return all the form fields
	 * Hastable of {name:JOTFormElement}
	 */
	public Hashtable getAll()
	{
		return elements;
	}

	/**
	 * Return an element (by name)
	 * @param name
	 * @return
	 */
	public JOTFormElement get(String name)
	{
		JOTFormElement elem = ((JOTFormElement) elements.get(name));
		if (elem != null)
		{
			int type = elem.getType();
			return get(type, name);
		}
		return null;
	}

	/**
	 * Returns an element.
	 * @param type
	 * @param name
	 * @return
	 */
	private JOTFormElement get(int type, String name)
	{
		JOTFormElement el = (JOTFormElement) elements.get(name);
		if (el == null)
		{
			el = new JOTFormElement(type);
			String value = "";
			if (requestParams != null && requestParams.containsKey(name))
			{
				String[] values = (String[]) requestParams.get(name);
				value = values[0];
			} else
			{
				// set to false checkbox, radio etc...
				// browser don't send checbox=off, but instead don't send it at all if off
				/*if(type==JOTFormConst.INPUT_CHECKBOX || type==JOTFormConst.INPUT_RADIO)
				{
				value=JOTFormConst.VALUE_UNCHECKED;
				}*/
			}
			el.setValue(value);
			elements.put(name, el);
		}
		return el;
	}

	/**
	 * if new values are found in the request, this is going to be called to parse the new values
	 * @param request
	 */
	public void reparseForm(JOTFlowRequest request)
	{
		JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "Reparsing form");
		requestParams = request.getParameterMap();

		Set set = elements.keySet();
		Iterator i = set.iterator();

		while (i.hasNext())
		{
			String key = (String) i.next();
			JOTFormElement el = (JOTFormElement) elements.get(key);
			if (requestParams.containsKey(key))
			{
				String[] values = (String[]) requestParams.get(key);
				String value = "";
				for (int j = 0; j != values.length; j++)
				{
					value += values[j] + ",";
				}
				if (value.endsWith(","))
				{
					value = value.substring(0, value.length() - 1);
				}
				JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.TRACE_LEVEL, this, "New Value from request: " + values[0]);
				el.setValue(value);
			} else
			{
				if (el.getType() == JOTFormConst.INPUT_CHECKBOX || el.getType() == JOTFormConst.INPUT_RADIO)
				{
					el.setValue(JOTFormConst.VALUE_UNCHECKED);
				} else if (el.getType() == JOTFormConst.SELECT)
				{
					el.setValue("");
				}
			}
			elements.put(key, el);
		}

	}

	public Hashtable getErrors()
	{
		return errors;
	}

	public void setErrors(Hashtable errors)
	{
		this.errors = errors;
	}

	public boolean hasValidated()
	{
		return hasValidated;
	}

	public void setHasValidated(boolean hasValidated)
	{
		this.hasValidated = hasValidated;
	}

	/**
	Result of the form processing: ie: success, validationFailure etc...
	@return
	 */
	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	/**
	 * Update a model's values using the request params.
	 * Internal use.
	 * @param model
	 * @param items
	 * @param request
	 * @throws java.lang.Exception
	 */
	protected JOTModel updateModelFromRequest(JOTModel model, Vector items, JOTFlowRequest request) throws Exception
	{
		for (int i = 0; i != items.size(); i++)
		{
			if (items.get(i) instanceof JOTFormField)
			{
				JOTFormField field = (JOTFormField) items.get(i);
				String name = field.getName();
				JOTFormElement el = get(name);
				if (el != null && field.isSaveAutomatically())
				{
					Object value = el.getValue();
					Field f = getField(model, name);
					if (model.getMapping().getFields().containsKey(name))
					{
						boolean isTransient = f != null && Modifier.isTransient(f.getModifiers());
						if (!isTransient && !name.startsWith("__"))
						{
							String v = (String) value;
							if (f.getType() == String.class)
							{
								value = (String) value;
							} else if (f.getType() == Boolean.class)
							{
								value = new Boolean(v.length() > 0 && !v.equalsIgnoreCase("false"));
							} else if (f.getType() == Integer.class)
							{
								value = new Integer(v);
							} else if (f.getType() == Byte.class)
							{
								value = new Byte(v);
							} else if (f.getType() == Short.class)
							{
								value = new Short(v);
							} else if (f.getType() == Long.class)
							{
								value = new Long(v);
							} else if (f.getType() == Float.class)
							{
								value = new Float(v);
							} else if (f.getType() == Double.class)
							{
								value = new Double(v);
							} else if (f.getType() == BigDecimal.class)
							{
								value = new BigDecimal(v);
							}

							//TBD: timstamp / date /time ?
							f.set(model, value);
						} else
						{
							JOTLogger.debug(this, "Skipping form field: " + name);
						}
					} else
					{
						JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Form field: '" + name + "' does not exist in DB, ignoring.");
					}
				}
			}
		}
		return model;
	}

	private Field getField(JOTModel model, String fieldName)
	{
		Field f = null;
		try
		{
			f = model.getClass().getField(fieldName);
		} catch (NoSuchFieldException e)
		{
		}
		return f;
	}
}
