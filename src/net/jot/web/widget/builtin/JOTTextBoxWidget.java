/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.widget.builtin;

import net.jot.web.widget.JOTWidgetBaseProperties;
import net.jot.web.widget.JOTWidgetNoAjaxBase;

/**
 * Smple text widget
 * @author thibautc
 */
public abstract class JOTTextBoxWidget extends JOTWidgetNoAjaxBase
{
    // can ovveride in subclass: either: hidden, scroll, auto, visible
    public String overflow="hidden";
    

    public void customizeProperties()
    {
        properties.updatePropertyDefaultValue(JOTWidgetBaseProperties.PROP_BG_COLOR, "");
    }

}
