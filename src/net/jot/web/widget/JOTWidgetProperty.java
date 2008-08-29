/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget;

/**
 *
 * @author thibautc
 */
public class JOTWidgetProperty 
{
    public static final int TYPE_SELECT=1;
    public static final int TYPE_TEXT=2;
    public static final int TYPE_CHECKBOX=3;
    
    public static final String CHECKBOX_FALSE="FALSE";
    public static final String CHECKBOX_TRUE="TRUE";

    int type=TYPE_SELECT;
    String[] possibleValues=new String[0];
    String defaultValue="";
    
    public JOTWidgetProperty(int type, String[] possibleValues, String defaultValue)
    {
        this.type=type;
        this.possibleValues=possibleValues;
        this.defaultValue=defaultValue;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public String[] getPossibleValues()
    {
        return possibleValues;
    }

    public int getType()
    {
        return type;
    }

    public void setDefaultValue(String value)
    {
        this.defaultValue=value;
    }
}
