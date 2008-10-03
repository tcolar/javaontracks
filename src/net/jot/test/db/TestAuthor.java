/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.db;

import java.lang.reflect.TypeVariable;
import net.jot.persistance.HasMany;
import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 *
 * @author thibautc
 */
public class TestAuthor extends JOTModel
{
    // would need to pass an id so books.find can search only books by this author
    public HasMany books=new HasMany(TestBook.class);
    
    public TypeVariable[] getTypeParameters()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void customize(JOTModelMapping mapping)
    {
    }

}
