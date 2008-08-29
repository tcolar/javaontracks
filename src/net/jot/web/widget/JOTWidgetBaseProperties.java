/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget;

import java.util.Hashtable;

/**
 * Base properties of a widget.
 * For most widget you will want to create an ovveride of this to:
 * - redefine some values.
 * - add some other propertiers unique to your widget.
 * @author thibautc
 */
public class JOTWidgetBaseProperties
{
    // CONSTANTS
 
    public final static String PROP_WIDTH="Width";
    public final static String PROP_HEIGHT="Height";
    public final static String PROP_BORDER_TYPE="BorderType";
    public final static String PROP_BORDER_WIDTH="BorderWidth";
    public final static String PROP_BG_COLOR="bgColor";
    public final static String PROP_REFRESH_EVERY="RefreshEvery";

    public final static String VAL_HEIGHT_AUTO= "-1";
    public final static String VAL_BORDER_TYPE_SOLID="solid";
    public final static String VAL_BORDER_TYPE__DASH="dashes";
    public final static String VAL_BORDER_TYPE__DOTTED="dotted";
    public final static String VAL_BORDER_THICKNESS_NONE="0px";
    public final static String VAL_BORDER_THICKNESS_THIN="1px";
    public final static String VAL_BORDER_THICKNESS_MEDIUM="2px";
    public final static String VAL_BORDER_THICKNESS_THICK="3px";
    public final static String VAL_BG_WHITE="#FFFFFF";
    public final static String VAL_BG_BLUE="#0000FF";
    public final static String VAL_BG_TRANSPARENT="";
    public final static String VAL_AUTO_REFRESH_NEVER="Never";
    

    // store
    private Hashtable store=new Hashtable();
    
    public JOTWidgetBaseProperties()
    {
        // defaults
        /*addProperty(PROP_HEIGHT,new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT,new String[]{"12"},"12"));
        addProperty(PROP_WIDTH,new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT,new String[]{"12",},"12"));
        addProperty(PROP_BORDER_TYPE,new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT,new String[]{VAL_BORDER_TYPE_SOLID,VAL_BORDER_TYPE__DASH,VAL_BORDER_TYPE__DOTTED},VAL_BORDER_TYPE_SOLID));
        addProperty(PROP_BG_COLOR,new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT,new String[]{VAL_BG_TRANSPARENT,VAL_BG_WHITE,VAL_BG_BLUE},VAL_BG_TRANSPARENT));
        addProperty(PROP_BORDER_WIDTH,new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT,new String[]{VAL_BORDER_THICKNESS_NONE,VAL_BORDER_THICKNESS_THIN,VAL_BORDER_THICKNESS_MEDIUM,VAL_BORDER_THICKNESS_THICK},VAL_BORDER_THICKNESS_THIN));
        addProperty(PROP_REFRESH_EVERY,new JOTWidgetProperty(JOTWidgetProperty.TYPE_SELECT, new String[]{VAL_AUTO_REFRESH_NEVER,"1","2","3","5","10","15","20","30","45","60","120","180","240","300","600","900","1800","3600"},VAL_AUTO_REFRESH_NEVER));*/
   }

    public void addProperty(String propName, JOTWidgetProperty prop)
    {
        store.put(propName, prop);
    }
    
    public Hashtable getAllProperties()
    {
        return store;
    }

    public JOTWidgetProperty getProperty(String key)
    {
        return (JOTWidgetProperty)store.get(key);
    }

    public String getPropertyDefaultValue(String key, Hashtable options, String fallbackValue)
    {
        if(options!=null && options.get(key)!=null)
            return (String)options.get(key);
        
        JOTWidgetProperty prop=(JOTWidgetProperty)getProperty(key);
        String val=fallbackValue;
        if (prop!=null && prop.getDefaultValue()!=null)
        {
            val=prop.getDefaultValue();
        }
        return val;
                
    }

    public void updatePropertyDefaultValue(String key, String value)
    {
        JOTWidgetProperty prop = (JOTWidgetProperty)store.get(key);
        if(prop!=null)
            prop.setDefaultValue(value);
    }
    
    /**
     * -1 if never
     * @return
     */
    public int getRefreshEvery()
    {
        String val=getPropertyDefaultValue(JOTWidgetBaseProperties.PROP_REFRESH_EVERY,null,JOTWidgetBaseProperties.VAL_AUTO_REFRESH_NEVER);
        if(val==null || val.equals(VAL_AUTO_REFRESH_NEVER))
            val="-1";
        return new Integer(val).intValue();
    }

    public void removeProperty(String key)
    {
        store.remove(key);
    }
    // TODO: background image / background repeat??
    // TODO: marging, padding top,left,right,bottom
    // TODO: text align, vertical align
    
}
