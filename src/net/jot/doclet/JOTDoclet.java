/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;
import com.sun.tools.doclets.formats.html.ConfigurationImpl;
import com.sun.tools.doclets.internal.toolkit.AbstractDoclet;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import net.jot.logger.JOTLogger;
import net.jot.utils.JOTUtilities;
import net.jot.web.view.JOTViewParser;

/**
 *
 * @author thibautc
 */
public class JOTDoclet extends AbstractDoclet
{

    public static final String RES_ROOT = "/home/thibautc/NetBeansProjects/javaontracks/resources/doclet/";
    public static final String OUT_ROOT = "/tmp/";
    public ConfigurationImpl configuration = (ConfigurationImpl) configuration();

    public static boolean start(RootDoc root)
    {
        JOTDoclet doclet = new JOTDoclet();
        return doclet.start(doclet, root);
    }

    public boolean start(JOTDoclet doclet, RootDoc root)
    {
        JOTLogger.init("/tmp/jotdoclet.log", JOTLogger.ALL_LEVELS, null);

        copyResources();

        configuration.root = root;
        try
        {
            doclet.startGeneration(root);
        } catch (Exception exc)
        {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    private void copyResources()
    {
        try
        {
            File dest = new File(OUT_ROOT);
            File src = new File(RES_ROOT);
            dest.mkdirs();
            JOTUtilities.copyFolderContent(dest, src, true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void startGeneration(RootDoc root) throws Exception
    {
        if (root.classes().length == 0)
        {
            configuration.message.error("doclet.No_Public_Classes_To_Document");
            return;
        }
        configuration.setOptions();
        configuration.getDocletSpecificMsg().notice("doclet.build_version",
                configuration.getDocletSpecificBuildDate());
        ClassTree classtree = new ClassTree(configuration, configuration.nodeprecated);

        /*generateClassFiles(root, classtree);
        if (configuration.sourcepath != null && configuration.sourcepath.length() > 0)
        {
        StringTokenizer pathTokens = new StringTokenizer(configuration.sourcepath,
        String.valueOf(File.pathSeparatorChar));
        boolean first = true;
        while (pathTokens.hasMoreTokens())
        {
        Util.copyDocFiles(configuration,
        pathTokens.nextToken() + File.separator,
        DocletConstants.DOC_FILES_DIR_NAME, first);
        first = false;
        }
        }*/

        //PackageListWriter.generate(configuration);
        //generatePackageFiles(classtree);
        generatePackageList(classtree);

        //generateOtherFiles(root, classtree);
        configuration.tagletManager.printReport();
    }

    public static LanguageVersion languageVersion()
    {
        return LanguageVersion.JAVA_1_5;
    }

    public Configuration configuration()
    {
        return ConfigurationImpl.getInstance();
    }

    protected void generatePackageList(ClassTree tree) throws Exception
    {
        File navigator = new File(OUT_ROOT + "nav.html");
        PrintWriter writer = null;
        JOTDocletNavView view = new JOTDocletNavView();
        try
        {
            writer = new PrintWriter(navigator);

            PackageDoc[] packages = configuration.packages;
            Arrays.sort(packages);
            view.addVariable(JOTDocletNavView.PACKAGES, packages);

            String html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl/nav.html");
            writer.print(html);
        } catch (FileNotFoundException e)
        {
            throw (e);
        } finally
        {
            if (writer != null)
            {
                writer.close();
            }
        }

    }

    /*protected void generateClassFiles(ClassDoc[] arr, ClassTree classtree)
    {
    Arrays.sort(arr);
    for (int i = 0; i < arr.length; i++)
    {
    if (!(configuration.isGeneratedDoc(arr[i]) && arr[i].isIncluded()))
    {
    continue;
    }
    //ClassDoc prev = (i == 0) ? null : arr[i - 1];
    ClassDoc curr = arr[i];
    //ClassDoc next = (i + 1 == arr.length) ? null : arr[i + 1];
    try
    {
    if (curr.isAnnotationType())
    {
    //TODO

    } else
    {
    AbstractBuilder classBuilder =
    configuration.getBuilderFactory().getClassBuilder(curr, prev, next, classtree);
    classBuilder.build();
    }
    } catch (Exception e)
    {
    e.printStackTrace();
    throw new DocletAbortException();
    }
    }
    }*/
    public static boolean validOptions(String options[][],
            DocErrorReporter reporter)
    {
        // Construct temporary configuration for check
        return (ConfigurationImpl.getInstance()).validOptions(options, reporter);
    }

    public static int optionLength(String option)
    {
        // Construct temporary configuration for check
        return (ConfigurationImpl.getInstance()).optionLength(option);
    }

    protected void generatePackageFiles(ClassTree arg0) throws Exception
    {
    }

    protected void generateClassFiles(ClassDoc[] arg0, ClassTree arg1)
    {
    }
    /**
     * testing
     * @param args
     */
    /*public static void main(String args[])
    {
    start(null);
    }*/
}
