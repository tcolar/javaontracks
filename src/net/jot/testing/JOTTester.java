/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.testing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;
import java.util.Vector;

import net.jot.JOTInitializer;
import net.jot.utils.JOTUtilities;

/**
 * This is the Main class of the "Tester" <br>
 * The Tester is used to run all the test code you added in your own classes.<br>
 * You add test code to your own classes by implementing :<br>
 * 		public static void jotTest() throws Throwable {}<br>
 * 
 * You can run the tester either by using this class "Main method" or using the Ant task provided.<br>
 * Note: you could also run this class from your own java code if you wanted to.<br>
 * 
 * Call the -help option to get more infos. <br>
 * 
 * @author Thibaut Colar http://jot.colar.net/
 *
 */
public class JOTTester
{
    // contants
    /**
     * The name of the test method
     */
    protected static final String TEST_METHOD_NAME = "jotTest";
    /**
     * The Regular Expression to split classpathentries [:;]
     */
    private static final String SPLIT_REGEXP = "[:;]";
    private static final String OPTION_INCL_PK = "-includePackages=";
    private static final String OPTION_EXCL_PK = "-excludePackages=";
    private static final String OPTION_SOF = "-stopOnFailure=";
    private static final String OPTION_HELP = "-help";
    private static final String OPTION_DEBUG = "-debug";
    private static final String OPTION_OUTPUT_TO = "-outputTo=";
    private static final String OPTION_EMAIL_ALWAYS = "-emailAlways=";
    private static final String OPTION_EMAIL_FAILURE = "-emailOnFailure=";
    private static final String OPTION_SELFTEST = "-selfTest";
    private static final int OUTPUT_CONSOLE = 0;
    private static final int OUTPUT_TXT = 1;
    private static final int OUTPUT_HTML = 2;
    private static final int OUTPUT_TYPE_NONE = 0;
    private static final int OUTPUT_TYPE_SUCCESS = 1;
    private static final int OUTPUT_TYPE_ERROR = 2;    // variables
    private String currentClass = "";
    private int numberOfTests = 0;
    private int numberOfFailures = 0;
    private boolean enableDisplay = true;
    private boolean sof = true;
    private boolean selfTestMode = false;
    private boolean debug = false;
    private OutputStream outputTo = null;
    private int outputFormat = OUTPUT_CONSOLE;
    private String emailAlways = null;
    private String emailOnFailure = null;
    private String includePkgs = ".*";
    private String excludePkgs = null;

        //singleton
    private static JOTTester tester = new JOTTester();

    private JOTTester()
    {
    }

    public static JOTTester getInstance()
    {
        return tester;
    }

    /**
     * Enable/Disable display of messages (test results)
     */
    protected void setEnableDisplay(boolean enable)
    {
        enableDisplay = enable;
    }

    /**
     * Runs all the tests in all the classes in the given classpath, (filtered by packages) <br>
     *  
     * @param classpath   String: comma/semicolon separated list of classpath items (folders and jars/zips)  
     * @param packages    String: comma/semicolon separated list of packages to test (null = test all)
     * @param stopOnFailure  Wether to stop after a failure or not (run all tests) defaulted to no.
     * @return
     */
    public boolean runTests(String classpath)
    {
        boolean failure = false;

        String[] packages = includePkgs.split(SPLIT_REGEXP);
        String[] classDirs = classpath.split(SPLIT_REGEXP);

        Vector classes = new Vector();

        for (int j = 0; j != classDirs.length; j++)
        {
            classes.addAll(getTestableClasses(classDirs[j], packages, ""));
            debug("Testable classes: " + classes);
        }

        output("##### Starting tests. #####", OUTPUT_TYPE_NONE);
        for (int i = 0; i != classes.size() && !(sof && failure); i++)
        {
            boolean result = runTest((String) classes.get(i), sof);
            if (result == false)
            {
                failure = true;
            }
        }
        output("\n##### TESTS RAN: " + numberOfTests + " FAILURES:" + numberOfFailures + " #####", OUTPUT_TYPE_NONE);

        output("##### All Tests Completed.#####", OUTPUT_TYPE_NONE);

        if (outputTo != null)
        {
            try
            {
                if (getOutputFormat() == OUTPUT_HTML)
                {
                    outputTo.write("</body></html>".getBytes());
                    outputTo.flush();
                }
                outputTo.close();
            } catch (Exception e)
            {
            }
        }

        return !failure;
    }

