package com.hm.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.nutch.tools.JDBCConnector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.automate.events.PerformAction;
import com.hm.crawl.data.DomainMasterCRUD;
import com.hm.crawl.data.RequestMasterCURD;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.TransformationMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.HTMLPathVO;
import com.hm.crawl.data.vo.HTMLPathVO.FolderType;
import com.hm.crawl.data.vo.SegmentVO;
import com.hm.crawl.data.vo.TransformationVO;
import com.hm.util.MonitorUtil;

/**
 * Servlet implementation class ProcessFormOperation This class is called for
 * various operation on segment i.e Add,Edit,Delete,Merge,Split,Generate URL
 * location for Segment
 * 
 */
public class ProcessFormOperationServlet extends HttpServlet {

	private final static Logger logger = LoggerFactory.getLogger(ProcessFormOperationServlet.class);

	private static final long serialVersionUID = 1L;

	private List<String> eventList = new ArrayList<String>();
	private ResourceBundle bundle = null; 
	RequestMasterCURD requestMasterCurd = new RequestMasterCURD();
	PerformAction performAction = new PerformAction();
	
	public ProcessFormOperationServlet() {
		eventList.add("AutoCrawlConfirm");
		eventList.add("refreshDomainConfirm");
		eventList.add("htmlizeSelectedSegmentsConfirm");
		eventList.add("refreshSelectedSegmentsConfirm");
		eventList.add("refreshURLConfirm");
		eventList.add("AutoCrawl");
		eventList.add("htmlizeSelectedSegments");
		eventList.add("refreshSelectedSegments");
		eventList.add("refreshSegment");
		eventList.add("refreshDomain");
		eventList.add("refreshURL");
		eventList.add("refreshSelectedURLS");
		eventList.add("refreshSelectedURLSConfirm");
		eventList.add("htmlizeDomainConfirm");
		eventList.add("deleteDomainHtml");
		eventList.add("deleteDomainHtmlConfirm");
		eventList.add("deleteSegmentHtml");
		eventList.add("deleteSelectedSegmentHtml");
		eventList.add("deleteSelectedSegmentHtmlConfirm");
		eventList.add("deleteURLHtmlConfirm");
		eventList.add("deleteURLHtml");
		eventList.add("deleteSelectedURLHtmlConfirm");
		eventList.add("deleteSelectedURLHtml");
		eventList.add("trackConfirm");
		eventList.add("deleteApiConfirm");
		eventList.add("purgeURLConfirm");
		eventList.add("selectedDomainHtmlize");
		eventList.add("selectedDomainHtmlizeConfirm");
		eventList.add("deleteSegment");
		eventList.add("deleteDomain");
		eventList.add("deleteUser");
		eventList.add("deleteTransform");
		eventList.add("issueLockConfirm");
		eventList.add("clearLockConfirm");
		eventList.add("reset");
		eventList.add("swapPriority");
		
		//loading resoure bundle HMMessages file
		bundle =  ResourceBundle.getBundle("hmMessages");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SegmentMasterCRUD segment = new SegmentMasterCRUD();
		DomainMasterCRUD domain = new DomainMasterCRUD();
		SegmentVO segmentVO = new SegmentVO();
		DomainVO domainVO = new DomainVO();
		String event = request.getParameter("event");
		String eventName = null;
		String domainId = request.getParameter("domainId");
		String segmentId = request.getParameter("segmentId");
		String seedUrl = request.getParameter("seedUrl");
		request.getSession().removeAttribute("errorMessage");
		request.getSession().removeAttribute("successMessage");
		
		//checking if event is not null
		if(!event.isEmpty() && event!=null){
			// Add Segment Process block
			if (event.equalsIgnoreCase("Add")) {
				eventName = "Add Segment";
				if(domainId != null && !domainId.isEmpty()){
					CrawlUtil cUtil = new CrawlUtil();
					HTMLPathVO vo = new HTMLPathVO();
					List<FolderType> folderType = new ArrayList<FolderType>();
					HTMLPathVO.FolderType type = vo.new FolderType();
					folderType.add(type);
					vo.setFolderType(folderType);
					vo.setDefault(Boolean.TRUE);
					Map<Integer, HTMLPathVO> map = new HashMap<Integer, HTMLPathVO>();
					map.put(1, vo);
					segmentVO.setPathVO(map);
					request.getSession().setAttribute("eventname", eventName);
					try {
						String domainName = cUtil.getDomainName(Integer.parseInt(domainId));
						request.setAttribute("domainName",domainName);
						// Load Transformations from Master table
						TransformationMasterCRUD crud = new TransformationMasterCRUD();
						List<TransformationVO> transformation_master = new ArrayList<TransformationVO>();
						transformation_master = crud.load();
						request.setAttribute("transformation_master", transformation_master);
						segmentVO.setTransformationVO(transformation_master);
						request.setAttribute("segment", segmentVO);
						request.setAttribute("domainId", domainId);
						request.setAttribute("seedUrl", seedUrl);
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/addSegment.jsp");
						rd.forward(request, response);
					} catch (Exception e) {
						logger.error("Error While loading Add Segment Page: in Add block:"+e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("addSegmentPageError"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}
				}else{
					logger.error("Error while loading Add Segment Page: missing required values:");
					request.setAttribute("errorMessage", bundle.getString("addSegmentPageRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Add Domain process block
			else if (event.equalsIgnoreCase("AddDomain")) {
				eventName = "Add Domain";
				request.getSession().setAttribute("eventname", eventName);
				request.setAttribute("domain", domainVO);
				RequestDispatcher rd = request.getRequestDispatcher("/jsp/addDomain.jsp");
				rd.forward(request, response);
			}
	
			// Edit Segment process block
			else if (event.equalsIgnoreCase("Edit")) {
				eventName = "Edit Segment";
				CrawlUtil cUtil = new CrawlUtil();
				TransformationMasterCRUD crud = new TransformationMasterCRUD();
				List<TransformationVO> transformation_master = new ArrayList<TransformationVO>();
				if(domainId != null && !domainId.isEmpty() && segmentId != null && !segmentId.isEmpty()){
					segmentVO = segment.readByPrimaryKey(segmentId, null);
					segmentVO.setDomainId(Integer.parseInt(domainId));
					request.setAttribute("eventname", eventName);
					request.setAttribute("domainId", domainId);
					request.setAttribute("seedUrl", seedUrl);
					String domainName = cUtil.getDomainName(Integer.parseInt(domainId));
					request.setAttribute("domainName",domainName);
					try {
						transformation_master = crud.load();
						segmentVO.setTransformationVO(transformation_master);
						request.setAttribute("segment", segmentVO);
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/addSegment.jsp");
						rd.forward(request, response);
					} catch (Exception e) {
						logger.error("Error while loading edit segment page:"+e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("editSegmentPageError"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}
				} else{
					logger.error("Error while loading edit segment page: missing required values:");
					request.setAttribute("errorMessage", bundle.getString("editSegmentPageRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Delete Segment process block
			else if (event.equalsIgnoreCase("deleteSegmentConfirm")) {
				if(domainId != null && !domainId.isEmpty() && segmentId != null && !segmentId.isEmpty()){
					try {
						segment.deleteSegement(segmentId);
						request.setAttribute("successMessage", bundle.getString("segmentDeleted"));
						List<?> segmentData = segment.read(domainId);
						request.setAttribute("segment", segmentData);
						request.setAttribute("domainId", domainId);
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					} catch (Exception e) {
						logger.error("Error while deleting segment in deleteSegmentConfirm block:"+e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("deletSegmentError"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}
				}else{
					logger.error("Error while deleting segment: missing required values:");
					request.setAttribute("errorMessage", bundle.getString("deleteSegmentRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Merge Segment process block
			else if (event.equalsIgnoreCase("Merge")) {
				eventName = "Merge Segement";
				if(domainId != null && !domainId.isEmpty() && segmentId != null && !segmentId.isEmpty()){
					try{
						List<?> segmentList = segment.readSegmentMaster(Integer.parseInt(domainId));
						request.setAttribute("eventname", eventName);
						request.setAttribute("selectedSegmentId", segmentId);
						request.setAttribute("segment", segmentList);
						request.setAttribute("domainId", domainId);
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/mergeSegment.jsp");
						rd.forward(request, response);
					}catch(Exception e){
						logger.error("Error while loading Merge Segment Page:"+e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("mergeSegmentPageError"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}
				}else{
					logger.error("Error while loading Merge Segment Page: missing required values:");
					request.setAttribute("errorMessage", bundle.getString("mergeSegmentPageRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Split segment process block
			else if (event.equalsIgnoreCase("Split")) {
				eventName = "Split Segement";
				if(domainId != null && !domainId.isEmpty() && segmentId != null && !segmentId.isEmpty()){
					try{
						Map<Integer, SegmentVO> segmentList = new HashMap<Integer, SegmentVO>();
						SegmentVO segmentData = segment.readByPrimaryKey(segmentId, null);
						request.getSession().setAttribute("eventname", eventName);
						request.getSession().setAttribute("segment", segmentData);
						request.getSession().setAttribute("domainId", domainId);
						segmentList.put(1, new SegmentVO());
						request.setAttribute("segmentList", segmentList);
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/splitSegment.jsp");
						rd.forward(request, response);
					}catch(Exception e){
						logger.error("Error while loading Split Segment Page:"+e.getMessage());
						request.setAttribute("errorMessage", bundle.getString("splitSegmentPageError"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}
				}else{
					logger.error("Error while loading Split segment: missing required values:");
					request.setAttribute("errorMessage", bundle.getString("splitSegmentPageRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Delete Transformation for segment level
			else if (event.equalsIgnoreCase("deleteTransformConfirm")) {
				event = "Edit";
				boolean segDeleted = false;
				TransformationMasterCRUD transformationCrud = new TransformationMasterCRUD();
				TransformationVO vo = new TransformationVO();
				String transformationType = request.getParameter("transformationType");
				String segId = request.getParameter("segmentId");
				if(domainId !=null && !domainId.isEmpty() && !segId.isEmpty() && segId!=null && !transformationType.isEmpty() && transformationType!=null){
					vo.setTransformationType(transformationType);
					try {
						// delete transformation from segment.
						segDeleted = transformationCrud.deleteFromSegment(vo, segId);
						if(segDeleted){
							logger.info("Transformation: "+transformationType+" has deleted for segment:"+segId);
							request.setAttribute("successMessage",bundle.getString("transformationDeleted"));
						}else{
							logger.error("While deleting Transformation: "+transformationType+" for segment:"+segId);
							request.setAttribute("errorMessage",bundle.getString("deleteTransformationError"));
						}
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					} catch (Exception e) {
						logger.error("Error while deleting transformation for segment:" + e.getMessage());
						request.setAttribute("errorMessage",bundle.getString("deleteTransformationError"));
						request.getRequestDispatcher("/segment?domainId="+domainId).forward(request, response);
					}
				}else{
					request.setAttribute("errorMessage",bundle.getString("deleteTransformationRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Edit Domain process block
			else if (event.equalsIgnoreCase("editDomain")) {
				eventName = "Edit Domain";
				if(domainId!=null && !domainId.isEmpty()){
					try {
						domainVO = domain.readByPrimaryKey(Integer.parseInt(domainId));
						request.setAttribute("eventname", eventName);
						request.setAttribute("domain", domainVO);
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/addDomain.jsp");
						rd.forward(request, response);
					} catch (Exception e) {
						request.setAttribute("errorMessage", bundle.getString("editDomainPageError"));
						request.getRequestDispatcher("/index").forward(request, response);
					}
				}else{
					request.setAttribute("errorMessage", bundle.getString("editDomainPageRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			// Delete Domain process block
			else if (event.equalsIgnoreCase("deleteDomainConfirm")) {
				if(domainId!=null && !domainId.isEmpty()){
					boolean domainDeleted = false;
					try {
						domainDeleted = domain.delete(domainId);
						if(domainDeleted){
							List<?> domainData = domain.read();
							request.setAttribute("domain", domainData);
							request.setAttribute("domainId", domainId);
							request.setAttribute("successMessage", bundle.getString("domainDeleted"));
							request.getRequestDispatcher("/index").forward(request, response);
						}else{
							request.setAttribute("errorMessage", bundle.getString("deleteDomainError"));
							request.getRequestDispatcher("/index").forward(request, response);
						}
					} catch (Exception e) {
						request.setAttribute("errorMessage", bundle.getString("deleteDomainError"));
						request.getRequestDispatcher("/index").forward(request, response);
					}
				}else{
					request.setAttribute("errorMessage", bundle.getString("deleteDomainPageRequiredValuesMissed"));
					request.getRequestDispatcher("/index").forward(request, response);
				}
			}
	
			//swap segment priority process block
			else if (event.equalsIgnoreCase("swapPriorityConfirm")) {
				String move = request.getParameter("move");
				boolean segSwaped = false;
				if(domainId !=null && !domainId.isEmpty() && !segmentId.isEmpty() && segmentId!=null && !move.isEmpty() && move!=null){
					SegmentVO segVo = new SegmentVO();
					SegmentMasterCRUD segcrud = new SegmentMasterCRUD();
					segVo = segment.readByPrimaryKey(segmentId, null);
					if (segVo != null) {
						segSwaped = segcrud.swapPriority(segVo, move);
					}
					if(segSwaped){
						logger.info("Segment priority has swapped successfully:");
						request.setAttribute("successMessage", bundle.getString("segmentPrioritySwaped"));
					}else{
						request.setAttribute("errorMessage", bundle.getString("segmentPrioritySwapedError"));
					}
					RequestDispatcher rd = request.getRequestDispatcher("/segment?domainId=" + domainId);
					rd.forward(request, response);
				}else{
					request.setAttribute("errorMessage", bundle.getString("swapSegmentPriorityRequiredValuesMissed"));
					RequestDispatcher rd = request.getRequestDispatcher("/index");
					rd.forward(request, response);
				}
			}
	
			// Add User process block
			else if (event.equalsIgnoreCase("AddUser")) {
				eventName = "AddUser";
				try{
					Map<String,String> userRoles = CrawlUtil.userRoles();
					if(userRoles != null && !userRoles.isEmpty()){
						request.setAttribute("eventname", eventName);
						request.setAttribute("userRoles", userRoles);
						RequestDispatcher rd = request
								.getRequestDispatcher("/jsp/addUser.jsp");
						rd.forward(request, response);
					}else{
						request.setAttribute("errorMessage", bundle.getString("userRoleValuesMissed"));
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/myAccountInfo.jsp");
						rd.forward(request, response);
					}
				}catch(Exception e){
					request.setAttribute("errorMessage", bundle.getString("addUserPageError"));
					request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
				}
			}
	
			// Change Password for user process block
			else if (event.equalsIgnoreCase("ChangePwd")) {
				eventName = "ChangePwd";
				String uname = request.getParameter("uname");
				if(uname != null && !uname.isEmpty()){
					request.getSession().setAttribute("eventname", eventName);
					request.setAttribute("uname", uname);
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/changePassword.jsp");
					rd.forward(request, response);
				}else{
					request.setAttribute("errorMessage", bundle.getString("changePasswordPageRequiredValuesMissed"));
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/myAccountInfo.jsp");
					rd.forward(request, response);
				}
			}
	
			// Update user process block
			else if (event.equalsIgnoreCase("updateUser")) {
				eventName = "updateUser";
				String uname = request.getParameter("uname");
				if(uname != null && !uname.isEmpty()){
					Map<String,String> userRoles = CrawlUtil.userRoles();
					if(userRoles != null && !userRoles.isEmpty()){
						request.setAttribute("userRoles", userRoles);
						request.setAttribute("eventname", eventName);
						request.setAttribute("uname", uname);
						RequestDispatcher rd = request
								.getRequestDispatcher("/jsp/updateUser.jsp");
						rd.forward(request, response);
					}else{
						request.setAttribute("errorMessage", bundle.getString("userRoleValuesMissed"));
						RequestDispatcher rd = request.getRequestDispatcher("/jsp/myAccountInfo.jsp");
						rd.forward(request, response);
					}
				}else{
					request.setAttribute("errorMessage", bundle.getString("updateUserPageRequiredValuesMissed"));
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/myAccountInfo.jsp");
					rd.forward(request, response);
				}
			}
			
			//Delete User from NutchUser table by passing username 
			else if (event.equalsIgnoreCase("deleteUserConfirm")) {
				String userName = request.getParameter("uname");
				if(userName!=null  && !userName.isEmpty()){
					Connection conn = JDBCConnector.getConnection();
					if (conn == null) {
						logger.error("Unable to get Connection from Database: in DeleteUser block:");
						request.setAttribute("errorMessage", bundle.getString("connectionNotAvailable"));
						request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
					}
					else{
						PreparedStatement  ps = null;
						ResultSet rs = null;
						try {
							String query = " Delete from  NUTCH_USER WHERE username = ?";
							ps = conn.prepareStatement(query);
							ps.setString(1, userName);
							rs = ps.executeQuery();
							conn.commit();
							if (rs != null && rs.next()) {
								logger.info("User: "+userName+" has deleted successfully:");
								request.setAttribute("successMessage", bundle.getString("userDeleted"));
								request.getRequestDispatcher("/loginAuthenticate?requestType=myAccount").forward(request, response);
							}
						} catch (Exception e) {
							logger.error("Error while deleting user: "+userName+" from NUTCH_USER:" + e.getMessage());
							request.setAttribute("errorMessage", bundle.getString("deleteUserError"));
							request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
						} finally {
								try {
									if (ps != null) {
										ps.close();
									}
									if(conn != null){
										conn.close();
									}	
								} catch (SQLException e) {
									logger.error("Error while closing connection in DeleteUser block" + e.getMessage());
								}
						}
					}
				}else{
					request.setAttribute("errorMessage", bundle.getString("userNameNotProvided"));
					request.getRequestDispatcher("/jsp/myAccountInfo.jsp").forward(request, response);
					logger.error("Provided UserName is null");
				}
			}
	        
			//show status process block
			else if (event.equalsIgnoreCase("showStatus")) {
				eventName = "showStatus";
				int page =1;
				CrawlUtil cUtil = new CrawlUtil();
				request.getSession().setAttribute("eventName", eventName);
				try {
					LinkedHashMap<String, LinkedList<String>> status = cUtil.getStatus(page);
					request.setAttribute("statusMap", status);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
					request.setAttribute("errorMessage", bundle.getString("showStatusPageError"));
				}
				RequestDispatcher rd = request.getRequestDispatcher("/jsp/showStatus.jsp");
				rd.forward(request, response);
			} 
			else if (event.equalsIgnoreCase("status")) {
				eventName = "showStatus";
				CrawlUtil cUtil = new CrawlUtil();
				int page =1;
				boolean showNextPage = false;
				 if(request.getParameter("page") != null){
			        page = Integer.parseInt(request.getParameter("page"));
				 }
				 int totalRecords = cUtil.getNoOfRecords();
				 int remainingRecords = totalRecords - page*100;
				 if(remainingRecords > 0){
					 showNextPage = true;
				 }
				try {
					if(cUtil.checkRequestProcessStatus()){
						request.setAttribute("errorMessage", "REQUEST IS IN PROCESS");
						request.setAttribute("actionProgress", "true");
					}
					LinkedHashMap<String, LinkedList<String>> status = cUtil.getStatus(page);
					request.setAttribute("statusMap", status);
					request.setAttribute("showNextPage", showNextPage);
					request.setAttribute("currentPage", page);
					
				} catch (Exception e) {
					logger.error("Error status block "+e.getLocalizedMessage());
					request.setAttribute("errorMessage", bundle.getString("showStatusPageError"));
				}
				RequestDispatcher rd = request.getRequestDispatcher("/jsp/status.jsp");
				rd.forward(request, response);
			}
			
			//Cancel redirect to home page
			else if (event.equalsIgnoreCase("Cancel")) {
				response.sendRedirect("/hm/index");
			}
			
			//Monitor View Home page
			else if (event.equalsIgnoreCase("monitorView")) {
				eventName = "Save";
				CrawlUtil cUtil = new CrawlUtil();
				try{
					request.setAttribute("eventname", eventName);
					request.setAttribute("folderPaths", cUtil.getStaticFolderPaths());
					request.setAttribute("regexList",cUtil.getStaticRegex());
					request.setAttribute("jspPathList",cUtil.getJspPath());
					request.setAttribute("segmentUrlMap",cUtil.getSpecificFileDetails());
					request.setAttribute("domainId", domainId);
					LinkedHashMap<Integer,String> segmentNames = cUtil.getCrawlSegmentNames(cUtil.getLiveDomainId(null), null);
					request.setAttribute("segmentNames", segmentNames);
					request.setAttribute("docRoot", new MonitorUtil().getDocRoot());
					
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/monitorView.jsp");
					rd.forward(request, response);
				} catch(Exception e){
					logger.error("Error while loading MonitorView Page: "+e.getLocalizedMessage());
					request.setAttribute("errorMessage", bundle.getString("monitorViewPageError"));
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/monitorView.jsp");
					rd.forward(request, response);
				}
			}
			
			//Purge View Home page
			else if (event.equalsIgnoreCase("purgeCDN")) {
				eventName = "purgeCDN";
				Properties prop = new Properties();
				InputStream input = null;
				try {
					input = getClass().getClassLoader().getResourceAsStream("purge-config.properties");
					// load a properties file
					prop.load(input);
					logger.info(prop.getProperty("hostName"));
					request.setAttribute("eventname", eventName);
					
					String serializedMap = prop.getProperty("hostName");
					Map<String, String> finalsitemap = new HashMap<String, String>();
					JSONArray a;
					// converting string hostnames Map to JSONArray and then JSONArray to MAP<String, String>
					a = new JSONArray(serializedMap);
					for (int n = 0; n < a.length(); n++) {
						JSONObject object = a.getJSONObject(n);
						Map<String, String> sitemap = new HashMap<String, String>();
						ObjectMapper mapper = new ObjectMapper();
						try {
							sitemap = mapper.readValue(object.toString(),
									new TypeReference<HashMap<String, String>>() {
									});
							for (Map.Entry<String, String> entry : sitemap.entrySet()) {
								finalsitemap.put(entry.getKey(), entry.getValue());
							}
							
						} catch (Exception e) {
							logger.error("Error while converting  JSONArray to MAP : in purgeCDN block:"+e.getMessage());
							request.setAttribute("errorMessage", bundle.getString("purgeCDNPageError"));
							RequestDispatcher rd = request.getRequestDispatcher("/jsp/purgeCDN.jsp");
							rd.forward(request, response);
						}
					}
					//Fetching PurgeDetails from PURGE_MASTER table and setting to purgeDetailsMap
					LinkedHashMap<String, LinkedList<String>> purgeDetails = new LinkedHashMap<String, LinkedList<String>>();
					purgeDetails = CrawlUtil.getPurgeDetailStatus();
					request.setAttribute("purgeDetailsMap", purgeDetails);
					//setting hostNames to sitemap
					request.getSession().setAttribute("sitemap", finalsitemap);
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/purgeCDN.jsp");
					rd.forward(request, response);
				} catch (Exception e) {
					logger.error("Error while loading purgeCDN Page:"+e.getMessage());
					request.setAttribute("errorMessage", bundle.getString("purgeCDNPageError"));
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/purgeCDN.jsp");
					rd.forward(request, response);
				}
			}
			
			// To reset the status
			else if (event.equalsIgnoreCase("resetConfirm")) {
				//PerformAction performAction = new PerformAction();
				CrawlUtil cUtil = new CrawlUtil();
				eventName = "showStatus";
				String requestId = request.getParameter("requestId");
				if(requestId != null && !requestId.isEmpty()){
					boolean success = performAction.forceClearLock(requestId);
					if(!success){
						request.setAttribute("errorMessage", bundle.getString("forceClearLockError"));
					}
					// request.getSession().setAttribute("eventName", eventName);
					try {
						LinkedHashMap<String, LinkedList<String>> status = cUtil.getStatus(1);
						request.setAttribute("statusMap", status);
					} catch (Exception e) {
						logger.error("Error while executing  resetConfirm block: "+e.getLocalizedMessage());
						request.setAttribute("errorMessage", bundle.getString("resetConfirmError"));
					}
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/status.jsp");
					rd.forward(request, response);
				}else{
					logger.error("Error while executing resetConfirm block: RequestId value Missed:");
					request.setAttribute("errorMessage", bundle.getString("resetConfirmRequiredValuesMissed"));
					RequestDispatcher rd = request.getRequestDispatcher("/jsp/status.jsp");
					rd.forward(request, response);
				}
			}

			if (eventList.contains(event)) {
				doPost(request, response);
			}
		} else{
			response.sendRedirect("/hm/index");
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int domainId = 0;
		String domainNameList = null;
		String[] checkedSegments = null;
		if (request.getParameter("domainId") != null && request.getParameter("domainId") != "") {
			domainId = Integer.parseInt(request.getParameter("domainId"));
		}
		String event = request.getParameter("event");
		if (event.contains("Confirm")) {
			checkedSegments = request.getParameterValues("checkedSegments");
		} else {
			checkedSegments = request.getParameterValues("segmentIds");
		}
		String uname=request.getParameter("uname");
		String url = request.getParameter("url");
		String[] selectedURLS = request.getParameterValues("selectedURLS");
		String segmentId = request.getParameter("segmentId");
		String[] domainIdList = request.getParameterValues("domainIds");
		
		String domainName = request.getParameter("domainName");
		String segmentNames = request.getParameter("segmentNames");
		String isApi = request.getParameter("isApi");
		boolean isThreadWait = Boolean.valueOf(request.getParameter("isThreadWait")).booleanValue();
		// getting request id which need to track the status of the request
		String trackerId = request.getParameter("trackerId");
		String tag = request.getParameter("tag");
		String all = request.getParameter("all");
		String recursive = request.getParameter("recursive");
		String siteKey = request.getParameter("siteName");
		String actionName = request.getParameter("actionName");
		String transformationType = request.getParameter("transformationType");
		String move = request.getParameter("move");
		CrawlUtil cUtil = new CrawlUtil();
		
		//getting domainNames to display in confirmation page
		if(domainId != 0){
			domainNameList =cUtil.getDomainName(domainId);
		}else if(domainIdList != null && domainIdList.length > 0){
			for (String domain : domainIdList) {
				if(domainNameList == null){
					domainNameList = cUtil.getDomainName(Integer.parseInt(domain));
				}else{
					domainNameList = domainNameList +", "+cUtil.getDomainName(Integer.parseInt(domain));
				}
			}
		}
		
		if(domainIdList == null && isApi != null){
			if(request.getParameter("domainNames") != null){
				String domainIds = null;
				String[] domainNames = request.getParameter("domainNames").split(",");
				for (String domain : domainNames) {
					if(domainIds == null){
						domainIds = String.valueOf(cUtil.getDomainId(domain));
					}else{
						domainIds = domainIds +","+String.valueOf(cUtil.getDomainId(domain));
					}
				}
				if(domainIds != null){
					domainIdList = domainIds.split(",");
				}
			}
		}
		
		//Fetching domainName from DOMAIN table by passing DOMAIN_NAME value
		if (domainId == 0 && domainName != null && !domainName.isEmpty()) {
			domainId = cUtil.getDomainId(domainName);
		}
		
		if (selectedURLS == null && isApi != null) {
			String apiselectedURLS = request.getParameter("apiselectedURLS");
			if (apiselectedURLS != null && !apiselectedURLS.isEmpty()) {
				selectedURLS = apiselectedURLS.split(",");
			}
		}

		if (checkedSegments == null && segmentNames != null && !segmentNames.isEmpty()) {
			List<String> segments = cUtil.getSegments(segmentNames,domainId);
			checkedSegments = (String[]) segments.toArray(new String[segments.size()]);
		}
		if(checkedSegments == null && segmentId != null && !segmentId.isEmpty()){
			checkedSegments = (String[]) new String[]{segmentId};
		}
		if (url != null && segmentNames != null && segmentId == null) {
			List<String> segments = cUtil.getSegments(segmentNames,domainId);
			if (segments.size() > 0 && segments.size() == 1) {
				segmentId = segments.get(0);
			}
		}
		
		//Checking event value contains suffix Confirm  or not
		if (event.contains("Confirm")) {
			//Checking if event is coming from API Call or HM UI Call
			if(isApi != null){
				String domain_Name = null;
				String seg_Name = null;
				//Constructing Action Name by using DomainName, Segment Names
				if(checkedSegments!=null && checkedSegments.length>0){
					if(domainId > 0){
						domain_Name = cUtil.getDomainName(domainId);
					}
					for(String segId : checkedSegments){
						if(seg_Name == null){
							seg_Name = cUtil.getSegmentName(segId);
						}else{
							seg_Name = seg_Name + "," +cUtil.getSegmentName(segId);
						}
					}
					if(domain_Name !=null && seg_Name!=null){
						actionName ="Domain: "+ domain_Name+ "; Segment: "+ seg_Name;
					} else if(domain_Name !=null && seg_Name==null){
						actionName ="Domain: "+ domain_Name;
					}
				}
				else{
					if(domainId > 0){
						domain_Name = cUtil.getDomainName(domainId);
					}
					if(segmentId != null){
						seg_Name = cUtil.getSegmentName(segmentId);
					}
					if(domain_Name !=null && seg_Name!=null){
						actionName ="Domain: "+ domain_Name+ "; Segment: "+ seg_Name;
					} else if(domain_Name !=null && seg_Name==null){
						actionName ="Domain: "+ domain_Name;
					}
				}
				//Constructing action name if event is realted to Lock
				if(event.equalsIgnoreCase("issueLockConfirm")){
					actionName = "Issue Lock Action";
				}else if(event.equalsIgnoreCase("clearLockConfirm")){
					actionName = "Release Lock Action";
				}
			}
			//PerformAction performAction = new PerformAction();
			String forward = performAction.performAction(domainId, domainIdList,event,
					checkedSegments, url, segmentId, isApi, selectedURLS,
					trackerId,all,recursive, isThreadWait,siteKey,actionName,tag);
			if (forward != null) {
				RequestDispatcher rd = request.getRequestDispatcher(forward);
				rd.forward(request, response);
			}
		} 
		// if event value not contains suffix Confirm then redirecting request to ConfirmPage and appending suffix as Confirm for event value
		else {
			String domain_Name = null;
			String seg_Name = null;
			
			//Constructing Action Name by using domainId, checkedSegments
			if(checkedSegments!=null){
				if(domainId > 0){
					domain_Name = cUtil.getDomainName(domainId);
				}
				for(String segId : checkedSegments){
					if(seg_Name == null){
						seg_Name = cUtil.getSegmentName(segId);
					}else{
						seg_Name = seg_Name + "," +cUtil.getSegmentName(segId);
					}
				}
				if(domain_Name !=null && seg_Name!=null){
					actionName ="Domain: "+ domain_Name+ "; Segment: "+ seg_Name;
				} else if(domain_Name !=null && seg_Name==null){
					actionName ="Domain: "+ domain_Name;
				}
			}
			//Constructing Action Name by using domainId, segmentId
			else{
				if(domainId > 0){
					domain_Name = cUtil.getDomainName(domainId);
				}
				if(segmentId != null){
					seg_Name = cUtil.getSegmentName(segmentId);
				}
				if(domain_Name !=null && seg_Name!=null){
					actionName ="Domain: "+ domain_Name+ "; Segment: "+ seg_Name;
				} else if(domain_Name !=null && seg_Name==null){
					actionName ="Domain: "+ domain_Name;
				}
			}
			request.setAttribute("uname", uname);
			request.setAttribute("domainNameList", domainNameList);
			request.setAttribute("eventname", event.concat("Confirm"));
			request.setAttribute("domainId", domainId);
			request.setAttribute("requestId", request.getParameter("requestId"));
			request.setAttribute("checkedsegments", checkedSegments);
			request.setAttribute("selectedURLS", selectedURLS);
			request.setAttribute("url", url);
			request.setAttribute("domainIdList", domainIdList);
			request.setAttribute("segmentId", segmentId);
			request.setAttribute("actionName", actionName);
			request.setAttribute("transformationType", transformationType);
			request.setAttribute("move", move);
			RequestDispatcher rd = request.getRequestDispatcher("/jsp/confirmPage.jsp");
			rd.forward(request, response);
		}
	}

}
