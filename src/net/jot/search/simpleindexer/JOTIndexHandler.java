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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.jot.logger.JOTLogger;
import net.jot.utils.JOTUtilities;

/**
 * Helper to JOTSimpleSearchEngine to handle the index files.
 * The master file: index.txt
 * and the index files ex: 0A/EF.txt
 * 
 * @author thibautc
 */
public class JOTIndexHandler
{

    File indexRoot = null;
    File indexFile = null;
    File tempIndexFile = null;
    Hashtable indexById = new Hashtable();
    Hashtable indexByKey = new Hashtable();
    Hashtable indexStamps = new Hashtable();
    // pattern for reading index master file entries
    static Pattern pattern = Pattern.compile("^(\\d+)\\s+(\\d+)\\s+(.*)$");

    public JOTIndexHandler(File rootFolder) throws Exception
    {
        indexRoot = rootFolder;
        indexFile = new File(rootFolder, "index.txt");
        tempIndexFile = new File(rootFolder, "index.tmp");
        if (!indexFile.exists())
        {
            indexFile.createNewFile();
        }
        loadMasterFile();
    }

    /**
     * Called from constructor
     * loads existing index.txt
     * @throws java.lang.Exception
     */
    protected void loadMasterFile() throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(indexFile));
        try
        {
            String s = null;
            while ((s = reader.readLine()) != null)
            {
                Matcher m = pattern.matcher(s);
                if (m.matches())
                {
                    String id = m.group(1);
                    String stamp = m.group(2);
                    String key = m.group(3);

                    JOTLogger.log(JOTLogger.DEBUG_LEVEL, this, "Found in index: " + id + " " + stamp + " " + key);
                    addToMasterInMemory(id, key, stamp);
                }
            }
        } catch (Exception e)
        {
            throw (e);

        } finally
        {
            reader.close();
        }
    }

    /**
     * saves index.txt
     * @throws java.lang.Exception
     */
    public void saveMasterFile() throws Exception
    {
        Enumeration e = indexById.keys();
        BufferedWriter p = new BufferedWriter(new PrintWriter(new FileOutputStream(indexFile)));
        while (e.hasMoreElements())
        {
            String id = (String) e.nextElement();
            String key = (String) indexById.get(id);
            String stamp = (String) indexStamps.get(key);
            p.write(id + " " + stamp + " " + key + "\n");
        }
        p.close();
    }

    /**
     * Get the timestamp of last indexing of an entry
     * @param key
     * @return
     */
    public long getEntryStamp(String key)
    {
        String stamp = (String) indexStamps.get(key);
        long l = 0;
        try
        {
            l = new Long(stamp).longValue();
        } catch (Exception e)
        {
        }
        return l;
    }

    /**
     * wether the key is new or already indexed
     * @param key
     * @return
     */
    public boolean isNewKey(String key)
    {
        return !indexByKey.containsKey(key);
    }

    /**
     * Adds a new entry to index.txt
     * @param id
     * @param key
     * @param lastModified
     * @throws java.lang.Exception
     */
    public void addMasterEntry(int id, String key, long lastModified) throws Exception
    {
        addToMasterInMemory("" + id, key, "" + lastModified);
        //append to file right away .. safer to have it now so that indexes won't point to missing entry if indexing is shut down before completion
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(indexFile, true);
            fos.write(("" + id + " " + lastModified + " " + key + "\n").getBytes());
        } catch (Exception e)
        {
            if (fos != null)
            {
                fos.close();
            }
            throw (e);
        }
        if (fos != null)
        {
            fos.close();
        }
    }

    public String getMasterKeyById(String id)
    {
        return (String) indexById.get(id);
    }

    public String getMasterIdByKey(String key)
    {
        return (String) indexByKey.get(key);
    }

    /**
     * Remove an entry keywords from indexes
     * Does not remove the entry itself(ie: file) from the index master itself, call removeMasterEntry for that.
     * @param id
     */
    public void removeEntries(String key) throws Exception
    {
        String id = (String) indexByKey.get(key);
        //System.out.println("id["+id+"]");
        String entryPattern = " " + id + ":\\d+";
        if (id != null)
        {
            // crawl through all index files and remove entries !
            File[] dirs = indexRoot.listFiles();
            for (int i = 0; i != dirs.length; i++)
            {
                if (dirs[i].isDirectory())
                {
                    File[] files = dirs[i].listFiles();
                    for (int j = 0; j != files.length; j++)
                    {
                        if (files[j].isFile() && files[j].getName().endsWith(".txt"))
                        {
                            // we found an index file
                            BufferedReader reader = new BufferedReader(new FileReader(files[j]));
                            File tmpFile = new File(files[j].getParent(), files[j].getName().replace(".txt", ".tmp"));
                            PrintWriter p = new PrintWriter(new FileOutputStream(tmpFile));
                            try
                            {
                                String s = null;
                                while ((s = reader.readLine()) != null)
                                {
                                    s = s.replaceAll(entryPattern, "");
                                    // write the line back
                                    p.write(s + "\n");
                                }
                            } catch (Exception e)
                            {
                                throw (e);
                            } finally
                            {
                                reader.close();
                                p.close();
                            }
                            synchronized (this)
                            {
                                JOTUtilities.moveFile(files[j], tmpFile);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove the file/key from the master index (ie: deleted file)
     * @param id
     */
    public void removeMasterEntry(String key) throws Exception
    {
        String id = (String) indexByKey.get(key);
        if (id != null)
        {
            indexByKey.remove(key);
            indexStamps.remove(key);
            indexById.remove(id);
            // remove from the file itself.
            synchronized (this)
            {
                // if existing file, search if keyword already in it
                BufferedReader reader = new BufferedReader(new FileReader(indexFile));
                PrintWriter p = new PrintWriter(new FileOutputStream(tempIndexFile));
                try
                {
                    String s = null;
                    while ((s = reader.readLine()) != null)
                    {
                        if (!s.startsWith(id + " "))
                        {
                            p.write(s + "\n");
                        }
                    }
                } catch (Exception e)
                {
                    throw (e);
                } finally
                {
                    reader.close();
                    p.close();
                }
                JOTUtilities.moveFile(indexFile, tempIndexFile);
            }
        }
    }

    private void addToMasterInMemory(String id, String key, String stamp)
    {
        indexByKey.put(key, id);
        indexById.put(id, key);
        indexStamps.put(key, stamp);
    }

    /**
     * Add a keyword to the index
     * id: id of the key in index.txt
     * word: the keyword to be indexed
     * lines: vector of line numbers
     * return: wether it was a new keyword or not
     * @throws java.lang.Exception
     */
    public boolean indexKeyword(String id, String word, Vector lines) throws Exception
    {
        boolean newKeyword = true;
        Character c = new Character(word.charAt(0));
        Character c2 = new Character(word.charAt(1));
        /* Uses the character unicode hashcode value in hexadecimal (uppercase) as unique ID.
         * We store data in (keyword.letter1) / (keyword.letter2).txt  (in hexaddecimal) (Index) so we can search faster
         */
        String folder = Integer.toString(c.hashCode(), 16).toUpperCase();
        String file = Integer.toString(c2.hashCode(), 16).toUpperCase() + ".txt";
        //System.out.println("" + c + " -> " + folder + "/" + file);
        File dir = new File(indexRoot, folder);
        File tempFile = new File(indexRoot, Integer.toString(c2.hashCode(), 16).toUpperCase() + ".tmp");
        // create the index folder and file if necessary
        if (!dir.exists())
        {
            dir.mkdir();
        }
        File f = new File(dir, file);
        // build the new locations string
        String loc = "";
        for (int i = 0; i != lines.size(); i++)
        {
            loc += id + ":" + (String) lines.get(i) + " ";
        }
        //System.out.println("loc for " + word + " -> " + loc);

        // write to file
        if (!f.exists())
        {
            // if new file just create and add new entry right away
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            try
            {
                fos.write((word + " " + loc + "\n").getBytes());
            } catch (Exception e)
            {
                throw (e);
            } finally
            {
                fos.close();
            }
        } else
        {
            // if existing file, search if keyword already in it
            BufferedReader reader = new BufferedReader(new FileReader(f));
            PrintWriter p = new PrintWriter(new FileOutputStream(tempFile));
            try
            {
                String s = null;
                while ((s = reader.readLine()) != null)
                {
                    if (s.startsWith(word + " "))
                    {
                        newKeyword = false;
                        // we need to update the keyword(update this line)
                        p.write(s + loc + "\n");
                    } else
                    {
                        // leave the line alone, not our keyword.
                        p.write(s + "\n");
                    }
                }

                if (newKeyword)
                {
                    // append new keyword to file
                    p.write(word + " " + loc + "\n");
                }
            } catch (Exception e)
            {
                throw (e);
            } finally
            {
                reader.close();
                p.close();
            }
            // move temp file as real one.
            synchronized (this)
            {
                JOTUtilities.moveFile(f, tempFile);
            }
        }
        return newKeyword;
    }

    /**
     * Return the index file line for a specific keyword or empty string if none found. 
     */
    public String findKeywordIndexLine(String keyword) throws Exception
    {
        String word = keyword.trim().toLowerCase();
        Character c = new Character(word.charAt(0));
        Character c2 = new Character(word.charAt(1));
        /* Uses the character unicode hashcode value in hexadecimal (uppercase) as unique ID.
         * We store data in (keyword.letter1) / (keyword.letter2).txt  (in hexaddecimal) (Index) so we can search faster
         */
        String folder = Integer.toString(c.hashCode(), 16).toUpperCase();
        String file = Integer.toString(c2.hashCode(), 16).toUpperCase() + ".txt";
        String line = "";
        File f = new File(new File(indexRoot, folder), file);
        if (f.exists() && f.isFile())
        {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            try
            {
                String s = "";
                while (((s = reader.readLine()) != null) && line.length() == 0)
                {
                    if (s.startsWith(word + " "))
                    {
                        line = s;
                    }
                }
            } catch (Exception e)
            {
                throw (e);
            } finally
            {
                reader.close();
            }
        }
        return line;
    }
}
