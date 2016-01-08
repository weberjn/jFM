
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;

import de.jwi.ftp.FTPUploader;
import de.jwi.jfm.servlets.Controller;
import de.jwi.zip.Unzipper;
import de.jwi.zip.Zipper;

/**
 * @author J�rgen Weber Source file created on 27.03.2004
 */
public class Folder
{

	private boolean isNotInContext;

	String path;

	String url;

	private File myFile;

	File[] children;

	private FileWrapper[] wrappers;
	
	private Map<String,File> nameToFile;
	
	private List<FileWrapper> wrappersList;

	private List parents;

	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	
	private boolean calcRecursiveFolderSize = false;
	
	
    public static final int SORT_NAME_UP = 1;
    public static final int SORT_NAME_DOWN = 2;
    public static final int SORT_DATE_UP = 3;
    public static final int SORT_DATE_DOWN = 4;
    public static final int SORT_SIZE_UP = 5;
    public static final int SORT_SIZE_DOWN = 6;

	public boolean isCalcRecursiveFolderSize()
	{
		return calcRecursiveFolderSize;
	}

	public List getParents()
	{
		return parents;
	}

	private Folder()
	{
		// NOP
	}

	public Folder(File f, String path, String url)
			throws IOException
	{
		myFile = f;
		this.path = path;
		this.url = url;

		if (!myFile.exists())
		{
			throw new IOException(f.getPath() + " does not exist.");
		}
	}

	public List<FileWrapper> getFiles()
	{
		return wrappersList;
	}
	
	public void load()
	{

		children = myFile.listFiles();
		
		if (children == null)
		{
			return; // Windows special folders
		}

		wrappers = new FileWrapper[children.length];
		
		nameToFile = new HashMap<String,File>(children.length);

		for (int i = 0; i < children.length; i++)
		{
			String name = children[i].getName();

			wrappers[i] = new FileWrapper(this, i);
			
			nameToFile.put(name, children[i]);
		}

		wrappersList = Arrays.asList(wrappers);
		
		String[] pp = path.split("/");

		if ("/".equals(path))
		{
			pp = new String[1];
		}

		pp[0] = "/";

		HRef[] parentLinks = new HRef[pp.length];
		String s;
		int p = 0;
		for (int i = 0; i < pp.length - 1; i++)
		{
			s = path.substring(0, 1 + path.indexOf("/", p));
			p = s.length();
			parentLinks[i] = new HRef(pp[i], s);
		}
		parentLinks[pp.length - 1] = new HRef(pp[pp.length - 1], null);

		parents = Arrays.asList(parentLinks);
		
		sort(SORT_NAME_UP);
	}

	private boolean checkFileName(String name)
	{
		if (name.indexOf("..") > -1)
		{
			return false;
		}
		return true;
	}

	private String rename(String[] selectedIDs, String target)
			throws OutOfSyncException
	{
		if (selectedIDs.length > 1)
		{
			return "More than 1 file selected";
		}

		if (!checkFileName(target))
		{
			return "Illegal target name";
		}

		File f = checkAndGet(selectedIDs[0]);

		if (null == f)
		{
			throw new OutOfSyncException();
		}

		File f1 = new File(f.getParent(), target);

		if (f1.exists())
		{
			return target + " allready exists";
		}

		if (!f.renameTo(f1))
		{
			return "failed to rename " + f.getName();
		}

		return "";
	}

	private File getTargetFile(String target) throws IOException
	{
		File f = new File(myFile, target);

		f = f.getCanonicalFile();

		return f;
	}

