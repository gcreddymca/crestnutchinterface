package com.hm.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.file.monitor.JDBCConnector;

public class MonitorUtil {
	
	public static final Logger LOG = LoggerFactory.getLogger(MonitorUtil.class);
	
	/*
	 * This method returns all the segments ids configured for that regex path
	 */
	public static LinkedHashMap<String, Object> getRegexSegmentMonitorMap() {
		Connection	conn = JDBCConnector.getConnection();
		LinkedHashMap<String, Object> regexMonitorMap = new LinkedHashMap<>();
		if (conn != null) {
			Statement stmt1 = null;
			try {
				stmt1 = conn.createStatement();
				/*String query1 = "select a.monitor_type, b.regex, c.segment, c.url, b.folder_path from MONITOR_FOLDER a inner join  MONITOR_FOLDER_PATHS b  on a.MONITOR_ID = b.MONITOR_ID " +
						"left outer join  MONITOR_FOLDER_PATH_ACTIONS c on b.MONITOR_FOLDER_ID = c.MONITOR_FOLDER_ID order by a.priority, b.regex";*/
				
				//query changed as part of to get the domain name from domain master
				
				
				/*String query1 = "select a.monitor_type, b.regex, c.segment, c.url, b.folder_path, d.domain_name from MONITOR_FOLDER a inner join  MONITOR_FOLDER_PATHS b  on a.MONITOR_ID = b.MONITOR_ID "+ 
								"left outer join  MONITOR_FOLDER_PATH_ACTIONS c on b.MONITOR_FOLDER_ID = c.MONITOR_FOLDER_ID left outer join SEGMENT_MASTER sm on sm.SEGMENT_ID = c.segment left outer join "+ 
								"DOMAIN d on d.domain_id = sm.domain_id	order by a.priority, b.regex";*/
				
				String query1 = "select a.monitor_type, b.regex, c.segment, c.url, b.folder_path, NVL(d.domain_name, d1.domain_name) as domain_name from MONITOR_FOLDER a inner join "+
								"MONITOR_FOLDER_PATHS b on a.MONITOR_ID = b.MONITOR_ID	left outer join  MONITOR_FOLDER_PATH_ACTIONS c on b.MONITOR_FOLDER_ID = c.MONITOR_FOLDER_ID "+
								"left outer join URL_DETAIL ud on (ud.url = c.url OR ud.url = concat(c.url, '/')) left outer join domain d1 on d1.domain_id=ud.domain_id "+
								"left outer join SEGMENT_MASTER sm on sm.SEGMENT_ID = c.segment left outer join DOMAIN d on d.domain_id = sm.domain_id  order by a.priority";
				
				boolean success = stmt1.execute(query1);
				if (success) {
					ResultSet rs1 = stmt1.getResultSet();
					//List<String> urlList = new ArrayList<String>();
					//List<String> segList = new ArrayList<String>();
					Map<String, List<String>> domainSegs = new HashMap<String, List<String>>();
					Map<String, List<String>> domainUrls = new HashMap<String, List<String>>();
					String regex = null;
					List<Object> segsUrlsList = new ArrayList<Object>();
					String regexKey = null;
					String docRoot = CrawlUtil.getDocRootPath();
					while (rs1.next()) {
						if(!rs1.getString("FOLDER_PATH").endsWith("/")) {
							regexKey = rs1.getString("FOLDER_PATH") + "/" +rs1.getString("REGEX");
							if(!regexKey.contains(docRoot)){
								regexKey = docRoot + regexKey;
							}
						}else {
							regexKey = rs1.getString("FOLDER_PATH") + rs1.getString("REGEX");
							if(!regexKey.contains(docRoot)){
								regexKey = docRoot + regexKey;
							}
						}
						if(regex == null || regexKey.equalsIgnoreCase(regex)) {
							
						}else {
							if(!domainUrls.isEmpty()) {
								//segsUrlsList.add(urlList);
								segsUrlsList.add(domainUrls);
							}
							if(!domainSegs.isEmpty()){
								//segsUrlsList.add(segList);
								segsUrlsList.add(domainSegs);
							}
							if(!segsUrlsList.isEmpty()) {
								regexMonitorMap.put(regex, segsUrlsList);
							}
							domainUrls = new HashMap<String, List<String>>();
							domainSegs = new HashMap<String, List<String>>();
							segsUrlsList = new ArrayList<Object>(); 
						}
						regex = regexKey;
						if (rs1.getString("SEGMENT") != null) {
							//segList.add(rs1.getString("SEGMENT"));
							
							//domainSegs.put(rs1.getString("SEGMENT"), String.valueOf(rs1.getInt("DOMAIN_ID")));
							
							if(domainSegs.get(rs1.getString("domain_name")) != null) {
								domainSegs.get(rs1.getString("domain_name")).add(rs1.getString("SEGMENT"));
							} else {
								List<String> segsList = new ArrayList<String>();
								segsList.add(rs1.getString("SEGMENT"));
								domainSegs.put(rs1.getString("domain_name"), segsList);
							}
							//domainSegs.put(String.valueOf(rs1.getInt("DOMAIN_ID")), );
						}
						if (rs1.getString("URL") != null) {
							//urlList.add(rs1.getString("URL"));
							if(domainUrls.get(rs1.getString("domain_name")) != null) {
								domainUrls.get(rs1.getString("domain_name")).add(rs1.getString("URL"));
							} else {
								List<String> urlsList = new ArrayList<String>();
								urlsList.add(rs1.getString("URL"));
								domainUrls.put(rs1.getString("domain_name"), urlsList);
							}
						}
						
						if(rs1.getString("monitor_type").equalsIgnoreCase("CDN")) {
							regexMonitorMap.put(regexKey, "CDN");
						}
						if(rs1.getString("monitor_type").equalsIgnoreCase("JSP")) {
							regexMonitorMap.put(regexKey, "JSP");
						}
							
					}	
					
					/*if(!domainSegs.isEmpty() || !domainUrls.isEmpty()){
						segsUrlsList.add(domainSegs);
						segsUrlsList.add(domainUrls);
						regexMonitorMap.put(regex, segsUrlsList);
					}else{
						//String regex_path = folderPath+","+regex;
						regexMonitorMap.put(regex, segsUrlsList);
					}*/
				}
			} catch (SQLException e) {
				LOG.error(e.getMessage());
			} finally {
				if (stmt1 != null) {
					try {
						stmt1.close();
						conn.close();
					} catch (SQLException e) {
						LOG.info("Error while closing connection" + e);
					}
				}
			}
		}
		System.out.println("regexMonitorMap ......" +regexMonitorMap);
		return regexMonitorMap;
	}
	
