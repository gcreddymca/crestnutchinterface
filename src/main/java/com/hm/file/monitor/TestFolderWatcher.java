/*package com.hm.file.monitor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;


public class TestFolderWatcher {
	public void testFutureWatchService() throws IOException {
		Kind<?>[] events = new Kind<?>[3];
		events[0] = ENTRY_CREATE;
		events[1] = ENTRY_MODIFY;
		events[2] = ENTRY_DELETE;
		FolderWatcher.getInstance().addFolderListener(Paths.get("c:/temp/dir1"),
				new ChangeListener(events) {
					@Override
					public void onEvent(WatchEvent<Path> anEvent) {
						System.out.println("LISTENER 1 " + anEvent.kind().name().toString()
								+ " " + anEvent.context());
					}
				});
		FolderWatchers.getInstance().addFolderListener(Paths.get("C:/temp/dir1/dummy"),
				new ChangeListener(events) {
					@Override
					public void onEvent(WatchEvent<Path> anEvent) {
						System.out.println("LISTENER 2 " + anEvent.kind().name().toString()
								+ " " + anEvent.context());
					}
				});
		while (true) {
			;
		}
	}
}*/