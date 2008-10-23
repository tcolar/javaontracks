/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.web.server;

import java.util.regex.Pattern;

/**
 * Constants
 * @author thibautc
 */
public class JOTWebHelper {
   public final static String INFOS = "JavaOnTracks Server 1.0";
    public static final String MSG_HEAD = "<html><body><table width=100%><tr height=25 bgcolor='#eeeeff'><td><b>";
    public static final String MSG_HEAD2 = "</b></td></tr><tr><td>";
    public static final String MSG_TAIL = "</td></tr><tr height=15 bgcolor='#eeeeff'><td>" + INFOS + "</td></tr></table></body></html>";
    public static final Pattern PROTOCOL_PATTERN = Pattern.compile("^\\w+\\://.*");

}
