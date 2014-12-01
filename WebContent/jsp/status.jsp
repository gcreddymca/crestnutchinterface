<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<link rel="stylesheet" type="text/css" href="css/font-awesome.min.css"/>
<title>Process Status | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
	<jsp:include page="/jsp/profile.jsp"/>
	<div class="content">
	<c:if test="${not empty errorMessage}">
		<div id="display-error">
			<span>${errorMessage}</span>
		</div>
	</c:if>
	<c:if test="${not empty successMessage}">
		<div id="display-success">
			<span>${successMessage}</span>
		</div>
	</c:if>
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <span class="active">System Status</span>
            </div>
            <h2>System Status</h2>   
			<form name="status" id="status" method="get" action="./processForm">
			<input type="hidden" name="event" value="reset"/>
			
            <div class="tableData">
                <dl class="tableDataHeading">
                   <dt>REQUEST_ID</dt>
				   <dt>SOURCE</dt>
				   <dt>Domain Name</dt>
					<dt>ACTION</dt>
                   <dt>Request Status</dt>
                   <dt>Request Started Time</dt>
                   <dt>Request Completed Time</dt>
                </dl>
                <c:forEach var="status" items='<%=request.getAttribute("statusMap")%>' varStatus="counter">
                   <dl class="tableDataListing">
                       <c:forEach var="message" items="${status.value}" varStatus="messageCount">
			<c:choose>
			<c:when test="${message eq 'Request is in Process' && messageCount.count == 7}">
				<input type="hidden" name="requestId" value="${status.key}"/>
				<dd>${message}
				<c:if test="${rolename eq 'admin'}">
					<input style="font:10px/6px 'OpenSansSemibold', Arial; color:#fff; margin-left:10px" type="submit"  name="reset" value="Force Clear Lock"/>
				</c:if>
				</dd> 
			</c:when>
			<c:otherwise>
				<dd style="word-break: break-word; width:25px;">${message}</dd>
			</c:otherwise>
			 </c:choose>
                          
                       </c:forEach>
                   </dl>
                </c:forEach>
				
            </div>
			<div>
				<c:if test="${currentPage != 1}">
					<span id="newer" class="button">
						<a href="./processForm?event=status&page=${currentPage - 1}" title="Newer" class="tooltip">
						<i class="fa fa-arrow-circle-left" style="margin-right:5px;"></i>Newer</a>
					</span>
				</c:if>
				<c:if test="${showNextPage}">
					<span id="older" class="button" style="float:right">
						<a href="./processForm?event=status&page=${currentPage + 1}" title="Older" class="tooltip">
						Older<i style="margin-left:4px;" class="fa fa-arrow-circle-right"></i></a>
					</span>
					
				</c:if>
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
