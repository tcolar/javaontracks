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
import java.io.FileOutputStream;
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

    public static RootDoc rootDoc = null;
    private boolean navOnly = false;
    public static String RES_ROOT = "/home/thibautc/NetBeansProjects/javaontracks/resources/doclet/";
    public static String OUT_ROOT = "/tmp/";
    public ConfigurationImpl configuration = ConfigurationImpl.getInstance();
    public HtmlDocletWriter docWriter;
    public ClassTree classTree = null;

    public static boolean start(RootDoc root)
    {
        JOTDoclet doclet = new JOTDoclet();
        rootDoc = root;
        return doclet.start(doclet, root);
    }

    public boolean start(JOTDoclet doclet, RootDoc root)
    {
        String[][] options = root.options();
        for (int i = 0; i != options.length; i++)
        {
            //System.out.println(options[i][0]);
            if (options[i][0].equalsIgnoreCase("-navOnly"))
            {
                navOnly = true;
            }
        }
        configuration.root = root;

        configuration.setOptions();
        configuration.getDocletSpecificMsg().notice("doclet.build_version",
                configuration.getDocletSpecificBuildDate());

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
        view.addVariable("sourceEnabled", new Boolean(configuration.linksource));
    }

    private void copyResources()
    {
        try
        {
            File dest = new File(OUT_ROOT);
            File src = new File(RES_ROOT);
            dest.mkdirs();
            JOTUtilities.copyFolderContent(dest, src, true);
            JOTUtilities.deleteFolder(new File(dest.getAbsolutePath()+File.separator+"tpl"));
            JOTUtilities.deleteFolder(new File(dest.getAbsolutePath()+File.separator+".svn"));
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

    private void generateIndex() throws Exception
    {
        JOTDocletIndexWriter index = new JOTDocletIndexWriter(configuration, docWriter);
        String templatePath = RES_ROOT + "tpl";
        String templateName = "indexing.html";
        File folder = new File(OUT_ROOT);
        JOTDocletNavView view = new JOTDocletNavView(docWriter);
        view.addVariable("docTitle", configuration.doctitle);
        view.addVariable("windowTitle", configuration.windowtitle);
        view.addVariable("jotversion", JOTInitializer.VERSION);
        view.addVariable("splitindex", new Boolean(configuration.splitindex));
        view.addVariable("manualPath", "../");
        new File(folder+"/index-files").mkdirs();

        if (! configuration.splitindex)
        {
            File f = new File(folder+"/index-files", "index-all.html");
            view.addVariable("curpage", "index-files/index-all.html");
            System.out.println(f.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(f);
            String page = index.generateHtml(null);
            view.addVariable("indexHtml", page);
            page = JOTViewParser.parseTemplate(view, templatePath, templateName);
            fos.write(page.getBytes());
            fos.close();
        } else
        {
            Object[] cars = index.getCharactersAsObjects();
            view.addVariable("indexchars", cars);
            for (int i = 0; i != cars.length; i++)
            {
                Character c=(Character)cars[i];
                view.addVariable("curchar", c);
                int nb=(c.charValue()-('A'-1));
                if(c.equals(new Character('_')))
                    nb=27;
                if(c.equals(new Character('$')))
                    nb=28;
                File f = new File(folder+"/index-files", "index-" + nb + ".html");
                view.addVariable("curpage", "index-files/index-"+nb+".html");
                System.out.println(f.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(f);
                String page = index.generateHtml(c);
                view.addVariable("indexHtml", page);
                page = JOTViewParser.parseTemplate(view, templatePath, templateName);
                fos.write(page.getBytes());
                fos.close();
            }
        }
        view.getVariables().remove("manualPath");
    }

    private void startGeneration(RootDoc root) throws Exception
    {
        if (root.classes().length == 0)
        {
            configuration.message.error("doclet.No_Public_Classes_To_Document");
            return;
        }
        classTree = new ClassTree(configuration, configuration.nodeprecated);

        OUT_ROOT = configuration.docFileDestDirName;
        new File(OUT_ROOT).mkdirs();
        System.out.println(OUT_ROOT);
        String[] levels =
        {
            "" + JOTLogger.CRITICAL_LEVEL, "" + JOTLogger.ERROR_LEVEL, "" + JOTLogger.WARNING_LEVEL
        };
        JOTLogger.init(OUT_ROOT + File.separator + "jotdoclet.log", levels, null);

        copyResources();

        generateNav(classTree);

        if (!navOnly)
        {
            generateIndex();
            generatePackageList(classTree);
        // only save sitemap if !navonly
        } else
        {
            System.out.println("Done (packOnly requested.)");
        }

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

    protected void generateNav(ClassTree tree) throws Exception
    {
        int nb = 1;
        String cpt = "";
        if (new File((OUT_ROOT + "index.html")).exists())
        {
            while (new File(OUT_ROOT + "index" + nb + ".html").exists())
            {
                nb++;
            }
            cpt = "" + nb;
        }
        JOTDocletNavView view = new JOTDocletNavView(docWriter);
        view.addVariable("nav", "overview-frame" + cpt + ".html");
        // write index
        System.out.println("Index: " + OUT_ROOT + "index" + cpt + ".html");
        String html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "index.html");
        PrintWriter writer = new PrintWriter(OUT_ROOT + "index" + cpt + ".html");
        writer.write(html);
        writer.close();
        PackageDoc[] packages = configuration.packages;
        Arrays.sort(packages);
        File navigator = new File(OUT_ROOT + "overview-frame" + cpt + ".html");

        // writing navigator
        writer = new PrintWriter(navigator);
        System.out.println("Navigator: " + navigator.getAbsolutePath());
        addViewConstants(view);
        view.addVariable(JOTDocletNavView.PACKAGES, packages);
        html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "nav.html");
        writer.print(html);
        writer.close();
    }

    protected void generatePackageList(ClassTree tree) throws Exception
    {
        JOTDocletJava2HTML htmlEncoder = new JOTDocletJava2HTML(new File(OUT_ROOT), this);

        File packTree = new File(OUT_ROOT + "overview-summary.html");
        File packList = new File(OUT_ROOT + "package-list");
        PrintWriter writer = null;
        int pkLength = 0;
        int itemsLength = 0;
        try
        {
            // standard sucks out a lot of memory to create package list.
            System.gc();

            PackageDoc[] packages = configuration.packages;
            Arrays.sort(packages);
            JOTDocletNavView view = new JOTDocletNavView(docWriter);
            addViewConstants(view);

            // writing package list (used by -link javadoc option)
            System.out.println(packList.getAbsolutePath());
            writer = new PrintWriter(packList);
            for (int i = 0; i != packages.length; i++)
            {
                writer.println(packages[i].name());
            }
            writer.close();

            // write package tree
            view.reset();
            System.out.println(packTree.getAbsolutePath());
            view.addVariable("curitem", configuration.root);
            view.addVariable("curpage", "overview-summary.html");
            writer = new PrintWriter(packTree);
            String html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "packages.html");
            writer.print(html);
            writer.close();

            // write individual package pages
            pkLength = packages.length;
            for (int i = 0; i != packages.length; i++)
            {
                view.reset();
                PackageDoc pack = packages[i];
                view.addVariable("curitem", pack);
                String folder = getPkgFolder(pack);
                view.addVariable("curpage", folder + "package-summary.html");
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
                    view.addVariable("curpage", folder + item.name() + ".html");
                    File itemFile = new File(OUT_ROOT + folder + item.name() + ".html");
                    System.out.println(itemsLength + " " + itemFile.getAbsolutePath());
                    writer = new PrintWriter(itemFile);
                    String tpl = "class.html";
                    if (item.isInterface())
                    {
                        tpl = "interface.html";
                    } else if (item.isEnum())
                    {
                        tpl = "enum.html";
                    } else if (item.isAnnotationType())
                    {
                        tpl = "annot.html";
                    }
                    try
                    {
                        html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + tpl);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    writer.print(html);
                    writer.close();

                    if (configuration.linksource)
                    {
                        // write source file
                        String name = item.name();
                        // if "." in name is subclass, no need to write again.
                        if (name.indexOf(".") < 0)
                        {
                            File sourceFile = new File(OUT_ROOT + folder + item.name() + "-source.html");
                            System.out.println(sourceFile.getAbsolutePath());
                            writer = new PrintWriter(sourceFile);
                            File srcFile = new File(JOTUtilities.endWithSlash(configuration.sourcepath), folder + name + ".java");
                            String source = htmlEncoder.encodeFile(srcFile).toString();
                            //source=doCrossLinks(source);
                            view.addVariable("curpage", folder + item.name() + "-source.html");
                            view.addVariable("source", source.split("\n"));
                            try
                            {
                                html = JOTViewParser.parseTemplate(view, RES_ROOT, "tpl" + File.separator + "source.html");
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            writer.print(html);
                            writer.close();
                        }
                    }

                    itemsLength++;
                    if (itemsLength % 100 == 0)
                    {
                        System.out.println("Doing garbage collection");
                        System.out.println("Before " + Runtime.getRuntime().freeMemory());
                        System.gc();
                        System.out.println("After " + Runtime.getRuntime().freeMemory());
                    }
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
        System.out.println("Processed " + itemsLength + " items in " + pkLength + " packages.");
    }

    public static boolean validOptions(String options[][],
            DocErrorReporter reporter)
    {
        (ConfigurationImpl.getInstance()).validOptions(options, reporter);
        return true;
    }

    public static int optionLength(String option)
    {
        if (option.equalsIgnoreCase("-navOnly"))
        {
            return 1;
        }
        // Construct temporary configuration for check
        return ConfigurationImpl.getInstance().optionLength(option);
    }

    protected void generatePackageFiles(ClassTree arg0) throws Exception
    {
    }

    protected void generateClassFiles(ClassDoc[] arg0, ClassTree arg1)
    {
    }
}
