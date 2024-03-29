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
 * Object representing a user permission in DB
 * Used to assign a permission "String" (ie: "CAN_EDIT") to a Profile.
 * @author thibautc
 *
 */
public abstract class JOTAuthPermission extends JOTModel
{
	// dataProfile refers to dataProfile.id
        // should be a long
	public long profile;
	public String permission;

	public String getPermission()
	{
		return permission;
	}

	public void setPermission(String permission)
	{
		this.permission = permission;
	}

	public long getProfile()
	{
		return profile;
	}

	public void setProfile(long profile)
	{
		this.profile = profile;
	}

/**
	 * If you override this in the subclass, make sure you still call this (super.customize())
	 * Or copy the "defineFields" entries
	 */
	public void customize(JOTModelMapping mapping)
	{
            mapping.defineFieldSize("dataPermission",80);
	}

}
