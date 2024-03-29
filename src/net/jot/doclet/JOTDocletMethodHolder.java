/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ProgramElementDoc;

/**
 *
 * @author thibautc
 */
public class JOTDocletMethodHolder implements JOTDocletHolder, Comparable
{
    /** if the method comes from a superclass, which one*/
    private ClassDoc inSuperClass=null;
    private MethodDoc doc;
    /** if the method is overriding one in a superclass, which one*/
    private ClassDoc overridenIn;
    // where the method is specified (ex: abstract method/interface)
    private ClassDoc specifiedIn;

    public JOTDocletMethodHolder(MethodDoc doc, ClassDoc inSuperClass)
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
        JOTDocletMethodHolder m=(JOTDocletMethodHolder)m2;
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
}
