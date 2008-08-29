/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.logger;

import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * This is used to "tail" a file (read it from the bottom)
 * this is done tro be fast and not use much memory.
 * This is to be used together with the TailFilter interface
 * The tailfilter interface let you tail only matching lines
 * so this function let you do:
 * A Tail, a Grep,  or a combianison of both (Tail | Grep)
 *
 *
 *@author     tcolar
 *@created    May 6, 2003
 */
public class JOTFileTailer
{
    byte[] buffer;
    int bufferLength;
    int newLine;
    int startLength;
    int bytesToRead;
    int curPos;
    RandomAccessFile logFile = null;


    /**
     *Constructor for the FileTailer object
     */
    public JOTFileTailer()
    {
        this(1000000, Character.LINE_SEPARATOR);
    }


    /**
     *Constructor for the FileTailer object
     *
     *@param  length  Description of Parameter
     */
    public JOTFileTailer(int length)
    {
        this(length, Character.LINE_SEPARATOR);
    }


    /**
     *Constructor for the FileTailer object
     *
     *@param  separator  Description of Parameter
     *@param  length     Description of Parameter
     */
    public JOTFileTailer(int length, int separator)
    {
        bufferLength = length;
        newLine = separator;
    }


    /**
     * This is to be called to set the file to tail.
     *
     *@param  file           Description of Parameter
     *@exception  Exception  Description of Exception
     */
    public void loadFile(String file) throws Exception
    {
        if (file != null && file.length() > 0)
        {
            logFile = new RandomAccessFile(file, "r");
        }
    }


    /**
     * This does the job !
     *
     *@param  max            Description of Parameter
     *@param  filter         Description of Parameter
     *@return                Description of the Returned Value
     *@exception  Exception  Description of Exception
     */
    public Vector tail(int max, JOTTailFilter filter) throws Exception
    {
        int startLength = (int) logFile.length();
        int bytesToRead = bufferLength;
        int curPos = startLength;
        Vector entries = new Vector();
        buffer = new byte[bufferLength];
        String remains = "";

        if (logFile == null)
        {
            throw new Exception("You must load a file first !!");
        }

        while (curPos > 0 && entries.size() <= max)
        {
            curPos -= bufferLength;
            if (curPos < 0)
            {
                bytesToRead += curPos;
                curPos = 0;
            }

            logFile.seek(curPos);
            int nbBytes = logFile.read(buffer, 0, bytesToRead);
            // next end of line
            if (nbBytes > 0)
            {

                // index of the end of the previous line
                int lineEnd = nbBytes - 1;
                //scanning the buffer backward

                for (int i = nbBytes - 1; i > -1 && entries.size() <= max; i--)
                {
                    if (buffer[i] == newLine)
                    {

                        String str = "";
                        for (int cpt = i + 1; cpt < lineEnd; cpt++)
                        {
                            // reading this line
                            str += (char) buffer[cpt];
                        }
                        str += remains;
                        if (filter.acceptLine(str))
                        {
                            /*
                             *  if accepted
                             *  by the filter, adding the line
                             */
                            entries.add(str);
                        }
                        // marking the end of the previous line
                        lineEnd = i;
                    }
                }

                remains = "";
                if (entries.size() <= max)
                {
                    /*
                     *  keeping the remains of this buffer
                     *  (line broken on two buffer) for
                     *  next round.
                     */
                    for (int cpt = 0; cpt != lineEnd; cpt++)
                    {
                        // reading this line
                        remains += (char) buffer[cpt];
                    }
                }
            }
        }

        /*
         *  For the very first line of the file (doesn't have a
         *  carriage return preceding it)
         */
        if (filter.acceptLine(remains))
        {
            entries.add(remains);
            
        }

        // relese buffer to garbage collector for sure.
        buffer = null;

        return entries;
    }

}

