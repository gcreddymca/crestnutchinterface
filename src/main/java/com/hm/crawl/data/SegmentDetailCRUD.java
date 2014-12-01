package com.hm.crawl.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.nutch.tools.JDBCConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.SegmentVO;
import com.hm.crawl.data.vo.UrlHtmlLocVO;

public class SegmentDetailCRUD {
	
	private final static Logger logger = LoggerFactory.getLogger(SegmentDetailCRUD.class);
	/**
	 * @param segment
	 * @return
	 */
	public boolean create(SegmentVO segment) {
		Connection conn = JDBCConnector.getConnection();
		boolean result = false;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in create() process:");
			return result;
		}
		PreparedStatement stmt = null;
		try {
			String query = "INSERT INTO SEGMENT_URLS_DETAIL (SEGMENT_ID,URL,RAW_HTML_FILE_LOC) VALUES(?,?,?)";
			stmt = conn.prepareStatement(query);
			Map<String, String> map = segment.getUrlHtmlLocMap();
			logger.info("Map size" + map.size());
			logger.info("Key set size" + map.keySet().size());
			for (String key : map.keySet()) {
				stmt.setInt(1, segment.getSegmentId());
				stmt.setString(2, key);
				stmt.setString(3, map.get(key));
				stmt.addBatch();
			}
			int[] i = stmt.executeBatch();
		} catch (SQLException e) {
			logger.error("Error while inserting records into SEGMENT_URLS_DETAIL table in create() method: "+e.getMessage());
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logger.error("Error while closing connection in create() method: " + e.getMessage());
				}
		}
		return result;
	}

	//Read Segment Url's from URL_HTML_LOC table for current live crawlID
	/**
	 * @param segment
	 * @return
	 */
	public SegmentVO readBySegment(SegmentVO segment) {
		Connection conn = JDBCConnector.getConnection();
		//List<SegmentVO> result = null;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in readBySegment process:");
			segment = null;
			return segment;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			CrawlUtil crawlutil = new CrawlUtil();
			int crawlId = crawlutil.getCrawlId(null, segment.getDomainId());
			String query = "SELECT * from URL_HTML_LOC where SEGMENT_ID="+segment.getSegmentId()+" and CRAWL_ID="+crawlId;
			String defaultSegmentQuery ="SELECT * from URL_DETAIL where SEGMENT_ID="+segment.getSegmentId()+" and CRAWL_ID="+crawlId;
			
			//Creating URLHTMLLOCVO to segments other than default from URL_HTML_LOC
			if(!segment.getSegmentName().equalsIgnoreCase("default")){
				boolean success = stmt.execute(query);
				if (success) {
					ResultSet rs = stmt.getResultSet();
					while (rs.next()) {
						UrlHtmlLocVO urlHtmlLocVo = new UrlHtmlLocVO();
						urlHtmlLocVo.setUrl(rs.getString("URL"));
						urlHtmlLocVo.setUrlLoc(rs.getString("URL_LOC"));
						urlHtmlLocVo.setLastFetchedTime(rs.getString("LAST_FETCH_TIME"));
						urlHtmlLocVo.setHtmlFileStatus(rs.getInt("HTML_FILE_STATUS"));
						segment.getUrlHtmlLocVO().add(urlHtmlLocVo);
					}
				}
			}
			// creating urlHtmlVO for default segment url's fetching from url_detail table
			else{
				ResultSet resultSet = stmt.executeQuery(defaultSegmentQuery);
				while(resultSet.next()){
					UrlHtmlLocVO urlHtmlLocVo = new UrlHtmlLocVO();
					urlHtmlLocVo.setUrl(resultSet.getString("URL"));
					segment.getUrlHtmlLocVO().add(urlHtmlLocVo);
				}
			}
		} catch (Exception e) {
			logger.error("Error while fetching details from  URL_HTML_LOC, URL_DETAIL table in readBySegment() method:" + e.getMessage());
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error while closing connection in readBySegment() method:" + e.getMessage());
				}
			}
		return segment;
	}

	/**
	 * @param segmentList
	 * @return
	 */
	public List<SegmentVO> read(List<SegmentVO> segmentList) {
		Connection conn = JDBCConnector.getConnection();
		List<SegmentVO> result = null;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in read() process:");
			return result;
		}
		if (segmentList == null || segmentList.size() == 0) {
			logger.error("Segment List is null");
			return result;
		}
		Statement stmt = null;
		try {
			String query = "SELECT * from SEGMENT_URLS_DETAIL";
			stmt = conn.createStatement();
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					for (SegmentVO sg : segmentList) {
						if (rs.getInt(1) == sg.getSegmentId()) {
							sg.getUrlHtmlLocMap().put(rs.getString(2),rs.getString(3));
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error while fetching row from SEGMENT_URLS_DETAIL table in read() method: " + e.getMessage());
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					logger.error("Error while closing connection in read() process: " + e.getMessage());
				}
		}
		return result;
	}
	
	
	/**
	 * This method reads domain from Domain Master table for domainId.
	 * 
	 * @return List<DomainVO>
	 * @throws Exception
	 */
	public DomainVO readDomainDetails(int domainId) throws Exception {
		Connection conn = JDBCConnector.getConnection();
		DomainVO result = null;
		if (conn == null) {
			logger.error("Unable to get connection from DB: in readDomainDetails method process:");
			return result;
		}
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			String query = " SELECT * FROM DOMAIN where DOMAIN_ID="+domainId;
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				result = new DomainVO();
				while (rs.next()) {
					result.setDomainName(rs.getString("DOMAIN_NAME"));
					result.setRaw_content_directory(rs.getString("raw_content_directory"));
					result.setFinal_content_directory(rs.getString("final_content_directory"));
				}
			}
		} catch (Exception e) {
			logger.error("Error while fetching row from DOMAIN table in readDomainDetails method:" + e.getMessage());
			throw new Exception("Could not read domain from DOMAIN table");
		} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error while closing connection in readDomainDetails method:" + e.getMessage());
				}
			}
		return result;
	}

	/**
	 * @param mergeSegmentId
	 * @param selectedSegmentId
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */
	public Statement mergeSegmentforURL(int mergeSegmentId,	int selectedSegmentId, Statement stmt) throws SQLException {
		try {
			String query = "UPDATE SEGMENT_URLS_DETAIL SET SEGMENT_ID = "+ mergeSegmentId + " WHERE SEGMENT_ID = "+ selectedSegmentId;
			stmt.addBatch(query);
			return stmt;
		} catch (SQLException e) {
			logger.error("Error while updating record in SEGMENT_URLS_DETAIL table in mergeSegmentforURL() process: "+e.getMessage());
			throw new SQLException(e.getMessage());
		}
	}

	/**
	 * @param rule
	 * @param segmentId
	 * @param segmentIdtoSplit
	 * @param detailStmt
	 * @param stmt
	 * @throws SQLException
	 */
	public void runRuleonexistingURL(String rule, int segmentId,
			String segmentIdtoSplit, PreparedStatement detailStmt,
			Statement stmt) throws SQLException {
		try {
			String query = "SELECT * from SEGMENT_URLS_DETAIL where SEGMENT_ID="+ Integer.parseInt(segmentIdtoSplit);
			boolean success = stmt.execute(query);
			if (success) {
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					String url = rs.getString("URL");
					if (url.matches(rule)) {
						url = "'" + url + "'";
						detailStmt.setInt(1, segmentId);
						detailStmt.setString(2, url);
						detailStmt.addBatch();
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error while fetching rows from  SEGMENT_URLS_DETAIL table in runRuleonexistingURL() process:" + e.getMessage());
			throw new SQLException(e.getMessage());
		} finally {
			logger.info("done");
		}
	}
	
	//Returns the count of urls in URL_DETAIL table and htmlized pages from URL_HTML_LOC table
	public 	List<Integer> getURLDetailCount(int segmentId,int domainId){
		List<Integer> countList = new ArrayList<Integer>();
		String query = null;
		Connection conn = JDBCConnector.getConnection();
		if(conn != null){
			Statement countStmt = null;
			ResultSet resultSet = null;
			try {
				String liveCrawlId = new SegmentMasterCRUD().getCurrentCrawlId(domainId);
				if(liveCrawlId != null){
					countStmt = conn.createStatement();
					query ="SELECT COUNT(*) FROM URL_DETAIL WHERE SEGMENT_ID = "+segmentId+" AND CRAWL_ID = "+liveCrawlId;
					resultSet = countStmt.executeQuery(query);
					if(resultSet.next()){
						countList.add(resultSet.getInt(1));
					}
					resultSet = null;
					//query ="SELECT COUNT(*) FROM URL_HTML_LOC WHERE SEGMENT_ID = "+segmentId+" AND CRAWL_ID = "+liveCrawlId+" AND LAST_FETCH_TIME IS NOT NULL AND LAST_FETCH_TIME NOT IN ('Moved Permanently','Moved Temporarily','Page Not Found','Internal Server Error') AND HTML_FILE_STATUS !=1";
					query ="SELECT COUNT(*) FROM URL_HTML_LOC WHERE SEGMENT_ID = "+segmentId+" AND CRAWL_ID = "+liveCrawlId+" AND IS_HTMLIZED = '1'";
					resultSet = countStmt.executeQuery(query);
					if(resultSet.next()){
						countList.add(resultSet.getInt(1));
					}
				}else{
					countList = null;
				}
			} catch (SQLException e) {
				logger.error("Error while fetching rows from URL_DETAIL, URL_HTML_LOC table in getURLDetailCount() method :"+e.getMessage());
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
					} catch (Exception e) {
						logger.error("Error while closing connection in getURLDetailCount() method :"+e.getMessage());
					}
			}
		}else{
			logger.error("Unable to get connection from DB: in getURLDetailCount() process:");
			countList = null;
		}
		return countList;
	}
}
