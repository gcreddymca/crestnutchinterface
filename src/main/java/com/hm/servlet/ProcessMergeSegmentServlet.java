package com.hm.servlet;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.SegmentVO;

/**
 * Servlet implementation class ProcessMergeSegmentServlet
 */
public class ProcessMergeSegmentServlet extends HttpServlet {
	
	private final static Logger logger = LoggerFactory.getLogger(ProcessMergeSegmentServlet.class);
	
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
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		String requestType = request.getParameter("requestType");
		String domainId = request.getParameter("domainId");
		if (requestType.equalsIgnoreCase("Save")) {
			String mergeSegmentId = request.getParameter("merge_seg_name");
			String selectedSegmentId = request.getParameter("selected_seg_Id");
		
			SegmentMasterCRUD master = new SegmentMasterCRUD();
			SegmentVO segmentMerge = master.readByPrimaryKey(mergeSegmentId, null);
			SegmentVO segmentSelected = master.readByPrimaryKey(selectedSegmentId, null);
			if(segmentMerge != null && segmentSelected != null){
				try {
					boolean mergedSeg = master.mergeSegment(segmentMerge, segmentSelected);
					if(mergedSeg){
						request.setAttribute("successMessage", bundle.getString("segmentMerged"));
						request.getRequestDispatcher("/segment?domainId=" + domainId).forward(request, response);
					}
				} catch (Exception e) {
					logger.error("errorMessage", bundle.getString("mergeSegmentError"));
					request.getRequestDispatcher("/segment?domainId=" + domainId).forward(request, response);
				}
			}else{
				logger.error("errorMessage", bundle.getString("mergeSegmentError"));
				request.getRequestDispatcher("/segment?domainId=" + domainId).forward(request, response);
			}
		}
	}
}