	private File checkAndGet(String id)
	{
		String s = null;
		try
		{
			s = URLDecoder.decode(id, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// NOP
		}

		String s1 = s.substring(0, s.lastIndexOf('.'));
		String s2 = s.substring(s.lastIndexOf('.') + 1);

		File f = nameToFile.get(s1);

		if (null == f)
		{
			return null; // File not found
		}

		long l = f.lastModified();

		if (!(Long.toString(l).equals(s2)))
		{
			return null; // File modification changed
		}

		return f;

	}

	private void fileCopy(File source, File target) throws IOException
	{
		FileInputStream in = new FileInputStream(source);
		FileOutputStream out = new FileOutputStream(target);
		int c;

		try
		{
			while ((c = in.read()) != -1)
			{
				out.write(c);
			}
			target.setLastModified(source.lastModified());
		}
		finally
		{
			out.close();
			in.close();
		}
	}

	public void sum()
	{
		calcRecursiveFolderSize = true;
	}

	public void sort(int mode)
	{
		Comparator<File> c = null;

		switch (mode)
		{
			case SORT_NAME_UP:
				c = NameFileComparator.NAME_COMPARATOR;
				break;
			case SORT_NAME_DOWN:
				c = NameFileComparator.NAME_REVERSE;
				break;
			case SORT_SIZE_UP:
				c = SizeFileComparator.SIZE_COMPARATOR;
				break;
			case SORT_SIZE_DOWN:
				c = SizeFileComparator.SIZE_REVERSE;
				break;
			case SORT_DATE_UP:
				c = LastModifiedFileComparator.LASTMODIFIED_COMPARATOR;
				break;
			case SORT_DATE_DOWN:
				c = LastModifiedFileComparator.LASTMODIFIED_REVERSE;
				break;
		}

		Arrays.sort(children, c);
	}

	private String copyOrMove(boolean move, String[] selectedIDs, String target)
			throws IOException, OutOfSyncException
	{
		if ((selectedIDs == null) || (selectedIDs.length == 0))
		{
			return "No file selected";
		}

		File f1 = getTargetFile(target);

		if ((null == f1)
				|| (myFile.getCanonicalFile().equals(f1.getCanonicalFile())))
		{
			return "illegal target file";
		}

		if ((!f1.isDirectory()) && (selectedIDs.length > 1))
		{
			return "target is not a directory";
		}

		StringBuffer sb = new StringBuffer();

		File fx = null;

		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			if (!f1.isDirectory())
			{
				fx = f1;
			}
			else
			{
				fx = new File(f1, f.getName());
			}

			if (move)
			{
				if (!f.renameTo(fx))
				{
					sb.append(f.getName()).append(" ");
				}
			}
			else
			{
				try
				{
					FileUtils.copyFile(f, fx, true);
					// fileCopy(f, fx);
				}
				catch (IOException e)
				{
					sb.append(f.getName()).append(" ");
				}
			}
		}

		String s = sb.toString();

		if (!"".equals(s))
		{
			String op = move ? "move" : "copy";
			return "failed to " + op + " " + s + " to " + f1.toString();
		}

		return "";
	}

	private String delete(String[] selectedIDs, String target)
			throws OutOfSyncException
	{
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			if (!f.delete())
			{
				sb.append(f.getName());
			}
		}

		String s = sb.toString();

		if (!"".equals(s))
		{
			return "failed to delete " + s;
		}

