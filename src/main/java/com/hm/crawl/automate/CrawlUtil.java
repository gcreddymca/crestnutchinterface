package com.hm.crawl.automate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.nutch.tools.JDBCConnector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.DomainMasterCRUD;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.MonitorVO;
import com.hm.crawl.data.vo.SegmentVO;
import com.hm.util.TransformationUtil;



public class CrawlUtil {

	public static final Logger LOG = LoggerFactory.getLogger(CrawlUtil.class);
	
		 public void moveDirectory(String srcpath, String destPath) throws IOException{
			
		 File tempDirPath = new File(srcpath);
		 File DestDirPath = new File(destPath);
		 File[] files = null;
		 File[] files1 = null;
		 if(tempDirPath.exists()){
			 files = tempDirPath.listFiles();
			 files1 = DestDirPath.listFiles();
			 for (File file : files) {
	     		if(file.isDirectory()){
	     			/*for(File file1: files1){
        				if(file.getName().equals(file1.getName())){
        					deleteDirectory(file1.getAbsolutePath());
        				}
        			}*/
	     			moveDirectoryToDirectory(file, DestDirPath, false);
	     		}
	     		else if(file.isFile()){
	     			FileUtils.moveFileToDirectory(file, DestDirPath, false);
	     		}
			}
		 }
	}
		 
	 public static void moveDirectoryToDirectory(File src, File destDir, boolean createDestDir) throws IOException {
	        if (src == null) {
	            throw new NullPointerException("Source must not be null");
	        }
	        if (destDir == null) {
	            throw new NullPointerException("Destination directory must not be null");
	        }
	        if (!destDir.exists() && createDestDir) {
	            destDir.mkdirs();
	        }
	        if (!destDir.exists()) {
	            throw new FileNotFoundException("Destination directory '" + destDir +
	                    "' does not exist [createDestDir=" + createDestDir +"]");
	        }
	        if (!destDir.isDirectory()) {
	            throw new IOException("Destination '" + destDir + "' is not a directory");
	        }
	        moveDirectory(src, new File(destDir, src.getName()));
	    }
	 
	 public static void moveDirectory(File srcDir, File destDir) throws IOException {
	        if (srcDir == null) {
	            throw new NullPointerException("Source must not be null");
	        }
	        if (destDir == null) {
	            throw new NullPointerException("Destination must not be null");
	        }
	        if (!srcDir.exists()) {
	            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
	        }
	        if (!srcDir.isDirectory()) {
	            throw new IOException("Source '" + srcDir + "' is not a directory");
	        }
	       /* if (destDir.exists()) {
	            throw new IOException("Destination '" + destDir + "' already exists");
	        }*/
	        boolean rename = srcDir.renameTo(destDir);
	        if (!rename) {
	            FileUtils.copyDirectory( srcDir, destDir );
	            FileUtils.deleteDirectory( srcDir );
	            if (srcDir.exists()) {
	                throw new IOException("Failed to delete original directory '" + srcDir +
	                        "' after copy to '" + destDir + "'");
	            }
	        }
	    }
	
	 // Moving files for htmlize process 
	 public static int moveHtmlizedDomainDirectory(String srcpath, String destPath){
           int extVal =0;
	       try {  
		        Runtime r = Runtime.getRuntime();  
		        LOG.info("Files moving process started: for Domain:["+destPath+"]");
		        Process p = r.exec("rsync --recursive --delete "+srcpath+" "+destPath);  
		        p.waitFor();  
		        extVal = p.exitValue();
		        LOG.info("Process exit value: " + extVal); 
		        LOG.info("Files moving process ended: for Domain:["+destPath+"]");
		        if(extVal > 0){
		          LOG.info("Not deleting source directory:");
		        } else{
		          LOG.info("Deleting source directory: ["+srcpath+"*]");
		          try{
		                 Process delProcess = r.exec(new String[]{"sh", "-c", "rm -r "+srcpath});  
		                 delProcess.waitFor();
		                 extVal = delProcess.exitValue();
		                 LOG.info("Deleting source directory ended:"+extVal);
		          } catch (IOException e){  
		                extVal = 1;  
		                LOG.error("IO Exception in delete file process: - "+ e.getMessage());  
		          } catch (InterruptedException e1) {  
		                extVal = 1;  
		                LOG.error("Interrupted Exception in delete file process:- "+e1.getMessage()); 
		          } catch (Exception e2) {  
		                extVal = 1;  
		                LOG.error("Interrupted Exception in delete file process:- "+e2.getMessage());  
		          } 
		        }
	       } catch (IOException e) {  
		        extVal = 1;  
		        LOG.error("IO Exception - "+ e.getMessage());  
		    }  
		    catch (InterruptedException e1) {  
		        extVal = 1;  
		        LOG.error("Interrupted Exception - "+e1.getMessage()); 
		    }  
	       	catch (Exception e2) {  
			    extVal = 1;  
			    LOG.error("Interrupted Exception - "+e2.getMessage());  
		    } 
       return extVal;
	 }

	 
	 public boolean checkCrawlDirectory(String path){
		boolean flag = false;
		 File file = new File(path);
		File[] files = file.listFiles();
		if(files != null){
		for (File file2 : files) {
			if(file2.getName().equals("crawldb"))
			{
				flag=true;
			}
		  }
		}
		return flag;
	 }
	 
	/*
	 * this method deletes the directory and all sub directories
	 */
	public void deleteDirectory(String directoryName) {
		File directoryObj = new File(directoryName);
		if (directoryObj.exists()) {
			String[] fileList = directoryObj.list();
			if (fileList != null && fileList.length > 0) {
				for (String dirName : fileList) {
					deleteDirectory(directoryName + "/" + dirName);
				}
			}
			directoryObj.delete();
		}
	}
	
