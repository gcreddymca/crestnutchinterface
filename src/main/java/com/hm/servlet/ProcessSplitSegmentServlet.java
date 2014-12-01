package com.hm.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.SegmentVO;

/**
 * Servlet implementation class ProcessSplitSegmentServlet
 * This servlet is call in case segment is to be splitted into further sub-segment.
 * 
 */
public class ProcessSplitSegmentServlet extends HttpServlet {
	
	private final static Logger logger = LoggerFactory.getLogger(ProcessSplitSegmentServlet.class);
	private static final long serialVersionUID = 1L;
	
	private ResourceBundle bundle = null; 
	List<String> errorMessages = new ArrayList<String>();
	
	public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		String requestType = request.getParameter("requestType");
		String sequence_no[] = request.getParameterValues("sequence_no");
		int domainId = Integer.parseInt((String) request.getSession().getAttribute("domainId"));
		
		if (requestType.equalsIgnoreCase("Split Segment")) {
			errorMessages.clear();
			String segmentName[] = request.getParameterValues("segmentName");
			String rule[] = request.getParameterValues("segmentRule");
			String priority[] = request.getParameterValues("segmentPriority");
			String segmentIdtoSplit = request.getParameter("seg_Id");
			
			Map<Integer,SegmentVO> splitSegmentVO = new HashMap<Integer,SegmentVO>();
			for (int i = 0; i < segmentName.length; i++) {
				SegmentVO vo = new SegmentVO();
				vo.setSegmentName(segmentName[i]);
				vo.setUrl_pattern_rule(rule[i]);
				if(request.getParameter("crawl"+Integer.parseInt(sequence_no[i])).equalsIgnoreCase("Yes")){ 
					vo.setCrawl(Boolean.TRUE);
				}else{
					vo.setCrawl(Boolean.FALSE);
				}
				//vo.setPriority(priority[i]);
				vo.setDomainId(domainId);
				
				checkforValidation(Integer.parseInt(sequence_no[i]),vo);
				splitSegmentVO.put(Integer.parseInt(sequence_no[i]),vo); 
			}
			if(errorMessages.isEmpty()){
				SegmentMasterCRUD segmentMaster = new SegmentMasterCRUD();
				boolean segmentSplitted = false;
				try {
					request.getSession().removeAttribute("eventname");
					request.getSession().removeAttribute("segment");
					request.getSession().removeAttribute("upperLimit");
					segmentSplitted = segmentMaster.splitSegment(new ArrayList<SegmentVO>(splitSegmentVO.values()), segmentIdtoSplit);
					if(segmentSplitted){
						logger.info("Segment has splitted successfully: ");
						request.setAttribute("successMessage", bundle.getString("segmentSplitted"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}else{
						logger.error("Error while splitting segment process: ");
						request.setAttribute("errorMessage", bundle.getString("splitSegmentError"));
						request.setAttribute("segmentList",splitSegmentVO);
						request.getRequestDispatcher("/jsp/splitSegment.jsp").forward(request, response);
					}
				} catch (SQLException e) {
					logger.error("Error while splitting segment process: "+e.getMessage());
					request.setAttribute("errorMessage", bundle.getString("splitSegmentError"));
					request.setAttribute("segmentList",splitSegmentVO);
					request.getRequestDispatcher("/jsp/splitSegment.jsp").forward(request, response);
				}
			}else{
				request.setAttribute("errorMessageList", errorMessages);
				request.setAttribute("segmentList",splitSegmentVO);
				request.getRequestDispatcher("/jsp/splitSegment.jsp").forward(request, response);
			}
		} else{
			response.sendRedirect("/hm/segment?domainId="+domainId);
		}
	}
	
	/**
	 * This mehtod check whether given priority is unique or not
	 * @param row
	 * @param upperLimit
	 * @param vo
	 */
	private void checkPriorityforUnique(int row ,int upperLimit, SegmentVO vo) {
		if(Integer.parseInt(vo.getPriority()) >= upperLimit ){
			errorMessages.add("Please add priority between speified limit for row " + row);
		} else
			try {
				if(!new SegmentMasterCRUD().checkforUniquePriority(vo.getPriority())){
					errorMessages.add("Please add unique priority for row " + row);
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
	}
	
	/**
	 * This method is used for business validation
	 * @param row - Row number for new segment row
	 * @param vo - SegmentVO class for per row
	 */
	private void checkforValidation(int row,SegmentVO vo) {
		if(!isNullorEmpty(vo.getSegmentName())){
			errorMessages.add("Please provide Segment Name for Row " + row);
		}
		if(!isNullorEmpty(vo.getUrl_pattern_rule())){
			errorMessages.add("Please provide Segment Rule for Row " + row);
		}
		try{
			Pattern.compile(vo.getUrl_pattern_rule());
		}catch(PatternSyntaxException pEx){
			errorMessages.add(pEx.getDescription());
		}
		/*try {
			if(vo.getPriority() == ""){
				errorMessages.add("Please provide priority for Segment for Row " + row);
			} else if(Integer.parseInt(vo.getPriority()) < 0){
				errorMessages.add("Please provide priority for Segment greater than zero for Row " + row);
			}
		} catch (NumberFormatException e) {
			errorMessages.add("Please provide valid Interger value for Priority for Row " + row);
		}*/
		
	}

	/**
	 * Check whether given parameter for null and empty check
	 * @param param
	 * @return true or false
	 */
	private boolean isNullorEmpty(String param) {
		if(param == null || param.trim() == "" || param.isEmpty())
			return false;
		else
			return true;
	}
}
