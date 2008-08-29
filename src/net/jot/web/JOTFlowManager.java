/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web;

import java.io.File;

import net.jot.logger.JOTLogger;

/**
 * High level manager that handles loading and parsing flow.conf.
 * @author thibautc
 */
public class JOTFlowManager
{

  public static final String confFileName = "flow.conf";

  public static JOTFlowConfig init(String confFolder)
  {
    File flowFile = new File(confFolder, confFileName);
    JOTLogger.log(JOTLogger.CAT_FLOW, JOTLogger.INFO_LEVEL, JOTFlowManager.class, "Will start parsing flow file: " + flowFile.getAbsolutePath());
    // parse & cache the flow.conf file.
    JOTFlowConfig config = JOTFlowConfigParser.parseFlowFile(flowFile);
    return config;
  }
}
