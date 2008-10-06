/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance.query;

import net.jot.persistance.JOTTransaction;

import net.jot.db.JOTDBField;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTQueryResult;
import net.jot.persistance.JOTStatementFlags;

/**
 * Interface to query implementations<br>
 * The query Implementations provide backend specific support for executing search querries for Models.
 * @author thibautc
 *
 */
public interface JOTQueryInterface
{

	/**
	 * This is here, if you want to make manual custom SQL calls not covered by the other methods<br>
	 * 
	 * NOTE: your request MUST return records matching your model.<br>
	 * <b>THIS IS ONLY SUPPORTED WITH THE SQL Model (JOTDBModel)<b>
	 * 
	 * @param sql   ie: "select * from 'users' where first=?, last=? order by name" ... etc ...
	 * @param params ie: ['John','Doe']
	 * @return
	 */
	public JOTQueryResult executeSQL(JOTTransaction transaction, JOTModelMapping mapping, Class objectClass, String sql, Object[] params, JOTStatementFlags flags) throws Exception;

        /**
         * An "Update" type queries returns no results:
         * IE: insert, delete etc...
         * @param transaction
         * @param mapping
         * @param objectClass
         * @param sql
         * @param params
         * @param flags
         * @return
         * @throws java.lang.Exception
         */
        public void updateSQL(JOTTransaction transaction, JOTModelMapping mapping, String sql, Object[] params, JOTStatementFlags flags) throws Exception;
	
        /**
         * Saves record in backend
         * @param model
         * @throws java.lang.Exception
         */
	public void save(JOTTransaction transaction, JOTModel model) throws Exception;

        /**
         * Delete the WHOLE TABLE in backend
         * @param mapping
         * @throws java.lang.Exception
         */
	public void deleteTable(JOTModelMapping mapping) throws Exception;
        /**
         * Delete the WHOLE TABLE in backend
         * @param mapping
         * @throws java.lang.Exception
         */
	public void createTable(JOTModelMapping mapping) throws Exception;

        /**
         * Delete record in backend
         * @param model
         * @throws java.lang.Exception
         */
	//public void delete(JOTTransaction transaction, JOTModel model) throws Exception;

        /**
         * Add a new Field to a table
         * The field must be defined in the DB Model.
         * @throws java.lang.Exception
         */
        public void alterAddField(JOTModelMapping mapping, JOTDBField field, Object defaultValue) throws Exception;
        
  
}