    /**
     * Sercahes for classes that have implemented the test function
     * @param path
     * @param packages
     * @param currentPackage
     * @return
     */
    public Vector getTestableClasses(String path, String[] packages, String currentPackage)
    {
        debug("# Parsing package " + currentPackage);
        Vector v = new Vector();
        boolean validPackagePath = false;
        boolean validClassPath = false;
        for (int i = 0; i != packages.length && !validPackagePath; i++)
        {
            debug("Checking '" + currentPackage + "' vs " + packages[i]);

            if (packages[i].startsWith(currentPackage))
            {
                validPackagePath = true;
            }
            if (currentPackage.matches(packages[i]))
            {
                validPackagePath = true;
                validClassPath = true;
            }
        }

        // if we are in a folder, matching a listed package
        if (validPackagePath)
        {
            File[] files = new File(path).listFiles();
            for (int j = 0; j != files.length; j++)
            {
                File f = files[j];

                if (f.isDirectory())
                {
                    // recurse in the dir			   
                    path = f.getAbsolutePath();
                    v.addAll(getTestableClasses(path, packages, currentPackage + f.getName() + "."));
                } else
                {
                    // valid class package && file is a java class ?
                    if (validClassPath && f.getName().endsWith(".class"))
                    {
                        String className = f.getName().substring(0, f.getName().length() - 6);
                        // Class is Testable ?
                        if (isClassTestable(currentPackage + className) && !(selfTestMode == false && currentPackage.startsWith("net.jot")))
                        {
                            debug("** Adding class " + currentPackage + className);
                            v.add(currentPackage + className);
                        } else
                        {
                            debug("Skipping class " + currentPackage + className);
                        }
                    }
                }
            }
        } else
        {
            debug("Skipping path " + currentPackage);
        }

        return v;
    }

    public boolean isClassTestable(String className)
    {

        try
        {
            Class c = Class.forName(className);
            if (c.isInterface())
            {
                return false;
            }
            if (c.isInstance(JOTTestable.class))
            {
                return true;
            }
            Method m = c.getMethod(TEST_METHOD_NAME, (Class[]) null);
            debug("Testing class: " + className + m != null ? "succeeded" : "failed");

            return m != null;
        } catch (Exception e)
        {
            debug("Testing class: " + className + "failed");
            return false;
        }
    }

    /**
     * Run the test method(jotTest) of the given class
     * 
     * @param className
     * @param stopOnFailure
     * @return
     */
    protected boolean runTest(String className, boolean stopOnFailure)
    {
        currentClass = className;
        try
        {
            Class c = Class.forName(className);
            if (JOTTestable.class.isAssignableFrom(c))
            {
                // interface
                output("\n***** (I)" + className + " *****", OUTPUT_TYPE_NONE);
                ((JOTTestable) c.newInstance()).jotTest();
            } else
            {
                // static method
                Method m = c.getMethod(TEST_METHOD_NAME, (Class[]) null);
                output("\n***** (S)" + className + " *****", OUTPUT_TYPE_NONE);
                // call the classes test method ....
                m.invoke(null, (Object[]) null);
            }
        //TODO: ++ sometimes a nullpointerexception withing invoke doesn't get catched here ????
        } catch (InvocationTargetException invException)
        {
            Throwable t = invException.getTargetException();
            if (IllegalAccessException.class.isInstance(t) || ExceptionInInitializerError.class.isInstance(t))
            {
                // this is not a testable method .. ignore.
                debug(className + "is not testable");
            }
        }catch (Throwable t)
        {
            // unexpected Exception thrown by test code
            output("An unexpected Exception was Thrown !" + t.getMessage(), OUTPUT_TYPE_ERROR);
            t.printStackTrace();
            numberOfFailures++;
            // Hard failure, cannot continue
            sof = true;
        }
        debug("Nb Failures:" + numberOfFailures);
        return numberOfFailures == 0;
    }

