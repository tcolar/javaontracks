/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.javadoc.AnnotationTypeDocImpl;
import com.sun.tools.javadoc.ClassDocImpl;
import com.sun.tools.javadoc.PackageDocImpl;
import java.util.Arrays;
import net.jot.JOTInitializer;
import net.jot.web.views.JOTLightweightView;

/**
 *
 * @author thibautc
 */
public class JOTDocletNavView extends JOTLightweightView {

    public final static String PACKAGES = "packages";

    public JOTDocletNavView() {
        addVariable("jotversion", JOTInitializer.VERSION);
    }

    public String getItemLink(PackageDocImpl pack) {
        return JOTDoclet.getPkgFolder(pack) + "package-summary.html";
    }

    public String getItemLink(AnnotationTypeDocImpl pack) {
        return "";
    }

    public String getItemLink(ClassDocImpl pack) {
        return "";
    }

    public ClassDoc[] getSortedClasses() {
        PackageDoc pack = (PackageDoc) getVariables().get("curitem");
        return getSortedClasses(pack);
    }

    public ClassDoc[] getSortedClasses(PackageDocImpl pack) {
        return getSortedClasses((PackageDoc) pack);
    }

    public ClassDoc[] getSortedClasses(PackageDoc pack) {
        ClassDoc[] clazzes = pack.allClasses();
        Arrays.sort(clazzes);
        return clazzes;
    }

    public String getTypeImage(AnnotationTypeDocImpl annot) {
        return getPathToRoot() + "img/annotation.png";
    }

    public String getPathToRoot() {
        String path = "";
        PackageDoc pack = (PackageDoc) getVariables().get("curitem");
        if (pack != null) {
            for (int i = 0; i != pack.name().split("\\.").length; i++) {
                path += "../";
            }
        }
        return path;
    }

    public String getTypeImage(ClassDocImpl clazz) {
        if (clazz.isOrdinaryClass()) {
            if (!clazz.isAbstract()) {
                return getPathToRoot() + "img/class.png";
            } else {
                return getPathToRoot() + "img/abstract.png";
            }
        } else if (clazz.isInterface()) {
            return getPathToRoot() + "img/interface.png";
        } else if (clazz.isEnum()) {
            return getPathToRoot() + "img/enum.png";
        } else if (clazz.isError() || clazz.isException()) {
            return getPathToRoot() + "img/error.png";
        } else if (clazz.isAnnotationType()) {
            return getPathToRoot() + "img/annotation.png";
        }
        //default;
        return getPathToRoot() + "img/class.png";
    }

    public String getTreeOffset(PackageDocImpl pack) {
        String result = "";
        int nb = pack.name().split("\\.").length;
        for (int i = 1; i < nb; i++) {
            result += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }
        return result;
    }

    public String getShortDescription() {
        Doc pack = (Doc) getVariables().get("curitem");
        return getShortDescription(pack);
    }

    public String getShortDescription(ClassDocImpl pack) {
        return getShortDescription((Doc) pack);
    }

    public String getShortDescription(AnnotationTypeDocImpl pack) {
        return getShortDescription((Doc) pack);
    }

    public String getShortDescription(PackageDocImpl pack) {
        return getShortDescription((Doc) pack);
    }

    public String getShortDescription(Doc pack) {
        String text = getFullDescription(pack);
        //java spacs says
        text = getFirstSentence(text);
        // remove html tags, since we might only have the opening one on line 1 .. which then breaks our nice fancy page.
        text = text.replaceAll("\\<.*?>", "");
        return text;
    }

    public void getJDOCTags() {
        Doc pack = (Doc) getVariables().get("curitem");
        Tag[] tags = pack.tags();
        for (int i = 0; i != tags.length; i++) {
            System.out.println(tags[i].kind() + " " +
                    tags[i].name() + " " + tags[i].toString());
        }
    }

    public String getFullDescription() {
        Doc pack = (Doc) getVariables().get("curitem");
        return getFullDescription(pack);
    }

    public String getFullDescription(ClassDocImpl pack) {
        return getFullDescription((Doc) pack);
    }

    public String getFullDescription(AnnotationTypeDocImpl pack) {
        return getFullDescription((Doc) pack);
    }

    public String getFullDescription(PackageDocImpl pack) {
        return getFullDescription((Doc) pack);
    }

    public String getFullDescription(Doc pack) {
        String text = pack.commentText();
        if (text == null) {
            text = "";
        }
        if (containsHtml(text)) {
            if (text != null && text.indexOf(".") > 0) {
                text = text.substring(0, text.indexOf("."));
            }
        } else {
            /**
             * If there is no html tag, it's probably a raw text comments that would gain
             * fromm converting line feeds into <br/>
             */
            text = text.replaceAll("\n", "<br/>");
        }
        return text;
    }

    private boolean containsHtml(String txt) {
        // kinda lame
        return txt.indexOf("/>") != -1;
    }

    private String getFirstSentence(String text) {
        if (text.indexOf(". ") > 0) {
            text = text.substring(0, text.indexOf("."));
        } else if (text.indexOf(".\n") > 0) {
            text = text.substring(0, text.indexOf("."));
        } else if (text.indexOf(".\t") > 0) {
            text = text.substring(0, text.indexOf("."));
        }

        if (text.length() == 0) {
            text = "---- No Doc ----";
        }
        return text;
    }
}
