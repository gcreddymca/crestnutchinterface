<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<link rel="stylesheet" type="text/css" href="css/font-awesome.min.css"/>
<title>${eventname} | Hyperscale Commerce</title>

<script type="text/javascript">
var count=0;
function show(div) {
	if (document.getElementById(div).style.display == 'none') {
		document.getElementById(div).style.display = 'block';
	} 
}

function hide(div) {
	if (document.getElementById(div).style.display == 'block') {
		document.getElementById(div).style.display = 'none';
	} 
}

function setFolderType(id,block,row){
	var selectedOption = document.getElementById(id).options[document.getElementById(id).selectedIndex].value;
	if(selectedOption != 'resourceName' && selectedOption != ""){
		document.getElementById('folderName_'+block+"_"+row).style.visibility = "visible";
	}else{
		document.getElementById('folderName_'+block+"_"+row).style.visibility = "hidden"; 
	}
}

function setFileType(id,block){
	var selectedOption = document.getElementById(id).options[document.getElementById(id).selectedIndex].value;
	if(selectedOption != 'resourceName' && selectedOption != ""){
		document.getElementById('fileName_' + block).style.visibility = "visible";
	}else{
		document.getElementById('fileName_' + block).style.visibility = "hidden"; 
	}
}
	
function addFolder(blockId,number){
	var parentDiv = document.getElementById("folderBlock"+number);
	var count = parseInt(document.getElementById("folderCount"+number).value) + 1;
	var folder = document.getElementById("folderTemplate");
	var new_folder = folder.cloneNode(true);
	new_folder.setAttribute("style","visibility: visible;display: block");
		
	new_folder.id="folder_"+number+"_"+ count;
		
	var counter = document.getElementById("folderCount"+number);
	counter.value=count;
		
	var folderName = new_folder.getElementsByTagName("input")[1];
	folderName.id="folderName_"+number+"_"+ count;
	folderName.name="folderName"+number;
		
	var option = new_folder.getElementsByTagName("select")[0];
	option.id="folderType_"+number+"_"+count;
	option.name="folderType"+number;
	option.setAttribute("onchange" , "setFolderType('"+option.id+"',"+number+","+count+")");
	parentDiv.appendChild(new_folder);
	}
	
function addHTMLPathBlock(){
		
	var parentDiv = document.getElementById("parentDiv");
	var counter = parseInt(document.getElementById("blockCount").value) + 1;
	if(isNaN(counter)){
		counter = 1;			
	}
	var block = document.getElementById("blockTemplate");
	var new_block = block.cloneNode(true);
	new_block.setAttribute("style","visibility: visible;display: block");
		
	var folderCount = new_block.getElementsByTagName("input")[2];
	folderCount.id="folderCount"+(counter);
		
	var defaultPattern = new_block.getElementsByTagName("input")[0];
	defaultPattern.value=(counter);
	if(counter == 1){
		defaultPattern.checked = true;
	}
		
	new_block.id="block"+(counter); 
		
	var folderBlock = new_block.getElementsByTagName("div")[1];
	folderBlock.id="folderBlock"+(counter);
		
	var folderName = new_block.getElementsByTagName("input")[1];
	folderName.id="folderName_"+(counter)+"_1";
	folderName.name="folderName"+(counter);
		
	var option = new_block.getElementsByTagName("select")[0];
	option.id="folderType"+(counter)+"_1";
	option.name="folderType"+(counter);
	option.setAttribute("onchange" , "setFolderType('"+option.id+"',"+(counter)+",1)");
		
	var addFolder = new_block.getElementsByTagName("input")[3];
	addFolder.setAttribute("onclick" , "addFolder('folderBlock"+(counter)+"',"+(counter)+")");

	var fileOption = new_block.getElementsByTagName("select")[1];
	fileOption.id="fileType_"+(counter);
	fileOption.name="fileType"+(counter);
	fileOption.setAttribute("onchange" , "setFileType(this.id,"+(counter)+")");
		
	var file = new_block.getElementsByTagName("input")[4];
	file.id="fileName_"+(counter);
	file.name="fileName"+(counter);

	var extension = new_block.getElementsByTagName("input")[5];
	extension.id="extension_"+(counter);
	extension.name="fileExt"+(counter);
		
	var blockCounter = document.getElementById("blockCount");
	blockCounter.value = counter;
	
	var deleteOption = new_block.getElementsByTagName("a")[0];
	deleteOption.id="delete"+(counter);
	deleteOption.name="delete"+(counter);
	deleteOption.setAttribute('onclick',"deleteBlock("+counter+")");
	
	var blocks = new_block.getElementsByTagName("input")[6];
	blocks.value= counter;
	
	parentDiv.appendChild(new_block);
}