    public static boolean checkIf(String message, boolean test) throws JOTTestException
    {
        return checkIf(message, test, "");
    }

    /**
     * Use this to run an individual check<br>
     * ie:   checkIf(myValue==3);
     * 
     * @param message     test title
     * @param test        test value
     * @return
     * @throws JOTTestException
     */
    public static boolean checkIf(String message, boolean test, String failureInfos) throws JOTTestException
    {
        tester.numberOfTests++;
        if (!test)
        {
            tester.numberOfFailures++;
            tester.displayResult(message + " (Infos: " + failureInfos + ")", test);
        } else
        {
            tester.displayResult(message, test);
        }
        return test;
    }

    /**
     * Ouputs a test result (system.out)<br>
     * Throws a JTOTestException if the test failed.
     * 
     * @param message
     * @param result
     * @throws JOTTestException
     */
    private void displayResult(String message, boolean result) throws JOTTestException
    {
        if (result)
        {
            if (enableDisplay)
            {
                output(message + " -> Success.", OUTPUT_TYPE_SUCCESS);
            }
        } else
        {
            if (enableDisplay)
            {
                output("!! " + message + " -> FAILED !!", OUTPUT_TYPE_ERROR);
            }
            if (sof)
            {
                throw new JOTTestException("Test '" + message + "' failed");
            }
        }
    }

    protected void output(String s, int outputType)
    {
        PrintWriter p = null;

        if (outputTo != null)
        {
            p = new PrintWriter(outputTo);
        }

        if (p != null && outputFormat == OUTPUT_TXT)
        {
            p.println(s);
        } else if (p != null && outputFormat == OUTPUT_HTML)
        {
            s = s.replaceAll("\n", "<br>");

            if (outputType == OUTPUT_TYPE_ERROR)
            {
                p.println("<font color=\"#660000\">" + s + "<br></font>");
            } else if (outputType == OUTPUT_TYPE_SUCCESS)
            {
                p.println("<font color=\"#006600\">" + s + "<br></font>");
            } else
            {
                p.println(s + "<br>");
            }
        } else
        {
            if (outputType == OUTPUT_TYPE_ERROR)
            {
                System.err.println(s);
            } else
            {
                System.out.println(s);
            }
        }

        if (p != null)
        {
            p.flush();
        }
    }

    /**
     * Use this to run an Exception test<br>
     * You should create a test method that throws the exception you want to test<br>
     * And call this to test the exception is indeed thrown <br>
     * 
     * ie: checkThrowsException(java.lang.NullPointerException.class,"myExceptionTestMethod",null);
     * 
     * @param exceptionType   Expected result Exception Class:   ie: java.lang.NullPointerException
     * @param method          Name of the test method that should throw the exception (to be tested)
     * @param args			Arguments of the test method (or null if none)
     * @return
     * @throws JOTTestException
     */
    public static boolean checkThrowsException(Class exceptionType, String method, Object[] args) throws JOTTestException
    {
        boolean result = false;
        tester.numberOfTests++;
        try
        {
            Class[] types = null;
            if (args != null)
            {
                types = new Class[args.length];
                for (int i = 0; i != args.length; i++)
                {
                    types[i] = args.getClass();
                }
            }
            Class c = Class.forName(tester.currentClass);
            Method m = c.getMethod(method, types);
            m.invoke(null, args);
        } catch (InvocationTargetException invException)
        {
            if (exceptionType.isInstance(invException.getTargetException()))
            {
                result = true;
            } else
            {
                tester.output("Wrong type of Exception thrown ! " + invException.getTargetException().getClass().toString(), OUTPUT_TYPE_ERROR);
                invException.getTargetException().printStackTrace();
            }
        } catch (Throwable t)
        {
            tester.output("Unexpected Exception thrown ! " + t.getMessage(), OUTPUT_TYPE_ERROR);
            t.printStackTrace();
        }

        if (!result)
        {
            tester.numberOfFailures++;
        }

        tester.displayResult("Testing that " + exceptionType.getName() + " is thrown by " + tester.currentClass + "." + method, result);
        return result;
    }

