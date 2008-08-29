/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.search.simpleindexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jot.logger.JOTLogger;
import net.jot.prefs.JOTPropertiesPreferences;
import net.jot.utils.JOTUtilities;

/**
 * Implement a simple search engine using a text/keyword index
 * Use(or extend) to index/search plain text pfiles.
 * This intends to be 'barebone' and decoupled from teh ui part of presesnting the results. 
 * 
 * @author thibautc
 */
public class JOTSimpleSearchEngine
{

    /** index property file */
    public File indexRoot = null;
    public JOTPropertiesPreferences props = new JOTPropertiesPreferences();
    protected File propFile = null;
    /**
     *  Pattern matching "words"
     *  a single word is considered any letter or number (unicode case insensitive)
     *  as well as - and _
     */
    protected static Pattern pattern = Pattern.compile("[\\p{L}\\p{N}_\\-]+");
    JOTIndexHandler indexHandler = null;
    protected static JOTSearchSorter defaultSorter = new JOTDefaultSearchSorter();
    /**
     * Max words to process in memory before writing to file
     * Too low, and performance will be slower
     * Too high and it will use more memory.
     */
    protected int WORD_BATCH_SIZE = 2500;

    /**
     * 
     * @param indexRoot: root folder where the index data is/will go (empty folder)
     * @throws java.lang.Exception
     */
    public JOTSimpleSearchEngine(File indexRoot) throws Exception
    {
        this.indexRoot = indexRoot;
        indexRoot.mkdirs();
        propFile = new File(indexRoot, "index.properties");
        if (!propFile.exists())
        {
            propFile.createNewFile();
        }
        props.loadFrom(propFile);
        indexHandler = new JOTIndexHandler(indexRoot);
    }

    /**
     * Index the file using the filepath as the unique key, and only reindexing if file timestamp was updated
     * @param textFile
     * @return
     * @throws java.lang.Exception
     */
    public int indexFile(File textFile) throws Exception
    {
        return indexFile(textFile, null, true);
    }

    /**
     * Index the file using the filepath as the unique key
     * @param textFile
     * @param onlyIfModified if true only update if file timestamp chnaged since last indexing
     * @return
     * @throws java.lang.Exception
     */
    public int indexFile(File textFile, boolean onlyIfModified) throws Exception
    {
        return indexFile(textFile, null, onlyIfModified);
    }

    /**
     * Index the file, only if the timestamp chnaged since the last indexing.
     * @param textFile
     * @param uniqueId: a unique id for the file, ie: absolutepath, md5 etc .... if null absolutepath will be used.
     * @return
     * @throws java.lang.Exception
     */
    public int indexFile(File textFile, String uniqueId) throws Exception
    {
        return indexFile(textFile, uniqueId, true);
    }

    /**
     * index a file(update if already indexed)
     * @param textFile
     * @param onlyIfModified if true only update the file if file timestamp changed since last indexing
     * @param uniqueId a unique id for the file, ie: absolutepath, md5 etc .... if null absolutepath will be used.
     * @return number of new keywords added to Index
     */
    public int indexFile(File textFile, String uniqueId, boolean onlyIfModified) throws Exception
    {
        int newKeywords = 0;
        if (uniqueId == null)
        {
            uniqueId = textFile.getAbsolutePath();
        }
        // check timetamp if(onlyIfModified())
        boolean newKey = indexHandler.isNewKey(uniqueId);
        boolean newTimestamp = textFile.lastModified() > indexHandler.getEntryStamp(uniqueId);

        if (newKey)
        {
            // get a new id
            int id = -1;
            synchronized (this)
            {
                Integer curId = props.getDefaultedInt("nextId", new Integer(1));
                id = curId.intValue();
                props.setString("nextId", "" + (id + 1));
                props.saveTo(propFile);
            }
            if (id != -1)
            {
                indexHandler.addMasterEntry(id, uniqueId, textFile.lastModified());
            }
        }

        String id = indexHandler.getMasterIdByKey(uniqueId);

        if (!onlyIfModified || newTimestamp)
        {
            JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Adding to search index: " + uniqueId);
            if (!newKey)
            {
                // updated file, need to remove the current version from index
                indexHandler.removeEntries(uniqueId);
            }

            String s = null;
            int totalWords = 0;
            int totalKeywords = 0;
            int wordCount = 0;
            int lineCpt = 1;
            Hashtable hash = new Hashtable();
            BufferedReader reader = new BufferedReader(new FileReader(textFile));
            try
            {
                while ((s = reader.readLine()) != null)
                {
                    wordCount += indexLineInMemory(hash, "" + lineCpt, s);
                    if (wordCount > WORD_BATCH_SIZE)
                    {
                        totalWords += wordCount;
                        wordCount = 0;
                        totalKeywords += hash.size();
                        newKeywords += commitFromMemory(id, hash);
                        hash.clear();
                    }
                    lineCpt++;
                }
            } catch (Exception e)
            {
                throw (e);
            } finally
            {
                reader.close();
            }
            totalWords += wordCount;
            totalKeywords += hash.size();
            newKeywords += commitFromMemory(id, hash);

        }
        return newKeywords;
    }

    /**
     * Writes the temporary -in memory- hash to the index files.
     * @param hash
     * @param uniqueId
     * @return numberOfNewKeywords
     */
    protected int commitFromMemory(String id, Hashtable hash) throws Exception
    {
        int nbNewKeywords = 0;
        //System.out.println("Commiting "+hash.size()+" keywords for "+id);
        Enumeration e = hash.keys();
        while (e.hasMoreElements())
        {
            String word = (String) e.nextElement();
            Vector v = (Vector) hash.get(word);
            boolean newK = indexHandler.indexKeyword(id, word, v);
            if (newK)
            {
                nbNewKeywords++;
            }
        }
        return nbNewKeywords;
    }

