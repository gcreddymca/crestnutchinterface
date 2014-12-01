package com.hm.file.monitor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.nutch.crawl.dao.UrlDAO;
import org.apache.nutch.crawl.vo.UrlVO;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.purge.PurgeEdgeCastData;
import com.hm.util.MonitorUtil;

public class EventProcessorHelper {
	private static final String QA_HOST = "http://qa.plantronics.com";
	//private static final String DOC_ROOT = "/usr/local/teamsite/www/STAGING/Plantronics/doc";
	final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
	
	public void registerFoldersRecursively(Path[] paths, Kind<?>[] events, WatchService watchService) throws IOException {
		for(int i =0; i< paths.length; i++) {
			try{
				if(paths[i] != null) {
					walk(paths[i], keys, watchService);
				}
			}catch(Exception e){
				
			}
		}
	}
	
	void walk(Path root, final Map<WatchKey, Path> keys1,
			final WatchService ws) throws IOException {
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
				register(dir, keys, ws);
				return super.preVisitDirectory(dir, attrs);
			}
		});
	}
	
	private void register(Path dir, Map<WatchKey, Path> keys1, WatchService ws)
			throws IOException {
		System.out.println(dir.toAbsolutePath() +" is being registered");
		WatchKey key = dir.register(ws, getEventTypes());
		keys.put(key, dir);
	}
	
	public Kind<?>[] getEventTypes () {
		Kind<?>[] eventTypes = new Kind<?>[3];
		eventTypes[0] = ENTRY_CREATE;
		eventTypes[1] = ENTRY_MODIFY;
		eventTypes[2] = ENTRY_DELETE;
		return eventTypes;
	}
   
	
	
	
	public void processJSPAction(String eventActionName, String eventActionFilePath) {
		CrawlUtil cUtil = new CrawlUtil();
		//int domainId = cUtil.getLiveDomainId(null);
		String url = null;
		
		int domainId =0;
		eventActionFilePath = eventActionFilePath.replace(CrawlUtil.getDocRootPath(), "");
		List<String> urlTokenList = new ArrayList<String>();
		String[] urlTokens = eventActionFilePath.split("/");
		urlTokenList = Arrays.asList(urlTokens);
		String token1 = urlTokenList.get(1);
		String token2 = urlTokenList.get(2);
		if(!token2.contains(".jsp")){
			url="/"+token1+"/"+token2;
			domainId = cUtil.getJspDomainId(url);
		}
		if(domainId == 0){
			url="/"+token1;
			domainId = cUtil.getJspDomainId(url);
		}
		
		int crawlId = cUtil.getCrawlId(null, domainId);
		
		if(eventActionName.equalsIgnoreCase("ENTRY_CREATE") && !MonitorUtil.isUrlExists(eventActionFilePath, crawlId)) {
    		Map<Integer,String> segsMap = MonitorUtil.getCrawlSegmentIds(domainId);
    		Iterator<Integer> segsItr = segsMap.keySet().iterator();
    		
            while(segsItr.hasNext()) {
            	Integer segsKey = (Integer) segsItr.next();
            	Matcher match = Pattern.compile(segsMap.get(segsKey).toString()).matcher(eventActionFilePath);
            	if(match.find()) {
            		System.out.println("NEW JSP ADDED ...");
            		
            		//Create an entry into URL_DETAIL TABLE
            		UrlVO urlVO = new UrlVO();
            		
            		if(eventActionFilePath.endsWith("/index.jsp")){
            			eventActionFilePath = eventActionFilePath.replace("index.jsp", "");
            		}
            		urlVO.setUrl(eventActionFilePath);
            		urlVO.setSegmentId(segsKey);
            		new UrlDAO().createSpecificUrlDetail(urlVO, crawlId, domainId);
            		
            		//CREATE RESPECTIVE ENTRY INTO URL_HTML_LOC TABLE
            		try {
            			new SegmentMasterCRUD().generateURLforSegment(segsKey.toString(), String.valueOf(domainId), crawlId, eventActionFilePath);
            			
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		refreshUrlApiCall(eventActionFilePath, cUtil.getDomainName(domainId));
            		
            		break;
            	}
            }
    	}
		
		if(eventActionName.equalsIgnoreCase("ENTRY_DELETE")) {
			deleteUrlApiCall(eventActionFilePath,cUtil.getDomainName(domainId));
			if(eventActionFilePath.endsWith(".jsp")) {
				eventActionFilePath = eventActionFilePath.replace(".jsp", ".html");
			}else {
				eventActionFilePath = eventActionFilePath + "index.html";
			}
			//PURGE API CALL FOR  DELETE EVENT
			purgeURL(eventActionFilePath);
			
		} else if(eventActionName.equalsIgnoreCase("ENTRY_MODIFY")){		
			//REFRESH URL API CALL
			refreshUrlApiCall(eventActionFilePath, cUtil.getDomainName(domainId));
			if(eventActionFilePath.endsWith(".jsp")) {
				eventActionFilePath = eventActionFilePath.replace(".jsp", ".html");
			}else {
				eventActionFilePath = eventActionFilePath + "index.html";
			}
			//PURGE API CALL FOR  MODIFY EVENT
			purgeURL(eventActionFilePath);
		}
	}
	
	private void deleteUrlApiCall(String urlToDelete, String domainName) {
		StringBuilder apiUrl = new StringBuilder();
		apiUrl.append("domainName=").append(domainName)
		.append("&event=deleteApiConfirm&isApi=true&isThreadWait=true&url=").append(urlToDelete);
		
		URL url;
		try {
			url = getURIConstructed(apiUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			System.out.println(urlConnection.getResponseCode());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("DELETE URL API CALL - "+ urlToDelete);
	}
	
	private void refreshUrlApiCall(String url, String domainName) {
		List<String> urls = new ArrayList<String>();
		urls.add(url);
		refreshUrlApiCall(urls, domainName);
	}
	
	
	private void refreshUrlApiCall(List<String> urlsList, String domainName) {
		String urlToRefresh = getCommaSeparatedUrls(urlsList);
		urlToRefresh = urlToRefresh.replaceAll(CrawlUtil.getDocRootPath(), "");
		if(urlToRefresh.endsWith("/index.jsp")){
			urlToRefresh = urlToRefresh.replace("index.jsp", "");
		}
		StringBuilder apiUrl = new StringBuilder();
		apiUrl.append("domainName=").append(domainName)
		.append("&event=refreshSelectedURLSConfirm&isApi=true&isThreadWait=true&apiselectedURLS=").append(urlToRefresh);
		System.out.println("Url being refreshed from api call :"+apiUrl.toString());
		URL url;
		try {
			url = getURIConstructed(apiUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			System.out.println(urlConnection.getResponseCode());
		} catch (MalformedURLException e) {
			// TODO log the error message
			System.out.println("Error while connecting to "+ apiUrl.toString());
		} catch (IOException e) {
			// TODO log the error message
			System.out.println("Error while connecting to "+ apiUrl.toString());
		}
		
		//Purge list of urls
		//purgeURLs(urlsList);
	}
	
	
	public void purgeURLs(List<String> urls) {
		for(String purgeUrl : urls) {
			purgeURL(purgeUrl);
		}
	}
	
	
	public void purgeURL(String url) {
		String DOC_ROOT = CrawlUtil.getDocRootPath();
		if(url != null && !url.contains(":")) {
			if(url.startsWith(DOC_ROOT)) {
				System.out.println(url+ " STARTS WITH "+ DOC_ROOT);
				url = url.replace(DOC_ROOT, "");
				System.out.println(url + " AFTER STRIPPING OFF "+ DOC_ROOT);
			}
			PurgeEdgeCastData purgeECD = new PurgeEdgeCastData();
			String mediaPath = QA_HOST + url;
			CrawlUtil.purgeAddTimeStamptoURL(mediaPath);
			String respCode = purgeECD.getPurgeContentId(mediaPath);
			if(respCode!=null){
	        	CrawlUtil.purgeUpdateTimeStamptoURL(mediaPath,respCode);
	        }
			System.out.println("PURGE API CALL for URL "+ mediaPath+ ":  Response Code "+ respCode);
		}
	}
	
	
	public void processSepcificFileFolder(Map<String, Object> segmentsMap, String folderPathRegexKey, String domainName, String eventActionName) {
		if(eventActionName.equalsIgnoreCase("ENTRY_MODIFY")) {
			@SuppressWarnings("unchecked")
			List<Object> segsUrlsList = (List<Object>) segmentsMap.get(folderPathRegexKey);
			Set<String> domainNameSet = new HashSet<String>();
			List<String> urlSegmentsList = new ArrayList<String>();
			Map<String, String> segmentNamesMap = new HashMap<String, String>();
			for(Object segOrUrlList :  segsUrlsList) {
				if(segOrUrlList instanceof Map) {
				//getting domainNameList from segOrUrlList()
					domainNameSet = ((Map) segOrUrlList).keySet();
					for (String domain : domainNameSet) {
						urlSegmentsList = (List<String>) ((Map) segOrUrlList).get(domain);
						if(urlSegmentsList.get(0).contains("/")) {
							refreshUrlApiCall(urlSegmentsList, domain);
							purgeURLs(urlSegmentsList);
						} else {
							segmentNamesMap = MonitorUtil.getSegmentNamesAndPurgeUrls(getCommaSeparatedSegments((List<String>) urlSegmentsList));
							//refreshSegmentsApiCall(segmentNames, domainName);
							String segNames = getCommaSeparatedSegments(new ArrayList<String>(segmentNamesMap.keySet()));
							refreshSegmentsApiCall(segNames, domain);
							//purgeURLs((List<String>) segmentNamesMap.values());
							for(String purgeUrl : segmentNamesMap.values()) {
								purgeURL(purgeUrl);
							}
						}
					}
				}
			}
			
		}
	}
	
	private String getCommaSeparatedUrls(List<String> urlList) {
		StringBuilder urlBuilder = new StringBuilder();
		String urls = null;
		for(String url : urlList) {
			urlBuilder.append(url).append(",");
		}
		
		urls = urlBuilder.toString();
		if(urls.endsWith(",")) {
			urls = urls.substring(0, urls.length()-1);
		}
		
		return urls;
	}
	
	private String getCommaSeparatedSegments(List<String> segmentList) {
		StringBuilder segBuilder = new StringBuilder();
		String segs = null;
		for(String seg : segmentList) {
			segBuilder.append(seg).append(",");
		}
		
		segs = segBuilder.toString();
		if(segs.endsWith(",")) {
			segs = segs.substring(0, segs.length()-1);
		}
		
		return segs;
	}
	
	private String getCommaSeparatedSegmentNames(List<String> segmentList) {
		StringBuilder segBuilder = new StringBuilder();
		String segs = null;
		for(String seg : segmentList) {
			segBuilder.append(seg).append(",");
		}
		
		segs = segBuilder.toString();
		if(segs.endsWith(",")) {
			segs = segs.substring(0, segs.length()-1);
		}
		
		return segs;
	}
	
	/**
	 * Refresh list of segments and purge their respective purge urls
	 */
	private void refreshSegmentsApiCall(Map<String, String> segmentsMap, String domainName) {
		String segNames = getCommaSeparatedSegments(new ArrayList<String>(segmentsMap.keySet()));
		refreshSegmentsApiCall(segNames, domainName);
		
		for(String purgeUrl : segmentsMap.values()) {
			purgeURL(purgeUrl);
		}
	}
	
	
	private void refreshSegmentsApiCall(String segsToRefresh, String domainName) {
		StringBuilder apiUrl = new StringBuilder();
		apiUrl.append("domainName=").append(domainName)
		.append("&event=refreshSelectedSegmentsConfirm&isApi=true&isThreadWait=true&segmentNames=").append(segsToRefresh);
		
		URL url;
		try {
			url = getURIConstructed(apiUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			System.out.println(urlConnection.getResponseCode());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//System.out.println("REFRESH SEGMENT API CALL - "+ segsToRefresh);
	}
	
	private URL getURIConstructed(StringBuilder queryString) {
		URL url = null;
		try {
			URI uri = new URI("http", "localhost:9080", "/hm/processForm", queryString.toString(), null);
			url = uri.toURL();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return url;

		
	}
	
}