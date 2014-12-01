package com.hm.crawl.data;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.nutch.tools.JDBCConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.vo.EditTransformationVO;
import com.hm.crawl.data.vo.HTMLPathVO;
import com.hm.crawl.data.vo.SegmentVO;
import com.hm.crawl.data.vo.TransformationVO;
import com.hm.util.NutchConstants;

/**
 * This class is CRUD operation for Segment Master Table.It handles operation
 * for creating,deleting,updating,splitting and merging segment.
 * 
*/
public class SegmentMasterCRUD {
	private final static Logger logger = LoggerFactory.getLogger(SegmentMasterCRUD.class);

	/**
	 * This method creates new Segment
	 * 
	 * @param segment
	 * @return
	 * @throws SQLException
	 */
	public boolean create(SegmentVO segment) throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		boolean segCreated = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in segment creation process:");
			return segCreated;
		}
		else{
			PreparedStatement stmt = null;
			PreparedStatement stmturlPath = null;
			Statement stmtNextVal = null;
			PreparedStatement transPath = null;
			ResultSet rs = null;
			ResultSet rs1 = null;
			ResultSet addTransResult = null;
			conn.setAutoCommit(false);
			try {
				stmtNextVal = conn.createStatement();
				stmtNextVal.execute("SELECT SEGMENT_MASTER_SEQUENCE.NEXTVAL FROM DUAL");
				rs = stmtNextVal.getResultSet();
				rs.next();
				int seqNum = rs.getInt("NEXTVAL");
				rs = null;
				int priority = 0;
				stmtNextVal.execute("SELECT max(priority) as PRIORITY from SEGMENT_MASTER where DOMAIN_ID=" + segment.getDomainId() );
				rs = stmtNextVal.getResultSet();
				while(rs.next()) {
					if(rs.getInt("PRIORITY") == 0){
					priority = 99999;
					}else if(rs.getInt("PRIORITY") == 99999){
						priority = 1;
						stmtNextVal.execute("SELECT max(priority) as PRIORITY FROM segment_master WHERE priority < (SELECT max(priority) FROM segment_master) and domain_id= "+segment.getDomainId());
						rs1 = stmtNextVal.getResultSet();
						while(rs1.next()){
							priority = rs1.getInt("PRIORITY") + 1;
						}
					}
				}
				String query = "INSERT INTO SEGMENT_MASTER (SEGMENT_ID,SEGMENT_NAME,URL_PATTERN_RULE,CRAWL,PRIORITY,DOMAIN_ID,URL_TYPE,CRAWL_INTERVAL,NEXT_FETCH_TIME,PURGE_URL) VALUES(?,?,?,?,?,?,?,?,?,?)";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, seqNum);
				stmt.setString(2, segment.getSegmentName());
				stmt.setString(3, segment.getUrl_pattern_rule());
				if (segment.isCrawl() == true) {
					stmt.setInt(4, 1);
				} else {
					stmt.setInt(4, 0);
				}
				stmt.setInt(5, priority);
				stmt.setInt(6, segment.getDomainId());
				stmt.setString(7, segment.getUrlType());
				stmt.setLong(8, segment.getCrawlInterval());
				stmt.setTimestamp(9, new Timestamp(segment.getNextFetchTime()));
				stmt.setString(10, segment.getPurgeUrl());
				if (!segment.getSegmentName().equalsIgnoreCase("default")) {
					query = "INSERT INTO SEGMENT_HTML_PATH_PATTERN (SEGMENT_ID,PATTERN_FORMAT,IS_DEFAULT,PATTERN_ID,FILE_TYPE,FILE_NAME,FILE_EXT) VALUES(?,?,?,?,?,?,?)";
					stmturlPath = conn.prepareStatement(query);
					for (Iterator<HTMLPathVO> i = segment.getPathVO().values().iterator(); i.hasNext();) {
						HTMLPathVO vo = i.next();
						String param = "";
						for (Iterator<HTMLPathVO.FolderType> j = vo.getFolderType().iterator(); j.hasNext();) {
							HTMLPathVO.FolderType type = j.next();
							param = param + type.getFolderType() + ":"+ type.getFolderName();
							if (j.hasNext()) {
								param = param + ",";
							}
						}
						stmturlPath.setInt(1, seqNum);
						stmturlPath.setString(2, param);
						stmturlPath.setInt(3, vo.isDefault() ? 1 : 0);
						stmturlPath.setInt(4, (Integer) vo.getPattern_id());
						stmturlPath.setString(5, vo.getFiletype());
						stmturlPath.setString(6, vo.getFileName());
						stmturlPath.setString(7, vo.getFileExt());
						stmturlPath.addBatch();
					}
				}
				stmt.execute();
				if (!segment.getSegmentName().equalsIgnoreCase("default")) {
					stmturlPath.executeBatch();
				}
				// Url redistribution on creation of new segment
				query = "SELECT * from URL_DETAIL";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						String url = rs.getString("URL");
						if (url.matches(segment.getUrl_pattern_rule())) {
							url = "'" + url + "'";
							query = "UPDATE URL_DETAIL SET SEGMENT_ID = "
									+ seqNum + " WHERE URL = " + url;
							stmt = conn.prepareStatement(query);
							stmt.execute();
						}
					}
				}
				//Add transformations
				List<TransformationVO> transformationList = segment.getTransformationVO();
				if(transformationList!=null && transformationList.size()>0){
				for(TransformationVO tsvo : transformationList){
				String transType=tsvo.getTransformationId().trim();
				String transPrty=tsvo.getTransformationPriority();
				String transformationId=null;
				String transquery = "SELECT TRANSFORMATION_ID FROM TRANSFORMATION_MASTER WHERE TRANSFORMATION_TYPE='"+ transType + "'";
				boolean transResult = stmt.execute(transquery);
				if(transResult){
					addTransResult = stmt.getResultSet();
					while(addTransResult.next()){
						transformationId = addTransResult.getString("TRANSFORMATION_ID");
						String addtransquery = "INSERT INTO SEGMENT_TRANSFORMATION (TRANSFORMATION_ID,SEGMENT_ID,TRANSFORMATION_PRIORITY) VALUES(?,?,?)";
						transPath = conn.prepareStatement(addtransquery);
						transPath.setInt(1, Integer.parseInt(transformationId));
						transPath.setInt(2, seqNum);
						transPath.setInt(3,Integer.parseInt(transPrty));
						transPath.executeQuery();
						}
					}
				}
			}
				conn.commit();
				segCreated = true;
			} catch (SQLException e) {
				conn.rollback();
				logger.error("Error while creating row in SEGMENT_MASTER in segment create method:" + e.getMessage());
				return segCreated;
			} finally {
					try {
						if(rs != null){
							rs.close();
						}
						if(rs1 != null){
							rs1.close();
						}
						if(addTransResult != null){
							addTransResult.close();
						}
						if (stmt != null) {
							stmt.close();
						}
						if(stmturlPath != null){
							stmturlPath.close();
						}
						if(stmtNextVal != null){
							stmtNextVal.close();
						}
						if(transPath != null){
							transPath.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						logger.error("Error while closing connection in segment create method:" + e);
					}
			}
	   }	
		return segCreated;
	}

	public List<SegmentVO> read() {
		return read(null);
		
	}
	
	/**
	 * This method gets all segment available in table for a given domain
	 * 
	 * @return List of SegmentVO
	 */
	public List<SegmentVO> read(String domainId){
		Connection conn = JDBCConnector.getConnection();
		List<SegmentVO> result = null;
		if (conn == null) {
			logger.error("Unable to get connection from Database: in read method block:");
			return result;
		}
		else{
			Statement stmt = null;
			ResultSet rs  = null;
			try {
				stmt = conn.createStatement();
				String query = " SELECT * FROM SEGMENT_MASTER";
				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					result = new ArrayList<SegmentVO>();
					while (rs.next()) {
						SegmentVO sg = new SegmentVO();
						sg.setSegmentId(rs.getInt("SEGMENT_ID"));
						sg.setSegmentName(rs.getString("SEGMENT_NAME"));
						sg.setUrl_pattern_rule(rs.getString("URL_PATTERN_RULE"));
						if (rs.getInt("CRAWL") == 0) {
							sg.setCrawl(false);
						} else {
							sg.setCrawl(true);
						}
						sg.setPriority(rs.getString("PRIORITY"));
						sg.setCrawlInterval(rs.getLong("CRAWL_INTERVAL"));
						if (rs.getTimestamp("NEXT_FETCH_TIME") != null) {
							sg.setNextFetchTime(rs.getTimestamp("NEXT_FETCH_TIME").getTime());
						}
						SegmentDetailCRUD detail = new SegmentDetailCRUD();
						sg.setDomainId(Integer.parseInt(domainId));
						detail.readBySegment(sg);
						result.add(sg);
					}
				}
			} catch (SQLException e) {
				logger.error("Error while executing query in read method block:" + e);
			} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
						if(conn != null){
							conn.close();
						}
						if(rs != null){
							rs.close();
						}
					} catch (SQLException e) {
						logger.info("Error while closing connection" + e);
					}
				}
		}
		return result;
	}

	public List<SegmentVO> readSegmentMaster(int domainId) {
		Connection conn = JDBCConnector.getConnection();
		LinkedList<SegmentVO> result = null;
		if (conn == null) {
			logger.error("Unable to get connection in readSegmentMaster method block:");
			return result;
		}
		else{
			Statement stmt = null;
			PreparedStatement pstmt1 = null;
			PreparedStatement pstmt2 = null;
			String liveCrawlId = null;
			try {
				stmt = conn.createStatement();
				String query = " SELECT * FROM SEGMENT_MASTER WHERE DOMAIN_ID="+ domainId + " ORDER BY PRIORITY ";
				
				//Read total count of urls from URL_HTML_LOC table for selected SEGMENT_ID
				//String countQuery = "SELECT COUNT(*) FROM URL_HTML_LOC where SEGMENT_ID=? and CRAWL_ID=? and LAST_FETCH_TIME IS NOT NULL and LAST_FETCH_TIME NOT IN ('Moved Permanently','Moved Temporarily','Page Not Found','Internal Server Error') and html_file_status !=1";
				String countQuery = "SELECT COUNT(*) FROM URL_HTML_LOC where SEGMENT_ID=? and CRAWL_ID=? and IS_HTMLIZED = '1'";
				pstmt1= conn.prepareStatement(countQuery);
				
				//Read total count of urls from URL_DETAIL table for selected SEGMENT_ID
				String urlCountQuery = "SELECT COUNT(*) FROM URL_DETAIL where SEGMENT_ID=? and CRAWL_ID=?";
				pstmt2= conn.prepareStatement(urlCountQuery);
				
				liveCrawlId = getCurrentCrawlId(domainId);
				
				boolean success = stmt.execute(query);
				if (success) {
					ResultSet rs = stmt.getResultSet();
					result = new LinkedList<SegmentVO>();
					ResultSet countRs = null;
					ResultSet urlCountRs = null;
					while (rs.next()) {
						SegmentVO sg = new SegmentVO();
						sg.setSegmentId(rs.getInt("SEGMENT_ID"));
						sg.setSegmentName(rs.getString("SEGMENT_NAME"));
						sg.setUrl_pattern_rule(rs.getString("URL_PATTERN_RULE"));
						if (rs.getInt("CRAWL") == 0) {
							sg.setCrawl(false);
						} else {
							sg.setCrawl(true);
						}
						sg.setPriority(rs.getString("PRIORITY"));
						sg.setDomainId(rs.getInt("DOMAIN_ID"));
						sg.setCrawlInterval(rs.getLong("CRAWL_INTERVAL"));
						if (rs.getTimestamp("NEXT_FETCH_TIME") != null) {
							sg.setNextFetchTime(rs.getTimestamp("NEXT_FETCH_TIME").getTime());
						}
						
						if(liveCrawlId != null){
							//Query for get total count of urls from URL_HTML_LOC
							pstmt1.setInt(1, sg.getSegmentId());
							pstmt1.setInt(2, Integer.parseInt(liveCrawlId));
							countRs = pstmt1.executeQuery();
							while(countRs.next()) {
								sg.setHtmlPageCount(countRs.getInt(1));
							}
							//Query for get total count of urls from URL_DETAIL
							pstmt2.setInt(1, sg.getSegmentId());
							pstmt2.setInt(2, Integer.parseInt(liveCrawlId));
							urlCountRs = pstmt2.executeQuery();
							while(urlCountRs.next()) {
								sg.setCrawledUrlCount(urlCountRs.getInt(1));
							}
						}else{
							sg.setHtmlPageCount(0);
							sg.setCrawledUrlCount(0);
						}
						result.add(sg);
					}
				}
			} catch (SQLException e) {
				logger.error("Error while executing query in readSegmentMaster method block:" + e.getMessage());
			} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
						if (pstmt1 != null) {
							pstmt1.close();
						}
						if (pstmt2 != null) {
							pstmt2.close();
						}
						if (conn != null) {
							conn.close();
						}
					} catch (SQLException e) {
						logger.error("Error while closing connection in readSegmentMaster method block:" + e.getMessage());
					}
				}
		}
		return result;
	}
	
	//Checking DOMAIN table any SEG_RULE_CHANGED is equal to 1
	public List<Integer> checkSegmentRuleStatus(){
		//boolean changed = false;
		List<Integer> segRuleChangedDomains = null;
		Statement segstmt = null;
		ResultSet segstatus = null;
		Connection conn = JDBCConnector.getConnection();
		try {
			segstmt = conn.createStatement();
			String segQuery = "SELECT DOMAIN_ID FROM DOMAIN WHERE SEG_RULE_CHANGED=1";
			segstatus = segstmt.executeQuery(segQuery);
			segRuleChangedDomains = new ArrayList<Integer>();
			while(segstatus.next()){
				//changed = true;
				segRuleChangedDomains.add(segstatus.getInt("DOMAIN_ID"));
			}	
		} catch (SQLException e) {
			logger.error("Error in checkSegmentRuleStatus() method: "+e.getMessage());
		} finally{
			if (segstmt != null) {
				try {
					segstatus.close();
					segstmt.close();
					conn.close();
				} catch (SQLException e) {
					logger.info("Error while closing connection" + e);
				}
			}
		}
		return segRuleChangedDomains;
	}
	
	//Checking DOMAIN table any SEG_HTML_PATH_CHANGED equal to 1
	public List<Integer> checkSegmentHtmlPathStatus(){
		List<Integer> segHtmlPathChangedDomains = null;
		Statement segstmt = null;
		ResultSet segstatus = null;
		Connection conn = JDBCConnector.getConnection();
		try {
			segstmt = conn.createStatement();
			String segQuery = "SELECT DOMAIN_ID FROM DOMAIN WHERE SEG_HTML_PATH_CHANGED=1";
			segstatus = segstmt.executeQuery(segQuery);
			segHtmlPathChangedDomains = new ArrayList<Integer>();
			while(segstatus!=null & segstatus.next()){
				segHtmlPathChangedDomains.add(segstatus.getInt("DOMAIN_ID"));
			}	
		} catch (SQLException e) {
			logger.error("Error in checkSegmentHtmlPathStatus() method:"+e.getMessage());
		} finally{
			if (segstmt != null) {
				try {
					segstatus.close();
					segstmt.close();
					conn.close();
				} catch (SQLException e) {
					logger.info("Error while closing connection in checkSegmentHtmlPathStatus() method:" + e.getMessage());
				}
			}
		}
		return segHtmlPathChangedDomains;
	}	

	//check connection
	public SegmentVO readByPrimaryKey(String primaryKey, Connection conn) {
		boolean connCreated = false;
		SegmentVO segment = new SegmentVO();
		if(conn == null) {
			conn = JDBCConnector.getConnection();
			connCreated = true;
		}
		if(conn != null){
			boolean result = false;
			boolean transresult = false;
			ResultSet rs = null;
			ResultSet translist = null;
			ResultSet resultSet = null;
			PreparedStatement stmt = null;
			try {
				String query = "SELECT * from SEGMENT_MASTER where SEGMENT_ID = ?";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, Integer.parseInt(primaryKey));
				resultSet = stmt.executeQuery();
				while(resultSet.next()) {
					segment.setSegmentId(resultSet.getInt("SEGMENT_ID"));
					segment.setSegmentName(resultSet.getString("SEGMENT_NAME"));
					segment.setUrl_pattern_rule(resultSet.getString("URL_PATTERN_RULE"));
					segment.setPurgeUrl(resultSet.getString("PURGE_URL"));
					if (resultSet.getInt("CRAWL") == 0) {
						segment.setCrawl(false);
					} else {
						segment.setCrawl(true);
					}
					segment.setPriority(resultSet.getString("PRIORITY"));
					segment.setUrlType(resultSet.getString("URL_TYPE"));
					segment.setCrawlInterval(resultSet.getLong("CRAWL_INTERVAL"));
					if (resultSet.getTimestamp("NEXT_FETCH_TIME") != null) {
						segment.setNextFetchTime(resultSet.getTimestamp("NEXT_FETCH_TIME").getTime());
					}
					segment.setDomainId(resultSet.getInt("DOMAIN_ID"));
				}
				resultSet.close();
				
				query = "SELECT * from SEGMENT_HTML_PATH_PATTERN where SEGMENT_ID = " + Integer.parseInt(primaryKey);
				Statement st = conn.createStatement();
				ResultSet rs1 = st.executeQuery(query);
				Map<Integer, HTMLPathVO> urlPath = new HashMap<Integer, HTMLPathVO>();
				while (rs1.next()) {
					HTMLPathVO vo = new HTMLPathVO();
					List<HTMLPathVO.FolderType> list = new ArrayList<HTMLPathVO.FolderType>();
					String param[] = rs1.getString("PATTERN_FORMAT").split(",");
					for (String s : param) {
					     HTMLPathVO.FolderType type = vo.new FolderType();
					     if(s.trim().length() > 1) {
					      type.setFolderType(s.split(":")[0]);
					      //type.setFolderName("");
					      type.setFolderName(s.split(":")[1]);
					     }else {
					      type.setFolderType("");
					      type.setFolderName("");
					     }
					     list.add(type);
					}
					vo.setPattern_id(rs1.getInt("PATTERN_ID"));
					vo.setDefault(rs1.getBoolean("IS_DEFAULT"));
					vo.setFolderType(list);
					vo.setFiletype(rs1.getString("FILE_TYPE"));
					vo.setFileName(rs1.getString("FILE_NAME"));
					vo.setFileExt(rs1.getString("FILE_EXT"));
					urlPath.put(rs1.getInt("PATTERN_ID"), vo);
				}
				
				rs1.close();
				//Get transformation Details for segmentId
				query = "SELECT * from SEGMENT_TRANSFORMATION where SEGMENT_ID = ?";
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, Integer.parseInt(primaryKey));
				rs = stmt.executeQuery();
				List<EditTransformationVO> tvoList=new ArrayList<EditTransformationVO>();
				while(rs.next()){
					EditTransformationVO tvo=new EditTransformationVO();
					tvo.setTransformationPriority(rs.getString("TRANSFORMATION_PRIORITY"));
					tvo.setTransformationId(rs.getString("TRANSFORMATION_ID"));
					//Get Transformation details by passing transformationId
					query = "SELECT * from TRANSFORMATION_MASTER where TRANSFORMATION_ID = ?";
					stmt = conn.prepareStatement(query);
					stmt.setInt(1, Integer.parseInt(rs.getString("TRANSFORMATION_ID")));
					transresult = stmt.execute();
					translist = stmt.getResultSet();
					while(translist.next()){
						tvo.setTransformationType(translist.getString("TRANSFORMATION_TYPE"));
					}	
					tvoList.add(tvo);
				}
				segment.setEditTransformVO(tvoList);
				segment.setPathVO(urlPath);
				
				rs.close();
			} catch (SQLException e) {
				logger.error("Error while executing query in readByPrimaryKey() method:" + e.getMessage());
				
			} finally {
					try {
						if (stmt != null) {
							stmt.close();
						}
						if(connCreated){
							conn.close();
						}
					} catch (SQLException e) {
						logger.error("Error while closing connection  in readByPrimaryKey() method:" + e.getMessage());
					}
				}
		}else{
			logger.error("Unable to get connection in readByPrimaryKey() method block:");
			segment = null;
		}
		return segment;
	}

	/**
	 * This method is used to delete a given Segment using a SegmentID
	 * 
	 * @param SegmentID
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteSegement(String segmentId)	throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.info("Connection not found. Could not create row in SEGMENT_MASTER");
			return result;
		}
		SegmentVO vo = readByPrimaryKey(segmentId, conn);
		Statement stmt = null;
		conn.setAutoCommit(false);
		try {
			stmt = conn.createStatement();
			// Delete transformation for segment.
			String query = "DELETE FROM SEGMENT_TRANSFORMATION WHERE SEGMENT_ID="+ segmentId;
			stmt.executeUpdate(query);
			//Updating Segment Id of URL which are there in deleted segment with the default segment for that domain
			query = "UPDATE URL_DETAIL set SEGMENT_ID = (SELECT SEGMENT_ID from SEGMENT_MASTER WHERE SEGMENT_NAME = 'default' and DOMAIN_ID = "
					+ vo.getDomainId() + ") WHERE SEGMENT_ID = " + segmentId;
			stmt.executeUpdate(query);

			query = "DELETE from SEGMENT_MASTER where SEGMENT_ID = "+ segmentId;
			stmt.executeUpdate(query);
			
			query = "UPDATE SEGMENT_MASTER SET PRIORITY = (PRIORITY - 1) WHERE PRIORITY > " + vo.getPriority() + " AND PRIORITY != 99999 AND DOMAIN_ID =  " + vo.getDomainId();
			stmt.executeUpdate(query);
			
			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			logger.info("Error while fetching row in SEGMENT_MASTER in deleteSegement() method" + e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					logger.info("Error while closing connection  in deleteSegement() method" + e);
				}
			}
		}
		return result;
	}

	/**
	 * This method is used to update a given Segment. It also update related
	 * information in child table.
	 * 
	 * @param segmentVO
	 * @return
	 * @throws SQLException
	 */
	public boolean updateSegement(SegmentVO segmentVO) throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in Segment updating process:");
			return result;
		}
		Statement stmtBatch = null;
		Statement stmt = null;
		PreparedStatement preStmt = null;
		PreparedStatement preStmt1 = null;
		PreparedStatement preStmt2 = null;
		Statement segstmt = null;
		conn.setAutoCommit(false);
		try {
			SegmentVO oldSegment = readByPrimaryKey(String.valueOf(segmentVO.getSegmentId()), conn); 
			//To check whether data has changed or not
			stmtBatch = conn.createStatement();
			stmt = conn.createStatement();
			String query = "UPDATE SEGMENT_MASTER set SEGMENT_NAME = '"
					+ segmentVO.getSegmentName() + "',URL_PATTERN_RULE = '"
					+ segmentVO.getUrl_pattern_rule() + "',CRAWL = "
					+ (segmentVO.isCrawl() ? 1 : 0) + ",PRIORITY = "
					+ segmentVO.getPriority() + ",URL_TYPE = '"
					+ segmentVO.getUrlType() + "',CRAWL_INTERVAL ='"
					+ segmentVO.getCrawlInterval() + "', PURGE_URL = '"+ segmentVO.getPurgeUrl()+"' where SEGMENT_ID = "
					+ segmentVO.getSegmentId();
			stmtBatch.addBatch(query);
			
			//Checking old segment url values with new url values
			if(oldSegment!=null && segmentVO!=null){
				if(!oldSegment.getUrl_pattern_rule().equalsIgnoreCase(segmentVO.getUrl_pattern_rule())){
					//Update Segment_Status_Master table
					segstmt = conn.createStatement();
					String segQuery = "UPDATE DOMAIN set SEG_RULE_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
					segstmt.execute(segQuery);
				}
				else if(!oldSegment.isCrawl()==segmentVO.isCrawl()){
					//Update Segment_Status_Master table
					segstmt = conn.createStatement();
					String segQuery = "UPDATE DOMAIN set SEG_RULE_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
					segstmt.execute(segQuery);
				}
				else if(!oldSegment.getPriority().equalsIgnoreCase(segmentVO.getPriority())){
					//Update Segment_Status_Master table
					segstmt = conn.createStatement();
					String segQuery = "UPDATE DOMAIN set SEG_RULE_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
					segstmt.execute(segQuery);
				}
				else if(!oldSegment.getUrlType().equalsIgnoreCase(segmentVO.getUrlType())){
					//Update Segment_Status_Master table
					segstmt = conn.createStatement();
					String segQuery = "UPDATE DOMAIN set SEG_RULE_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
					segstmt.execute(segQuery);
				}
			}
			
			/*
			 * The code updates the HTMLPath Pattern for a Segment and insert or delete existing HTMLPath Pattern.
			 */
			Set<Entry> entries = segmentVO.getPathVO().entrySet();
			for (Entry pairs : entries) {
				HTMLPathVO vo = (HTMLPathVO) pairs.getValue();
				String param = "";
				for (Iterator<HTMLPathVO.FolderType> j = vo.getFolderType()
						.iterator(); j.hasNext();) {
					HTMLPathVO.FolderType type = j.next();
					param = param + type.getFolderType() + ":"
							+ type.getFolderName();
					if (j.hasNext()) {
						param = param + ",";
					}
				}
				if (oldSegment.getPathVO().containsKey(pairs.getKey())) { //If oldSegment contains key,it means pattern needs to be updated else insert new pattern
					query = "UPDATE SEGMENT_HTML_PATH_PATTERN set PATTERN_FORMAT = '"
							+ param
							+ " ', IS_DEFAULT = "
							+ (vo.isDefault() ? 1 : 0)
							+ ", FILE_TYPE = '"
							+ vo.getFiletype()
							+ "', FILE_NAME = '"
							+ vo.getFileName()
							+ "', FILE_EXT = '"
							+ vo.getFileExt()
							+ "' where SEGMENT_ID = "
							+ segmentVO.getSegmentId()
							+ " and PATTERN_ID = '"
							+ pairs.getKey() + "'";
					stmtBatch.addBatch(query);
					//checking segment HtmlPath values changed or not
					HTMLPathVO oldSegVO = new HTMLPathVO();
					oldSegVO = (HTMLPathVO) oldSegment.getPathVO().get(pairs.getKey());
					if(oldSegVO!=null && vo!=null){
						if(oldSegVO.getFileExt()!=null && vo.getFileExt()!=null){
							if(!oldSegVO.getFileExt().equalsIgnoreCase(vo.getFileExt())){
								segstmt = conn.createStatement();
								String segQuery = "UPDATE DOMAIN set SEG_HTML_PATH_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
								segstmt.execute(segQuery);
							}
						}
						if(oldSegVO.getFileName()!=null && vo.getFileName()!=null){
						    if(!oldSegVO.getFileName().equalsIgnoreCase(vo.getFileName())){
								segstmt = conn.createStatement();
								String segQuery = "UPDATE DOMAIN set SEG_HTML_PATH_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
								segstmt.execute(segQuery);
						    }
						}
						if(oldSegVO.getFiletype()!=null && vo.getFiletype()!=null){
							if(!oldSegVO.getFiletype().equalsIgnoreCase(vo.getFiletype())){
								segstmt = conn.createStatement();
								String segQuery = "UPDATE DOMAIN set SEG_HTML_PATH_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
								segstmt.execute(segQuery);
							}
						}
					}
					
				} else {
					query = "INSERT INTO SEGMENT_HTML_PATH_PATTERN (SEGMENT_ID,PATTERN_FORMAT,IS_DEFAULT,PATTERN_ID,FILE_TYPE,FILE_NAME,FILE_EXT) values("
							+ segmentVO.getSegmentId()
							+ ",'"
							+ param
							+ "',"
							+ (vo.isDefault() ? 1 : 0)
							+ ","
							+ vo.getPattern_id()
							+ ",'"
							+ vo.getFiletype()
							+ "','"
							+ vo.getFileName()
							+ "','"
							+ vo.getFileExt() + "')";
					stmtBatch.addBatch(query);
					//Updating DOMAIN table status of SEG_HTML_PATH_CHANGED value to 1
					segstmt = conn.createStatement();
					String segQuery = "UPDATE DOMAIN set SEG_HTML_PATH_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
					segstmt.execute(segQuery);
				}
				oldSegment.getPathVO().remove(pairs.getKey());
			}

			Set<Entry> oldEntries = oldSegment.getPathVO().entrySet(); //If there are still data exist in oldSegment,it means there are path to be deleted
			if (!oldSegment.getPathVO().isEmpty()) {
				for (Entry entryDel : oldEntries) {
					query = "DELETE from SEGMENT_HTML_PATH_PATTERN WHERE SEGMENT_ID = "
							+ oldSegment.getSegmentId()
							+ " and PATTERN_ID = '"
							+ entryDel.getKey() + "'";
					stmtBatch.addBatch(query);
				}
			}
			
			//Update Transformations for segmentId
			if(segmentVO.getTransformationVO()!=null){
				String transformationId = null;
				for(TransformationVO tvo : segmentVO.getTransformationVO()){
					query = "SELECT TRANSFORMATION_ID FROM TRANSFORMATION_MASTER WHERE TRANSFORMATION_TYPE='"+ tvo.getTransformationType().trim() + "'";
					stmt = conn.createStatement();
					boolean success = stmt.execute(query);
					if (success) {
						ResultSet rs = stmt.getResultSet();
						ResultSet transList = null;
						while (rs.next()) {
							transformationId = rs.getString("TRANSFORMATION_ID");
							// Add new transformation for segment.
							query = "SELECT * from SEGMENT_TRANSFORMATION where SEGMENT_ID=? and TRANSFORMATION_ID=?";
							preStmt = conn.prepareStatement(query);
							preStmt.setInt(1, segmentVO.getSegmentId());
							preStmt.setInt(2, Integer.parseInt(transformationId));
							transList = preStmt.executeQuery();
							if (transList != null && transList.next()) {
								query = "UPDATE SEGMENT_TRANSFORMATION SET TRANSFORMATION_PRIORITY=? where SEGMENT_ID=? and TRANSFORMATION_ID=?";
								preStmt1 = conn.prepareStatement(query);
								preStmt1.setInt(1, Integer.parseInt(tvo.getTransformationPriority()));
								preStmt1.setInt(2, segmentVO.getSegmentId());
								preStmt1.setInt(3, Integer.parseInt(transformationId));
								preStmt1.executeUpdate();
								//Updating DOMAIN table status of SEG_HTML_PATH_CHANGED value to 1
								if(!transList.getString("TRANSFORMATION_PRIORITY").equalsIgnoreCase(tvo.getTransformationPriority())){
									segstmt = conn.createStatement();
									String segQuery = "UPDATE DOMAIN set SEG_HTML_PATH_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
									segstmt.execute(segQuery);
								}
							}
							else{
								query = "INSERT INTO SEGMENT_TRANSFORMATION (SEGMENT_ID,TRANSFORMATION_ID,TRANSFORMATION_PRIORITY) VALUES(?,?,?)";
								preStmt2 = conn.prepareStatement(query);
								preStmt2.setInt(1, segmentVO.getSegmentId());
								preStmt2.setInt(2, Integer.parseInt(transformationId));
								preStmt2.setInt(3, Integer.parseInt(tvo.getTransformationPriority()));
								preStmt2.executeQuery();
								//Updating DOMAIN table status of SEG_HTML_PATH_CHANGED value to 1
								segstmt = conn.createStatement();
								String segQuery = "UPDATE DOMAIN set SEG_HTML_PATH_CHANGED=1 WHERE DOMAIN_ID="+segmentVO.getDomainId();
								segstmt.execute(segQuery);
							}
						}
					}
				}
			}
			stmtBatch.executeBatch();
			conn.commit();
			result =true;
		} catch (SQLException e) {
			conn.rollback();
			logger.info("Error while fetching row in SEGMENT_MASTER in updateSegement() method:" + e);
		} finally {
			try {
				if (stmtBatch != null){
					stmtBatch.close();
				}	
				if (stmt != null){
					stmt.close();
				}
				if (preStmt != null){
					preStmt.close();
				}	
				if (preStmt1 != null){
					preStmt1.close();
				}	
				if (preStmt2 != null){
					preStmt2.close();
				}
				if (segstmt != null){
					segstmt.close();
				}	
				if (conn != null){
					conn.close();
				}	
			} catch (SQLException e) {
				logger.error("Error while closing connection in updateSegement() method:" + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * This method is used to Merge two Segment. First it update segmentId of
	 * segment to be merge with the selected SegmentId,then delete the segmentId
	 * 
	 * @param segmentMerge
	 * @param segmentSelected
	 * @throws SQLException
	 */
	public boolean mergeSegment(SegmentVO segmentMerge, SegmentVO segmentSelected)	throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in Segment merging process:");
			return result;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			conn.setAutoCommit(false);
			SegmentDetailCRUD detailCrud = new SegmentDetailCRUD();
			stmt = detailCrud.mergeSegmentforURL(segmentMerge.getSegmentId(),
					segmentSelected.getSegmentId(), stmt);
			String query = "";
			/*
			 * Here,rules are merged with the "|" sign of regex pattern between the two segment
			 */
			if (segmentMerge.getUrl_pattern_rule() != null) {
				query = "UPDATE SEGMENT_MASTER SET URL_PATTERN_RULE = '"
						+ segmentMerge.getUrl_pattern_rule() + "|"
						+ segmentSelected.getUrl_pattern_rule() + "' WHERE SEGMENT_ID = "
						+ segmentMerge.getSegmentId();
				stmt = conn.createStatement();
				stmt.addBatch(query);
			}
			/*
			 * Here,segment id of the URL to be merged is set with the segment of the merging Segment
			 */
			query = "UPDATE URL_DETAIL SET SEGMENT_ID = "
					+ segmentMerge.getSegmentId() + " WHERE SEGMENT_ID = "+ segmentSelected.getSegmentId();
			stmt.addBatch(query);
			
			// updating the segment id in URL_HTML_LOC table
			
			query = "UPDATE URL_HTML_LOC SET SEGMENT_ID = "
					+ segmentMerge.getSegmentId() + " WHERE SEGMENT_ID = "+ segmentSelected.getSegmentId();
			stmt.addBatch(query);
			/*
			 * Merge segment is deleted
			 */
			query = "DELETE FROM SEGMENT_MASTER WHERE SEGMENT_ID = "+ segmentSelected.getSegmentId();
			stmt.addBatch(query);
			stmt.executeBatch();
			conn.commit();
			result = true;
		} catch (SQLException sqlEx) {
			conn.rollback();
			logger.error("Error while merging segment in mergeSegment() method:" + sqlEx.getMessage());
		} finally {
			try {
				if (null != stmt){
					stmt.close();
				}	
				if (null != conn){
					conn.close();
				}	
			} catch (SQLException e) {
				logger.error("Error while closing connection in mergeSegment() method:" + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * This method is used to split segment into sub-segment. It add the URL to
	 * the splitted Segment based on provided rule.
	 * 
	 * @param splitSegmentVO
	 * @param segmentIdtoSplit
	 * @throws SQLException
	 */
	public boolean splitSegment(List<SegmentVO> splitSegmentVO, String segmentIdtoSplit) throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in splitSegment() process:");
			return result;
		}
		Statement detailStmt = null;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		SegmentVO splitVO = readByPrimaryKey(segmentIdtoSplit, conn);
		Iterator<SegmentVO> segmentIter = splitSegmentVO.iterator();
		int priorityCount =  Integer.parseInt(splitVO.getPriority());
		String query = "";
		try {
			detailStmt = conn.createStatement();
			String masterQuery = "INSERT INTO SEGMENT_MASTER (SEGMENT_ID,SEGMENT_NAME,URL_PATTERN_RULE,CRAWL,PRIORITY,DOMAIN_ID,CRAWL_INTERVAL,NEXT_FETCH_TIME) VALUES(?,?,?,?,?,?,?,?)";
			pstmt = conn.prepareStatement(masterQuery);
			conn.setAutoCommit(false);
			while (segmentIter.hasNext()) {
				SegmentVO segmentVO = segmentIter.next();
				stmt = conn.createStatement();
				stmt.execute("SELECT SEGMENT_MASTER_SEQUENCE.NEXTVAL FROM DUAL");
				ResultSet rs = stmt.getResultSet();
				rs.next();
				int seqNum = rs.getInt("NEXTVAL");
				pstmt.setInt(1, seqNum);
				pstmt.setString(2, segmentVO.getSegmentName());
				pstmt.setString(3, segmentVO.getUrl_pattern_rule());
				if (segmentVO.isCrawl() == true) {
					pstmt.setInt(4, 1);
				} else {
					pstmt.setInt(4, 0);
				}
				pstmt.setInt(5, ++priorityCount);
				pstmt.setInt(6, segmentVO.getDomainId());
				pstmt.setLong(7, segmentVO.getCrawlInterval());

				pstmt.setTimestamp(8, new Timestamp(segmentVO.getNextFetchTime()));
				pstmt.addBatch(); // Adding the new Segment to Segment_Master Table

				query = "SELECT * from URL_DETAIL where SEGMENT_ID="+ Integer.parseInt(segmentIdtoSplit);

				boolean success = stmt.execute(query);
				if (success) {
					rs = stmt.getResultSet();
					while (rs.next()) {
						String url = rs.getString("URL");
						if (url.matches(segmentVO.getUrl_pattern_rule())) { // If the URL matches with the Rule,then it is added to the newly created segment
							url = "'" + url + "'";
							query = "UPDATE URL_DETAIL SET SEGMENT_ID = "
									+ seqNum + " WHERE URL = " + url;
							detailStmt.addBatch(query); 
						}
					}
				}
			}
			//moving the priority with number of split segment of existing segment of priority above it 
			query = "UPDATE SEGMENT_MASTER SET PRIORITY = (PRIORITY + " + (priorityCount - Integer.parseInt(splitVO.getPriority()))  + ") WHERE PRIORITY > " + splitVO.getPriority() + " AND DOMAIN_ID =  " + splitVO.getDomainId();
			stmt.addBatch(query);
			stmt.executeBatch();
			pstmt.executeBatch();
			detailStmt.executeBatch();
			conn.commit();
			result = true;
		} catch (SQLException sqlEx) {
			conn.rollback();
			logger.error("Error while splitting segment in splitSegment() method:"+sqlEx.getMessage());
		} finally {
			try{
				if (null != detailStmt){
					detailStmt.close();
				}	
				if (null != stmt){
					stmt.close();
				}
				if (null != conn){
					conn.close();
				}	
			}catch(Exception e){
				logger.error("Error while closing connection in splitSegment() method: "+ e.getMessage());
			}
		}
		return result;
	}

	public boolean checkforUniquePriority(String priority) throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		boolean result = false;
		try {
			stmt = conn.createStatement();
			boolean success = stmt.execute("select * from segment_master where priority = " + Integer.parseInt(priority));
			ResultSet rs = stmt.getResultSet();
			if (success) {
				if (rs.next()){
					result = false;
				}	
				else{
					result = true;
				}	
			}
		} catch (SQLException sqlEx) {
			logger.error("Error in checkforUniquePriority() method: "+sqlEx.getMessage());
		} finally {
			if (null != conn)
				conn.close();
			if (null != stmt)
				stmt.close();
		}
		return result;
	}

	public void generateURLforSegment(String segmentId, String domainId, int crawlId) throws SQLException {
		generateURLforSegment(segmentId, domainId, crawlId, null);
	}
	
	//Generates Html mapping links for urls using live crawlId, segmentId then insert to URL_HTML_LOC table
	public void generateURLforSegment(String segmentId, String domainId, int crawlId, String specificUrl) throws SQLException {
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			PreparedStatement stmt = null;
			PreparedStatement checkstmt = null;
			PreparedStatement updateStmt = null;
			conn.setAutoCommit(false);
			ResultSet urlMaplinks = null;
			String query = null;
			String updateQuery = null;
			String insertQuery = null;
			try {
				updateQuery = "UPDATE URL_HTML_LOC SET URL_LOC=?, PATTERN_ID=?, DEFAULT_LOCATION=? where URL=? and CRAWL_ID=? and DOMAIN_ID=?";
				updateStmt = conn.prepareStatement(updateQuery);
				
				insertQuery = "INSERT INTO URL_HTML_LOC(URL,URL_LOC,SEGMENT_ID,PATTERN_ID,DEFAULT_LOCATION,CRAWL_ID,DOMAIN_ID) VALUES(?,?,?,?,?,?,?)";
				stmt = conn.prepareStatement(insertQuery);
				
				query = "SELECT * from URL_HTML_LOC where URL=? and CRAWL_ID=?";
				checkstmt = conn.prepareStatement(query);
				
				for (HTMLPathVO format : getPatternFormatforSegment(segmentId)) {
					for (String urls : getURLforSegment(segmentId,crawlId, specificUrl)) {
						String url = urls;
						//Common process for insert, update urlMapLinks in URL_HTML_LOC table
						if (urls.endsWith("/")) {
							url = urls.substring(0, url.lastIndexOf("/"));
						}
						String path = "";
						String finalPath = "";
						for (ListIterator<HTMLPathVO.FolderType> i = format.getFolderType().listIterator(); i.hasNext();) {
							HTMLPathVO.FolderType type = i.next();
							if (type.getFolderType().equalsIgnoreCase("resourceName")) {
								path = path + "/" + getHtmlContentFileName(url).trim(); // If folder type is resourceName,then it will get fileName and add it to HTML path.
							} else if (type.getFolderType().equalsIgnoreCase("paramName")) {
								path = path	+ "/" + getRequestParameterValue(url, type.getFolderName()).trim(); // TO DO : If parameter is not found in request parameter ,then folder will not be created
							} else if (type.getFolderType().equalsIgnoreCase("plainText")) {
								path = path + "/" + type.getFolderName().trim();
							}
						}
	
						if (format.getFiletype().equalsIgnoreCase("resourceName")) {
							path = path + "/" + getHtmlContentFileName(url).trim();
						} else if (format.getFiletype().equalsIgnoreCase("paramName")) {
							String param = getRequestParameterValue(url,format.getFileName()).trim();
							if (param != null || param != "") {
								path = path + "/" + param.trim();
							}
						} else if (format.getFiletype().equalsIgnoreCase("plainText")) {
							path = path + "/" + format.getFileName().trim();
						}
						
						finalPath = getURLPath(url) + path.trim() + format.getFileExt(); 
						
						//Checking url is existed with crawlId
						checkstmt.setString(1, urls);
						checkstmt.setInt(2, crawlId);
						urlMaplinks = checkstmt.executeQuery();
						
						if (urlMaplinks != null && urlMaplinks.next()) {
							updateStmt.setString(1, finalPath.replace("//", "/"));
							updateStmt.setInt(2, format.getPattern_id());
							updateStmt.setInt(3, format.isDefault() ? 1 : 0);
							updateStmt.setString(4, urls);
							updateStmt.setInt(5, crawlId);
							updateStmt.setString(6, domainId);
							updateStmt.addBatch();
						}
						else{
							stmt.setString(1, urls);
							stmt.setString(2, finalPath.replace("//", "/"));
							stmt.setInt(3, format.getSegment_id());
							stmt.setInt(4, format.getPattern_id());
							stmt.setInt(5, format.isDefault() ? 1 : 0);
							stmt.setInt(6, crawlId);
							stmt.setString(7, domainId);
							stmt.addBatch();
						}
					}
					/*if(urlMaplinks != null){
						urlMaplinks.close();
					}*/
				}
				stmt.executeBatch();
				updateStmt.executeBatch();
				conn.commit();
			} catch (Exception sqlEx) {
				try {
					conn.rollback();
					logger.error("Error in generateURLforSegment() method:"+sqlEx.getMessage());
				} catch (SQLException e) {
					logger.error("Error while closing connection in generateURLforSegment() method:"+e.getMessage());
				}
			} finally {
				try {
					if (null != stmt)
						stmt.close();
					if (null != updateStmt)
						updateStmt.close();
					if (null != checkstmt)
						checkstmt.close();
					if (null != urlMaplinks)
						urlMaplinks.close();
					if (null != conn)
						conn.close();
				} catch (SQLException e) {
					logger.error("Error while closing connection in generateURLforSegment() method: "+e.getMessage());
				}
			}
		}else{
			logger.error("Unable to get connection from DB: in generateURLforSegment() method:");
		}
	}

	/**
	 * @param domainId
	 * @return
	 */
	private String getDirectoryPathForDomain(String domainId) {
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		String result = "";
		try {
			stmt = conn.createStatement();
			boolean success = stmt.execute("select directory from domain where domain_id = "+ domainId);
			ResultSet rs = stmt.getResultSet();
			if (success) {
				rs.next();
				result = rs.getString(1);
			}
		} catch (SQLException sqlEx) {
			logger.error("Error in getDirectoryPathForDomain() method:"+sqlEx.getMessage());
		} finally {
			try {
				if (null != conn)
					conn.close();
				if (null != stmt)
					stmt.close();
			} catch (SQLException e) {
				logger.error("Error while closing connection in getDirectoryPathForDomain() method:"+e.getMessage());
			}
		}
		return result;
	}

	/**
	 * @param segmentId
	 * @return
	 */
	private String[] getURLforSegment(String segmentId,int crawlId, String url) {
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		List<String> result = new ArrayList<String>();
		try {
			stmt = conn.createStatement();
			String query = " SELECT URL FROM URL_DETAIL WHERE SEGMENT_ID = "	+ segmentId+" and CRAWL_ID ="+crawlId;
			if(url != null) {
				query = query.concat(" and URL ='"+url+"'");
			}
			stmt.execute(query);
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				result.add(rs.getString("URL"));
			}
			rs.close();
		} catch (SQLException sqlEx) {
			logger.error("Error in getURLforSegment() method: "+sqlEx.getMessage());
		} finally {
			try {
				if (null != stmt)
					stmt.close();
				if (null != conn)
					conn.close();
			} catch (SQLException e) {
				logger.error("Error while closing connection  in getURLforSegment() method: "+e.getMessage());
			}
		}
		return result.toArray(new String[result.size()]);
	}

	private String[] getURLforSegment(String segmentId,int crawlId) {
		return getURLforSegment(segmentId,crawlId, null);
	}
	
	/**
	 * @return
	 */
	private HTMLPathVO[] getPatternFormatforSegment(String segmentId) {
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		List<HTMLPathVO> result = new ArrayList<HTMLPathVO>();
		try {
			stmt = conn.createStatement();
			stmt.execute("SELECT * FROM SEGMENT_HTML_PATH_PATTERN WHERE SEGMENT_ID = "+ segmentId);
			ResultSet resultSet = stmt.getResultSet();
			while (resultSet.next()) {
				HTMLPathVO vo = new HTMLPathVO();
				List<HTMLPathVO.FolderType> list = new ArrayList<HTMLPathVO.FolderType>();
				String param[] = resultSet.getString("PATTERN_FORMAT").split(",");
				 for (String s : param) {
				     HTMLPathVO.FolderType type = vo.new FolderType();
				     if(s.length() > 1) {
				      type.setFolderType(s.split(":")[0]);
				      type.setFolderName(s.split(":")[1]);
				     }else {
				      type.setFolderType("");
				      type.setFolderName("");
				     }
					 list.add(type);
					}
				vo.setPattern_id(resultSet.getInt("PATTERN_ID"));
				vo.setDefault(resultSet.getBoolean("IS_DEFAULT"));
				vo.setFolderType(list);
				vo.setFiletype(resultSet.getString("FILE_TYPE"));
				vo.setFileName(resultSet.getString("FILE_NAME"));
				vo.setFileExt(resultSet.getString("FILE_EXT"));
				vo.setSegment_id(resultSet.getInt("SEGMENT_ID"));
				result.add(vo);
			}
			resultSet.close();
		} 
		catch (SQLException sqlEx) {
			logger.error("Error in getPatternFormatforSegment() method:"+sqlEx.getMessage());
		} 
		finally {
			try {
				if (null != stmt)
					stmt.close();
				if (null != conn)
					conn.close();
			} catch (SQLException e) {
				logger.error("Error while closing connection  in getPatternFormatforSegment() method:"+e.getMessage());
			}
		}
		return result.toArray(new HTMLPathVO[result.size()]);
	}

	public static String getURLPath(String url) {
		String path = null;
		Matcher matcher = NutchConstants.URL_PATH_PATTERN.matcher(url);
		if (matcher.find()) {
			path = matcher.group(); //This get url navigation path till the file name eg : /us/storeus/ProductDetail.jsp - It return /us/storeus/
		}
		return path;
	}

	public String getHtmlContentFileName(String url) throws MalformedURLException {
		String group = getURLPath(url);
		String fileName = "";
		String reqParam = "";
		String finalFileName = "";
		if (group != null){
			fileName = url.replace(group, NutchConstants.EMPTY_STRING);
		}	
		Matcher reqMatcher = NutchConstants.URL_REQUEST_PARAMS_PATTERN.matcher(fileName);
		if (reqMatcher.find()) {
			reqParam = reqMatcher.group();
		}
		if (reqParam != null){
			fileName = fileName.replace(reqParam, NutchConstants.EMPTY_STRING);
		}	
		//if fileName contains .jsp,it will replace with emtpy string
		if (fileName.contains(".jsp")) {
			finalFileName = fileName.replace(".jsp",NutchConstants.EMPTY_STRING);
		} else {
			finalFileName = fileName;
		}
		return finalFileName;
	}

	public String getHtmlContentFilePath(String url) throws MalformedURLException {
		String group = getURLPath(url);
		return group;
	}

	public static String getURLRequestParameters(String url) {
		String reqparams = null;
		Matcher matcher = NutchConstants.URL_REQUEST_PARAMS_PATTERN.matcher(url);
		if (matcher.find()) {
			reqparams = matcher.group();
		}
		return reqparams;
	}

	/**
	 * @param url
	 * @param string
	 * @return
	 */
	private String getRequestParameterValue(String url, String reqParam) {
		String reqParamValue = "";
		Matcher matcher = Pattern.compile("[;]*" + reqParam + "[\\w%/.]*[=\\w-/.+%]*[$]*").matcher(url);
		if (matcher.find()) {
			reqParamValue = matcher.group();
		}
		/*
		 * If parameter is found in url query parameter,then it return the parameter value else return the parameter name
		 */
		if (!reqParamValue.isEmpty()){
			return reqParamValue.split("&")[0].split("=")[1];
		}
		else{
			return reqParam;
		}
	}
	

	//Fetching current crawlId from CRAWL_MASTER table by passing DOMAIN_ID, LIVE=1
	public String getCurrentCrawlId(int domainId) {
		Connection conn = JDBCConnector.getConnection();
		String currCrawlId = null;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in getCurrentCrawlId() method:");
			return currCrawlId;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = " SELECT CRAWL_ID FROM CRAWL_MASTER MASTER WHERE LIVE=1 and DOMAIN_ID="+domainId;
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					currCrawlId = rs.getString("CRAWL_ID");
				}
			}
		} catch (Exception e) {
			logger.error("Error while fetching row in SEGMENT_MASTER  in getCurrentCrawlId() method:" + e.getMessage());
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error while closing connection  in getCurrentCrawlId() method:" + e.getMessage());
				}
			}
		return currCrawlId;
	}
	
	//Swap Priority for segment
	public boolean swapPriority(SegmentVO segVo, String move){
		String currPriority = segVo.getPriority();
		Connection conn = JDBCConnector.getConnection();
		Statement stmt = null;
		boolean update = false;
		String selectQuery=null;
		int updatePriority=0;
		if(move.equalsIgnoreCase("up")){
			updatePriority = Integer.parseInt(currPriority) - 1;
			selectQuery = "SELECT max(priority) from SEGMENT_MASTER where DOMAIN_ID="+segVo.getDomainId()+" and PRIORITY<"+currPriority;
		} else if(move.equalsIgnoreCase("down")){
			updatePriority = Integer.parseInt(currPriority) + 1;
			selectQuery = "SELECT min(priority) from SEGMENT_MASTER where DOMAIN_ID="+segVo.getDomainId()+" and PRIORITY>"+currPriority;
		}
		if(conn!=null){
			try{
				conn.setAutoCommit(false);
				stmt = conn.createStatement();
				stmt.execute(selectQuery);
				ResultSet segResult = stmt.getResultSet();
				while(segResult.next()){
					String updateQuery = "UPDATE SEGMENT_MASTER SET PRIORITY="+currPriority+" Where PRIORITY="+segResult.getInt(1) +" and DOMAIN_ID="+segVo.getDomainId();
					stmt.execute(updateQuery);
					updateQuery = "UPDATE SEGMENT_MASTER SET PRIORITY="+updatePriority+" Where SEGMENT_ID="+segVo.getSegmentId()+" and DOMAIN_ID="+segVo.getDomainId();
					stmt.execute(updateQuery);
					update=true;
				}
				conn.commit();
			}
			catch (SQLException e) {
				logger.info("Error while fetching row in SEGMENT_MASTER in swapPriority() method:" + e.getMessage());
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						logger.info("Error while closing connection  in swapPriority() method:" + e);
					}
				}
			}
		}
		return update;
	}
	
	//Returns the count of urls in URL_DETAIL table and htmlized pages from URL_HTML_LOC table
	public List<Integer> getCount(int domainId){
		List<Integer> countList = new ArrayList<Integer>();
		String query = null;
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			Statement countStmt = null;
			ResultSet resultSet = null;
			try {
				String liveCrawlId = getCurrentCrawlId(domainId);
				if(liveCrawlId != null){
					countStmt = conn.createStatement();
					query = "SELECT COUNT(*) FROM SEGMENT_MASTER WHERE DOMAIN_ID = "+domainId;
					resultSet = countStmt.executeQuery(query);
					if(resultSet.next()){
						countList.add(resultSet.getInt(1));
					}
					resultSet = null;
					query ="SELECT COUNT(*) FROM URL_DETAIL WHERE DOMAIN_ID = "+domainId+" AND CRAWL_ID = "+liveCrawlId;
					resultSet = countStmt.executeQuery(query);
					if(resultSet.next()){
						countList.add(resultSet.getInt(1));
					}
					resultSet = null;
					query ="SELECT COUNT(*) FROM URL_HTML_LOC WHERE DOMAIN_ID = "+domainId+" AND CRAWL_ID = "+liveCrawlId+" AND IS_HTMLIZED = '1'";
					resultSet = countStmt.executeQuery(query);
					if(resultSet.next()){
						countList.add(resultSet.getInt(1));
					}
				}else{
					countList = null;
				}
			} catch (SQLException e) {
				logger.error("Error while fetching rows from  SEGMENT_MASTER, URL_DETAIL, URL_HTML_LOC table: in getCount() method :"+e.getMessage());
			}finally{
					try {
						if(resultSet != null){
							resultSet.close();
						}
						if(countStmt != null){
							countStmt.close();
						}
						if(conn != null){
							conn.close();
						}
					} catch (SQLException e) {
						logger.error("Error while closing statement in getCount() method : "+e.getMessage());
					}
				}
		}else{
			countList = null;
		}
		return countList;
	}
}
