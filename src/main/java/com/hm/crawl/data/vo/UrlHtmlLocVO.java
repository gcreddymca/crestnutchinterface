package com.hm.crawl.data.vo;

public class UrlHtmlLocVO {
	 
	private String url;
	private String urlLoc;
	private String lastFetchedTime;
	private int htmlFileStatus;
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return the urlLoc
	 */
	public String getUrlLoc() {
		return urlLoc;
	}
	/**
	 * @param urlLoc the urlLoc to set
	 */
	public void setUrlLoc(String urlLoc) {
		this.urlLoc = urlLoc;
	}
	/**
	 * @return the lastFetchedTime
	 */
	public String getLastFetchedTime() {
		return lastFetchedTime;
	}
	/**
	 * @param lastFetchedTime the lastFetchedTime to set
	 */
	public void setLastFetchedTime(String lastFetchedTime) {
		this.lastFetchedTime = lastFetchedTime;
	}
	/**
	 * @return the htmlFileStatus
	 */
	public int getHtmlFileStatus() {
		return htmlFileStatus;
	}
	/**
	 * @param htmlFileStatus the htmlFileStatus to set
	 */
	public void setHtmlFileStatus(int htmlFileStatus) {
		this.htmlFileStatus = htmlFileStatus;
	}
}
