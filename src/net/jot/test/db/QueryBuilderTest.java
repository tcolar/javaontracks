/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.db;

import java.io.ByteArrayOutputStream;
import net.jot.persistance.JOTQueryResult;
import net.jot.persistance.builders.JOTQueryBuilder;
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
        JOTQueryResult v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").withParams(params).find();
        JOTTester.checkIf("Checking simple manual where query", v.size()==2);

        String[] params2={"Doe","Jane"};
        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").where("FIRST_NAME=?").withParams(params2).find();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==1);

        String[] params3={"Doe"};
        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").limit(1).withParams(params3).find();
        JOTTester.checkIf("Checking simple manual where query with limit", v.size()==1);

        String[] params5={"Doe"};
        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").orderBy("FIRST_NAME",false).withParams(params5).find();
        JOTTester.checkIf("Checking orderBy Desc with where", v.size()==2 && ((TestUser)v.get(0)).firstName.equals("John"));

        String[] params6={"Doe","Wayne"};
        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").orWhere("FIRST_NAME=?").withParams(params6).find();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==3);

        String[] params7={"Doe","33"};
        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").appendToSQL("AND AGE=?").withParams(params7).find();
        JOTTester.checkIf("Checking manual append", v.size()==1 && ((TestUser)v.get(0)).firstName.equals("Jane"));

        // not prepared statement style
        
        JOTTester.tag("testing plain SQL");
        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME='Doe'").find();
        JOTTester.checkIf("Checking simple manual where query", v.size()==2);

        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME='Doe'").where("FIRST_NAME='Jane'").find();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==1);

        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME='Doe'").limit(1).find();
        JOTTester.checkIf("Checking simple manual where query with limit", v.size()==1);

        v=JOTQueryBuilder.selectQuery(null, TestUser.class).orderBy("FIRST_NAME").find();
        JOTTester.checkIf("Checking order by", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Billy"));

        v=JOTQueryBuilder.selectQuery(null, TestUser.class).orderBy("FIRST_NAME",false).find();
        JOTTester.checkIf("Checking orderBy Desc", v.size()==4 && ((TestUser)v.get(0)).firstName.equals("Wayne"));

        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME=?").orderBy("FIRST_NAME",false).withParams(params5).find();
        JOTTester.checkIf("Checking orderBy Desc with where", v.size()==2 && ((TestUser)v.get(0)).firstName.equals("John"));

        v=JOTQueryBuilder.selectQuery(null, TestUser.class).where("LAST_NAME='Doe'").orWhere("FIRST_NAME='Wayne'").find();
        JOTTester.checkIf("Checking simple manual dual where query", v.size()==3);
        
        /**
         * New querybuilder tests
         */
        JOTTester.tag("QueryBuilder Tests");
        // reset the data
        DBTestData.populateUserTestData();
        TestUser user=(TestUser)JOTQueryBuilder.selectQuery(null, TestUser.class).findOne();
        TestUser user2=(TestUser)JOTQueryBuilder.findByID(null, TestUser.class, user.getId());
        JOTTester.checkIf("Checking findyID works",user2.getId()==user.getId());
                
        // testing helpers
        JOTQueryBuilder.deleteByID(null, TestUser.class, user.getId());
        JOTQueryBuilder.findByID(null, TestUser.class, user.getId());
        JOTTester.checkIf("Checking delete by ID worked", JOTQueryBuilder.findByID(null, TestUser.class, user.getId())==null);
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        JOTQueryBuilder.dumpToCSV(bos, TestUser.class);
        JOTTester.checkIf("Checking dumpToCSV", bos.toString().length()>0);
        bos=null;
        JOTQueryBuilder.findAll(null, TestUser.class);
        JOTTester.checkIf("Checking findAll worked", JOTQueryBuilder.findAll(null, TestUser.class).size()==3);
        
        // test other select functions
        user=(TestUser)JOTQueryBuilder.selectQuery(null, TestUser.class).findOne();
        JOTTester.checkIf("Checking findOne",JOTQueryBuilder.selectQuery(null, TestUser.class).where("id=-1").findOne()==null);
        JOTTester.checkIf("Checking findOne2",JOTQueryBuilder.selectQuery(null, TestUser.class).findOne()!=null);
        TestUser user3=(TestUser)JOTQueryBuilder.selectQuery(null, TestUser.class).where("id="+user.getId()).findOrCreateOne();
        JOTTester.checkIf("Checking findOrCreateOne",user.getId()==user3.getId(),user.getId()+" vs "+user3.getId());
        TestUser user4=(TestUser)JOTQueryBuilder.selectQuery(null, TestUser.class).where("id=-5").findOrCreateOne();
        JOTTester.checkIf("Checking findOrCreateOne2",user4.isNew());
    }

    
}
