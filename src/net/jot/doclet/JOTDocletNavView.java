/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.tools.javadoc.AnnotationTypeDocImpl;
import com.sun.tools.javadoc.ClassDocImpl;
import com.sun.tools.javadoc.PackageDocImpl;
import java.util.Arrays;
import net.jot.web.views.JOTLightweightView;

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

    public String getTypeImage(AnnotationTypeDocImpl annot)
    {
        return "img/annotation.png";
    }

    public String getTypeImage(ClassDocImpl clazz)
    {
        if (clazz.isOrdinaryClass())
        {
            return "img/class.png";
        } else if (clazz.isInterface())
        {
            return "img/interface.png";
        } else if (clazz.isEnum())
        {
            return "img/enum.png";
        } else if (clazz.isError() || clazz.isException())
        {
            return "img/error.png";
        } else if (clazz.isAnnotationType())
        {
            return "img/annotation.png";
        }
        //default;
        return "img/class.png";
    }

    public String getTreeOffset(PackageDocImpl pack)
    {
        String result = "";
        int nb = pack.name().split("\\.").length;
        for (int i = 1; i < nb; i++)
        {
            result += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        return result;
    }

    public String getShortDescription(PackageDocImpl pack)
    {
        String text=getFullDescription(pack);
        if (text.indexOf(".") > 0)
        {
            text = text.substring(0, text.indexOf("."));
        }
        if(text.length()==0)
        {
            text="---- No Doc ----";
        }
        return text;
    }

    public String getFullDescription(PackageDocImpl pack)
    {
        String text = pack.getRawCommentText();
        if (containsHtml(text))
        {
            text = pack.commentText();
            if (text != null && text.indexOf(".") > 0)
            {
                text = text.substring(0, text.indexOf("."));
            }
        } else
        {
            /**
             * If there is no html tag, it's probably a raw text comments that would gain
             * fromm converting line feeds into <br/>
             */
            if (text != null)
            {
                text.replaceAll("\n", "<br/>");
            }
        }
        if (text == null)
        {
            text = "";
        }
        return text;
    }

    private boolean containsHtml(String txt)
    {
        // kinda lame
        return txt.indexOf("/>") != -1;
    }
}
