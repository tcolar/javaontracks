/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.db;

import java.util.Vector;
import net.jot.persistance.JOTQueryBuilder;
import net.jot.persistance.query.JOTQueryManager;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 * Test DB QueryBuilder
 * @author thibautc
 */
public class QueryBuilderTest implements JOTTestable
{

    public void jotTest() throws Throwable
    {
        // add some user data to the DB
        DBTest.populateUserTestData();
        JOTQueryManager.dumpToCSV(System.out, TestUser.class);

        JOTTester.tag("testing prepared Statements");
        String[] params={"Doe"};
        Vector v=JOTQueryBuilder.findAll(TestUser.class).where("lastname=?").withParams(params).execute();
        JOTTester.checkIf("Checking simple manual where query", v.size()==2);

        String[] params2={"Doe","Jane"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname=?").where("firstname=?").withParams(params2).execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==1);

        String[] params3={"Doe"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname=?").limit(1).withParams(params3).execute();
        JOTTester.checkIf("Checking simple manual where query with limit", v.size()==1);

        String[] params4={};
        v=JOTQueryBuilder.findAll(TestUser.class).orderBy("firstname",false).withParams(params4).execute();
        JOTTester.checkIf("Checking order by", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Billy"));

        String[] params5={"Doe"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname='?'").orderBy("firstname",false).withParams(params5).execute();
        JOTTester.checkIf("Checking orderBy Desc", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Wayne"));

        String[] params6={"Doe","Wayne"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname=?").orWhere("firstname=?").withParams(params6).execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==3);

        // not prepared statement style
        
        JOTTester.tag("testing plain SQL");
        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname='Doe'").execute();
        JOTTester.checkIf("Checking simple manual where query", v.size()==2);

        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname='Doe'").where("firstname='Jane'").execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==1);

        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname='Doe'").limit(1).execute();
        JOTTester.checkIf("Checking simple manual where query with limit", v.size()==1);

        v=JOTQueryBuilder.findAll(TestUser.class).orderBy("firstname",false).execute();
        JOTTester.checkIf("Checking order by", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Billy"));

        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname='Doe'").orderBy("firstname",false).execute();
        JOTTester.checkIf("Checking orderBy Desc", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Wayne"));

        v=JOTQueryBuilder.findAll(TestUser.class).where("lastname='Doe'").orWhere("firstname='Wayne'").execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==3);
    }

}
