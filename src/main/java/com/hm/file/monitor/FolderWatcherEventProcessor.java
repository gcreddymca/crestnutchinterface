package com.hm.file.monitor;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.purge.PurgeEdgeCastData;
import com.hm.util.MonitorUtil;

public class FolderWatcherEventProcessor implements Runnable {

	private LinkedHashMap<WatchKey, List<WatchEvent<?>>> watchKeyEventsMap = new LinkedHashMap<WatchKey, List<WatchEvent<?>>>();
	
	private EventProcessorHelper epHelper = null;
	private WatchService wService;
	private WatchKey wKey;
	private volatile Map<WatchKey, Path> keys;
	private boolean folderPathsUpdated;
	long startTime = 0, currTime = 0;
	
	
 

	public WatchKey getwKey() {
		return wKey;
	}

	public void setwKey(WatchKey wKey) {
		this.wKey = wKey;
	}
	
	public  synchronized void setWatchKeys(WatchKey watchKey, List<WatchEvent<?>> pollEvents) {
		List<WatchEvent<?>> pEvents = null;
		if(watchKeyEventsMap.containsKey(watchKey)) {
			pEvents = watchKeyEventsMap.get(watchKey);
			pEvents.addAll(pollEvents);
		}
		if(pEvents == null) {
			pEvents = pollEvents;
		}
		watchKeyEventsMap.put(watchKey, pEvents);
		
		startTime = System.currentTimeMillis();
		/*
		LinkedHashMap<WatchKey, List<WatchEvent<?>>> wKeysMap = new LinkedHashMap<WatchKey, List<WatchEvent<?>>>();
		wKeysMap.putAll(watchKeyEventsMap);
		if(watchKeyEventsMap.containsKey(watchKey)) {
			if(watchKeyEventsMap.get(watchKey).equals(StandardWatchEventKinds.ENTRY_CREATE)){
				if(pollEvents.get(0).kind().equals(StandardWatchEventKinds.ENTRY_DELETE)){
					wKeysMap.remove(watchKey);
				}
			}else {
				//wKeysMap.put(watchKey, pollEvents);
			}
			
		} else {
			wKeysMap.put(watchKey, pollEvents);
		}
		//watchKeyEventsMap.put(watchKey, pollEvents);
		watchKeyEventsMap.clear();
		
		watchKeyEventsMap.putAll(wKeysMap);*/
		
		
	}

	public void updateFolderPaths() {
		folderPathsUpdated = true;
	}
	
	/*public LinkedHashMap<WatchKey, List<WatchEvent<?>>> getWatchKeyEventsMap() {
		return watchKeyEventsMap;
	}

	public void setWatchKeyEventsMap(
			LinkedHashMap<WatchKey, List<WatchEvent<?>>> watchKeyEventsMap) {
		this.watchKeyEventsMap = watchKeyEventsMap;
	}*/

	public FolderWatcherEventProcessor(WatchService ws, final Map<WatchKey, Path> keys) {
		this.wService = ws;
		this.keys = keys;
	}

	public FolderWatcherEventProcessor(WatchService ws, EventProcessorHelper epHelper) {
		this.wService = ws;
		this.epHelper = epHelper;
	}
	   
