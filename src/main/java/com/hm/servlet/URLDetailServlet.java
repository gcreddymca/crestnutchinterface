package com.hm.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.SegmentDetailCRUD;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.SegmentVO;

/**
 * This Servlet is used to display Segment URL Details
 */
public class URLDetailServlet extends HttpServlet {
	
	private final static Logger logger = LoggerFactory.getLogger(URLDetailServlet.class);
	private static final long serialVersionUID = 1L;
       
	private ResourceBundle bundle = null; 
	
	public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SegmentDetailCRUD segmentDetail = new SegmentDetailCRUD();
		SegmentMasterCRUD segment = new SegmentMasterCRUD();
		SegmentVO segmentVO = new SegmentVO();
		CrawlUtil cUtil = new CrawlUtil();
		List<Integer> segRuleChangedDomainsList = null;
		List<Integer> segHtmlChangedDomainsList = null;
		List<String> errorMessages = new ArrayList<String>();
		
		String domin_Id= request.getParameter("domainId");
		String segId= request.getParameter("segmentId");
		String segName= request.getParameter("segmentName");
		
		if(domin_Id !=null && !domin_Id.isEmpty() && segId != null && !segId.isEmpty() && segName!=null && !segName.isEmpty()){
			try {
				int domainId = Integer.parseInt(domin_Id);
				int segmentId = Integer.parseInt(segId);
				segmentVO.setDomainId(domainId);
				segmentVO.setSegmentId(segmentId);
				segmentVO.setSegmentName(segName);
				
				DomainVO domainVO = segmentDetail.readDomainDetails(domainId);
				SegmentVO segmentDetailList = segmentDetail.readBySegment(segmentVO); 
				List<Integer> urlDetailCount = segmentDetail.getURLDetailCount(segmentId, domainId);
				
				request.setAttribute("urlDetailCount",urlDetailCount);
				request.setAttribute("segmentDetail",segmentDetailList);
				request.setAttribute("domainId",domainId);
				request.setAttribute("segmentId",segmentId);
				request.setAttribute("domainDetail", domainVO);
				
				//Checking CRAWL_STATUS value in CRAWL_MASTER table
				if(cUtil.checkRequestProcessStatus()){
					request.setAttribute("errorMessage", "REQUEST IS IN PROCESS");
					request.setAttribute("actionProgress", "true");
				}
				
				//Checking DOMAIN table to show error message if any segment rule changed
				segRuleChangedDomainsList = segment.checkSegmentRuleStatus();
				if(segRuleChangedDomainsList != null && segRuleChangedDomainsList.size()>0){
					String domainNames = "";
					for(Integer domainKey : segRuleChangedDomainsList){
						if(!domainNames.isEmpty()){
							domainNames = domainNames+","+cUtil.getDomainName(domainKey);
						}else{
							domainNames = domainNames+cUtil.getDomainName(domainKey);
						}
					}
					errorMessages.add("Warning!! Segment Rule has changed, Please run HTMLize Process at domain level:"+domainNames);
					request.setAttribute("errorMessageList", errorMessages);
					request.setAttribute("segRuleChangedDomains", segRuleChangedDomainsList);
				}
				
				//Checking DOMAIN table to show error message if any segment path changed.
				segHtmlChangedDomainsList = segment.checkSegmentHtmlPathStatus();
				if(segHtmlChangedDomainsList != null && segHtmlChangedDomainsList.size()>0){
					String domainNames = "";
					for(Integer domainKey : segHtmlChangedDomainsList){
						if(!domainNames.isEmpty()){
							domainNames = domainNames+","+cUtil.getDomainName(domainKey);
						}else{
							domainNames = domainNames+cUtil.getDomainName(domainKey);
						}
					}
					errorMessages.add("Warning!! Segment Html Path has changed, Please run RefreshContent Process at domain level:"+domainNames);
					request.setAttribute("errorMessageList", errorMessages);
					request.setAttribute("segPathChangedDomains", segHtmlChangedDomainsList);
				}
				RequestDispatcher rd = request.getRequestDispatcher("/jsp/segmentURLDetails.jsp");
				rd.forward(request, response);
			}catch(Exception e){
				logger.error("Unable to load Segment URL Details:"+e.getMessage());
				request.setAttribute("errorMessage", bundle.getString("segmentURLDetailsPageError"));
				request.getRequestDispatcher("/segment?domainId="+domin_Id).forward(request, response);
			}
		}else{
			logger.error("Unable to load Segment URL Details: Missing required values:");
			request.setAttribute("errorMessage", bundle.getString("segmentURLDetailsPageRequiredValuesMissed"));
			request.getRequestDispatcher("/index").forward(request, response);
		}
	}
}
