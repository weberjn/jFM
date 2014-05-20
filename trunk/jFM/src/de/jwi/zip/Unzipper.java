package de.jwi.zip;

/*
 * jFM - Java Web File Manager
 * 
 * Copyright (C) 2004 Juergen Weber
 * 
 * This file is part of jFM.
 * 
 * jFM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * jFM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jFM; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Jürgen Weber
 * Source file created on 12.03.2004
 *  
 */
public class Unzipper
{
    private static final int BUFSIZE = 1024;
    
    public static void unzip(InputStream is, File targetDir) throws FileNotFoundException, IOException
    {
        ZipEntry entry;
        ZipInputStream zis = new ZipInputStream(is);
        byte[] buf = new byte[BUFSIZE];
        
        if (!targetDir.exists())
        {
            throw new FileNotFoundException(targetDir.toString() + " does not exist.");
        }

        if (!targetDir.isDirectory())
        {
            throw new FileNotFoundException(targetDir.toString() + " is not a directory.");
        }
        
        while ((entry = zis.getNextEntry()) != null)
        {
            String name = entry.getName(); 
            
            long size = entry.getSize();
            
            long time = entry.getTime();
            time = (time != -1) ? time : new Date().getTime();
            
            File f = new File(targetDir,name);
            
            
            
            if (entry.isDirectory())
            {
                f.mkdirs();
            }
            else
            {
                f.getParentFile().mkdirs();
                
                FileOutputStream fos = new FileOutputStream(f); 
                int len;
                while ((len = zis.read(buf,0,BUFSIZE)) > 0)
                {
                    fos.write(buf,0,len);
                    size-=len;
                }
                fos.close(); 
            }
            
            // size should be 0 here
            
            f.setLastModified(time);

            zis.closeEntry();
            
        }
        zis.close();
    }

    public static void main(String[] args) throws Exception
    {
        FileInputStream fis = new FileInputStream(args[0]);
        unzip(fis,new File(args[1]));
    }
}
