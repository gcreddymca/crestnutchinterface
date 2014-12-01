<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<title>${eventname} | Hyperscale Commerce</title>
</head>
<body>
<div id="wrapper">
<jsp:include page="/jsp/profile.jsp"/>
	<div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <span class="active">${eventname}</span>
            </div>
            <h2>${eventname}</h2>            
            <c:if test="${not empty errorMessageList}">
                <div id="display-error" class="rounded4 css3">
                    <c:forEach var="errorMsg" items="${errorMessageList}">
                        <span>${errorMsg}</span>
                    </c:forEach>
                </div>
            </c:if>
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
            <form method="get" action="./processAddDomain" class="rounded4 css3">
                <input type="hidden" name="event" value="${eventname}"/> 
                <input type="hidden" name="domainId" value="${domain.domainId}"/>
                <c:set var="domainUrl" value="${domain.url}"/>
                <div class="inputbox">
                    <label for="domain_name">Domain Name</label>
                    <input type="text" name="domain_name" id="domain_name" value="${domain.domainName}" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <label for="domain_url">DomainBase URL</label>
                    <input type="text" name="domain_url" id="domain_url" value="${domain.url}" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <label for="seed_url">Seed URL</label>
                    <input type="text" name="seed_url" id="seed_url" value="${domain.seedUrl}" class="rounded4 css3" />
                </div>
                <div class="inputbox">
                    <label for="raw_content_directory">Raw Content Directory</label>
                    <input type="text" name="raw_content_directory" id="raw_content_directory" value="${domain.raw_content_directory}" class="rounded4 css3" />
                </div>
                <div class="inputbox last">
                    <label for="final_content_directory">Final Content Directory</label>
                    <input type="text" name="final_content_directory" id="final_content_directory" value="${domain.final_content_directory}" class="rounded4 css3" />
                </div>
                <div class="button cf">
                    <input type="submit" title="Save" value="Save" name="requestType" class="save tooltip left rounded4 css3" />
                    <input type="submit" title="Cancel" value="Cancel" name="requestType" class="tooltip left rounded4 css3" />
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