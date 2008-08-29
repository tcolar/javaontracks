/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.util.Vector;

/**
 * Object representation of a flow directive, from flow.conf
 * @author thibautc
 */
public class JOTFlowDirective extends Vector
{

  private static final long serialVersionUID = 1L;
  public static final int UNSET = 0;
  public static final int DIRECTIVE_REQUEST = 1;
  public static final int DIRECTIVE_CONTROLLER = 2;
  public static final int DIRECTIVE_CONTROLLER_BUNDLE = 3;
  //public static final int DIRECTIVE_FORM=4;

  public int type = UNSET;
  public String[] args = null;
  public Vector markersToKeep = new Vector();
  private int lineNumber = 0;

  public JOTFlowDirective(int lineNumber, int type, String[] args)
  {
    this.lineNumber = lineNumber;
    this.type = type;
    this.args = args;
  }

  public int getLineNumber()
  {
    return lineNumber;
  }

  public int getType()
  {
    return type;
  }

  public String[] getArgs()
  {
    return args;
  }

  public void keepMarker(String markerName)
  {
    markersToKeep.add(markerName);
  }

  public Vector getMarkersToKeep()
  {
    return markersToKeep;
  }
}
