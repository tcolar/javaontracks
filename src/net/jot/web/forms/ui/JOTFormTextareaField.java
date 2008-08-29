/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.web.forms.ui;

import net.jot.web.forms.JOTFormConst;

/**
Html Textarea within form
@author thibautc
*/
public class JOTFormTextareaField extends JOTFormField
{
	protected int rows=3;
	protected int cols=10;
	
/**

@param name 
@param description 
@param cols 
@param rows 
@param defaultValue   will be the "content" (text) in the textarea
*/
	public JOTFormTextareaField(String name, String description, int cols, int rows, String defaultValue)
	{
		setType(JOTFormConst.TEXTAREA);
		setName(name);
		setDescription(description);
		setDefaultValue(defaultValue);
		this.rows=rows;
		this.cols=cols;
	}

	public int getRows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public int getCols()
	{
		return cols;
	}

	public void setCols(int cols)
	{
		this.cols = cols;
	}


}
