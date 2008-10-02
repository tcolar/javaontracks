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
        user=(TestUser)JOTQueryManager.findByID(TestUser.class,1);
        JOTTester.checkIf("rollback before", user.firstName.equals("xyz"));
        tx.rollBack();
        user=(TestUser)JOTQueryManager.findByID(TestUser.class,1);
        JOTTester.checkIf("rollback after", user.firstName.equals("toto"));
        
        /* multithread test - test trnsaction was atomic
         transaction (some add 1 remove 1, some 3 remove 3)x 10 threads ?*/

        //multithread with rollbacks
    }

}
