/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms;

/**
Form Constants
Form types / field types
@author thibautc
*/
public class JOTFormConst
{
	// Values to use for radio buttons/checkboxes
	public static final String VALUE_CHECKED="CHECKED";
	public static final String VALUE_UNCHECKED = "";
	public static final String VALUE_SELECTED="SELECTED";
	public static final String VALUE_UNSELECTED = "";
	// element types
	public static final int UNDEFINED=0;
	public static final int LABEL=1;
	public static final int INPUT=2;
	public static final int BUTTON=3;
	public static final int TEXTAREA=4;
	public static final int OBJECT=5;
	public static final int SELECT=6;
	// shortcuts for common input types
	public static final int INPUT_TEXT=20;
	public static final int INPUT_HIDDEN=21;
	public static final int INPUT_SUBMIT=22;
	public static final int INPUT_PASSWORD=23;
	public static final int INPUT_CHECKBOX=24;
	public static final int INPUT_RADIO=25;
	public static final int INPUT_IMAGE=26;
	public static final int INPUT_BUTTON=27;
	public static final int INPUT_RESET=28;
        public static final int INPUT_CAPTCHA=29;
	// subtypes
	public static final int OBJECT_PARAM=10;
	public static final int SELECT_OPTGRP=11;
	public static final int SELECT_OPTION=12;

}
