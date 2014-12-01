package com.hm.crawl.data.vo;

import java.util.List;

public class MonitorVO {

	public String getSpecificFileregEx() {
		return specificFileregEx;
	}
	public void setSpecificFileregEx(String specificFileregEx) {
		this.specificFileregEx = specificFileregEx;
	}
	public String getSpecificFilePath() {
		return specificFilePath;
	}
	public void setSpecificFilePath(String specificFilePath) {
		this.specificFilePath = specificFilePath;
	}
	public List<String> getRefreshSegmentList() {
		return refreshSegmentList;
	}
	public void setRefreshSegmentList(List<String> refreshSegmentList) {
		this.refreshSegmentList = refreshSegmentList;
	}
	public List<String> getRefreshUrls() {
		return refreshUrls;
	}
	public void setRefreshUrls(List<String> refreshUrls) {
		this.refreshUrls = refreshUrls;
	}
	
	public List<String> getJspFolderPath() {
		return jspFolderPath;
	}
	public void setJspFolderPath(List<String> jspFolderPath) {
		this.jspFolderPath = jspFolderPath;
	}
	public String getStaticregEx() {
		return staticregEx;
	}
	public void setStaticregEx(String staticregEx) {
		this.staticregEx = staticregEx;
	}
	
	public List<String> getStaticFolderPath() {
		return staticFolderPath;
	}
	public void setStaticFolderPath(List<String> staticFolderPath) {
		this.staticFolderPath = staticFolderPath;
	}

	public String getDocRoot() {
		return docRoot;
	}
	public void setDocRoot(String docRoot) {
		this.docRoot = docRoot;
	}

	private String staticregEx;
	private List<String> staticFolderPath;
	private String specificFileregEx;
	private String specificFilePath;
	private List<String> refreshSegmentList;
	private List<String> refreshUrls;
	
	private List<String> jspFolderPath;
	private String docRoot;

}
