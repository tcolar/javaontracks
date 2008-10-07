/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.persistance.builders;

import java.util.Vector;
import net.jot.persistance.JOTStatementFlags;

/**
 *
 * @author tcolar
 */
public abstract class JOTQueryBase {
    protected StringBuffer sql = new StringBuffer();
    protected Class modelClass;
    protected Vector params = new Vector();
    protected JOTStatementFlags flags = new JOTStatementFlags();
    protected int nbWhere = 0;

    protected void setModelClass(Class modelClass)
    {
        this.modelClass = modelClass;
    }

    public String showSQL()
    {
        return sql.toString();
    }

    /**
     * Show special statement flags (if any)
     * @return
     */
    public String showFlags()
    {
        return flags.toString();
    }


}
