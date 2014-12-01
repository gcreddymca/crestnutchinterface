package com.hm.file.monitor;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.vo.MonitorVO;
import com.hm.util.MonitorUtil;

/**
 * Servlet implementation class FolderMonitorServlet
 */
public class FolderMonitorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static WatchService watchService = null;
	private static ExecutorService executor = null;
	Future future = null;
	FolderWatcherEventHandler aWatcher = null;
	static volatile boolean folderPathsUpdated = false;
	private FolderWatcherEventProcessor fwEventProcessor;
	static EventProcessorHelper epHelper = new EventProcessorHelper();
	List<String> errorMessages = new ArrayList<String>();
	CrawlUtil cUtil = new CrawlUtil();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FolderMonitorServlet() {
        super();
    }
    
    /* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			executor = Executors.newCachedThreadPool();
			if(MonitorUtil.getFoldersToMonitor()!=null){
				startWatchingFolders(MonitorUtil.getFoldersToMonitor(), MonitorUtil.getListOfEvents());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.init();
	}

    

	public void addFolderListener(Path[] path, Kind<?>[] events) throws IOException, InterruptedException {
		fwEventProcessor = new FolderWatcherEventProcessor(watchService, epHelper);
		new Thread(fwEventProcessor, "EventProcessor thread").start();
		
		aWatcher = new FolderWatcherEventHandler(path, events, watchService, fwEventProcessor);
		future = executor.submit(aWatcher);
	}
	
	public void startWatchingFolders(Path[] paths, Kind<?>[] events) throws IOException {		
		try {
			addFolderListener(MonitorUtil.getFoldersToMonitor(), MonitorUtil.getListOfEvents());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}


	public void refreshFolderPaths() {
		if(fwEventProcessor != null) {
		   fwEventProcessor.updateFolderPaths();
		}
	}
	
	public void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException{
				
		String event = request.getParameter("event");
		//String domainId = request.getParameter("domainId");
		int domainId = cUtil.getLiveDomainId(null);
		String segmentsList = null;
		
		if(event.equalsIgnoreCase("Save")){
			errorMessages.clear();
			//List<MonitorVO> monitorList = new ArrayList<MonitorVO>();
			MonitorVO monitorVO = new MonitorVO();
			Map<String,String> staticRegFolderMap = new HashMap<>();
			String cdnBlocks = request.getParameter("cdnBlocks");
			
			
			String[] refreshSegmentsList = request.getParameterValues("refreshSegmentList");
			//getting segmentd IDS by passing segment names
			if(refreshSegmentsList != null){
			for (String segment : refreshSegmentsList) {
				if(segmentsList == null){
					segmentsList = segment;
				}else{
					segmentsList = segmentsList+","+segment;
				}
			}
			monitorVO.setRefreshSegmentList(Arrays.asList(refreshSegmentsList));
			}
			
			if (request.getParameterValues("refreshUrls") != null) {
				monitorVO.setRefreshUrls(Arrays.asList(request
						.getParameterValues("refreshUrls")));
			}
			monitorVO.setDocRoot(request.getParameter("docRoot"));
			monitorVO.setSpecificFileregEx(request
					.getParameter("specificFileregEx"));
			monitorVO.setSpecificFilePath(request
					.getParameter("specificFilePath"));

			// monitorVO.setJspregEx(request.getParameter("jspRegex"));
			String[] jspFolderPathList = request
					.getParameterValues("jspFolderPath_0");
			String[] extensions = request.getParameterValues("cdnregexType");
			String[] foldertypes = request.getParameterValues("cdnfolderType");
			String staticextensions = cUtil.getStaticRegEx();
			if (extensions != null) {
				for (String extension : extensions) {
					if(extension != null && extension != ""){
					String[] regexes = extension.split(" ");
					for (String regex : regexes) {
						if (staticextensions == null || staticextensions.isEmpty()) {
							if (regex != null && regex != "") {
								staticextensions = ".*\\.("+regex;
							}
						} else {
							if(staticextensions.contains(")")){
							staticextensions = staticextensions.substring(0,staticextensions.indexOf(")"));
							if (regex != null && regex != "") {
								staticextensions = staticextensions + "|"+ regex;
							}
						  } else{
							  staticextensions = staticextensions + "|"+ regex;
						  }
					  }		
				   }
				   staticextensions = staticextensions+")$";	
				}
			  }
			}	
			if (staticextensions != null) {
				monitorVO.setStaticregEx(staticextensions);
			}
			if (foldertypes != null) {
				monitorVO.setStaticFolderPath(Arrays.asList(foldertypes));
			}
			if (jspFolderPathList != null) {
				monitorVO.setJspFolderPath(Arrays.asList(jspFolderPathList));
			}

			if (!checkValidations(monitorVO)) {
				request.removeAttribute("errorMessage");
				request.setAttribute("errorMessage", errorMessages);
				request.setAttribute("errorMessage", errorMessages);
				request.getRequestDispatcher("/processForm?event=monitorView").forward(request, response);
			}else{
				cUtil.createMonitorPath(monitorVO);
				errorMessages.add("Successfully inserted into DB..");
				refreshFolderPaths();
				request.setAttribute("errorMessage", errorMessages);
				request.getRequestDispatcher("/processForm?event=monitorView").forward(request, response);
			} 
			
		}else if(event.equalsIgnoreCase("delete")){
			errorMessages.clear();
			String regex = request.getParameter("regex");
			String folderPath = request.getParameter("folderPath");
			String jspfolderPath = request.getParameter("jspPath");
			String specificRegex = request.getParameter("specificRegex");
			
			if (regex != null) {
				cUtil.deleteStaticRegex(regex);
			}
			
			if(folderPath != null){
				cUtil.deleteStaticFolderPath(folderPath);
			}
			
			if(jspfolderPath != null){
				cUtil.deleteJspPath(jspfolderPath);
			}
			if(specificRegex != null && specificRegex != ""){
				cUtil.deleteSpecificPathEntry(specificRegex);
			}
			errorMessages.add("Successfully deleted from DB..");
			request.setAttribute("errorMessage", errorMessages);
			request.getRequestDispatcher("/processForm?event=monitorView").forward(request, response);
	    }
	}
	
	public boolean checkValidations(MonitorVO monitorVO) {
		try {
			if(monitorVO.getSpecificFileregEx() != null){
			Pattern.compile(monitorVO.getSpecificFileregEx());
			}
		} catch (Exception pEx) {
			errorMessages.add(pEx.getMessage());
		}
		if (errorMessages.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

}
