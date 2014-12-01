<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<title>Split Segment ${segment.segmentName} | Hyperscale Commerce</title>
<script language="javascript">

function deleteRow(rowId,tableID)
{
	var table = document.getElementById(tableID);
	var rowCount = table.rows.length;
	for (var i = 0; i < rowCount; i++) {
		var row = table.rows[i];
		if (row.cells[0].innerHTML == rowId) {
			table.deleteRow(i);
			return;
		}
	}
}


function insRow(tableID)
{
    var x=document.getElementById(tableID);
    var new_row = x.rows[0].cloneNode(true);
    var len = x.rows.length;
       var nextNum = parseInt(x.rows[len-1].cells[0].innerHTML)
    new_row.cells[0].innerHTML = nextNum+1;
    
    var inp1 = new_row.cells[2].getElementsByTagName('input')[0];
    inp1.value = '';
    var inp2 = new_row.cells[4].getElementsByTagName('input')[0];
    inp2.value = '';

    var lenght = nextNum+1;
    new_row.deleteCell(6);
    var element1 = new_row.insertCell(6);
    var label1 = document.createElement("label");
	var aTag1 = document.createElement("input");
    aTag1.type="radio";
	aTag1.setAttribute('value',"Yes");
	aTag1.setAttribute('name',"crawl"+(lenght));
	label1.innerHTML = "Yes";
	element1.appendChild(aTag1);
	element1.appendChild(label1);

    var label2 = document.createElement("label");
	var aTag2 = document.createElement("input");
    aTag2.type="radio";
	aTag2.setAttribute('value',"No");
	aTag2.setAttribute('name',"crawl"+(lenght));
	aTag2.setAttribute('checked',"checked");
	label2.innerHTML = "No";
	element1.appendChild(aTag2);
	element1.appendChild(label2);
	
	var aTag3 = document.createElement("input");
    aTag3.type="hidden";
	aTag3.setAttribute('value',(lenght));
	aTag3.setAttribute('name',"sequence_no");
	element1.appendChild(aTag3);
	
    /* var inp5 = new_row.cells[8].getElementsByTagName('input')[0];
    inp5.value = ''; */
  
    var element = new_row.insertCell(7);
	var aTag = document.createElement("a");
	aTag.setAttribute('href',"#");
	aTag.setAttribute('onclick',"deleteRow("+lenght+",'dataTable')");
	aTag.innerHTML = "Delete";
	element.appendChild(aTag);
    
    x.appendChild( new_row );
}
</script>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp" />
    <div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Home</a>
              <span>>></span>
              <a href="segment?domainId=2">Domain URL</a>
              <span>>></span>
              <span class="active">Split Segment</span>
            </div>
            <h2>Split Segment</h2>
             <c:if test="${not empty errorMessageList}">
                <div id="display-error" class="rounded4 css3">
                    <c:forEach var="errorMsg" items="${errorMessageList}">
                        <span>${errorMsg}</span>
                    </c:forEach>
                </div>
            </c:if>
            <c:if test="${not empty errorMessage}">
                <div id="display-error" class="rounded4 css3">
                    <c:forEach var="errorMessage" items="${errorMessage}">
                        <div>${errorMessage}</div>
                    </c:forEach>
                </div>
            </c:if>
             <c:if test="${not empty successMessage}">
                <div id="display-success" class="rounded4 css3">
                    <span>${successMessage}</span>
                </div>
            </c:if>
            
            <form method="get" action="./processSplitSegment">
                <input type="hidden" name="seg_Id" value="${segment.segmentId}" />
                <input type="hidden" name="seg_priority" value="${segment.priority}" />
                <p>Please add segment priority between ${segment.priority} and ${upperLimit}</p>                    
                <p>
                    <span>Split Segement: <strong>${segment.segmentName}</strong></span><br />
                    <span>Url_Pattern_Rule: <strong>${segment.url_pattern_rule}</strong></span>
                </p>
                <div id="dataTable" class="tableData">
                	<c:forEach var="splitSegmentTableRow" items="${segmentList}">
                        <dl class="tableDataListing">
                            <dd>
                                <p>${splitSegmentTableRow.key}</p>
                            </dd>
                            <dd>
                                <p>Segment Name</p>
                            </dd>
                            <dd>
                               <div class="inputbox heading">
                               	   <input type="text" id="segmentName0" name="segmentName" value="${splitSegmentTableRow.value.segmentName}" class="rounded4 css3" />
                               </div>
                            </dd>
                            <dd>
                                <p>Url_Pattern_Rule</p>
                            </dd>
                            <dd>
                               <div class="inputbox heading">
                               	   <input type="text" id="segmentRule0" name="segmentRule" value="${splitSegmentTableRow.value.url_pattern_rule}" class="rounded4 css3" />
                               </div>
                            </dd>
                            <dd class="check">
                                <c:choose>
                                    <c:when test="${splitSegmentTableRow.value.crawl}">
                                        <input type="radio" name="crawl${splitSegmentTableRow.key}" id="yes" value="Yes" checked="checked"/>
                                        <label for="yes">Yes</label>
                                        <input type="radio" name="crawl${splitSegmentTableRow.key}" id="no" value="No"/>
                                        <label for="no">No</label>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="radio" name="crawl${splitSegmentTableRow.key}" id="yes1" value="Yes"/>
                                        <label for="yes1">Yes</label>
                                        <input type="radio" name="crawl${splitSegmentTableRow.key}" id="no1" value="No" checked="checked"/>
                                        <label for="no1">No</label>
                                    </c:otherwise>
                                </c:choose>
                                <input type="hidden" name="sequence_no" value="${splitSegmentTableRow.key}" />
                            </dd>  
                            <c:if test="${splitSegmentTableRow.key ne 1}">
                                <dd>
                                    <a href="#" onClick="deleteRow(${splitSegmentTableRow.key},'dataTable')">Delete</a>
                                </dd>
                            </c:if>  
                        </dl>
                    </c:forEach>
                </div>                    
                <input type="button" value="Add more Segment" title="Add more Segment" onClick="insRow('dataTable')" class="tooltip rounded4 css3" />
                <input type="submit" value="Split Segment" title="Split Segment" name="requestType" class="tooltip rounded4 css3" />
                <input type="submit" value="Cancel" title="Cancel" name="requestType" class="tooltip rounded4 css3" />
            </form>
        </div>
    </div>  
    <div class="push"></div>      
</div>    
<jsp:include page="../footer.jsp" />

<!-- Start Javascript -->     
<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="js/jquery.tooltipster.min.js"></script>
<script type="text/javascript" src="js/general.js"></script>
</body>
</html>