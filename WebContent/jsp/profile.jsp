<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:choose>
	<c:when test="${not empty username}">
	  <div id="header">
	     <jsp:include page="../header.jsp"/>
	     <div class="navbar">
	     	<div class="container cf">
	            <ul id="nav">
	               <li><a href="/hm/index" class="first">Plantronics Home</a></li>
	               <li><a href="./processForm?event=status">System Status</a></li>
	               <!--<li><a href="./processForm?event=monitorView"/>Change Monitor</a></li>-->
	               <li><a href="./processForm?event=purgeCDN">Purge CDNCache</a></li>
	            </ul>
	            <div class="welcomeAdmin">
	               <span>Welcome!! <strong>${username}</strong></span>
	               <a href="./loginAuthenticate?requestType=myAccount">MyAccount</a>
	               <a href="./loginAuthenticate?requestType=logOut" class="last">Logout</a>
	            </div>
	         </div>
	     </div>
	  </div>
	</c:when>
	<c:otherwise>
		<jsp:forward page="../homepage.jsp"/>
	</c:otherwise>
</c:choose>

