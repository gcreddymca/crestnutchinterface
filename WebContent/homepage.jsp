<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<link rel="icon" type="image/ico" href="images/favicon.ico" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
<link rel="stylesheet" type="text/css" href="css/tooltipster.css" />
<title>Login | Hyperscale Commerce</title>
</head>
<body class="loginScreen">
<div id="wrapper">
    <c:if test="${empty username}">
        <h1 class="hyperscaleLogo"><a href="/hm/index"><img src="images/logo.png" alt="Hyperscale Commerce" title="Hyperscale Commerce" width="240" height="82" /></a></h1>
        <form action="./loginAuthenticate" method="post" id="loginForm" class="rounded4 css3">
            <div class="login cf">
                <h2>Login</h2>
                <h3 class="plantronicsLogo"><a href="#"><img src="images/logo-plantronics-small.png" alt="Plantronics - Simply Smarter Commnunications" title="Plantronics - Simply Smarter Commnunications" width="130" height="26" /></a></h3>
            </div>
            <p class="error">
              <c:if test="${not empty errorMessage && errorMessage ne 'REQUEST IS IN PROCESS'}">
                <c:out value="${errorMessage}" />
              </c:if>
            </p> 
            <div class="input cf">
                <label for="ctl00_BodyContent_Username">Username</label>
                <div class="inputBox">
                	<input name="username" type="text" id="ctl00_BodyContent_Username" tabindex="1" class="rounded4 css3" />
                    <span>Enter Username for PLT accounts</span>
                </div>
            </div>
            <div class="input cf">
            	<label for="ctl00_BodyContent_Password">Password</label>
                <div class="inputBox">
                	<input name="password" type="password" id="ctl00_BodyContent_Password" tabindex="2" class="rounded4 css3" />
                </div>
            </div>
            <div class="loginBtn">
                <input type="submit" tabindex="3" value="Login" name="requestType" title="Login" class="tooltip rounded4 css3" />
            </div>
        </form>
    </c:if>
    <div class="push"></div>
</div>
<jsp:include page="footer.jsp"/>

<!-- Start Javascript -->     
<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="js/jquery.tooltipster.min.js"></script>
<script type="text/javascript" src="js/general.js"></script>
</body>
</html>