function deleteBlock(blockID){
	var element = document.getElementById("block"+blockID);
	element.parentNode.removeChild(element);
}

function addTransform(){
	transformType=document.getElementById("transdropdown").value;
	
	var folder = document.getElementById("transformDiv_0");
	var new_folder = folder.cloneNode(true);
	new_folder.setAttribute("style","visibility: visible;display: block");
	count=count+1;
 	new_folder.id="transformDiv_"+count;
 	
	new_folder.getElementsByTagName("input")[0].value=transformType;
 	
	var deleteOption = new_folder.getElementsByTagName("input")[2];
	deleteOption.id="deleteDiv_"+count;
	deleteOption.setAttribute("style","visibility: visible;display: block");
	
	transformBlock.appendChild(new_folder);
}
	
window.onload = function() {  
	document.getElementById("transformDiv_0").style.display="none";
	document.getElementById("deleteDiv_0").style.display="none";
	var segment= "<%=request.getAttribute("segment")%>"; 
	var urltype = "${segment.urlType}";
	if(urltype != "")
	{
		if(urltype.startsWith("Absolute"))
		{
			show('urlType_absolute');
			
		}
		else if(urltype.startsWith("Relative")){
			show('urlType_relative');
			
		}
	}
	
	
}

function removeTransform(cc){
	var pare=cc.split("_");
	var id="transformDiv_"+pare[1];
	var child = document.getElementById(id);
	 var parent = document.getElementById("transformBlock");
	 if(pare[1]==0){
		 parent.removeChild(child);
		 var child1 = document.getElementById(cc);
		 parent.removeChild(child1);
		 
	 }else{
	 	parent.removeChild(child);
	 	var child1 = document.getElementById(cc);
		 parent.removeChild(child1);
	 }
	}