	@Override
	public void run() {
		
		LinkedHashMap<WatchKey, List<WatchEvent<?>>> mWatchKeyEventsMap = new LinkedHashMap<WatchKey, List<WatchEvent<?>>>();
		LinkedHashMap<String, Object> monitoredActions = MonitorUtil.getMonitorFolderActions();
		
		while (true) {
			try {
				//Wait for 1 second to see if the batch of events is ready
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(monitoredActions == null || monitoredActions.isEmpty()) {
				monitoredActions = MonitorUtil.getMonitorFolderActions();
			}
			if(monitoredActions != null && !monitoredActions.isEmpty()) {
				currTime = System.currentTimeMillis();
				//Batch the list of events for every 10 seconds and process
				if( (currTime - startTime) > 10000 && !watchKeyEventsMap.isEmpty()) {
					//startTime = currTime;
					
					//Check for folderPaths updated and registers all of the updated folders again if so.
					processUpdatedFolderPaths();
					
					//Copy all the elements from watchKeyEventsMap to a local variable i.e.,mWatchKeyEventsMap 
					//so that watchKeyEventsMap can be updated with the new events by the main thread.
					synchronized (watchKeyEventsMap) {
						mWatchKeyEventsMap.putAll(watchKeyEventsMap);
						watchKeyEventsMap.clear();		
						System.out.println("file events size :" + mWatchKeyEventsMap.size());
					}
					
					//Get updated folder paths to be monitored when its changed from HM UI
					if(folderPathsUpdated) {
						monitoredActions = MonitorUtil.getMonitorFolderActions();
						
						//Mark the folderPathsUpdated flag to false once the updated folder list is retrieved
						folderPathsUpdated = false;
					}
					
					//Start processing the events from mWatchKeyEventsMap
					processEventActions(mWatchKeyEventsMap, monitoredActions);
					
				}
			}
		}
	}

	
	private void processUpdatedFolderPaths() {
		if(folderPathsUpdated) {
			try {
				Path[] foldersToMonitor = MonitorUtil.getFoldersToMonitor();
				
				if(foldersToMonitor != null && foldersToMonitor.length > 0) {
					epHelper.registerFoldersRecursively(foldersToMonitor, epHelper.getEventTypes(), wService);
					//System.out.println(epHelper.keys.size());
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Create a filtered map by removing duplicate events
	 */
	private LinkedHashMap<String, String> processEvents(Map<WatchKey, List<WatchEvent<?>>> watchKeyEvents) {
		LinkedHashMap<String, String> eventActionMap = new LinkedHashMap<>();
		Iterator<WatchKey> itr = watchKeyEvents.keySet().iterator();
		Path filename = null;
		String fName = null;
		
		while(itr.hasNext()) {
			WatchKey wKey = itr.next();
			
			for(WatchEvent<?> fileEvent : watchKeyEvents.get(wKey)) {
				
				WatchEvent.Kind<?> kind = fileEvent.kind();
	            if (kind != OVERFLOW) {
	                @SuppressWarnings("unchecked")
					WatchEvent<Path> event = (WatchEvent<Path>)fileEvent;
	                filename = event.context();
	                fName = wKey.watchable() +"/"+ filename;
	                System.out.println(" Event to be handled : " + kind + " "+ fName);
	                
	                if(eventActionMap.get(fName) == null || 
	                		(!kind.toString().equalsIgnoreCase(eventActionMap.get(fName)) && 
	                				! eventActionMap.get(fName).equalsIgnoreCase("ENTRY_CREATE"))) {
	                	
	                	eventActionMap.put(fName, kind.toString());
	                }
	            }
			}
		}
		System.out.println("========eventActionMap after filtering "+ eventActionMap.size());
		return eventActionMap;
	}
		
		
	/*
	 * Process all the events of different monitor types i.e., CDN, Specific File/Folder, JSP
	 */
	private void processEventActions(Map<WatchKey, List<WatchEvent<?>>> watchKeyEvents, Map<String, Object> segmentsMap) {
		LinkedHashMap<String, String> eventActionMap = processEvents(watchKeyEvents);
		
		Iterator<String> eventActionMapItr = eventActionMap.keySet().iterator();
		String eventActionName = null, eventActionFilePath = null;
		CrawlUtil cUtil = new CrawlUtil();
		int domainId = cUtil.getLiveDomainId(null);
		while(eventActionMapItr.hasNext()) {
			eventActionFilePath = eventActionMapItr.next();
			eventActionName = eventActionMap.get(eventActionFilePath);
			
			Iterator<String> segItr = segmentsMap.keySet().iterator();
			boolean eventProcessed  = false;
            while(segItr.hasNext() && !eventProcessed) {
            	String folderPathRegexKey = (String) segItr.next();
            	eventActionFilePath = eventActionFilePath.replaceAll("\\\\", "/");
            	
            	
            	Matcher match = Pattern.compile(folderPathRegexKey).matcher(eventActionFilePath);
            	//System.out.println("matching regex "+folderPathRegexKey+" with url "+eventActionFilePath);
            	if(match.find()) {
            		System.out.println(folderPathRegexKey+"=============="+eventActionName+"================"+eventActionFilePath+"===========================");
            		if(!eventActionFilePath.startsWith("/") && !eventActionFilePath.contains(":")) {
	            		eventActionFilePath = "/".concat(eventActionFilePath);
	            	}
            		if(segmentsMap.get(folderPathRegexKey) instanceof String) {
            			if(segmentsMap.get(folderPathRegexKey).toString().equalsIgnoreCase("JSP")){
            				epHelper.processJSPAction(eventActionName, eventActionFilePath);
            				
            			} else if(segmentsMap.get(folderPathRegexKey).toString().equalsIgnoreCase("CDN")){
            				//PURGE API CALL ONLY ON STATIC ASSETS DELETE/MODIFY
            				if(!eventActionName.equalsIgnoreCase("ENTRY_CREATE")) {
	            				epHelper.purgeURL(eventActionFilePath);
            				}
            			}
            		} else if(segmentsMap.get(folderPathRegexKey) instanceof List) {
            			epHelper.processSepcificFileFolder(segmentsMap, folderPathRegexKey, cUtil.getDomainName(domainId), eventActionName);
            		
            		}
            		
            		eventProcessed = true;
            		
            	}
            }
            //Event is not matched with any of the monitor types available and is of a new folder is created, then
            //register the new folder recursively.
            if(!eventProcessed) {
            	if(eventActionName.equalsIgnoreCase("ENTRY_CREATE") && new File(eventActionFilePath).isDirectory()) {
            		try {
						epHelper.walk(Paths.get(eventActionFilePath), keys, wService);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            }
        }
		
		//Clear the watchKeyEvents map after all of the elements were processed
		watchKeyEvents.clear();
	}
		
		
}
