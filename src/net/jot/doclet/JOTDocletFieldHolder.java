/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.ProgramElementDoc;

/**
 *
 * @author thibautc
 */
public class JOTDocletFieldHolder implements JOTDocletHolder, Comparable{
    /** if the field comes from a superclass, which one*/
    private ClassDoc inSuperClass=null;
    private FieldDoc doc;
    /** if the field is overriding one in a superclass, which one*/
    private ClassDoc overridenIn;
    // where the field is specified (interface)
    private ClassDoc specifiedIn;

    public JOTDocletFieldHolder(FieldDoc doc, ClassDoc inSuperClass)
    {
        this.doc=doc;
        this.inSuperClass=inSuperClass;
    }

    public ProgramElementDoc getDoc()
    {
        return doc;
    }
    public boolean isLocal()
    {
        return inSuperClass==null;
    }
    public boolean isOverride()
    {
        return overridenIn!=null;
    }
    public boolean isSpecified()
    {
        return specifiedIn!=null;
    }
    public ClassDoc getSuperClass()
    {
        return inSuperClass;
    }

    public String name()
    {
        return doc.name();
    }

    public int compareTo(Object m2)
    {
        JOTDocletFieldHolder m=(JOTDocletFieldHolder)m2;
        return doc.compareTo(m.getDoc());
    }

    public ClassDoc getOverridenIn() {
        return overridenIn;
    }

    public ClassDoc getSpecifiedIn() {
        return specifiedIn;
    }

    public void setOverridenIn(ClassDoc doc) {
        overridenIn=doc;
    }

    public void setSpecifiedIn(ClassDoc doc) {
        specifiedIn=doc;
    }
    public void setSuperClass(ClassDoc doc)
    {
        inSuperClass=doc;
    }
}
