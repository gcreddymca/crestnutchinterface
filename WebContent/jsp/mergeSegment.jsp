<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
<link rel="stylesheet" type="text/css" href="css/tooltipster.css" />
<title>Merge Segment | Hyperscale Commerce</title>
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
              <span class="active">Merge Segment</span>
            </div>
            <h2>Merge Segment</h2>
			<c:if test="${not empty errorMessage}">
                <div id="display-error" class="rounded4 css3">
                    <span>${errorMessage}</span>
                </div>
            </c:if>
            <form method="get" action="./processMergeSegment">
                <input type="hidden" name="event" value="${eventname}"/> 
                <input type="hidden" name="selected_seg_Id" value="${selectedSegmentId}"/> 
                <input type="hidden" name="domainId" value="${domainId}"/> 
                <div class="inputbox">
                    <label for="merge_seg_name">Merge Segment with </label>
                    <select name="merge_seg_name" id="merge_seg_name">
                        <c:forEach var="segment" items="${segment}" varStatus="counter">
                            <c:if test="${segment.segmentId ne selectedSegmentId}">
                                <option value="${segment.segmentId}">${segment.segmentName}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
                <div class="button cf">
                    <input type="submit" title="Save" value="Save" name="requestType" class="tooltip rounded4 css3" />
                    <input type="submit" title="Cancel" value="Cancel" name="requestType" class="tooltip rounded4 css3" />
                </div>
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