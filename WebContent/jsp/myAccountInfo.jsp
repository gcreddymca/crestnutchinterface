<%@ page language="java" contentType="text/html; charset=ISO-8859-1"   pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico" />
<link rel="stylesheet" type="text/css" href="css/style.css" />
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<link rel="stylesheet" type="text/css" href="css/font-awesome.min.css"/>

<title>My Account | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp"/>
    <div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <span class="active">My Account</span>
            </div>
            <h2>My Account</h2>
            <c:if test="${not empty errorMessage}">
                <div id="display-error" class="rounded4 css3">
                    <span>${errorMessage}</span>
                </div>
            </c:if>
			<c:if test="${not empty successMessage}">
                <div id="display-success" class="rounded4 css3">
                    <span>${successMessage}</span>
                </div>
            </c:if>
            <form method="post" action="./loginAuthenticate">
            	<input type="hidden" name="event" value="${eventname}" />
                <div class="myAccount cf">
                    <div class="myAccountUser">
                        <div class="input cf">
                            <label>User Name: </label>
                            <span class="user">${username}</span>
                        </div>
                        <div class="input cf">
                            <label>Role Name: </label>
                            <span class="user">${rolename}</span>
                        </div>
						<div class="button" class="tooltip rounded4 css3">
							<a href="./processForm?event=ChangePwd&uname=${username}">Change Password</a>
                        </div>
					</div>
                    <div class="myAccountAllUser">
                        <c:if test="${rolename eq 'admin' }">
                            <h3>All Users</h3>
							<div class="tableData">
							<dl class="tableDataHeading">
							   <dt>User Name</dt>
							   <dt>Role</dt>
							   <dt>	</dt>
							</dl>
                            <c:if test="${userList!=null}">
                                <c:forEach items="${userList}" var="userObj" varStatus="userCount">
									<c:if test="${userObj.roleName ne 'admin'}">
									<dl class="tableDataListing">
                                    <dd>
										<p>${userObj.userName}</p>  
									</dd>
                                    <dd>
										<p>${userObj.roleName}</p>
									</dd>
                                    <dd>
										<div class="buttons">
                                        <a href="./processForm?event=updateUser&uname=${userObj.userName}" title="Update User" class="tooltip"><i class="fa fa-pencil"></i></a>
                                        <a href="./processForm?event=deleteUser&uname=${userObj.userName}" title="Delete User" class="tooltip"><i class="fa fa-close"></i></a>
										<a href="./processForm?event=ChangePwd&uname=${userObj.userName}" title="ChangePassword" class="tooltip">
										<span class="fa-stack fa" style="margin-left:-9px">
										  <i class="fa fa-lock fa-stack" style="font-size:0.5em; margin-top:9px;"></i>
										  <i class="fa fa-refresh fa-stack-1x"></i>
										</span></a>
										</div>
									</dd>
									</dl>
									</c:if>
                                </c:forEach>
                            </c:if>
							</div>
                            <div class="button" class="tooltip rounded4 css3">
                                <a href="./processForm?event=AddUser">Add User
								<span class="plus-white sprite ml10">Add</span></a>
                            </div>
                        </c:if>
                     </div>
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