/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.logger;

/**
 * This is to be use with a FileTailer.
 *
 */
public interface JOTTailFilter
{

        /**
         * Accept or not this line as one of the tail results.
         *
         * @param  str  Description of Parameter
         * @return      Description of the Returned Value
         */
        public boolean acceptLine(String str);
}