    /** 
     * mem is the hashtable storing the keyword data. (keyword -> Vector(lineNumber(String)))
     * index one line of text 
     * return number of words found in the line.
     **/
    protected int indexLineInMemory(Hashtable hash, String lineNb, String s)
    {
        int cpt = 0;
        Matcher m = pattern.matcher(s);
        while (m.find())
        {
            String word = m.group().toLowerCase();
            if (word.length() >= 3)
            {
                // add the keyword to in memory hash
                Vector v = null;
                if (hash.containsKey(word))
                {
                    v = (Vector) hash.get(word);
                } else
                {
                    v = new Vector();
                }
                v.add(lineNb);
                hash.put(word, v);
            }
            cpt++;
        }
        return cpt;
    }

    /**
     * remove a file from the index
     * @param textFile
     * @param uniqueId the unique id for the file(used in indexFile), ie: absolutepath, md5 etc .... if null absolutepath will be used.
     * @return number of keywords removed from Index
     */
    public int removeFile(File textFile, String uniqueId) throws Exception
    {
        if (uniqueId == null)
        {
            uniqueId = textFile.getAbsolutePath();
        }
        JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Removing from search index: " + uniqueId);

        indexHandler.removeEntries(uniqueId);
        indexHandler.removeMasterEntry(uniqueId);

        return 0;
    }

    protected void updateKeywordsCount(int nbNewKeywords) throws Exception
    {
        int keywords = props.getDefaultedInt("keywords", new Integer(0)).intValue();
        keywords += nbNewKeywords;
        props.setString("keywords", "" + keywords);
        props.saveTo(propFile);
    }

    /**
     * completely whipeout the index, so you can reindex from scratch
     * Simply deletes everyhting in the indexRoot folder !
     */
    public static void whipeoutIndex(File indexRoot)
    {
        JOTUtilities.deleteFolderContent(indexRoot);
    }

    /**
     * return sorted list of files(uniqueIds) and score (1-5)
     * @param keywords
     * @return
     */
    public JOTSearchResult[] performSearch(String[] keywords, JOTSearchSorter sorter) throws Exception
    {
        if(sorter==null)
            sorter=new JOTDefaultSearchSorter();
        JOTRawSearchResult[] rawResults = performRawSearch(keywords);
        return sorter.sortResults(rawResults);
    }

    /**
     * Utility method to parse a user typed query (ex: "a java server   pAGes ") into keywords
     * ex: [java,server,pages]
     * @param qeryString
     * @return
     */
    public static String[] parseQueryIntoKeywords(String queryString)
    {
        String[] pass1=queryString.trim().toLowerCase().split(" ");
        Vector v=new Vector();
        for(int i=0;i!=pass1.length;i++)
        {
            if(pass1[i].length()>=3)
                v.add(pass1[i]);
        }
        return (String[])v.toArray(new String[0]);
    }

    /**
     * return an array of rawSearchResults (one rawsearchresult per keyword, in the same order as the keywords).
     * @param keywords: keywords should be space separated: ie: "java server pages"
     * @return
     */
    public JOTRawSearchResult[] performRawSearch(String[] keywords) throws Exception
    {
        // return array, for each keyword, list of {file/line} where found.
        JOTRawSearchResult[] results = new JOTRawSearchResult[keywords.length];
        for (int i = 0; i != results.length; i++)
        {
            String keyword = keywords[i];
            // lookup the index file for the line
            String line = "";
            if (keyword.length() >= 3)
            {
                line = indexHandler.findKeywordIndexLine(keyword);
            }
            results[i] = new JOTRawSearchResult(indexHandler, keyword, line);
        }
        return results;
    }

    /**
     * for testing / Example
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            //JOTSimpleSearchEngine.whipeoutIndex(new File("/tmp/index/"));
            JOTSimpleSearchEngine engine = new JOTSimpleSearchEngine(new File("/tmp/index/"));
            File fol = new File("/opt/jotwiki/data/default/pages/");
            File[] files = fol.listFiles();
            for (int i = 0; i != files.length; i++)
            {
                if (files[i].isFile())
                {
                    int nbkeyw = engine.indexFile(files[i]);
                    System.out.println(files[i].getAbsolutePath() + " : new keywords:" + nbkeyw);
                }
            }
            // test remov e file
            engine.removeFile(new File("/opt/jotwiki/data/default/pages/vpn_tips.txt"), null);
            // test raw search query
            String query = "  java sap track nwdi";
            String[] keywords = engine.parseQueryIntoKeywords(query);
            JOTRawSearchResult[] results = engine.performRawSearch(keywords);
            for (int i = 0; i != results.length; i++)
            {
                String keyword = results[i].getKeyword();
                String[] keys = results[i].getMatchingIds();
                for (int j = 0; j != keys.length; j++)
                {
                    Integer[] lines = results[i].getResultsForId(keys[j]);
                    String lns = "";
                    for (int k = 0; k != lines.length; k++)
                    {
                        lns += lines[k].toString() + ",";
                    }
                    System.out.println("Keyword:  " + keyword + " lines: " + lns + " in:" + keys[j]);
                }
            }
            // test sorted search query
            JOTSearchResult[] results2 = engine.performSearch(keywords, defaultSorter);
            for (int i = 0; i != results2.length; i++)
            {
                System.out.println("Score: " + results2[i].getScore() + " hits: " + results2[i].getHits() + " for: " + results2[i].getID());
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
