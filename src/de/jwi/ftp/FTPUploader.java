
package de.jwi.ftp;
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

/**
 * @author Jürgen Weber
 * Source file created on 27.05.2004
 */
public class FTPUploader
{

    public static String upload(URL url, List files)
    {
        String rcs = "";

        if (!"ftp".equals(url.getProtocol())) { return "not ftp protocol"; }

        String host = url.getHost();
        String userInfo = url.getUserInfo();
        String path = url.getPath();
        String user = null;
        String pass = null;

        int p;
        if ((userInfo != null) && ((p = userInfo.indexOf(':')) > -1))
        {
            user = userInfo.substring(0, p);
            pass = userInfo.substring(p + 1);
        }
        else
        {
            user = userInfo;
        }

        FTPClient ftp = new FTPClient();

        try
        {
            int reply;
            ftp.connect(host);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                return "connection refused";
            }
        }
        catch (IOException e)
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            return "could not connect to " + host;
        }

        try
        {
            if (!ftp.login(user, pass))
            {
                ftp.logout();
                return "failed to login";
            }

            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            // Use passive mode as default because most of us are
            // behind firewalls these days.
            ftp.enterLocalPassiveMode();

            rcs = uploadFiles(ftp, path, files);

            ftp.logout();
        }
        catch (FTPConnectionClosedException e)
        {
            return "connection closed";
        }
        catch (IOException e)
        {
            return e.getMessage();
        }
        finally
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
        }

        return rcs;
    }

    private static String uploadFiles(FTPClient ftp, String ftpServerPath,
            List files) throws IOException
    {
        boolean rc = false;
        String rcs = "error";
        List l = null;

        rc = ftp.makeDirectory(ftpServerPath);
        boolean rc1 = ftp.changeWorkingDirectory(ftpServerPath);
        System.out.println("cd: "+ftpServerPath);

        if (rc1)
        {
            Iterator it = files.iterator();
            while (it.hasNext())
            {
                File f = (File) it.next();

                if (f.isDirectory())
                {
                    String name = f.getName();

                    String newPath = ftpServerPath + "/" + name;

                    l = Arrays.asList(f.listFiles());

                    rcs = uploadFiles(ftp, newPath, l);
                    
                    rc1 = ftp.changeWorkingDirectory(ftpServerPath);
                    
                    if (!rc1)
                    {
                        return "failed to chdir to " + ftpServerPath;
                    }
                }
                else
                {
                    rcs = uploadFile(ftp, ftpServerPath, f);
                }
            }
        }
        else
        {
            rcs = "failed to chdir to " + ftpServerPath;
        }
        return rcs;
    }

    private static String uploadFile(FTPClient ftp, String ftpServerPath, File f)
            throws IOException
    {
        String name = f.getName();
        String rcs = "error";

        FileInputStream fi = new FileInputStream(f);

        boolean rc = false;
        try
        {
            System.out.println(f);
            System.out.println(ftpServerPath + " " + name);
            rc = ftp.storeFile(name, fi);
            rcs = rc ? "" : rcs;
        }
        finally
        {
            fi.close();
        }
        return rcs;
    }

    public static void main(String[] args) throws Exception
    {
        URL url = new URL("ftp://ftp:none@localhost/tmp");
        String protocol = url.getProtocol();

        String host = url.getHost();
        String userInfo = url.getUserInfo();
        String path = url.getPath();
        String file = url.getFile();

        File f = new File("D:/temp/out");
        List l = new ArrayList();
        l.add(f);
        String s = upload(url, l);
        System.out.println(s);
        int x = 5;
    }
}