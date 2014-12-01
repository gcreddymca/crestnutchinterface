<%@ page language="java" contentType="text/html; charset=ISO-8859-1"   pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css" />
<link rel="stylesheet" type="text/css" href="css/font-awesome.min.css" />
<title>Monitor View | Hyperscale Commerce</title>

<script type="text/javascript">
var count = 0;
function show (div) {
	if(document.getElementById(div).style.display == 'none'){
		document.getElementById(div).style.display = 'block';		
	}
	
}
function hide (div) {
	if(document.getElementById(div).style.display == 'block'){
		document.getElementById(div).style.display = 'none';		
	}
	
}

function addRegexCDNBlock(){
	regexType=document.getElementById("cdnregexType_m").value;
	//pathType=document.getElementById("cdnfolderType_m").value;
	var path = document.getElementById("regex_cdnBlockDiv_0");
	var new_path = path.cloneNode(true);
	new_path.setAttribute("style","visibility: visible;display: block");
	count=count+1;
 	new_path.id="regex_cdnBlockDiv_"+count;
	new_path.getElementsByTagName("input")[0].value=regexType;
	var cdnregexType = new_path.getElementsByTagName("input")[0];
	cdnregexType.id ="cdnregexType";
	cdnregexType.name ="cdnregexType";
	//document.getElementById("cdnBlocks").value= count;
	
 	
	var deleteOption = new_path.getElementsByTagName("input")[1];
	deleteOption.id="regex_deleteDiv_"+count;
	deleteOption.setAttribute("style","visibility: visible;display: block");
	
	regex_appendDivs.appendChild(new_path);
	document.getElementById("cdnregexType_m").value="";
}
function removeRegex(cc){
	var pare=cc.split("_");
	var id="regex_cdnBlockDiv_"+pare[2];
	var child = document.getElementById(id);
	 var parent = document.getElementById("regex_appendDivs");
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
function addfolderCDNBlock(){
pathType=document.getElementById("cdnfolderType_m").value;
var path = document.getElementById("foldertype_cdnBlockDiv_0");
var new_path = path.cloneNode(true);
new_path.setAttribute("style","visibility: visible;display: block");
	count=count+1;
 	new_path.id="foldertype_cdnBlockDiv_"+count;
	new_path.getElementsByTagName("input")[0].value=pathType;
	var cdnfolderType = new_path.getElementsByTagName("input")[0];
	cdnfolderType.id ="cdnfolderType";
	cdnfolderType.name ="cdnfolderType";
	var deleteOption = new_path.getElementsByTagName("input")[1];
	deleteOption.id="foldertype_deleteDiv_"+count;
	deleteOption.setAttribute("style","visibility: visible;display: block");
foldertype_appendDivs.appendChild(new_path);
document.getElementById("cdnfolderType_m").value="";
}
function addJspPath(){
pathType=document.getElementById("jspFolderPath_m").value;
var path = document.getElementById("jspFolderPath_0");
var new_path = path.cloneNode(true);
new_path.setAttribute("style","visibility: visible;display: block");
	count=count+1;
 	new_path.id="jspFolderPath_"+count;
	new_path.getElementsByTagName("input")[0].value=pathType;
	var deleteOption = new_path.getElementsByTagName("input")[1];
	deleteOption.id="jsp_deleteDiv_"+count;
	deleteOption.setAttribute("style","visibility: visible");
appendJspPath.appendChild(new_path);
document.getElementById("jspFolderPath_m").value="";
}
function addUrls(){
url=document.getElementById("urls").value;
var path = document.getElementById("url_template_0");
var new_path = path.cloneNode(true);
new_path.setAttribute("style","visibility: visible;display: block");
count=count+1;
new_path.id="url_template_"+count;
var inputurl = new_path.getElementsByTagName("input")[0];
inputurl.value=url;
inputurl.id ="refreshUrls";
inputurl.name ="refreshUrls";
//inputurl.setAttribute("style","border:0px");
var deleteOption = new_path.getElementsByTagName("input")[1];
	deleteOption.id="url_deleteDiv_"+count;
	deleteOption.setAttribute("style","visibility: visible");
document.getElementById("specificFileBlocks").value= count;
appendUrls.appendChild(new_path);
document.getElementById("urls").value="";
}
function listbox_moveacross(sourceID, destID) {
    var src = document.getElementById(sourceID);
    var dest = document.getElementById(destID);
 
    for(var selectedcount=0; selectedcount < src.options.length; selectedcount++) {
 
        if(src.options[selectedcount].selected == true) {
                var option = src.options[selectedcount];
 
                var newOption = document.createElement("option");
                newOption.value = option.value;
                newOption.text = option.text;
                newOption.selected = true;
                try {
                         dest.add(newOption, null); //Standard
                         src.remove(selectedcount, null);
                 }catch(error) {
                         dest.add(newOption); // IE only
                         src.remove(selectedcount);
                 }
                selectedcount--;
        }
    }
}

function removeFolderPath(cc){
	var pare=cc.split("_");
	var id="foldertype_cdnBlockDiv_"+pare[2];
	var child = document.getElementById(id);
	 var parent = document.getElementById("foldertype_appendDivs");
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
	
function removeUrls(cc){
	var pare=cc.split("_");
	var id="url_template_"+pare[2];
	var child = document.getElementById(id);
	 var parent = document.getElementById("appendUrls");
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
	
function editDocRoot(){
document.getElementById('docRootId').readOnly = false;
document.getElementById('docRootId').style.boxShadow = "0 0 6px #999999";
}	
	
function removejspPath(cc){
	var pare=cc.split("_");
	var id="jspFolderPath_"+pare[2];
	
	var child = document.getElementById(id);
	 var parent = document.getElementById("appendJspPath");
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
function removeOptions(selectbox)
{
var i;
var src = document.getElementById(selectbox);
for(i=src.options.length-1;i>=0;i--)
{
if(src.options[i].selected == true)
src.remove(i);
}
}
</script>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp"/>
	<div class="content">
    	<div class="container">
           <div class="breadcrumb cf">
              <a href="index">Home</a>
              <span>>></span>
              <span class="active">Change Monitor</span>
           </div>
           <h2>Change Monitor</h2>
           <c:if test="${not empty errorMessage}">
              <div id="display-error" class="rounded4 css3">
                  <c:forEach var="errMessage" items="${errorMessage}">
                      <span>${errMessage}</span>
                  </c:forEach>
              </div>
           </c:if>
           <form method="get" action="./monitorServlet">
           		<h3>Folder/File Monitoring</h3>
           		<input type="hidden" name="event" value="Save" />
                <div class="inputbox">
                    <label for="docRootId">Docroot</label> 
                    <input id="docRootId" type="text" name="docRoot" value="${docRoot}" readonly="readonly" class="w100 rounded4 css3" />
                </div>
                <div class="button">
		    <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">	
                   	 <input type="button" value="Edit" onclick="editDocRoot()" title="Edit" class="tooltip rounded4 css3" />
                    </c:if>
		</div>
                <input type="hidden" value="${domainId}" name="domainId"/>
                <input type="hidden" value="" name="cdnBlocks" id="cdnBlocks"/>
                <input type="hidden" value="" name="specificFileBlocks" id="specificFileBlocks"/>
                <div class="tabs">
                	<ul class="cf">
                    	<li><a href="#tab1" class="tab css3">Static File Folder Monitor</a></li>
                        <li><a href="#tab2" class="tab css3">Specific File Monitor</a></li>
                    </ul>
                    <div class="tabsContainer">
                        <div id="tab1" class="staticFile tabContent">	                
                            <div class="tableData">
                                <dl class="tableDataHeading">
                                    <dt>Folder Path</dt>
                                    <dt></dt>
                                </dl>
                                <c:if test="${not empty folderPaths}">
                                    <c:forEach items="${folderPaths}" var="folderPaths" varStatus="transCount">
                                        <dl class="tableDataListing">
                                            <dd>${folderPaths}</dd>
					<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                                            <dd class="last"><a href="./monitorServlet?event=delete&folderPath=${folderPaths}&domainId=${domainId}" title="Delete Regex" class="tooltip"><i class="fa fa-close"></i></a></dd>
					</c:if>
                                        </dl>
                                    </c:forEach>
                                </c:if>
                            </div>
                            <div id="foldertype_appendDivs">
                            </div>
                            <div class="inputbox">
                                <label for="cdnfolderType_m">Folder Path</label>
                                <input type="text" value="" id="cdnfolderType_m" name="cdnfolderType_m" class="inputWhite rounded4 css3" />
                            </div>
                            <div class="button">
                                <a href="javascript:;" title="Add" onclick="addfolderCDNBlock()" class="tooltip rounded4 css3">Add  <i class="fa fa-plus-circle ml5"></i></a>
                            </div>
                            
                            <div class="tableData">
                                <dl class="tableDataHeading">
                                    <dt>Regex</dt>
                                </dl>
                                <dl class="tableDataListing">
                                    <dd>
                                        <ul class="listing">
                                           <c:if test="${not empty regexList}">
                                               <c:forEach items="${regexList}" var="regex" varStatus="transCount">
						<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                                                  <li><span>${regex}</span> <a href="./monitorServlet?event=delete&regex=${regex}&domainId=${domainId}" title="Delete Regex" class="tooltip"><i class="fa fa-close"></i></a></li>
						</c:if>
                                               </c:forEach>
                                           </c:if>
                                        </ul>
                                    </dd>
                                 </dl>
                            </div>  
                            <div id="regex_appendDivs">
                            </div>
			    <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                            <div class="inputbox">
                                <label for="cdnregexType_m">Regex</label>
                                <input type="text" id="cdnregexType_m" name="cdnregexType_m" class="inputWhite rounded4 css3" />
                            </div>
                            <div class="button">
                                <a href="javascript:;" title="Add" onclick="addRegexCDNBlock()" class="tooltip rounded4 css3">Add <i class="fa fa-plus-circle ml5"></i></a>
                            </div> 
			    </c:if>
                        </div>
                        <div id="tab2" class="specificFile tabContent">
                            <div class="tableData">
                                <dl class="tableDataHeading">
                                    <dt>Folder Path</dt>
                                    <dt>Regex</dt>
                                    <dt>Segments</dt>
                                    <dt>URLs</dt>
                                    <dt></dt>
                                </dl>
                                <c:if test="${not empty segmentUrlMap}">
                                    <c:forEach items="${segmentUrlMap}" var="pathsegments" varStatus="pathcount">
                                        <dl class="tableDataListing">
                                            <c:forEach items="${fn:split(pathsegments.key,',')}" var="regexpath" varStatus="regexpathcount">
                                                <c:if test="${regexpathcount.count == 1}">
                                                    <dd>
                                                        <c:set var="specificRegex" value="${regexpath}"/>
                                                        <span>${regexpath}</span>
                                                    </dd>
                                                </c:if> 
                                                <c:if test="${regexpathcount.count == 2}">
                                                    <dd>
                                                        <span>${regexpath}</span>
                                                    </dd>
                                                </c:if>  
                                            </c:forEach>
                                            <c:choose>
                                                <c:when test="${fn:length(pathsegments.value) > 0}">  
                                                    <c:forEach items="${pathsegments.value}" var="urlsegmentsList" varStatus="urlsegmentcount">
                                                        <c:if test="${urlsegmentcount.count == 1}">
                                                            <dd>
                                                                <c:choose>
                                                                    <c:when test="${empty urlsegmentsList}">
                                                                        <span id="empty_segments">
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <c:forEach items="${urlsegmentsList}" var="segments" varStatus="segmentcount">
                                                                            <c:if test="${segments != null }">
                                                                                <span>${segments}</span><br />
                                                                            </c:if>
                                                                        </c:forEach>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </dd>
                                                        </c:if>
                                                        <c:if test="${urlsegmentcount.count == 2}">
                                                            <dd>
                                                                <c:choose>
                                                                    <c:when test="${empty urlsegmentsList}}">
                                                                        <span id="empty_segments">
                                                            
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                    <c:forEach items="${urlsegmentsList}" var="urls" varStatus="urlcount">
                                                                        <c:if test="${urls != null}">
                                                                            <span>${urls}</span><br>											
                                                                        </c:if>
                                                                    </c:forEach>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </dd>
                                                        </c:if>
                                                    </c:forEach>
                                                    <dd>
							<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                                                        <a href="./monitorServlet?event=delete&specificRegex=${specificRegex}&domainId=${domainId}" title="Delete Regex" class="tooltip"><i class="fa fa-close"></i></a>
							</c:if>
                                                    </dd>
                                                </c:when>
                                            </c:choose>
                                        </dl>
                                    </c:forEach>
                                </c:if>
                            </div>
                            <div class="inputbox">
                                <label for="specificFileregEx">Regex</label> 
                                <input type="text" name="specificFileregEx" id="specificFileregEx" size="30" class="inputWhite rounded4 css3" />
                            </div>
                            <div class="inputbox">
                                <label for="specificFilePath">Folder Path</label>
                                <input type="text" name="specificFilePath" id="specificFilePath" size="30" class="inputWhite rounded4 css3" />
                            </div>
                            <div class="inputbox">
                                <!--<select id="multi_select"  size="6" multiple="multiple" name="SegmentList">
                                    <c:forEach items="${segmentNames}" var="segmentName" varStatus="ObjCount">
                                        <option value="${segmentName}">${segmentName}</option>
                                    </c:forEach>	
                                </select>-->
                                <div class="categories cf">
                                    <div class="categoriesLeft" id="multi_select">
                                        <c:forEach items="${segmentNames}" var="segmentName" varStatus="ObjCount">
                                           <div class="radios cf">
                                               <input type="checkbox" name="${segmentName}" id="${segmentName}" /> 
                                               <label for="${segmentName}" onclick="move_list_items('to_select_list','from_select_list');">${segmentName}</label>
                                           </div>
                                        </c:forEach>	
                                    </div>
                                    <div class="categoriesRight">
                                    	<select size="6" multiple="multiple">
                                            <c:forEach items="${segmentNames}" var="segmentName" varStatus="ObjCount">
                                                <option>${segmentName}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                
                            </div>
                            <!--<div class="mb10">
                                <a onclick="removeOptions('selected')" href="#"><<</a>
                                <a onclick="listbox_moveacross('multi_select', 'selected')" href="#" class="ml10">>></a>                   
                            </div>-->
                            <div id="appendUrls">
                                <div class="inputbox" id="Urls">
                                    <label for="urls">URLs</label>
                                    <input type="text" name="urls" id="urls" size="30" class="inputWhite rounded4 css3" />                                
                                </div>
                                <div class="button">
                                    <a href="javascript:;" title="Add" onclick="addUrls()" class="tooltip rounded4 css3">Add <i class="fa fa-plus-circle ml5"></i></a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
          
            	<h3>JSP File Monitor</h3>
                <div id="saved_jspPaths">
                    <c:if test="${not empty jspPathList}">
                        <c:forEach items="${jspPathList}" var="jspPath" varStatus="jspCount">
                            <div class="inputbox cf" id="saved">
                            	<div class="left">	
                                	<input type="text" value="${jspPath}" name="jspFolderPath_saved" id="jspFolderPath_s" readonly="readonly" class="rounded4 css3" />
                                </div>
                                <div class="left">
				   <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                                	<a href="./monitorServlet?event=delete&jspPath=${jspPath}&domainId=${domainId}" title="Delete Regex" class="tooltip ml10 mt5"><i class="fa fa-close"></i></a>
                                   </c:if>
			        </div>
                            </div>
                        </c:forEach>
                    </c:if>
                </div>
                <div id="appendJspPath">
                </div>
                <div id="specificFile">
		<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                    <div class="inputbox" id="folderPath_text">
                    	<label for="jspFolderPath_m" id="folderPath">Folder Path</label>
	                    <input type="text" name="jspFolderPath_m" id="jspFolderPath_m" class="rounded4 css3" />
                    </div>
                    <div class="button">
    	                <!--<input type="button" value="Add" onClick="addJspPath()" title="Add" class="tooltip rounded4 css3" />-->
                        <a href="javascript:;" title="Add" onclick="addJspPath()" class="tooltip rounded4 css3">Add <i class="fa fa-plus-circle ml5"></i></a>
                    </div>
		    </c:if>		
                </div>
                <div id="jspFolderPath_0" class="displayNone">
                    <div id="saved">
                        <div class="inputbox">
                        	<input type="text" name="jspFolderPath_0" readonly="readonly" class="rounded4 css3" />
                        </div>
                        <div class="button">
                        	<input type="button" name="delete" class="tooltip rounded4 css3 deleteButton" id="jsp_deleteDiv_0" value="Delete" onclick="removejspPath(this.id)" />
                        </div>
                    </div>
                </div>
                <div class="button cf">
		<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                	<input type="submit" name="save" value="Save" title="Save" class="tooltip mr5 rounded4 css3" />
                    <input type="submit" name="cancel" value="Cancel" title="Cancel" class="tooltip rounded4 css3" />
		</c:if>
                </div>
                
                <div id="regex_cdnBlockDiv_0" style="display:none;">
                    <div class="inputbox">
                      <input type="text" id="cdnregexType_0" name="cdnregexType_0" readonly="readonly" class="rounded4 css3" />
                    </div>
                    <div class="button">
                        <input type="button" name="delete" class="tooltip rounded4 css3" id="deleteDiv_0" value="Delete" title="Delete" onclick="removeRegex(this.id)" />
                    </div>
                </div>
                
                <div id="foldertype_cdnBlockDiv_0" style="display:none;">
                    <div class="inputbox">
                        <input type="text" value="" id="cdnfolderType_0" name="cdnfolderType_0" readonly="readonly" class="rounded4 css3" />
                    </div>
                    <div class="button">
                    	<input type="button" name="delete" class="tooltip rounded4 css3" id="deleteDiv_0" value="Delete" title="Delete" onclick="removeFolderPath(this.id)" />
                    </div>
                </div>
                
                <div id="url_template_0" style="display:none;">
                    <div class="inputbox"> 
                        <input type="text" name="refreshUrls_0" readonly="readonly" class="rounded4 css3" />
                    </div>
                    <div class="button">
                        <input type="button" name="delete" class="tooltip rounded4 css3" id="url_deleteDiv_0" value="Delete" title="Delete" onclick="removeUrls(this.id)" />
                    </div>
                    <!--<span>
                    <input type="text" name="refreshUrls_0" size="30" readonly="readonly"/></span>
                    <span><input type="button" name="delete" id="url_deleteDiv_0" value="Delete" onclick="removeUrls(this.id)"/></span>-->
                </div>
           </form>
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