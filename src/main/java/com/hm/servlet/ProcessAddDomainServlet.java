package com.hm.servlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hm.crawl.data.DomainMasterCRUD;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.SegmentVO;

/**
 * This Servlet is used to ADD, Edit Domain if DomainVO is valid. 
 */
public class ProcessAddDomainServlet extends HttpServlet {
	
	private final static Logger logger = LoggerFactory.getLogger(ProcessAddDomainServlet.class);
	private static final long serialVersionUID = 1L;
	
	private ResourceBundle bundle = null;
	int domainId;
	List<String> errorMessages = new ArrayList<String>();
	SegmentVO segment = new SegmentVO();
	DomainVO vo = new DomainVO();

	public void init() throws ServletException {
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
		super.init();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String event = request.getParameter("event");
		String requestType = request.getParameter("requestType");

		String domainName = request.getParameter("domain_name");
		String url = request.getParameter("domain_url");
		String seed_url = request.getParameter("seed_url");
		String raw_content_directory = request.getParameter("raw_content_directory");
		String final_content_directory = request.getParameter("final_content_directory");
		domainId = (Integer.parseInt(request.getParameter("domainId")));

		if(requestType !=null && !requestType.isEmpty()) {
			if (requestType.equalsIgnoreCase("Save")) {
				DomainMasterCRUD mast = new DomainMasterCRUD();
				SegmentMasterCRUD segCrud = new SegmentMasterCRUD();
	
				vo.setDomainName(domainName);
				vo.setUrl(url);
				vo.setSeedUrl(seed_url);
				vo.setDomainId(domainId);
				vo.setRaw_content_directory(raw_content_directory.replace("\\",
						"//").endsWith("/") ? raw_content_directory.replace("\\",
						"/") : raw_content_directory.replace("\\", "/").concat("/"));
				vo.setFinal_content_directory(final_content_directory.replace("\\",
						"//").endsWith("/") ? final_content_directory.replace("\\",
						"/") : final_content_directory.replace("\\", "/").concat(
						"/"));
				
				//clearing errorMessages
				if (!errorMessages.isEmpty()) {
					errorMessages.clear();
				}
				
				//validate domainVO
				if (!checkforValidation(vo)) {
					request.setAttribute("errorMessageList", errorMessages);
					request.setAttribute("eventname", event);
					request.setAttribute("domain", vo);
					request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
				} 
				
				//Add Domain process block
				else if (event.equalsIgnoreCase("Add Domain")) {
					boolean domainCreated = false;
					boolean defaultSegCreated = false;
					try {
						// create new entry in Domain table
						domainCreated = mast.create(vo);
						if(domainCreated){
							// get domainId for creation of new Segment
							domainId = mast.getDomainId(vo);
							// sets attributes for default segment
							if(domainId != 0){
								createDefaultSegment();
								// creates default segment per domain.
								defaultSegCreated = segCrud.create(segment);
								if(defaultSegCreated){
									logger.info("Domain has added successfully: "+vo.getDomainName());
									request.setAttribute("successMessage", bundle.getString("domainAdded"));
									request.getRequestDispatcher("/index").forward(request, response);
								}else{
									logger.error("errorMessage", bundle.getString("addDomainError"));
									request.setAttribute("domain", vo);
									request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
								}
							}else{
								logger.error("errorMessage", bundle.getString("addDomainError"));
								request.setAttribute("domain", vo);
								request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
							}
						}else{
							logger.error("errorMessage", bundle.getString("addDomainError"));
							request.setAttribute("domain", vo);
							request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
						}
					} catch (Exception e) {
						logger.error("errorMessage", bundle.getString("addDomainError"));
						request.setAttribute("domain", vo);
						request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
					}
				} 
				
				//Edit Domain process block
				else if (event.equalsIgnoreCase("Edit Domain")) {
					boolean domainUpdated = false;
					try {
						// updates the Domain table
						domainUpdated = mast.update(vo);
						if(domainUpdated){
							logger.info("Domain has updated successfully: "+vo.getDomainName());
							request.setAttribute("successMessage", bundle.getString("domainUpdated"));
							request.getRequestDispatcher("/index").forward(request, response);
						}else{
							logger.error("errorMessage", bundle.getString("editDomainError"));
							request.setAttribute("domain", vo);
							request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
						}
					} catch (Exception e) {
						logger.error("errorMessage", bundle.getString("editDomainError"));
						request.setAttribute("domain", vo);
						request.getRequestDispatcher("/jsp/addDomain.jsp").forward(request, response);
					}
				}
			} else if (requestType.equalsIgnoreCase("Cancel")) {
				response.sendRedirect("/hm/index");
			}
		}else{
			response.sendRedirect("/hm/index");
		}
	}

	/**
	 * creates default segment for domain with priority -1.
	 * 
	 */
	private void createDefaultSegment() {
		segment.setDomainId(domainId);
		segment.setSegmentName("default");
		segment.setUrl_pattern_rule("");
		segment.setCrawl(true);
		segment.setPriority("-1");
		segment.setCrawlInterval(2592000);
	}

	/**
	 * validates the form for null or empty values.
	 * 
	 * @param vo
	 * @return true if no error.
	 */
	private boolean checkforValidation(DomainVO vo) {
		if (!isNullorEmpty(vo.getDomainName())) {
			errorMessages.add("Please provide Domain Name");
		}
		if (!isNullorEmpty(vo.getSeedUrl())) {
			errorMessages.add("Please provide Seed URL");
		}
		if (!isNullorEmpty(vo.getUrl())) {
			errorMessages.add("Please provide DomainBase URL");
		}
		File finalContentFile = new File(vo.getFinal_content_directory());
		if (!finalContentFile.isDirectory()) {
			if (!finalContentFile.mkdirs()) {
				errorMessages.add("Please enter valid Final Content Directory");
			}
		}
		File rawContentFile = new File(vo.getRaw_content_directory());
		if (!rawContentFile.isDirectory()) {
			if (!rawContentFile.mkdirs()) {
				errorMessages.add("Please enter valid Raw Content Directory");
			}
		}

		if (errorMessages.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	
	//Checking given input is not Null and IsEmpty
	private boolean isNullorEmpty(String param) {
		if (param == null || param.trim() == "" || param.isEmpty())
			return false;
		else
			return true;
	}
}