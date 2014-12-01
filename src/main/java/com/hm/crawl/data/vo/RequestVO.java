package com.hm.crawl.data.vo;

public class RequestVO {

	private int requestId;
	private int domainId;
	private String event;
	private String checkedSegments;
	private String url;
	private String segmentId;
	private boolean isApi;
	private String checkedUrls;
	private int trackId;
	private String all;
	private String recursive;
	private String status;
	private String siteKey;
	private String domainList;
	
	
	public int getRequestId() {
		return requestId;
	}
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	public int getDomainId() {
		return domainId;
	}
	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getCheckedSegments() {
		return checkedSegments;
	}
	public void setCheckedSegments(String checkedSegments) {
		this.checkedSegments = checkedSegments;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSegmentId() {
		return segmentId;
	}
	public void setSegmentId(String segmentId) {
		this.segmentId = segmentId;
	}
	public boolean isApi() {
		return isApi;
	}
	public void setApi(boolean isApi) {
		this.isApi = isApi;
	}
	public String getCheckedUrls() {
		return checkedUrls;
	}
	public void setCheckedUrls(String checkedUrls) {
		this.checkedUrls = checkedUrls;
	}
	public int getTrackId() {
		return trackId;
	}
	public void setTrackId(int trackId) {
		this.trackId = trackId;
	}
	public String getAll() {
		return all;
	}
	public void setAll(String all) {
		this.all = all;
	}
	public String getRecursive() {
		return recursive;
	}
	public void setRecursive(String recursive) {
		this.recursive = recursive;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the siteKey
	 */
	public String getSiteKey() {
		return siteKey;
	}
	/**
	 * @param siteKey the siteKey to set
	 */
	public void setSiteKey(String siteKey) {
		this.siteKey = siteKey;
	}
	public String getDomainList() {
		return domainList;
	}
	public void setDomainList(String domainList) {
		this.domainList = domainList;
	}
	
}
