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
        JOTTester.tag("DB Test Data");
        DBTestData.populateUserTestData();
        
        //JOTQueryManager.dumpToCSV(System.out, TestUser.class);

        JOTTester.tag("testing prepared Statements");
        String[] params={"Doe"};
        Vector v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").withParams(params).execute();
        JOTTester.checkIf("Checking simple manual where query", v.size()==2);

        String[] params2={"Doe","Jane"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").where("FIRST_NAME=?").withParams(params2).execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==1);

        String[] params3={"Doe"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").limit(1).withParams(params3).execute();
        JOTTester.checkIf("Checking simple manual where query with limit", v.size()==1);

        String[] params5={"Doe"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").orderBy("FIRST_NAME",false).withParams(params5).execute();
        JOTTester.checkIf("Checking orderBy Desc with where", v.size()==2 && ((TestUser)v.get(0)).firstName.equals("John"));

        String[] params6={"Doe","Wayne"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").orWhere("FIRST_NAME=?").withParams(params6).execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==3);

        String[] params7={"Doe","33"};
        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").append("AND AGE=?").withParams(params7).execute();
        JOTTester.checkIf("Checking manual append", v.size()==1 && ((TestUser)v.get(0)).firstName.equals("Jane"));

        // not prepared statement style
        
        JOTTester.tag("testing plain SQL");
        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME='Doe'").execute();
        JOTTester.checkIf("Checking simple manual where query", v.size()==2);

        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME='Doe'").where("FIRST_NAME='Jane'").execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==1);

        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME='Doe'").limit(1).execute();
        JOTTester.checkIf("Checking simple manual where query with limit", v.size()==1);

        v=JOTQueryBuilder.findAll(TestUser.class).orderBy("FIRST_NAME").execute();
        JOTTester.checkIf("Checking order by", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Billy"));

        v=JOTQueryBuilder.findAll(TestUser.class).orderBy("FIRST_NAME",false).execute();
        JOTTester.checkIf("Checking orderBy Desc", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Wayne"));

        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME=?").orderBy("FIRST_NAME",false).withParams(params5).execute();
        JOTTester.checkIf("Checking orderBy Desc with where", v.size()==2 && ((TestUser)v.get(0)).firstName.equals("John"));

        v=JOTQueryBuilder.findAll(TestUser.class).where("LAST_NAME='Doe'").orWhere("FIRST_NAME='Wayne'").execute();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==3);
    }

}
