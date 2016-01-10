package de.jwi.jfm.servlets;

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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;

import de.jwi.jfm.ClipBoardContent;
import de.jwi.jfm.Folder;
import de.jwi.jfm.OutOfSyncException;

/**
 * @author Jürgen Weber Source file created on 26.03.2004
 * 
 */
public class Controller extends HttpServlet {

	private static final String PATH_URL_SERVLET = "/path";

	private static final String CTX_DOWNLOAD_SERVLET = "/dlx";

	private static final String FILE_DOWNLOAD_SERVLET = "/dlf";

	private static String version = "unknown";
	private static String builddate = "unknown";

	private static final String VERSIONCONFIGFILE = "/version.txt";

	public static final int NOP_ACTION = 0;

	public static final int RENAME_ACTION = 1;

	public static final int COPY_ACTION = 2;

	public static final int MOVE_ACTION = 3;

	public static final int DELETE_ACTION = 4;

	public static final int DELETE_RECURSIVE_ACTION = 5;

	public static final int MKDIR_ACTION = 6;

	public static final int UNZIP_ACTION = 7;

	public static final int ZIP_ACTION = 8;

	public static final int GETURL_ACTION = 9;

	public static final int FTPUP_ACTION = 10;

	public static final int JOIN_ACTION = 11;

	public static final int CHMOD_ACTION = 12;
	
	public static final int CLIPBOARD_COPY_ACTION = 13;
	
	public static final int CLIPBOARD_CUT_ACTION = 14;
	
	public static final int CLIPBOARD_PASTE_ACTION = 15;
	
	public static final int CLIPBOARD_CLEAR_ACTION = 16;

	private Properties dirmapping = null;

	private File tempDir = null;

	private String filebase = null;

