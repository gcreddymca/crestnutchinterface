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
<title>SegmentURL Detail | Hyperscale Commerce</title>

<script type="text/javascript">
function OnSubmitForm(){
	 if(document.event == 'refreshsubmit' || document.event == 'deletesubmit'){
		document.urlDetailform.action="./processForm";
		if(document.event == 'refreshsubmit'){
			document.getElementById('event').value = "refreshSelectedURLS";
		}else{
			document.getElementById('event').value = "deleteSelectedURLHtml";
		}
	 }else if(document.event == '< Back'){
		 document.urlDetailform.action="./segment";
	 }
	return true;
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
              <a href="segment?domainId=${domainId}">Domain: ${domainDetail.domainName}</a>
              <span>>></span>
              <span class="active">Segment: ${segmentDetail.segmentName}</span>
           </div>
           <h2>URLs of Segment: ${segmentDetail.segmentName}</h2>  
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
           <form method="get"  name="urlDetailform" onSubmit="return OnSubmitForm()">
                <input type="hidden" name="domainId" value="${domainId}"/>
                <input type="hidden" name="segmentId" value="${segmentId}"/>
                <input type="hidden" name="event" id="event" value=""/>
                
                <span>
					<c:forEach var="countDetail" items="${urlDetailCount}" varStatus="counter">
				<c:choose>
					<c:when test="${counter.count == 1}">
						Total No of URLS : ${countDetail}<br/>
					</c:when>
					<c:when test="${counter.count == 2}">
						Total No of Htmlized Pages : ${countDetail}<br/>
					</c:when>
				</c:choose>
			</c:forEach>
                	RawContent Directory: ${domainDetail.raw_content_directory}<br />
                	HTML Content Directory: ${domainDetail.final_content_directory}
                </span>
                <div class="tableData">
                	<dl class="tableDataHeading">
						<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                        <dt class="w10">
                           <div class="buttons">
                                <c:choose>  
									<c:when test="${fn:length(segRuleChangedDomains) gt 0 && cfn:contains(segRuleChangedDomains, domainId)}">  
									  <span class="disable"><i class="fa fa-refresh"></i></span>
									  <span class="disable"><i class="fa fa-trash"></i></span>
									</c:when>
									<c:when test="${fn:length(segPathChangedDomains) gt 0 && cfn:contains(segPathChangedDomains, domainId)}">  
									   <span class="disable"><i class="fa fa-refresh"></i></span>
									  <span class="disable"><i class="fa fa-trash"></i></span>
									</c:when>
									<c:otherwise>
										<button type="submit" style="background:none; margin-left:-15px" onClick="document.event=this.value" title="Refresh" value="refreshsubmit"><i class="fa fa-refresh"></i></button>
										<button type="submit" style="background:none; margin-left:-15px" onClick="document.event=this.value" title="Delete" value="deletesubmit"><i class="fa fa-trash"></i></button>
									</c:otherwise>
                                </c:choose>
                            </div>
                         </dt>
						</c:if>
                        <dt class="w25">
                            <p>URL</p>
                        </dt>
                        <dt class="w35">
                            <p>HTML File Location</p>
                        </dt>
                        <dt class="w20">
                            <p>Status</p>
                        </dt>
                        <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                            <dt class="w10">
                            </dt>
                        </c:if>
                    </dl>
                    
                    <c:forEach var="segmentDetailURL" items="${segmentDetail.urlHtmlLocVO}" varStatus="counterURL">
                        <dl class="tableDataListing">
							<c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
			                <dd class="textCenter">
								<input type="checkbox" name="selectedURLS" value="${segmentDetailURL.url}"/>
							</dd>
							</c:if>
                            <dd>
                                <p>${segmentDetailURL.url}</p>
                            </dd>
                            <dd>
                                <p>${segmentDetailURL.urlLoc}</p>
                            </dd>
                            <dd>
                                <c:choose>
                                    <c:when test="${segmentDetailURL.htmlFileStatus eq 0}">
                                        <p>${segmentDetailURL.lastFetchedTime}</p>
                                    </c:when>
                                    <c:otherwise>
                                        <p style="color:red">FileDeleted:&nbsp;&nbsp;${segmentDetailURL.lastFetchedTime}</p>
                                    </c:otherwise>
                                </c:choose>
                            </dd>
                            <c:if test="${rolename eq 'admin' || rolename eq 'manager'}">
                                <c:choose> 
									<c:when test="${fn:length(segRuleChangedDomains) gt 0 && cfn:contains(segRuleChangedDomains, domainId)}">  
									  <span class="disable"><i class="fa fa-refresh"></i></span>
                                               				  <span class="disable"><i class="fa fa-trash"></i></span>
									</c:when>
									<c:when test="${fn:length(segPathChangedDomains) gt 0 && cfn:contains(segPathChangedDomains, domainId)}">  
									    <span class="disable"><i class="fa fa-refresh"></i></span>
                                               				    <span class="disable"><i class="fa fa-trash"></i></span>
									</c:when>
                                    <c:when test="${segmentDetailURL.htmlFileStatus eq 1}">  
                                           <a href="./processForm?event=refreshURL&segmentId=${segmentId}&domainId=${domainId}&url=${segmentDetailURL.url}" title="Refresh URL" class="tooltip"><i class="fa fa-refresh"></i></a>
                                           <span class="disable"><i class="fa fa-trash"></i></span>
                                    </c:when>	
                                    <c:otherwise>
                                        <dd>
                                        	<div class="buttons">
                                        		<a href="./processForm?event=refreshURL&segmentId=${segmentId}&domainId=${domainId}&url=${segmentDetailURL.url}" title="Refresh URL" class="tooltip"><i class="fa fa-refresh"></i></a>
                                            	<a href="./processForm?event=deleteURLHtml&segmentId=${segmentId}&domainId=${domainId}&url=${segmentDetailURL.url}" title="Delete URL" class="tooltip"><i class="fa fa-trash"></i></a>
                                            </div>
                                        </dd>
                                    </c:otherwise>  
                                </c:choose>
                            </c:if>
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