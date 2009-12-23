/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.test.db;

import net.jot.persistance.JOTTransaction;
import net.jot.persistance.builders.JOTQueryBuilder;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;

/**
 * Test DB transactions
 * @author thibautc
 */
public class TransactionTest implements JOTTestable
{

    public void jotTest() throws Throwable
    {
        DBTestData.populateUserTestData();
        TestUser user = (TestUser) JOTQueryBuilder.selectQuery(null, TestUser.class).orderBy("ID").findOne();
        // test normal commit
        JOTTransaction tx = new JOTTransaction("test");
        user.firstName = "toto";
        user.save(tx);
        tx.commit();
        user = (TestUser) JOTQueryBuilder.selectQuery(null, TestUser.class).orderBy("ID").findOne();
        JOTTester.checkIf("Transaction worked", user.firstName.equals("toto"));

        // test transaction with rollback
        tx = new JOTTransaction("test");
        user.firstName = "xyz";
        user.save(tx);
        user = (TestUser) JOTQueryBuilder.selectQuery(tx, TestUser.class).orderBy("ID").findOne();
        JOTTester.checkIf("rollback before", user.firstName.equals("xyz"));
        tx.rollBack();
        user = (TestUser) JOTQueryBuilder.selectQuery(tx, TestUser.class).orderBy("ID").findOne();
        JOTTester.checkIf("rollback after", user.firstName.equals("toto"));

        /* multithread test - test atomic transactions, some commit, some rolled back*/
        String saved = user.firstName;
        TransactionTestThread[] threads=new TransactionTestThread[5];
        for (int i = 0; i != 5; i++)
        {
            threads[i]=new TransactionTestThread(i % 2 == 1);
            threads[i].start();
        }
        for (int i = 0; i != 5; i++)
        {
            threads[i].join();
        }
        user = (TestUser) JOTQueryBuilder.selectQuery(tx, TestUser.class).orderBy("id").findOne();
        JOTTester.warnIf("7 concurrent transactions (might fail on some DB's)", user.firstName.equals(saved),""+user.firstName);
    }

    class TransactionTestThread extends Thread
    {

        private boolean rollBack;

        public TransactionTestThread(boolean rollBack)
        {
            this.rollBack = rollBack;
        }

        public void run()
        {
            try
            {
                // this changes firstname but then fix it back in a transaction
                JOTTransaction tx = new JOTTransaction("test");
                TestUser user = (TestUser) JOTQueryBuilder.selectQuery(tx, TestUser.class).orderBy("id").findOne();
                user.firstName = user.firstName + "x";
                user.save(tx);
                user = (TestUser) JOTQueryBuilder.selectQuery(tx, TestUser.class).orderBy("id").findOne();
                //System.out.println(user.firstName);
                user.firstName = user.firstName.substring(0, user.firstName.length() - 1);
                user.save(tx);
                if (!rollBack)
                {
                    tx.commit();
                } else
                {
                    tx.rollBack();
                }
            } catch (Exception e)
            {
                // should use it in test result ..
                e.printStackTrace();
            }
        }
    }
}
