package com.hm.crawl.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.nutch.tools.JDBCConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.vo.RequestVO;

public class RequestMasterCURD {
	public static final Logger logger = LoggerFactory.getLogger(RequestMasterCURD.class);
	
	
	/**
	 * this method is used to insert the request parameters into DB
	 * @param domainId
	 * @param eventName
	 * @param checkedSegments
	 * @param url
	 * @param segmentId
	 * @param isApi
	 * @param selectedURLS
	 * @param trackId
	 * @param all
	 * @param recursive
	 */
	public int createRequest(int domainId,String [] domainIdList, String eventName,
			String[] checkedSegments, String url, String segmentId,
			String isApi, String[] selectedURLS, int trackId, String all,
			String recursive,String siteKey, String actionName, String status,String startTime,String tag) {

		Connection conn = JDBCConnector.getConnection();
		int requestId = 0;
		String checked_segments = null;
		String domain_list = null;
		
		int seqNum = 0;
		if (checkedSegments != null && checkedSegments.length > 0) {
			for (String segment : checkedSegments) {
				if (checked_segments == null) {
					checked_segments = segment;
				} else {
					checked_segments = checked_segments + "," + segment;
				}

			}
		}
		
		if(domainIdList != null && domainIdList.length>0){
			for (String domain : domainIdList) {
				if (domain_list == null) {
					domain_list = domain;
				} else {
					domain_list = domain_list + "," + domain;
				}

			}
		}
		

		if (conn != null) {

			PreparedStatement pstmt = null;
			PreparedStatement stmt = null;
			Statement stmtNextVal = null;
			ResultSet rs = null;
			String insertQuery = "INSERT INTO REQUEST_PROCESSOR (REQUEST_ID,DOMAIN_ID,EVENT,CHECKED_SEGMENTS,URL,SEGMENT_ID,IS_API,TRACK_ID,IS_ALL,IS_RECURSIVE,SITEKEY,ACTION,STATUS,DOMAIN_LIST,REQUEST_STARTED,TAG) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			try {

				stmtNextVal = conn.createStatement();
				stmtNextVal
						.execute("SELECT REQUEST_PROCESSOR_SEQUENCE.NEXTVAL FROM DUAL");
				rs = stmtNextVal.getResultSet();
				if (rs.next()) {
					seqNum = rs.getInt("NEXTVAL");
				}
				if(seqNum != 0){
					pstmt = conn.prepareStatement(insertQuery);
					pstmt.setInt(1, seqNum);
					pstmt.setInt(2, domainId);
					pstmt.setString(3, eventName);
					pstmt.setString(4, checked_segments);
					pstmt.setString(5, url);
					pstmt.setString(6, segmentId);
					if(isApi != null && !isApi.isEmpty()){
					if (isApi.equalsIgnoreCase("true")) {
						pstmt.setInt(7, 1);
					} else {
						pstmt.setInt(7, 0);
					}
					}else {
						pstmt.setInt(7, 0);
					}
					//pstmt.setString(8, checked_urls);
					pstmt.setInt(8, trackId);
					pstmt.setString(9, all);
					pstmt.setString(10, recursive);
					pstmt.setString(11, siteKey);
					pstmt.setString(12, actionName);
					pstmt.setString(13, status);
					pstmt.setString(14, domain_list);
					pstmt.setString(15, startTime);
					pstmt.setString(16, tag);
					pstmt.execute();
					
					if(selectedURLS != null && selectedURLS.length>0){
					String insertQuery1 = "INSERT INTO REQUEST_PROCESSOR_PARAMS (REQUEST_ID,CHECKED_URLS) VALUES(?,?)";
					stmt = conn.prepareStatement(insertQuery1);
					for (String selectedUrl : selectedURLS) {
						stmt.setInt(1, seqNum);
						stmt.setString(2, selectedUrl);
						stmt.addBatch();
					}
					stmt.executeBatch();
					}
					conn.commit();
					requestId = seqNum;
					if(eventName.equalsIgnoreCase("issueLockConfirm")){
						setRequestTime(seqNum);
					}
					conn.commit();
				}else{
					logger.error("Error while Generating Request Id in createRequest() method:");
				}
			} catch (SQLException e) {
				logger.error("Error while inserting into REQUEST_PROCESSOR in createRequest() method: "+e.getMessage());
				try {
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("Error while rolling back data into REQUEST_PROCESSOR in createRequest() method: "+e1.getMessage());
				}
				
			} finally {
				try {
					if (stmtNextVal != null) {					
							stmtNextVal.close();					
					}
					if(stmt != null){
						stmt.close();
					}
					if (pstmt != null) {			
							pstmt.close();						
						} 
					if(rs != null){
						rs.close();
					}
					if(conn != null){
					conn.close();
					}
				}catch (SQLException e) {
						logger.error("Error while closing Connection in createRequest() method: "+e.getMessage());
					}

				}
			}
		return requestId;
		}

	
	/**
	 * This method returns all the requests with pending status
	 * @return
	 */
	public List<Integer> getRequests(){
		Connection conn = JDBCConnector.getConnection();
		CrawlUtil cUtil = new CrawlUtil();
		Statement  stmt = null;
		ResultSet rs = null;
		String st = null;
		List<Integer> requestIdList = new ArrayList<Integer>();
		String stausQuery = cUtil.getStausQuery();
		if(conn != null){
			try {
				stmt = conn.createStatement();
				
				String query = "SELECT REQUEST_ID FROM REQUEST_PROCESSOR WHERE STATUS ='PENDING' ORDER BY REQUEST_ID" ;
				rs= stmt.executeQuery(stausQuery);
				if(!rs.next()){
					rs = null;
					rs = stmt.executeQuery(query);
					while(rs.next()){
						requestIdList.add(rs.getInt("REQUEST_ID"));
						
					}
				}
			}catch (SQLException e) {
				logger.error("Error in getRequests() method"+e.getMessage());
			}finally{
				
					try {
						if(rs != null){
							rs.close();
						}
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						logger.error("Unable to close the connection  in getRequests() method"+e.getMessage());
					}
				}
			}
		return requestIdList;
	}
	
	public RequestVO readByPrimaryKey(int requestId){
		
		Connection conn = JDBCConnector.getConnection();
		RequestVO request = new RequestVO();
		Statement stmt = null;
		ResultSet rs = null;
		
		if(conn != null){
			if(requestId != 0){
				String query = "SELECT * FROM REQUEST_PROCESSOR WHERE REQUEST_ID = "+requestId;
				try {
					stmt = conn.createStatement();
					rs = stmt.executeQuery(query);
					while(rs.next()){
						request.setRequestId(rs.getInt("REQUEST_ID"));
						request.setDomainId(rs.getInt("DOMAIN_ID"));
						request.setEvent(rs.getString("EVENT"));
						request.setCheckedSegments(rs.getString("CHECKED_SEGMENTS"));
						request.setUrl(rs.getString("URL"));
						request.setSegmentId(rs.getString("SEGMENT_ID"));
						if(rs.getInt("IS_API") == 1){
							request.setApi(true);
						}else{
							request.setApi(false);
						}
						//request.setCheckedUrls(rs.getString("CHECKED_URLS"));
						request.setTrackId(rs.getInt("TRACK_ID"));
						request.setAll(rs.getString("IS_ALL"));
						request.setRecursive(rs.getString("IS_RECURSIVE"));
						request.setStatus(rs.getString("STATUS"));
						request.setSiteKey(rs.getString("SITEKEY"));
						request.setDomainList(rs.getString("DOMAIN_LIST"));
					}
					String query1 = "SELECT CHECKED_URLS FROM REQUEST_PROCESSOR_PARAMS WHERE REQUEST_ID = "+requestId;
					String checked_urls = null;
					rs=stmt.executeQuery(query1);
					while(rs.next()){
						if(checked_urls == null){
							checked_urls = rs.getString("CHECKED_URLS");
						}
						else{
							checked_urls = checked_urls+","+rs.getString("CHECKED_URLS");
						}
					}
					if(checked_urls != null){
						request.setCheckedUrls(checked_urls);
					}
				
				} catch (SQLException e) {
					logger.error("Error in readByPrimaryKey() method"+e.getMessage());
				}finally{
						try {
							if(stmt != null){
								stmt.close();
							}
							if(rs != null){
								rs.close();
							}
							if(conn != null){
								conn.close();
							}
						} catch (SQLException e) {
							logger.error("unable to close the connection in readByPrimaryKey() method"+e.getMessage());
						}
						
					}
				}else{
					logger.error("Request id is null in readByPrimaryKey() method: "+requestId);
				}
			}
		return request;
		
	}
	/**
	 * this method changes request status pending to PROCESSED
	 * @param requestId
	 */
	public void changeRequestStatus(int requestId,String message){
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		if(conn != null){
			if(requestId != 0){
			try {
				stmt = conn.createStatement();
				String query ="UPDATE REQUEST_PROCESSOR SET STATUS = '"+message+"' WHERE REQUEST_ID ="+requestId;
				stmt.execute(query);
				conn.commit();
			} catch (SQLException e) {
				logger.error("Error while updating REQUEST_PROCESSOR in changeRequestStatus() method"+e.getMessage());
			}finally{
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						logger.error("unable to close the connection in changeRequestStatus() method"+e.getMessage());
					}
					
				}
			}else{
				logger.error("Request id is null in changeRequestStatus() method: ");
			}
		}
	}
	
	public boolean getThreadState(){
		Connection conn = JDBCConnector.getConnection();
		boolean isRunning = false;
		Statement stmt = null;
		if(conn != null){
			String query = "SELECT * FROM REQUEST_PROCESSOR WHERE STATUS = 'PROCESSING'";
			try {
				stmt.execute(query);
				ResultSet rs = stmt.getResultSet();
				if(rs.next()){
					isRunning = true;
				}
			} catch (SQLException e) {
				logger.error("Error in getThreadState() method"+e.getMessage());
			}
		}
		return isRunning;
		
	}
	
	public void setRequestTime(int requestId){
		Connection conn = JDBCConnector.getConnection();
		String date = new Date().toString();
		if(conn != null){
			Statement stmt = null;
			try {
				if(requestId != 0){
					stmt = conn.createStatement();
					String query = "UPDATE REQUEST_PROCESSOR SET REQUEST_STARTED= '"+ date+"', REQUEST_COMPLETED ='Request is in Process' WHERE REQUEST_ID= "+ requestId;
					stmt.execute(query);
					conn.commit();
					logger.info("REQUEST_STARTED Time stamp updated  " + date + " TO Request " + requestId);
				}else{
					logger.error("Request id is null in setRequestTime() method:");
				}
			} catch (SQLException e) {
				logger.error("Error while executing update REQUEST_STARTED in REQUEST_PROCESSOR table in setRequestStartTime() method:" + e.getMessage());
			}finally{
				
					try {
						if(conn != null){
							conn.close();
						}
						if(stmt != null){
							stmt.close();
						}
					} catch (SQLException e) {
						logger.error("Error while closing connection in setRequestStartTime() method:"+e.getMessage());
					}
				
			}
		}
	}
	
	public void setRequestEndTime(int requestId){
		Connection conn = JDBCConnector.getConnection();
		String date = new Date().toString();
		if(conn != null){
			Statement stmt = null;
			try {
				if(requestId != 0){
					stmt = conn.createStatement();
					String query = "UPDATE REQUEST_PROCESSOR SET REQUEST_COMPLETED= '"+ date+"' WHERE REQUEST_ID= "+ requestId;
					stmt.execute(query);
					conn.commit();
					logger.info("REQUEST_COMPLETED Time stamp updated " + date + " TO Request " + requestId);
				}else{
					logger.error("Request id is null in setRequestEndTime() method:");
				}
			} catch (SQLException e) {
				logger.error("Error while executing update REQUEST_COMPLETED in REQUEST_PROCESSOR table in setRequestEndTime() method:" + e.getMessage());
			}finally{
				
					try {
						if(conn != null){
							conn.close();
						}
						if(stmt != null){
							stmt.close();
						}
					} catch (SQLException e) {
						logger.error("Error while closing connection in setRequestEndTime() method"+e.getMessage());
					}
				
			}
		}
	}
	
	public void verifyRequestStatus(int requestId){
		Connection conn = JDBCConnector.getConnection();
		boolean changeStatus = false;
		if(conn != null){
			Statement stmt = null;
			ResultSet rs = null;
			try {
				if(requestId != 0){
					stmt = conn.createStatement();
					String statusQuery = "SELECT * FROM REQUEST_PROCESSOR WHERE STATUS IN ('HTMLIZATION DOMAIN COMPLETED','HTMLIZATION SEGMENTS COMPLETED','REFRESHING DOMAIN COMPLETED','REFRESHING SEGMENTS COMPLETED','REFRESHING URL COMPLETED','REFRESHING SELECTED URLS COMPLETED','DELETE SELECTED SEGMENT HTML PROCESS COMPLETED','DELETE DOMAIN HTML PROCESS COMPLETED','DELETE URL HTML PROCESS COMPLETED','DELETE FILES  PROCESS COMPLETED') AND REQUEST_ID = "+requestId ;
					rs = stmt.executeQuery(statusQuery);
					while (rs.next()) {
	
						changeStatus = true;
	
					}
					if(!changeStatus){
						String updateQuery = "UPDATE REQUEST_PROCESSOR SET REQUEST_COMPLETED= '"+ new Date().toString()+"' AND  STATUS = 'PROCESS FAILED' WHERE REQUEST_ID= "+ requestId;
						stmt.execute(updateQuery);
						conn.commit();
						logger.info("Request status verified and process failed");
					}else{
						logger.info("Request status verified and process completed successfully");
					}
				}else{
					logger.error("Request id is null in verifyRequestStatus() method:");
				}
			} catch (SQLException e) {
				logger.error("Error while verifying the status in verifyRequestStatus() method" + e.getMessage());
			}finally{
					
						try {
							if(conn != null){
								conn.close();
							}
						} catch (SQLException e) {
							logger.error("Error while closing connection in verifyRequestStatus() method"+ e.getMessage());
						}
					
						try {
							if(stmt != null){
								stmt.close();
							}
						} catch (SQLException e) {
							logger.error("Error while closing statement in verifyRequestStatus() method"+e.getMessage());
						}
						
						try {
							if(rs != null){
								rs.close();
							}
						} catch (SQLException e) {
							logger.error("Error while closing resultset in verifyRequestStatus() method"+e.getMessage());
						}
						
			}
		}
	}
	
	public boolean verifyLockStatus(String requestId){
		Connection conn = JDBCConnector.getConnection();
		boolean success = false;
		if(conn != null){
			Statement stmt = null;
			ResultSet rs = null;
			try{
				stmt = conn.createStatement();
					String query = "SELECT STATUS FROM REQUEST_PROCESSOR WHERE REQUEST_ID= "+requestId;
					rs = stmt.executeQuery(query);
					if(rs.next()){
						if(rs.getString("STATUS").equalsIgnoreCase("LOCK ISSUED")){
							success =true;
						}
					}
			}catch (SQLException e) {
				logger.error("Error while getting status from REQUEST_PROCESSOR in verifyLockStatus() method:"+e.getMessage()); 
			}finally{
				try {
					 if(rs != null){
						 rs.close();
					  }
					 if(stmt != null){
						stmt.close();
					  }
					 if(conn != null){
						conn.close();
					  }
					} catch (SQLException e) {
						logger.error("Error while closing connection in verifyLockStatus() method:"+ e.getMessage());
					}
				}
		}else{
			logger.error("Unable to get the connection from DB: in verifyLockStatus() method:");
		}
		return success;
		
	}
	
	public boolean clearMarker(String event,String requestId){
		Connection conn = JDBCConnector.getConnection();
		boolean success = false;
		String query = null;
		if(conn != null){
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				if(event != null && !event.isEmpty() && requestId != null && !requestId.isEmpty()){
					query = "UPDATE REQUEST_PROCESSOR SET EVENT='"+event+"', STATUS='LOCK RELEASED',ACTION ='Release Lock Action', REQUEST_COMPLETED='"+new Date().toString()+ "' WHERE REQUEST_ID= "+requestId;
					stmt.executeUpdate(query);
					conn.commit();
					success = true;
				}else{
					logger.error("event is null or request id is null in clearMarker() method: ");
				}
			} catch (SQLException e) {
				logger.error("Error while creating updating REQUEST_PROCESSOR in clearMarker() method:"+e.getMessage()); 
				try {
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("Error while creating rolling back REQUEST_PROCESSOR in clearMarker() method:"+e.getMessage());
				}
				
			}finally{
					try {
						if(stmt != null){
							stmt.close();
						  }
						if(conn != null){
							conn.close();
						}
				} catch (SQLException e) {
					logger.error("Error while closing connection in clearMarker() method"+ e.getMessage());
				}
			}
		}else{
			logger.error("Unable to get the connection from DB: in clearMarker() method:");
		}
		return success;
	}
	//Resets the status as well as domain status and crawlstatus
	public boolean doReset(String requestId){
		Connection conn = JDBCConnector.getConnection();
		CrawlUtil cUtil = new CrawlUtil();
		boolean success = false;
		if(conn != null){
			Statement stmt = null;
			Statement updateStmt = null;
			ResultSet rs = null;
			int domainId = 0;
			String query = null;
			try {
				stmt= conn.createStatement();
				updateStmt = conn.createStatement();
				if(requestId != null && !requestId.isEmpty()){
					if(checkRequestProcessStatus(Integer.parseInt(requestId))){
						query = "UPDATE REQUEST_PROCESSOR SET STATUS = 'LOCK RELEASED FORCEIBLY',REQUEST_COMPLETED = '"+new Date().toString()+"' WHERE REQUEST_ID = "+requestId;
						stmt.execute(query);
						query = "SELECT * FROM REQUEST_PROCESSOR  WHERE REQUEST_ID = "+requestId;
						rs = stmt.executeQuery(query);
						//Changing the Crawl status in Domain table
						if(rs.next()){
							if(rs.getString("EVENT").equalsIgnoreCase("htmlizeDomainConfirm") || rs.getString("EVENT").equalsIgnoreCase("AutoCrawlConfirm") || rs.getString("EVENT").equalsIgnoreCase("selectedDomainHtmlizeConfirm")){
								domainId = rs.getInt("DOMAIN_ID");
								String domainList = rs.getString("DOMAIN_LIST");
								String updateQuery = null;
								if(domainList != null){
									updateQuery = "UPDATE DOMAIN SET CRAWL_STATUS ='DONE' WHERE DOMAIN_ID IN ("+domainList+")";
								}else if (domainId != 0){
									updateQuery = "UPDATE DOMAIN SET CRAWL_STATUS = 'DONE' WHERE DOMAIN_ID = "+domainId;
								}
								updateStmt.execute(updateQuery);
							}
						}
						//setting the progress = 0 in Crawl master table
						cUtil.changeProgressStatus(conn);
						conn.commit();
						success = true;
						logger.info("Status reset succesfull for request :" + requestId);
					}else{
						success = true;
						logger.info("Process already completed successfully");
					}
				}else{
					logger.error("requestId is null in doReset() method: ");
				}
			} catch (SQLException e) {
				logger.error("doReset() method: Error while resetting the status: "+e.getMessage());
				try {
					conn.rollback();
				} catch (SQLException e1) {
					logger.error("Error while rolling back the data in doReset() method: "+e1.getMessage());
				}
			}finally{
				try {
						if(conn != null){
							conn.close();
						}
						if(stmt != null){
							stmt.close();
						}
						if(updateStmt != null){
							updateStmt.close();
						}
						if(rs != null){
							rs.close();
						}
					
				} catch (SQLException e) {
					logger.error("Error while closing connection in doReset() method"+ e.getMessage());
				}
			}
		}
		return success;
	}
	
	public boolean checkRequestProcessStatus(int requestId){ 
		boolean actionStatus = true;
		Statement statusStmt = null;
		String status = null;
		ResultSet rs = null;
		Connection conn = JDBCConnector.getConnection();
	try {
		statusStmt = conn.createStatement();
		String query1 = "SELECT * FROM REQUEST_PROCESSOR WHERE STATUS IN ('PROCESS FAILED','LOCK RELEASED','LOCK RELEASED FORCEIBLY','HTMLIZATION DOMAIN COMPLETED','HTMLIZATION SEGMENTS COMPLETED','REFRESHING DOMAIN COMPLETED','REFRESHING SEGMENTS COMPLETED'," +
				"'REFRESHING URL COMPLETED','REFRESHING SELECTED URLS COMPLETED','DELETE SELECTED URL HTML PROCESS COMPLETED'," +
				"'DELETE SELECTED SEGMENT HTML PROCESS COMPLETED','DELETE DOMAIN HTML PROCESS COMPLETED','DELETE URL HTML PROCESS COMPLETED','DELETE FILES  PROCESS COMPLETED','PURGE URL PROCESS COMPLETED') AND REQUEST_ID = "+requestId ;
		
		rs = statusStmt.executeQuery(query1);
		if(rs.next()){
			actionStatus = false;
		}
	} catch (SQLException e) {
		logger.error("Error in checkRequestProcessStatus() method: "+e.getMessage());
	}finally{
			try {
					if(rs != null) {
						rs.close();
					}
					if(statusStmt != null){
						statusStmt.close();
					}
					if(conn != null){
						conn.close();
					}
			} catch (SQLException e) {
				logger.error("Error while closing connection in checkRequestProcessStatus() method:"+e.getMessage());
			}
		}
	return actionStatus;
	} 
}
