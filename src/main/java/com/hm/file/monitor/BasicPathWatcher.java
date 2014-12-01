package com.hm.file.monitor;

import java.io.File;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

import com.sun.nio.file.ExtendedWatchEventModifier;

import static java.nio.file.StandardWatchEventKinds.*;

public class BasicPathWatcher {
    public static void main(String[] args) throws Exception {
        //the path to watch; we assume no recursive watching (would be even more complicated)
        Path pathToWatch = Paths.get("c:/temp/dir1");
        Path pathToWatch2 = Paths.get("c:/temp/dir2");
        Path pathToWatch3 = Paths.get("c:/temp/dir3");
        Path[] paths = new Path[3];
        paths[0] = pathToWatch;
		paths[1] = pathToWatch2;
		paths[2] = pathToWatch3;
		
		Kind<?>[] events = new Kind<?>[3];
		events[0] = ENTRY_CREATE;
		events[1] = ENTRY_MODIFY;
		events[2] = ENTRY_DELETE;
		
		
        WatchService ws = pathToWatch.getFileSystem().newWatchService(); // 1.
        // we do not care about the returned WatchKey in this simple example
        pathToWatch.register(ws, events, ExtendedWatchEventModifier.FILE_TREE); //2.
        pathToWatch2.register(ws, events, ExtendedWatchEventModifier.FILE_TREE); //2.
        pathToWatch3.register(ws, events, ExtendedWatchEventModifier.FILE_TREE); //2.
        
        boolean valid = true;
        while(valid) {
            WatchKey key = ws.take(); //3.
            for (WatchEvent <?> e: key.pollEvents()) { //4.
                WatchEvent.Kind<?> kind = e.kind();
                if (kind != OVERFLOW) {
                    WatchEvent<Path> event = (WatchEvent<Path>)e;
                    Path filename = event.context();
                    Path child = pathToWatch.resolve(filename);
                    System.out.println("Got event '" + kind + "' for path '" + child + "'");
                }
            }
            valid = key.reset(); //5.
        }
    }
}