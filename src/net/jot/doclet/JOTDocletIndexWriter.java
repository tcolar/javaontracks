/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.doclets.formats.html.LinkInfoImpl;
import com.sun.tools.doclets.internal.toolkit.util.IndexBuilder;
import java.util.Iterator;
import java.util.List;
import net.jot.utils.JOTHTMLUtilities;

/**
 * Write all methods,class,fields etc.. index
 * @author thibautc
 */
public class JOTDocletIndexWriter
{

    private IndexBuilder builder;
    private HtmlDocletWriter docWriter;
    private ConfigurationImpl configuration;

    /**
     * create the writer
     * @param configuration
     * @param path
     * @param filename
     * @param split
     */
    public JOTDocletIndexWriter(ConfigurationImpl configuration, HtmlDocletWriter writer)
    {
        this.docWriter = writer;
        this.configuration = configuration;
        builder = new IndexBuilder(configuration, false);
    }

    /**
     * Build html for elements stating with given charcater
     * if c is null, then generate everyhting (all elements)
     * @param c
     * @return
     * @throws java.lang.Exception
     */
    public String generateHtml(Character car) throws Exception
    {
        Object[] cars = new Object[1];
        cars[0] = car;
        if (car == null)
        {
            cars = (Object[]) builder.elements();
        }
        String page = "";
        for (int i = 0; i != cars.length; i++)
        {
            Character c = (Character) cars[i];
            List members = builder.getMemberList(c);
            Iterator it = members.iterator();
            String text = "";
            String style = "";
            while (it.hasNext())
            {
                Doc doc = (Doc) it.next();
                if (doc instanceof PackageDoc)
                {
                    continue;
                } else if (doc.isAnnotationType())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Annotation</b> in " + mdoc.containingPackage().name();
                    style = "annot";
                } else if (doc.isConstructor())
                {
                    ConstructorDoc mdoc = (ConstructorDoc) doc;
                    text = getLink(doc) + " <b>Constructor</b> in " + mdoc.containingClass().qualifiedName();
                    style = "constructor";
                } else if (doc.isMethod())
                {
                    MethodDoc mdoc = (MethodDoc) doc;
                    text = getLink(doc) + " <b>Method</b> in " + mdoc.containingClass().qualifiedName();
                    style = "method";
                } else if (doc.isEnum())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Enumeration</b> in " + mdoc.containingPackage().name();
                    style = "enum";
                } else if (doc.isEnumConstant())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Enumeration Constant</b> in " + mdoc.containingClass().qualifiedName();
                    style = "constant";
                } else if (doc.isError())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Error</b> in " + mdoc.containingPackage().name();
                    style = "error";
                } else if (doc.isException())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Exception</b> in " + mdoc.containingPackage().name();
                    style = "error";
                } else if (doc.isAnnotationTypeElement())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Annotation</b> Element in " + mdoc.containingClass().qualifiedName();
                    style = "annot";
                } else if (doc.isField())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Field</b> in " + mdoc.containingClass().qualifiedName();
                    style = "field";
                } else if (doc.isInterface())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Interface</b> in " + mdoc.containingPackage().name();
                    style = "interface";
                } else if (doc.isClass())
                {
                    ProgramElementDoc mdoc = (ProgramElementDoc) doc;
                    text = getLink(doc) + " <b>Class</b> in " + mdoc.containingPackage().name();
                    style = "class";
                } else
                {
                    text = doc.name();
                }
                page += text+"<br/>\n";
            }
        }
        return page;
    }

    public String getLink(Doc doc)
    {
        if (doc instanceof ProgramElementDoc)
        {
            String link = getLink((ProgramElementDoc) doc, LinkInfoImpl.CONTEXT_INDEX);
            return "<a href='" + link + "'>" + JOTHTMLUtilities.textToHtml(getSignature(doc),JOTHTMLUtilities.ENCODE_HTML_CHARS) + "</a>";
        }
        return "#######" + doc.name();
    }

    public String getLink(ProgramElementDoc doc, int context)
    {
        ClassDoc cdoc = doc.containingClass();
        if (doc instanceof ClassDoc || doc.isInterface() || doc.isEnum() || doc.isError() || doc.isException() || doc.isAnnotationType())
        {
            cdoc = (ClassDoc) doc;
        }

        LinkInfoImpl lnInfo = new LinkInfoImpl(context, cdoc, "", null);
        if (lnInfo == null)
        {
            return null;
        }
        String link = docWriter.getLink(lnInfo);
        if (link == null)
        {
            return null;
        }
        return getHref(link) + "#" + getSignature(doc);
    }

    public Object[] getCharactersAsObjects()
    {
        return builder.elements();
    }

    private String getHref(String link)
    {
        if (link == null)
        {
            return null;
        }
        int i = link.indexOf("HREF=\"") + 6;
        if (i >= 6)
        {
            return link.substring(i, link.indexOf("\"", i));
        } else
        {
            return null;
        }
    }

    /*public String getLink(ClassDoc doc, int context)
    {
    LinkInfoImpl lnInfo = new LinkInfoImpl(context, doc, "", null);
    String link = docWriter.getLink(lnInfo);
    return getHref(link);
    }*/
    public String getSignature(Doc doc)
    {
        if (doc instanceof MethodDoc)
        {
            return doc.name() + ((MethodDoc) doc).signature();
        } else if (doc instanceof FieldDoc)
        {
            return doc.name();
        } else if (doc instanceof ConstructorDoc)
        {
            return doc.name() + ((ConstructorDoc) doc).signature();
        }
        return doc.name();
    }

    public String getPathToRoot(Doc doc)
    {
        String path = "";
        if (doc != null && !(doc instanceof RootDoc))
        {
            String name = doc.name();
            if (doc instanceof ClassDoc)
            {
                name = ((ClassDoc) doc).containingPackage().name();
            }
            for (int i = 0; i != name.split("\\.").length; i++)
            {
                path += "../";
            }
        }
        return path;
    }
}
