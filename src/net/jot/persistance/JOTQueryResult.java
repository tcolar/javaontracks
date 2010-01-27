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

	/**
	 * Client side "distinct"
	 * Remove from the result list, the duplicate entries(values) for the given column
	 * (Keep the first one as sorted by prder by).
	 */
	public void filterDistinct(String colName)
	{
		Vector newElems=new Vector();
		for(int i = 0 ;i!=elems.size(); i++)
		{
			JOTModel model = (JOTModel)elems.get(i);
			Object val = model.getFieldValue(colName);
			boolean duplicate = false;
			for(int j = 0; j != newElems.size() ; j++)
			{
				JOTModel model2 = (JOTModel)newElems.get(j);
				Object val2 = model2.getFieldValue(colName);
				if(val.equals(val2))
				{
					duplicate=true;
					break;
				}
			}
			if( ! duplicate)
				newElems.add(model);
		}
		elems = newElems;
	}

}
