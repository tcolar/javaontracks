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
import com.sun.tools.doclets.formats.html.HtmlDocletWriter;
import com.sun.tools.doclets.internal.toolkit.AbstractDoclet;
import com.sun.tools.doclets.internal.toolkit.Configuration;
import com.sun.tools.doclets.internal.toolkit.util.ClassTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import net.jot.JOTInitializer;
import net.jot.logger.JOTLogger;
import net.jot.utils.JOTUtilities;
import net.jot.web.view.JOTViewParser;

/**
 * Custom Javadoc Doclet (Javadoc Generator)
 * It looks better than standard javadoc (IMHO), and is more modern (CSS based)
 * It allows package filtering (using javascript)
 * It's alos template base (JOT templates) so it's real easy to customize the output.
 * @author thibautc
 */
public class JOTDoclet extends AbstractDoclet
{

    public static final String RES_ROOT = "/home/thibautc/NetBeansProjects/javaontracks/resources/doclet/";
    public static final String OUT_ROOT = "/tmp/";
    public ConfigurationImpl configuration = (ConfigurationImpl) configuration();
    HtmlDocletWriter docWriter;
    ClassTree classTree=null;
    
    public static boolean start(RootDoc root)
    {
        JOTDoclet doclet = new JOTDoclet();
        return doclet.start(doclet, root);
    }

    public boolean start(JOTDoclet doclet, RootDoc root)
    {
        String[] levels={""+JOTLogger.CRITICAL_LEVEL,""+JOTLogger.ERROR_LEVEL,""+JOTLogger.WARNING_LEVEL};
        JOTLogger.init("/tmp/jotdoclet.log", levels , null);

        copyResources();

        configuration.root = root;
        try
        {
            docWriter = new HtmlDocletWriter(configuration, null);
            doclet.startGeneration(root);
        } catch (Exception exc)
        {
            exc.printStackTrace();
            return false;
        }
        return true;
    }

    private void addViewConstants(JOTDocletNavView view)
    {
        view.addVariable("classTree", classTree);
        view.addVariable("docTitle", configuration.doctitle);
        view.addVariable("windowTitle", configuration.windowtitle);
        view.addVariable("jotversion", JOTInitializer.VERSION);
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

    static String getPkgFolder(PackageDoc pack)
    {
        String[] subs = pack.name().split("\\.");
        String folder = "";
        for (int j = 0; j != subs.length; j++)
        {
            folder += subs[j] + File.separator;
        }
        return folder;
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
        classTree = new ClassTree(configuration, configuration.nodeprecated);

        
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
        generatePackageList(classTree);

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
        File navigator = new File(OUT_ROOT + "overview-frame.html");
        File packTree = new File(OUT_ROOT + "overview-summary.html");
        PrintWriter writer = null;
        int pkLength=0;
        int itemsLength=0;
        try
        {
            writer = new PrintWriter(navigator);

            PackageDoc[] packages = configuration.packages;
            Arrays.sort(packages);
            JOTDocletNavView view = new JOTDocletNavView(docWriter);
            addViewConstants(view);

            // write nav
            System.out.println(navigator.getAbsolutePath());
            view.addVariable(JOTDocletNavView.PACKAGES, packages);
            String html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "nav.html");
            writer.print(html);
            writer.close();

            // write package tree
            view.reset();
            System.out.println(packTree.getAbsolutePath());
            view.addVariable("curitem", configuration.root);
            view.addVariable("curpage","overview-summary.html");
            writer = new PrintWriter(packTree);
            html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "packages.html");
            writer.print(html);
            writer.close();

            // write individual package pages
            pkLength=packages.length;
            for (int i = 0; i != packages.length; i++)
            {
                view.reset();
                PackageDoc pack = packages[i];
                view.addVariable("curitem", pack);
                String folder = getPkgFolder(pack);
                view.addVariable("curpage",folder+"package-summary.html");
                new File(OUT_ROOT + folder).mkdirs();
                File packFile = new File(OUT_ROOT + folder + "package-summary.html");
                System.out.println(packFile.getAbsolutePath());
                writer = new PrintWriter(packFile);
                html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "package.html");

                writer.print(html);
                writer.close();

                //package items pages
                ClassDoc[] items = view.getSortedClasses(pack);
                for (int j = 0; j != items.length; j++)
                {
                    view.reset();
                    ClassDoc item = items[j];
                    view.addVariable("curitem", item);
                    view.addVariable("curpage",folder + item.name()+".html");
                    File itemFile = new File(OUT_ROOT + folder + item.name()+".html");
                    System.out.println(itemFile.getAbsolutePath());
                    writer = new PrintWriter(itemFile);
                    String tpl="class.html";
                    if(item.isInterface())
                        tpl="interface.html";
                    html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + tpl);
                    writer.print(html);
                    writer.close();
                    itemsLength++;
                }
            }

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
        System.out.println("Processed "+itemsLength+ " in "+pkLength+" packages.");
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
}
