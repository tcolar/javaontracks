/*
------------------------------------
JavaOnTracks          Thibaut Colar
tcolar-jot AT colar DOT net
Artistic Licence 2.0
http://www.javaontracks.net
------------------------------------
 */
package net.jot.db.authentication;

import net.jot.persistance.JOTModel;
import net.jot.persistance.JOTModelMapping;

/**
 * Object representing a permission set (a.k.a profile) in DB
 * The dataProfile is basically used to map a user to a set of permissions
 * The dataProfile name should be unique.
 * @author thibautc
 *
 */
public abstract class JOTAuthProfile extends JOTModel
{
	// ie: "Admin"
	public String dataName;
	// ie: "Administartor account, full permissions"
	public String dataDescription;
	
	/**
	 * If you override this in the subclass, make sure you still call this (super.customize())
	 * Or copy the "defineFields" entries
	 */
	public void customize(JOTModelMapping mapping)
	{
			mapping.defineFieldSize("dataName",30);
			mapping.defineFieldSize("dataDescription",80);
	}

	public String getDescription()
	{
		return dataDescription;
	}

	public void setDescription(String description)
	{
		this.dataDescription = description;
	}

	public String getName()
	{
		return dataName;
	}

	public void setName(String name)
	{
		this.dataName = name;
	}
        
 
}
