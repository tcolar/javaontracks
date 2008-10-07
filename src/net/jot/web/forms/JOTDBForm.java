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
import java.math.BigDecimal;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.ui.JOTFormField;

/**
 * This form helps into automatically generating/parsing an HTMl form to/From a Database entry(DB Model) 
 * It allows for simple CRUD actions on a db object.
 *
 * This is an extension of JOTGeneratedForm, see that for extra documentation.
 *
 * TODO: maybe sublass this for fully automated form
 * this would use a text file description of the form 
 * and be fully automatic (stored in db)
 * @author thibautc
 *
 */
public abstract class JOTDBForm extends JOTGeneratedForm
{

    /** ID for JOT form*/
    public static final String JOT_GENERATED_FORM_ID = "JOT_GENERATED_FORM_ID";
    /** The token is here for security reasons, so that somebody can't fake the form request and change a different DB entry than the one he is suppose to be editing*/
    public static final String JOT_GENERATED_FORM_TOKEN = "JOT_GENERATED_FORM_TOKEN";
    /** stores the form/model java class */
    public static final String JOT_GENERATED_FORM_MODEL_CLASS = "JOT_GENERATED_FORM_MODEL_CLASS";
    /** You should set this value in your implementatio in upatemodel()*/
    protected JOTModel model = null;

    /**
    Generate the html for the "inside" of the form, using the DB object(model) to set the values etc...
    @param request 
    @return 
     */
    protected String getFormBowells(JOTFlowRequest request) throws Exception
    {
        // start with standard call
        String html = super.getFormBowells(request);

        // We want to add the form ID
        JOTDBForm form = (JOTDBForm) this;
        long id = form.getModel().getId();
        if (id != -1)
        {
            String modelClass = form.getModel().getClass().getName();
            html += "\n<input type='hidden' name='" + JOT_GENERATED_FORM_ID + "' value='" + id + "'>";

            //We also add an ID token for a bit of security, so a user can't just mess with the ID and mess things up too much.
            String token = "SHA1_FAILURE";
            try
            {
                token = JOTUtilities.getSHA1Hash(modelClass + id);
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "Failed to generate SHA-1 hash !", e);
            }
            html += "\n<input type='hidden' name='" + JOT_GENERATED_FORM_TOKEN + "' value='" + token + "'>";
            html += "\n<input type='hidden' name='" + JOT_GENERATED_FORM_MODEL_CLASS + "' value='" + modelClass + "'>";
        }
        return html;
    }

    /** Adds a field to the form*/
    public void addFormField(JOTFormField field)
    {
        Object obj = model.getFieldValue(field.getName());
        if (!model.isNew() && obj != null)
        {
            field.setDefaultValue(obj.toString());
        }
        super.addFormField(field);
    }

    /**
     * Needs to be called whenever you want the form data to be re-fetched from the data source/db
     * Need to be called at least once after the form is created.
     */
    public void refreshData(JOTFlowRequest request) throws Exception
    {
        if (request.getParameter(JOT_GENERATED_FORM_ID) != null)
        {
            // This is a form being submitted, so getting that form by it's ID
            boolean good = false;
            if (request.getParameter(JOT_GENERATED_FORM_ID) != null)
            {
                if (request.getParameter(JOT_GENERATED_FORM_MODEL_CLASS) != null)
                {
                    String id = request.getParameter(JOT_GENERATED_FORM_ID);
                    String token = request.getParameter(JOT_GENERATED_FORM_TOKEN);
                    String modelClass = request.getParameter(JOT_GENERATED_FORM_MODEL_CLASS);
                    if (JOTUtilities.getSHA1Hash(modelClass + id).equals(token))
                    {
                        good = true;
                        Class theClass = Class.forName(modelClass);
                        model = JOTQueryBuilder.findByID(theClass, new Integer(id).intValue());
                    }
                }
            }
            if (!good)
            {
                throw new Exception("Could not validate form id token !");
            }
        } else
        {
            // this is an initial form request, so calling updateModel impl. to get it.
            updateModel(request);
        }
        super.refreshData(request);
    }

    /**
    Saves the form value into the DB object.
    @param request 
    @throws java.lang.Exception 
     */
    public void save(JOTFlowRequest request) throws Exception
    {
        // get all the form fields values and store them in the model
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
                    //#TODO: fix this , should noty use "data"
                    if(3<1+5)
                        throw(new Exception("need to be fixed!"));
                    if (name.startsWith("data") && model.getMapping().getFields().containsKey(name))
                    {
                        Field f = model.getClass().getField(name);
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
                        JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.DEBUG_LEVEL, this, "Form field: '" + name + "' does not exist in DB, ignoring.");
                    }
                }
            }
        }
        // then save the model
        model.save();
    }

    
    /**
     * This should be implemented so that it:
     * - retrieves/update the "model" object(ie: does the db query to find/update the DB/Model object).
     * 
     * ex:
     * .... 
     * JOTSQLQueryParams params=new JOTSQLQueryParams();
     * params.addCondition(new JOTSQLCondition("id",JOTSQLCondition.IS_EQUAL,request.getParameter("userId")));
     * model=JOTQueryManager.findOrCreateOne(MyModelImpl.class, params);
     * ....
     */
    public abstract void updateModel(JOTFlowRequest request) throws Exception;

    public JOTModel getModel()
    {
        return model;
    }
}
