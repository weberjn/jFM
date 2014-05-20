<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>jFM - File Manager</title>
  <link rel="stylesheet" type="text/css" href="${url}/styles/jFM.css" />
</head>




<body bgcolor="#FFFFFF">
<h3>jFM - File Manager </h3>

<c:if test="${!principal}">
<a href="${self}${path}?logout=t" title="Log out">log out ${principal}</a>
</c:if>
<p>

<span style="color: rgb(255, 0, 0);">${actionresult}</span>

<c:if test="${!fatalerror}"> 

<form name="files" action="${self}${path}" method="get">

<table border="1" cellpadding="3" cellspacing="0">
<tbody>
<tr>
<td colspan="5" class="title">

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


</tr>
<tr>
<td class="header-right" style="width: 10%;">
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




<td class="header-left"><small>Filename</small>&nbsp;
<a href="${self}${path}?sort=nd">
<img src="${url}/img/shift-down.gif" title="sort by name ascending" width="16" height="16" alt="SORTDN" border="0"></a>

&nbsp;

<a href="${self}${path}?sort=nu">
<img src="${url}/img/shift-up.gif" title="sort by name descending" width="16" height="16" alt="SORTUP" border="0"></a>

</td>
<td class="header-left"><small>Type</small></td>
<td class="header-center"><small>Size</small>
&nbsp;
<a href="${self}${path}?sort=sd">
<img src="${url}/img/shift-down.gif" title="sort by size ascending" width="16" height="16" alt="SORTDN" border="0"></a>

&nbsp;

<a href="${self}${path}?sort=su">
<img src="${url}/img/shift-up.gif" title="sort by size descending" width="16" height="16" alt="SORTUP" border="0"></a>

&nbsp;

<a href="${self}${path}?sum=t">
<img src="${url}/img/sum.gif" title="display folder tree size" width="16" height="16" alt="SUM" border="0"></a>

</td>
<td class="header-center"><small>Last Modification</small>&nbsp;
<a href="${self}${path}?sort=dd">
<img src="${url}/img/shift-down.gif" title="sort by date ascending" width="16" height="16" alt="SORTDN" border="0"></a>
&nbsp;

<a href="${self}${path}?sort=du">
<img src="${url}/img/shift-up.gif" title="sort by date descending" width="16" height="16" alt="SORTUP" border="0"></a>

</td>
</tr>

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
  <a href="${file.url}"><img src="${url}/img/file.gif" title="file" width="16" height="16" alt="FILE" border="0"></a>
  <a href="${file.url}">${file.name}</a>
  </c:otherwise> 
</c:choose>  </small></td>
<td class="row-center">${file.type}</td>

<td class="row-center">${file.size} </td>

<td class="row-center">${file.lastModified}</td>



</tr>

</c:forEach>


<tr>
<td colspan="5" class="header-left">Action on selected Files</td>
</tr>


<tr>
<td class="row-right"> <input type="submit" name="command" value="Rename" title="Rename selected file"></td> 
<td class="row-left">to <input name="renameto" type="text"></td>



<td class="row-right"> <input type="submit" name="command" value="Delete" title="Delete selected files"></td> 


<td class="row-left"> <input type="submit" name="command" value="Join" title="Append to first selected file other selected files"></td> 


</tr>

<tr>
<td class="row-right"> <input type="submit" name="command" value="Copy" title="Copy selected files"></td> 
<td class="row-left">to <input name="copyto" type="text"></td>


<td class="row-right"> <input type="submit" name="command" value="ZipDownload" title="Zip download files"></td> 

<td class="row-left" colspan="2"> <input type="submit" name="command" value="FtpUpload" title="ftp upload files"> to <input name="ftpto" type="text" size="60" value="" title="user:password@host/path"></td> 


</tr>

<tr>
<td class="row-right"> <input type="submit" name="command" value="Move" title="Move selected files"></td> 
<td class="row-left">to <input name="moveto" type="text"></td>

<td class="row-right"> <input type="submit" name="command" value="DeleteRecursively" title="Delete selected folders recursively"></td> 
<td class="row-left">type YES <input name="confirm" type="text" size="3" title="Confirm with YES"></td>
</tr>




</tbody>
</table>

</form>


<table style="width: 60%; text-align: left;" border="1" cellpadding="2"
cellspacing="2">
<tbody>
<tr>

<td class="row-center">

<form action="${self}${path}" method="get">

<input name="newdir" type="text">
<input type="submit" name="command" value="Mkdir" title="make directory">
</form>
</td>


</tr>
</tbody>
</table>




<form action="${self}${path}"
 method="post" enctype="multipart/form-data">

<table border="1" cellpadding="3" cellspacing="0">
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

<form action="${self}${path}" method="get">

<input name="url" type="text" title="http or ftp">

<input type="submit" name="command" value="GetURL" title="Upload from URL">

</form>

</td>

</tr>

</tbody>
</table>

</c:if>

<table border="0" cellpadding="3" cellspacing="0">
<tbody>
<tr>

<td class="row-left"><small><a href="http://jfm.dev.java.net/" title="http://jfm.dev.java.net/">jFM ${version}</a> Copyright &copy; 2004 Juergen Weber</small></td>

<td class="row-right"><small>jFM running on ${serverInfo}</small></td>

</tr>

</tbody>
</table>
</body>
</html>