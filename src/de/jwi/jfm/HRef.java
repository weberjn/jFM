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

/**
 * @author Jürgen Weber
 * Source file created on 27.03.2004
 */
public class HRef
{
    private String display;
    private String link;

    public HRef(String display,String link)
    {
        this.display = display;
        this.link = link;
    }
    
    public boolean getIsActive()
    {
        return link!=null;
    }
    
    public String getDisplay()
    {
        return display;
    }
    
    public String getLink()
    {
        return link;
    }
}
