<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<title>Purge CDN Cache | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp" />
    <div class="content">
    	<div class="container">
           <div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <span class="active">Purge CDN Cache</span>
           </div>
           <h2>Purge CDN Cache</h2>
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
		   
           <form action="PurgeServlet" method="post">
                <div class="inputbox">
                    <label for="site">Media Path</label>
                    <div class="inputboxIn cf">
                        <select name="siteName" size="1" id="site">
                            <c:forEach var="site" items="${sitemap}">
                                <option value="${site.value}">${site.key}</option>
                            </c:forEach>
                        </select>
                        <input size="80" type="text" name="mediapath" class="rounded4 css3" />
                    </div>
                </div>    
                <div class="button">
                    <input type="submit" value="Purge" title="Purge" class="tooltip rounded4 css3" />
                </div>
           </form>
           <h3>Examples:</h3>		
           <ul>
              <li>
                  <p>To purge cache of a specific file or URL enter the specific full URL without any query string<br /><strong>e.g. /images/logo.jpg</strong></p>
              </li>
              <li>
                  <p>To purge cache for a group of files, asterisk * can be used as a wild card<br /><strong>e.g. /images/*.jpg</strong></p>
              </li>
              <li>
                  <p>To purge cache for multiple files under a folder - non-recursively<br /><strong>e.g. /images/*.*</strong></p>
              </li>
              <li>
                  <p>To purge cache for all files under a folder - recursively<br /><strong>e.g. /images/*</strong></p>
              </li>
           </ul>
           <h4>Purged URL Details</h4>
           <div class="tableData">
              <dl class="tableDataHeading">
                  <dt><p>URL</p></dt>
                  <dt><p>PURGE_REQUEST_TIME</p></dt>
              </dl>
              <c:forEach var="status" items='<%=request.getAttribute("purgeDetailsMap")%>' varStatus="counter">
                  <dl class="tableDataListing">
                      <c:forEach var="message" items="${status.value}">
                         <dd><p>${message}</p></dd>
                      </c:forEach>
                  </dl>
              </c:forEach>
           </div>		
           <c:if test="${not empty errMessage}">
                <c:out value="${errMessage}" />
           </c:if>
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