<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
  <title>jFM - File Manager</title>
  <meta http-equiv="Content-Type"
 content="text/html; charset=UTF-8">
  <link rel="stylesheet" type="text/css" href="${url}/styles/jFM.css" />
  <link rel="shortcut icon" href="${url}/img/folder.gif" type="image/gif" /> 
</head>




<body bgcolor="#FFFFFF">
<h3>jFM - File Manager </h3>

<c:if test="${!principal}">
<a href="${self}${path}?logout=t" title="Log out">log out ${principal}</a>
</c:if>
<p>

<span style="color: rgb(255, 0, 0);">${actionresult}</span>

<c:if test="${!fatalerror}"> 

<form name="files" action="${self}${path}" method="post">

<table border="1">
<tr>
<td class="title" style="border-right-width: 0;">

<img src="${url}/img/openfolder.gif" title="current folder" width="24" height="24" alt="DIR" border="0">

<c:set var="parentlink" value="" scope="request"/>

<c:forEach var="parent" items="${folder.parents}" varStatus="status">

<c:choose> 
  <c:when test="${parent.isActive}"> 
    <a href="${self}${parent.link}">${parent.display}</a>
  </c:when> 
  <c:otherwise> 
    ${parent.display} 
  </c:otherwise> 
</c:choose>  

<c:if test="${!status.last}"> 
	           &gt;               
	<c:set var="parentlink" value="${self}${parent.link}" scope="request"/>
</c:if> 

</c:forEach>

&nbsp;

<c:if test="${parentlink != ''}">
<a href="${parentlink}"><img src="${url}/img/up-one-dir.gif" title="to parent folder" width="16" height="16" alt="UP" border="0"></a>
  </c:if>

  &nbsp;
  
  <a href="${self}${path}"><img src="${url}/img/reload.gif" title="reload folder" width="24" height="24" alt="RELOAD" border="0"></a>

</td>
<td class="title" style="border-left-width: 0; text-align: right;">${date}</td>
</tr>

</table>


<table class="files">
<thead>

<tr>
<td class="header-center" style="width: 5%;">
<script>
function doChkAll(oChkBox) {
    var bChecked = oChkBox.checked;
    var docFrmChk = document.forms['files'].index;
    for (var i = 0; i < docFrmChk.length; i++) {
        docFrmChk[i].checked = bChecked;
    }
}
</script>
<small>
Check all
<input type="checkbox" name="chkAll" onclick="doChkAll(this);">
</small>
</td>




<td class="header-left" style="width: 40%;"><small>Filename</small>&nbsp;
<a href="${self}${path}?sort=nu">
<img src="${url}/img/shift-up.gif" title="sort by name ascending" width="16" height="16" alt="SORTUP" border="0"></a>
&nbsp;

<a href="${self}${path}?sort=nd">

<img src="${url}/img/shift-down.gif" title="sort by name descending" width="16" height="16" alt="SORTDN" border="0"></a>

</td>
<td class="header-center" style="width: 3em;"><small>Type</small></td>

<td class="header-center" style="width: 10%;"><small>Size</small>
&nbsp;
<a href="${self}${path}?sort=su">
<img src="${url}/img/shift-up.gif" title="sort by size ascending" width="16" height="16" alt="SORTUP" border="0"></a>

&nbsp;

<a href="${self}${path}?sort=sd">
<img src="${url}/img/shift-down.gif" title="sort by size descending" width="16" height="16" alt="SORTDN" border="0"></a>


&nbsp;

<a href="${self}${path}?sum=t">
<img src="${url}/img/sum.gif" title="display folder tree size" width="16" height="16" alt="SUM" border="0"></a>

</td>
<td class="header-center" style="width: 15%;"><small>Last Modification</small>&nbsp;
<a href="${self}${path}?sort=du">
<img src="${url}/img/shift-up.gif" title="sort by date ascending" width="16" height="16" alt="SORTUP" border="0"></a>
&nbsp;

<a href="${self}${path}?sort=dd">

<img src="${url}/img/shift-down.gif" title="sort by date descending" width="16" height="16" alt="SORTDN" border="0"></a>
</td>

<td class="header-center"><small>Attributes</small></td>


</tr>

</thead>

<tbody>

<c:forEach var="file" items="${folder.files}">


<tr>
<td class="row-right"> 

<c:if test="${file.isZip}"> 
<a href="${self}${path}?command=Unzip&index=${file.id}"> <img src="${url}/img/unpack.gif" title="unzip ${file.name}" width="16" height="16" alt="UNZIP" border="0"></a>
</c:if>
<c:if test="${!file.isDirectory}"> 
<a href="${self}${path}?command=Delete&index=${file.id}"> <img src="${url}/img/delete.gif" title="delete ${file.name}" width="16" height="16" alt="DEL" border="0"></a>
</c:if>

<small><input type="checkbox" name="index" value="${file.id}"></small></td>

