package de.jwi.jfm;

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
import java.util.Comparator;


/**
 * @author Jürgen Weber
 * Source file created on 08.03.2004
 */
public class FileComparator implements Comparator
{
    public static final int SORT_NAME_UP = 1;
    public static final int SORT_NAME_DOWN = 2;
    public static final int SORT_DATE_UP = 3;
    public static final int SORT_DATE_DOWN = 4;
    public static final int SORT_SIZE_UP = 5;
    public static final int SORT_SIZE_DOWN = 6;

    
    public static final Comparator nameUpInstance = new FileComparator(SORT_NAME_UP);
    public static final Comparator nameDownInstance = new FileComparator(SORT_NAME_DOWN);
    public static final Comparator dateUpInstance = new FileComparator(SORT_DATE_UP);
    public static final Comparator dateDownInstance = new FileComparator(SORT_DATE_DOWN);
    public static final Comparator sizeUpInstance = new FileComparator(SORT_SIZE_UP);
    public static final Comparator sizeDownInstance = new FileComparator(SORT_SIZE_DOWN);
    
    
    private int mode;
    
    FileComparator(int mode) 
    {
        this.mode = mode;
    }
    
    public int compare(Object o1,
            Object o2)
    {
        FileWrapper fw1 = (FileWrapper)o1;
        FileWrapper fw2 = (FileWrapper)o2;
        
        File f1 = fw1.getFile();
        File f2 = fw2.getFile();
        
        int rc = 0;
        
        if (SORT_NAME_UP == mode)
        {
            rc= f1.getName().compareToIgnoreCase(f2.getName());
        }
        else if (SORT_NAME_DOWN == mode)
        {
            rc= f2.getName().compareToIgnoreCase(f1.getName());
        }
        else if (SORT_SIZE_UP == mode)
        {
             long l =(f1.length()  - f2.length() ) ;
             rc = l < 0l ? -1 : l == 0l ? 0 : 1;
        }        
        else if (SORT_SIZE_DOWN == mode)
        {
            long l =(f2.length()  - f1.length() ) ;
            rc = l < 0l ? -1 : l == 0l ? 0 : 1;
        }
        
        else if (SORT_DATE_UP == mode)
        {
             long l =(f1.lastModified() - f2.lastModified()) ;
             rc = l < 0l ? -1 : l == 0l ? 0 : 1;
        }        
        else if (SORT_DATE_DOWN == mode)
        {
            long l =(f2.lastModified() - f1.lastModified()) ;
            rc = l < 0l ? -1 : l == 0l ? 0 : 1;
        }
        return rc;
    }
}
