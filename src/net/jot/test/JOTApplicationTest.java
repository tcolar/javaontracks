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

import net.jot.JOTInitializer;
import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLOrderBy;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.prefs.JOTPreferenceInterface;
import net.jot.prefs.JOTPreferences;
import net.jot.testing.JOTTester;

/**
 * Internal JOT class for self-test
 * This is the main selftest for JOT
 * It is used to test that JOT itself is working properly
 * @author thibautc
 *
 */
public class JOTApplicationTest
{

	private static final boolean testJdbc=true;
	private static final boolean testJotDB=true;

	public static void main(String[] args)
	{
		// main method is mainly here to be able to run this in debug mode.
		try
		{
			jotTest();
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * will be called by ant test task
	 * @throws Throwable
	 */
	public static void jotTest() throws Throwable
	{
		try
		{
			// we need this to have the Prefs, Logger etc.. ready.
			JOTInitializer.init();

			// testing the prefs
			JOTPreferenceInterface prefs=JOTPreferences.getInstance();
			JOTTester.checkIf("Checking preferences initialized properly: ",prefs!=null);
			String pref=prefs.getString("jot.logger.levels");
			JOTTester.checkIf("Reading a pref (jot.logger.levels): ",pref!=null);

			// DB tests
			if(testJdbc)
			{
				JOTTester.tag("Starting JDBC Test");
				testJdbc();
			}

			if(testJotDB)
			{
				JOTTester.tag("Starting JOTDB Test");
				testJotdb();
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			//cleanup resources
			JOTInitializer.destroy();
		}
	}

	private static void testJotdb() throws Throwable
	{
		// create 3 test users and saves them
		FSUser user=new FSUser();

		// we should empty the table first and check it's empty after

		user.resetTable();

		JOTTester.checkIf("Testing that flushing the table worked", JOTQueryManager.findOne(FSUser.class, null)==null);

		user.firstName="John";
		user.lastName="Doe";
		user.age=25;
		user.save();

		user=new FSUser();
		user.firstName="Jane";
		user.lastName="Doe";
		user.age=33;
		user.save();

		user=new FSUser();
		user.firstName="Billy";
		user.lastName="Bob";
		user.age=58;
		user.save();

		user=new FSUser();
		user.firstName="Wayne";
		user.lastName="Gretzky";
		user.age=48;
		user.save();

		Vector users=JOTQueryManager.find(user.getClass(), null);
		//System.out.println("users: "+users);
		JOTTester.checkIf("Checking that 4 users where created (using find)", users!=null && users.size()==4);

		FSUser readUser=(FSUser)JOTQueryManager.findByID(user.getClass(), (int)user.getId());		
		JOTTester.checkIf("Checking reading user 4 back (find by ID)", readUser!=null);
		JOTTester.checkIf("Checking user firstname", readUser.firstName.equals(user.firstName));
		JOTTester.checkIf("Checking user lastname", readUser.lastName.equals(user.lastName));
		JOTTester.checkIf("Checking user age", readUser.age == user.age);

		// test update
		long id=user.getId();
		user.firstName="superwayne";
		user.save();
		readUser=(FSUser)JOTQueryManager.findByID(user.getClass(), (int)user.getId());		
		JOTTester.checkIf("Checking update worked and ID didn't change", readUser.firstName.equals(user.firstName) && id==readUser.getId());

		// findOne
		JOTSQLQueryParams params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_GREATER,new Integer(40)));
		readUser=(FSUser)JOTQueryManager.findOne(user.getClass(), params);
		JOTTester.checkIf("Checking findOne", readUser!=null && readUser.age>40, ""+readUser);

		// find with multiple conditions and descending, order by
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_GREATER,new Integer(28)));
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_LOWER_OR_EQUAL,new Integer(48)));
		params.addOrderBy(new JOTSQLOrderBy("age",JOTSQLOrderBy.ASCENDING));
		users=JOTQueryManager.find(FSUser.class, params);
		JOTTester.checkIf("Checking find() with mutliple conds, orderBy", users.size()==2 && ((FSUser)users.get(0)).age==33, ""+users);

		// find with conditions and limits
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_GREATER,new Integer(28)));
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_LOWER_OR_EQUAL,new Integer(48)));
		params.addOrderBy(new JOTSQLOrderBy("age",JOTSQLOrderBy.DESCENDING));
		params.setLimit(1);
		users=JOTQueryManager.find(FSUser.class, params);
		JOTTester.checkIf("Checking find() with mutliple conds, orderBy, [limit]", users.size()==1 , ""+users);
		JOTTester.checkIf("Checking find() with mutliple conds, [orderBy], limit", ((FSUser)users.get(0)).age==48, ((FSUser)users.get(0)).toString());

		// find with 'like' conditions
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("firstName",JOTSQLCondition.IS_LIKE,"%J%"));
		users=JOTQueryManager.find(FSUser.class, params);
		JOTTester.checkIf("Checking find() with 'like' condition", users.size()==2);

	}

	private static void testJdbc() throws Throwable
	{
//		create 3 test users and saves them
		DBUser user=new DBUser();

		// we should empty the table first and check it's empty after
		user.resetTable();
		JOTTester.checkIf("Testing that flushing the table worked", JOTQueryManager.findOne(DBUser.class, null)==null);

		user.firstName="John";
		user.lastName="Doe";
		user.age=25;
		user.save();

		user=new DBUser();
		user.firstName="Jane";
		user.lastName="Doe";
		user.age=33;
		user.save();

		user=new DBUser();
		user.firstName="Billy";
		user.lastName="Bob";
		user.age=58;
		user.save();

		user=new DBUser();
		user.firstName="Wayne";
		user.lastName="Gretzky";
		user.age=48;
		user.save();

		Vector users=JOTQueryManager.find(user.getClass(), null);
		JOTTester.checkIf("Checking that 4 users where created (using find)", users!=null && users.size()==4);

		DBUser readUser=(DBUser)JOTQueryManager.findByID(user.getClass(), (int)user.getId());		
		JOTTester.checkIf("Checking reading user 4 back (find by ID)", readUser!=null);
		JOTTester.checkIf("Checking user firstname", readUser.firstName.equals(user.firstName));
		JOTTester.checkIf("Checking user lastname", readUser.lastName.equals(user.lastName));
		JOTTester.checkIf("Checking user age", readUser.age == user.age);

		// test update
		long id=user.getId();
		user.age=52;
		user.save();
		readUser=(DBUser)JOTQueryManager.findByID(user.getClass(), (int)user.getId());		
		JOTTester.checkIf("Checking update worked and ID didn't change", readUser.age==user.age && id==readUser.getId());

		// findOne
		JOTSQLQueryParams params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_GREATER,new Integer(40)));
		readUser=(DBUser)JOTQueryManager.findOne(user.getClass(), params);
		JOTTester.checkIf("Checking findOne", readUser!=null && readUser.age>40);


		// findBySQL
		users=JOTQueryManager.findUsingSQL(user.getClass(), "SELECT * FROM \"JOT_TEMP_TEST_USER\" ORDER BY dataid", null);
		JOTTester.checkIf("Checking that findBySQL works", users.size()==4, ""+users);

		// find with multiple conditions and descending
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_GREATER,new Integer(22)));
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_LOWER_OR_EQUAL,new Integer(38)));
		params.addOrderBy(new JOTSQLOrderBy("dataid",JOTSQLOrderBy.ASCENDING));
		users=JOTQueryManager.find(DBUser.class, params);
		JOTTester.checkIf("Checking find() with mutliple conds, orderBy", users.size()==2 && ((DBUser)users.get(0)).age==25, ""+users);

		// find with conditions and limits & orderBy
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_GREATER,new Integer(22)));
		params.addCondition(new JOTSQLCondition("age",JOTSQLCondition.IS_LOWER_OR_EQUAL,new Integer(38)));
		params.addOrderBy(new JOTSQLOrderBy("age",JOTSQLOrderBy.DESCENDING));
		params.setLimit(1);
		users=JOTQueryManager.find(DBUser.class, params);
		JOTTester.checkIf("Checking find() with mutliple conds, limit, orderBy", users.size()==1 && ((DBUser)users.get(0)).age==33);

		// find with 'like' conditions
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("firstname",JOTSQLCondition.IS_LIKE,"%J%"));
		users=JOTQueryManager.find(DBUser.class, params);
		JOTTester.checkIf("Checking find() with 'like' condition", users.size()==2);

		// OrderBy
		params=new JOTSQLQueryParams();
		params.addCondition(new JOTSQLCondition("firstname",JOTSQLCondition.IS_LIKE,"%J%"));
		users=JOTQueryManager.find(DBUser.class, params);
		JOTTester.checkIf("Checking orderBy", users.size()==2);
                
 	}
}
