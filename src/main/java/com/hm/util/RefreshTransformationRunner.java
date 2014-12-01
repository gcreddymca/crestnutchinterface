package com.hm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.tools.JDBCConnector;
import org.apache.nutch.util.NutchConfiguration;

import org.apache.nutch.util.URLTransformationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.automate.CrawlUtil;
import com.hm.crawl.data.DomainMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.TransformationVO;

public class RefreshTransformationRunner implements Runnable{

	public static final Logger LOG = LoggerFactory.getLogger(RefreshTransformationRunner.class);
	
	private String domainUrl;
	private String url;
	private String final_content_path;
	private String raw_content_path;
	private Map<String, String> urlHtmlLoc;
	private String urlType;
	private List<TransformationVO> transformations;
	private int crawlId;
	private int domainId;
	private static final Configuration conf;
	private static Pattern[] requestParamsExclusionPatterns =null;
	private static final String newLine = System.getProperty("line.separator").toString();
	private static StatusCodeMessages statusMessages = new StatusCodeMessages();
	
	static {
		conf = NutchConfiguration.create();
		String[] exclusionList = conf.getStrings("url.request.parameter.exclusion.list");
		if(exclusionList != null){
		requestParamsExclusionPatterns = new Pattern[exclusionList.length];
		StringBuilder pattern = null;
		for (int i = 0; i < exclusionList.length; i++) {
			pattern = new StringBuilder();
			pattern.append("[;&]*");
			pattern.append(exclusionList[i]);
			pattern.append("[\\w%/.]*[=\\w-/.+%]*[$&]*");
			requestParamsExclusionPatterns[i] = Pattern.compile(pattern.toString());
			}
		}
	}
	
	@Override
	public void run() {
		try {
			convertHtmlFile(domainUrl, url, final_content_path, raw_content_path, urlHtmlLoc, urlType, transformations, crawlId,domainId);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	public RefreshTransformationRunner(String domainUrl, String url, String final_content_path, String raw_content_path,
			Map<String, String> urlHtmlLoc, String urlType,	List<TransformationVO> transformations, int crawlId, int domainId){
		this.domainUrl=domainUrl;
    	this.url=url;
    	this.final_content_path=final_content_path;
    	this.raw_content_path=raw_content_path;
    	this.urlHtmlLoc=urlHtmlLoc;
    	this.urlType=urlType;
    	this.transformations=transformations;
    	this.crawlId=crawlId;
    	this.domainId = domainId;
	}
	
	public void convertHtmlFile(String domainUrl, String url, String final_content_path, String raw_content_path,
			Map<String, String> urlHtmlLoc, String urlType,	List<TransformationVO> transformations, int crawlId,int domainId) throws Exception  {
		String rawHtml;
		URLTransformationUtil utUtil = new URLTransformationUtil();
		CrawlUtil cUtil = new CrawlUtil();
		Connection conn = null;
		try {
			conn = JDBCConnector.getConnection();
			if(conn == null){
				LOG.error("Unable get the connection from DB: in convertHtmlFile process");
			}else{
					if(url != null && !url.isEmpty() && crawlId !=0){
						List<String> urlSegments = utUtil.getURLHTMLLOC(url,crawlId,conn);
						if(!urlSegments.isEmpty()){
						// Read content
							if(domainUrl != null && !domainUrl.isEmpty()){
								rawHtml = getResourceAsString(domainUrl.concat(url),url);
								if(rawHtml!=null){
									// write RawContent to file
									//rawHtml = rawHtml.replaceAll("\r\n", "");
									//rawHtml = rawHtml.replaceAll("\0", "");
									if(raw_content_path != null && !raw_content_path.isEmpty()){
										utUtil.writeContentToFile(raw_content_path.concat(urlSegments.get(0)), rawHtml);
										Iterator<TransformationVO> iterator = transformations.iterator();
										// Iterate through available transformations for segment.
										while (iterator.hasNext()) {
											String method = iterator.next().getTransformationType();
											rawHtml = utUtil.handleTransformations(rawHtml, method, domainUrl, url, crawlId, conn, requestParamsExclusionPatterns);
										}
										if(final_content_path != null && !final_content_path.isEmpty()){
											// write transformedContent to file
											utUtil.writeContentToFile(final_content_path.concat(urlSegments.get(0)), rawHtml);
										}else{
											LOG.error("Final Content temp directory is null in convertHtmlFile() method:");
										}
									}else{
										LOG.error("Raw Content temp directory is null in convertHtmlFile() method:");
									}
								  }else{
									  //delete old html file of url
									  cUtil.deleteOldHtmlFile(url,crawlId,domainId);
									  
								  }
								}else{
									LOG.error("domainUrl is null in convertHtmlFile() method:");
								}
						}else{
							LOG.error("urlSegments List is null in convertHtmlFile() method: ");
						}
					}else{
						LOG.error("url or Crawl id is null in convertHtmlFile() method: url is: "+url+" Crawl id is: "+crawlId);
					}
			}
		}finally {
			try {
				if(conn!=null){
					conn.close();
				}
			} catch (SQLException e) {
				LOG.info("Error while closing connection in RefreshTransformationRunner: convertHtmlFile method: " + e.getMessage());
			}
		}
	}
	
	/**
	 * This method opens a new connection ,reads the content line by
	 * line,appends it to a string and returns rawHtmlContent
	 * 
	 * @param urlLink
	 * @return htmlContent
	 * @throws IOException
	 */
	public String getResourceAsString(String urlLink,String baseurl) throws Exception {
		StringBuilder htmlContent = new StringBuilder();
		URLTransformationUtil utUtil = new URLTransformationUtil();
		String htmlContentAsString;
		BufferedReader reader = null;
		try {
			if(urlLink != null && !urlLink.isEmpty()){
				URL url = new URL(urlLink);
				URLConnection urlConnection = url.openConnection();
				HttpURLConnection connection = null;
				if (urlConnection instanceof HttpURLConnection) {
					connection = (HttpURLConnection) urlConnection;
					connection.setInstanceFollowRedirects(false);
					int responseCode = connection.getResponseCode();
					// if response code is 200 then write content to file
					if(responseCode==200){
						reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						while ((htmlContentAsString = reader.readLine()) != null) {
							htmlContent.append(htmlContentAsString).append(newLine);
						}
					}else{
						htmlContent=null;
						String statusMessage = statusMessages.getStatusMessage(responseCode);
						utUtil.updateLastFetchTime(statusMessage,baseurl,crawlId);
						//LOG.info("=================URL ["+urlLink+"] -->> Http ResponseCode:"+responseCode);
						return null;
					}
				}
			}else{
				LOG.error("URL is null in getResourceAsString() method: "); 
			}
		} catch (MalformedURLException e) {
			LOG.error("Url is incorrect");
		} catch (IOException e) {
			LOG.error("Could not connect to server");
		}catch(Exception e){
			LOG.error(e.getMessage());
		}
		return htmlContent.toString();
	}
}
