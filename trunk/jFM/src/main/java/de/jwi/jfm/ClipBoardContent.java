
package de.jwi.jfm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jürgen Weber Source file created on 10.01.2016
 */
public class ClipBoardContent {

	public static final int CUT_CONTENT = 1;
	public static final int COPY_CONTENT = 2;
	
	public int contentType;
	
	public File[] selectedfiles;

	public ClipBoardContent(int contentType, File[] selectedfiles) {
		super();
		this.contentType = contentType;
		this.selectedfiles = selectedfiles;
	}

	public String getFileCount()
	{
		return "" + selectedfiles.length;
	}
	
	public String getFiles()
	{
		List<File> l = Arrays.asList(selectedfiles);
		
		List<String> sl = new ArrayList<String>(l.size());
		
		for (File f : l)
		{
			sl.add(f.getName());
		}
		
		String s = l.get(0).getParent();
		
		s = s + ": " + sl.toString();
		return s;
	}
	
	public String getKind()
	{
		String s = contentType == COPY_CONTENT ? "copy" : "cut";
		
		return s;
	}
}