</script>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp" />
    <div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <a href="segment?domainId=${domainId}">${domainName}</a>
              <span>>></span>
              <span class="active">${eventname}</span>
            </div>
            <h2>${eventname}</h2>
            <c:if test="${not empty errorMessage}">
                <div id="display-error" class="rounded4 css3">
                    <div id="display-error" class="rounded4 css3">
                    <span>${errorMessage}</span>
                </div>
                </div>
            </c:if>
            <c:if test="${not empty successMessage}">
                <div id="display-success" class="rounded4 css3">
                    <div>${successMessage}</div>
                </div>
            </c:if>
            <form method="get" action="./processAddSegment">
                <input type="hidden" name="event" value="${eventname}"/> 
                <input type="hidden" name="seg_Id" value="${segment.segmentId}"/> 
                <input type="hidden" name="domainId" value="${domainId}"/>
                <div class="inputbox">
                    <label for="seg_name">Segment Name</label>
                    <input type="text" name="seg_name" value="${segment.segmentName}" id="seg_name" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <label for="rule">URL Pattern Rule</label>
                    <input type="text" name="rule" value="${segment.url_pattern_rule}" id="rule" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <label for="purgeUrl">Purge URL</label>
                    <input type="text" name="purgeUrl" value="${segment.purgeUrl}" id="purgeUrl" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <label for="crawl">Crawl</label>
                    <c:choose>
                        <c:when test="${segment.crawl}">
                            <div class="radios cf">
                                <input type="radio" name="crawl" value="Yes" checked="checked" id="yes1" />
                                <label for="yes1">Yes</label>
                                <input type="radio" name="crawl" value="No" id="no1" />
                                <label for="no1">No</label>
                            </div>
                        </c:when>
                        <c:otherwise>
                        	<div class="radios cf">
                                <input type="radio" name="crawl" value="Yes" id="yes2" />
                                <label for="yes2">Yes</label>
                                <input type="radio" name="crawl" value="No" checked="checked" id="no2" />
                                <label for="no2">No</label>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${eventname eq 'Edit Segment'}">
                    <div class="inputbox">
                        <label for="priority">Priority</label>
                        <input type="text" name="priority" value="${segment.priority}" id="priority" readonly="readonly" class="rounded4 css3" />
                    </div>
                </c:if>
                <div class="inputbox">
                    <label for="crawlInterval">Crawl Interval</label>
                    <input type="text" name="crawlInterval" value="${segment.crawlInterval}" id="crawlInterval" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <h3>Url Type</h3>
                    <c:choose>
                        <c:when test="${not empty segment.urlType}">
                            <c:choose>
                                <c:when test="${segment.urlType == 'AbsoluteWithHttp'}">
                                	 <div class="radios cf">
                                         <input type="radio" name="url" value="absolute" id="Absolute" checked="checked" onclick="show('urlType_absolute'),hide('urlType_relative')" />
                                         <label for="Absolute">Absolute</label>
                                     </div>
                                     <div id="urlType_absolute" class="radios ml10 cf" style="display:none;"> 
                                         <input	type="radio" name="url_type" value="AbsoluteWithHttp" checked="checked" id="absWithhttp" />
                                         <label for="absWithhttp">With Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithoutHttp" id="absWithouthttp" />
                                         <label for="absWithouthttp">Without Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithSlash" id="absWithslash" />
                                         <label for="absWithslash">With Slash</label>
                                     </div>
                                     <div class="radios mt10 cf">
                                         <input type="radio" name="url" value="absolute" id="Relative" onchange="show('urlType_relative'),hide('urlType_absolute')" />
                                         <label for="Relative">Relative</label>
                                     </div>
                                     <div id="urlType_relative" class="radios ml10 cf" style="display:none;"> 
                                         <input	type="radio" name="url_type" value="RelativeWithSlash" id="relWithslash" />
                                         <label for="relWithslash">With Slash</label>
                                         <input type="radio" name="url_type" value="RelativeWithoutSlash" id="relWithoutslash" />
                                         <label for="relWithoutslash">Without Slash</label>
                                     </div>
                                </c:when>
                                <c:when test="${segment.urlType == 'AbsoluteWithoutHttp'}">
                                	 <div class="radios cf">
                                         <input type="radio" name="url" value="absolute" id="Absolute" checked="checked" onclick="show('urlType_absolute'),hide('urlType_relative')" />
                                         <label for="Abosolute">Absolute</label>
                                     </div>
                                     <div id="urlType_absolute" class="radios ml10 cf" style="display:none;">
                                         <input	type="radio" name="url_type" value="AbsoluteWithHttp" id="absWithHttp" />
                                         <label for="absWithHttp">With Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithoutHttp" checked="checked" id="absWithoutHttp" />
                                         <label for="absWithoutHttp">Without Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithSlash" id="absWithSlash" />
                                         <label for="absWithSlash">With Slash</label>
                                     </div>
                                	 <div class="radios mt10 cf">
                                         <input type="radio" name="url" value="absolute" id="Relative" onchange="show('urlType_relative'),hide('urlType_absolute')" />
                                         <label for="Relative">Relative</label>
                                	 </div>
                                     <div id="urlType_relative" class="radios ml10 cf" style="display:none;">
                                         <input	type="radio" name="url_type" value="RelativeWithSlash" id="relWithSlash" />
                                         <label for="relWithSlash">With Slash</label>
                                         <input type="radio" name="url_type" value="RelativeWithoutSlash" id="relWithoutSlash" />
                                         <label for="relWithoutSlash">Without Slash</label>
                                     </div>
                                </c:when>
                                <c:when test="${segment.urlType == 'AbsoluteWithSlash'}">
                                	 <div class="radios cf">
                                         <input type="radio" name="url" value="absolute" id="Absolute" checked="checked" onclick="show('urlType_absolute'),hide('urlType_relative')" />
                                         <label for="Absolute">Absolute</label>
                                     </div>
                                     <div id="urlType_absolute" class="radios ml10 cf" style="display:none;"> 
                                         <input	type="radio" name="url_type" value="AbsoluteWithHttp" id="abWithHttp" />
                                         <label for="abWithHttp">With Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithoutHttp" id="abWithoutHttp" />
                                         <label for="abWithoutHttp">Without Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithSlash" checked="checked" id="abWithSlash" />
                                         <label for="abWithSlash">With Slash</label>
                                     </div>
                                     <div class="radios mt10 cf">
                                         <input type="radio" name="url" value="absolute" id="Relative" onchange="show('urlType_relative'),hide('urlType_absolute')" />
                                         <label for="Relative">Relative</label>
                                     </div>        
                                     <div id="urlType_relative" class="radios ml10 cf" style="display:none;"> 
                                         <input	type="radio" name="url_type" value="RelativeWithSlash" id="reWithslash" />
                                         <label for="reWithslash">With Slash</label>
                                         <input type="radio" name="url_type" value="RelativeWithoutSlash" id="reWithoutSlash" />
                                         <label for="reWithoutSlash">Without Slash</label>
                                     </div>
                                </c:when>
                                 <c:when test="${segment.urlType == 'RelativeWithSlash'}">
                                 	 <div class="radios cf">
                                         <input type="radio" name="url" value="absolute" id="Absolute" onchange="show('urlType_absolute'),hide('urlType_relative')" />
                                         <label for="Absolute">Absolute</label>
                                     </div>
                                     <div id="urlType_absolute" class="radios ml10 cf" style="display:none;">
                                         <input	type="radio" name="url_type" value="AbsoluteWithHttp" id="withHTTP" />
                                         <label for="withHTTP">With Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithoutHttp" id="withoutHTTP" />
                                         <label for="withoutHTTP">Without Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithSlash" id="withSLASH" />
                                         <label for="withSLASH">With Slash</label>
                                     </div>
                                     <div class="radios mt10 cf">
                                         <input type="radio" name="url" value="absolute" id="Relative" checked="checked" onclick="show('urlType_relative'),hide('urlType_absolute')" />
                                         <label for="Relative">Relative</label>
                                     </div>
                                     <div id="urlType_relative" class="radios ml10 cf" style="display:none;">
                                         <input	type="radio" name="url_type" value="RelativeWithSlash" checked="checked" id="withslash" />
                                         <label for="withslash">With Slash</label>
                                         <input type="radio" name="url_type" value="RelativeWithoutSlash" id="withoutslash" />
                                         <label for="withoutslash">Without Slash</label>
                                     </div>						
                                 </c:when>
                                 <c:when test="${segment.urlType == 'RelativeWithoutSlash'}">
                                 	 <div class="radios cf">
                                         <input type="radio" name="url" value="absolute" id="Absolute" onchange="show('urlType_absolute'),hide('urlType_relative')" />
                                         <label for="Absolute">Absolute</label>
                                     </div>
                                     <div id="urlType_absolute" class="radios ml10 cf" style="display:none;">
                                         <input	type="radio" name="url_type" value="AbsoluteWithHttp" id="aWithHttp" />
                                         <label for="aWithHttp">With Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithoutHttp" id="aWithoutHttp" />
                                         <label for="aWithoutHttp">Without Http</label>
                                         <input type="radio" name="url_type" value="AbsoluteWithSlash" id="aWithSlash" />
                                         <label for="aWithSlash">With Slash</label>
                                     </div>
                                 	 <div class="radios mt10 cf">
                                         <input type="radio" name="url" value="absolute" id="Relative" checked="checked" onclick="show('urlType_relative'),hide('urlType_absolute')" />
                                         <label for="Relative">Relative</label>
                                     </div>        
                                     <div id="urlType_relative" class="radios ml10 cf" style="display:none;"> 
                                         <input	type="radio" name="url_type" value="RelativeWithSlash" id="rWithSlash" />
                                         <label for="rWithSlash">With Slash</label>
                                         <input type="radio" name="url_type" value="RelativeWithoutSlash" checked="checked" id="rWithoutSlash" />
                                         <label for="rWithoutSlash">Without Slash</label>
                                     </div>						
                                 </c:when>
                             </c:choose>
                        </c:when>
                        <c:otherwise>
                        	<div class="radios cf">
                                <input type="radio" name="url" value="absolute" id="Absolute" onchange="show('urlType_absolute'),hide('urlType_relative')" />
                                <label for="Absolute">Absolute</label>
                            </div>
                            <div id="urlType_absolute" class="radios ml10 cf" style="display:none;"> 
                                <input type="radio" name="url_type" value="AbsoluteWithHttp" id="absoluteWithHttp" />
                                <label for="absoluteWithHttp">With Http</label>
                                <input type="radio" name="url_type" value="AbsoluteWithoutHttp" id="absoluteWithoutHttp" />
                                <label for="absoluteWithoutHttp">Without Http</label>
                                <input type="radio" name="url_type" value="AbsoluteWithSlash" id="absoluteWithSlash" />
                                <label for="absoluteWithSlash">With Slash</label>
                            </div>
                            <div class="radios mt10 cf">
                                <input type="radio" name="url" value="absolute" id="Relative" onchange="show('urlType_relative'),hide('urlType_absolute')" />
                                <label for="Relative">Relative</label>
                            </div>        
                            <div id="urlType_relative" class="radios ml10 cf" style="display:none;"> 
                                <input type="radio" name="url_type" value="RelativeWithSlash" id="relativeWithSlash" />
                                <label for="relativeWithSlash">With Slash</label>
                                <input type="radio" name="url_type"	value="RelativeWithoutSlash" id="relativeWithoutSlash" />
                                <label for="relativeWithoutSlash">Without Slash</label>
                            </div>		
                        </c:otherwise>
                     </c:choose>
                </div>
                <div id="parentDiv" class="mb10">
                    <h3>HTML Path</h3>
                    <c:forEach items="${segment.pathVO}" var="path"	varStatus="theCount">
                        <div id="block${path.key}">
                            <div id="folderBlock${path.key}">
                                <div class="tableData">
                                    <dl class="tableDataListing">
                                        <dd class="w95">
                                            <div class="tableData">
                                                <dl class="tableDataListing">
                                                    <dd class="inputbox">
                                                        <div class="radios cf">
                                                            <input type="radio" value="${path.key}" id="dfu" name="defaultPattern" <c:if test="${path.value.default}">checked</c:if> />
                                                            <label for="dfu">Default</label>
                                                        </div>
                                                    </dd>
                                                </dl>
                                            </div>
                                            
                                            <div class="tableData">
                                                <c:forEach items="${path.value.folderType}" var="folder" varStatus="folderCount">
                                                    <div id="folder_${path.key}_${folderCount.count}" style="display:block;">
                                                       <div class="tableData">
                                                           <dl class="tableDataHeading">
                                                               <dt>Folder</dt>
                                                               <dt></dt>
                                                           </dl>
                                                           <dl class="tableDataListing">
                                                               <dd class="inputbox w25">
                                                                   <select onchange="setFolderType('folderType_${path.key}_${folderCount.count}',${path.key},${folderCount.count})" id="folderType_${path.key}_${folderCount.count}"
                                                                    name="folderType${path.key}" class="rounded4 css3">
                                                                        <option></option>
                                                                        <option value="resourceName" <c:if test="${folder.folderType eq 'resourceName' }">selected</c:if>>Resource Name</option>
                                                                        <option value="paramName" <c:if test="${folder.folderType eq 'paramName' }">selected</c:if>>Parameter Name</option>
                                                                        <option value="plainText" <c:if test="${folder.folderType eq 'plainText' }">selected</c:if>>Plain Text</option>
                                                                   </select>
                                                               </dd>
                                                               <dd class="inputbox w70"><input type="text" value="${folder.folderName}" id="folderName_${path.key}_${folderCount.count}" name="folderName${path.key}" class="rounded4 css3 w100 inputWhite" <c:if test="${folder.folderType eq 'resourceName' || folder.folderType eq ''}"></c:if> /></dd>
                                                           </dl>
                                                       </div>
                                                    </div>
                                                    <c:set value="${folderCount.count}" var="folderNumber"></c:set>
                                                </c:forEach>
                                                <input type="hidden" value="${folderNumber}" id="folderCount${path.key}" />
                                                <dl class="tableDataListing">
                                                	<dd class="button"><input type="button" value="Add Folder" onclick="addFolder(folderBlock${path.key},${path.key})" title="Add Folder" class="tooltip rounded4 css3" /></dd>
                                                </dl>
                                            </div>
                                            
                                            <div class="tableData">
                                                <dl class="tableDataHeading">
                                                    <dt>File</dt>
                                                    <dt></dt>
                                                </dl>
                                                <dl class="tableDataListing">
                                                    <dd class="inputbox w25">
                                                        <select onchange="setFileType(this.id,${path.key})" id="fileType_${path.key}" name="fileType${path.key}" class="rounded4 css3">
                                                            <option></option>
                                                            <option value="resourceName" <c:if test="${path.value.filetype eq 'resourceName' }">selected</c:if>>Resource Name</option>
                                                            <option value="paramName" <c:if test="${path.value.filetype eq 'paramName' }">selected</c:if>>Parameter Name</option>
                                                            <option value="plainText" <c:if test="${path.value.filetype eq 'plainText' }">selected</c:if>>Plain Text</option>
                                                        </select>
                                                    </dd>
                                                    <dd class="inputbox w75"><input type="text" value="${path.value.fileName}" id="fileName_${path.key}" name="fileName${path.key}" class="rounded4 css3 w100 inputWhite" <c:if test="${path.value.filetype eq 'resourceName' }">style="visibility: hidden"</c:if> /></dd>
                                                </dl>
                                            </div>
                                            
                                            <div class="tableData">
                                                <dl class="tableDataHeading">
                                                    <dt>File Extension</dt>
                                                </dl>
                                                <dl class="tableDataListing">
                                                    <dd class="inputbox">
                                                        <input type="text" value="${path.value.fileExt}" name="fileExt${path.key}" class="rounded4 css3 w100 inputWhite" id="fileExt" />
                                                    </dd>
                                                </dl>
                                            </div>
                                            
                                        </dd>
                                        <dd class="w5"><a href="javascript:void(0);" onclick="deleteBlock(${path.key})" title="Delete" class="tooltip left mt10 ml10"><i class="fa fa-close"></i></a></dd>
                                    </dl>
                                </div>        
                            </div>    
                           
                            <div class="mb10 cf">
                                <input type="button" title="Add More HTML Path" value="Add More HTML Path" name="requestType" onclick='addHTMLPathBlock()' class="tooltip left rounded4 css3" />
                                <!--<a href="javascript:void(0);" onclick="deleteBlock(${path.key})" title="Delete" class="tooltip left mt10 ml10 cancel sprite">Delete</a>-->
                            </div>
                            <input type="hidden" value="${path.key}" name = "blocks" />
                        </div>
                        <c:set var="blockNumber" value="${path.key}"></c:set>
                    </c:forEach>
                    <input type="hidden" value="${blockNumber}" id="blockCount" name="blockCount" />
                </div>
                <h4>Transformations</h4>
                <div id="transformBlock" class="mb10">
                	 <div class="tableData">
                         <c:if test="${segment.editTransformVO!=null}">
                            <c:forEach items="${segment.editTransformVO}" var="editTransObj" varStatus="transCount">
                                <dl class="tableDataListing">
                                    <dd class="inputbox"><label for="transType">Transformation</label></dd>
                                    <dd class="inputbox"><input type="text" value="${editTransObj.transformationType}" id="transType" name="transType" readonly="readonly" class="inputWhite rounded4 css3" /> </dd>
                                    <dd class="inputbox"><label for="transPriority">Priority</label></dd>
                                    <dd class="inputbox"><input type="text" value="${editTransObj.transformationPriority}" id="transPriority" name="transPriority" class="inputWhite rounded4 css3" /></dd>
                                    <dd><a href="./processForm?event=deleteTransform&transformationType=${editTransObj.transformationType}&domainId=${segment.domainId}&segmentId=${segment.segmentId}" title="Delete" class="tooltip"> <i class="fa fa-close"></i></a></dd>
                                </dl>
                            </c:forEach>
                         </c:if>
                     </div>
                     <div id="transformDiv_0">
                     	<div class="tableData">
                            <dl class="tableDataListing">
                                <dd class="inputbox"><label for="transType2">Transformation</label></dd>
                                <dd class="inputbox"><input type="text" id="transType" name="transType" id="transType2" readonly="readonly" class="inputWhite rounded4 css3" /></dd>
                                <dd class="inputbox"><label for="transPriority2">Priority</label></dd>
                                <dd class="inputbox"><input type="text" value="" id="transPriority2" name="transPriority" class="inputWhite rounded4 css3" /></dd>
                                <dd class="button"><input type="button" name="delete" id="deleteDiv_0" value="Delete" title="Delete" onclick="removeTransform(this.id)" class="tooltip rounded4 css3" /></dd>
                            </dl>
                        </div>
                     </div>
                     <div class="inputbox">
                        <select id="transdropdown" name="transdropdown">
                            <c:forEach items="${segment.transformationVO}" var="transObj" varStatus="transObjCount">
                                <option value="${transObj.transformationType}" >${transObj.transformationType}</option> 
                            </c:forEach>
                        </select>
                     </div>
                     <div class="button">
                     	<input type="button" value="Add Transform" onclick="addTransform()" title="Add Transform" class="tooltip rounded4 css3" />
                     </div>
                </div>
                <div class="button cf">
                     <input type="submit" title="Save" value="Save" name="requestType" class="tooltip mr5 rounded4 css3" />
                     <input type="submit" title="Cancel" value="Cancel" name="requestType" class="tooltip rounded4 css3" />
                </div>
        	</form>
            <div id="folderTemplate" style="display:none;">
                <input type="hidden" value="1" id="folderCount" /> 
                <div class="tableData">
                	<dl class="tableDataHeading">
                    	<dt>Folder</dt>
                        <dt></dt>
                        <dt></dt>
                    </dl>
                    <dl class="tableDataListing">
                    	<dd class="inputbox w25">
                        	<select onchange="setFolderType(this.id,1,1)" id="folderType" name="folderType" class="rounded4 css3">
                                <option></option>
                                <option value="resourceName">Resource Name</option>
                                <option value="paramName">Parameter Name</option>
                                <option value="plainText">Plain Text</option>
                            </select>
                        </dd>
                        <dd class="inputbox w70">
                        	<input type="text" value="" id="folderName" name="folderName_1_1" style="visibility: hidden" class="rounded4 css3 w100" />
                        </dd>
                        <dd class="w5"><a href="javascript:void(0);" onclick="deleteBlock(${path.key})" title="Delete" class="tooltip"><i class="fa fa-close"></i></a></dd>
                    </dl>
                </div>
            </div>
			<div id="blockTemplate" style="display:none;">
                <div id="block1" class="mb10">
                    <div id="folderBlock1">
                    <div class="tableData">
                        <dl class="tableDataListing">
                        	<dd class="w95">
                                <div class="tableData">
                                    <dl class="tableDataListing">
                                        <dd class="w25">
                                            <div class="radios cf">
                                                <input type="radio" value="1" name="defaultPattern" id="def" />
                                                <label for="def">Default</label>
                                            </div>
                                        </dd>
                                    </dl>
                                </div>
                                <div class="tableData" id="folder_1_1">
                                    <dl class="tableDataHeading">
                                    	<dt>Folder</dt>
                                        <dt></dt>
                                    </dl>
                                    <dl class="tableDataListing">
                                        <dd class="inputbox w25">
                                            <select onchange="setFolderType('folderType_1_1',1,1)" id="folderType_1_1" name="folderType1" class="rounded4 css3">
                                                <option></option>
                                                <option value="resourceName">Resource Name</option>
                                                <option value="paramName">Parameter Name</option>
                                                <option value="plainText">Plain Text</option>
                                            </select> 
                                        </dd>
                                        <dd class="inputbox"><input type="text" value="" id="folderName_1_1" name="folderName1" style="visibility: hidden" class="rounded4 css3 w100 inputWhite" /></dd>
                                    </dl>
                                    <input type="hidden" value="1" id="folderCount1" />
                                    <dl class="tableDataListing">
                                    	<dd class="button">
                                            <input type="button" value="Add Folder" onclick="addFolder(folderBlock1,1)" title="Add Folder" class="tooltip rounded4 css3" />
                                        </dd>
                                        <dd></dd>
                                    </dl>
                                </div>
                                <div class="tableData">
                                	<dl class="tableDataHeading">
                                    	<dt>File</dt>
                                        <dt></dt>
                                    </dl> 
                                    <dl class="tableDataListing">
                                        <dd class="inputbox w25">
                                            <select onchange="setFileType(this.id,1)" id="fileType_1" name="fileType1" class="rounded4 css3">
                                                <option></option>
                                                <option value="resourceName">Resource Name</option>
                                                <option value="paramName">Parameter Name</option>
                                                <option value="plainText">Plain Text</option>
                                            </select>
                                        </dd>
                                        <dd class="inputbox w70"><input type="text" value="" id="fileName_1" name="fileName1" style="visibility: hidden" class="rounded4 css3 w100 inputWhite" /></dd>
                                    </dl>
                                </div>
                                <div class="tableData">
                                    <dl class="tableDataHeading">
                                        <dt>File Extension</dt>
                                    </dl>
                                    <dl class="tableDataListing">
                                        <dd class="inputbox"><input type="text" value="" name="fileExt1" class="rounded4 css3 w100 inputWhite" /></dd>
                                    </dl>
                                </div>
                            </dd>
                            <dd class="w5"><a href="javascript:void(0);" onclick="deleteBlock(1,'dataTable')" title="Delete" class="tooltip"><i class="fa fa-close"></i></a></dd>
                        </dl>
                    </div>
                </div>
                <input type="hidden" value="${path.key}" name="blocks" />
            </div>
        </div>
    </div>
    <div class="push"></div>
</div>
<jsp:include page="../footer.jsp"/>

<!-- Start Javascript -->     
<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="js/jquery.tooltipster.min.js"></script>
<script type="text/javascript" src="js/general.js"></script>                        
</body>	
</html>