    /**
     * Main method to be called from the command line<br>
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            tester.displayHelp();
            return;
        }
        String classpath = args[0];
        for (int i = 1; i != args.length; i++)
        {
            String arg = args[i];
            if (arg != null)
            {
                if (arg.startsWith(OPTION_HELP))
                {
                    tester.displayHelp();
                    return;
                }
                if (arg.startsWith(OPTION_DEBUG))
                {
                    tester.setDebug(true);
                }
                if (arg.startsWith(OPTION_EMAIL_ALWAYS))
                {
                    String email = arg.substring(OPTION_EMAIL_ALWAYS.length(), arg.length());
                    if (!JOTUtilities.checkEmail(email))
                    {
                        System.err.println("Invalid email address");
                        return;
                    }
                    tester.setEmailAlways(email);
                }
                if (arg.startsWith(OPTION_EMAIL_FAILURE))
                {
                    String email = arg.substring(OPTION_EMAIL_FAILURE.length(), arg.length());
                    if (!JOTUtilities.checkEmail(email))
                    {
                        System.err.println("Invalid email address");
                        return;
                    }
                    if (tester.emailAlways != null)
                    {
                        System.err.println("Use " + OPTION_EMAIL_ALWAYS + " OR " + OPTION_EMAIL_FAILURE);
                        return;
                    }
                    tester.setEmailOnFailure(email);
                }
                if (arg.startsWith(OPTION_EXCL_PK))
                {
                    String packages = arg.substring(OPTION_EXCL_PK.length(), arg.length());
                    tester.setExcludePkgs(packages);
                }
                if (arg.startsWith(OPTION_INCL_PK))
                {
                    if (tester.excludePkgs != null)
                    {
                        System.err.println("Use " + OPTION_EXCL_PK + " OR " + OPTION_INCL_PK);
                        return;
                    }
                    String packages = arg.substring(OPTION_EMAIL_FAILURE.length(), arg.length());
                    tester.setIncludePkgs(packages);
                }
                if (arg.startsWith(OPTION_OUTPUT_TO))
                {
                    String file = arg.substring(OPTION_OUTPUT_TO.length(), arg.length());
                    File f = new File(file);
                    if (f.getParentFile().exists() && f.getParentFile().isDirectory())
                    {
                        tester.setOutputTo(file);
                    } else
                    {
                        System.err.println("Output file " + file + " is not valid");
                        return;
                    }
                }
                if (arg.startsWith(OPTION_SELFTEST))
                {
                    tester.output("#########SELTEST#######", OUTPUT_TYPE_NONE);
                    tester.setSelfTestMode(true);
                }
                if (arg.startsWith(OPTION_SOF))
                {
                    String sof = arg.substring(OPTION_SELFTEST.length(), arg.length());
                    tester.setStopOnFailure(!sof.equalsIgnoreCase("false"));
                }
            }
        }

        try
        {
            tag("--- Initializing JOT env.---");
            JOTInitializer initializer=JOTInitializer.getInstance();
            initializer.setTestMode(true);
            initializer.init();
            tag("--- Starting tests.---");
            tester.runTests(classpath);
            tag("--- Shutting down JOT env.---");
            JOTInitializer.getInstance().destroy();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void displayHelp()
    {
        System.out.println("JOTTester help: \n");
        System.out.println("Syntax: java JOTTester classpath  <options>\n");
        System.out.println("Classpath: column or semiC. separated list of class folders.\n\n");
        System.out.println("\tOption: -includePackages=\"\"\n");
        System.out.println("\t\t Column or semiC. List of packages that should be tested (all others will NOT)\n");
        System.out.println("\tOption: -excludePackages=\"\"\n");
        System.out.println("\t\t Column or semiC. separated List of packages that should NOT be tested (all others WILL)\n");
        System.out.println("\tOption: -debug\n");
        System.out.println("\t\t debug or not\n");
        System.out.println("\tOption: -stopOnFailure=\n");
        System.out.println("\t\t Stop testing after a failure: true or false\n");
        System.out.println("\tOption: -help\n");
        System.out.println("\t\t Display this help message\n");
        System.out.println("\tOption: -outputTo=\"\"\n");
        System.out.println("\t\t Path to a file where you want to output the results (.txt or .html) default: console\n");
        System.out.println("\tOption: -emailAlways=\"\"\n");
        System.out.println("\t\t email address to which you want to send the result\n");
        System.out.println("\tOption: -emailOnFailure=\"\"\n");
        System.out.println("\t\t email address to which you want to send the result (only on failure)\n");
        System.out.println("\tOption: -selfTest\n");
        System.out.println("\t\t Also test the Testing API itself.\n\n");
        System.out.println("Example: java JOTTEster \"classes;c:\\my_classes\" -includePackages=\"com.mypkg.*;com.otherpkg.*\" -debug=true \n\n");
        System.out.println("See http://jot.colar.net/ for more informations.");
    }

    protected int getNumberOfFailures()
    {
        return numberOfFailures;
    }

    protected int getNumberOfTests()
    {
        return numberOfTests;
    }

    protected void setNumberOfFailures(int numberOfFailures)
    {
        this.numberOfFailures = numberOfFailures;
    }

    protected void setNumberOfTests(int numberOfTests)
    {
        this.numberOfTests = numberOfTests;
    }

    private void debug(String s)
    {
        if (debug)
        {
            System.out.println("DEBUG: " + s);
        }
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public String getEmailAlways()
    {
        return emailAlways;
    }

    public void setEmailAlways(String emailAlways)
    {
        this.emailAlways = emailAlways;
    }

    public String getEmailOnFailure()
    {
        return emailOnFailure;
    }

    public void setEmailOnFailure(String emailOnFailure)
    {
        this.emailOnFailure = emailOnFailure;
    }

    public String getExcludePkgs()
    {
        return excludePkgs;
    }

    public void setExcludePkgs(String excludePkgs)
    {
        this.excludePkgs = excludePkgs;
    }

    public String getIncludePkgs()
    {
        return includePkgs;
    }

    public void setIncludePkgs(String includePkgs)
    {
        this.includePkgs = includePkgs;
    }

    public int getOutputFormat()
    {
        return outputFormat;
    }

    public void setOutputFormat(int outputFormat)
    {
        this.outputFormat = outputFormat;
    }

    public boolean isSelfTestMode()
    {
        return selfTestMode;
    }

    public void setSelfTestMode(boolean selfTestMode)
    {
        this.selfTestMode = selfTestMode;
    }

    public boolean isSof()
    {
        return sof;
    }

    public void setStopOnFailure(boolean sof)
    {
        this.sof = sof;
    }

    public OutputStream getOutputTo()
    {
        return outputTo;
    }

    public void setOutputTo(String file)
    {
        GregorianCalendar cal = new GregorianCalendar();
        String now = cal.get(GregorianCalendar.MONTH) + "_";
        now += cal.get(GregorianCalendar.DAY_OF_MONTH) + "_";
        now += cal.get(GregorianCalendar.YEAR) + "__";
        now += cal.get(GregorianCalendar.HOUR_OF_DAY) + "_";
        now += cal.get(GregorianCalendar.MINUTE);

        String filename = null;
        if (file.endsWith(".txt"))
        {
            filename = file.substring(0, file.lastIndexOf(".txt")) + "_" + now + ".txt";
            setOutputFormat(OUTPUT_TXT);
        }
        if (file.endsWith(".html"))
        {
            filename = file.substring(0, file.lastIndexOf(".html")) + "_" + now + ".html";
            setOutputFormat(OUTPUT_HTML);
        }
        if (filename != null)
        {
            try
            {
                outputTo = new FileOutputStream(filename);
                if (getOutputFormat() == OUTPUT_HTML)
                {
                    outputTo.write("<html><body>".getBytes());
                    outputTo.flush();
                }
            } catch (Exception e)
            {
                debug("Failed creating outputSream for: " + filename);
            }
        }
        debug("outputTo: " + filename);

    }

    /**
     * Lets the user output a custom text (tag)
     * @param tag
     */
    public static void tag(String tag)
    {
        getInstance().output("\n===== " + tag + " =====", OUTPUT_TYPE_NONE);
    }
}
