
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
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/**
 * @author Jürgen Weber Source file created on 27.03.2004
 */
public class FileWrapper {

	private Folder folder;
	int index;

	FileWrapper(Folder folder, int index) {
		this.folder = folder;
		this.index = index;
	}

	public File getFile() {
		return folder.children[index];
	}

	public String getUrl() {
		File file = getFile();
		return folder.url + file.getName();
	}

	public String getId() {
		String s = null;
		File file = getFile();
		try {
			s = URLEncoder.encode(file.getName() + "." + Long.toString(file.lastModified()), "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return s;
	}

	public String getName() {
		return getFile().getName();
	}

	// return the virtual path
	public String getPath() {
		File file = getFile();
		return folder.path + file.getName();
	}

	public String toString() {
		return getFile().toString();
	}

	public String getSize() {
		long l;

		File file = getFile();
		
		l = file.length();

		if (file.isDirectory() && folder.isCalcRecursiveFolderSize()) {
			l = FileUtils.sizeOfDirectory(file);
		}

		String s = humanReadableByteCount(l, true);
		
		return s;
	}

	public boolean getIsDirectory() {
		File file = getFile();
		return file.isDirectory();
	}

	public boolean getIsZip() {
		File file = getFile();
		if (file.isDirectory()) {
			return false;
		}
		String s = file.getName().toLowerCase();

		return s.endsWith(".zip");
	}

	public String getType() throws IOException {
		File file = getFile();
		return file.isDirectory() ? "d" : "";
	}

	public String getLastModified() {
		File file = getFile();
		long l = file.lastModified();
		String s = folder.dateFormat.format(new Date(l));
		return s;
	}

	public String getAttributes() throws IOException {
		FileSystem fileSystem = FileSystems.getDefault();
		Set<String> fileSystemViews = fileSystem.supportedFileAttributeViews();

		File file = getFile();
		Path p = file.toPath();

		try {
		if (fileSystemViews.contains("posix")) {
			Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(p, LinkOption.NOFOLLOW_LINKS);

			PosixFileAttributes attrs = Files.getFileAttributeView(p, PosixFileAttributeView.class).readAttributes();

			String owner = attrs.owner().toString();
			String group = attrs.group().toString();

			String permissions = PosixFilePermissions.toString(attrs.permissions());

			String res = String.format("%s %s %s", permissions, owner, group);
			return res;
		} else if (fileSystemViews.contains("dos")) {
			StringWriter sw = new StringWriter();
			DosFileAttributeView attributeView = Files.getFileAttributeView(p, DosFileAttributeView.class);
			DosFileAttributes dosFileAttributes = null;

				dosFileAttributes = attributeView.readAttributes();
				if (dosFileAttributes.isArchive()) {
					sw.append('A');
				}
				if (dosFileAttributes.isHidden()) {
					sw.append('H');
				}
				if (dosFileAttributes.isReadOnly()) {
					sw.append('R');
				}
				if (dosFileAttributes.isSystem()) {
					sw.append('S');
				}

			return sw.toString();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	// from http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java/3758880
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
}