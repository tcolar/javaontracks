/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import com.sun.tools.javadoc.PackageDocImpl;
import java.util.Arrays;
import java.util.List;
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
        return getPathToRoot()+JOTDoclet.getPkgFolder(pack) + "package-summary.html";
    }

    public String getItemLink(ClassDoc doc)
    {
        return getPathToRoot()+JOTDoclet.getPkgFolder(doc.containingPackage())+doc.name()+".html";
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

    public Vector getHierarchy()
    {
        Vector results=new Vector();
        ClassDoc doc = (ClassDoc) getVariables().get("curitem");        
        while(doc!=null)
        {
            results.add(0, doc);
            doc=doc.superclass();
        }
        return results;
    }

    public List getSubClasses()
    {
        ClassDoc doc = (ClassDoc) getVariables().get("curitem");
        ClassTree tree=(ClassTree) getVariables().get("classTree");
        if(tree.subs(doc, false).size()>25)
            return new Vector().subList(0, 0);
        return tree.subs(doc, false);
    }
    
    public List getAllSubClasses()
    {
        ClassDoc doc = (ClassDoc) getVariables().get("curitem");
        ClassTree tree=(ClassTree) getVariables().get("classTree");
        if(tree.allSubs(doc, false).size()>100)
            return new Vector().subList(0, 0);
        return tree.allSubs(doc, false);
    }

    public String getPathToRoot()
    {
        String path = "";
        Doc doc = (Doc) getVariables().get("curitem");
        if (doc != null)
        {
            String name=doc.name();
            if(doc instanceof ClassDoc)
            {
                name=((ClassDoc)doc).containingPackage().name();
            }
            for (int i = 0; i != name.split("\\.").length; i++)
            {
                path += "../";
            }
        }
        return path;
    }

    public ConstructorDoc[] getConstructors()
    {
        ClassDoc doc = (ClassDoc) getVariables().get("curitem");
        ConstructorDoc[] docs=doc.constructors(false);
        Arrays.sort(docs);
        return docs;
    }
    
    public String getParamString(ConstructorDoc doc)
    {
        String str="(";
        Parameter[] params=doc.parameters();
        for(int i=0;i!=params.length;i++)
        {
            if(str.length()>1)
                str+=", ";
            if(params[i].type().asClassDoc()!=null)
            {
                String link=getItemLink(params[i].type().asClassDoc());
                str+="<a class='regular' href='"+link+"'><font class='type'>"+params[i].type().simpleTypeName()+"</font></a>";
            }
            else
            {
                str+="<font class='type'>"+params[i].typeName()+"</font>";
            }
            str+=" "+params[i].name();
        }
        str+=")";
        return str;
    }

    /**
     * Vompares full desc. to 1line des to see wether more infos avail.
     * @return
     */
    public boolean hasMoreInfos()
    {
        Doc doc = (Doc) getVariables().get("curitem");
        return hasMoreInfos(doc);
    }
    public boolean hasMoreInfos(Doc doc)
    {
        return docWriter.commentTagsToString(null, doc, doc.inlineTags(), false).length() > docWriter.commentTagsToString(null, doc, doc.firstSentenceTags(), true).length();
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

    public String getTreeOffset(Doc doc)
    {
        String result = "";
        int nb = doc.name().split("\\.").length;
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

    public String getShortDescription(Doc doc)
    {
        String text = docWriter.commentTagsToString(null, doc, doc.firstSentenceTags(), true);
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
        Doc doc = (Doc) getVariables().get("curitem");
        return getFullDescription(doc);
    }

    public String getFullDescription(Doc doc)
    {
        String text = docWriter.commentTagsToString(null, doc, doc.inlineTags(), false);

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

    public String getStrippedShortDescription(Doc doc)
    {
        // short description without any html tags whch messthings up when bot closed on first line.
        return getShortDescription(doc).replaceAll("\\<.*>", "");
    }

    private boolean containsBreaks(String txt)
    {
        // kinda lame
        return txt.indexOf("<br/>") != -1 || txt.indexOf("<BR/>") != -1 || txt.indexOf("<p>") != -1 || txt.indexOf("<P>") != -1;
    }

}
