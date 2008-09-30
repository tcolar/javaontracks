/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.test;

import java.util.Vector;

import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLOrderBy;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 * Internal JOT class for self-test
 * This is the main selftest for JOT
 * It is used to test that JOT itself is working properly
 * @author thibautc
 *
 */
public class ApplicationTest implements JOTTestable
{
    /**
     * will be called by ant test task
     * @throws Throwable
     */
    public void jotTest() throws Throwable
    {
        JOTTester.tag("Starting JDBC Test");
        testModel();
    }

    private static void testModel() throws Throwable
    {
        // create 3 test users and saves them
        TestUser user = new TestUser();

        // we should empty the table first and check it's empty after

        user.resetTable();

        JOTTester.checkIf("Testing that flushing the table worked", JOTQueryManager.findOne(TestUser.class, null) == null);

        user.firstName = "John";
        user.lastName = "Doe";
        user.age = 25;
        user.save();

        user = new TestUser();
        user.firstName = "Jane";
        user.lastName = "Doe";
        user.age = 33;
        user.save();

        user = new TestUser();
        user.firstName = "Billy";
        user.lastName = "Bob";
        user.age = 58;
        user.save();

        user = new TestUser();
        user.firstName = "Wayne";
        user.lastName = "Gretzky";
        user.age = 48;
        user.save();

        //JOTQueryManager.dumpToCSV(System.out, TestUser.class);

        Vector users = JOTQueryManager.find(user.getClass(), null);
        //System.out.println("users: "+users);
        JOTTester.checkIf("Checking that 4 users where created (using find)", users != null && users.size() == 4);

        TestUser readUser = (TestUser) JOTQueryManager.findByID(user.getClass(), (int) user.getId());
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
        params.setLimit(1);
        users = JOTQueryManager.find(TestUser.class, params);
        JOTTester.checkIf("Checking find() with mutliple conds, orderBy, [limit]", users.size() == 1, "" + users);
        JOTTester.checkIf("Checking find() with mutliple conds, [orderBy], limit", ((TestUser) users.get(0)).age == 48, ((TestUser) users.get(0)).toString());

        // find with 'like' conditions
        params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("firstName", JOTSQLCondition.IS_LIKE, "%J%"));
        users = JOTQueryManager.find(TestUser.class, params);
        JOTTester.checkIf("Checking find() with 'like' condition", users.size() == 2);

        // findBySQL - should fell when using jotdb
        JOTTester.checkThrowsException(Exception.class, "findUsingSQL",null);

    }

    public static void findUsingSQL() throws Throwable
    {
        JOTQueryManager.findUsingSQL(TestUser.class, "SELECT * FROM \"JOT_TEMP_TEST_USER\" ORDER BY dataid", null);
    }
    /**
     * For debugging JOTTester
     * @param args
     */
    public static void main(String[] args)
    {
        String[] args2 =
        {
            "classes", "-selfTest"
        };
        int breakpoint = 1;
        JOTTester.main(args2);
    }
}
