/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.views;

import net.jot.web.view.*;

/**
 * Implements a simple/generic user message View
 * The request should provide the attributes MESSAGE_TITLE,MESSAGE_TEXT,MESSAGE_LINK
 * 
 * The view implementation will then use link,message and title and a template to render the message to the user.
 * 
 * @author thibautc
 */
public class JOTMessageView extends JOTView
{
  public static final String MESSAGE_TITLE="JOT_MESSAGE_TITLE";
  public static final String MESSAGE_TEXT="JOT_MESSAGE_TEXT";
  // where do we go when the user click "ok".
  public static final String MESSAGE_LINK="JOT_MESSAGE_LINK";
  
  public void prepareViewData() throws Exception
  {
    String link=(String)request.getAttribute(MESSAGE_LINK);
    if(link==null) link="javascript:history.back();";
    String message=(String)request.getAttribute(MESSAGE_TEXT);
    if(message==null) message="";
    String title=(String)request.getAttribute(MESSAGE_TITLE);
    if(title==null) title="Message";
    
    addVariable("link", link);
    addVariable("message", message);
    addVariable("title", title);
  }

  public boolean validatePermissions()
  {
    // no permisiiosn required to see the message.
    return true;
  }
  
  
}
