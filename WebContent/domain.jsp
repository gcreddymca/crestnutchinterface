<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="/WEB-INF/custom-functions.tld" prefix="cfn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<link rel="stylesheet" type="text/css" href="css/tooltipster.css"/>
<link rel="stylesheet" type="text/css" href="css/font-awesome.min.css"/>
<title>Domain | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp"/> 
    <div class="content">
    	<div class="container">
	   <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
		<c:if test="${not empty errorMessageList}">
                <div id="display-error" class="rounded4 css3">
                    <c:forEach var="errorMsg" items="${errorMessageList}">
                        <span>${errorMsg}</span><br>
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
		</c:if>	
	<form method="get" action="./processForm">
            <div class="tableData">
			<input type="hidden" name="event"  id="event"/>
                <dl class="tableDataHeading">
					<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
					<dt class="w10">
                        <div class="buttons">
							<p>
								<button type="submit" title="Htmlize Domains" style="background:none" value="submit" onclick="document.getElementById('event').value = 'selectedDomainHtmlize'"><i class="fa fa-file-code-o"></i></button>
							</p>
						</div>
					</dt>
					</c:if>
		            <dt class="w5">
                        <p>Domain Name</p>
                    </dt>
                    <dt class="w20">
                        <p>DomainBase URL</p>
                    </dt>
                    <dt class="w10">
                        <p>Seed URL</p>
                    </dt>
                    <dt class="w20">
                        <p>Raw Content Directory</p>
                    </dt>
                    <dt class="w20">
                        <p>Final Content Directory</p>
                    </dt>
                    <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                    <dt class="w15">
                        
                    </dt>
                    </c:if>
                </dl>
            	<c:forEach var="domain" items="${domain}" varStatus="counter">
            	<c:set var="domainId" value="${domain.domainId}"/>
						
                <dl class="tableDataListing">
					<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
					<dd class="textCenter">
						<input type="checkbox" name="domainIds" value="${domainId}" ></input>
					</dd>
					</c:if>
                    <dd>
                        <p><a href="./segment?domainId=${domain.domainId}">${domain.domainName}</a></p>
                    </dd>
                    <dd>
                        <p>${domain.url}</p>
                    </dd>
                    <dd>
                        <p>${domain.seedUrl}</p>
                    </dd>
                    <dd>
                        <p>${domain.raw_content_directory}</p>
                    </dd>
                    <dd>
                        <p>${domain.final_content_directory}</p>
                    </dd>
                    <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                    <dd>
                        <div class="buttons" style="white-space: nowrap">
                            <c:choose>
                                <c:when test="${empty fn:trim(domain.url) || actionProgress eq true}">
                                   <span class="disable"><i class="fa fa-pencil"></i></span>
                                   <span class="disable"><i class="fa fa-close"></i></span>
                                   <a href="./processForm?event=AutoCrawl&domainId=${domain.domainId}" title="Htmlize Domain" class="tooltip"><i class="fa fa-file-code-o"></i></a>
                                   <a href="./processForm?event=refreshDomain&domainId=${domain.domainId}" title="Refresh Domain" class="tooltip"><i class="fa fa-refresh"></i></a>
                                   <a href="./processForm?event=deleteDomainHtml&domainId=${domain.domainId}" title="Delete Domain Html" class="tooltip last"><i class="fa fa-trash"></i></a>
                                </c:when>
                                <c:otherwise>
                                    <a href="./processForm?event=editDomain&domainId=${domain.domainId}" title="Edit Domain" class="tooltip"><i class="fa fa-pencil"></i></a>
                                    <a href="./processForm?event=deleteDomain&domainId=${domain.domainId}" title="Delete Domain" class="tooltip"><i class="fa fa-close"></i></a>
                                    <c:choose>  
										<c:when test="${fn:length(segRuleChangedDomains) gt 0 && cfn:contains(segRuleChangedDomains, domain.domainId)}">    
											<a href="./processForm?event=AutoCrawl&domainId=${domain.domainId}" title="Htmlize Domain" class="tooltip"><i class="fa fa-file-code-o"></i></a>
											<span class="refresh-dis sprite">Refresh</span>
											<span class="disable"><i class="fa fa-trash"></i></span>
                                        </c:when>
                                        <c:otherwise> 
                                           <a href="./processForm?event=AutoCrawl&domainId=${domain.domainId}" title="Htmlize Domain" class="tooltip"><i class="fa fa-file-code-o"></i></a>
                                           <a href="./processForm?event=refreshDomain&domainId=${domain.domainId}" title="Refresh Domain" class="tooltip"><i class="fa fa-refresh"></i></a>
                                           <a href="./processForm?event=deleteDomainHtml&domainId=${domain.domainId}" title="Delete Domain Html" class="tooltip last"><i class="fa fa-trash"></i></a>
                                        </c:otherwise>  
                                    </c:choose> 
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </dd>
                    </c:if>
                </dl>
            </c:forEach>
            </div>
            <div class="button">
                <c:if test="${rolename eq 'admin'}">
                     <a href="./processForm?event=AddDomain" title="Add Domain" class="tooltip rounded4 css3">Add Domain <i class="fa fa-plus-circle ml5"></i></a>
                </c:if>
            </div>
        </div>
    </div>
    <div class="push"></div>
</div>    
<jsp:include page="footer.jsp"/>

<!-- Start Javascript -->     
<script type="text/javascript" src="js/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="js/jquery.tooltipster.min.js"></script>
<script type="text/javascript" src="js/general.js"></script>
</body>
</html>