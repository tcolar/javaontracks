/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.AnnotationTypeElementDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.doclets.formats.html.LinkInfoImpl;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
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
    private List subs;
    private List allSubs;
    private Doc curtag;
    // cahe methods of an item(ie: class etc..) cause it's slow
    private Hashtable methodCache = new Hashtable();
    private Hashtable fieldsCache = new Hashtable();

    public void reset()
    {
        subs = null;
        allSubs = null;
    }

    public JOTDocletNavView(HtmlDocletWriter docWriter)
    {
        this.docWriter = docWriter;
    }
    public final static String PACKAGES = "packages";

    public String getItemLink(PackageDoc pack)
    {
        return getPathToRoot() + JOTDoclet.getPkgFolder(pack) + "package-summary.html";
    }

    public String getItemLink(ClassDoc doc, int context)
    {
        LinkInfoImpl lnInfo = new LinkInfoImpl(context, doc, "", null);
        String link = docWriter.getLink(lnInfo);
        return getHref(link);
    }

    public boolean isClassDoc()
    {
        Object cur = getVariables().get("curitem");
        return (cur instanceof ClassDoc);
    }

    public String getItemLink(ClassDoc doc)
    {
        return getItemLink(doc, LinkInfoImpl.CONTEXT_CLASS);
    }

    public String getItemLink(JOTDocletHolder holder)
    {
        return getItemLink(holder, LinkInfoImpl.CONTEXT_CLASS);
    }

    public String getPackageLink(ClassDoc doc)
    {
        return getItemLink(doc.containingPackage());
    }

    public String getItemLink(JOTDocletHolder holder, int context)
    {
        LinkInfoImpl lnInfo = new LinkInfoImpl(context, holder.getDoc().containingClass(), "", null);
        String link = docWriter.getLink(lnInfo);
        if (link == null)
        {
            return null;
        }
        return getHref(link) + "#" + getSignature(holder.getDoc());
    }

    public String getDirectLink()
    {
        return getDirectLink((String) null);
    }

    public String getDirectLink(JOTDocletHolder handler)
    {
        return getDirectLink(getAnchorName(handler));
    }

    public String getDirectLink(ConstructorDoc doc)
    {
        return getDirectLink(getAnchorName(doc));
    }

    public String getDirectLink(FieldDoc doc)
    {
        return getDirectLink(getAnchorName(doc));
    }

    public String getDirectLink(String anchor)
    {
        String curPage = (String) getVariables().get("curpage");
        String index = (String) getVariables().get("indexfile");
        return getPathToRoot() + index+"?page=" + curPage + (anchor == null ? "" : "#" + anchor);
    }

    public String getAnchorName(JOTDocletHolder handler)
    {
        return getSignature(handler.getDoc());
    }

    public String getAnchorName(ConstructorDoc doc)
    {
        return getSignature(doc);
    }

    public String getAnchorName(FieldDoc doc)
    {
        return doc.name();
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

    public boolean isDeprecated(Doc doc)
    {
        return doc.tags("deprecated").length > 0;
    }

    public boolean isDeprecated(JOTDocletHolder holder)
    {
        return isDeprecated(holder.getDoc());
    }

    public Vector getEnumConstants()
    {
        Vector myDocs = new Vector();
        ClassDoc mainDoc = (ClassDoc) getVariables().get("curitem");
        FieldDoc[] docs = mainDoc.enumConstants();
        ClassDoc doc = mainDoc.superclass();
        for (int i = 0; i != docs.length; i++)
        {
            myDocs.add(docs[i]);
        }
        // go through all superclasses fields
        while (doc != null && !doc.name().equalsIgnoreCase("object"))
        {
            //note: We don't add the "object" fields (clutter)
            docs = doc.enumConstants();
            for (int i = 0; i != docs.length; i++)
            {
                myDocs.add(docs[i]);
            }
            doc = doc.superclass();
        }
        FieldDoc[] docArray = (FieldDoc[]) myDocs.toArray(new FieldDoc[0]);
        Arrays.sort(docArray);
        return myDocs;

    }

    public Vector getInnerClasses()
    {
        Vector myDocs = new Vector();
        ClassDoc mainDoc = (ClassDoc) getVariables().get("curitem");
        ClassDoc[] docs = mainDoc.innerClasses(false);
        for (int i = 0; i != docs.length; i++)
        {
            // don't add private
            if (docs[i].isPrivate())
            {
                continue;
            }
            myDocs.add(docs[i]);
        }
        ClassDoc doc = mainDoc.superclass();
        // go through all superclasses fields
        while (doc != null && !doc.name().equalsIgnoreCase("object"))
        {
            //note: We don't add the "object" fields (clutter)
            docs = doc.innerClasses(false);
            for (int i = 0; i != docs.length; i++)
            {
                // don't add superclasses private fields
                if (docs[i].isPrivate())
                {
                    continue;
                }
                // don't add superclasses package private and != packages
                if (docs[i].isPackagePrivate() && !mainDoc.containingPackage().name().equals(doc.containingPackage().name()))
                {
                    continue;
                }
                // OK, finally add it.
                myDocs.add(docs[i]);
            }
            doc = doc.superclass();
        }
        ClassDoc[] docArray = (ClassDoc[]) myDocs.toArray(new ClassDoc[0]);
        Arrays.sort(docArray);
        return myDocs;
    }

    public Vector getHierarchy()
    {
        Vector results = new Vector();
        ClassDoc doc = (ClassDoc) getVariables().get("curitem");
        while (doc != null)
        {
            results.add(0, doc);
            if (doc.isInterface())
            {
                ClassDoc[] docs = doc.interfaces();
                if (docs == null || docs.length == 0)
                {
                    doc = null;
                } else // an interface might have ONE superinterface
                {
                    doc = docs[0];
                }
            } else
            {
                doc = doc.superclass();
            }

        }
        return results;
    }

    public String getShortDescription(JOTDocletHolder holder)
    {
        return getShortDescription(holder.getDoc());
    }

    public String getFullDescription(JOTDocletHolder holder)
    {
        return getFullDescription(holder.getDoc());
    }

    public Boolean hasMoreClasses()
    {
        return new Boolean(getAllSubClasses().size() > getSubClasses().size());
    }

    public List getSubClasses()
    {
        if (subs == null)
        {
            ClassDoc doc = (ClassDoc) getVariables().get("curitem");
            ClassTree tree = (ClassTree) getVariables().get("classTree");
            subs = getSubClassesCopy(doc, tree, false);
        }
        return subs;
    }

    private String buildParamTagText(ParamTag tag)
    {
        String result = "<font class='tagInfo'>" + tag.parameterName() + "</font>";
        result += " - " + tag.parameterComment();
        return result;
    }

    private String buildThrowTag(ThrowsTag tag)
    {
        String result = "";
        if (tag.exception() == null)
        {
            result = "<font class='tagInfo'>" + tag.exceptionName() + "</font>";
        } else
        {
            String link = getItemLink(tag.exception());
            if (link == null)
            {
                result = "<font class='tagInfo'>" + tag.exception().qualifiedName() + "</font>";
            } else
            {
                result = "<a href='" + link + "'>" + tag.exceptionName() + "</a>";
            }
        }
        result += " - " + tag.exceptionComment();
        return result;
    }

    private String formatValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof String)
        {
            return "\"" + value + "\"";
        }
        if (value instanceof Character)
        {
            return "'" + value + "'";
        }
        return value.toString();
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

    public String getAnchorImage()
    {
        return "<img border=0 src='" + getPathToRoot() + "img/anchor.png'>";
    }

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
        return "";
    }

    private List getSubClassesCopy(ClassDoc doc, ClassTree tree, boolean all)
    {
        List subs = all ? tree.allSubs(doc, false) : tree.subs(doc, false);
        if (subs.size() > 100)
        {
            subs = new Vector().subList(0, 0);
        }
        // we do a defensive copy, otherwise we have issues later.
        ArrayList list = new ArrayList(subs);
        //System.out.println("copy " + list.size() + " " + all);
        return list;
    }

    public List getAllSubClasses()
    {
        if (allSubs == null)
        {
            ClassDoc doc = (ClassDoc) getVariables().get("curitem");
            ClassTree tree = (ClassTree) getVariables().get("classTree");
            allSubs = getSubClassesCopy(doc, tree, true);
        }
        return allSubs;
    }

    public String getPathToRoot()
    {
        String path = "";
        Doc doc = (Doc) getVariables().get("curitem");
        if (getVariables().get("manualPath") != null)
        {
            path = (String) getVariables().get("manualPath");
        } else if (doc != null && !(doc instanceof RootDoc))
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

    public boolean hasValue(JOTDocletFieldHolder doc)
    {
        return ((FieldDoc) doc.getDoc()).constantValue() != null;
    }

    public String getElemValue(AnnotationTypeElementDoc doc)
    {
        Object value = doc.defaultValue();
        return value == null ? null : formatValue(value);
    }

    public String getFieldValue(JOTDocletFieldHolder doc)
    {
        Object value = ((FieldDoc) doc.getDoc()).constantValue();
        if (value == null)
        {
            return "";
        }
        return formatValue(value.toString());
    }

    public ConstructorDoc[] getConstructors()
    {
        ClassDoc doc = (ClassDoc) getVariables().get("curitem");
        ConstructorDoc[] docs = doc.constructors(false);
        // constructors are not inherited.
        // we show private constructors as this is useful info.
        Arrays.sort(docs);
        return docs;
    }

    public JOTDocletFieldHolder[] getFields()
    {
        ClassDoc mainDoc = (ClassDoc) getVariables().get("curitem");
        return getFields(mainDoc);
    }

    public AnnotationTypeElementDoc[] getAnnotElems()
    {
        Vector myDocs = new Vector();
        ClassDoc mainDoc = (ClassDoc) getVariables().get("curitem");
        //System.out.println(mainDoc.typeName());
        //System.out.println(mainDoc instanceof AnnotationTypeDoc);
        AnnotationTypeDoc doc = (AnnotationTypeDoc) mainDoc;
        AnnotationTypeElementDoc[] docs = doc.elements();

        for (int i = 0; i != docs.length; i++)
        {
            if (docs[i].isPrivate())
            {
                continue;
            }
            myDocs.add(docs[i]);
        }
        AnnotationTypeElementDoc[] docArray = (AnnotationTypeElementDoc[]) myDocs.toArray(new AnnotationTypeElementDoc[0]);
        Arrays.sort(docArray);
        return docArray;
    }

    public Integer getLineNb(Integer nb)
    {
        return new Integer(nb.intValue() + 1);
    }

    public JOTDocletFieldHolder[] getFields(ClassDoc mainDoc)
    {
        if (fieldsCache.containsKey(mainDoc.qualifiedName()))
        {
            return (JOTDocletFieldHolder[]) fieldsCache.get(mainDoc.qualifiedName());
        }
        Vector myDocs = new Vector();
        FieldDoc[] docs = mainDoc.fields(false);
        for (int i = 0; i != docs.length; i++)
        {
            // don't add private fields
            if (docs[i].isPrivate())
            {
                continue;
            }
            JOTDocletFieldHolder holder = new JOTDocletFieldHolder(docs[i], null);
            myDocs.add(holder);
        }
        // for all the fields in those class, check if fields where
        // specified in implemented interfaces, to mark them as such
        ClassDoc[] interfaces = mainDoc.interfaces();
        for (int k = 0; k != interfaces.length; k++)
        {
            // go trough each impl interfaces and their potential superclasses
            JOTDocletFieldHolder[] intFields = getFields(interfaces[k]);
            for (int l = 0; l != intFields.length; l++)
            {
                intFields[l].setSuperClass(interfaces[k]);
                myDocs.add(intFields[l]);
            }
        }
        ClassDoc doc = mainDoc.superclass();
        // go through all superclasses fields
        while (doc != null && !doc.name().equalsIgnoreCase("object"))
        {
            //note: We don't add the "object" fields (clutter)
            docs = doc.fields(false);
            for (int i = 0; i != docs.length; i++)
            {
                // don't add superclasses private fields
                if (docs[i].isPrivate())
                {
                    continue;
                }
                // don't add superclasses package private and != packages
                if (docs[i].isPackagePrivate() && !mainDoc.containingPackage().name().equals(doc.containingPackage().name()))
                {
                    continue;
                }
                // check if the field was overriden in our object
                // if so mark that
                boolean skip = false;
                String sig = getSignature(docs[i]);
                for (int j = 0; j != myDocs.size(); j++)
                {
                    JOTDocletFieldHolder holder = (JOTDocletFieldHolder) myDocs.get(j);
                    if (sig.equals(getSignature((FieldDoc) holder.getDoc())))
                    {
                        skip = true;
                        if (holder.getOverridenIn() == null)
                        {
                            if (holder.getSuperClass() == null)
                            {
                                // mark only the first level override (closer in hierarchy)
                                holder.setOverridenIn(doc);
                            }
                        }
                    }
                }
                // if it was overidden/impl. we don't want to add the original field
                if (skip)
                {
                    continue;
                }
                // OK, finally add it.
                myDocs.add(new JOTDocletFieldHolder(docs[i], doc));
            }
            doc = doc.superclass();
        }
        JOTDocletFieldHolder[] docArray = (JOTDocletFieldHolder[]) myDocs.toArray(new JOTDocletFieldHolder[0]);
        Arrays.sort(docArray);
        fieldsCache.put(mainDoc.qualifiedName(), docArray);
        return docArray;
    }

    public JOTDocletMethodHolder[] getMethods()
    {
        ClassDoc mainDoc = (ClassDoc) getVariables().get("curitem");
        return getMethods(mainDoc);
    }

    public JOTDocletMethodHolder[] getMethods(ClassDoc mainDoc)
    {
        if (methodCache.containsKey(mainDoc.qualifiedName()))
        {
            return (JOTDocletMethodHolder[]) methodCache.get(mainDoc.qualifiedName());
        }
        Vector myDocs = new Vector();
        //Vector intMethods=getAllInterfaceMethods(mainDoc);
        MethodDoc[] docs = mainDoc.methods(false);
        for (int i = 0; i != docs.length; i++)
        {
            // don't add private methods
            if (docs[i].isPrivate())
            {
                continue;
            }

            JOTDocletMethodHolder holder = new JOTDocletMethodHolder(docs[i], null);
            String sig = getSignature(docs[i]);
            ClassDoc[] interfaces = mainDoc.interfaces();
            // for all the methods in this class, check if methods where
            // specified in implemented interfaces, to mark them as such
            for (int k = 0; k != interfaces.length; k++)
            {
                // go trough each impl interfaces and their potential superclasses

                JOTDocletMethodHolder[] intMethods = getMethods(interfaces[k]);
                for (int l = 0; l != intMethods.length; l++)
                {
                    if (getSignature((MethodDoc) intMethods[l].getDoc()).equals(sig))
                    {
                        if (holder.getSpecifiedIn() == null)
                        {
                            holder.setSpecifiedIn(interfaces[k]);
                        }
                    }
                }
            }
            myDocs.add(holder);
        }
        ClassDoc doc = mainDoc.superclass();
        // go through all superclasses methods
        while (doc != null && !doc.name().equalsIgnoreCase("object"))
        {
            //note: We don't add the "object" methods (clutter)
            docs = doc.methods(false);
            for (int i = 0; i != docs.length; i++)
            {
                // don't add superclasses private methods
                if (docs[i].isPrivate())
                {
                    continue;
                }
                // don't add superclasses package private and != packages
                if (docs[i].isPackagePrivate() && !mainDoc.containingPackage().name().equals(doc.containingPackage().name()))
                {
                    continue;
                }
                // check if the method was overriden in our object
                // if so mark that
                boolean skip = false;
                String sig = getSignature(docs[i]);
                for (int j = 0; j != myDocs.size(); j++)
                {
                    JOTDocletMethodHolder holder = (JOTDocletMethodHolder) myDocs.get(j);
                    if (sig.equals(getSignature((MethodDoc) holder.getDoc())))
                    {
                        skip = true;
                        if (docs[i].isAbstract())
                        {
                            //in case of absract method, mark as "specified" rather than ovveriden
                            if (holder.getSpecifiedIn() == null)
                            {
                                holder.setSpecifiedIn(doc);
                            }

                        } else if (holder.getOverridenIn() == null)
                        {
                            if (holder.getSuperClass() == null)
                            {
                                // mark only the first level override (closer in hierarchy)
                                holder.setOverridenIn(doc);
                            }
                        }
                    }
                }
                // if it was overidden/impl. we don't want to add the original method
                if (skip)
                {
                    continue;
                }
                // OK, finally add it.
                myDocs.add(new JOTDocletMethodHolder(docs[i], doc));
            }
            doc = doc.superclass();
        }
        JOTDocletMethodHolder[] docArray = (JOTDocletMethodHolder[]) myDocs.toArray(new JOTDocletMethodHolder[0]);
        Arrays.sort(docArray);
        methodCache.put(mainDoc.qualifiedName(), docArray);
        return docArray;
    }

    public String getModifiersString(JOTDocletMethodHolder holder)
    {
        return getModifiersString(holder.getDoc());
    }

    public String getModifiersString(JOTDocletFieldHolder holder)
    {
        return getModifiersString(holder.getDoc());
    }

    public String getReturnString(JOTDocletHolder holder)
    {
        Type type = null;
        if (holder instanceof JOTDocletFieldHolder)
        {
            type = ((FieldDoc) holder.getDoc()).type();
        } else
        {
            type = ((MethodDoc) holder.getDoc()).returnType();
        }
        String str = "";
        if (type.asParameterizedType() != null)
        {
            str += handleGenerics(type, 0);
        } else if (type.asTypeVariable() != null)
        {
            str += handleGenerics(type, 0);
        } else if (type.asClassDoc() != null)
        {
            str += getTypeLink(type.asClassDoc());
        } else
        {
            str += "<font class='type'>" + type.simpleTypeName() + "</font>";
        }
        return str;
    }

    private String getTypeLink(ClassDoc doc)
    {
        String str = "";
        String link = getItemLink(doc);
        if (link == null)
        {
            str = "<font class='type'>" + doc.qualifiedName() + "</font>";
        } else
        {
            str = "<a class='regular' href='" + link + "'><font class='type'>" + doc.simpleTypeName() + "</font></a>";
        }
        return str;
    }

    public String getModifiersString(ProgramElementDoc doc)
    {
        String modifs = "";
        int modif = doc.modifierSpecifier();
        if (Modifier.isPrivate(modif))
        {
            modifs += "<span class='private'>private</span>";
        } else if (Modifier.isProtected(modif))
        {
            modifs += "<span class='protected'>protected</span>";
        } else if (Modifier.isPublic(modif))
        {
            modifs += "<span class='public'>public</span>";
        } else
        {
            modifs += "<span class='protected'>pack-private</span>";
        }
        if (Modifier.isAbstract(modif))
        {
            modifs += "<span class='abstract'>abstract</span>";
        }
        if (Modifier.isFinal(modif))
        {
            modifs += "<span class='final'>final</span>";

        }
        if (Modifier.isStatic(modif))
        {
            modifs += "<span class='static'>static</span>";

        }
        if (Modifier.isSynchronized(modif))
        {
            modifs += "<span class='synchronized'>synchronized</span>";

        }
        if (Modifier.isNative(modif))
        {
            modifs += "<span class='native'>native</span>";

        }
        if (Modifier.isTransient(modif))
        {
            modifs += "<span class='transient'>transient</span>";
        }
        if (Modifier.isVolatile(modif))
        {
            modifs += "<span class='volatile'>volatile</span>";
        }
        return modifs;
    }

    public String getParamString(ConstructorDoc doc)
    {
        String str = "(";
        Parameter[] params = doc.parameters();
        str = str + processParams(params);
        str += ")";
        return str;
    }

    public String getParamString(JOTDocletMethodHolder holder)
    {
        String str = "(";
        Parameter[] params = ((MethodDoc) holder.getDoc()).parameters();
        str = str + processParams(params);
        str += ")";
        return str;
    }

    public boolean hasMoreInfos(JOTDocletHolder holder)
    {
        return hasMoreInfos(holder.getDoc());
    }

    public boolean hasMoreInfos()
    {
        Doc doc = (Doc) getVariables().get("curitem");
        return hasMoreInfos(doc);
    }

    /**
     * Compares full desc. to short desc. to see wether more infos avail.
     * @return
     */
    public boolean hasMoreInfos(Doc doc)
    {
        //System.out.println(doc.name());
        String s1, s2;
        try
        {
            s1 = docWriter.commentTagsToString(null, doc, doc.inlineTags(), false);
            s2 = docWriter.commentTagsToString(null, doc, doc.firstSentenceTags(), true);
        } catch (ClassCastException e)
        {
            // java/lang/StringBuilder.html codePointAt javadic causes this -> why ??
            System.err.println("Error parsing comments for " + doc.name());
            return false;
        }
        return s1.length() > s2.length() ||
                doc.tags("return").length > 0 ||
                doc.tags("param").length > 0 ||
                doc.tags("see").length > 0 ||
                doc.tags("since").length > 0 ||
                doc.tags("throws").length > 0 ||
                doc.tags("exception").length > 0;
    }

    public String getType(ClassDoc clazz)
    {
        if (clazz.isOrdinaryClass())
        {
            if (!clazz.isAbstract())
            {
                return "class";
            } else
            {
                return "abstract class";
            }
        } else if (clazz.isInterface())
        {
            return "interface";
        } else if (clazz.isEnum())
        {
            return "enum";
        } else if (clazz.isError())
        {
            return "error";
        } else if (clazz.isException())
        {
            return "exception";
        } else if (clazz.isAnnotationType())
        {
            return "annotation";
        }
        //default;
        return "class";
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
        docWriter.relativePath = getPathToRoot();
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

    public boolean hasItemTags(String tagName)
    {
        return getItemTags(tagName).length > 0;
    }

    public Tag[] getItemTags(String tagName)
    {
        Tag[] tags = getJDOCTags(curtag, tagName);
        //System.out.println(curtag.name()+" / "+ tagName+" : "+tags.length);
        return tags;
    }

    public String charVal(Character c)
    {
        int nb = (c.charValue() - ('A' - 1));
        if (c.equals(new Character('_')))
        {
            nb = 27;
        }
        if (c.equals(new Character('$')))
        {
            nb = 28;
        }
        return "" + nb;
    }

    public String getIndexFile()
    {
        // TODO: index-1.html might not exist if there are no items starting by 'a' ... unlikely
        if (docWriter.configuration.splitindex)
        {
            return getPathToRoot() + "index-files/index-1.html";
        } else
        {
            return getPathToRoot() + "index-files/index-all.html";
        }
    }

    public Tag[] getJDOCTags(String tagName)
    {
        Doc doc = (Doc) getVariables().get("curitem");
        return getJDOCTags(doc, tagName);
    }

    public Tag[] getJDOCTags(Doc doc, String tagName)
    {
        Tag[] tags = doc.tags(tagName);
        return tags;
    }

    public AnnotationDesc[] getAnnotations(JOTDocletHolder holder)
    {
        return getAnnotations(holder.getDoc());
    }

    public AnnotationDesc[] getAnnotations(ProgramElementDoc doc)
    {
        AnnotationDesc[] annots = doc.annotations();
        return annots;
    }

    public AnnotationDesc[] getAnnotations()
    {
        Object doc = getVariables().get("curitem");
        if (!(doc instanceof ProgramElementDoc))
        {
            return null;
        }
        return getAnnotations((ProgramElementDoc) doc);
    }

    public String getAnnotationString(AnnotationDesc annot)
    {
        String result = "<span class='annotation'>";
        String link = getItemLink(annot.annotationType());
        result += link == null ? "@" + annot.annotationType().qualifiedName() : "<a href='" + link + "'>@" + annot.annotationType().name() + "</a>";
        AnnotationDesc.ElementValuePair[] pairs = annot.elementValues();
        if (pairs.length > 0)
        {
            result += " (";
            for (int i = 0; i != pairs.length; i++)
            {
                if (i > 0)
                {
                    result += ", ";
                }
                AnnotationDesc.ElementValuePair pair = pairs[i];
                String link2 = getItemLink(pair.element().containingClass());
                result += link2 == null ? pair.element().qualifiedName() : "<a href='" + link2 + "#" + getSignature(pair.element()) + "'>" + pair.element().name() + "</a>";
                result += "=" + formatValue(pair.value());
            }
            result += ")";
        }
        return result + "</span>";
    }

    public String getTagText(Tag tag)
    {
        if (tag.name().equals("@see"))
        {
            try
            {
                return docWriter.seeTagToString((SeeTag) tag);
            } catch (ClassCastException e)
            {
                System.err.println("Error bulding @see tag " + tag.name());
                return tag.text();
            }
        } else if (tag.name().equals("@param"))
        {
            return buildParamTagText((ParamTag) tag);
        } else if (tag.name().equals("@throws") || tag.name().equals("@exception"))
        {
            return buildThrowTag((ThrowsTag) tag);
        }

        return tag.text();
    }

    public String getShortItemName()
    {
        return getShortItemName((Doc) getVariables().get("curitem"));
    }

    public String getShortItemName(Doc doc)
    {
        String name = doc.name();
        if (name.indexOf(".") > 0)
        {
            name = name.substring(0, name.indexOf("."));
        }
        return name;
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

    private String handleGenerics(Type type, int depth)
    {
        String str = "";
        depth++;
        // lame anti-recusrion hack
        if (depth > 15)
        {
            return str;
        }
        //System.out.println(type.qualifiedTypeName());
        if (type.asParameterizedType() != null)
        {
            str += getTypeLink(type.asParameterizedType().asClassDoc());
            Type[] args = type.asParameterizedType().typeArguments();
            if (args.length > 0)
            {
                str += "<";
                for (int i = 0; i != args.length; i++)
                {
                    if (i > 0)
                    {
                        str += ", ";
                    }
                    // generic might be imbricated, so recurse as needed
                    //System.out.println("$ "+str);
                    str += handleGenerics(args[i], depth);
                }
                str += ">";
            }
        } else if (type.asTypeVariable() != null)
        {
            String name = type.simpleTypeName();

            /*Type[] types=type.asTypeVariable().bounds();
            for (int i = 0; i != types.length; i++)
            {
            if (i > 0)
            {
            str += ", ";
            }
            // generic might be imbricated, so recurse as needed
            str+=handleGenerics(types[i],depth);
            }*/
            str += (sameName(name, type.asTypeVariable()) ? "" : " <font class='genericVar'>" + type.asTypeVariable().typeName() + "</font>");
        } else if (type.asClassDoc() != null)
        {
            str = getTypeLink(type.asClassDoc());
        } else
        {
            str = "<font class='type'>" + type.simpleTypeName() + "</font>";
        }

        return str;
    }

    private String processParams(Parameter[] params)
    {
        String str = "";
        for (int i = 0; i != params.length; i++)
        {
            if (str.length() > 1)
            {
                str += ", ";
            }
            if ((params[i].type().asParameterizedType() != null || params[i].type().asTypeVariable() != null))
            {
                str += handleGenerics(params[i].type(), 0);
            } else if (params[i].type().asClassDoc() != null)
            {
                str += getTypeLink(params[i].type().asClassDoc());
            } else
            {
                str += "<font class='type'>" + params[i].typeName() + "</font>";
            }
            str += " " + params[i].name();
        }
        return str;
    }

    public String getInheritedFromItem(JOTDocletHolder holder)
    {
        String link = getItemLink(holder.getSuperClass());
        if (link == null)
        {
            return "<font class='type'>" + holder.getSuperClass().qualifiedName() + "</font>";
        }
        link += "#" + getSignature(holder.getDoc());
        return "<a class='regular' href='" + link + "'><font class='type'>" + holder.getSuperClass().name() + "</font></a>";
    }

    public String getOverridenInItem(JOTDocletHolder holder)
    {
        String link = getItemLink(holder.getOverridenIn());
        if (link == null)
        {
            return "<font class='type'>" + holder.getOverridenIn().qualifiedName() + "</font>";
        }
        link += "#" + getSignature(holder.getDoc());
        return "<a class='regular' href='" + link + "'><font class='type'>" + holder.getOverridenIn().name() + "</font></a>";
    }

    public String getSpecifiedInItem(JOTDocletHolder holder)
    {
        String link = getItemLink(holder.getSpecifiedIn());
        if (link == null)
        {
            return "<font class='type'>" + holder.getSpecifiedIn().qualifiedName() + "</font>";
        }
        link += "#" + getSignature(holder.getDoc());
        return "<a class='regular' href='" + link + "'><font class='type'>" + holder.getSpecifiedIn().name() + "</font></a>";
    }

    public boolean hasDeclaredThrows(ConstructorDoc doc)
    {
        return getDeclaredThrows(doc).size() > 0;
    }

    public Vector getDeclaredThrows(ConstructorDoc doc)
    {
        ClassDoc[] exceptions = doc.thrownExceptions();
        return getDeclaredThrows(exceptions);
    }

    public boolean hasDeclaredThrows(JOTDocletMethodHolder holder)
    {
        return getDeclaredThrows(holder).size() > 0;
    }

    public Vector getDeclaredThrows(JOTDocletMethodHolder holder)
    {
        ClassDoc[] exceptions = ((MethodDoc) holder.getDoc()).thrownExceptions();
        return getDeclaredThrows(exceptions);
    }

    public Vector getDeclaredThrows(ClassDoc[] exceptions)
    {
        Vector v = new Vector();
        for (int i = 0; i != exceptions.length; i++)
        {
            String link = getItemLink(exceptions[i]);
            if (link != null)
            {
                v.add("<a href=" + link + ">" + exceptions[i].name() + "</a>");
            } else
            {
                v.add(exceptions[i].containingPackage().name() + "." + exceptions[i].name());
            }
        }
        return v;
    }

    public String setTagsItem(JOTDocletHolder holder)
    {
        return setTagsItem(holder.getDoc());
    }

    public String setTagsItem(Doc doc)
    {
        curtag = doc;
        return "";
    }

    private boolean sameName(String name, TypeVariable type)
    {
        if (name == null || type == null)
        {
            return true;
        }
        if (type.asClassDoc() == null || type.asClassDoc().name() == null)
        {
            return true;
        }
        return name.equals(type.asClassDoc().name());
    }
}