	public void init() throws ServletException {
		tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");

		filebase = getServletContext().getInitParameter("filebase");

		String s = getServletContext().getInitParameter("dirmappings");

		dirmapping = new Properties();

		if (null != s) {
			StringTokenizer st = new StringTokenizer(s, ",");
			while (st.hasMoreTokens()) {
				String s1 = st.nextToken();
				int p = s1.indexOf('=');
				if (p > -1) {
					String key = s1.substring(0, p);
					String val = s1.substring(p + 1);
					dirmapping.setProperty(key, val);
				}
			}
		}

		try {
			InputStream is = getClass().getResourceAsStream(VERSIONCONFIGFILE);

			if (is != null) {
				Properties versionProperties = new Properties();
				versionProperties.load(is);
				is.close();
				
				s = versionProperties.getProperty("version");
				if (null != s) {
					version = s;
				}
				
				s = versionProperties.getProperty("build.date");
				if (null != s) {
					builddate = s;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String self = null;
		String contextPath = null;
		String pathInfo = null;
		Folder folder = null;
		String queryString = null;
		
		try {
			contextPath = request.getContextPath();
			String servletPath = request.getServletPath();
			String method = request.getMethod();
			boolean formPosted = "POST".equals(method);

			pathInfo = request.getPathInfo();

			if (null == pathInfo) {

				PrintWriter writer = response.getWriter();
				writer.print(contextPath + servletPath + " is alive.");

				return;
			}

			File f = new File(filebase, pathInfo);

			if (!f.exists()) {

				PrintWriter writer = response.getWriter();
				writer.print(contextPath + pathInfo + " does not exist.");

				return;
			}
			
			if (f.isFile()) {
				doDownload(request, response, f);
				return;
			}

			if (!pathInfo.endsWith("/")) {
				response.sendRedirect(request.getRequestURL() + "/");
				return;
			}

			queryString = request.getQueryString();

			String pathTranslated = request.getPathTranslated();
			String requestURI = request.getRequestURI();
			String requestURL = request.getRequestURL().toString();

			self = contextPath + servletPath;

			String fileURL = requestURI.replaceFirst(contextPath, "");
			fileURL = fileURL.replaceFirst(servletPath, "");

			folder = new Folder(f, pathInfo, fileURL);

			folder.load();

			String actionresult = "";

			if (FileUpload.isMultipartContent(request)) {
				try {
					actionresult = handleUpload(request, folder);
					folder.load();
				} catch (Exception e) {
					throw new ServletException(e.getMessage(), e);
				}
			} else if (formPosted || null != queryString) {
				try {
					actionresult = handleQuery(request, response, folder);
				} catch (OutOfSyncException e) {
					actionresult = e.getMessage();
				}
				if (null == actionresult) {
					return;
				}
			}

			request.setAttribute("actionresult", actionresult);
		} catch (SecurityException e) {
			request.setAttribute("actionresult", e.getClass().getName() + " " + e.getMessage());
			request.setAttribute("fatalerror", new Boolean(true));

		}

		String s = request.getRemoteUser();

		Principal principal = request.getUserPrincipal();

		if (principal != null) {
			request.setAttribute("principal", principal.getName());
		}

		request.setAttribute("self", self);

		s = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z").format(new Date());
		
		request.setAttribute("date", s);
		
		request.setAttribute("version", version);
		
		request.setAttribute("builddate", builddate);

		request.setAttribute("javaversion", System.getProperty("java.version"));

		request.setAttribute("serverInfo", getServletContext().getServerInfo());

		request.setAttribute("jfmhome", "https://java.net/projects/jfm");
		
		request.setAttribute("url", contextPath);

		request.setAttribute("path", pathInfo);

		request.setAttribute("folder", folder);

		String forward = "/WEB-INF/fm.jsp";
		
		if (queryString != null)
		{
			// hide get query parameters
//			response.sendRedirect(request.getRequestURL() + "");
//			return;
		}

		RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher(forward);

		requestDispatcher.forward(request, response);
	}

	
	
	private String handleQuery(HttpServletRequest request, HttpServletResponse response, Folder folder)
			throws OutOfSyncException, IOException {
		String rc = "";

		HttpSession session = request.getSession();
		
		String target = null;
		int action = NOP_ACTION;
		String[] selectedfiles = request.getParameterValues("index");

		String logout = request.getParameter("logout");
		if ("t".equals(logout)) {
			request.getSession().invalidate();
			return "";
		}

		String sum = request.getParameter("sum");
		if ("t".equals(sum)) {
			folder.sum();
			return "";
		}
		String sort = request.getParameter("sort");
		if ("nd".equals(sort)) {
			folder.sort(Folder.SORT_NAME_DOWN);
			return "";
		} else if ("nu".equals(sort)) {
			folder.sort(Folder.SORT_NAME_UP);
			return "";
		} else if ("su".equals(sort)) {
			folder.sort(Folder.SORT_SIZE_UP);
			return "";
		} else if ("sd".equals(sort)) {
			folder.sort(Folder.SORT_SIZE_DOWN);
			return "";
		}

		else if ("du".equals(sort)) {
			folder.sort(Folder.SORT_DATE_UP);
			return "";
		} else if ("dd".equals(sort)) {
			folder.sort(Folder.SORT_DATE_DOWN);
			return "";
		}

		OutputStream out = null;

		String command = request.getParameter("command");
		if ("Mkdir".equals(command)) {
			target = request.getParameter("newdir");
			action = MKDIR_ACTION;
		}
		if ("GetURL".equals(command)) {
			target = request.getParameter("url");
			action = GETURL_ACTION;
		} else if ("Delete".equals(command)) {
			action = DELETE_ACTION;
		} else if ("Rename to".equals(command)) {
			target = request.getParameter("renameto");
			action = RENAME_ACTION;
		} else if ("Unzip".equals(command)) {
			action = UNZIP_ACTION;
		} else if ("ZipDownload".equals(command)) {
			action = ZIP_ACTION;
			response.setContentType("application/zip");

			response.setHeader("Content-Disposition", "inline; filename=\"jFMdownload.zip\"");

			out = response.getOutputStream();
		} else if ("Copy to".equals(command)) {
			target = request.getParameter("copyto");
			action = COPY_ACTION;
		} else if ("Move to".equals(command)) {
			target = request.getParameter("moveto");
			action = MOVE_ACTION;
		} else if ("Chmod to".equals(command)) {
			target = request.getParameter("chmodto");
			action = CHMOD_ACTION;
		} else if ("DeleteRecursively".equals(command)) {
			target = request.getParameter("confirm");
			action = DELETE_RECURSIVE_ACTION;
		} else if ("FtpUpload to".equals(command)) {
			target = request.getParameter("ftpto");
			action = FTPUP_ACTION;
		} else if ("Join".equals(command)) {
			action = JOIN_ACTION;
		} else if ("cut".equals(command)) {
			action = CLIPBOARD_CUT_ACTION;
		} else if ("copy".equals(command)) {
			action = CLIPBOARD_COPY_ACTION;
		} else if ("paste".equals(command)) {
			action = CLIPBOARD_PASTE_ACTION;
		}
		else if ("clear".equals(command)) {
			action = CLIPBOARD_CLEAR_ACTION;
		}
		
		
				
		if (NOP_ACTION == action) {
			return "";
		}

		try {
			rc = folder.action(action, out, selectedfiles, target, session);
		} catch (SecurityException e) {
			rc = "SecurityException: " + e.getMessage();
			return rc;
		}

		folder.load();

		return rc;
	}

	private String handleUpload(HttpServletRequest request, Folder folder) throws Exception {
		DiskFileUpload upload = new DiskFileUpload();
		upload.setRepositoryPath(tempDir.toString());
		System.out.println(upload.getSizeMax());

		// parse this request by the handler
		// this gives us a list of items from the request
		List items = upload.parseRequest(request);

		Iterator itr = items.iterator();

		boolean unzip = false;

		while (itr.hasNext()) {
			FileItem item = (FileItem) itr.next();

			// check if the current item is a form field or an uploaded file
			if (item.isFormField()) {
				String name = item.getFieldName();
				String value = item.getString();
				if ("command".equals(name) && "unzip".equals(value)) {
					unzip = true;
				}
			} else {
				String name = item.getFieldName();
				unzip = "unzip".equals(name);

				if (!"".equals(item.getName())) {
					folder.upload(item, unzip);
				}
				// the item must be an uploaded file save it to disk. Note that
				// there
				// seems to be a bug in item.getName() as it returns the full
				// path on
				// the client's machine for the uploaded file name, instead of
				// the file
				// name only. To overcome that, I have used a workaround using
				// fullFile.getName().
				/*
				 * File fullFile = new File(item.getName()); File savedFile =
				 * new File(getServletContext().getRealPath("/"),
				 */
				// item.write(savedFile);
			}
		}
		return "";
	}

	public void doDownload(HttpServletRequest request, HttpServletResponse response, File f) throws IOException {

		String name = f.getName();

		String mimeType = getServletContext().getMimeType(name);

		response.setContentType(mimeType);

		response.setHeader("Content-Disposition", "inline; filename=\"" + name + "\"");

		OutputStream out = response.getOutputStream();

		FileInputStream in = new FileInputStream(f);

		byte[] buf = new byte[512];
		int l;

		try {
			while ((l = in.read(buf)) > 0) {
				out.write(buf, 0, l);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			in.close();
		}

	}
}