<td class="row-left"><small><c:choose> 
  <c:when test="${file.isDirectory}"> 
    <a href="${self}${file.path}"><img src="${url}/img/folder.gif" title="folder" width="16" height="16" alt="DIR" border="0"></a>
    <a href="${self}${file.path}">${file.name}</a>
  </c:when> 
  <c:otherwise>
  <a href="${self}${file.path}"><img src="${url}/img/file.gif" title="file" width="16" height="16" alt="FILE" border="0"></a>
  <a href="${self}${file.path}">${file.name}</a>
  </c:otherwise> 
</c:choose>  </small></td>
<td class="row-center">${file.type}</td>

<td class="row-right">${file.size} </td>

<td class="row-center">${file.lastModified}</td>

<td class="row-center-mono">${file.attributes}</td>

</tr>

</c:forEach>

</tbody>
</table>

<table>
<tbody>

<tr>
<td colspan="4" class="header-left">Clipboard on selected Files</td>
</tr>

<tr>
<td class="row-center"><input type="submit" name="command" value="cut" title="cut selected files to clipboard"></td> 
<td class="row-center"><input type="submit" name="command" value="copy" title="copy selected files to clipboard"></td>
<td class="row-center"><input type="submit" name="command" value="paste" title="paste clipboard to current directory"></td>
<td class="row-center"><input type="submit" name="command" value="clear" title="clear clipboard"></td>
<tr>
<c:if test="${not empty sessionScope.clipBoardContent}"> 
<tr><td colspan="4" style="font-size:small;">Clipboard contains ${sessionScope.clipBoardContent.fileCount} files to ${sessionScope.clipBoardContent.kind}.
</td></tr>
<tr><td colspan="4" style="font-size:small;">${sessionScope.clipBoardContent.files}</td></tr>
</c:if>
<tr>
<td colspan="4" class="header-left">Action on selected Files</td>
</tr>


<tr>
<td class="row-right"><input type="submit" name="command" value="Rename to" title="Rename selected file"></td> 
<td class="row-left"><input name="renameto" type="text"></td>



<td class="row-right"> <input type="submit" name="command" value="Delete" title="Delete selected files"></td> 


<td class="row-left"> <input type="submit" name="command" value="Join" title="Append to first selected file other selected files"></td> 


</tr>

<tr>
<td class="row-right"><input type="submit" name="command" value="Copy to" title="Copy selected files"></td> 
<td class="row-left"><input name="copyto" type="text"></td>


<td class="row-right"> <input type="submit" name="command" value="ZipDownload" title="Zip download files"></td> 



</tr>

<tr>
<td class="row-right"><input type="submit" name="command" value="Move to" title="Move selected files"></td> 
<td class="row-left"><input name="moveto" type="text"></td>

<td class="row-right"> <input type="submit" name="command" value="DeleteRecursively" title="Delete selected folders recursively"></td> 
<td class="row-left">type YES <input name="confirm" type="text" size="3" title="Confirm with YES"></td>
</tr>

<tr>
<td class="row-right"><input type="submit" name="command" value="Chmod to" title="Chmod selected files"></td> 
<td class="row-left"><input name="chmodto" type="text" size="9" title="format: rwxr-xr-x"></td>

<td class="row-right"><input type="submit" name="command" value="FtpUpload to" title="ftp upload files"></td>
<td class="row-left"><input name="ftpto" type="text" value="" title="user:password@host/path"></td> 



</tr>



</tbody>
</table>

</form>


<table border="1">
<tbody>
<tr>

<td class="row-center">

<form action="${self}${path}" method="post">

<input type="submit" name="command" value="Mkdir" title="make directory">
<input name="newdir" type="text">

</form>
</td>


</tr>
</tbody>
</table>




<form action="${self}${path}"
 method="post" enctype="multipart/form-data">

<table border="1">
<tbody>
<tr>

<td colspan="2" class="title">
File upload to current directory
</td>

<td colspan="2" class="title">
File upload and unzip to current directory
</td>

</tr>


<tr>
<td class="row-right">Choose file</td> <td class="row-left"><input type="file" name="file"></td>

<td class="row-right">Choose zip file</td> <td class="row-left"><input type="file" name="unzip"></td>

</tr>


<tr>
<td class="row-right">Choose file</td> <td class="row-left"><input type="file" name="myimage"></td> 

<td class="row-right"></td> 


<td class="row-left"><input type="submit" name="command" value="Upload and unzip"/></td>

</tr>



<tr>
<td></td>
<td class="row-left"><input type="submit" name="command" value="Upload"/></td>
</form>


<td class="row-right">Get file from URL</td>

<td class="row-left">

<form action="${self}${path}" method="post">

<input name="url" type="text" title="http or ftp">

<input type="submit" name="command" value="GetURL" title="Upload from URL">

</form>

</td>

</tr>

</tbody>
</table>

</c:if>

<table border="0">
<tbody>
<tr>

<td class="row-left"><small><a href="${jfmhome}" title="${jfmhome}">jFM ${version}</a>(${builddate}) Copyright &copy; 2004,2016 J&uuml;rgen Weber</small></td>

<td class="row-right"><small>jFM running on Java ${javaversion}, ${serverInfo}</small></td>

</tr>

</tbody>
</table>
</body>
</html>