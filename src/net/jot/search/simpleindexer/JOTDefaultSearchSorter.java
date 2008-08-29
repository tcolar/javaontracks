/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.search.simpleindexer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

/**
 * Default search sorter
 * Sorting results by score / hits
 * if all keywords are found in the page then you get maximum score(10/10)
 * each time one of the keyword is found in the page it counts as a "hit"
 * also finds the best line in the file(most keywords) can be use to show an abstract
 * @author tcolar
 */
public class JOTDefaultSearchSorter implements JOTSearchSorter
{

    static JOTDefaultSearchComparator comp = new JOTDefaultSearchComparator();

    public JOTSearchResult[] sortResults(JOTRawSearchResult[] rawResults)
    {
        Hashtable results = new Hashtable();
        for (int i = 0; i != rawResults.length; i++)
        {
            JOTRawSearchResult result = rawResults[i];
            String[] ids = result.getMatchingIds();
            for (int j = 0; j != ids.length; j++)
            {
                Hashtable lineScores = new Hashtable();
                String id = ids[j];
                int score = 1;
                Integer[] lines = result.getResultsForId(id);
                int bestLine = 0;
                int bestLineScore = 0;
                for (int l = 0; l != lines.length; l++)
                {
                    Integer lnScore = (Integer) lineScores.get(lines[l]);
                    if (lnScore == null)
                    {
                        lnScore = new Integer(1);
                    }
                    lnScore = new Integer(lnScore.intValue() + 1);
                    if (lnScore.intValue() > bestLineScore)
                    {
                        bestLine = lines[l].intValue();
                        bestLineScore = lnScore.intValue();
                    }
                    lineScores.put(lines[l], lnScore);
                }
                int pts = lines.length;

                if (!results.containsKey(id))
                {
                    for (int k = i + 1; k < rawResults.length; k++)
                    {
                        if (rawResults[k].getResultsForId(id).length > 0)
                        {
                            score++;
                            pts += rawResults[k].getResultsForId(id).length;
                            Integer[] lines2 = rawResults[k].getResultsForId(id);
                            for (int l = 0; l != lines2.length; l++)
                            {
                                Integer lnScore = (Integer) lineScores.get(lines2[l]);
                                if (lnScore == null)
                                {
                                    lnScore = new Integer(1);
                                }
                                lnScore = new Integer(lnScore.intValue() + 1);
                                if (lnScore.intValue() > bestLineScore)
                                {
                                    bestLine = lines2[l].intValue();
                                    bestLineScore = lnScore.intValue();
                                }
                                lineScores.put(lines2[l], lnScore);
                            }
                        }
                    }
                    score = (score * 10) / rawResults.length;
                    JOTSearchResult entry = new JOTSearchResult(id, score, pts,bestLine,bestLineScore);
                    results.put(id, entry);
                }
            }
        }

        List values = Arrays.asList(results.values().toArray());
        //sort
        Collections.sort(values, comp);
        return (JOTSearchResult[]) values.toArray(new JOTSearchResult[0]);
    }

    /**
     * Default comparator to compare search results using score, hits
     */
    static class JOTDefaultSearchComparator implements Comparator
    {

        public int compare(Object arg0, Object arg1)
        {
            JOTSearchResult result1 = (JOTSearchResult) arg0;
            JOTSearchResult result2 = (JOTSearchResult) arg1;
            if (result1.getScore() > result2.getScore())
            {
                return -1;
            } else if (result1.getScore() < result2.getScore())
            {
                return 1;
            } else
            {
                // same score, look at best line score
                if (result1.getBestLineScore() > result2.getBestLineScore())
                {
                    return -1;
                }
                else if (result1.getBestLineScore() < result2.getBestLineScore())
                {
                    return 1;
                }
                else
                {
                    //same best line score, look at number of hits
                    return result2.getHits().compareTo(result1.getHits());
                }
            }
        }
    }
}
