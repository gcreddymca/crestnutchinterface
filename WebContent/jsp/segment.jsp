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
<title>Segment | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp"/>
   	<div class="content">
    	<div class="container">
        	<div class="breadcrumb cf">
              <a href="index">Plantronics</a>
              <span>>></span>
              <span class="active">Domain: ${domainName}</span>
            </div>
            <h2>Segments of Domain: ${domainName}</h2><br>
			<c:forEach var="countDetail" items="${countDetails}" varStatus="counter">
				<c:choose>
					<c:when test="${counter.count == 1}">
					Total No of Segments : ${countDetail}<br/>
					</c:when>
					<c:when test="${counter.count == 2}">
					Total No of Urls  : ${countDetail}<br/>
					</c:when>
					<c:when test="${counter.count == 3}">
					Total Htmlized Pages : ${countDetail}<br/>
					</c:when>
				</c:choose>
			</c:forEach>
			
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
                <input type="hidden" name="domainId" value="${domainId}"/>
                <input type="hidden" name="seedUrl" value="${seedUrl}"/>			
                <input type="hidden" name="event"  id="event"/>
                <div class="buttons textLeft">
		
                </div>
                <div class="tableData">
                    <dl class="tableDataHeading">
						<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                        <dt>
							<div class="buttons">
                            <c:choose>  
								<c:when test="${fn:length(segRuleChangedDomains) gt 0 && cfn:contains(segRuleChangedDomains, domainId)}">  
								  <span class="disable"><i class="fa fa-refresh"></i></span>
								  <span class="disable"><i class="fa fa-close"></i></span>
								</c:when>
								<c:when test="${fn:length(segPathChangedDomains) gt 0 && cfn:contains(segPathChangedDomains, domainId)}">  
								   <span class="disable"><i class="fa fa-refresh"></i></span>
								  <span class="disable"><i class="fa fa-close"></i></span>
								</c:when>
								<c:otherwise> 
								  <button type="submit" style="background:none;margin-left:-15px" onClick="document.getElementById('event').value = 'refreshSelectedSegments'" title="Refresh"><i class="fa fa-refresh"></i></button>
								  <button type="submit" style="background:none;margin-left:-15px" onClick="document.getElementById('event').value = 'deleteSelectedSegmentHtml'" title="Delete"><i class="fa fa-trash"></i></button>
								</c:otherwise> 
							</c:choose>
							</div>
                        </dt>
						</c:if>
                        <dt>
                            <p>Segment Name</p>
                        </dt>
                        <dt>
                            <p>URL Pattern</p>
                        </dt>
                        <dt>
                            <p>Crawl</p>
                        </dt>
                        <dt>
                            <p>URLs Found</p>
                        </dt>
                        <dt>
                            <p>Htmlized Pages</p>
                        </dt>
                        <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                            <dt>
                            </dt>
                        </c:if>
                    </dl>
                    <c:set var="segmentcount" value="0" /> 
                    <c:forEach var="segment" items="${segment}" varStatus="counter">
                        <dl class="tableDataListing">
							<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
							<dd>
								<c:if test="${segment.crawl == 'true' && segment.segmentName != 'default'}">
                                    <input type="checkbox" name="segmentIds" value="${segment.segmentId}" class="checkbox" />
                                </c:if>
                                <c:if test="${segment.crawl == 'false' || segment.segmentName == 'default'}">
                                    <input type="checkbox" name="segmentIds" value="${segment.segmentId}" disabled="disabled" class="checkbox" />
                                </c:if>
                                <c:choose>
                                    <c:when test="${counter.count eq 1 || actionProgress eq true || empty fn:trim(segment.url_pattern_rule)}">  
                                       <span class="disable"><i class="fa fa-angle-up"></i></span>
                                    </c:when>  
                                    <c:otherwise> 
                                        <a href="./processForm?event=swapPriority&segmentId=${segment.segmentId}&domainId=${segment.domainId}&move=up" title="Move Up" class="tooltip"><i class="fa fa-angle-up"></i></a>
                                    </c:otherwise>  
                                </c:choose>
                                <c:choose>
                                    <c:when test="${empty fn:trim(segment.url_pattern_rule) || actionProgress eq true}">  
                                        <span class="disable"><i class="fa fa-angle-down"></i></span>
                                    </c:when>  
                                    <c:otherwise> 
                                    	<a href="./processForm?event=swapPriority&segmentId=${segment.segmentId}&domainId=${segment.domainId}&move=down" title="Move Down" class="tooltip"><i class="fa fa-angle-down"></i></a>
                                    </c:otherwise>  
                                </c:choose>
							</dd>
							</c:if>
							<dd>
                                <p>
                                    <a href="./segmentURLDetail?segmentId=${segment.segmentId}&segmentName=${segment.segmentName}&domainId=${segment.domainId}">${segment.segmentName}</a>
                                </p>
                            </dd>
                            <dd>
                                <p>${segment.url_pattern_rule}</p>
                            </dd>
                            <dd>
                                
				<p>
				<c:choose>
					<c:when test="${segment.crawl}">
						<i class="fa fa-check"></i>
					</c:when>	
					<c:otherwise>
						<i class="fa fa-ban"></i>
					</c:otherwise>
				</c:choose>	
				</p>
                            </dd>
                            <dd class="textRight">
                                <p>${segment.crawledUrlCount}</p>
                            </dd>
                            <dd class="textRight">
                                <p>${segment.htmlPageCount}</p>
                            </dd>
                            <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                                <dd>
                                    <div class="buttons">
                                        <c:choose> 
                                            <c:when test="${empty fn:trim(segment.url_pattern_rule) || actionProgress eq true}">
                                                <span class="disable"><i class="fa fa-pencil"></i></span>
                                                <span class="disable"><i class="fa fa-close"></i></span>
                                                <span class="disable"><i class="fa fa-compress"></i></span>
                                                <span class="disable"><i class="fa fa-expand"></i></span>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="./processForm?event=Edit&segmentId=${segment.segmentId}&domainId=${segment.domainId}" title="Edit Segment" class="tooltip"><i class="fa fa-pencil"></i></a>
                                                <a href="./processForm?event=deleteSegment&segmentId=${segment.segmentId}&domainId=${segment.domainId}" title="Delete Segment" class="tooltip"><i class="fa fa-close"></i></a>
                                                <a href="./processForm?event=Merge&segmentId=${segment.segmentId}&domainId=${segment.domainId}" title="Merge Segment" class="tooltip"><i class="fa fa-compress"></i></a>
                                                <a href="./processForm?event=Split&segmentId=${segment.segmentId}&domainId=${segment.domainId}" title="Split Segment" class="tooltip"><i class="fa fa-expand"></i></a>
                                            </c:otherwise>
                                        </c:choose>
                                        <c:choose>
                                            <c:when test="${fn:length(segRuleChangedDomains) gt 0 && cfn:contains(segRuleChangedDomains, segment.domainId)}">  
                                              	<span class="disable"><i class="fa fa-refresh"></i></span>
                                                <span class="disable"><i class="fa fa-close"></i></span>
                                            </c:when>
					    <c:when test="${fn:length(segPathChangedDomains) gt 0 && cfn:contains(segPathChangedDomains, segment.domainId)}">  
                                              	<span class="disable"><i class="fa fa-refresh"></i></span>
                                                <span class="disable"><i class="fa fa-close"></i></span>
                                            </c:when>
					    <c:when test="${empty fn:trim(segment.url_pattern_rule)}">  
                                              	<span class="disable"><i class="fa fa-refresh"></i></span>
                                                <span class="disable"><i class="fa fa-close"></i></span>
                                            </c:when>
                                            <c:otherwise> 
                                               <a href="./processForm?event=refreshSelectedSegments&segmentId=${segment.segmentId}&domainId=${segment.domainId}" title="Refresh Segment" class="tooltip"><i class="fa fa-refresh"></i></a>
                                               <a href="./processForm?event=deleteSelectedSegmentHtml&segmentId=${segment.segmentId}&domainId=${segment.domainId}" title="Delete Segment Html" class="tooltip"><i class="fa fa-trash"></i></a>
                                            </c:otherwise>  
                                        </c:choose>
                                    </div>  
                                </dd>
                            </c:if>
                        </dl>
                    </c:forEach>
                </div>
                <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                    <div class="button">
                        <c:choose>
                            <c:when test="${actionProgress eq true}">
                                <span class="rounded4 css3 disableSegment">Add Segment<i class="fa fa-plus-circle ml5"></i></span>
                            </c:when>
                            <c:otherwise>
                                 <a href="./processForm?event=Add&domainId=${domainId}&seedUrl=${seedUrl}" title="Add Segment" class="tooltip rounded4 css3">Add Segment<i class="fa fa-plus-circle ml5"></i></a>
                            </c:otherwise>  
                        </c:choose> 
                    </div>
                </c:if>
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