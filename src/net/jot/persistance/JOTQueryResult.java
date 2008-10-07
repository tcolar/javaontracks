/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance;

import java.util.Vector;

/**
 *
 * @author thibautc
 */
public class JOTQueryResult
{
    private Vector elems=new Vector();
    
    public void add(JOTModel model)
    {
        elems.add(model);
    }
    /**
     * Return the first element of the results
     * or null if no elements where found.
     * @return
     */
    public JOTModel getFirstResult()
    {
        JOTModel result=null;
        if(elems.size()>0)
            return (JOTModel)elems.get(0);
        return result;
    }
    public Vector getAllResults()
    {
        return elems;
    }
    public boolean isEmpty()
    {
        return elems.isEmpty();
    }
    public int size()
    {
        return elems.size();
    }
    public JOTModel get(int i)
    {
        return (JOTModel)elems.get(i);
    }
}
