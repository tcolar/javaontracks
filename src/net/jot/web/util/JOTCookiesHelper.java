/*
 * Thibaut Colar Mar 8, 2010
 */

package net.jot.web.util;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Helps reading cookies
 * @author thibautc
 */
public class JOTCookiesHelper
{
	List cookies;

	public JOTCookiesHelper(HttpServletRequest request)
	{
		cookies = Arrays.asList(request.getCookies());
	}

	/**
	 * Find the first cookie with matching name
	 * In theory there can be multiple (!= paths)
	 * @return
	 */
	public Cookie findFirstByName(String name)
	{
		for(int i = 0; i!= cookies.size(); i++)
		{
			Cookie c = (Cookie)cookies.get(i);
			if(c.getName().equals(name))
				return c;
		}
		return null;
	}

	/**
	 * Find unique cookie by name & path
	 * @return
	 */
	public Cookie findByNameAndPath(String name, String path)
	{
		for(int i = 0; i!= cookies.size(); i++)
		{
			Cookie c = (Cookie) cookies.get(i);
			if(c.getName().equals(name) && c.getPath().equals(path))
				return c;
		}
		return null;
	}

}
