/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.test.db;

import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 * Internal JOT class for self-test
 * Test user for testing of JOTFS DB implementation
 * @author thibautc
 *
 */
public class TestUser extends JOTModel
{
    // NOTE: must be protected or public

    public String firstName;
    public String lastName;
    public int age;
    //private String fieldToIgnore=null;

    public String defineStorage()
    {
        return DEFAULT_STORAGE;
    }

    // Required (A model version MUST be defined)
    protected int defineVersion()
    {
        return 1;
    }

    // Required, but can be empty
    public void customize(JOTModelMapping mapping)
    {
        // All the following definitions are optional (example)

        // The name of the table in the database, if different than classname.toLowerCase()
        //mapping.defineTableName("JOT_TMP_TEST_USER");
        // Set the Primary key FB field name (if not 'dataId')
        //mapping.definePrimaryKey("dataid");

        // If you have defined fields in this class that you want to ignore (Not saved in the database table)
        // Note, you should call this very first, preferably.
        //String[] ignore={"fieldToIgnore"};
        //mapping.defineFieldsToIgnore(ignore);

        // For variable-length fields(ie: varchar), you SHOULD define the length
        mapping.defineFieldSize("firstName", 57);
        mapping.defineFieldSize("lastName", 60);

        //Optional definitions examples

        // Field(column) name in DB, if different than fieldname.tolowercase()
        mapping.defineFieldDBName("firstName", "firstname");

        // Define validation values
        mapping.defineFieldMaxlength("firstName", 20);
        mapping.defineFieldMinlength("firstName", 0);
        mapping.defineFieldMinValue("age", 0);
        mapping.defineFieldMaxValue("age", 130);

    }

    // Required (but can be empty)
    public void upgradeTable(int fromVersion)
    {
    }

//	 for testing purpose we want to reset the table before each run
    public void resetTable() throws Exception
    {
        deleteTable();
        createTableIfNecessary(getMapping());
    }

    public String toString()
    {
        return "Age: " + age + " ID:" + id + " first:" + firstName + " last:" + lastName;
    }

 
}
