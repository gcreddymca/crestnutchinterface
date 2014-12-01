<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<title>Process Status | Hyperscale Commerce</title>
<script type="text/javascript">
window.onload= function(){
	document.getElementById('statusform').submit();
}
</script>
</head>
 
<body>
<div id="wrapper">
	<jsp:include page="/jsp/profile.jsp"/>
	<div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <span class="active">System Status</span>
            </div>
            <h2>System Status</h2> 
			<form id="statusform" name="status" action="./processForm" method="get">
            <div class="tableData">
            	<input type="hidden" id="eventName" name="event" value="status"/>
                <dl class="tableDataHeading">
                   <dt>Domain Name</dt>
                   <dt>Request Status</dt>
                   <dt>Request Started Time</dt>
                   <dt>Request Completed Time</dt>
                </dl>
                <c:forEach var="status" items='<%=request.getAttribute("statusMap")%>' varStatus="counter">
                   <dl class="tableDataListing">
                       <c:forEach var="message" items="${status.value}">
                          <dd>${message}</dd>
                       </c:forEach>
                   </dl>
                </c:forEach>
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
