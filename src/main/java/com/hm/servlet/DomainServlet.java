/**
 * 
 */
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
import com.hm.crawl.data.DomainMasterCRUD;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;

public class DomainServlet extends HttpServlet {

	/**
	 * This class is used to display all Domain details by looking up DOMAIN table.
	 */
	private final static Logger logger = LoggerFactory.getLogger(DomainServlet.class);
	
	private static final long serialVersionUID = 1L;
	private ResourceBundle bundle = null; 
	
	
	public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {

		DomainMasterCRUD domainCRUD = new DomainMasterCRUD();
		SegmentMasterCRUD segCRUD = new SegmentMasterCRUD();
		List<DomainVO> domainData = new ArrayList<DomainVO>();
		CrawlUtil cUtil = new CrawlUtil();
		List<Integer> segRuleChangedDomainsList = null;
		List<Integer> segHtmlChangedDomainsList = null;
		List<String> errorMessages = new ArrayList<String>();
		try {
			domainData = domainCRUD.read();
			
			if(domainData != null && domainData.size() > 0){
				req.setAttribute("domain", domainData);
				
				//Checking CRAWL_STATUS value in CRAWL_MASTER table
				if(cUtil.checkRequestProcessStatus()){
					req.setAttribute("errorMessage", "REQUEST IS IN PROCESS");
					req.setAttribute("actionProgress", "true");
				}
				
				//Checking DOMAIN table to show error message if any segment rule changed
				segRuleChangedDomainsList = segCRUD.checkSegmentRuleStatus();
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
				segHtmlChangedDomainsList = segCRUD.checkSegmentHtmlPathStatus();
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
				RequestDispatcher rd = req.getRequestDispatcher("/domain.jsp");
				rd.forward(req, resp);
			}else{
				logger.error("Unable to load Domain Details:");
				req.setAttribute("errorMessage", bundle.getString("domainDetailsPageError"));
				req.setAttribute("domain", domainData);
				RequestDispatcher rd = req.getRequestDispatcher("/domain.jsp");
				rd.forward(req, resp);
			}
		} catch (Exception e) {
			logger.error("Unable to load Domain Details:"+e.getMessage());
			req.setAttribute("errorMessage", bundle.getString("domainDetailsPageError"));
			req.setAttribute("domain", domainData);
			RequestDispatcher rd = req.getRequestDispatcher("/domain.jsp");
			rd.forward(req, resp);
		}
	}
}
