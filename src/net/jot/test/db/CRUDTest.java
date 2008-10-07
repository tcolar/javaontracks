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
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 * Test standard ORM / DB model crud operations
 * @author thibautc
 *
 */
public class CRUDTest implements JOTTestable
{
    /**
     * 
     * Testing JOTMOdel / CRUD
     */
    public void jotTest() throws Throwable
    {
        //new TestAuthor().books.find(null);
        
        DBTestData.populateUserTestData();
        //JOTQueryManager.dumpToCSV(System.out, TestUser.class);
        TestUser user=DBTestData._users[3];

        Vector users = JOTQueryBuilder.selectQuery(TestUser.class).find().getAllResults();
        //System.out.println("users: "+users);
        JOTTester.checkIf("Checking that 4 users where created (using find)", users != null && users.size() == 4);

        TestUser readUser = (TestUser) JOTQueryBuilder.findByID(TestUser.class, (int) user.getId());
        JOTTester.checkIf("Checking reading user 4 back (find by ID)", readUser != null);
        JOTTester.checkIf("Checking user firstname", readUser.firstName.equals(user.firstName));
        JOTTester.checkIf("Checking user lastname", readUser.lastName.equals(user.lastName));
        JOTTester.checkIf("Checking user age", readUser.age == user.age);

        // test update
        long id = user.getId();
        user.firstName = "superwayne";
        user.save();
        readUser = (TestUser) (TestUser) JOTQueryBuilder.findByID(TestUser.class, (int) user.getId());
        JOTTester.checkIf("Checking update worked and ID didn't change", readUser.firstName.equals(user.firstName) && id == readUser.getId());
        //revert
        user.firstName = "Wayne";
        user.save();

        // findOne
        JOTSQLCondition cond=new JOTSQLCondition("age", JOTSQLCondition.IS_GREATER, new Integer(40));
        readUser = (TestUser) JOTQueryBuilder.selectQuery(TestUser.class).where(cond).findOne();
        JOTTester.checkIf("Checking findOne", readUser != null && readUser.age > 40, "" + readUser);

        // find with multiple conditions and descending, order by
        cond=new JOTSQLCondition("age", JOTSQLCondition.IS_GREATER, new Integer(28));
        JOTSQLCondition cond2=new JOTSQLCondition("age", JOTSQLCondition.IS_LOWER_OR_EQUAL, new Integer(48));
        users = JOTQueryBuilder.selectQuery(TestUser.class).where(cond).where(cond2).orderBy("age").find().getAllResults();
        JOTTester.checkIf("Checking find() with mutliple conds, orderBy", users.size() == 2 && ((TestUser) users.get(0)).age == 33, "" + users);

        // find with conditions and limits
        cond=new JOTSQLCondition("age", JOTSQLCondition.IS_GREATER, new Integer(28));
        cond2=new JOTSQLCondition("age", JOTSQLCondition.IS_LOWER_OR_EQUAL, new Integer(48));
        users = JOTQueryBuilder.selectQuery(TestUser.class).where(cond).where(cond2).orderBy("age",false).limit(1).find().getAllResults();
        JOTTester.checkIf("Checking find() with mutliple conds, orderBy, [limit]", users.size() == 1, "" + users);
        JOTTester.checkIf("Checking find() with mutliple conds, [orderBy], limit", ((TestUser) users.get(0)).age == 48, ((TestUser) users.get(0)).toString());

        // find with 'like' conditions
        cond=new JOTSQLCondition("FIRST_NAME", JOTSQLCondition.IS_LIKE, "%J%");
        users = JOTQueryBuilder.selectQuery(TestUser.class).where(cond).find().getAllResults();
        JOTTester.checkIf("Checking find() with 'like' condition", users.size() == 2);

        user.delete();
        users = JOTQueryBuilder.selectQuery(TestUser.class).find().getAllResults();
        JOTTester.checkIf("checking delete worked",users.size()==3);
        users = JOTQueryBuilder.selectQuery(TestUser.class).find().getAllResults();
        JOTTester.checkIf("checking delete worked 2",users.size()==0);
        
        //TODO: check custom field names(mapping), constraints etc....
    }

}
