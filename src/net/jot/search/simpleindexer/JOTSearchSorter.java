/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */

package net.jot.search.simpleindexer;

/**
 * Interface for custom implementations of an algorithm for sorting search query results
 * @author tcolar
 */
public interface JOTSearchSorter 
{
   /**
    * Resturns the results sorted.
    * @param rawResults
    * @return
    */
    public JOTSearchResult[] sortResults(JOTRawSearchResult[] rawResults);
}
