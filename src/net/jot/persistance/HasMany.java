/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.persistance;

import java.util.Vector;
import net.jot.persistance.query.JOTQueryManager;

/**
 *
 * @author thibautc
 */
public class HasMany
{
    private static Class modelClass = null;

    private long parentId=-1;

    public HasMany(Class clazz)
    {
        modelClass = (Class) clazz;
        System.out.println("Created a hasmany for class: " + modelClass.getName());
    }

    public void init(JOTModel parent)
    {
        parentId=parent.getId();
    }

    /**
     * Returns all the records matching the parameters<br>
     * For example: Authors.books.find(null); will return all the books by that author
     * @param params - null means findAll
     * @return a Vector of JOTModel objects
     */
    /*public static Vector find(JOTTransaction transaction, JOTSQLQueryParams params) throws Exception
    {
        //TODO: where modelClass."parentname"_id == parentId;
        return JOTQueryManager.find(transaction, modelClass, params);
    }*/

    /*public static Vector find(JOTSQLQueryParams params) throws Exception
    {
        return find(null,params);
    }*/
    
    /**
     * Create a new "HasMany" type item, referencing the parent.
     * Note: won't be saved until you do save it (model.save())
     * @return
     */
    /*public static JOTModel createOne()
    {
        
    }*/
    /**
     * Deletes all the "HasMany" items refreing to this parent and matching the params.
     * For example: Authors.books.delete(null); will delete all the books by that author
     * @param transaction
     * @param params - null means deleteAll
     * @return
     */
    /*public static void delete(JOTTransaction transaction, JOTSQLQueryParams params)
    {
        JOTQueryBuilder.delete(modelClass);
    }
    public static void delete(JOTSQLQueryParams params)
    {
        delete(null, params);
    }*/
 }
