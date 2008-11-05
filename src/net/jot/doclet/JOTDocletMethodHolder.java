/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;

/**
 *
 * @author thibautc
 */
public class JOTDocletMethodHolder implements Comparable
{
    private ClassDoc inSuperClass=null;
    private MethodDoc doc;

    public JOTDocletMethodHolder(MethodDoc doc, ClassDoc inSuperClass)
    {
        this.doc=doc;
        this.inSuperClass=inSuperClass;
    }
    
    public MethodDoc getDoc()
    {
        return doc;
    }
    public boolean isLocal()
    {
        return inSuperClass==null;
    }
    public ClassDoc getSuperClass()
    {
        return inSuperClass;
    }

    public String getName()
    {
        return doc.name();
    }

    public int compareTo(Object m2)
    {
        JOTDocletMethodHolder m=(JOTDocletMethodHolder)m2;
        return doc.compareTo(m.getDoc());
    }
}
