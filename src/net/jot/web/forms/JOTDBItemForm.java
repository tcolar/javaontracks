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

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.utils.JOTUtilities;
import net.jot.web.JOTFlowRequest;
import net.jot.web.forms.ui.JOTFormField;

/**
 * This form helps into automatically generating/parsing an HTMl form to/From a Database entry(DB Model) 
 * It allows for simple CRUD actions on a particular db object (just one)
 *
 * This is an extension of JOTGeneratedForm, see that for extra documentation.
 *
 * @author thibautc
 *
 */
public abstract class JOTDBItemForm extends JOTGeneratedForm
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
        long id = getModel().getId();
        if (id != -1)
        {
            String modelClass = getModel().getClass().getName();
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
                        model = JOTQueryBuilder.findByID(null, theClass, new Integer(id).intValue());
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
		model=updateModelFromRequest(model, items, request);
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
