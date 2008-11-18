/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ProgramElementDoc;

/**
 *
 * @author thibautc
 */
public interface JOTDocletHolder {
   

    public boolean isLocal();
    public boolean isOverride();
    public boolean isSpecified();

    public ClassDoc getSuperClass();

    public String name();

    public int compareTo(Object m2);

    public ClassDoc getOverridenIn();

    public ClassDoc getSpecifiedIn();

    void setOverridenIn(ClassDoc doc);

    void setSpecifiedIn(ClassDoc doc);
    public ProgramElementDoc getDoc();
}
