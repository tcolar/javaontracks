/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.db;

import net.jot.persistance.JOTTransaction;
import net.jot.persistance.query.JOTQueryManager;
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
        TestUser user=(TestUser)JOTQueryManager.findByID(TestUser.class,1);
        // test normal commit
        JOTTransaction tx=new JOTTransaction("test");
        user.firstName="toto";
        user.save(tx);
        tx.commit();
        user=(TestUser)JOTQueryManager.findByID(TestUser.class,1);
        JOTTester.checkIf("Transaction worked", user.firstName.equals("toto"));

        // test transaction with rollback
        tx=new JOTTransaction("test");
        user.firstName="xyz";
        user.save(tx);
        user=(TestUser)JOTQueryManager.findByID(tx,TestUser.class,1);
        JOTTester.checkIf("rollback before", user.firstName.equals("xyz"));
        tx.rollBack();
        user=(TestUser)JOTQueryManager.findByID(TestUser.class,1);
        JOTTester.checkIf("rollback after", user.firstName.equals("toto"));
        
        /* multithread test - test atomic transactions, some commit, some rolled back*/
        String saved=user.firstName;
        for(int i=0;i!=7;i++)
            new TransactionTestThread(i%2==1).start();
        user=(TestUser)JOTQueryManager.findByID(TestUser.class,1);
        JOTTester.warnIf("7 concurrent transactions (might fail on some DB's)", user.firstName.equals(saved));        
    }   

    class TransactionTestThread extends Thread
    {
        private boolean rollBack;

        public TransactionTestThread(boolean rollBack)
        {
            this.rollBack=rollBack;
        }
        public void run()
        {
            try
            {
            // this changes firstname but then fix it back in a transaction
            JOTTransaction tx=new JOTTransaction("test");
            TestUser user=(TestUser)JOTQueryManager.findByID(tx,TestUser.class,1);
            user.firstName=user.firstName+"x";
            user.save(tx);
            user=(TestUser)JOTQueryManager.findByID(tx,TestUser.class,1);
            //System.out.println(user.firstName);
            user.firstName=user.firstName.substring(0,user.firstName.length()-1);
            user.save(tx);
            if(!rollBack)
                tx.commit();
            else
                tx.rollBack();
            }
            catch(Exception e)
            {
                // should use it in test result ..
                e.printStackTrace();
            }
        }
        
    }
}
