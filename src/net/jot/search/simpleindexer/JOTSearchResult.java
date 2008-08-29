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

/**
 * Represented an entry of the sorted search result (ie: a file)
 * This is of type hashtable, so that a user writing it's own JOTSearchsorter implementation could add it's own extra element data
 * @author tcolar
 */
public class JOTSearchResult extends Hashtable
{
    public static final String ID="ID";
    public static final String SCORE="SCORE";
    public static final String HITS="HITS";
    public static final String BEST_LINE="BESTLINE";
    public static final String BEST_LINE_SCORE="BESTLINESCORE";
    
    /**
     * creates an entry with specific id,score,hits
     * @param id
     * @param score
     * @param hits
     */
    public JOTSearchResult(String id, int score, int hits, int bestLine, int bestLineScore )
    {
        put(ID,id);
        put(SCORE,new Integer(score));
        put(HITS,new Integer(hits));
        put(BEST_LINE,new Integer(bestLine));
        put(BEST_LINE_SCORE,new Integer(bestLineScore));
    }
    
    /**
     * score 0 to 10   (how many of the keywords where found in the file)
     * @return
     */
    public int getScore()
    {
        return ((Integer)get(SCORE)).intValue();
    }
    /**
     * how many of the keywords on the best line
     * @return
     */
    public int getBestLineScore()
    {
        return ((Integer)get(BEST_LINE_SCORE)).intValue();
    }
    /**
     * line number of best line
     * @return
     */
    public int getBestLine()
    {
        return ((Integer)get(BEST_LINE)).intValue();
    }
    /**
     * total number of keywords hits in whole file
     * @return
     */
    public Integer getHits()
    {
        return ((Integer)get(HITS));
    }
    /**
     * return uniqueId of the matching entry(ex: filename)
     * @return
     */
    public String  getID()
    {
        return (String)get(ID);
    }
}