		return "";
	}

	private String deleteRecursive(String[] selectedIDs, String target)
			throws OutOfSyncException
	{
		StringBuffer sb = new StringBuffer();

		if (!"YES".equals(target))
		{
			return "Please confirm with YES";
		}

		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			try
			{
				FileUtils.deleteDirectory(f);
			}
			catch (IOException e)
			{
				sb.append(f.getName());
			}
		}

		String s = sb.toString();

		if (!"".equals(s))
		{
			return "failed to delete " + s;
		}

		return "";
	}

	private String ftpToURL(String[] selectedIDs, String target)
			throws OutOfSyncException
	{
		URL url = null;
		String s = "";

		try
		{
			url = new URL("ftp://" + target);
		}
		catch (MalformedURLException e)
		{
			return "Malformed URL";
		}

		ArrayList l = new ArrayList();

		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			l.add(f);
		}

		if (selectedIDs.length > 0)
		{
			s = FTPUploader.upload(url, l);
		}

		return s;
	}

	private String mkdir(String target) throws IOException
	{
		File f = getTargetFile(target);

		if (!f.mkdir())
		{
			return "could not mkdir " + target;
		}

		return "";
	}

	private String getURL(String url)
	{
		URL remote;
		try
		{
			remote = new URL(url);
		}
		catch (MalformedURLException e1)
		{
			return url + "is not a valid URL";
		}
		String s = remote.getFile();
		int p = s.lastIndexOf('/');
		if (p > -1)
		{
			s = s.substring(p);
		}
		File f = new File(myFile, s);

		try
		{
			FileUtils.copyURLToFile(remote, f, 1000, 1000);
		}
		catch (IOException e)
		{
			return "could not get " + remote.toString();
		}

		return "";
	}

	private String join(String[] selectedIDs)
			throws OutOfSyncException, IOException
	{
		Arrays.sort(selectedIDs, new Comparator<String>()
		{

			public int compare(String o1, String o2)
			{
				return (o1).compareTo(o2);
			}
		});

		File target = checkAndGet(selectedIDs[0]);

		if (null == target)
		{
			throw new OutOfSyncException();
		}

		byte[] b = new byte[512];
		int n;

		for (int i = 1; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			FileInputStream fi = null;
			FileOutputStream fo = null;

			try
			{
				fi = new FileInputStream(f);

				fo = new FileOutputStream(target, true);

				while ((n = fi.read(b)) > 0)
				{
					fo.write(b, 0, n);
				}
			}
			finally
			{
				if (null != fo)
				{
					fo.close();
				}
				if (null != fo)
				{
					fi.close();
				}
			}
		}

		return "";
	}


	private String unzip(String[] selectedIDs) throws OutOfSyncException
	{
		StringBuffer sb = new StringBuffer();
		boolean done;

		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			FileInputStream is = null;
			try
			{
				is = new FileInputStream(f);
				Unzipper.unzip(is, myFile);
				done = true;
			}
			catch (FileNotFoundException e)
			{
				done = false;
			}
			catch (IOException e)
			{
				done = false;
			}
			finally
			{
				if (null != is)
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
						// NOP
					}
				}
			}
			if (!done)
			{
				sb.append(f.getName());
			}
		}

		String s = sb.toString();

		if (!"".equals(s))
		{
			return "failed to unzip " + s;
		}

		return "";
	}

	private String zip(OutputStream out, String[] selectedIDs)
			throws IOException, OutOfSyncException
	{

		Collection c = null;

		List l = new ArrayList();

		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			if (f.isDirectory())
			{
				c = FileUtils.listFiles(f, TrueFileFilter.INSTANCE,
						TrueFileFilter.INSTANCE);
				l.addAll(c);
			}
			else
			{
				l.add(f);
			}
		}

		ZipOutputStream z = new ZipOutputStream(out);
		try
		{
			new Zipper().zip(z, l, myFile);
		}
		finally
		{
			z.close();
		}

		return null;
	}

	// caller must have called load() before

	public String action(int action, OutputStream out, String[] selectedIDs,
			String target) throws IOException, OutOfSyncException
	{
		String res = null;

		switch (action)
		{
			case Controller.RENAME_ACTION:
				res = rename(selectedIDs, target);
				break;
			case Controller.COPY_ACTION:
				res = copyOrMove(false, selectedIDs, target);
				break;
			case Controller.MOVE_ACTION:
				res = copyOrMove(true, selectedIDs, target);
				break;
			case Controller.CHMOD_ACTION:
				res = chmod(selectedIDs, target);
				break;
			case Controller.DELETE_ACTION:
				res = delete(selectedIDs, target);
				break;
			case Controller.DELETE_RECURSIVE_ACTION:
				res = deleteRecursive(selectedIDs, target);
				break;
			case Controller.UNZIP_ACTION:
				res = unzip(selectedIDs);
				break;
			case Controller.ZIP_ACTION:
				res = zip(out, selectedIDs);
				break;
			case Controller.MKDIR_ACTION:
				res = mkdir(target);
				break;
			case Controller.GETURL_ACTION:
				res = getURL(target);
				break;
			case Controller.FTPUP_ACTION:
				res = ftpToURL(selectedIDs, target);
				break;
			case Controller.JOIN_ACTION:
				res = join(selectedIDs);
				break;
		}

		if ("".equals(res)) // no error, action succeded.
		{
			load();
		}

		return res;
	}

	public void upload(FileItem item, boolean unzip) throws Exception
	{
		String name = item.getName();

		name = name.replaceAll("\\\\", "/");
		int p = name.lastIndexOf('/');
		if (p > -1)
		{
			name = name.substring(p);
		}
		if (unzip)
		{
			InputStream is = item.getInputStream();
			Unzipper.unzip(is, myFile);
		}
		else
		{
			File f = new File(myFile, name);
			item.write(f);
		}
	}

	private String chmod(String[] selectedIDs, String target)
			throws OutOfSyncException, IOException
	{
		for (int i = 0; i < selectedIDs.length; i++)
		{
			File f = checkAndGet(selectedIDs[i]);

			if (null == f)
			{
				throw new OutOfSyncException();
			}

			chmod(f, target);
		}
		return "";
	}

	private void chmod(File file, String value) throws IOException
	{
		Path p = file.toPath();

		FileSystem fileSystem = FileSystems.getDefault();
		Set<String> fileSystemViews = fileSystem.supportedFileAttributeViews();
		
		if (fileSystemViews.contains("posix"))
		{
			Set<PosixFilePermission> posixFilePermissions = PosixFilePermissions
					.fromString(value);

			if (posixFilePermissions == null)
			{
				throw new IllegalArgumentException(value);
			}

			Files.getFileAttributeView(p, PosixFileAttributeView.class)
					.setPermissions(posixFilePermissions);
		}
		else if (fileSystemViews.contains("dos"))
		{
		}
	}

}