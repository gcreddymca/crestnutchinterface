<%@ page language="java" contentType="text/html; charset=ISO-8859-1"   pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<title>Update User | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp"/>
    <div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <a href="loginAuthenticate?requestType=myAccount">My Account</a>
              <span>>></span>
              <span class="active">Update User</span>
            </div>
        	<h2>Update User</h2>
            <c:if test="${not empty errorMessage}">
                <div id="display-error" class="rounded4 css3">
                    <c:forEach var="errorMessage" items="${errorMessage}">
                        <span>${errorMessage}</span>
                    </c:forEach>
                </div>
            </c:if>
	    <c:if test="${not empty successMessage}">
                <div id="display-success" class="rounded4 css3">
                    <span>${successMessage}</span>
                </div>
            </c:if>
            <form method="post" action="./loginAuthenticate">
            	<input type="hidden" name="event" value="${eventname}"/> 
                <div class="inputbox cf">
                    <label for="userName">User Name</label>
                    <input name="userName" id="userName" type="text" value="${uname}" readonly="readonly" class="rounded4 css3" />
                </div>
                <div class="inputbox last cf">
                    <label for="roleType">New Role</label>
                    <select name="roleType" size="1" id="roleType">
                       <c:forEach items="${userRoles}" var="userRole" varStatus="ObjCount">
				<option value="${userRole.key}" >${userRole.value}</option>
			</c:forEach>
                    </select>
                </div>
                <div class="button cf">
                    <input type="submit" title="Update User" value="updateUser" name="requestType" class="tooltip mr5 rounded4 css3" />
                    <input type="submit" title="Cancel" value="Cancel" name="requestType" class="tooltip rounded4 css3" />
                </div>
            </form>
		</div>
	</div>
    <c:if test="${not empty errMessage}">
       <c:out value="${errMessage}" />
    </c:if>
    <div class="push"></div>
</div>
<jsp:include page="../footer.jsp" />

<!-- Start Javascript -->     
<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="js/jquery.tooltipster.min.js"></script>
<script type="text/javascript" src="js/general.js"></script>
</body>
</html>