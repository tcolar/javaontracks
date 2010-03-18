/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.persistance;

import net.jot.db.JOTDBField;
import net.jot.logger.JOTLogger;
import net.jot.persistance.query.JOTQueryInterface;
import net.jot.persistance.query.JOTQueryManager;

/**
 * Extend this class to create your own DBUpdater.
 * If you have an existing JOTDB (Using JOTMOdel's) and you need to change some model/tables
 * definition (ie: add fields, rename columns etc ...) you should create a DBUpdater from it and imp,ement upgradeDb()
 * You must define your implementaion in db.properties:
 * Ex:  db.upgrader.class = com.mycomp.mydb.MyUpgrader
 * Then in the upgradeDb() you can handle the upgrade,
 * @author thibautc
 */
public abstract class JOTDBUpgrader
{
	// Field type alterations constants.

	public static final int ALTER_TYPE_TINYINT_TO_SMALLINT = 1;
	public static final int ALTER_TYPE_SMALLINT_TO_INT = 2;
	public static final int ALTER_TYPE_INT_TO_BIGINT = 3;
	public static final int ALTER_TYPE_BIGINT_TO_DECIMAL = 4;
	public static final int ALTER_TYPE_INT_TO_FLOATL = 5;
	public static final int ALTER_TYPE_INT_TO_DOUBLE = 6;
	public static final int ALTER_TYPE_DATE_TO_TIMESTAMP = 7;
	public static final int ALTER_TYPE_TIMESTAMP_TO_DATE = 8;
	// Provide "down" alterations ? (ie: BIGINT to TINYINT etc...), not much use and dangerous.

	protected JOTDBUpgrader()
	{
	}

	/**
	 * Implement this method to handle a db upgrade.
	 *
	 * You can use the other methods provided here to upgrade your tables(ie: addTableColumn etc...)
	 *
	 * Make sure to bump the version after each version upgrade : setVersion(x);
	 * Note: if the DB is brand new, the version passed will be "1".
	 *
	 * @param:version  current Version Of the Db (on file)
	 */
	public abstract void upgradeDb(String dbName, int version) throws Exception;

	/**
	 *  return the DBModel Version(code). a.k.a the version we should be at after the upgrade.
	 */
	public abstract int getLatestVersion();

	/**
	 * Add a new Field to a table
	 * The field must exists in the table model (JOTModel).
	 * @throws java.lang.Exception
	 */
	public void addTableColumn(JOTTransaction transaction, Class modelClass, String fieldName, Object defaultValue) throws Exception
	{
		// skip validation since it would fail anyhow.
		JOTModelMapping mapping = JOTQueryManager.getMapping(transaction, modelClass, false, true);
		JOTDBField field = (JOTDBField) mapping.getMappedFields().get(fieldName);
		if (field == null)
		{
			field = (JOTDBField) mapping.getFields().get(fieldName);
		}
		if (field == null)
		{
			throw new Exception("The field named :" + fieldName + " does not exists in the JOTModel: " + mapping.getTableName());
		}
		JOTLogger.log(JOTLogger.CAT_DB, JOTLogger.INFO_LEVEL, this, "Adding column:" + field.getFieldName() + " to Table: " + mapping.getTableName());
		JOTQueryInterface impl = JOTQueryManager.getImplementation(mapping.getQueryClassName());
		impl.alterAddField(null, mapping, field, defaultValue);
		// if we get here , it went ok, updating the table metadata
		JOTModelMapping.writeMetaFile(mapping);
	}
	/**
	 * Removed a field from a table
	 * THAT FIELD DATA WILL BE LOST.
	 * @throws java.lang.Exception
	 */
	/*public void removTableColumn(String tableName, String fieldName) throws Exception
	{
	//TODO
	}*/
	/*public void renameTable(String oldName, String newName) throws Exception
	{
	impl.renameTable(oldName, newName);
	}
	 */
	/*public void alterFieldName(String oldName, String newName) throws Exception
	{
	impl.alterRenameField(JOTModel model, oldName, newName);
	}*/
	/*public void alterFieldType(String fieldName, int alterType) throws Exception
	{
	impl.alterFieldType(JOTModel model, fieldName, alterType);
	}*/

	/*public void alterVarcharLength(String fieldName, int newLength) throws Exception
	{
	impl.alterVarcharLength(JOTModel model, fieldName, newLength);
	}*/
}
