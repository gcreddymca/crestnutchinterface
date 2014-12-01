package com.hm.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.hm.crawl.data.TransformationMasterCRUD;
import com.hm.crawl.data.vo.HTMLPathVO;
import com.hm.crawl.data.vo.SegmentVO;
import com.hm.crawl.data.vo.TransformationVO;
import com.hm.util.TransformationUtil;

/**
 * Servlet implementation class ProcessAddSegment This servlet is call in case
 * new segment or existing is to be saved
 * 
 */
public class ProcessAddSegmentServlet extends HttpServlet {

	private final static Logger logger = LoggerFactory.getLogger(ProcessAddSegmentServlet.class);
	private static final long serialVersionUID = 1L;

	List<String> errorMessages = new ArrayList<String>();
	private ResourceBundle bundle = null; 
	
	public ProcessAddSegmentServlet(){
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String event = request.getParameter("event");
		String requestType = request.getParameter("requestType");
		String domainId = request.getParameter("domainId");
		int segmentId = 0;
		if(requestType !=null && !requestType.isEmpty()) {
			if (requestType.equalsIgnoreCase("Save")) {
				if (!errorMessages.isEmpty()) {
					errorMessages.clear();
				}
				SegmentMasterCRUD mast = new SegmentMasterCRUD();
				SegmentVO vo = new SegmentVO();
				if(isNullorEmpty(request.getParameter("seg_Id"))){
					segmentId = Integer.parseInt(request.getParameter("seg_Id"));
				}
				String rule = request.getParameter("rule");
				String purgeUrl = request.getParameter("purgeUrl");
				String segmentName = request.getParameter("seg_name");
				String crawl = request.getParameter("crawl");
				//String priority = request.getParameter("priority");
				String urlType = request.getParameter("url_type");
				String priority = request.getParameter("priority");
				String crawlInterval = request.getParameter("crawlInterval");
				
				
				/*
				 * The below code is for getting all HTML Path Pattern and save it
				 * as object of HTMLPathVO
				 */
	
				String blocks[] = request.getParameterValues("blocks");
				Map<Integer, HTMLPathVO> pathVO = new HashMap<Integer, HTMLPathVO>();
				if (blocks != null) {
					int defaultPattern = 0;
					try {
						defaultPattern = Integer.parseInt(request
								.getParameter("defaultPattern"));
					} catch (NullPointerException e1) {
						errorMessages
								.add(bundle.getString("selectDefaultHTMLPathPatternError"));
					} catch (NumberFormatException e) {
						errorMessages
								.add(bundle.getString("selectDefaultHTMLPathPatternError"));
					}
					for (int i = 0; i < blocks.length; i++) {
						HTMLPathVO htmlVO = new HTMLPathVO();
						String fileType = request.getParameter("fileType"
								+ blocks[i]);
						String fileExt = request
								.getParameter("fileExt" + blocks[i]);
						String folderType[] = request
								.getParameterValues("folderType" + blocks[i]);
						String folderName[] = request
								.getParameterValues("folderName" + blocks[i]);
						List<HTMLPathVO.FolderType> folder = new ArrayList<HTMLPathVO.FolderType>();
						int j = 0;
						for (String f : folderType) {
							HTMLPathVO.FolderType type = htmlVO.new FolderType();
							type.setFolderType(f.trim());
							if (f.equalsIgnoreCase("resourceName")) {
								type.setFolderName(" ");
							} else {
								type.setFolderName(folderName[j].trim());
							}
							j++;
							folder.add(type);
						}
						htmlVO.setFolderType(folder);
						htmlVO.setFiletype(fileType);
						if (fileType.equalsIgnoreCase("resourceName")) {
							htmlVO.setFileName(" ");
						} else {
							htmlVO.setFileName(request.getParameter("fileName"
									+ Integer.parseInt(blocks[i])));
						}
						htmlVO.setFileExt(fileExt);
						if (defaultPattern == Integer.parseInt(blocks[i])) {
							htmlVO.setDefault(Boolean.TRUE);
						}
						htmlVO.setPattern_id(Integer.parseInt(blocks[i]));
						//checkValidationforHTMLPattern((i + 1), htmlVO);
						pathVO.put(Integer.parseInt(blocks[i]), htmlVO);
					}
				}
				vo.setSegmentId(segmentId);
				vo.setSegmentName(segmentName);
				vo.setUrl_pattern_rule(rule);
				vo.setPurgeUrl(purgeUrl);
				if(isNullorEmpty(crawlInterval)){
					vo.setCrawlInterval(Integer.parseInt(crawlInterval));
				}
				if(isNullorEmpty(domainId)){
					vo.setDomainId(Integer.parseInt(domainId));
				}
				
				if (crawl !=null && (boolean) crawl.equalsIgnoreCase("Yes")) {
					vo.setCrawl(Boolean.TRUE);
				} else {
					vo.setCrawl(Boolean.FALSE);
				}
				//vo.setPriority(priority);
				vo.setPathVO(pathVO);
				vo.setUrlType(urlType);
	
				if (event.equalsIgnoreCase("Add Segment")) {
					if (!checkforValidationforAdd(vo)) {
						request.removeAttribute("errorMessage");
						request.setAttribute("errorMessage", errorMessages);
						request.setAttribute("domainId", domainId);
						request.setAttribute("eventname", event);
						// Load Transformations from Master table
						TransformationMasterCRUD crud = new TransformationMasterCRUD();
						List<TransformationVO> transformation_master = new ArrayList<TransformationVO>();
						try {
							transformation_master = crud.load();
						} catch (Exception e) {
							logger.error("Unable to load transformations from table:");
						}
						vo.setTransformationVO(transformation_master);
						request.setAttribute("segment", vo);
						request.getRequestDispatcher("/jsp/addSegment.jsp").forward(request, response);
					} else {
						try {
							List<TransformationVO> tvoList = new ArrayList<TransformationVO>();
							String transformIds[] = request.getParameterValues("transType");
							String transformPrtys[] = request.getParameterValues("transPriority");
							if(transformIds!=null && transformIds.length>0){
								for(int i=0; i<transformIds.length; i++){
									String transformationId = transformIds[i];
									String transformationPriority = transformPrtys[i];
									if(transformationPriority!=null && !transformationPriority.isEmpty()){
										TransformationVO tvo = new TransformationVO();
										tvo.setTransformationId(transformationId);
										tvo.setTransformationPriority(transformationPriority);
										tvoList.add(tvo);
									}else{
										logger.info("Default Transformation");
									}
								}
							}
							vo.setTransformationVO(tvoList);
							boolean success = mast.create(vo);
							if(success){
								logger.info("Segment Created Successfully");
								request.setAttribute("successMessage", bundle.getString("segmentCreated"));
							}else{
								logger.info("Unable to Create the Segment");
								request.setAttribute("errorMessage","Unable to Create Segment");
							}
							request.removeAttribute("eventname");
							request.getRequestDispatcher("/segment?domainId="+ domainId).forward(request, response);
						} catch (SQLException e) {
							logger.error(e.getMessage());
						}
					}
				} else if (event.equalsIgnoreCase("Edit Segment")) {
					if (!checkforValidationforEdit(vo)) {
						request.removeAttribute("errorMessage");
						request.setAttribute("errorMessageList", errorMessages);
						
						request.setAttribute("domainId", domainId);
						request.setAttribute("eventname", event);
						// Load Transformations from Master table
						TransformationMasterCRUD crud = new TransformationMasterCRUD();
						List<TransformationVO> transformation_master = new ArrayList<TransformationVO>();
						try {
							transformation_master = crud.load();
						} catch (Exception e) {
							logger.error("Unable to load transformations from table:");
						}
						vo.setTransformationVO(transformation_master);
						request.setAttribute("segment", vo);
						request.getRequestDispatcher("/jsp/addSegment.jsp")
								.forward(request, response);
					} else {
						try {
							TransformationVO tvo = new TransformationVO();
							boolean validatePriority = false;
							List<TransformationVO> tvoList = new ArrayList<TransformationVO>();
							String transformationType[] = request.getParameterValues("transType");
							String transformationPriority[] = request.getParameterValues("transPriority");
							if(transformationType!=null && transformationType.length>0){
								for(int i=0; i<transformationType.length; i++){
									String transformType = transformationType[i];
									String transformPrty = transformationPriority[i];
									if(transformPrty!=null && !transformPrty.isEmpty()){
										TransformationVO tsvo = new TransformationVO();
										tsvo.setTransformationType(transformType);
										tsvo.setTransformationPriority(transformPrty);
										tvoList.add(tsvo);
									}else{
										//System.out.println("Default Transformation");
									}
									//System.out.println("transformationType="+transformType+" -----transformationPriority="+transformPrty);
								}
							}
							validatePriority = true;
							if (!(TransformationUtil.checkforValidation(tvo, validatePriority,segmentId))) {
								request.setAttribute("errorMessageList", errorMessages);
							} 
							vo.setTransformationVO(tvoList);
							vo.setPriority(priority);
							boolean updateSegementSuccess = mast.updateSegement(vo);
							if(updateSegementSuccess){
								request.setAttribute("successMessage", bundle.getString("segmentEdited"));
							}else{
								request.setAttribute("errorMessage", "Unable to edit the Segment");
							}
							request.removeAttribute("eventname");
							request.setAttribute("domainId", domainId);
							request.getRequestDispatcher("/segment?domainId="+ domainId).forward(request, response);
						} catch (SQLException e) {
							logger.error(e.getMessage());
						}
					}
				}
	
			} else if (requestType.equalsIgnoreCase("Cancel")) {
				response.sendRedirect("/hm/segment?domainId=" + domainId);
			}
		}else{
			response.sendRedirect("/hm/index");
		}	
	}
	/**
	 * @param i
	 * @param htmlVO
	 */
	private void checkValidationforHTMLPattern(int i, HTMLPathVO htmlVO) {
		for (Iterator<HTMLPathVO.FolderType> iter = htmlVO.getFolderType()
				.iterator(); iter.hasNext();) {
			HTMLPathVO.FolderType type = iter.next();
			if (!type.getFolderName().equalsIgnoreCase("resourceName")) {
				if (!isNullorEmpty(type.getFolderName())) {
					errorMessages
							.add(bundle.getString("folderNameMissedError")+ i);
				}
			}
		}
		if (!isNullorEmpty(htmlVO.getFiletype())) {
			errorMessages.add(bundle.getString("fileTypeMissed")+ i);
		}
		if (!isNullorEmpty(htmlVO.getFileName())) {
			errorMessages.add(bundle.getString("fileNameMissed")+ i);
		}
		if (!isNullorEmpty(htmlVO.getFileExt())) {
			errorMessages
					.add(bundle.getString("fileExtensionMissed")+ i);
		}

	}

