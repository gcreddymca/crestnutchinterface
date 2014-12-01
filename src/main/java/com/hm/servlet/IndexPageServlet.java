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
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.SegmentVO;


public class IndexPageServlet extends HttpServlet {

	/**
	 *  This Servelt is used to display Domain Segments details
	 */
	private final static Logger logger = LoggerFactory.getLogger(IndexPageServlet.class);
	
	private static final long serialVersionUID = 1L;
	private ResourceBundle bundle = null;
	
	public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String domainId = req.getParameter("domainId");
		String seedUrl = req.getParameter("seedUrl");
		SegmentMasterCRUD segment = new SegmentMasterCRUD();
		CrawlUtil cUtil = new CrawlUtil();
		List<Integer> segRuleChangedDomainsList = null;
		List<Integer> segHtmlChangedDomainsList = null;
		List<SegmentVO> segmentData = null;
		List<String> errorMessages = new ArrayList<String>();
		if(domainId != null && !domainId.isEmpty()){
			try {
				segmentData = segment.readSegmentMaster(Integer.parseInt(domainId));
				String domainName = cUtil.getDomainName(Integer.parseInt(domainId));
				req.setAttribute("seedUrl", seedUrl);
				req.setAttribute("domainId", Integer.parseInt(domainId));
				req.setAttribute("segment",segmentData);
				req.setAttribute("domainName",domainName);
				req.setAttribute("countDetails",segment.getCount(Integer.parseInt(domainId)));
				
				//Checking CRAWL_STATUS value in CRAWL_MASTER table
				if(cUtil.checkRequestProcessStatus()){
					req.setAttribute("errorMessage", "REQUEST IS IN PROCESS");
					req.setAttribute("actionProgress", "true");
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
					req.setAttribute("errorMessageList", errorMessages);
					req.setAttribute("segRuleChangedDomains", segRuleChangedDomainsList);
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
					req.setAttribute("errorMessageList", errorMessages);
					req.setAttribute("segPathChangedDomains", segHtmlChangedDomainsList);
				}
				RequestDispatcher rd = req.getRequestDispatcher("/jsp/segment.jsp");
				rd.forward(req, resp);
			}catch (Exception e) {
				logger.error("Error while loading Segment Details Page:"+bundle.getString("segmentDetailsPageError"));
				req.getRequestDispatcher("/segment?domainId="+domainId).forward(req, resp);
			}
		}else{
			logger.error("Error while loading Segment Details Page: missing required values: domainId:");
			req.setAttribute("errorMessage", bundle.getString("segmentDetailsPageRequiredValuesMissed"));
			req.getRequestDispatcher("/index").forward(req, resp);
		}
	}
}
