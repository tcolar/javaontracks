/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.db.authentication;

import net.jot.logger.JOTLogger;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;
import net.jot.persistance.JOTSQLCondition;
import net.jot.persistance.JOTSQLQueryParams;
import net.jot.persistance.query.JOTQueryManager;

/**
 * Object representation of a User in DB and providing a basic authentication system. 
 * It provides is a basic authentication scheme, this is intended to be bare-bone and probably would be extended.
 * It provides this:
 * - A user, identified by a Login and Password
 * - The user has a dataProfile assigned to him
 * - A Profile is a set of "Permissions", for example "CAN_EDIT", "CAN_DELETE"
 * You will probably want to had custom fields such as fistname, address in this class subclass.
 * @author thibautc
 *
 */
public abstract class JOTAuthUser extends JOTModel
{

    public String dataLogin;
    // dataProfile refers to dataProfile.id
    public int dataProfile = -1;
    public String dataPassword = "";

    /**
     * If you override this in the subclass, make sure you still call this (super.customize())
     * Or copy the "defineFields" entries
     */
    public void customize(JOTModelMapping mapping)
    {
        mapping.defineFieldSize("dataLogin", 20);
        mapping.defineFieldSize("dataPassword", 20);
    }

    /**
     * Wether the dataLogin is available (not already in use)
     * implClass is your subclass of JOTAuthUser
     */
    public static boolean isNewUser(Class implClass, String login) throws Exception
    {
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("dataLogin", JOTSQLCondition.IS_EQUAL, login));
        return JOTQueryManager.findOne(implClass, params) == null;
    }

    /**
     * Use to check if a user with given login/password exists.
     * Return true if a user with the given Login and Password exists
     * implClass is your subclass of JOTAuthUser
     * @param Login
     * @param Password
     * @return
     * @throws Exception
     */
    public static boolean isUserValid(Class implClass, String login, String password) throws Exception
    {
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("dataLogin", JOTSQLCondition.IS_EQUAL, login));
        params.addCondition(new JOTSQLCondition("dataPassword", JOTSQLCondition.IS_EQUAL, password));
        return JOTQueryManager.findOne(implClass, params) != null;
    }

    public static JOTAuthUser getUserByLogin(Class implClass, String login) throws Exception
    {
        JOTSQLQueryParams params = new JOTSQLQueryParams();
        params.addCondition(new JOTSQLCondition("dataLogin", JOTSQLCondition.IS_EQUAL, login));
        return (JOTAuthUser) JOTQueryManager.findOne(implClass, params);
    }

    /**
     * checks wether a user has a given permission
     * @param permission
     * @return
     */
    public boolean hasPermission(String permission)
    {
        if (dataProfile != -1)
        {
            JOTSQLQueryParams params = new JOTSQLQueryParams();
            params.addCondition(new JOTSQLCondition("dataProfile", JOTSQLCondition.IS_EQUAL, "" + dataProfile));
            params.addCondition(new JOTSQLCondition("dataPermission", JOTSQLCondition.IS_EQUAL, permission));
            try
            {
                return JOTQueryManager.findOne(JOTAuthPermission.class, params) != null;
            } catch (Exception e)
            {
                JOTLogger.logException(JOTLogger.ERROR_LEVEL, this, "error looking for prmission", e);
            }
        }
        return false;
    }
}
