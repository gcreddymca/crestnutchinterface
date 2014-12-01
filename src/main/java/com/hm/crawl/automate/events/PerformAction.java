package com.hm.crawl.automate.events;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.AutoCrawlerThread;
import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.RequestMasterCURD;


public class PerformAction implements ThreadCompleteListener {
	
	public static final Logger logger = LoggerFactory.getLogger(PerformAction.class);
	private boolean isCompleted = true;

	public PerformAction() {
	}
	RequestMasterCURD reqMastCurd = new RequestMasterCURD();

	
	public String performAction(int domainId, String eventName, String[] domainIdList,String[] checkedSegments, String url, String segmentId, String isApi,String[] selectedURLS, String trackId,String all, String recursive, String siteKey, String actionName,String tag) {
		return performAction(domainId, domainIdList,eventName, checkedSegments, url, segmentId, isApi, selectedURLS, trackId, all, recursive, false, siteKey, actionName,tag);
	}
	
	public String performAction(int domainId,String[] domainIdList, String eventName, String[] checkedSegments, String url, 
			String segmentId, String isApi,String[] selectedURLS, String trackId,String all, String recursive, boolean isThreadWait, String siteKey, String actionName,String tag) {
		String forwardJsp = null;		
		int trackerId = 0;
		if(isApi == null){ 
				forwardJsp="/jsp/showStatus.jsp";
				/*inserting the request into DB*/
				int createdRequestId = reqMastCurd.createRequest(domainId, domainIdList,eventName, checkedSegments, url, segmentId, isApi, selectedURLS, 0, all, recursive, siteKey, actionName, "PENDING", null,tag);
				if (createdRequestId != 0){
					logger.info("Request Created : Request Id is : "+createdRequestId);
				}else{
					logger.error("Unable to Create the Request");
				}
				//boolean running = reqMastCurd.getThreadState();
				if(isCompleted){
					isCompleted = false;
					AutoCrawlerThread autoCrawlerThread = new AutoCrawlerThread();
					Thread thread = new Thread(autoCrawlerThread);
					autoCrawlerThread.addListener(this);
					thread.start();
				}
			
		}else if(eventName.equalsIgnoreCase("trackConfirm")){
				if(trackId != null && !trackId.isEmpty()){
					String trackerStatus = new CrawlUtil().getTrackerStatus(Integer.parseInt(trackId));
					forwardJsp = "/jsp/api/showTrackStatus.jsp?trackerStatus="+trackerStatus;
				}else{
					logger.error("tracker id missed to track the status");
				}
		//	Issue lock api call
		}else if(isApi.equalsIgnoreCase("true") && eventName.equalsIgnoreCase("issueLockConfirm")){
				//issuing the marker
				forwardJsp = issuMarker(domainId, domainIdList,eventName, checkedSegments, url, segmentId, isApi, selectedURLS, trackerId, all, recursive, siteKey, actionName,tag);
		}
		//clearing the issued lock
		else if(isApi.equalsIgnoreCase("true") && eventName.equalsIgnoreCase("clearLockConfirm")){
				if(trackId !=null && !trackId.isEmpty()){
						//veryifing the lock status is it already cleared or not
						boolean notCleared = reqMastCurd.verifyLockStatus(trackId);
						//if not cleared clears the lock
						if(notCleared){
								boolean success = reqMastCurd.clearMarker(eventName,trackId);
								if(success){
									forwardJsp = "/jsp/api/track.jsp?trackerId=LOCK RELEASED";
									logger.info("LOCK RELEASED");
									AutoCrawlerThread autoCrawlerThread = new AutoCrawlerThread();
									Thread thread = new Thread(autoCrawlerThread);
									autoCrawlerThread.addListener(this);
									thread.start();
								}else{
									forwardJsp = "/jsp/api/track.jsp?trackerId=UNABLE TO RELEASE THE LOCK";
									logger.info("UNABLE TO RELEASE THE LOCK");
								}
						}else{
							forwardJsp = "/jsp/api/track.jsp?trackerId=LOCK ALREADY RELEASED";
							logger.info("LOCK ALREADY RELEASED");
						}
			}else{
				logger.error("Request id is null in clearLock block:");
				forwardJsp = "/jsp/api/track.jsp?trackerId=UNABLE TO RELEASE THE LOCK";
				logger.info("UNABLE TO RELEASE THE LOCK");
			}
		}else{
				//Handling the Api call 
				CrawlUtil cUtil = new CrawlUtil();
				trackerId =cUtil.createTrackerId();
				/*inserting the request into DB*/
				int requestId = reqMastCurd.createRequest(domainId, domainIdList,eventName, checkedSegments, url, segmentId, isApi, selectedURLS, trackerId, all, recursive, siteKey, actionName, "PENDING",null,tag);
				if (requestId != 0){
					logger.info("Request Created : Request Id is : "+requestId);
				}else{
					logger.error("Unable to Create the Request");
				}
				//Verifying is there any action in progress or not
				if(isCompleted){
						isCompleted = false;
						AutoCrawlerThread autoCrawlerThread = new AutoCrawlerThread();
						Thread thread = new Thread(autoCrawlerThread);
						autoCrawlerThread.addListener(this);
						thread.start(); 
						/*if(isThreadWait) {
							try {
								thread.join();
							} catch (InterruptedException e) {
								logger.error("Error in performAction method"+e.getMessage());
							}
						}*/
				}
				//Returning request id as response to track status
				forwardJsp = "/jsp/api/track.jsp?trackerId="+requestId;
			}		
			return forwardJsp;
	}
	@Override
	public void notifyOfThreadComplete(AutoCrawlerThread autoCrawlerThread) {
		isCompleted = true;
	}
	public synchronized String issuMarker(int domainId, String[] domainIdList,String eventName, String[] checkedSegments, String url,String segmentId,String isApi,String[] selectedURLS, int trackId, String all, String recursive, String siteKey, String actionName,String tag){
		
		String forwardJsp = null;
		
		//checking the request process table where any action in progress or not
		if(!new CrawlUtil().checkRequestProcessStatus()){
				int createRequestId = reqMastCurd.createRequest(domainId, domainIdList,eventName, checkedSegments, url, segmentId, isApi, selectedURLS, trackId, all, recursive, siteKey, actionName, "LOCK ISSUED",null,tag);
				//reqMasterCurd.setRequestTime(requestId);
				isCompleted = false;
				forwardJsp = "/jsp/api/track.jsp?trackerId="+createRequestId;
				logger.info("Lock Issued");
		}else{
			forwardJsp = "/jsp/api/track.jsp?trackerId=LOCK IS NOT AVAILABLE";
			logger.info("Lock is Not available");
		}
		return forwardJsp;
		
	}
	
	public boolean forceClearLock(String requestId){
		boolean success = false;
		if(requestId != null && !requestId.isEmpty()){
			success = reqMastCurd.doReset(requestId);
			if(success){
				logger.info("Lock Cleared and Pending actions will be resume");
				AutoCrawlerThread autoCrawlerThread = new AutoCrawlerThread();
				Thread thread = new Thread(autoCrawlerThread);
				autoCrawlerThread.addListener(this);
				thread.start();
			}else{
				logger.error("Unable to reset the status in forceClearLock() method:");
			}
		}
		return success;
	}
}


	

