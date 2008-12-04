/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.doclet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jot.logger.JOTLoggerLocation;
import net.jot.utils.JOTHTMLUtilities;
import net.jot.utils.JOTUtilities;
import net.jot.web.view.JOTViewParser;

/**
 * Converts java code file into an easier to read HTML version
 * @author thibautc
 */
public class JOTDocletJava2HTML
{

    private File destFolder;
    private final JOTLoggerLocation loc = new JOTLoggerLocation(getClass());
    private final static Pattern PATTERN_PACK = Pattern.compile("^\\s*package\\s+(\\S+)", Pattern.MULTILINE);
    private final static Pattern PATTERN_COMMENTS = Pattern.compile("/\\*[^*].*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
    private final static Pattern PATTERN_COMMENTS_1LINER = Pattern.compile("//.*");
    private final static Pattern PATTERN_JAVADOC = Pattern.compile("/\\*\\*.*?\\*/", Pattern.MULTILINE | Pattern.DOTALL);
    private final static Pattern PATTERN_PIECES = Pattern.compile("__JOT_PIECE_\\d+__");
    private final static Pattern PATTERN_STRING = Pattern.compile("&quot;.*?&quot;");
    private final static Pattern PATTERN_ANNOTATIONS = Pattern.compile("^\\s*(@.*?)$", Pattern.MULTILINE);
    private final static Pattern PATTERN_NUMBER = Pattern.compile("(\\W)([\\-0-9]+[fFlL]?)");
    private final static Pattern PATTERN_FUNCTION = Pattern.compile("(\\S+)(\\s*\\([^)]*\\)\\s*(\\{|throws))", Pattern.MULTILINE | Pattern.DOTALL);
    private final static String[] KEYWORDS =
    {
        "abstract", "assert", "boolean", "break", "byte", "case",
        "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
        "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
        "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
        // not technically keywords ... but same idea
        "true", "false", "null"
    };
    private Hashtable pieces = new Hashtable();

    public JOTDocletJava2HTML(File destFolder)
    {
        this.destFolder = destFolder;
    }

    public StringBuffer encodeFile(File javaFile)
    {
        StringBuffer buf=null;
        try
        {
            buf = readJavaFile(javaFile);
            buf=buf.insert(0, "\n");
            String pack = findPackage(buf);
            String itemName = javaFile.getName().substring(0, javaFile.getName().toLowerCase().lastIndexOf(".java"));
            buf = encodeHtml(buf);
            buf = doComments(buf);
            buf = doJavadoc(buf);
            buf = doStrings(buf);
            buf = doAnnotations(buf);
            buf = doNumbers(buf);
            buf = doFunctionNames(buf);
            for (int i = 0; i != KEYWORDS.length; i++)
            {
                buf = doKeyword(buf, KEYWORDS[i]);
            }
            buf=new StringBuffer(buf.toString().replaceAll("("+itemName+")", "<b>$1</b>"));
            buf = reinsertPieces(buf);
            buf=buf.deleteCharAt(0);
            //File destFile = computeDestFile(pack, itemName);
            //writeHTMLFile(destFile, buf);
        } catch (Exception e)
        {
            loc.exception("Failed encoding the java code file " + javaFile.getAbsolutePath(), e);
            e.printStackTrace();
        }
        return buf;
    }

    private File computeDestFile(String pack, String itemName)
    {
        String folder = JOTUtilities.endWithSlash(destFolder.getAbsolutePath());
        String[] packs = pack.split("\\.");
        for (int i = 0; i != packs.length; i++)
        {
            folder += packs[i] + File.separator;
        }
        return new File(folder, itemName + ".html");
    }

    private StringBuffer doAnnotations(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_ANNOTATIONS.matcher(buf);
        while (m.find())
        {
            String key = insertPiece("<font color='#993300'>" + m.group(1) + "</font>");
            JOTViewParser.safeAppendReplacement(m, newBuf, key);
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private StringBuffer doComments(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_COMMENTS.matcher(buf);
        while (m.find())
        {
            String key = insertPiece("<font color='#666666'>" + m.group() + "</font>");
            JOTViewParser.safeAppendReplacement(m, newBuf, key);
        }
        m.appendTail(newBuf);
        StringBuffer newBuf2 = new StringBuffer();
        m = PATTERN_COMMENTS_1LINER.matcher(newBuf);
        while (m.find())
        {
            String key = insertPiece("<font color='#666666'>" + m.group() + "</font>");
            JOTViewParser.safeAppendReplacement(m, newBuf2, key);
        }
        m.appendTail(newBuf2);
        return newBuf2;
    }

    private StringBuffer doFunctionNames(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_FUNCTION.matcher(buf);
        while (m.find())
        {
            String name = m.group(1);
            boolean skip = false;
            for (int i = 0; i != KEYWORDS.length && !skip; i++)
            {
                if (KEYWORDS[i].equals(name))
                {
                    skip = true;
                }
            }
            if (skip == false)
            {
                String key = insertPiece("<b>" + name + "</b>" + m.group(2));
                JOTViewParser.safeAppendReplacement(m, newBuf, key);
            }
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private StringBuffer doJavadoc(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_JAVADOC.matcher(buf);
        while (m.find())
        {
            String text = m.group();
            text = text.replaceAll("(@\\S+)", "<b>$1</b>");
            String key = insertPiece("<font color='#4444FF'>" + text + "</font>");
            JOTViewParser.safeAppendReplacement(m, newBuf, key);
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private StringBuffer doKeyword(StringBuffer buf, String keyword)
    {
        StringBuffer newBuf = new StringBuffer();
        Pattern p = Pattern.compile("(\\W+)(" + keyword + ")(\\W+)");
        Matcher m = p.matcher(buf);
        while (m.find())
        {
            JOTViewParser.safeAppendReplacement(m, newBuf, m.group(1) + "<font color='#880088'><b>" + m.group(2) + "</b></font>" + m.group(3));
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private StringBuffer doNumbers(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_NUMBER.matcher(buf);
        while (m.find())
        {
            String key = insertPiece(m.group(1) + "<font color='#CC0000'>" + m.group(2) + "</font>");
            JOTViewParser.safeAppendReplacement(m, newBuf, key);
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private StringBuffer doStrings(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_STRING.matcher(buf);
        while (m.find())
        {
            String key = insertPiece("<font color='#008800'>" + m.group() + "</font>");
            JOTViewParser.safeAppendReplacement(m, newBuf, key);
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private StringBuffer encodeHtml(StringBuffer buf)
    {
        String newText = JOTHTMLUtilities.textToHtml(buf.toString(), JOTHTMLUtilities.ENCODE_HTML_CHARS);
        return new StringBuffer(newText);
    }

    private String findPackage(StringBuffer buf) throws Exception
    {
        Matcher m = PATTERN_PACK.matcher(buf);
        if (m.find())
        {
            String pack = m.group(1);
            if (pack.endsWith(";"))
            {
                pack = pack.substring(0, pack.length() - 1);
            }
            return pack;
        } else
        {
            throw (new Exception("Package definition not found !"));
        }
    }

    private String insertPiece(String newText)
    {
        String key = "__JOT_PIECE_" + pieces.size() + "__";
        pieces.put(key, newText);
        return key;
    }

    private StringBuffer readJavaFile(File javaFile) throws IOException
    {
        int length = (int) javaFile.length();
        byte[] buffer = new byte[length];
        DataInputStream dis = new DataInputStream(new FileInputStream(javaFile));
        dis.readFully(buffer);
        dis.close();
        StringBuffer str = new StringBuffer(new String(buffer));
        return str;
    }

    private StringBuffer reinsertPieces(StringBuffer buf)
    {
        StringBuffer newBuf = new StringBuffer();
        Matcher m = PATTERN_PIECES.matcher(buf);
        while (m.find())
        {
            String value = (String) pieces.get(m.group());
            JOTViewParser.safeAppendReplacement(m, newBuf, value);
        }
        m.appendTail(newBuf);
        return newBuf;
    }

    private void writeHTMLFile(File destFile, StringBuffer buf) throws IOException
    {
        destFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(destFile);
        buf.insert(0, "<html><body><pre>");
        buf.append("</pre></body></html>");
        fos.write(buf.toString().getBytes());
        fos.close();
    }

    public static void main(String[] args)
    {
        JOTDocletJava2HTML encoder = new JOTDocletJava2HTML(new File("/tmp"));
        encoder.encodeFile(new File("/tmp/JOTDoclet.java"));
    }
}
