package com.hm.file.monitor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.hm.util.MonitorUtil;


public class FolderWatcher {

	private static FolderWatcher watcher = null;
	private static WatchService watchService = null;
	Future future = null;
	private static final ExecutorService executor = Executors.newCachedThreadPool();
	static FolderWatcherEventHandler aWatcher = null; 
	static FolderWatcherEventProcessor fwEventProcessor = null;
	static EventProcessorHelper epHelper = new EventProcessorHelper();

	private FolderWatcher() throws IOException{
		watchService = FileSystems.getDefault().newWatchService();
	}
	
	public static FolderWatcher getInstance() throws IOException {
		if (null == watcher){
			watcher = new FolderWatcher();
		}
		return watcher;
	}
	
	
	public void addFolderListener(Path[] path, Kind<?>[] events) throws IOException, InterruptedException {
		
		fwEventProcessor = new FolderWatcherEventProcessor(watchService, epHelper);
		new Thread(fwEventProcessor, "EventProcessor thread").start();
		
		aWatcher = new FolderWatcherEventHandler(path, events, watchService, fwEventProcessor);
		future = executor.submit(aWatcher);
	}
	
	public void startWatchingFolders(Path[] paths, Kind<?>[] events) throws IOException {		
		try {
			addFolderListener(paths, events);
						
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block	e.printStackTrace();
		}
	}
	
	public void stopMonitoring() {
		executor.shutdown();
	}
	
	public Path[] getPathsToMonitor() {
		Path[] paths = MonitorUtil.getFoldersToMonitor();
		if(paths == null || paths.length == 0) {
			paths = new Path[10];
			paths[0] = Paths.get("E:/NVIZ/PLT/jboss-eap-4.2/jboss-as/server/atg2/deploy/Plantronics.ear/plt_estore.war");
		}
		
		return paths;
	}
	
	public static void main(String args[]) throws IOException {
		
		Kind<?>[] events = new Kind<?>[3];
		events[0] = ENTRY_CREATE;
		events[1] = ENTRY_MODIFY;
		events[2] = ENTRY_DELETE;
		
		FolderWatcher fWatcher = FolderWatcher.getInstance();
		fWatcher.startWatchingFolders(fWatcher.getPathsToMonitor(), events);
		
		
		
		try {
			Thread.sleep(10000);
			//aWatcher.folderPathsUpdated = true;
			fwEventProcessor.updateFolderPaths();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}
}



