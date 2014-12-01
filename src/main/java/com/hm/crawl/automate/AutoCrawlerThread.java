package com.hm.crawl.automate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.nutch.tools.JDBCConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.events.ThreadCompleteListener;
import com.hm.crawl.data.RequestMasterCURD;
import com.hm.crawl.data.vo.RequestVO;

public class AutoCrawlerThread implements Runnable {

	public static final Logger LOG = LoggerFactory.getLogger(AutoCrawlerThread.class);
	
	
	RequestMasterCURD reqMasterCurd = new RequestMasterCURD();
	
	public AutoCrawlerThread() {
			}

	 private final Set<ThreadCompleteListener> listeners
     = new CopyOnWriteArraySet<ThreadCompleteListener>();
	 public final void addListener(final ThreadCompleteListener listener) {
		 listeners.add(listener);
	 }
	 public final void removeListener(final ThreadCompleteListener listener) {
		 listeners.remove(listener);
	 }
	private final void notifyListeners() {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this);
		}
	}
	@Override
	public void run() {

		boolean IS_CONTINUE = true;
		try{
		while (IS_CONTINUE) {
			List<Integer> requests = reqMasterCurd.getRequests();
			if (!requests.isEmpty()) {
				for (Integer requestId : requests) {
					try{
					reqMasterCurd.changeRequestStatus(requestId,"PROCESSING");
					reqMasterCurd.setRequestTime(requestId);
					processRequest(requestId);
					reqMasterCurd.setRequestEndTime(requestId);
					}finally{
						if(checkRequestProcessStatus(requestId)){
							reqMasterCurd.changeRequestStatus(requestId,"PROCESS FAILED");
							reqMasterCurd.setRequestEndTime(requestId);
						}
					}
				}
			}else{
				IS_CONTINUE = false;
			}
		}
		}finally{
			     notifyListeners();
		}
	}
	
	/**
	 * this method process all the requests
	 * @param requestId
	 */
	
	public void processRequest(int requestId){
		RequestVO requestVO = reqMasterCurd.readByPrimaryKey(requestId);
		AutoCrawler aCralwer = new AutoCrawler();
		String[] checkedSegments = null;
		String[] urls = null;
		String isApi = null;
		if(requestVO.isApi()){
			isApi = "true";
		}else{
			isApi ="false";
		}
		if(requestVO.getCheckedSegments() != null){
			 checkedSegments = requestVO.getCheckedSegments().split(",");
		}
		if(requestVO.getCheckedUrls() != null){
			urls = requestVO.getCheckedUrls().split(",");
		}
		if(requestVO.getCheckedUrls() != null){
			urls = requestVO.getCheckedUrls().split(",");
		}
		
		try {
			if("AutoCrawlConfirm".equalsIgnoreCase(requestVO.getEvent()) || "htmlizeDomainConfirm".equalsIgnoreCase(requestVO.getEvent())) {
				try {
					aCralwer.autoCrawl(requestVO.getDomainId(),requestVO.getTrackId(),requestId);
				} catch (SQLException e) {
					LOG.error("Error in processRequest() method while executing autoCrawl "+e.getMessage());
				}
			}else if(requestVO.getEvent().equalsIgnoreCase("htmlizeSelectedSegmentsConfirm")) {
				aCralwer.htmlizeSegments(requestVO.getDomainId(), checkedSegments,requestVO.getTrackId(),requestId);
			}else if(requestVO.getEvent().equalsIgnoreCase("refreshSelectedSegmentsConfirm")) {
				aCralwer.refreshSelectedSegments(requestVO.getDomainId(), checkedSegments,requestVO.getTrackId(),requestId);
			}else if(requestVO.getEvent().equalsIgnoreCase("refreshDomainConfirm")) {
				aCralwer.refreshDomain(requestVO.getDomainId(),requestVO.getTrackId(),requestId);				
			}else if(requestVO.getEvent().equalsIgnoreCase("refreshURLConfirm")) {
				aCralwer.refreshURL(requestVO.getDomainId(), requestVO.getSegmentId(), requestVO.getUrl(),requestVO.getTrackId(),requestId);				
			}else if(requestVO.getEvent().equalsIgnoreCase("refreshSelectedURLSConfirm")) {
				aCralwer.refreshSelectedURLS(requestVO.getDomainId(), requestVO.getSegmentId(), urls,requestVO.getTrackId(),isApi,requestId);				
			}
			else if(requestVO.getEvent().equalsIgnoreCase("deleteURLHtmlConfirm")) {
				aCralwer.deleteURLHtml(requestVO.getUrl(),requestVO.getDomainId(),requestVO.getSegmentId(),requestVO.getTrackId(),requestId);				
			}else if(requestVO.getEvent().equalsIgnoreCase("deleteSelectedURLHtmlConfirm")) {
				aCralwer.deleteSelectedURLHtml(urls,requestVO.getDomainId(),requestVO.getTrackId(),requestId);				
			}			
			else if(requestVO.getEvent().equalsIgnoreCase("deleteSelectedSegmentHtmlConfirm")) {
				aCralwer.deleteSelectedSegmentHtml(requestVO.getDomainId(), checkedSegments,requestVO.getTrackId(),requestId);			
			}else if(requestVO.getEvent().equalsIgnoreCase("deleteDomainHtmlConfirm")) {
				aCralwer.deleteDomainHtml(requestVO.getDomainId(),requestVO.getTrackId(),requestId);			
			}else if (requestVO.getEvent().equalsIgnoreCase("deleteApiConfirm")){
				aCralwer.deleteSelectedURLFiles(requestVO.getDomainId(),requestVO.getUrl(),requestVO.getAll(),requestVO.getRecursive(),requestVO.getTrackId(),requestId);
			}else if(requestVO.getEvent().equalsIgnoreCase("purgeURLConfirm")) {
				aCralwer.purgeURL(requestVO.getDomainId(),  requestVO.getSiteKey(), requestVO.getUrl(),requestVO.getTrackId(),requestId);				
			}else if(requestVO.getEvent().equalsIgnoreCase("selectedDomainHtmlizeConfirm")) {
				aCralwer.selectedHtmlizeDomain(requestVO.getDomainList(),requestVO.getTrackId(),requestId);	
			}
			
		} catch (IOException e) {
			LOG.error("Error in processRequest() method: "+e.getMessage());
		}
		
	}
	
	
	public boolean checkRequestProcessStatus(int requestId){ 
			boolean actionStatus = false;
			Statement statusStmt = null;
			String status = null;
			ResultSet rs = null;
			Connection conn = JDBCConnector.getConnection();
		try {
			statusStmt = conn.createStatement();
			String query1 = "SELECT * FROM REQUEST_PROCESSOR WHERE STATUS IN ('LOCK RELEASED','LOCK RELEASED FORCEIBLY','HTMLIZATION DOMAIN COMPLETED','HTMLIZATION SEGMENTS COMPLETED','REFRESHING DOMAIN COMPLETED','REFRESHING SEGMENTS COMPLETED'," +
					"'REFRESHING URL COMPLETED','REFRESHING SELECTED URLS COMPLETED','DELETE SELECTED URL HTML PROCESS COMPLETED'," +
					"'DELETE SELECTED SEGMENT HTML PROCESS COMPLETED','DELETE DOMAIN HTML PROCESS COMPLETED','DELETE URL HTML PROCESS COMPLETED','DELETE FILES  PROCESS COMPLETED','PURGE URL PROCESS COMPLETED') AND REQUEST_ID = "+requestId ;
			
			rs = statusStmt.executeQuery(query1);
			if(!rs.next()){
				actionStatus = true;
			}
		} catch (SQLException e) {
			LOG.error("Error in checkRequestProcessStatus() method: "+e.getMessage());
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
					LOG.error("Error while closing connection in checkRequestProcessStatus() method:"+e.getMessage());
				}
			}
		return actionStatus;
		} 
}
