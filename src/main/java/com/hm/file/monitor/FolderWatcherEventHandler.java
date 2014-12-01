package com.hm.file.monitor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FolderWatcherEventHandler implements Runnable {
	private WatchKey watchKey = null;
	private WatchService wService = null;
	private EventProcessorHelper epHelper = new EventProcessorHelper();
	final Map<WatchKey, Path> keys = new ConcurrentHashMap<>();
	private FolderWatcherEventProcessor fwEventProcessor;
	
	
	
	protected FolderWatcherEventHandler(Path[] paths, Kind<?>[] events, 
			WatchService watchService, FolderWatcherEventProcessor fwEventProcessor) throws IOException, InterruptedException {
		epHelper.registerFoldersRecursively(paths, events, watchService);
		wService = watchService;
		this.fwEventProcessor = fwEventProcessor;
	}
	
	
	
	
	@Override
	public void run() {
		//Thread.currentThread().setName(threadName);
		boolean valid = true;
		
		List<WatchEvent<?>> pollEvents = null;
		
		while(valid){
			try {
				//Waiting for file events 
				watchKey = wService.take();
				/*if(fwEventProcessor == null) {
					fwEventProcessor = new FolderWatcherEventProcessor(wService, keys);
					new Thread(fwEventProcessor, "EventProcessor thread").start();
				}*/
			} catch (InterruptedException e) {
				// TODO log error message
				e.printStackTrace();
			}
			pollEvents = watchKey.pollEvents();
			
			//Update the FolderWatcherEventProcessor.watchKeyEventsMap with new pollEvents and watch key
			synchronized (fwEventProcessor) {
				fwEventProcessor.setWatchKeys(watchKey, pollEvents);
				System.out.println("poll events size :" + pollEvents.size());
			}
			
			
			//Check is the watch key is valid and reset, if not remove this watch key from the list of keys
			if (watchKey.reset() == false) {
				System.out.printf("%s is invalid %n", watchKey);
				epHelper.keys.remove(watchKey);
				
			}
		}
		
		
	}

	
	
		
}
