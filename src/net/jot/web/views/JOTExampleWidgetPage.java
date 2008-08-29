/*
 * JavaOnTrack/Jotwiki
 * Thibaut Colar.
 * tcolar-jot AT colar  DOT net
 */

package net.jot.web.views;

import net.jot.web.view.JOTView;

/**
 *
 * @author thibautc
 */
public class JOTExampleWidgetPage extends JOTView
{

    public void prepareViewData() throws Exception
    {
        setBuiltinTemplate(JOTBuiltinTemplates.EXAMPLEWIDGET_TEMPLATE);
    }

    public boolean validatePermissions()
    {
        return true;
    }

}