	public int createCrawlId(int domainId){
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		int seqNum = 0;
		if (conn != null) {
			PreparedStatement pstmt = null;
			try {
				stmt = conn.createStatement();
				stmt.execute("SELECT CRAWL_MASTER_SEQ.NEXTVAL FROM DUAL");
				rs = stmt.getResultSet();
				if(rs.next()) {
		        seqNum = rs.getInt("NEXTVAL");
				String query = "INSERT INTO CRAWL_MASTER (CRAWL_ID,LIVE,PROGRESS,CRAWL_START,DOMAIN_ID) VALUES(?,?,?,?,?)";
				pstmt=conn.prepareStatement(query);
				pstmt.setInt(1, seqNum);
				pstmt.setInt(2, 0);
				pstmt.setInt(3, 1);
				pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				pstmt.setInt(5, domainId);
				pstmt.execute();
				conn.commit();
				}
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					LOG.error("Error while closing connection" + e.getMessage());
				}
			} finally {
				closeJDBCObjects(rs, stmt, null, conn);
			}
		}else{
			LOG.error("Unable to get connection from DB: in createCrawlId() process?: ");
			seqNum = 0;
		}
	return seqNum;
  }
	
	//Get current live crawl id from CRAWL_MASTER
	public int getCrawlId(Connection conn, int domainId){
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		int crawl_id = 0;
		if(conn != null){
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				if(domainId != 0){
					String query = "SELECT CRAWL_ID FROM CRAWL_MASTER WHERE LIVE=1 AND DOMAIN_ID="+domainId;
					stmt.execute(query);
					rs = stmt.getResultSet();
					if(rs.next()){
						crawl_id = rs.getInt("CRAWL_ID");
					}
				}else{
					LOG.error("Domain id is null in getCrawlId() method:");
				}
			} catch (SQLException e) {
				LOG.error("Error while fetching details from CRAWL_MASTER table in getCrawlId() process: "+e.getMessage());
			}finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
					
				}
			}else{
				LOG.error("Unable to get the connection from DB: in getCrawlId() method: ");
			}
		return crawl_id;
	}	
	
	//Get current domain id
		public int getLiveDomainId(Connection conn){
			boolean connCreated = false;
			if(conn == null) {
				conn = JDBCConnector.getConnection();
				connCreated = true;
			}
			
			int domainId = 0;
			if(conn != null){
				Statement stmt = null;
				ResultSet rs = null;
				try {
					stmt = conn.createStatement();
					String query = "SELECT DOMAIN_ID FROM CRAWL_MASTER WHERE LIVE=1";
					stmt.execute(query);
					rs = stmt.getResultSet();
					if(rs.next()) {
						domainId = rs.getInt("DOMAIN_ID");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}finally {
					
						try {
							if (stmt != null) {
								stmt.close();
							}
							if(conn != null){
								if(connCreated) {
									conn.close();
								}
							}
							if(rs != null){
								rs.close();
							}
						} catch (SQLException e) {
							LOG.info("Error while closing connection" + e);
						}
					}
				}
			return domainId;
		}	
		
	/*
	 * This method returns all the segments ids configured for that domain
	 */
	public List<Integer> getCrawlSegmentIds(int domainId, Connection conn) {
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		List<Integer> segmentList = new ArrayList<Integer>();
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT SEGMENT_ID FROM  SEGMENT_MASTER where DOMAIN_ID="
						+ domainId+" and CRAWL = 1 order by priority";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						segmentList.add(rs.getInt("SEGMENT_ID"));
					}
				}
			} catch (SQLException e) {
				LOG.error("Error while fetching details from SEGMENT_MASTER table in getCrawlSegmentIds() process: "+e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
			}
			} else{
				LOG.error("Unable to get connection from DB: in getCrawlSegmentIds() method:");
				segmentList = null;
			}
		return segmentList;
	}
	
	/*
	 * This method returns all the segments ids configured for that domain
	 */
	public LinkedHashMap<Integer,String>  getCrawlSegmentNames(int domainId, Connection conn) {
		LinkedHashMap<Integer,String> SegmentIDNameMap = new LinkedHashMap<Integer,String>();
		boolean connCreated = false;
		//DomainMasterCRUD domainMasterCurd = new DomainMasterCRUD();
		//String domainName = null;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		//LinkedList<String> segmentNameList = new LinkedList<String>();
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT SM.SEGMENT_ID,SM.SEGMENT_NAME, D.DOMAIN_NAME FROM SEGMENT_MASTER SM, DOMAIN d where D.DOMAIN_ID = SM.DOMAIN_ID AND CRAWL = 1 order by D.DOMAIN_NAME";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						
						if(!rs.getString("SEGMENT_NAME").equalsIgnoreCase("default")){
							SegmentIDNameMap.put(rs.getInt("SEGMENT_ID"), rs.getString("DOMAIN_NAME")+"."+rs.getString("SEGMENT_NAME"));
						//segmentNameList.add(rs.getString("DOMAIN_NAME")+"."+rs.getString("SEGMENT_NAME"));
						}
					}
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
				}
			}
		return SegmentIDNameMap;
	}
	
	/**
	 * This method changes the Crawl_Status to null in DOMAIN table
	 * 
	 * @param domainId
	 */
	public void changeDomainCrawlStatus(int domainId, Connection conn){
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		String crawl_status = null;
		
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT CRAWL_STATUS FROM DOMAIN WHERE DOMAIN_ID = "+domainId;
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						crawl_status = rs.getString("CRAWL_STATUS");
					}
					if (crawl_status != null) {
						String updateQuery = "UPDATE domain SET CRAWL_STATUS= 'DONE'  WHERE DOMAIN_ID="	+domainId;
						boolean execute = stmt.execute(updateQuery);
						conn.commit();
					}
				}
			} catch (SQLException e) {
				LOG.error("Error while updating CRAWL_STATUS table in changeDomainCrawlStatus() process: "+e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
			}
		} else {
			LOG.error("Unable to get connection from DB: in changeDomainCrawlStatus() process:");
		}
	}
	
	
	/*
	 * this method returns all the html patterns
	 */

	public List<Integer> allHTMLPatterns(Connection conn) {
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		List<Integer> htmlPatterns = new ArrayList<Integer>();
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT * FROM SEGMENT_HTML_PATH_PATTERN";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						htmlPatterns.add(rs.getInt("PATTERN_ID"));
					}
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
				}
		}else{
			LOG.error("Unable to get connection from DB: in allHTMLPatterns() process:");
			htmlPatterns = null;
		}
		return htmlPatterns;
	}
	
	
	public Map<String, List<String>> getTransformationMap(List<Integer> segmentIds, int domainId, int crawlId) throws Exception {
		Map<String, List<String>> transformationsMap = new HashMap<String, List<String>>();
		SegmentVO segVo = null;
		SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
		for (Integer segmentID : segmentIds) {
			segVo = segmentCrud.readByPrimaryKey(String.valueOf(segmentID), null);
			if(segVo != null){
				if (segVo.getPathVO().isEmpty()) {
					LOG.error("HTML Path does not exist for " + segVo.getSegmentName()
							+ " segment.Please edit the Segment add the HTML Path.");
				} else {
					segmentCrud.generateURLforSegment(String.valueOf(segmentID),String.valueOf(domainId),crawlId);
					transformationsMap.put(segmentID.toString(), new TransformationUtil().getSegmentsTransformations(segmentID));
				}
			}
		}
		return transformationsMap;
	}
	
	
	/*
	 * this method returns all the transformations
	 */
	public List<Integer> getTransformations(Connection conn) {
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		List<Integer> transformationIDS = new ArrayList<Integer>();
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT * FROM TRANSFORMATION_MASTER";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						transformationIDS.add(rs.getInt("TRANSFORMATION_ID"));
					}
				}
			} catch (SQLException e) {
				LOG.error("Error while fetching details from TRANSFORMATION_MASTER table in getTransformations() process: "+e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
			}
		}else{
			LOG.error("Unable to get connection from DB: in getTransformations() process:");
			transformationIDS = null;
		}
		return transformationIDS;
	}
	
	
	public void changeProgressStatus(Connection conn){
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		if (conn != null) {
			  Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query="update CRAWL_MASTER  SET PROGRESS = 0 where PROGRESS =1";
				 stmt.execute(query);
				 conn.commit();
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					LOG.error("Error while closing connection" + e1.getMessage());
				}
			}finally {
				if(connCreated) {
					closeJDBCObjects(null, stmt, null, conn);
				}else {
					closeJDBCObjects(null, stmt, null, null);
				}
			}
		}
	}
	
	/**
	 * This method returns all the segments ids configured in that domain
	 * 
	 * @param domainId
	 * @return
	 */
	public List<Integer> getSegmentIds(int domainId, Connection conn) {
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		List<Integer> segmentList = new ArrayList<Integer>();
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT SEGMENT_ID FROM SEGMENT_MASTER where DOMAIN_ID="
						+ domainId;
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						segmentList.add(rs.getInt("SEGMENT_ID"));
					}
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
			}
		} else{
			LOG.error("Unable to get connection from DB: in getSegmentIds() process:");
			segmentList = null;
		}
		return segmentList;
	}
	
	public void changeCrawlStatus(int crawlId, int domainId, Connection conn){
		boolean connCreated = false;
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		if (conn != null) {
			Statement stmt = null;
			PreparedStatement pstmt = null;
			try {
			    
			  	String query = "update CRAWL_MASTER  SET LIVE = ?,CRAWL_COMPLETED=? where CRAWL_ID= "+crawlId;
			 	pstmt=conn.prepareStatement(query);
				pstmt.setInt(1, 1);
				pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
				pstmt.execute();
				stmt = conn.createStatement();
				String query1 ="UPDATE CRAWL_MASTER SET LIVE=0 WHERE CRAWL_ID !="+crawlId+" AND DOMAIN_ID="+domainId;
				stmt.execute(query1);
				conn.commit();
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					LOG.error("Error while closing connection" + e.getMessage());
				}
				LOG.error(e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(null, stmt, pstmt, conn);
				}else {
					closeJDBCObjects(null, stmt, pstmt, null);
				}
			}
		}
	}

	public void handleError(Connection conn, String errorMessage, int domainId,
			int crawlId,int trackerId,int requestId) {
		changeProgressStatus(conn);
		try {
			setCrawlStatus(conn, errorMessage, crawlId,trackerId,requestId);
		} catch (SQLException e) {
			LOG.error(e.getLocalizedMessage());
		}
	}

	public void handleSuccess(Connection conn, String successMessage,
			int domainId, int crawlId,int trackerId,int requestId) {
		changeProgressStatus(conn);
		if(crawlId > 0) {
			changeCrawlStatus(crawlId, domainId, conn);
		}
		try {
			setCrawlStatus(conn, successMessage, crawlId,trackerId,requestId);
		} catch (SQLException e) {
			LOG.error(e.getLocalizedMessage());
		}
		
	}
	
	public DomainVO getDomain(int domainId) {
		DomainVO domainVO = new DomainVO();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		try {
			domainVO = domainCrud.readByPrimaryKey(domainId);
		} catch (Exception e1) {
			LOG.error("Error while getting domain in getDomain() process: "+e1.getLocalizedMessage());
		}
		return domainVO;
	}

	public void setCrawlStatus(Connection conn, String message, int crawlId,int trackerId,int requestId)
			throws SQLException {
		boolean connCreated = false;
		if(trackerId != 0){
			setTrackerStatus(conn, trackerId, crawlId, message);
		}
		if (conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		
		if(message != null && message.length()>255)
		{
			message.substring(0, 254);
		}
		if (conn != null) {
			conn.setAutoCommit(false);
			  PreparedStatement pstmt = null;
			  String updateQuery = "UPDATE REQUEST_PROCESSOR SET STATUS= ? WHERE REQUEST_ID= ?";
			  try {
				  pstmt=conn.prepareStatement(updateQuery);
				pstmt.setString(1,message);
				pstmt.setInt(2, requestId);
				pstmt.executeUpdate();
				conn.commit();
			} catch (SQLException e) {
				LOG.error("Error while updating table REQUEST_PROCESSOR in setCrawlStatus() process:"+e.getLocalizedMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(null, null, pstmt, conn);
				}else {
					closeJDBCObjects(null, null, pstmt, null);
				}
			}
		}else{
			LOG.error("Unable to get connection from DB: in setCrawlStatus() method:");
		}
	}

	//gets last 100 records from request_processor table
	 public  LinkedHashMap<String,LinkedList<String>> getStatus(int page) throws Exception {
		  LinkedHashMap<String,LinkedList<String>> statusMap = new LinkedHashMap<String,LinkedList<String>>();
		
				//String query = "SELECT * FROM (SELECT * FROM REQUEST_PROCESSOR ORDER BY REQUEST_ID DESC)  WHERE ROWNUM <= 100";
		  		  String query = "SELECT * FROM ( SELECT a.*, rownum r__  FROM (SELECT * FROM REQUEST_PROCESSOR  ORDER BY REQUEST_ID DESC ) a  WHERE rownum < (("+page+" * 100) + 1 )) WHERE r__ >= ((("+page+"-1) * 100) + 1)";
				statusMap = getShowStatusMap(statusMap,query);
				/*String query1 =getStausQuery();
				statusMap = getShowStatusMap(statusMap,query1);*/
				
		  	return statusMap;
	  }
 
		//returns the no of records in Request_processor table
		public int getNoOfRecords(){
			Connection conn = JDBCConnector.getConnection();
			int count = 0;
			if(conn != null){
				Statement stmt = null;
				 ResultSet rs = null; 
				try {
				stmt = conn.createStatement();
				String query = "SELECT COUNT(*) FROM REQUEST_PROCESSOR";
				rs= stmt.executeQuery(query);
				while(rs.next()){
					count = rs.getInt(1);
				}
				} catch (SQLException e) {
					LOG.error("Error while getting row count from REQUEST_PROCESSOR in getNoOfRecords() methods: ");
				}finally{
					closeJDBCObjects(rs, stmt, null, conn);
				}
			}
			return count;
		}
	 public  LinkedHashMap<String,LinkedList<String>> getShowStatusMap(LinkedHashMap<String,LinkedList<String>> stausMap, String query){

		  Connection conn = JDBCConnector.getConnection();
		  LinkedHashMap<String,LinkedList<String>> statusMap = stausMap;
		  DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		  DomainVO domainVO;
		 // List<String> messageList = new ArrayList<String>();
		  if(conn != null){
			    ResultSet rs = null; 
				Statement stmt = null;			
				
				//String query = "SELECT * FROM (SELECT * FROM REQUEST_PROCESSOR ORDER BY REQUEST_ID DESC)  WHERE ROWNUM <= 100";
				//"SELECT * FROM (SELECT * FROM REQUEST_PROCESSOR ORDER BY REQUEST_ID DESC)  WHERE ROWNUM <= 10 ORDER BY ROWNUM DESC";
				try {
					stmt = conn.createStatement();
					rs = stmt.executeQuery(query);
					
					while(rs.next()){	
						LinkedList<String> messageList = new LinkedList<String>();
						messageList.clear();
						
						if(rs.getInt("REQUEST_ID") != 0){
							messageList.add(String.valueOf(rs.getInt("REQUEST_ID")));
						}
						if(rs.getInt("IS_API") == 1 ){
							if(rs.getString("TAG") != null){
								messageList.add("API - "+rs.getString("TAG"));
							}else{
								messageList.add("API");
							}
						}else{
							messageList.add("HMUI");
						}
						if(rs.getInt("DOMAIN_ID") != 0){
							domainVO = domainCrud.readByPrimaryKey(rs.getInt("DOMAIN_ID"));
							messageList.add(domainVO.getDomainName());
						}else if(rs.getString("DOMAIN_LIST") != null){
							String domainNames = null;
							String domains =rs.getString("DOMAIN_LIST");
							String[] domainList = domains.split(",");
							for (String domain : domainList) {
								domainVO = domainCrud.readByPrimaryKey(Integer.parseInt(domain));
								if(domainNames == null){
									domainNames = domainVO.getDomainName();
								}else{
									domainNames = domainNames+","+domainVO.getDomainName();
								}
							}
							messageList.add(domainNames);
						}else{
							messageList.add("-");
						}
						
						
						messageList.add(rs.getString("ACTION"));
						messageList.add(rs.getString("STATUS"));
						if(rs.getString("REQUEST_STARTED") == null){
							messageList.add("NOT STARTED");
							
						}else if(rs.getString("REQUEST_STARTED") != null){
							messageList.add(rs.getString("REQUEST_STARTED"));
						}
						if(rs.getString("REQUEST_COMPLETED") != null){
							messageList.add(rs.getString("REQUEST_COMPLETED"));
						}else{
							messageList.add(" ");
						}
						statusMap.put(String.valueOf(rs.getInt("REQUEST_ID")),messageList);
					}
				} catch (Exception e) {
					LOG.error(e.getLocalizedMessage());
				}finally{
					closeJDBCObjects(rs, stmt, null, conn);
				}
			}
		  	return statusMap;
	  
		 
	 }
	 
	 
	 public String getStausQuery(){
		 String query = "SELECT * FROM REQUEST_PROCESSOR WHERE STATUS IN ('PROCESSING','HTMLIZATION DOMAIN IN PROGRESS'," +
					"'CRAWLING DOMAIN COMPLETED','GENERATING HTML PATHS STARTED','GENERATING HTML PATHS COMPLETED','TRANSFORMING HTML FILES  STARTED'," +
					"'TRANSFORMING HTML FILES  COMPLETED','Successfully completed the autocrawl process.html files generated successfully','No transformations added.Please add the transformations'," +
					"'HTML Paths does not exist for segments. Please add the HTML Paths for segments','Crawl Failed,Unable to delete the crawldb Directory'," +
					"'No Domain available to crawl.Please add the domain','REFRESHING DOMAIN STARTED','Transformations Failed','Successfully completed the Refresh HTML files generated & saved successfully'," +
					"'HTMLIZATION SEGMENTS STARTED','GENERATING HTML PATHS STARTED','GENERATING HTML PATHS COMPLETED','TRANSFORMING HTML FILES  STARTED','TRANSFORMING HTML FILES  COMPLETED'," +
					"'Successfully completed the crawling selected segments. html files generated & saved successfully','No transformations added.Please add the transformations'," +
					"'HTML Paths does not exist for segments. Please add the HTML Paths for segments','HTMLIZATION Failed','Unable to delete the crawldb Directory','Please select the segments you want to crawl'," +
					"'REFRESHING SEGMENTS STARTED','HTML Path does not exist for this Segment.Please edit the Segment add the HTML Path','GENERATING HTML PATHS COMPLETED'," +
					"'Refreshing  selected segments completed','Please select the segments you want to crawl','REFRESHING URL STARTED',"+
					"'REFRESHING SELECTED URLS STARTED','REFRESHING SELECTED URLS COMPLETED Successfully','DELETE URL HTML PROCESS STARTED','DELETE SELECTED URL HTML PROCESS STARTED','DELETE SELECTED SEGMENT HTML PROCESS STARTED',"+
					"'DELETE DOMAIN HTML PROCESS STARTED','DELETE FILES  PROCESS STARTED','PURGE URL PROCESS STARTED','LOCK ISSUED')";
		 return query;
		 
	 }
	 
	 public String getDomainName(int domainId) {
			Connection conn = JDBCConnector.getConnection();
			String domainName = null;
			if (conn == null) {
				LOG.error("Connection not found in getDomainName method:");
				return domainName;
			}
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = " SELECT DOMAIN_NAME FROM DOMAIN WHERE DOMAIN_ID="+ "'" + domainId + "'";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						domainName = rs.getString("DOMAIN_NAME");
					}
				}
			} catch (SQLException e) {
				LOG.error("Error while fetching row from  DOMAIN table in getDomainName() method block:" + e.getMessage());
			} finally {
				closeJDBCObjects(rs, stmt, null, conn);
			}
			return domainName;
		}
	 
	 public int getDomainId(String domainName) {
			Connection conn = JDBCConnector.getConnection();
			int domainId = 0;
			if (conn == null) {
				LOG.info("Connection not found. Could not create row in SEGMENT_MASTER");
				return 0;
			}
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = " SELECT DOMAIN_ID FROM DOMAIN WHERE DOMAIN_NAME="+ "'" + domainName + "'";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						domainId = rs.getInt("DOMAIN_ID");
					}
				}
			} catch (SQLException e) {
				LOG.info("Error while fetching row in DOMAIN" + e);
			} finally {
				closeJDBCObjects(rs, stmt, null, conn);
			}
			return domainId;
		}
 
	 public List<String> getSegments(String segments){
		return getSegments(segments, 0);
	 }
	 
	 public List<String> getSegments(String segments, int domainId){
	   
	   String[] segmentNames = segments.split(",");
	  
	   List<String> segmentIds = new ArrayList<String>();
	    Connection conn = JDBCConnector.getConnection();
	    if(conn != null){
	    	ResultSet resultSet = null;
	    	Statement stmt =null;
	    	try {
				stmt = conn.createStatement();
				for (String segmentName : segmentNames) {
				String query = "SELECT SEGMENT_ID FROM SEGMENT_MASTER WHERE SEGMENT_NAME = '"+ segmentName+"'";
				if(domainId != 0) {
					query = query + " AND DOMAIN_ID ="+domainId;
				}
				boolean success = stmt.execute(query);
				if(success){
					resultSet = stmt.getResultSet();
					while(resultSet.next()){
						segmentIds.add(String.valueOf(resultSet.getInt("SEGMENT_ID")));
					}
				  }
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally {
				closeJDBCObjects(resultSet, stmt, null, conn);
			}
	    }
	return segmentIds;
   }
	
   		public boolean checkRequestProcessStatus(){ 
   			boolean actionStatus = false;
   			Statement statusStmt = null;
   			ResultSet rs = null;
			Connection conn = JDBCConnector.getConnection();
			try {
				statusStmt = conn.createStatement();
				
				String query = "SELECT * FROM REQUEST_PROCESSOR WHERE STATUS IN ('PENDING','PROCESSING','HTMLIZATION DOMAIN IN PROGRESS'," +
								"'CRAWLING DOMAIN COMPLETED','GENERATING HTML PATHS STARTED','GENERATING HTML PATHS COMPLETED','TRANSFORMING HTML FILES  STARTED'," +
								"'TRANSFORMING HTML FILES  COMPLETED','Successfully completed the autocrawl process.html files generated successfully','No transformations added.Please add the transformations'," +
								"'HTML Paths does not exist for segments. Please add the HTML Paths for segments','Crawl Failed,Unable to delete the crawldb Directory'," +
								"'No Domain available to crawl.Please add the domain','REFRESHING DOMAIN STARTED','Transformations Failed','Successfully completed the Refresh HTML files generated & saved successfully'," +
								"'HTMLIZATION SEGMENTS STARTED','GENERATING HTML PATHS STARTED','GENERATING HTML PATHS COMPLETED','TRANSFORMING HTML FILES  STARTED','TRANSFORMING HTML FILES  COMPLETED'," +
								"'Successfully completed the crawling selected segments. html files generated & saved successfully','No transformations added.Please add the transformations'," +
								"'HTML Paths does not exist for segments. Please add the HTML Paths for segments','HTMLIZATION Failed','Unable to delete the crawldb Directory','Please select the segments you want to crawl'," +
								"'REFRESHING SEGMENTS STARTED','HTML Path does not exist for this Segment.Please edit the Segment add the HTML Path','GENERATING HTML PATHS COMPLETED'," +
								"'Refreshing  selected segments completed','Please select the segments you want to crawl','REFRESHING URL STARTED',"+
								"'REFRESHING SELECTED URLS STARTED','REFRESHING SELECTED URLS COMPLETED Successfully','DELETE URL HTML PROCESS STARTED','DELETE SELECTED URL HTML PROCESS STARTED','DELETE SELECTED SEGMENT HTML PROCESS STARTED',"+
								"'DELETE DOMAIN HTML PROCESS STARTED','DELETE FILES  PROCESS STARTED','PURGE URL PROCESS STARTED','LOCK ISSUED')";
				boolean execute = statusStmt.execute(query);
				if(execute){
					rs = statusStmt.getResultSet();
					while(rs.next()){
						actionStatus = true;
					}
				}
			} catch (SQLException e) {
				LOG.error("Error in checkRequestProcessStatus method:"+e.getMessage());
			}finally{
				closeJDBCObjects(rs, statusStmt, null, conn);
			}
			return actionStatus;
			
   		}
	//Checking CRAWL_STATUS_MASTER table any status in progress or not
	/*	public boolean checkCrawlStatus(){
			boolean actionStatus = false;
			Statement crawlStmt = null;
			Statement statusStmt = null;
			ResultSet crawlStatus = null;
			ResultSet finalRS = null;
			String statusQuery = null;
			Connection conn = JDBCConnector.getConnection();
			try {
				int crawlId = getCrawlId(conn);
				crawlStmt = conn.createStatement();
				String crawlQuery = "SELECT * FROM CRAWL_MASTER WHERE PROGRESS=1";
				crawlStatus = crawlStmt.executeQuery(crawlQuery);
				if(crawlStatus.next()){
					statusQuery = "SELECT * FROM CRAWL_MASTER WHERE CRAWL_STATUS IN ('HTMLIZATION DOMAIN COMPLETED','HTMLIZATION SEGMENTS COMPLETED','REFRESHING DOMAIN COMPLETED','REFRESHING SEGMENTS COMPLETED','REFRESHING URL COMPLETED','REFRESHING SELECTED URLS COMPLETED','DELETE SELECTED SEGMENT HTML PROCESS COMPLETED','DELETE DOMAIN HTML PROCESS COMPLETED','DELETE URL HTML PROCESS COMPLETED','DELETE FILES  PROCESS COMPLETED') AND PROGRESS=1";
					statusStmt= conn.createStatement();
					finalRS = statusStmt.executeQuery(statusQuery);
					if(finalRS.next()){
						actionStatus = true;
					}else{
						actionStatus = false;
					}
				} else{
					statusQuery = "SELECT * FROM CRAWL_MASTER WHERE CRAWL_STATUS IN ('HTMLIZATION DOMAIN COMPLETED','HTMLIZATION SEGMENTS COMPLETED','REFRESHING DOMAIN COMPLETED','REFRESHING SEGMENTS COMPLETED','REFRESHING URL COMPLETED','REFRESHING SELECTED URLS COMPLETED','DELETE SELECTED SEGMENT HTML PROCESS COMPLETED','DELETE DOMAIN HTML PROCESS COMPLETED','DELETE URL HTML PROCESS COMPLETED','DELETE FILES  PROCESS COMPLETED') AND CRAWL_ID="+crawlId;
					statusStmt= conn.createStatement();
					finalRS = statusStmt.executeQuery(statusQuery);
					if(finalRS.next()){
						actionStatus = true;
					}else{
						actionStatus = false;
					}
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally{
				if (crawlStmt != null) {
					try {
						finalRS.close();
						statusStmt.close();
						crawlStatus.close();
						crawlStmt.close();
						conn.close();
					} catch (SQLException e) {
						LOG.error("Error while closing connection" + e.getMessage());
					}
				}
			}
			return actionStatus;
		}
	
*/	public int createTrackerId() {

		Connection conn = JDBCConnector.getConnection();
		int seqNum = 0;
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			java.sql.PreparedStatement pstmt = null;
			try {
				stmt = conn.createStatement();
				stmt.execute("SELECT STATUS_TRACKER_SEQ.NEXTVAL FROM DUAL");
				rs = stmt.getResultSet();
				rs.next();
				seqNum = rs.getInt("NEXTVAL");
				String query = "INSERT INTO STATUS_TRACKER (TRACKER_ID) VALUES(?)";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, seqNum);
				pstmt.execute();
				conn.commit();
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					LOG.error("Error while closing connection in createTrackerId method:" + e.getMessage());
				}
			} finally {
				closeJDBCObjects(rs, stmt, pstmt, conn);
			}
		}

		return seqNum;

	}

	public void setTrackerStatus(Connection conn, int trackerId, int crawlId, String message) {
		boolean connCreated = false;
		if (conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}

		if (message != null && message.length() > 255) {
			message.substring(0, 254);
		}
		if (conn != null) {
			PreparedStatement pstmt = null;
			String updateQuery = "UPDATE STATUS_TRACKER SET HTMLIZE_REFRESH_STATUS= ?,CRAWL_ID= ? WHERE TRACKER_ID= ?";
			try {
				pstmt = conn.prepareStatement(updateQuery);
				pstmt.setString(1, message);
				pstmt.setInt(2, crawlId);
				pstmt.setInt(3, trackerId);
				pstmt.executeUpdate();
				conn.commit();
			} catch (SQLException e) {
				LOG.error("Error while updating STATUS_TRACKER table in setTrackerStatus() method:"+e.getLocalizedMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(null, null, pstmt, conn);				
				}else {
					closeJDBCObjects(null, null, pstmt, null);
				}
			}
		}else{
			LOG.error("Unable to get connection from DB: in setTrackerStatus() process:");
		}
	}

	public String getTrackerStatus(int requestID) {
		String message = null;
		Connection conn = JDBCConnector.getConnection();
		if (conn != null) {
			Statement stmt = null;
			ResultSet resultSet = null;
			try {
				 stmt = conn.createStatement();
				//String query = "SELECT HTMLIZE_REFRESH_STATUS FROM STATUS_TRACKER WHERE TRACKER_ID = "+ trackerId;
				 String query = "SELECT STATUS FROM REQUEST_PROCESSOR WHERE REQUEST_ID = "+ requestID;	
				stmt.execute(query);
				resultSet = stmt.getResultSet();
				while (resultSet.next()) {
					message = resultSet.getString("STATUS");
				}
			} catch (SQLException e) {
				LOG.error("Error in getTrackerStatus method:"+e.getMessage());
			}finally{
				closeJDBCObjects(resultSet, stmt, null, conn);
									
			}
		}
		return message;

	}

	public int getTrackerId() {
		int trackerId = 0;
		Connection conn = JDBCConnector.getConnection();
		if (conn != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT TRACKER_ID FROM STATUS_TRACKER WHERE ROWID =(SELECT  MAX(ROWID) FROM STATUS_TRACKER)";
				stmt.execute(query);
				rs = stmt.getResultSet();
				while (rs.next()) {
					trackerId = rs.getInt("TRACKER_ID");
				}
			} catch (SQLException e) {
				LOG.error("Error in getTrackerId method:"+e.getMessage());
			}finally{
				closeJDBCObjects(rs, stmt, null, conn);
			}
		}
		return trackerId;

	}

	public int getSegmentId(Connection conn,int crawlId,String url) {
		boolean connCreated = false;
		int segmentId = 0;
		if (conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		
		if(conn != null){
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = conn.createStatement();
				if(url != null && crawlId != 0){
					String query = "select SEGMENT_ID from URL_HTML_LOC where crawl_id in (select crawl_id from CRAWL_MASTER where live=1) and url ='"+url+"'";
					 rs = stmt.executeQuery(query);
						while(rs.next()){
							segmentId = rs.getInt("SEGMENT_ID");
						}
				}else{
					LOG.error("URL or Crawl Id is Null in getSegmentId() method: ");
				}
				
			} catch (SQLException e) {
				LOG.error("Error in getSegmentId method:"+e.getMessage());
			} finally {
				if(connCreated) {
					closeJDBCObjects(rs, stmt, null, conn);				
				}else {
					closeJDBCObjects(rs, stmt, null, null);
				}
					
			}
		}
		return segmentId;		
	}
	
	
	//getting domain id based on url
	
	public int getUrlDomainId(String url){
		Connection conn = JDBCConnector.getConnection();
		int domainId = 0;
		if(conn != null){
			Statement stmt = null;
			ResultSet rs = null;
			try {
					if(url != null && !url.isEmpty()){
						String query = "select SEGMENT_ID from URL_HTML_LOC where crawl_id in (select crawl_id from CRAWL_MASTER where live=1) and url ='"+url+"'";
						rs = stmt.executeQuery(query);
						if(rs.next()){
							domainId=rs.getInt("DOMAIN_ID");
						}
					}else{
						LOG.error("Url is null in getUrlDomainId() method: ");
					}
			} catch (SQLException e) {
				LOG.error("Error in getSegmentId method:"+e.getMessage());
			} finally {
				closeJDBCObjects(rs, stmt, null, conn);
					
			}
		}else{
			LOG.error("Unable to get the connection from DB: in getUrlDomainId() method: ");
		}
		return domainId;		
	}
	
	//Delete only files from given input folder path
		public static void deleteFiles(File folder) throws IOException {
		    File[] files = folder.listFiles();
		    for(File file: files){
		        if(file.isFile()){
		            file.delete();
		        }else if(file.isDirectory()) {
		            deleteFiles(file);
		        }
		    }
		}
		
		//Update DOMAIN table if any event has status 1
		/*public void updateSegmentEventStatus(){
			Statement segStmt = null;
			Statement updateStmt = null;
			ResultSet segRS = null;
			boolean updateRS = false;
			String selectQuery = null;
			String updateQuery = null;
			Connection conn = JDBCConnector.getConnection();
			try {
				segStmt = conn.createStatement();
				selectQuery = "SELECT * FROM SEGMENT_STATUS_MASTER WHERE STATUS=1";
				segRS = segStmt.executeQuery(selectQuery);
				if(segRS.next()){
					updateQuery = "UPDATE  SEGMENT_STATUS_MASTER SET STATUS=0";
					updateStmt= conn.createStatement();
					updateRS = updateStmt.execute(updateQuery);
					conn.commit();
				} 
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally{
				if (segStmt != null) {
					try {
						segRS.close();
						segStmt.close();
						conn.close();
					} catch (SQLException e) {
						LOG.error("Error while closing connection" + e.getMessage());
					}
				}
			}
		}*/
		
		
		
		/**
		 * this method adds timestamp in URL_HTML_LOC table to each url
		 * @param urlhtmlloc
		 * @param crawlId
		 */
		public void deleteTimeStamptoURL(String urlLoc,int crawlId){
			String date = new Date().toString();
			try {
				Connection conn = JDBCConnector.getConnection();
				if(urlLoc == null){
					LOG.error("URL_LOC is null in deleteTimeStamptoURL method:");
					return;
				}
				if(crawlId == 0){
					LOG.error("Crawl id is null in deleteTimeStamptoURL method:");
				}
				if(conn != null){
				Statement stmt = null;
				try {
					stmt= conn.createStatement();
					String query = "UPDATE URL_HTML_LOC SET LAST_FETCH_TIME= '"+ date+"', HTML_FILE_STATUS='1', IS_HTMLIZED= '0' WHERE URL_LOC= '"+urlLoc+"' and  CRAWL_ID= "+crawlId;
					stmt.execute(query);
					conn.commit();
				} catch (SQLException e) {
					LOG.error("Error while executing update LAST_FETCH_TIME in URL_HTML_LOC table :" + e.getMessage());
				}finally {
					closeJDBCObjects(null, stmt, null, conn);
				}
			} else{
				LOG.error("Connection null in addTimeStamptoURL method:");
			}
		} catch(Exception e){
			LOG.error("Error While getting connection in deleteTimeStamptoURL method:"+e.getMessage());
		}
	}
		
	
		/**
		 * this method adds timestamp in PURGE_MASTER table to each url
		 * @param urlhtmlloc
		 * @param crawlId
		 */
		public static void purgeAddTimeStamptoURL(String purgeUrl){
			Connection conn = JDBCConnector.getConnection();
			if (conn != null) {
				java.sql.PreparedStatement pstmt = null;
				try {
					String query = "INSERT INTO PURGE_MASTER (URL,PURGE_RESPONSE_CODE,START_TIME,END_TIME) VALUES(?,?,?,?)";
					pstmt=conn.prepareStatement(query);
					pstmt.setString(1, purgeUrl);
					pstmt.setString(2, null);
					pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					pstmt.setTimestamp(4, null);
					pstmt.execute();
					conn.commit();
				} catch (SQLException e) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						LOG.error("Error while inserting purge request into  PURGE_MASTER table in purgeAddTimeStamptoURL() process: " + e.getMessage());
					}
				} finally {
						try {
							if (pstmt != null) {
								pstmt.close();
							}
							if (conn != null) {
								conn.close();
							}
						} catch (SQLException e) {
							LOG.error("Error while closing connection in purgeAddTimeStamptoURL() process: " + e.getMessage());
						}
					}
			}
	}
		
	
		/**
		 * this method adds timestamp in PURGE_MASTER table to each url
		 * @param urlhtmlloc
		 * @param crawlId
		 */
		public static void purgeUpdateTimeStamptoURL(String purgeUrl, String purgeCode){
			Connection conn = JDBCConnector.getConnection();
			if (conn != null) {
				java.sql.PreparedStatement pstmt = null;
				try {
					String query = "UPDATE PURGE_MASTER SET  PURGE_RESPONSE_CODE=?, END_TIME=? WHERE URL=? AND PURGE_RESPONSE_CODE IS NULL";
					pstmt=conn.prepareStatement(query);
					pstmt.setString(1, purgeCode);
					pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
					pstmt.setString(3, purgeUrl);
					pstmt.executeUpdate();
					conn.commit();
				} catch (SQLException e) {
					LOG.error("Error while updating record in PURGE_MASTER table in purgeUpdateTimeStamptoURL() process: " + e.getMessage());
					try {
						conn.rollback();
					} catch (SQLException e1) {
						LOG.error("Error while updating record in PURGE_MASTER table in purgeUpdateTimeStamptoURL() process: " + e1.getMessage());
					}
				} finally {
						try {
							if (pstmt != null) {
								pstmt.close();
							}if (conn != null) {
								conn.close();
							}
						} catch (SQLException e) {
							LOG.error("Error while closing connection in purgeUpdateTimeStamptoURL() process:" + e.getMessage());
						}
				}
			}
		}
		
		public  static LinkedHashMap<String,LinkedList<String>> getPurgeDetailStatus() throws Exception {
			  Connection conn = JDBCConnector.getConnection();
			  LinkedHashMap<String,LinkedList<String>> purgeDetailMap = new LinkedHashMap<String,LinkedList<String>>();
			  if(conn != null){
					Statement stmt = null;			
					stmt = conn.createStatement();
					String query = "SELECT * FROM  (SELECT * FROM PURGE_MASTER ORDER BY START_TIME DESC) WHERE ROWNUM <= 10";
					try {
						stmt.execute(query);
						ResultSet rs = stmt.getResultSet();
						int i=0;
						String startTime = null;
						//String endTime = null;
						while(rs.next()){	
							LinkedList<String> resultList = new LinkedList<String>();
							resultList.add(rs.getString("URL"));
							//resultList.add(rs.getString("PURGE_RESPONSE_CODE"));
							if(rs.getTimestamp("START_TIME")!=null){
								startTime=rs.getTimestamp("START_TIME").toString();
							}else{
								startTime=null;
							}
							
							/*if(rs.getTimestamp("END_TIME")!=null){
								endTime=rs.getTimestamp("END_TIME").toString();
							}else{
								endTime=null;
							}*/
							resultList.add(startTime);
							//resultList.add(endTime);
							i++;
							purgeDetailMap.put("URL_"+i,resultList);
						}
					} catch (SQLException e) {
						LOG.error("Error while fetching purge details from PURGE_MASTER in getPurgeDetailStatus() method: "+e.getLocalizedMessage());
					}finally{
						try {
							if(stmt != null){
								stmt.close();
							}if(conn != null){
								conn.close();
							}
						}catch (SQLException e) {
							LOG.error("Error while closing connection in getPurgeDetailStatus() process:" + e.getMessage());
						}
					}
				}else{
					purgeDetailMap = null;
				}
			  return purgeDetailMap;
		  }
		  
		  		/**
		 * this method deletes all the html files with in that folder
		 * @param file
		 * @param finalPath
		 * @param crawlId
		 */
	public void deleteAll(File file, String finalPath, int crawlId) {
		String url_loc = null;
		if(file.exists()){
			File[] listFiles = file.listFiles();
			if(listFiles != null) {
			for (File file1 : listFiles) {
				if (file1.isFile() && file1.getName().endsWith(".html")) {
					if (file1.delete()) {
						LOG.info(file1 + " is deleted!");
						String path = file1.getAbsolutePath();
						path = path.replace("\\", "/");
	
						if (path.contains(finalPath)) {
							url_loc = path.replace(finalPath, "");
							if (!url_loc.startsWith("/")) {
								url_loc = "/" + url_loc;
								if(crawlId != 0){
									deleteTimeStamptoURL(url_loc, crawlId);
								}else{
									LOG.error("Crawl id is null in deleteAll method:"+crawlId);
								}
							}
						}
					} else {
						LOG.error("File is Not available:" + file1);
					}
				}
	
			}
			}else {
				file.delete();
			}
		}
	}
	
	/**
	 * deletes all the index.html files with in that folder and in all the sub folders
	 * @param file
	 * @param finalPath
	 * @param crawlId
	 */

	public void deleteRecursive(File file, String finalPath, int crawlId) {
		String url_loc = null;
		String filePath = null;
		if(file.exists()){
			File[] listFiles = file.listFiles();
			for (File file1 : listFiles) {
				if (file1.isFile()
						&& file1.getName().equalsIgnoreCase("index.html")) {
					if (file1.delete()) {
						LOG.info(file1 + " is deleted!");
						filePath = file1.getAbsolutePath();
						filePath = filePath.replace("\\", "/");
						if (filePath.contains(finalPath)) {
							url_loc = filePath.replace(finalPath, "");
							if (!url_loc.startsWith("/")) {
								url_loc = "/" + url_loc;
								if(crawlId != 0){
									deleteTimeStamptoURL(url_loc, crawlId);
								}else{
									LOG.error("Crawl id is null in deleteRecursive method: ");
								}
							}
	
						}
	
					} else {
						LOG.error("File is Not available:" + file1);
					}
				}
				if (file1.isDirectory()) {
					deleteRecursive(file1, finalPath, crawlId);
				}
	
			}
		}else{
			LOG.error("Specified file doesn't exist in deleteRecursive method: ");
		}
	}

	/**
	 * this method deletes all the html files with in that folder as well as in all sub folders
	 * @param file
	 * @param finalPath
	 * @param crawlId
	 */
	public void deleteAllRecursiveFiles(File file, String finalPath, int crawlId) {
		String url_loc = null;
		String filePath = null;
		if(file.exists()){
			File[] listFiles = file.listFiles();
			for (File file1 : listFiles) {
				if (file1.isFile() && file1.getName().endsWith(".html")) {
					if (file1.delete()) {
						LOG.info(file1 + " is deleted!");
						filePath = file1.getAbsolutePath();
						filePath = filePath.replace("\\", "/");
						if (filePath.contains(finalPath)) {
							url_loc = filePath.replace(finalPath, "");
							if (!url_loc.startsWith("/")) {
								url_loc = "/" + url_loc;
								if(crawlId != 0 ){
									deleteTimeStamptoURL(url_loc, crawlId);
								}else{
									LOG.error("Crawl id is null in deleteAllRecursiveFiles method: ");
								}
							}
	
						}
	
					} else {
						LOG.error("File is Not available:" + file1);
					}
				}
				if (file1.isDirectory()) {
					deleteAllRecursiveFiles(file1, finalPath, crawlId);
				}
			}
		}else{
			LOG.error("Specified doesn't exist in deleteAllRecursiveFiles method: ");
		}
	}
	
	/**
	 * 
	 */
	public void deleteIndexFile(File file, String finalPath, int crawlId){
		
		String url_loc = null;
		String filePath = null;
		if(file.exists()){
		File[] listFiles = file.listFiles();
		for (File file1 : listFiles) {
			if (file1.isFile() && file1.getName().equalsIgnoreCase("index.html")){
				if(file1.delete()){
					LOG.info(file1 + " is deleted!");
					filePath = file1.getAbsolutePath();
					filePath = filePath.replace("\\", "/");
					if (filePath.contains(finalPath)) {
						url_loc = filePath.replace(finalPath, "");
						if (!url_loc.startsWith("/")) {
							url_loc = "/" + url_loc;
							if(crawlId != 0){
								deleteTimeStamptoURL(url_loc, crawlId);
							}else{
								LOG.error("Crawl id is null in deleteIndexFile method: ");
							}
						}
					}
				}else {
					LOG.error("File is Not available:" + file1);
				}
			}
		}
		}else{
			LOG.error("Specified file doesn't exist in deleteIndexFile method: ");
		}
	}

	public void createMonitorPath(MonitorVO monitorVO){
		Connection conn = JDBCConnector.getConnection();
		int folderId = 0;
		int specificFolderId = 0;
		int actionFolderId =0;
		
		if(conn != null){
			PreparedStatement pstmt = null;
			PreparedStatement prepareStmt = null;
			Statement actionStmt = null;
			Statement stmt = null;
			
			try {
				
				String insertQuery = "INSERT INTO MONITOR_FOLDER_PATHS (MONITOR_FOLDER_ID,FOLDER_PATH,REGEX,MONITOR_ID) VALUES (?,?,?,?) ";
				pstmt = conn.prepareStatement(insertQuery);
				if(monitorVO.getStaticFolderPath() != null){
				for (String folderPath : monitorVO.getStaticFolderPath()) {
					if(folderPath != null && folderPath != ""){
						folderPath = folderPath.replaceAll("\\\\", "/");
					stmt = conn.createStatement();
					stmt.execute("SELECT MONITOR_FOLDER_PATHS_SEQUENCE.NEXTVAL FROM DUAL");
					ResultSet rs = stmt.getResultSet();
					if(rs.next()){
						folderId =rs.getInt("NEXTVAL");
					}
						
					pstmt.setInt(1, folderId);
					pstmt.setString(2,folderPath);
					pstmt.setString(3, monitorVO.getStaticregEx());
					pstmt.setInt(4, 1);
					
					pstmt.addBatch();
					}
				}
				}
				if(monitorVO.getSpecificFilePath() != null && monitorVO.getSpecificFilePath() != ""){
					monitorVO.setSpecificFilePath(monitorVO.getSpecificFilePath().replaceAll("\\\\", "/"));
					stmt = conn.createStatement();
					stmt.execute("SELECT MONITOR_FOLDER_PATHS_SEQUENCE.NEXTVAL FROM DUAL");
					ResultSet rs = stmt.getResultSet();
					if(rs.next()){
						specificFolderId =rs.getInt("NEXTVAL");
					}
					pstmt.setInt(1, specificFolderId);
					pstmt.setString(2, monitorVO.getSpecificFilePath());
					pstmt.setString(3, monitorVO.getSpecificFileregEx());
					pstmt.setInt(4, 2);
					pstmt.addBatch();
				}
				if(monitorVO.getJspFolderPath() != null){
					
					stmt = conn.createStatement();
					String jspRegex = ".*\\.jsp";
					for (String folderPath : monitorVO.getJspFolderPath()) {
						if(folderPath != "" && folderPath != null){
							folderPath = folderPath.replaceAll("\\\\", "/");
						stmt.execute("SELECT MONITOR_FOLDER_PATHS_SEQUENCE.NEXTVAL FROM DUAL");
						ResultSet rs = stmt.getResultSet();
						if(rs.next()){
							folderId =rs.getInt("NEXTVAL");
						}
						pstmt.setInt(1, folderId);
						pstmt.setString(2, folderPath);
						pstmt.setString(3,jspRegex);
						pstmt.setInt(4, 3);
						pstmt.addBatch();
					}
					}
					
				}
				String insertQuery1 ="INSERT INTO MONITOR_FOLDER_PATH_ACTIONS (MONITOR_ACTION_ID,SEGMENT,URL,MONITOR_FOLDER_ID) VALUES (?,?,?,?)";
				prepareStmt = conn.prepareStatement(insertQuery1);
				if(monitorVO.getRefreshSegmentList() != null && monitorVO.getRefreshUrls() != null){
				if(monitorVO.getRefreshSegmentList().size() >= monitorVO.getRefreshUrls().size()){
					if(specificFolderId != 0){
					Iterator segments = monitorVO.getRefreshSegmentList().iterator();
					Iterator urls = monitorVO.getRefreshUrls().iterator();
					
					while(segments.hasNext()){
						String segment = (String) segments.next();
						String url = null;
						if(urls.hasNext()){
						url = (String) urls.next();
						}
						actionStmt = conn.createStatement();
						actionStmt.execute("SELECT MONITOR_ACTIONS_SEQUENCE.NEXTVAL FROM DUAL");
						ResultSet rs = actionStmt.getResultSet();
						if(rs.next()){
							actionFolderId =rs.getInt("NEXTVAL");
						}
						prepareStmt.setInt(1, actionFolderId);
						prepareStmt.setString(2, segment);
						prepareStmt.setString(3, url);
						prepareStmt.setInt(4,specificFolderId);
						prepareStmt.addBatch();
					}
				}else{
					Iterator segments = monitorVO.getRefreshSegmentList().iterator();
					Iterator urls = monitorVO.getRefreshUrls().iterator();
					
					while(urls.hasNext()){
						String segment = null;
						if(segments.hasNext()){
						segment = (String) segments.next();
						}
						String url = (String) urls.next();
						actionStmt = conn.createStatement();
						actionStmt.execute("SELECT MONITOR_ACTIONS_SEQUENCE.NEXTVAL FROM DUAL");
						ResultSet rs = actionStmt.getResultSet();
						if(rs.next()){
							actionFolderId =rs.getInt("NEXTVAL");
						}
						prepareStmt.setInt(1, actionFolderId);
						prepareStmt.setString(2, segment);
						prepareStmt.setString(3, url);
						prepareStmt.setInt(4,specificFolderId);
						prepareStmt.addBatch();
					}
					
				}
				}
				}else if(monitorVO.getRefreshSegmentList() != null){
					if(specificFolderId != 0){
						Iterator segments = monitorVO.getRefreshSegmentList().iterator();
						while(segments.hasNext()){
							String segment = (String) segments.next();
							
							actionStmt = conn.createStatement();
							actionStmt.execute("SELECT MONITOR_ACTIONS_SEQUENCE.NEXTVAL FROM DUAL");
							ResultSet rs = actionStmt.getResultSet();
							if(rs.next()){
								actionFolderId =rs.getInt("NEXTVAL");
							}
							prepareStmt.setInt(1, actionFolderId);
							prepareStmt.setString(2, segment);
							prepareStmt.setString(3, null);
							prepareStmt.setInt(4,specificFolderId);
							prepareStmt.addBatch();
						}
						
					}
				}
				else if(monitorVO.getRefreshUrls() != null){
					if(specificFolderId != 0){
						Iterator urls = monitorVO.getRefreshUrls().iterator();
						
						while(urls.hasNext()){
							
							String url = (String) urls.next();
							actionStmt = conn.createStatement();
							actionStmt.execute("SELECT MONITOR_ACTIONS_SEQUENCE.NEXTVAL FROM DUAL");
							ResultSet rs = actionStmt.getResultSet();
							if(rs.next()){
								actionFolderId =rs.getInt("NEXTVAL");
							}
							prepareStmt.setInt(1, actionFolderId);
							prepareStmt.setString(2, null);
							prepareStmt.setString(3, url);
							prepareStmt.setInt(4,specificFolderId);
							prepareStmt.addBatch();
						}
						
					}
				}
				pstmt.executeBatch();
				if(specificFolderId !=0){
				prepareStmt.executeBatch();
				}
				
				String query = "UPDATE MONITOR_FOLDER_PATHS SET REGEX = '"+monitorVO.getStaticregEx()+"' WHERE MONITOR_ID = 1";
				stmt.execute(query);
				String updatedocrootQuery = "UPDATE MONITOR_MASTER SET VALUE = '"+monitorVO.getDocRoot()+"' WHERE TYPE = 'DOC_ROOT'";
				stmt.execute(updatedocrootQuery);
				conn.commit();
				
					
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					LOG.error("error while roll back in createMonitorPath method:");
				}
			}finally{
				if(prepareStmt != null){
					try {
						prepareStmt.close();
					} catch (SQLException e) {
						LOG.error("Error while closing prepareStmt in createMonitorPath method:"+ e.getMessage());
					}
				}
				if(actionStmt != null){
					try {
						actionStmt.close();
					} catch (SQLException e) {
						LOG.error("Error while closing actionStmt in createMonitorPath method:"+ e.getMessage());
					}
				}
				if(stmt != null){
					try {
						stmt.close();
					} catch (SQLException e) {
						LOG.error("Error while closing stmt in createMonitorPath method:"+ e.getMessage());
					}
				}
				
					try {
						if(pstmt != null){
							pstmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing the statement in createMonitorPath method:"+e.getMessage());
					}
				}
		}
	}
	
	
	
	public List<String> getStaticFolderPaths(){
		Connection conn = JDBCConnector.getConnection();
		List<String> folderPaths = new ArrayList<String>();
		if(conn != null){
			Statement stmt = null;
			String query = "select * from MONITOR_FOLDER_PATHS where MONITOR_ID = 1";
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()){
					folderPaths.add(rs.getString("FOLDER_PATH"));
				}
				
			} catch (SQLException e) {
				LOG.error("Error in getStaticFolderPaths method:"+e.getMessage());
			}finally{
				
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in getStaticFolderPaths method:"+e.getMessage());
					}
			}
		}
		return folderPaths;
	}
	
	public List<String> getStaticRegex(){
		Connection conn = JDBCConnector.getConnection();
		List<String> regexList = new ArrayList<String>();
		String regex = null;
		if(conn != null){
			Statement stmt = null;
			String query = "select * from MONITOR_FOLDER_PATHS where MONITOR_ID = 1";
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()){
					regex=rs.getString("REGEX");
				}
				if(regex != null){
					regex = regex.replace(".*\\.(","");
					regex = regex.substring(0, regex.indexOf(")"));
					String[] regexs = regex.split("\\|");
					for (String expression : regexs) {
						if(!expression.isEmpty()){
							regexList.add(expression);
						}
					}
				}	
			} catch (SQLException e) {
				LOG.error("Error in getStaticRegex method:"+e.getMessage());
			}finally{
				
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in getStaticRegex method:"+e.getMessage());
					}
			}
		}
		return regexList;
	}
	
	
	public void deleteStaticRegex(String regex){
		Connection conn = JDBCConnector.getConnection();
		String currentRegex = null;
		String staticextensions = null;
		if(conn != null){
			Statement  stmt = null;
			try {
				stmt = conn.createStatement();
				String query ="SELECT REGEX FROM MONITOR_FOLDER_PATHS WHERE MONITOR_ID= 1";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()){
					if(rs.getString("REGEX") != null){
						currentRegex = rs.getString("REGEX");
					}
				}
				/*currentRegex = currentRegex.replace(".*\\","");
				String[] regexs = currentRegex.split("\\|");
				for (String regex_db : regexs) {
					if(!regex_db.equalsIgnoreCase(regex)){
					 if(staticextensions == null){
						 staticextensions=".*\\"+regex_db;
						 			 
					 }else{
						  staticextensions = staticextensions +"|.*\\"+regex_db;
						 
					 }
				}
				}*/
				
				currentRegex = currentRegex.replace(".*\\.(","");
				currentRegex = currentRegex.substring(0, currentRegex.indexOf(")"));
				String[] regexs = currentRegex.split("\\|");
				for (String regex_db : regexs) {
					if(!regex_db.equalsIgnoreCase(regex)){
						 if(staticextensions == null){
							 staticextensions=".*\\.("+regex_db;
							 			 
						 }else{
							  staticextensions = staticextensions +"|"+regex_db;
							 
						 }
					}
				}
				if(staticextensions!=null){
					staticextensions = staticextensions+")$";
				}
					String updateQuery = "UPDATE MONITOR_FOLDER_PATHS SET REGEX='"+staticextensions+"' WHERE MONITOR_ID= 1";
					stmt.execute(updateQuery);
					
					conn.commit();
					
				} catch (SQLException e) {
				LOG.error("Error in deleteStaticRegex method"+e.getMessage());
			}finally{
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in deleteStaticRegex method:"+e.getMessage());
					}
			}
		}
		
	}
	
	public void deleteStaticFolderPath(String folderPath){
		Connection conn = JDBCConnector.getConnection();
		
		if(conn != null){
			Statement stmt = null;
			try {
				stmt= conn.createStatement();
				String query = "DELETE FROM MONITOR_FOLDER_PATHS WHERE MONITOR_ID=1 and FOLDER_PATH = '"+folderPath+"'";
				stmt.execute(query);
				conn.commit();
			} catch (SQLException e) {
				LOG.error("Error in deleteStaticFolderPath method"+e.getMessage());
			}finally{
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing Connection in deleteStaticFolderPath method: "+ e.getMessage());
					}
			}
					
		}
		
	}
	
	public List<String> getJspPath(){
		Connection conn = JDBCConnector.getConnection();
		List<String> jspPath = new ArrayList<String>();
		
		if(conn != null){
			Statement stmt = null;
			try {
				stmt= conn.createStatement();
				String query = "SELECT FOLDER_PATH FROM MONITOR_FOLDER_PATHS WHERE MONITOR_ID = 3";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()){
					jspPath.add(rs.getString("FOLDER_PATH"));
				}
			} catch (SQLException e) {
				LOG.error("Error in getJspPath method:"+e.getMessage());
			}finally{
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing Connection in getJspPath method:"+ e.getMessage());
					}
				}
		}
		return jspPath;
		
	}
	
	public  void deleteJspPath(String jspPath){
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			Statement stmt = null;
			try {
				stmt= conn.createStatement();
				String query = "DELETE FROM MONITOR_FOLDER_PATHS WHERE MONITOR_ID=3 and FOLDER_PATH = '"+jspPath+"'";
				stmt.execute(query);
				conn.commit();
			} catch (SQLException e) {
				LOG.error("Error in deleteJspPath method:"+e.getMessage());
			}finally{
					try {
						if(stmt != null){
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing Connection  in deleteJspPath method:"+ e.getMessage());
					}
			}
		}
	}
	
	public Map getSpecificFileDetails(){
		Connection conn = JDBCConnector.getConnection();
		Map segmentUrlMap = new HashMap<>();
		LinkedList<List<String>> segmentUrlList = new LinkedList<List<String>>();
		if(conn != null){
			Statement stmt = null;
			PreparedStatement domainSegmentsPrep = null;
			String specificPathsSql = "SELECT MONITOR_FOLDER_PATHS.REGEX,MONITOR_FOLDER_PATHS.FOLDER_PATH,MONITOR_FOLDER_PATH_ACTIONS.SEGMENT,MONITOR_FOLDER_PATH_ACTIONS.URL from MONITOR_FOLDER_PATHS INNER JOIN MONITOR_FOLDER_PATH_ACTIONS " +
				    "ON MONITOR_FOLDER_PATHS.MONITOR_FOLDER_ID=MONITOR_FOLDER_PATH_ACTIONS.MONITOR_FOLDER_ID ORDER BY MONITOR_FOLDER_PATHS.REGEX, MONITOR_FOLDER_PATHS.FOLDER_PATH";
			String domainSegmentNamesSql = "SELECT D.DOMAIN_NAME,SM.SEGMENT_NAME from SEGMENT_MASTER SM, DOMAIN D WHERE SEGMENT_ID=? and D.DOMAIN_ID = SM.DOMAIN_ID";
			try {
				stmt = conn.createStatement();
				String regex = null;
				String folderPath = null;
				List<String> segmentList = new ArrayList<String>();
				List<String> urlList = new ArrayList<String>();
				ResultSet rs1 = stmt.executeQuery(specificPathsSql);
				ResultSet domainSegmentsRs = null;
				String regexKey = null;
				while(rs1.next()){
					
					
					/*String regextemp = rs.getString("REGEX");
					String folderPathtemp = rs.getString("FOLDER_PATH");
					
						
						
						if(!(regex == null || regextemp.equalsIgnoreCase(regex) || folderPath == null || folderPathtemp.equalsIgnoreCase(folderPath))){
							
							if(!segmentList.isEmpty() || !urlList.isEmpty()){
								segmentUrlList.add(segmentList);
								segmentUrlList.add(urlList);
								String regex_path = folderPath+","+regex;
								segmentUrlMap.put(regex_path, segmentUrlList);
							}
							
							segmentList = new ArrayList<String>();
							urlList = new ArrayList<String>();
							segmentUrlList = new LinkedList<List<String>>();
						}
						
						segmentList.add(getSegmentName(rs.getString("SEGMENT")));
						if(segmentList != null){
							segmentList.removeAll(Collections.singleton(null));
						}
						urlList.add(rs.getString("URL"));
						if(urlList != null){
							urlList.removeAll(Collections.singleton(null));
						}
					
						regex = regextemp;
						folderPath = folderPathtemp;*/
					
					regexKey = rs1.getString("FOLDER_PATH") + "," +rs1.getString("REGEX");
					//checking regex key equal or not with previous record regex
					if(regex == null || regexKey.equalsIgnoreCase(regex)) {
						
					}else {
						
						
						//if(!segmentList.isEmpty()){
							segmentUrlList.add(segmentList);
						//}
						//if(!urlList.isEmpty()) {
							segmentUrlList.add(urlList);
						//}
						if(!segmentUrlList.isEmpty()) {
							segmentUrlMap.put(regex, segmentUrlList);
						}
						segmentList = new ArrayList<String>();
						urlList = new ArrayList<String>();
						segmentUrlList = new LinkedList<List<String>>(); 
					}
					regex = regexKey;
					domainSegmentsPrep = conn.prepareStatement(domainSegmentNamesSql);
					if(rs1.getString("SEGMENT") != null){
					domainSegmentsPrep.setInt(1, Integer.parseInt(rs1.getString("SEGMENT")));
					domainSegmentsRs = domainSegmentsPrep.executeQuery();
					String domainSegmentNames = null;
					
					if(domainSegmentsRs.next()) {
						domainSegmentNames = domainSegmentsRs.getString("DOMAIN_NAME") + "." + domainSegmentsRs.getString("SEGMENT_NAME");
					}
					
					
					if (rs1.getString("SEGMENT") != null) {
						//segmentList.add(rs1.getString("SEGMENT"));
						if(domainSegmentNames != null) {
							segmentList.add(domainSegmentNames);
						}
					}
					}
					if (rs1.getString("URL") != null) {
						urlList.add(rs1.getString("URL"));
					}
				}
				
					if(!segmentList.isEmpty() || !urlList.isEmpty()){
						segmentUrlList.add(segmentList);
						segmentUrlList.add(urlList);
						segmentUrlMap.put(regex, segmentUrlList);
					}else{
						//String regex_path = folderPath+","+regex;
						segmentUrlMap.put(regex, segmentUrlList);
					}
									
			} catch (SQLException e) {
				LOG.error("Error in getSpecificFileDetails method:"+e.getMessage());
			}finally{
				
					try {
						if(stmt != null){
						stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing the connection in getSpecificFileDetails method:"+e.getMessage());
					}
			}
		}
		return segmentUrlMap;
	}
	  
	public void deleteSpecificPathEntry(String regex) {
		Connection conn = JDBCConnector.getConnection();
		int folder_id = 0;
		if (conn != null) {
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT MONITOR_FOLDER_ID FROM MONITOR_FOLDER_PATHS WHERE REGEX = '"
						+ regex + "'";
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					folder_id = rs.getInt("MONITOR_FOLDER_ID");
				}
				if (folder_id != 0) {
					String deleteQuery1 = "DELETE FROM MONITOR_FOLDER_PATH_ACTIONS WHERE MONITOR_FOLDER_ID ="
							+ folder_id;
					String deleteQuery2 = "DELETE FROM MONITOR_FOLDER_PATHS WHERE MONITOR_FOLDER_ID ="
							+ folder_id;
					stmt.execute(deleteQuery1);
					stmt.execute(deleteQuery2);
					conn.commit();
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			}finally{
				try {
					if(stmt != null){
					stmt.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while closing statement "+e.getMessage());
				}
				try {
					if(conn != null){
					conn.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while closing connection "+e.getMessage());
				}
			}
		}
	}
	  
  public String getSegmentName(String segmentId){
	  Connection  conn = JDBCConnector.getConnection();
	  String segmentName = null;
	  if(conn != null){
		  Statement stmt = null;
		  try {
			stmt = conn.createStatement();
			String query = "SELECT 	SEGMENT_NAME FROM SEGMENT_MASTER WHERE SEGMENT_ID = "+segmentId;
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				segmentName = rs.getString("SEGMENT_NAME");
			}
		} catch (SQLException e) {
			LOG.error("Error While Fetching Segment Name From Segment_Master" + e.getMessage());
		}finally{
			try {
				if(stmt != null){
				stmt.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing statement "+e.getMessage());
			}
			try {
				if(conn != null){
				conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing connection "+e.getMessage());
			}
		 }
	  }
	return segmentName;
  }
	  
public String getStaticRegEx(){
	Connection conn = JDBCConnector.getConnection();
	String staticRegex = null;
	if(conn != null){
		Statement  stmt = null;
		try {
			stmt = conn.createStatement();
			String query ="SELECT REGEX FROM MONITOR_FOLDER_PATHS WHERE MONITOR_ID= 1";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				if(rs.getString("REGEX") != null){
					staticRegex = rs.getString("REGEX");
				}
			}
		}catch(SQLException e){
			LOG.error("Error while getting RegEx "+e.getMessage());
		}finally{
			try {
				if(stmt != null){
				stmt.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing statement "+e.getMessage());
			}
			try {
				if(conn != null){
				conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing connection "+e.getMessage());
			}
		}
	}
	return staticRegex;
   }

public static String getDocRootPath(){
	Connection conn = JDBCConnector.getConnection();
	String docRootPath = null;
	if(conn != null){
		Statement  stmt = null;
		try {
			stmt = conn.createStatement();
			String query ="SELECT VALUE FROM MONITOR_MASTER WHERE TYPE='DOC_ROOT'";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				if(rs.getString("VALUE") != null){
					docRootPath = rs.getString("VALUE");
				}
			}
		}catch(SQLException e){
			LOG.error("Error while getting DOCROOT VALUE "+e.getMessage());
		}finally{
			try {
				if(stmt != null){
				stmt.close();
				}
				if(conn != null){
				conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing connection "+e.getMessage());
			}
		}
	}
	return docRootPath;
   }

	public String getSpecificURLHTMLLOC(String url, int crawlId) {
		String url_loc = null;
		Connection conn = JDBCConnector.getConnection();
		if (conn != null) {
		  Statement stmt = null;
		  ResultSet rs = null;
		 try{
			 stmt = conn.createStatement();
			 if(url != null || crawlId != 0){
				 String query = "SELECT URL_LOC FROM URL_HTML_LOC WHERE url= '"+url+"' and CRAWL_ID="+crawlId;
				 rs = stmt.executeQuery(query);
				 
				 if(rs.next()){
					 url_loc = rs.getString("URL_LOC");
					 
				 }
			 }else{
				 LOG.error("Url or Crawl id is null in getSpecificURLHTMLLOC() method: url is: "+url+" crawl id is: "+crawlId);
			 }
		} catch(SQLException e){
			 LOG.error("Error in getUrlHtmlLoc method:"+e.getMessage());
		}
		 finally {
			try {
				if (stmt != null) {
				stmt.close();
				rs.close();
				}
				if(conn != null){
					conn.close();
				}
			}catch (SQLException e) {
				LOG.error("Error while closing connection "+e.getMessage()); 
			}
			}
		}else{
			LOG.error("Unable to get the connection from DB: in getSpecificURLHTMLLOC() method: ");
		}
		return url_loc;
	}
	
	//get siteNames Map from purge-config properties file
	public  Map<String, String> getSiteNamesMap(){
	Properties prop = new Properties();
	InputStream input = null;
	input = getClass().getClassLoader().getResourceAsStream("purge-config.properties");
	
	// load a properties file
	try {
		prop.load(input);
	} catch (IOException e1) {
		LOG.error("While loading purge-config properties file in getSiteNamesMap() :"+e1.getMessage());
	}
	LOG.info(prop.getProperty("hostName"));
	String serializedMap = prop.getProperty("hostName");
	Map<String, String> finalsitemap = new HashMap<String, String>();
	JSONArray a;
	try {
		if(serializedMap != null && !serializedMap.isEmpty()){
			a = new JSONArray(serializedMap);
			for (int n = 0; n < a.length(); n++) {
				JSONObject object = a.getJSONObject(n);
				Map<String, String> sitemap = new HashMap<String, String>();
				ObjectMapper mapper = new ObjectMapper();
				try {
					sitemap = mapper.readValue(object.toString(),
							new TypeReference<HashMap<String, String>>() {
							});
					if(!sitemap.isEmpty()){
						for (Map.Entry<String, String> entry : sitemap.entrySet()) {
							finalsitemap.put(entry.getKey(), entry.getValue());
						}
					}else{
						LOG.error("sitemap is empty in getSiteNamesMap method: ");
					}
				} catch (IOException e) {
					LOG.error("Error in getSiteNamesMap method:"+e.getMessage());
				}
			}
		}else{
			LOG.error("HostName is Null in getSiteNamesMap() method: ");
		}
	}catch (JSONException e1) {
		LOG.error("While converting  JSON array to Map in getSiteNamesMap() :"+e1.getMessage());
	}
	return finalsitemap;
	}
	
	
	public List<String> readUrlsForSegment(int segmentId, int crawlId) {
		Connection conn = JDBCConnector.getConnection();
		List<String> urlList = new ArrayList<String>();
		if (conn == null) {
			LOG.error("Unable to get connection from DB: in readUrlsForSegment() process:");
			urlList = null;
			return urlList;
		}
		else{
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = " SELECT * from URL_HTML_LOC where default_location=1 and SEGMENT_ID="+segmentId+" and CRAWL_ID="+crawlId;
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					if(!rs.getString("URL").contains(" "))
						urlList.add(rs.getString("URL"));
				}
			} catch (SQLException e) {
				LOG.error("Error fetching details from URL_HTML_LOC table in  readUrlsForSegment() process: " + e.getMessage());
			} finally {
					try {
						if(stmt!=null){
							stmt.close();	
						}
						if(conn!=null){
							conn.close();	
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in readUrlsForSegment() process:" + e.getMessage());
					}
				}
			}
		return urlList;
	}
	
	public static Map<String,String> userRoles(){
		Map<String,String> userRoles = new LinkedHashMap<String,String>();
		Connection conn = JDBCConnector.getConnection();
		if (conn == null) {
			LOG.info("Connection not found in myAccount block:");
		}
		else{
			PreparedStatement  ps = null;
			ResultSet rs = null;
			try {
				String query = " select * from ROLE_MASTER";
				ps = conn.prepareStatement(query);
				rs = ps.executeQuery();
				while (rs.next()) {
					if(rs.getInt("ROLE_ID") != 0 && rs.getString("ROLE_NAME")!=null){
						userRoles.put(String.valueOf(rs.getInt("ROLE_ID")), rs.getString("ROLE_NAME"));
					}
				}
			} catch (Exception e) {
				LOG.info("Error while getting user roles in userRoles() method:" + e.getMessage());
			} finally {
					try {
						if (ps != null) {
							ps.close();
						}
						if (conn != null) {
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in userRoles() method:" + e);
					}
				}
		}
		return userRoles;
	}	
	
	//updating SEG_RULE_CHANGED,SEG_HTML_PATH_CHANGED value to 0 in DOMAIN table
	public void chageSegmentStatusMasterValues(int domainId){
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = "UPDATE DOMAIN SET SEG_RULE_CHANGED=0,SEG_HTML_PATH_CHANGED=0 WHERE DOMAIN_ID="+domainId;
				stmt.execute(query);
				conn.commit();
			} catch (SQLException e) {
				LOG.error("Error in creating statement in chageSegmentStatusMasterValues() method:"+e.getMessage());
			}finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while closing connection in chageSegmentStatusMasterValues() method:" + e);
				}
			}
		}else{
			LOG.error("Error while getting connection in chageSegmentPathStatusValue() method:");
		}
	}
	
	//updating SEG_HTML_PATH_CHANGED value to 0 in DOMAIN table
	public void chageSegmentHTMLPathStatusValue(int domainId){
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = "UPDATE DOMAIN SET SEG_HTML_PATH_CHANGED=0 WHERE DOMAIN_ID="+domainId;
				stmt.execute(query);
				conn.commit();
			} catch (SQLException e) {
				LOG.error("Error in creating statement in chageSegmentPathStatusValue() method:"+e.getMessage());
			}finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while closing connection in chageSegmentPathStatusValue() method:" + e.getMessage());
				}
			}
		}else{
			LOG.error("Error while getting connection in chageSegmentPathStatusValue() method:");
		}
	}
	
	public int getJspDomainId(String jspUrl){
		int domainId =0;
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			Statement stmt = null;
			try {
				String domainQuery = "select DOMAIN_ID from URL_HTML_LOC where crawl_id in (select crawl_id from CRAWL_MASTER where live=1) and url  like '%"+jspUrl+"%'";
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(domainQuery);
				if(rs.next()){
					domainId = rs.getInt("DOMAIN_ID");
				}
			} catch (SQLException e) {
				LOG.error("Error while getting domain id in getJspDomainId() method:"+e.getMessage());
			}finally{
				
					try {
						if(stmt != null){
						stmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in getJspDomainId() method:"+e.getMessage());
					}
			}
		}
		return domainId;
		
	}
	
	/**
	 * Close all the jdbc objects
	 * 
	 * @param rs
	 * @param stmt
	 * @param pStmt
	 * @param conn
	 */
	private void closeJDBCObjects(ResultSet rs, Statement stmt, PreparedStatement pStmt, Connection conn) {
		try {
			if(rs != null) {
				rs.close();
			}
			if(stmt != null) {
				stmt.close();
			}
			if(conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			LOG.info("Error while closing jdbc objects" + e);
		}
	}
	
	//Delete URL HTML File Process
			public void deleteOldHtmlFile(String url, int crawlId,int domainId){
				LOG.info("Old URL to be Delete:["+url+"]");
				DomainVO domainVo = new DomainVO();
				DomainMasterCRUD domainCrud = new DomainMasterCRUD();
				String domainPath=null;
				try{
					if(url != null && crawlId != 0 && domainId != 0){
						domainVo = domainCrud.readByPrimaryKey(domainId);
						if(domainVo == null) {
							return;
						}
						String urlLoc = getSpecificURLHTMLLOC(url, crawlId);
						domainPath = domainVo.getUrl();
						if(urlLoc != null && urlLoc.contains(domainPath)){
							urlLoc = urlLoc.replace(domainPath, "");
						
							File file = null;
							file = new File(domainVo.getFinal_content_directory()+urlLoc);
							if(file.exists()){
					    		if(file.delete()){
					    			//cUtil.deleteTimeStamptoURL(urlLoc,crawlId);
					    			LOG.info(file + " is deleted!");
					    		}else{
					    			LOG.error("File is Not available:"+file);
					    		}
							}
						}
					}else{
						LOG.error("Url or CrawlId or Domain Id is null in deleteOldHtmlFile() method: url is:"+url+" Crawl id is: "
								+crawlId+" Domain id is:"+domainId);
					}
				}catch(Exception e){
					LOG.error("Error while deleting old URL Html file:"+e.getMessage());
				}
			}
		
}