	/**
	 * This method is used for business validation while adding new segment
	 * 
	 * @param vo
	 *            - SegmentVO class
	 */
	private boolean checkforValidationforEdit(SegmentVO vo) {
		if (!isNullorEmpty(vo.getSegmentName())) {
			errorMessages.add(bundle.getString("segmentNameMissedError"));
		}
		if (!isNullorEmpty(vo.getUrl_pattern_rule())) {
			errorMessages.add(bundle.getString("segmentRuleMissedError"));
		}
		try {
			Pattern.compile(vo.getUrl_pattern_rule());
		} catch (PatternSyntaxException pEx) {
			errorMessages.add(pEx.getDescription());
		}
		/*try {
			if (vo.getPriority() == "") {
				errorMessages.add("Please provide priority for Segment");
			} else if (Integer.parseInt(vo.getPriority()) < 0) {
				errorMessages
						.add("Please provide priority for Segment greater than zero");
			}

		} catch (NumberFormatException e) {
			errorMessages
					.add("Please provide valid Interger value for Priority");
		}*/
		if(vo.isCrawl()){
			if (vo.getPathVO() == null || vo.getPathVO().isEmpty()) {
				errorMessages.add(bundle.getString("htmlFileLocationMissed"));
			}
		}
		if (errorMessages.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method is used for business validation while adding new segment
	 * 
	 * @param vo
	 *            - SegmentVO class
	 */
	private boolean checkforValidationforAdd(SegmentVO vo) {
		if(vo.getDomainId() == 0){
			errorMessages.add("Domain is Null");
		}
		if (!isNullorEmpty(vo.getSegmentName())) {
			errorMessages.add(bundle.getString("segmentNameMissedError"));
		}
		if (!isNullorEmpty(vo.getUrl_pattern_rule())) {
			errorMessages.add(bundle.getString("segmentRuleMissedError"));
		}
		if(!isNullorEmpty(vo.getPurgeUrl())){
			errorMessages.add(bundle.getString("purgeUrlMissed"));
		}
		try {
			Pattern.compile(vo.getUrl_pattern_rule());
		} catch (PatternSyntaxException pEx) {
			errorMessages.add(pEx.getDescription());
		}
		/*try {
			if (vo.getPriority() == "") {
				errorMessages.add("Please provide priority for Segment");
			} else if (Integer.parseInt(vo.getPriority()) < 0) {
				errorMessages
						.add("Please provide priority for Segment greater than zero");
			}

			if (!new SegmentMasterCRUD().checkforUniquePriority(vo
					.getPriority())) {
				errorMessages.add("Please add unique priority ");
			}
		} catch (NumberFormatException e) {
			errorMessages
					.add("Please provide valid Interger value for Priority");
		} catch (SQLException e) {
			logger.severe(e.getMessage());
		}*/
		if(vo.isCrawl()){
			if (vo.getPathVO() == null || vo.getPathVO().isEmpty()) {
				errorMessages.add(bundle.getString("htmlFileLocationMissed"));
			}
		}
		if (errorMessages.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check whether given parameter for null and empty check
	 * 
	 * @param param
	 * @return true or false
	 */
	private boolean isNullorEmpty(String param) {
		if (param == null || param.trim() == "" || param.isEmpty())
			return false;
		else
			return true;
	}
}
