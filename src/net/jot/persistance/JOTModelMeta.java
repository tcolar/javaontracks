/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */

package net.jot.persistance;

/**
 * Model metadata
 * @author thibautc
 */
public class JOTModelMeta 
{

  private int version=-1;
  private int rowSize=-1;
  private String fields=null;

  public void setFields(String fields)
  {
    this.fields = fields;
  }

  public void setRowSize(String rowSize)
  {
    try
    {
      this.rowSize = new Integer(rowSize).intValue();
    }
    catch(Exception e){}
  }

  public void setVersion(String version)
  {
    try
    {
      this.version = new Integer(version).intValue();
    }
    catch(Exception e){}
  }

  public String getFields()
  {
    return fields;
  }

  public int getRowSize()
  {
    return rowSize;
  }

  public int getVersion()
  {
    return version;
  }
  
  
}