	public static Kind<?>[] getListOfEvents() {
		Kind<?>[] events = new Kind<?>[3];
		events[0] = ENTRY_CREATE;
		events[1] = ENTRY_MODIFY;
		events[2] = ENTRY_DELETE;
		return events;
	}
	public static Path[] getDummyFoldersToMonitor()  {
		Path[] paths = new Path[1];
		paths[0] = Paths.get("E:/NVIZ/PLT/jboss-eap-4.2/jboss-as/server/atg2/deploy/Plantronics.ear/plt_estore.war");
		return paths;
	}
	
	public static Path[] getFoldersToMonitor()  {
		List<Path> monitorPaths = new ArrayList<Path>();
		Path[] paths = null;
		Connection	conn = JDBCConnector.getConnection();
		String monitorPathsSql = "SELECT MONITOR_FOLDER_PATHS.folder_path from MONITOR_FOLDER INNER JOIN MONITOR_FOLDER_PATHS " +
				"ON MONITOR_FOLDER.MONITOR_ID = MONITOR_FOLDER_PATHS.MONITOR_ID ORDER BY monitor_folder_paths.folder_path";
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(monitorPathsSql);
			String fPath = null, mPath = null;
			String docRoot = CrawlUtil.getDocRootPath();
			while(rs.next()) {
				fPath = rs.getString("folder_path");
				docRoot = docRoot.replaceAll("\\\\\\\\", "/");
				if(!fPath.contains(docRoot)){
					fPath = docRoot + fPath;
				}
				monitorPaths.add(Paths.get(fPath));
				
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if(conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing connection in getFoldersToMonitor(): " + e);
			}
		}
		
		String prevPath = null;
		List<Path> newMonitorPaths = new ArrayList<Path>();
		for(Path monitorPath : monitorPaths) {
			if(prevPath == null) {
				prevPath = monitorPath.toString();
				newMonitorPaths.add(monitorPath);
			}
			
			if(!prevPath.equalsIgnoreCase(monitorPath.toString()) && !monitorPath.startsWith(prevPath.toString())){
				newMonitorPaths.add(monitorPath);
			}
			prevPath = monitorPath.toString();
					
		}
		
		paths = newMonitorPaths.toArray(new Path[0]);
		/*if(paths.length == 0) {
			paths = getDummyFoldersToMonitor();
		}*/
		return paths;
	}
	
	
	/**
	 * This Method returns segmentNames for passed segmentIDs
	 * @param segmentIds
	 * @return
	 */
	public static List<String> getSegmentNames(String segmentIds){
		   List<String> segmentNameList = new ArrayList<String>();
		    Connection conn = JDBCConnector.getConnection();
		    if(conn != null){
		    	try {
					Statement stmt = conn.createStatement();
					String segsNamequery = "SELECT SEGMENT_NAME  FROM SEGMENT_MASTER WHERE SEGMENT_ID IN ("+ segmentIds+")";
					ResultSet resultSet = stmt.executeQuery(segsNamequery);
					while(resultSet.next()){
						segmentNameList.add(resultSet.getString("SEGMENT_NAME"));
					}
				} catch (SQLException e) {
					LOG.error(e.getMessage());
				}
		    }
		return segmentNameList;
	}
	
