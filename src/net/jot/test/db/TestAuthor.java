/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.db;

import java.lang.reflect.TypeVariable;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 *
 * @author thibautc
 */
public class TestAuthor extends JOTModel
{
    HasMany books=new HasMany(TestBook.class);
    
    public TypeVariable[] getTypeParameters()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void customize(JOTModelMapping mapping)
    {
    }

}
