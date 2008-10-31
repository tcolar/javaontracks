/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.javadoc.AnnotationTypeDocImpl;
import com.sun.tools.javadoc.PackageDocImpl;
import java.util.Arrays;
import java.util.Vector;
import net.jot.web.views.JOTLightweightView;

/**
 * View object passed from the doclet to the Template parser.
 * Contains data and methods used by the parser to fill in tne pacge variables.
 * @author thibautc
 */
public class JOTDocletNavView extends JOTLightweightView
{

    /**
     * We will leverage some of the standard javadoc features
     * In particualr comments parsing/formatting.
     */
    private HtmlDocletWriter docWriter;

    public JOTDocletNavView(HtmlDocletWriter docWriter)
    {
        this.docWriter = docWriter;
    }
    public final static String PACKAGES = "packages";

    public String getItemLink(PackageDocImpl pack)
    {
        return JOTDoclet.getPkgFolder(pack) + "package-summary.html";
    }

    public String getItemLink(AnnotationTypeDocImpl pack)
    {
        return "";
    }

    public String getItemLink(ClassDoc pack)
    {
        return "";
    }

    public ClassDoc[] getSortedClasses()
    {
        PackageDoc pack = (PackageDoc) getVariables().get("curitem");
        return getSortedClasses(pack);
    }

    public ClassDoc[] getSortedClasses(PackageDoc pack)
    {
        ClassDoc[] clazzes = pack.allClasses();
        Arrays.sort(clazzes);
        return clazzes;
    }

    public String getPathToRoot()
    {
        String path = "";
        PackageDoc pack = (PackageDoc) getVariables().get("curitem");
        if (pack != null)
        {
            for (int i = 0; i != pack.name().split("\\.").length; i++)
            {
                path += "../";
            }
        }
        return path;
    }

    public String getTypeImage(ClassDoc clazz)
    {
        if (clazz.isOrdinaryClass())
        {
            if (!clazz.isAbstract())
            {
                return getPathToRoot() + "img/class.png";
            } else
            {
                return getPathToRoot() + "img/abstract.png";
            }
        } else if (clazz.isInterface())
        {
            return getPathToRoot() + "img/interface.png";
        } else if (clazz.isEnum())
        {
            return getPathToRoot() + "img/enum.png";
        } else if (clazz.isError() || clazz.isException())
        {
            return getPathToRoot() + "img/error.png";
        } else if (clazz.isAnnotationType())
        {
            return getPathToRoot() + "img/annotation.png";
        }
        //default;
        return getPathToRoot() + "img/class.png";
    }

    public String getTreeOffset(Doc pack)
    {
        String result = "";
        int nb = pack.name().split("\\.").length;
        for (int i = 1; i < nb; i++)
        {
            result += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        return result;
    }

    public String getShortDescription()
    {
        Doc pack = (Doc) getVariables().get("curitem");
        return getShortDescription(pack);
    }

    public String getShortDescription(Doc pack)
    {
        String text = docWriter.commentTagsToString(null, pack, pack.firstSentenceTags(), true);
        return text;
    }

    public Vector getSeeTags()
    {
        // adjust the path, so the links get build correctly
        docWriter.relativePath=getPathToRoot();
        Doc pack = (Doc) getVariables().get("curitem");
        Tag[] tags = pack.tags("see");
        Vector results = new Vector();
        for (int i = 0; i != tags.length; i++)
        {
            SeeTag tag = (SeeTag) tags[i];
            
            String link = docWriter.seeTagToString(tag);
  
            results.add(link);
        }
        return results;
    }

    public String getJDOCTags(String tagName)
    {
        Doc pack = (Doc) getVariables().get("curitem");
        Tag[] tags = pack.tags(tagName);
        String results = "";
        for (int i = 0; i != tags.length; i++)
        {
            results += tags[i].text() + "<br/>";
        }
        return results;
    }

    public String getFullDescription()
    {
        Doc pack = (Doc) getVariables().get("curitem");
        return getFullDescription(pack);
    }

    public String getFullDescription(Doc pack)
    {
        String text = docWriter.commentTagsToString(null, pack, pack.inlineTags(), false);

        if (text == null)
        {
            text = "";
        }
        if (!containsBreaks(text))
        {
            /**
             * If there is no html tag, it's probably a raw text comments that would gain
             * fromm converting line feeds into <br/>
             */
            text = text.replaceAll("\n", "<br/>");
        }
        return text;
    }

    public String getStrippedShortDescription(Doc pack)
    {
        // short description without any html tags whch messthings up when bot closed on first line.
        return getShortDescription(pack).replaceAll("\\<.*>", "");
    }

    private boolean containsBreaks(String txt)
    {
        // kinda lame
        return txt.indexOf("<br/>") != -1 || txt.indexOf("<BR/>") != -1 || txt.indexOf("<p>") != -1 || txt.indexOf("<P>") != -1;
    }
}
