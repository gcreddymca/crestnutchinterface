package com.hm.crawl.automate.events;

import com.hm.crawl.automate.AutoCrawlerThread;



public interface ThreadCompleteListener {
	 void notifyOfThreadComplete(final AutoCrawlerThread autoCrawlerThread);

	
}
