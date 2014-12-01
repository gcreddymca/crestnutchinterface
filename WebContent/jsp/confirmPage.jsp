<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<link rel="icon" type="image/ico" href="images/favicon.ico"/>
<link rel="stylesheet" type="text/css" href="css/style.css"/>
<title>Crawl Confirm | Hyperscale Commerce</title>
</head>

<body>
<div id="wrapper">
    <jsp:include page="/jsp/profile.jsp"/>
    <div class="content">
    	<div class="container">
            <form method="get" name="confirm" action="./processForm">
                <input type="hidden" name="event" value="<%=request.getAttribute("eventname")%>" />
                <c:choose>
                    <c:when test="${eventname == 'AutoCrawlConfirm'}">
                        <div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <p>Are you sure you want to start <strong>${domainNameList} </strong>Domain  HTMLIZATION Process?</p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="AutoCrawl" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description: (If you click on Yes it starts domain htmlization process(In domain Htmlization process it starts the crawling process and generates url html locations based on urls,finally it generates the html files) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'htmlizeSelectedSegmentsConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
                             <c:forEach var="checkedsegment" items="${checkedsegments}">
                                <input type="hidden" name="checkedSegments" value="${checkedsegment}" />
                            </c:forEach>
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <p>Are you sure you want to start Htmlize Selected Segments Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="htmlizeSelectedSegments" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description: (If you click on Yes it starts selected segments htmlization process(In selected segments Htmlization process it starts the crawling process to that segements and it keeps all the remaining segments as non crawl segments and generates url html locations based on urls,finally it generates the html files) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'refreshSelectedSegmentsConfirm'}">
                    	<div class="textCenter">
                             <input type="hidden" name="domainId" value="${domainId}" />
                             <c:forEach var="checkedsegment" items="${checkedsegments}">
                                <input type="hidden" name="checkedSegments" value="${checkedsegment}" />
                            </c:forEach>
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <p>Are you sure you want to start Refresh Selected Segments Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="refreshSelectedSegments" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description: (If you click on Yes it starts  selected segments  refresh process(In selected segments refresh process it opens the http connection and it get's the content of html page. after transformation it generates final html files) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'refreshDomainConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}"/>
							<input type="hidden" name="actionName" value="${actionName}"/>
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <p>Are you sure you want to start Refresh Domain Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="refreshDomain" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>
                            </div>
                            <p>Description: (If you click on Yes it starts  refresh Domain Process(In  refresh Domain process it opens the http connection for each live crawled  url and it get's the content of the html page. after transformation it generates final html files) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'refreshURLConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
                            <input type="hidden" name="segmentId" value="${segmentId}" />
							<input type="hidden" name="actionName" value="${actionName}" />
                            <input type="hidden" name="url" value="${url}"/>
                            <p>Are you sure you want to start Refresh URL Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="refreshURL" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description: If you click on Yes it starts  refresh URL Process(In  refresh URL Process it opens the http</p><p> connection that  url and it get's the content of the html page. after transformation it generates final html file for that url) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'refreshSelectedURLSConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
                            <input type="hidden" name="segmentId" value="${segmentId}" />
                            <c:forEach var="selectedURL" items="${selectedURLS}">
                            	<input type="hidden" name="selectedURLS" value="${selectedURL}" />
                        	</c:forEach>
							<input type="hidden" name="actionName" value="${actionName}"/>
                        	<p>Are you sure you want to start Refresh Selected URLS Process?</p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="refreshSelectedURLS" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>
                            </div>
                            <p>Description: (If you click on Yes it starts  Refresh Selected URLS Process(In Refresh Selected URLS Process it opens the http connection for each url and it get's the content of the html page for each url. after transformation it generates final html files for that urls)or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'deleteURLHtmlConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
                            <input type="hidden" name="segmentId" value="${segmentId}" />
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <input type="hidden" name="url" value="${url}"/>
                            <p>Are you sure you want to start Delete URL HTML File Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="deleteURLHtml" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description:(If you click on Yes it starts Delete URL HTML File Process(In Delete URL HTML File Process it deletes the html file from that location) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'deleteDomainHtmlConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <p>Are you sure you want to start Delete Domain Html Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="deleteDomainHtml" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description: (If you click on Yes it starts Delete Domain Html Process(In  Delete Domain Html Process it deletes all the html files from final content directory) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'deleteSelectedSegmentHtmlConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
                             <c:forEach var="checkedsegment" items="${checkedsegments}">
                                <input type="hidden" name="checkedSegments" value="${checkedsegment}" />
                            </c:forEach>
							<input type="hidden" name="actionName" value="${actionName}"/>
                            <p>Are you sure you want to start Delete Selected Segments Html Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="deleteSelectedSegmentHtml" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                            <p>Description: (If you click on Yes it starts Delete Selected Segments Html Process(In Delete Selected Segments Html Process it deletes all the html files from final content directory to that segments) or if you click on No it redirects to the home page)</p>
                        </div>
                    </c:when>
                    <c:when test="${eventname == 'deleteSelectedURLHtmlConfirm'}">
                    	<div class="textCenter">
                            <input type="hidden" name="domainId" value="${domainId}" />
                            <input type="hidden" name="segmentId" value="${segmentId}" />
                             <c:forEach var="selectedURL" items="${selectedURLS}">
                                <input type="hidden" name="selectedURLS" value="${selectedURL}" />
                            </c:forEach>
							<input type="hidden" name="actionName" value="${actionName}"/>
                        	<p>Are you sure you want to start Delete Selected URL Html Process?<br><Strong>${actionName}</Strong></p>
                            <div class="button buttonConfirm cf">
                                <input type="submit" name="deleteSelectedURLHtml" value="Yes" title="Yes" class="rounded4 css3" />
                                <a href="./index" title="No" class="rounded4 css3">No</a>	
                            </div>
                        </div>
                    </c:when>
					<c:when test="${eventname == 'selectedDomainHtmlizeConfirm'}">
						<div class="textCenter">
							<c:forEach var="domainId" items="${domainIdList}">
								<input type="hidden" name="domainIds" value="${domainId}"/>
							</c:forEach>
							<p>Are you sure you want to start  <strong>${domainNameList} </strong> Domains HTMLIZATION process?</p>
							<div class="button buttonConfirm cf">
								<input type="submit" name="SelectedDomainHtmlization" value="Yes" title="Yes" class="rounded4 css3"/>
								<a href="./index" title="No" class="rounded4 css3">No</a>	
							</div>
						</div>
					</c:when>
					<c:when test="${eventname == 'deleteSegmentConfirm'}">
						<div class="textCenter">
							<input type="hidden" name="domainId" value="${domainId}"/>
							<input type="hidden" name="segmentId" value="${segmentId}"/>		
							<p>Are you sure you want to delete the segment?<br><Strong>${actionName}</Strong></p>
							<div class="button buttonConfirm cf">
								<input type="submit" name="deleteSegment" value="Yes" title="Yes" class="rounded4 css3"/>
								<a href="./segment?domainId=${domainId}" title="No" class="rounded4 css3">No</a>	
							</div>
						</div>
					</c:when>
					<c:when test="${eventname == 'deleteDomainConfirm'}">
						<div class="textCenter">
							<input type="hidden" name="domainId" value="${domainId}"/>
							<p>Are you sure you want to delete the segment?<br><Strong>${actionName}</Strong></p>
							<div class="button buttonConfirm cf">
								<input type="submit" name="deleteSegment" value="Yes" title="Yes" class="rounded4 css3"/>
								<a href="./index" title="No" class="rounded4 css3">No</a>	
							</div>
						</div>
					</c:when>
					<c:when test="${eventname == 'deleteUserConfirm'}">
						<div class="textCenter">
							<input type="hidden" name="uname" value="${uname}"/>
							<p>Are you sure you want to delete the User:<Strong>${uname}</Strong>?</p>
							<div class="button buttonConfirm cf">
								<input type="submit" name="deleteUser" value="Yes" title="Yes" class="rounded4 css3"/>
								<a href="./loginAuthenticate?requestType=myAccount" title="No" class="rounded4 css3">No</a>	
							</div>
						</div>
					</c:when>
					<c:when test="${eventname == 'deleteTransformConfirm'}">
						<div class="textCenter">
							<input type="hidden" name="domainId" value="${domainId}"/>
							<input type="hidden" name="segmentId" value="${segmentId}"/>
							<input type="hidden" name="transformationType" value="${transformationType}"/>
							<p>Are you sure you want to delete the Transformation:<Strong>${transformationType}</Strong>?</p>
							<div class="button buttonConfirm cf">
								<input type="submit" name="deleteUser" value="Yes" title="Yes" class="rounded4 css3"/>
								<a href="./processForm?event=Edit&segmentId=${segmentId}&domainId=${domainId}" title="No" class="rounded4 css3">No</a>	
							</div>
						</div>
					</c:when>
					<c:when test="${eventname == 'resetConfirm'}">
						<input type="hidden" name="requestId" value="${requestId}"/>
						<input type="hidden" name="event" value="reset"/>		
						<p>Are you sure you want to clear the lock?</p>
						<div class="button buttonConfirm cf">
							<input type="submit" name="resetConfirm" value="Yes"/>
							<a href="./processForm?event=status" title="No" class="rounded4 css3">No</a>	
						</div>	
					</c:when>
					<c:when test="${eventname == 'swapPriorityConfirm'}">
						<input type="hidden" name="domainId" value="${domainId}"/>
						<input type="hidden" name="segmentId" value="${segmentId}"/>
						<input type="hidden" name="move" value="${move}"/>
						<input type="hidden" name="event" value="swapPriority"/>		
						<p>Are you sure you want to swap priority of segment?</p>
						<div class="button buttonConfirm cf">
							<input type="submit" name="swapPriorityConfirm" value="Yes"/>
							<a href="./segment?domainId=${domainId}" title="No" class="rounded4 css3">No</a>	
						</div>	
					</c:when>
                </c:choose>
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
