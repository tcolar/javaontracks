/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jottools;

import java.io.File;

/**
 * Helper to create a new Project
 * @author thibautc
 */
public class NewProject 
{
    public static void main(String[] args)
    {
        if(args.length<1)
        {
            System.err.println("Need the project name as an argument");
            return;
        }
        String name=args[1];
        name=name.trim();
        if(name.length()<1)
        {
            System.err.println("Need the project name as an argument");
            return;
        }
        File folder=new File(name);
        if(folder.exists())
        {
            System.err.println("There is already a file/folder called "+folder.getAbsolutePath()+" !!");
            return;
        }
        
        // create the project
        folder.mkdirs();
        new File(folder+File.separator+"lib").mkdirs();
    }
}
