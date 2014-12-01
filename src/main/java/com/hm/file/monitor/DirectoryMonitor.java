package com.hm.file.monitor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.sun.nio.file.ExtendedWatchEventModifier;

public class DirectoryMonitor {
	
	public static void main(String args[]) throws IOException, InterruptedException {
		
		Path path = Paths.get("c:/temp/dir1");
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Kind<?>[] events = new Kind<?>[3];
		events[0] = ENTRY_CREATE;
		events[1] = ENTRY_MODIFY;
		events[2] = ENTRY_DELETE;
		
		//path.register(watchService,ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		path.register(watchService, events, ExtendedWatchEventModifier.FILE_TREE);

		boolean notDone = true;
		WatchKey watchKey;
		             do {
		                 watchKey = watchService.take();
		                 final Path eventDir = path;
		                 for (final WatchEvent<?> event : watchKey.pollEvents()) {
		                     final WatchEvent.Kind kind = event.kind();
		                     final Path eventPath = (Path) event.context();
		                     System.out.println(eventDir + ": " + event.kind() + ": " + event.context());
		                 }
		             } while (watchKey.reset());
		/*//WatchKey watchable returns the calling Path object of Path.register
		Path watchedPath = (Path) watchKey.watchable();
		//returns the event type
		StandardWatchEventKinds eventKind = event.kind();
		//returns the context of the event
		Path target = (Path)event.context();*/
	}

}
