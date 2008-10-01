/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.test.db;

/**
 * Test data for db tests
 * @author thibautc
 */
public final class DBTestData
{
    public static TestUser[] _users=new TestUser[4];

    /**
     * DO NOT CHANGE this TEST DATA or TESTS WILL FAIL !!!
     * Used by most DB tests
     * @throws java.lang.Exception
     */
    public final static void populateUserTestData() throws Exception
    {
        // create 3 test users and saves them
        TestUser user = new TestUser();
        // we empty the table first
        user.resetTable();

        user.firstName = "John";
        user.lastName = "Doe";
        user.age = 25;
        user.save();
        _users[0] = user;

        user = new TestUser();
        user.firstName = "Jane";
        user.lastName = "Doe";
        user.age = 33;
        user.save();
        _users[1] = user;

        user = new TestUser();
        user.firstName = "Billy";
        user.lastName = "Bob";
        user.age = 58;
        user.save();
        _users[2] = user;

        user = new TestUser();
        user.firstName = "Wayne";
        user.lastName = "Gretzky";
        user.age = 48;
        user.save();
        _users[3] = user;

    }
}
