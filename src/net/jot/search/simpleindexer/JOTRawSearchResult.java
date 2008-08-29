/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.search.simpleindexer;

import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The "raw" search results for one keyword esrach results.
 * @author tcolar
 */
public class JOTRawSearchResult
{
    static final Pattern resultPattern=Pattern.compile(" (\\d+):(\\d+)");
    // whichever keyword those results are for.
    private String keyword=null;
    private Hashtable results=new Hashtable();
    
    /**
     * Return an array of Integer[], containing the line number(s) where the keyword was found in the specified file(identified by it's id)
     * If a keyword is on the same line twice, that line number will be in the array twice
     * @param id
     * @return
     */
    public Integer[] getResultsForId(String id)
    {
        if(results.get(id) == null)
            return new Integer[0];
        return (Integer[])((Vector)results.get(id)).toArray(new Integer[0]);
    }
    
    /**
     * Get the list of macthing id's (default: filepath), or whatever the uniqueId was when you indexed the file
     * @return
     */
    public String[] getMatchingIds()
    {
        return (String[])results.keySet().toArray(new String[0]);
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    /**
     * return the keyword those results are for.
     * @return
     */
    public String getKeyword()
    {
        return keyword;
    }
    
    /**
     * constructor, called from searchengine
     * indexLine: the keyword line from the index file for the keyword
     */
    protected JOTRawSearchResult(JOTIndexHandler handler,String keyword,  String indexLine)
    {
        this.keyword=keyword;
        Matcher m=resultPattern.matcher(indexLine);
        while(m.find())
        {
            String id=m.group(1);
            String line=m.group(2);
            String key=handler.getMasterKeyById(id);
            Vector v=new Vector();
            if(results.containsKey(key))
            {
                v=(Vector)results.get(key);
            }
            v.add(new Integer(line));
            results.put(key,v);
        }
    }
}
