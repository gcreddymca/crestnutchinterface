package com.hm.crawl.data.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Value Object Class for Segment.
 * 
 */
public class SegmentVO {
	
	private int segmentId;
	private String segmentName;
	private String url_pattern_rule;
	private boolean crawl;
	private int domainId;
	private Map<String,String> urlHtmlLocMap;
	private List<SegmentVO> listsegmentVO;
	private String priority;
	private Map pathVO;
	private String urlType;
	private long crawlInterval;
	private long nextFetchTime;
	private List<TransformationVO> transformationVO;
	private List<EditTransformationVO> editTransformVO;
	private int htmlPageCount;
	private int crawledUrlCount;
	private List<UrlHtmlLocVO> urlHtmlLocVO;
	private String purgeUrl;
	
	public int getSegmentId() {
		return segmentId;
	}
	public void setSegmentId(int segmentId) {
		this.segmentId = segmentId;
	}
	public String getSegmentName() {
		return segmentName;
	}
	public void setSegmentName(String segmentName) {
		this.segmentName = segmentName;
	}
	
	public boolean isCrawl() {
		return crawl;
	}
	public void setCrawl(boolean crawl) {
		this.crawl = crawl;
	}
	
	public Map<String,String> getUrlHtmlLocMap() {
		return urlHtmlLocMap;
	}
	public void setUrlHtmlLocMap(Map<String,String> urlHtmlLocMap) {
		this.urlHtmlLocMap = urlHtmlLocMap;
	}
	/**
	 * @return the listsegmentVO
	 */
	public List<SegmentVO> getListsegmentVO() {
		return listsegmentVO;
	}
	/**
	 * @param listsegmentVO the listsegmentVO to set
	 */
	public void setListsegmentVO(List<SegmentVO> listsegmentVO) {
		this.listsegmentVO = listsegmentVO;
	}
	/**
	 * @return the priority
	 */
	public String getPriority() {
		return priority;
	}
	/**
	 * @param priority the priority to set
	 */
	public void setPriority(String priority) {
		this.priority = priority;
	}

	public int getDomainId() {
		return domainId;
	}
	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}
	/**
	 * @return the pathVO
	 */
	public Map getPathVO() {
		return pathVO;
	}
	/**
	 * @param pathVO the pathVO to set
	 */
	public void setPathVO(Map pathVO) {
		this.pathVO = pathVO;
	}
	public String getUrlType() {
		return urlType;
	}
	public void setUrlType(String urlType) {
		this.urlType = urlType;
	}
	public long getCrawlInterval() {
		return crawlInterval;
	}
	public void setCrawlInterval(long crawlInterval) {
		this.crawlInterval = crawlInterval;
	}
	public long getNextFetchTime() {
		return nextFetchTime;
	}
	public void setNextFetchTime(long nextFetchTime) {
		this.nextFetchTime = nextFetchTime;
	}
	public String getUrl_pattern_rule() {
		return url_pattern_rule;
	}
	public void setUrl_pattern_rule(String url_pattern_rule) {
		this.url_pattern_rule = url_pattern_rule;
	}
	/**
	 * @return the transformationVO
	 */
	public List<TransformationVO> getTransformationVO() {
		return transformationVO;
	}
	/**
	 * @param transformationVO the transformationVO to set
	 */
	public void setTransformationVO(List<TransformationVO> transformationVO) {
		this.transformationVO = transformationVO;
	}
	/**
	 * @return the editTransformVO
	 */
	public List<EditTransformationVO> getEditTransformVO() {
		return editTransformVO;
	}
	/**
	 * @param editTransformVO the editTransformVO to set
	 */
	public void setEditTransformVO(List<EditTransformationVO> editTransformVO) {
		this.editTransformVO = editTransformVO;
	}
	/**
	 * @return the htmlPageCount
	 */
	public int getHtmlPageCount() {
		return htmlPageCount;
	}
	/**
	 * @param htmlPageCount the htmlPageCount to set
	 */
	public void setHtmlPageCount(int htmlPageCount) {
		this.htmlPageCount = htmlPageCount;
	}
	/**
	 * @return the urlHtmlLocVO
	 */
	public List<UrlHtmlLocVO> getUrlHtmlLocVO() {
		if(urlHtmlLocVO == null)
			urlHtmlLocVO = new ArrayList<UrlHtmlLocVO>();
		return urlHtmlLocVO;
	}
	/**
	 * @param urlHtmlLocVO the urlHtmlLocVO to set
	 */
	public void setUrlHtmlLocVO(List<UrlHtmlLocVO> urlHtmlLocVO) {
		this.urlHtmlLocVO = urlHtmlLocVO;
	}
	/**
	 * @return the crawledUrlCount
	 */
	public int getCrawledUrlCount() {
		return crawledUrlCount;
	}
	/**
	 * @param crawledUrlCount the crawledUrlCount to set
	 */
	public void setCrawledUrlCount(int crawledUrlCount) {
		this.crawledUrlCount = crawledUrlCount;
	}
	public String getPurgeUrl() {
		return purgeUrl;
	}
	public void setPurgeUrl(String purgeUrl) {
		this.purgeUrl = purgeUrl;
	}
	
}