	public static Map<String, String> getSegmentNamesAndPurgeUrls(String segmentIds){
		   Map<String, String> segmentsMap = new HashMap<String, String>();
		    Connection conn = JDBCConnector.getConnection();
		    if(conn != null){
		    	try {
					Statement stmt = conn.createStatement();
					String segsNamequery = "SELECT SEGMENT_NAME, PURGE_URL  FROM SEGMENT_MASTER WHERE SEGMENT_ID IN ("+ segmentIds+")";
					ResultSet resultSet = stmt.executeQuery(segsNamequery);
					while(resultSet.next()){
						segmentsMap.put(resultSet.getString("SEGMENT_NAME"), resultSet.getString("PURGE_URL"));
					}
				} catch (SQLException e) {
					LOG.error(e.getMessage());
				}
		    }
		return segmentsMap;
	   }
	
	
	public static boolean isUrlExists(String url, int crawlId) {
		boolean isUrlExists = false;
		String checkquery = "SELECT * from URL_DETAIL where URL=? and CRAWL_ID=?";
		Connection conn = JDBCConnector.getConnection();
		PreparedStatement pStmt = null;
		try {
			if(conn != null) {
				pStmt = conn.prepareStatement(checkquery);
				pStmt.setString(1, url);
				pStmt.setInt(2, crawlId);
				ResultSet rs = pStmt.executeQuery();
				if(rs.next()) {
					isUrlExists = true;
				}
			}
		}catch(SQLException sqle) {
			LOG.error("Error while fetching details from SEGMENT_MASTER table in getCrawlSegmentIds() method: "+sqle.getMessage());
		}finally {
				try {
					if (pStmt != null) {
						pStmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while closing connection in getCrawlSegmentIds() :" + e);
				}
		}
		return isUrlExists;
	}
	
	/*
	 * This method returns all the segments ids configured for that domain
	 */
	public static Map<Integer,String> getCrawlSegmentIds(int domainId) {
		Connection	conn = JDBCConnector.getConnection();
		Map<Integer,String> segmentMap = new HashMap<Integer,String>();
		if (conn != null) {
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT SEGMENT_ID,URL_PATTERN_RULE FROM SEGMENT_MASTER where CRAWL = 1 and DOMAIN_ID="+domainId+" order by priority";
				boolean success = stmt.execute(query);
				if (success) {
					ResultSet rs = stmt.getResultSet();
					while (rs.next()) {
						if(rs.getString("URL_PATTERN_RULE") != null){
							segmentMap.put(rs.getInt("SEGMENT_ID"),rs.getString("URL_PATTERN_RULE"));
						}
					}
				}
			} catch (SQLException e) {
				LOG.error("Error while fetching details from SEGMENT_MASTER table in getCrawlSegmentIds() method: "+e.getMessage());
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
							conn.close();
					} catch (SQLException e) {
						LOG.error("Error while closing connection in getCrawlSegmentIds() method: " + e.getMessage());
					}
				}
			}
		}
		return segmentMap;
	}
	
	
	//Fetch all the actions defined for different folder paths to be monitored from DB
	public static LinkedHashMap<String, Object> getMonitorFolderActions() {
		//Fetch all the segment regex of folder paths and construct a map with key as segment id and value as regex
		LinkedHashMap<String, Object> allMonitorMap = new LinkedHashMap<>();
		allMonitorMap = getRegexSegmentMonitorMap();
		System.out.println("allMonitorMap:"+allMonitorMap);
		if(allMonitorMap.isEmpty())
			allMonitorMap = getDummyMonitorFolderActions();
		
		//Fetch all the regexes in the order of CDN static files, specific files, jsp files
		//LinkedHashMap<String, Object> segmentsMap = new LinkedHashMap<>();
		
		return allMonitorMap;
	}
	
	//Fetch all the actions defined for different folder paths to be monitored from DB
	public static LinkedHashMap<String, Object> getDummyMonitorFolderActions() {
		
		//Fetch all the regexes in the order of CDN static files, specific files, jsp files
		LinkedHashMap<String, Object> segmentsMap = new LinkedHashMap<>();
		
		//CDN static file rules
		segmentsMap.put(".*\\.css", "CDN");
		
		//Specific file rules
		segmentsMap.put("/middle-east/catalog/category.jsp", new ArrayList<>().add("304"));
		segmentsMap.put("/middle-east/catalog/product.jsp", new ArrayList<>().add("305"));
		
		List<String> listOfSegs = new ArrayList<String>();
		listOfSegs.add("320");
		listOfSegs.add("321");
		segmentsMap.put("/inc/*", listOfSegs);
		
		List<String> listOfUrls = new ArrayList<String>();
		listOfUrls.add("/middle-east/company/");
		listOfUrls.add("/middle-east/terms/");
		segmentsMap.put("/middle-east/inc/*", listOfUrls);
		
		//JSP file rules
		listOfUrls = new ArrayList<String>();
		listOfUrls.add("/company/contact.jsp");
		listOfUrls.add("/company/management/kannappan.jsp");		
		segmentsMap.put("//company/*", listOfUrls);
		
		segmentsMap.put(".*\\.jsp", "JSP");

		return segmentsMap;
	}
	
	public String getDocRoot(){
		Connection conn = JDBCConnector.getConnection();
		String docRoot = null;
		if(conn != null){
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String query = "SELECT VALUE FROM MONITOR_MASTER";
				ResultSet rs = stmt.executeQuery(query);
				while(rs.next()){
					docRoot = rs.getString("VALUE");
				}
			} catch (SQLException e) {
				LOG.error("Error while fetching details from MONITOR_MASTER table in getDocRoot() process: "+e.getMessage());
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while Closing connection in  getDocRoot() process: "+ e.getMessage());
				}
			}
		}
		return docRoot;
	}

}
