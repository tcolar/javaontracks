/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance.query;

import java.util.Vector;

import net.jot.db.JOTDBField;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTSQLQueryParams;

/**
 * Interface to query implementations<br>
 * The query Implementations provide backend specific support for executing search querries for Models.
 * @author thibautc
 *
 */
public interface JOTQueryInterface
{
        
	/**
	 * Returns the record whith the given ID <br>
	 * @param dataId
	 * @return
	 */
	public JOTModel findByID(JOTModelMapping mapping, Class objectClass, long id) throws Exception;
		
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
	public Vector findUsingSQL(JOTModelMapping mapping, Class objectClass,String sql, Object[] params) throws Exception;
	
	/**
	 * Returns the first records matching the parameters<br>
	 * @return
	 */
	public JOTModel findOne(JOTModelMapping mapping, Class objectClass,JOTSQLQueryParams params) throws Exception;

	/**
	 * Returns all the records matching the parameters<br>
	 * @return
	 */
	public Vector find(JOTModelMapping mapping, Class objectClass,JOTSQLQueryParams params) throws Exception;

	public void createTable(JOTModelMapping mapping) throws Exception; 

        /**
         * Saves record in backend
         * @param model
         * @throws java.lang.Exception
         */
	public void save(JOTModel model) throws Exception;

        /**
         * Delete the WHOLE TABLE in backend
         * @param mapping
         * @throws java.lang.Exception
         */
	public void deleteTable(JOTModelMapping mapping) throws Exception;

        /**
         * Delete record in backend
         * @param model
         * @throws java.lang.Exception
         */
	public void delete(JOTModel model) throws Exception;

        /**
         * Add a new Field to a table
         * The field must be defined in the DB Model.
         * @throws java.lang.Exception
         */
        public void alterAddField(JOTModelMapping mapping, JOTDBField field, Object defaultValue) throws Exception;
        
        
        /**
         * Removed a field from a table
         * THAT FIELD/Column DATA WILL BE LOST.
         * @throws java.lang.Exception
         */
        //public void alterRemoveField(String fieldName) throws Exception;

        
   /*
   * Following methods do not have simple standard/Ansi SQL implementation
   * To make those happend without using non standard SQL, iw will requitre create a new
   * table and copying over all the data...
   * TODO: implement those later
   */
        /**
         * Renames an existing table
         * @throws java.lang.Exception
         */
        //public void renameTable(String oldName, String newName) throws Exception;

        
        /**
         * Rename an existing field in the table.
         * @throws java.lang.Exception
         */
        //public void alterRenameField(String oldName, String newName) throws Exception;

        /**
         * Change the field type to something else, for example:
         * A limited type of conversion are available, since it makes no sense to for example convert an INT field into a Date etc...
         * Use the ALTER_** types provided in JOTDBUpdater as alteration type: ie: ALTER_TYPE_TINYINT_TO_SMALLINT
         * Call in succession if neded, ie: to go from tinyInt to int: 
         * ALTER_TYPE_TINYINT_TO_SMALLINT ALTER_TYPE_SMALLINT_TO_INT
         * @throws java.lang.Exception
         */
        //public void alterFieldType(String fieldName, int alterType) throws Exception;
        
        /**
         * Change the size of a varchar(text) field
         * NOTE THAT IF THE NEW SIZE IS SMALLER, SOME OF THE EXISTING STRINGS MIGHT GET TRONCATED to the new smaller size.
         * @throws java.lang.Exception
         */
        //public void alterVarcharLength(String fieldName, int newLength) throws Exception;
}
