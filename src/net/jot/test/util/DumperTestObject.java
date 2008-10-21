/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.test.util;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author thibautc
 */
class DumperTestObject
{
    public Object filIn=null;
    public Object filIn2=null;
    int field1 = 5;
    Integer field2 = new Integer(2);
    String field3 = "field3";
    float field4 = 2.25f;
    byte[] b =
    {
        1, 2, 3, 4
    };
    Hashtable hash = new Hashtable();
    public Vector v = new Vector();

    public DumperTestObject()
    {
        hash.put("blah", field3);
        hash.put("bloh", field2);
        hash.put("blut", b);

        v.add(field3);
        v.add(field2);
        v.add(b);
        v.add(hash);
    }

}
