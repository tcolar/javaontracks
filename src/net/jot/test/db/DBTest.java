/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.test.db;

import java.util.Vector;

import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLOrderBy;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.JOTStatementFlags;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 * Test standard ORM / DB model crud operations
 * @author thibautc
 *
 */
public class DBTest implements JOTTestable
{
    public static TestUser[] _users=new TestUser[4];

    /**
     * DO NOT CHANGE this TEST DATA or TESTS WILL FAIL !!!
     * Used by most DB tests
     * @throws java.lang.Exception
     */
    public static void populateUserTestData() throws Exception
    {
        // create 3 test users and saves them
        TestUser user = new TestUser();
        // we empty the table first
        user.resetTable();

        user.firstName = "John";
        user.lastName = "Doe";
        user.age = 25;
        user.save();
        _users[0]=user;

        user = new TestUser();
        user.firstName = "Jane";
        user.lastName = "Doe";
        user.age = 33;
        user.save();
        _users[1]=user;

        user = new TestUser();
        user.firstName = "Billy";
        user.lastName = "Bob";
        user.age = 58;
        user.save();
        _users[2]=user;

        user = new TestUser();
        user.firstName = "Wayne";
        user.lastName = "Gretzky";
        user.age = 48;
        user.save();
        _users[3]=user;

    }
    
    /**
     * 
     * Testing JOTMOdel / CRUD
     */
    public void jotTest() throws Throwable
    {
        populateUserTestData();
        //JOTQueryManager.dumpToCSV(System.out, TestUser.class);

        TestUser user=_users[3];

        Vector users = JOTQueryManager.find(TestUser.class, null);
        //System.out.println("users: "+users);
        JOTTester.checkIf("Checking that 4 users where created (using find)", users != null && users.size() == 4);

        TestUser readUser = (TestUser) JOTQueryManager.findByID(TestUser.class, (int) user.getId());
        JOTTester.checkIf("Checking reading user 4 back (find by ID)", readUser != null);
        JOTTester.checkIf("Checking user firstname", readUser.firstName.equals(user.firstName));
        JOTTester.checkIf("Checking user lastname", readUser.lastName.equals(user.lastName));
        JOTTester.checkIf("Checking user age", readUser.age == user.age);

        // test update
        long id = user.getId();
        user.firstName = "superwayne";
        user.save();
        readUser = (TestUser) JOTQueryManager.findByID(user.getClass(), (int) user.getId());
        JOTTester.checkIf("Checking update worked and ID didn't change", readUser.firstName.equals(user.firstName) && id == readUser.getId());
        //revert
        user.firstName = "Wayne";
        user.save();

        // findOne
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("age", JOTSQLCondition.IS_GREATER, new Integer(40)));
        readUser = (TestUser) JOTQueryManager.findOne(user.getClass(), params);
        JOTTester.checkIf("Checking findOne", readUser != null && readUser.age > 40, "" + readUser);

        // find with multiple conditions and descending, order by
        params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("age", JOTSQLCondition.IS_GREATER, new Integer(28)));
        params.addCondition(new JOTSQLCondition("age", JOTSQLCondition.IS_LOWER_OR_EQUAL, new Integer(48)));
        params.addOrderBy(new JOTSQLOrderBy("age", JOTSQLOrderBy.ASCENDING));
        users = JOTQueryManager.find(TestUser.class, params);
        JOTTester.checkIf("Checking find() with mutliple conds, orderBy", users.size() == 2 && ((TestUser) users.get(0)).age == 33, "" + users);

        // find with conditions and limits
        params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("age", JOTSQLCondition.IS_GREATER, new Integer(28)));
        params.addCondition(new JOTSQLCondition("age", JOTSQLCondition.IS_LOWER_OR_EQUAL, new Integer(48)));
        params.addOrderBy(new JOTSQLOrderBy("age", JOTSQLOrderBy.DESCENDING));
        users = JOTQueryManager.find(TestUser.class, params);
        
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // TODO: make setLimit work again !
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        //JOTTester.checkIf("Checking find() with mutliple conds, orderBy, [limit]", users.size() == 1, "" + users);
        JOTTester.checkIf("Checking find() with mutliple conds, [orderBy], limit", ((TestUser) users.get(0)).age == 48, ((TestUser) users.get(0)).toString());

        // find with 'like' conditions
        params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("FIRST_NAME", JOTSQLCondition.IS_LIKE, "%J%"));
        users = JOTQueryManager.find(TestUser.class, params);
        JOTTester.checkIf("Checking find() with 'like' condition", users.size() == 2);

        Vector v=JOTQueryManager.findUsingSQL(TestUser.class, "SELECT * FROM test_user ORDER BY ID Desc", null);
        JOTTester.checkIf("checking plain SQL query",v.size()==4);
        JOTTester.checkIf("checking plain SQL query 2",((TestUser)v.get(0)).firstName.equals("Wayne"));
        
        //TODO: test delete
    }

}
