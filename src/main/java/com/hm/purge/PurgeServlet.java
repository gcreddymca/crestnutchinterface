package com.hm.purge;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;

/**
 * Servlet implementation class PurgeServlet
 */
public class PurgeServlet extends HttpServlet {
	private final static Logger logger = LoggerFactory.getLogger(PurgeServlet.class);
	private static final long serialVersionUID = 1L;
	private ResourceBundle bundle = null;
	private static final String MEDIA_TYPE="ADN";
       
	public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mediaPath = (String)request.getParameter("mediapath");
        String site = (String)request.getParameter("siteName");
       
        if(site!=null && !site.isEmpty()){
        	if(mediaPath != null && !mediaPath.isEmpty()){
	        	mediaPath=site+mediaPath;
		        CrawlUtil.purgeAddTimeStamptoURL(mediaPath);
		        PurgeEdgeCastData pData = new PurgeEdgeCastData();
		        String purgeId = pData.getPurgeContentId(mediaPath, MEDIA_TYPE);
		        if(purgeId!=null){
		        	CrawlUtil.purgeUpdateTimeStamptoURL(mediaPath,purgeId);
		        	request.setAttribute("successMessage", bundle.getString("purgeURLRequested"));
		        }
		        LinkedHashMap<String, LinkedList<String>> purgeDetails = new LinkedHashMap<String, LinkedList<String>>();
				try {
					purgeDetails = CrawlUtil.getPurgeDetailStatus();
					request.setAttribute("purgeDetailsMap", purgeDetails);
				} catch (Exception e) {
					request.setAttribute("errorMessage", bundle.getString("purgeDetailsFetchingError"));
			        request.getRequestDispatcher("/jsp/purgeCDN.jsp").forward(request, response);
				}
				request.setAttribute("purgeId", purgeId);
		        request.getRequestDispatcher("/jsp/purgeCDN.jsp").forward(request, response);
        	}else{
            	request.setAttribute("errorMessage", bundle.getString("purgeURLValueMissed"));
            	logger.error("Please provide purge request URL:");
            	request.getRequestDispatcher("/jsp/purgeCDN.jsp").forward(request, response);
            }
        }else{
        	logger.error("Please select site name to purge URL:");
        	request.setAttribute("errorMessage", bundle.getString("purgeSiteNameValueMissed"));
        	request.getRequestDispatcher("/jsp/purgeCDN.jsp").forward(request, response);
        }
	}
}
