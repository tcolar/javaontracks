/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.tools.javadoc.ClassDocImpl;
import com.sun.tools.javadoc.PackageDocImpl;
import java.util.Arrays;
import net.jot.web.view.JOTLightweightView;

/**
 *
 * @author thibautc
 */
public class JOTDocletNavView extends JOTLightweightView
{

    public final static String PACKAGES = "packages";

    public ClassDoc[] getSortedClasses(PackageDocImpl pack)
    {
        ClassDoc[] clazzes = pack.allClasses();
        Arrays.sort(clazzes);
        return clazzes;
    }
    
    public String getTypeImage(ClassDocImpl clazz)
    {
        if(clazz.isOrdinaryClass())
        {
            return "img/class.png";
        }
        else if(clazz.isInterface())
        {
            return "img/interface.png";
        }
        else if(clazz.isEnum())
        {
            return "img/enum.png";
        }
        else if(clazz.isError() || clazz.isException())
        {
            return "img/error.png";
        }
        else if(clazz.isAnnotationType())
        {
            return "img/annotation.png";
        }
        //default;
        return "img/class.png";
    }
}
