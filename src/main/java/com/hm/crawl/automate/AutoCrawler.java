package com.hm.crawl.automate;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.Crawler;
import org.apache.nutch.crawl.dao.SegmentMasterDAO;
import org.apache.nutch.segment.SegmentReader;
import org.apache.nutch.tools.JDBCConnector;
import org.apache.nutch.util.NutchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hm.crawl.data.DomainMasterCRUD;
import com.hm.crawl.data.SegmentMasterCRUD;
import com.hm.crawl.data.TransformationMasterCRUD;
import com.hm.crawl.data.vo.DomainVO;
import com.hm.crawl.data.vo.SegmentVO;
import com.hm.crawl.data.vo.TransformationVO;
import com.hm.purge.PurgeEdgeCastData;
import com.hm.util.RefreshTransformationRunner;

public class AutoCrawler {
	public static final Logger LOG = LoggerFactory.getLogger(AutoCrawler.class);
	private static String CRAWL_DIR = null;
	private static String RAW_CONTENT_TEMP_DIR = null;
	private static String CRAWLDB_TEMP_DIR = null;
	private static String FINAL_CONTENT_TEMP_DIR = null;
	private static String HOST_NAME = null;
	String errorMessage = null;
	String successMessage = null;
	
	Connection conn = null;
	private static Configuration conf;
	private static final String MEDIA_TYPE="ADN";
	ResourceBundle hmMessages = null;
	ResourceBundle hmConfigValuesBundle = null ;
	
	static {
		conf = NutchConfiguration.create();
	}
	
	public AutoCrawler() {
		//loading resource bundle of errorMessages
		hmMessages =  ResourceBundle.getBundle("hmMessages");
		hmConfigValuesBundle = ResourceBundle.getBundle("hmConfig");
	    
	}
	

	/**
	 * This method used to process Htmlize Domain 
	 * @param domainId
	 * @throws IOException
	 */
	public void autoCrawl(int domainId,int trackerId, int requestId) throws IOException, SQLException {
		CRAWLDB_TEMP_DIR = conf.get("crawlTempDirectory");
		RAW_CONTENT_TEMP_DIR = conf.get("raw_content_temp_dir");
		FINAL_CONTENT_TEMP_DIR = conf.get("final_content_temp_dir");
		CRAWL_DIR = conf.get("crawlDirectory");
		CrawlUtil cUtil = new CrawlUtil();
		
		//creating crawlid for this crawl
		int crawlId = cUtil.createCrawlId(domainId);
		if(crawlId != 0){
			if (domainId != 0){
				try {
					conn = JDBCConnector.getConnection();
					//getting all the segments ids (exculdes dont crawl segments) 
					List<Integer> segmentIDS = cUtil.getCrawlSegmentIds(domainId, conn);
					if(segmentIDS != null && segmentIDS.size() > 0){
					//delete the previous crawldb directory
					cUtil.deleteDirectory(CRAWLDB_TEMP_DIR);
					
					if(!cUtil.checkCrawlDirectory(CRAWLDB_TEMP_DIR)){
						//changing the SEG_RULE_CHANGED to 0 and SEG_HTML_PATH_CHANGED to 0 in DOMAIN table
						cUtil.chageSegmentStatusMasterValues(domainId);
						
						//changes the domain crawl_status to null in Domain table	
						cUtil.changeDomainCrawlStatus(domainId, conn);
						//starting the crawl process
						cUtil.setCrawlStatus(conn,  hmMessages.getString("domainHtmlizationInProgress"), crawlId,trackerId,requestId);
						boolean success = new Crawler().crawlByDomain(CRAWLDB_TEMP_DIR, 20000, domainId,null);
						if (success) {
							cUtil.setCrawlStatus(conn, hmMessages.getString("domainCrawled"), crawlId,trackerId,requestId);
							//read all html patterns
							List<Integer> allHTMLPatterns = cUtil.allHTMLPatterns(conn);
							Map<String, List<String>> transformationsMap = null;
							if (!allHTMLPatterns.isEmpty()) {
								cUtil.setCrawlStatus(conn, hmMessages.getString("generatingHtmlPathStarted"), crawlId,trackerId,requestId);
								transformationsMap = cUtil.getTransformationMap(segmentIDS, domainId, crawlId);
								LOG.info("Generating HTML Paths  Completed");
								cUtil.setCrawlStatus(conn, hmMessages.getString("generatingHtmlPathCompleted"), crawlId,trackerId,requestId);
								//read all transformations
								List<Integer> alltransformations = cUtil.getTransformations(conn);
								if (!alltransformations.isEmpty()) {
									cUtil.setCrawlStatus(conn, hmMessages.getString("transformingHtmlFilesStarted"), crawlId,trackerId,requestId);
									//String split = "-splitContent";
									File segmentsRoot = new File(CRAWLDB_TEMP_DIR+"/"+"segments");
									if(segmentsRoot.exists()){
										File[] listFiles = segmentsRoot.listFiles();
										//Looping through all segments folders from crawlTemp directory 
										if(listFiles != null && listFiles.length > 0){
											for (File file : listFiles) {
											 	String segmentPath =	segmentsRoot+"/"+file.getName();
											 	if(new File(segmentPath).exists()){
											 		Configuration conf = NutchConfiguration.create();
													SegmentReader segmentReader = new SegmentReader(conf, true, true, true, true, true, true);
													segmentReader.splitSegmentContent(new Path(segmentPath), RAW_CONTENT_TEMP_DIR, crawlId, domainId, FINAL_CONTENT_TEMP_DIR, transformationsMap);
													segmentReader.close();
											 	}
											}
										}
									}
									segmentsRoot = null;
								
									LOG.info("Transforming into HTML files  Completed");
									cUtil.setCrawlStatus(conn, hmMessages.getString("transformingHtmlFilesDone"), crawlId,trackerId,requestId);
									
									DomainVO domainVO = cUtil.getDomain(domainId);
									
									File directory = new File(CRAWLDB_TEMP_DIR);
									if(directory.exists()){
										FileUtils.copyDirectory(directory, new File(CRAWL_DIR));
									}
									directory = new File(FINAL_CONTENT_TEMP_DIR);
									if(directory.exists()){
										CrawlUtil.moveHtmlizedDomainDirectory(FINAL_CONTENT_TEMP_DIR+domainVO.getSeedUrl(), domainVO.getFinal_content_directory()+domainVO.getSeedUrl());
									}
									
									directory = new File(RAW_CONTENT_TEMP_DIR);
									if(directory.exists()){
										CrawlUtil.moveHtmlizedDomainDirectory(RAW_CONTENT_TEMP_DIR+domainVO.getSeedUrl(), domainVO.getRaw_content_directory()+domainVO.getSeedUrl());
									}
									successMessage = hmMessages.getString("autoCrawlProcessDone");
									LOG.info(successMessage);
									cUtil.handleSuccess(conn, successMessage,domainId,crawlId,trackerId,requestId);
									
									//purge API Call for delete Domain HTML files action through HM UI
									String segPurgeURL = null;
									SegmentVO segVO = null;
									SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
									String isPurge = hmConfigValuesBundle.getString("makePurgeCallAfterHtmlization");
									if(isPurge != null && !isPurge.isEmpty() && isPurge.equalsIgnoreCase("true")){
										for(Integer segId : segmentIDS){
											segVO = segmentCrud.readByPrimaryKey(segId.toString(),conn);
											if(segVO != null){
												segPurgeURL = segVO.getPurgeUrl();
												if(segPurgeURL != null && !segPurgeURL.isEmpty()){
													List<String> purgeUrlList = new ArrayList<String>();
													purgeUrlList = Arrays.asList(segPurgeURL.split(" "));
													if(purgeUrlList != null && purgeUrlList.size() > 0){
														for (String purgeUrl : purgeUrlList) {
															if(purgeUrl != null && !purgeUrl.isEmpty()){
																if(purgeUrl.equalsIgnoreCase("JSP")){
																	List<String> urlsList = cUtil.readUrlsForSegment(segVO.getSegmentId(),crawlId);
																	if(urlsList != null && !urlsList.isEmpty()){
																		for(String JSPurl: urlsList){
																			purgeURL(JSPurl);
																		}
																	}
																}else{
																	purgeURL(purgeUrl);
																	LOG.info("Purge Process Successfully Completed for :"+purgeUrl);
																}
															}
														}
													}	
												}
											}
										}
										LOG.info("Purge Process Successfully Completed for Domain Level");
									}
									LOG.info("HTMLIZATION DOMAIN COMPLETED TO " + domainVO.getDomainName());
									//update final status in RequestProcessor table
									cUtil.setCrawlStatus(conn, hmMessages.getString("domainHtmlizationCompleted"), crawlId,trackerId,requestId);
								} else {
									errorMessage = hmMessages.getString("noTransformationsAdded");//"No transformations added.Please add the transformations";
									LOG.error(errorMessage);
									cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
								}
							}else {
								errorMessage = hmMessages.getString("noHtmlPathsForSegment");//"HTML Paths does not exist for segments. Please add the HTML Paths for segments.";
								cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
							}
						}else{
							LOG.error("Crawl Failed");
							errorMessage = "Crawl Failed";
							cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
						}
					}else{
						LOG.error("Unable to delete the crawldb Directory");
						errorMessage = "Unable to delete the crawldb Directory";
						cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
					}
				}else{
					LOG.error("SegmentList is null:");
					errorMessage = "Process Failed";
					cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
				}	
			} catch (Exception e) {
				LOG.error(e.getMessage());
				errorMessage = "Crawl Failed";
				cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
			} finally{
					try {
						if(conn != null) {
							conn.close();
						}
					} catch (SQLException e) {
						LOG.error("Error while closing connection in autoCrawl() process: "+e.getMessage());
					}
				}
			}else {
				errorMessage = "No Domain available to crawl.Please add the domain";
				LOG.error("No Domain available to crawl.Please add the domain");
				cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
			}
		}else{
			LOG.error("CrawlId is 0: Unable to Process:");
			errorMessage = hmMessages.getString("crawlIdIsZero");
			cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
		}
	}
	
	
	/** 
	 * This method used to process Refresh Domain
	 * @param domainId
	 * @throws IOException
	 */
	public void refreshDomain(int domainId,int trackerId,int requestId) throws IOException {
		CrawlUtil cUtil = new CrawlUtil();
		int crawlId = 0;
		SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
		try{
			conn = JDBCConnector.getConnection();
		    crawlId = cUtil.getCrawlId(conn,domainId);
		    if(crawlId != 0){
			    //changing the SEG_RULE_CHANGED to 0 and SEG_HTML_PATH_CHANGED to 0 in DOMAIN table
				cUtil.chageSegmentHTMLPathStatusValue(domainId);
				//getting all the segments ids (exculdes dont crawl segments) 
				List<Integer> segmentIDS = cUtil.getCrawlSegmentIds(domainId, conn);
				if(segmentIDS != null && segmentIDS.size() > 0){
					//read all html patterns
					List<Integer> allHTMLPatterns = cUtil.allHTMLPatterns(conn);
					if (!allHTMLPatterns.isEmpty()) {
						cUtil.setCrawlStatus(conn, hmMessages.getString("domainRefreshStarted"), crawlId,trackerId,requestId);
						SegmentVO segVO = null;
						for (Integer segmentID : segmentIDS) {
							segVO = segmentCrud.readByPrimaryKey(String.valueOf(segmentID),conn);
							String segmentId = segmentID.toString();
							if(segVO != null){
								if (segVO.getPathVO().isEmpty()) {
									LOG.error("HTML Path does not exist for " + segVO.getSegmentName()
											+ " segment.Please edit the Segment add the HTML Path.");
								} else {
									segmentCrud.generateURLforSegment(String.valueOf(segmentID),String.valueOf(domainId),crawlId);
									LOG.info("Generating HTML Paths  Completed for segment:"+segVO.getSegmentName());
			                        // Transformation process at segment level
									TransformationMasterCRUD transformCrud = new TransformationMasterCRUD();
									DomainVO domainVo = new DomainVO();
									DomainMasterCRUD domainCrud = new DomainMasterCRUD();
									SegmentMasterDAO smDAO = new SegmentMasterDAO();
									Map<String, String> urlLocMapToTransform = new HashMap<String, String>();
									Map<String, String> urlLocMapToReplace = new HashMap<String, String>();
									List<TransformationVO> transformations = new ArrayList<TransformationVO>();
									try {
										// Read available transformations.
										transformations = transformCrud.readTransformationsForSegment(segmentId,conn);
										if(transformations != null && !transformations.isEmpty()){
											//Read DomainVo by passing domainId
											domainVo = domainCrud.readByPrimaryKey(domainId);
											// Read urls for current Segment
											urlLocMapToTransform = smDAO.readUrlsHtmlLocForSegment(Integer.parseInt(segmentId),crawlId);
											// Read urls all Segments with default location
											urlLocMapToReplace = smDAO.readUrlHtmlLocforAllSegment(crawlId,conn);
											//Read rawTemp, finalTemp dir path from web.xml file
											RAW_CONTENT_TEMP_DIR = conf.get("raw_content_temp_dir");
											FINAL_CONTENT_TEMP_DIR = conf.get("final_content_temp_dir");
											//Multi Threading at segment level
											ExecutorService executor = Executors.newFixedThreadPool(20);
											for (Map.Entry<String, String> entry : urlLocMapToTransform.entrySet()) {
												// for each url fetch content and write to a file
												RefreshTransformationRunner refThread = new RefreshTransformationRunner(domainVo.getUrl(),entry.getValue(),FINAL_CONTENT_TEMP_DIR,
														RAW_CONTENT_TEMP_DIR,urlLocMapToReplace, segVO.getUrlType(), transformations, crawlId,domainId);
												executor.execute(refThread);
											}
											// and finish all existing threads in the queue
										    executor.shutdown();
										    while(!executor.isTerminated()){
										    	//logger.info("Thread is waiting");
										    }
										    LOG.info("All RefreshThreads process has finished for segment:"+segVO.getSegmentName());
											LOG.info("Transformation Completed for segment : "+segVO.getSegmentName());
										}else{
											errorMessage = hmMessages.getString("noTransformationsAdded");//"No transformations added.Please add the transformations";
											LOG.error(errorMessage);
											cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
										}	
									  } catch (Exception e) {
										  errorMessage = "Transformations Failed";
										  cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
									 }
								}
							}
						}
						LOG.info("Transforming into HTML files  Completed for All Segments Successfully:");
						cUtil.setCrawlStatus(conn, hmMessages.getString("transformingHtmlFilesDone"), crawlId,trackerId,requestId);
						DomainVO domainVO = cUtil.getDomain(domainId);
						//copy rawTemp, finalTemp folders to Final folders
						if(new File(FINAL_CONTENT_TEMP_DIR).exists()){
							cUtil.moveDirectory(FINAL_CONTENT_TEMP_DIR, domainVO.getFinal_content_directory());
						}
						if(new File(RAW_CONTENT_TEMP_DIR).exists()){
							cUtil.moveDirectory(RAW_CONTENT_TEMP_DIR, domainVO.getRaw_content_directory());
						}
						successMessage = hmMessages.getString("refreshDomainProcessDone");
						cUtil.handleSuccess(conn, successMessage,domainId,crawlId,trackerId,requestId);
						
						//purge API Call for crawled segments of Refresh Domain through UI action
						String segPurgeURL = null;
						SegmentVO segmentVO = null;
						String isPurge = hmConfigValuesBundle.getString("makePurgeCallAfterRefresh");
						if(isPurge != null && !isPurge.isEmpty() && isPurge.equalsIgnoreCase("true")){
							for(Integer segId : segmentIDS){
								segmentVO = segmentCrud.readByPrimaryKey(String.valueOf(segId),conn);
								if(segmentVO != null){
									segPurgeURL = segmentVO.getPurgeUrl();
									if(segPurgeURL != null && !segPurgeURL.isEmpty()){
										List<String> purgeUrlList = new ArrayList<String>();
										purgeUrlList = Arrays.asList(segPurgeURL.split(" "));
										if(purgeUrlList != null && !purgeUrlList.isEmpty()){
											for (String purgeUrl : purgeUrlList) {
												if(purgeUrl != null && !purgeUrl.isEmpty()){
													if(purgeUrl.equalsIgnoreCase("JSP")){
														List<String> urlsList = cUtil.readUrlsForSegment(segVO.getSegmentId(),crawlId);
														if(urlsList != null && !urlsList.isEmpty()){
															for(String JSPurl: urlsList){
																purgeURL(JSPurl);
															}
														}
													}else{
														purgeURL(purgeUrl);
													}
												}
											}
										}
									}
								}
							}
							LOG.info("Purge Process Successfully Completed for All Segments:");
						}else{
							LOG.info("Purge Process Disabled");
						}
						LOG.info("REFRESHING DOMAIN COMPLETED:");
						cUtil.setCrawlStatus(conn, hmMessages.getString("domainRefreshDone"), crawlId,trackerId,requestId);
					}else {
						errorMessage = "HTML Paths does not exist for segments. Please add the HTML Paths for segments ";
						cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
					}
				}else {
					errorMessage = "SegmentsList NULL: Unable to Process:";
					cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
				}
			}else{
				LOG.error("Crawl Id is 0: Unable to Process:");
				errorMessage = hmMessages.getString("crawlIdIsZero");
				cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
			}	
		}catch(Exception e){
			LOG.error("Error while Refresh Domain Process: "+e.getMessage());
		} finally{
			try {
				if(conn != null){
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing connection in Refresh Domain process: "+e.getMessage());
			}
		}
	}
	
	
	/** 
	 * This method used to process Htmlize Selected Segments
	 * @param domainId
	 * @param checkedsegments
	 */
	public void htmlizeSegments(int domainId, String[] checkedsegments,int trackerId,int requestId) {
		CRAWLDB_TEMP_DIR = conf.get("crawlTempDirectory");
		RAW_CONTENT_TEMP_DIR = conf.get("raw_content_temp_dir");
		FINAL_CONTENT_TEMP_DIR = conf.get("final_content_temp_dir");
		CRAWL_DIR = conf.get("crawlDirectory");
		CrawlUtil cUtil = new CrawlUtil();
		try {
			conn = JDBCConnector.getConnection();
			int crawlId = cUtil.getCrawlId(conn,domainId);
			if(crawlId != 0){
				if(checkedsegments != null && checkedsegments.length > 0){
					List<String> crawlSegments = Arrays.asList(checkedsegments);
					List<String> url_pattern_rules = new ArrayList<>();
					// live crawl id
					SegmentVO segVO = null;
					SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
					List<Integer> segmentIds = cUtil.getSegmentIds(domainId, conn);
					if(segmentIds != null && !segmentIds.isEmpty()) {
						for (Integer segmentId : segmentIds) {
							if(!crawlSegments.contains(segmentId.toString()) && segmentId != 99999){
								segVO = segmentCrud.readByPrimaryKey(segmentId.toString(),conn);
								if(segVO != null){
									url_pattern_rules.add(segVO.getUrl_pattern_rule());
								}
							}
						}
						//delete the previous crawldb directory
						cUtil.deleteDirectory(CRAWLDB_TEMP_DIR);
						if(!cUtil.checkCrawlDirectory(CRAWLDB_TEMP_DIR)){
							//changes the domain crawl_status to null in Domain table	
							cUtil.changeDomainCrawlStatus(domainId, conn);
							//starting the crawl process
							try {
								cUtil.setCrawlStatus(conn, hmMessages.getString("segmentsHtmlizationStarted"), crawlId,trackerId,requestId);
								boolean success = new Crawler().crawlByDomain(CRAWLDB_TEMP_DIR, 20000, domainId,url_pattern_rules);
								if (success) {
									//read all html patterns
									List<Integer> allHTMLPatterns = cUtil.allHTMLPatterns(conn);
									Map<String, List<String>> transformationsMap = null;
									List<Integer> segmentIdlist = new ArrayList<Integer>();
									for(String segmentid:crawlSegments){
										segmentIdlist.add(Integer.parseInt(segmentid));
									}
									if (!allHTMLPatterns.isEmpty()) {
										cUtil.setCrawlStatus(conn, hmMessages.getString("generatingHtmlPathStarted"), crawlId,trackerId,requestId);
										transformationsMap = cUtil.getTransformationMap(segmentIdlist, domainId, crawlId);
										LOG.info("Generating HTML Paths  Completed");
										cUtil.setCrawlStatus(conn, hmMessages.getString("generatingHtmlPathCompleted"), crawlId,trackerId,requestId);
										//read all transformations
										List<Integer> alltransformations = cUtil.getTransformations(conn);
										if (!alltransformations.isEmpty()) {
											cUtil.setCrawlStatus(conn, hmMessages.getString("transformingHtmlFilesStarted"), crawlId,trackerId,requestId);
											String split = "-splitContent";
											File segmentsRoot = new File(CRAWLDB_TEMP_DIR+"/"+"segments");
											if(segmentsRoot.exists()){
												File[] listFiles = segmentsRoot.listFiles();
												//Looping through all segments folders from crawlTemp directory 
												if(listFiles != null && listFiles.length > 0){
													for (File file : listFiles) {
													 	String segmentPath =	segmentsRoot+"/"+file.getName();
													 	if(new File(segmentPath).exists()){
													 		Configuration conf = NutchConfiguration.create();
															SegmentReader segmentReader = new SegmentReader(conf, true, true, true, true, true, true);
															segmentReader.splitSegmentContent(new Path(segmentPath), RAW_CONTENT_TEMP_DIR, crawlId, domainId, FINAL_CONTENT_TEMP_DIR, transformationsMap);
													 	}
													}
												}
											}
											LOG.info("Transforming into HTML files  Completed");
											cUtil.setCrawlStatus(conn, hmMessages.getString("transformingHtmlFilesDone"), crawlId,trackerId,requestId);
											DomainVO domainVO = cUtil.getDomain(domainId);
											//Moving rawTemp, finalTemp folders to final folders
											if(new File(CRAWLDB_TEMP_DIR).exists()){
												FileUtils.copyDirectory(new File(CRAWLDB_TEMP_DIR), new File(CRAWL_DIR));
											}
											if(new File(FINAL_CONTENT_TEMP_DIR).exists()){
												cUtil.moveDirectory(FINAL_CONTENT_TEMP_DIR, domainVO.getFinal_content_directory());
											}
											if(new File(RAW_CONTENT_TEMP_DIR).exists()){
												cUtil.moveDirectory(RAW_CONTENT_TEMP_DIR, domainVO.getRaw_content_directory());
											}
											successMessage = hmMessages.getString("selectedSegmentsCrawlDone");
											LOG.info(successMessage);
											cUtil.handleSuccess(conn, successMessage,domainId,crawlId,trackerId,requestId);
											
											//purge API Call for selected segments of Domain action through HM UI
											String segPurgeURL = null;
											SegmentVO segmentVO = null;
											String isPurge = hmConfigValuesBundle.getString("makePurgeCallAfterHtmlization");
											if(isPurge != null && !isPurge.isEmpty() && isPurge.equalsIgnoreCase("true")){
												for(Integer segId : segmentIdlist){
													segmentVO = segmentCrud.readByPrimaryKey(String.valueOf(segId),conn);
													if(segmentVO != null){
														segPurgeURL = segmentVO.getPurgeUrl();
														if(segPurgeURL != null && !segPurgeURL.isEmpty()){
															List<String> purgeUrlList = new ArrayList<String>();
															purgeUrlList = Arrays.asList(segPurgeURL.split(" "));
															if(purgeUrlList != null && !purgeUrlList.isEmpty()){
																for (String purgeUrl : purgeUrlList) {
																	if(purgeUrl != null && !purgeUrl.isEmpty()){
																		if(purgeUrl.equalsIgnoreCase("JSP")){
																			List<String> urlsList = cUtil.readUrlsForSegment(segVO.getSegmentId(),crawlId);
																			if(urlsList != null && !urlsList.isEmpty()){
																				for(String JSPurl: urlsList){
																					purgeURL(JSPurl);
																				}
																			}
																		}else{
																			purgeURL(purgeUrl);
																		}
																	}
																}
															}	
														}
													}
												}
										}else{
											LOG.info("Purge process Disabled");
										}
											LOG.info("HTMLIZATION SEGMENTS COMPLETED:");
											cUtil.setCrawlStatus(conn, hmMessages.getString("selectedSegmentsHtmlizationDone"), crawlId,trackerId,requestId);
										
										} else {
											LOG.error("No transformations added.Please add the transformations:");
											errorMessage =  hmMessages.getString("noTransformationsAdded");
											cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
										}
									}else {
										errorMessage = hmMessages.getString("noHtmlPathsForSegment");
										LOG.error(errorMessage);
										cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
									}
								}else{
									errorMessage = hmMessages.getString("htmalizationSegmentsFailed");
									LOG.error(errorMessage);
									cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
								}
							} catch (Exception e) {
								LOG.error(e.getLocalizedMessage());
							}
						}else{
							errorMessage = hmMessages.getString("deleteCrawlDirectory");
							cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
							}
					}else{
						LOG.error("Segment list is NULL: for Domain:");
						errorMessage = hmMessages.getString("checkedSegmentsValueZero");
						cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
					}	
				}else{
					LOG.error("Please select the segments you want to crawl:");
					errorMessage = hmMessages.getString("checkedSegmentsValueZero");
					cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
				}
			}else{
				LOG.error("Crawl Id is 0: Unable to Process:");
				errorMessage = hmMessages.getString("crawlIdIsZero");
				cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
			}
		}catch(Exception e){
			LOG.error("Error while processing selected segments Htmlize process: "+e.getMessage());
		}
		finally {
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("Error while closing connection in Selected Segments Htmlize process: "+e.getMessage());
			}
		}
	}
	
	
	/**
	 * This method is used to process Refresh Selected Segments
	 * @param domainId
	 * @param checkedSegments
	 * @throws IOException
	 */
	public void refreshSelectedSegments(int domainId, String[] checkedSegments,int trackerId,int requestId) throws IOException {
		CrawlUtil cUtil = new CrawlUtil();
		DomainVO domainVo = new DomainVO();
		int crawlId = 0;
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
		try{
			conn = JDBCConnector.getConnection();
			crawlId = cUtil.getCrawlId(conn,domainId);
			if(crawlId != 0) {
				domainVo = domainCrud.readByPrimaryKey(domainId);
				if(checkedSegments != null && checkedSegments.length > 0){
					cUtil.setCrawlStatus(conn, hmMessages.getString("segmentsRefreshStarted"), crawlId,trackerId,requestId);
					List<String> crawlSegments = Arrays.asList(checkedSegments);
					if(crawlSegments != null && !crawlSegments.isEmpty()){
						for (String segmentId : crawlSegments) {
							boolean generated = false;
							SegmentVO segVO = segmentCrud.readByPrimaryKey(segmentId,conn);
							if (segVO != null && segVO.getPathVO().isEmpty()) {
								errorMessage = "HTML Path does not exist for this Segment.Please edit the Segment add the HTML Path";
								cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
							} else {
								try {
									segmentCrud.generateURLforSegment(segmentId, String.valueOf(domainId),crawlId);
									generated=true;
								} catch (SQLException e) {
									LOG.error(e.getLocalizedMessage());
								}
								LOG.info("HTML Path successfully generated for SegmentName:"	+ segVO.getSegmentName());
								cUtil.setCrawlStatus(conn,hmMessages.getString("generatingHtmlPathCompleted"), crawlId,trackerId,requestId);
							}
							//Transform HTML Links for SegmentId
							if(generated){
								TransformationMasterCRUD transformCrud = new TransformationMasterCRUD();
								SegmentMasterDAO smDAO = new SegmentMasterDAO();
								Map<String, String> urlLocMapToTransform = new HashMap<String, String>();
								Map<String, String> urlLocMapToReplace = new HashMap<String, String>();
								List<TransformationVO> transformations = new ArrayList<TransformationVO>();
								try {
									// Read available transformations.
									transformations = transformCrud.readTransformationsForSegment(segmentId,conn);
									if(transformations != null && !transformations.isEmpty()) {
										// Read urls for current Segment
										urlLocMapToTransform = smDAO.readUrlsHtmlLocForSegment(Integer.parseInt(segmentId),crawlId);
										
										// Read urls all Segments with default location
										urlLocMapToReplace = smDAO.readUrlHtmlLocforAllSegment(crawlId,conn);
										
										//Read rawTemp, finalTemp dir path from web.xml file
										RAW_CONTENT_TEMP_DIR = conf.get("raw_content_temp_dir");
										FINAL_CONTENT_TEMP_DIR = conf.get("final_content_temp_dir");
										
										//Multi Threading at segment level
										ExecutorService executor = Executors.newFixedThreadPool(20);
										if(urlLocMapToTransform != null && !urlLocMapToTransform.isEmpty()){
											for (Map.Entry<String, String> entry : urlLocMapToTransform.entrySet()) {
												// for each url fetch content and write to a file
												RefreshTransformationRunner refThread = new RefreshTransformationRunner(domainVo.getUrl(),entry.getValue(),FINAL_CONTENT_TEMP_DIR,
														RAW_CONTENT_TEMP_DIR,urlLocMapToReplace, segVO.getUrlType(), transformations, crawlId,domainId);
												executor.execute(refThread);
											}
										}	
										// and finish all existing threads in the queue
									    executor.shutdown();
									    while(!executor.isTerminated()){
									    	//logger.info("Thread is waiting");
									    }
										LOG.info("All RefreshThreads process has finished:");
										LOG.info("Transformation Completed for segment : "+segVO.getSegmentName());
									}else{
										LOG.error("No transformations added.Please add the transformations:");
										errorMessage =  hmMessages.getString("noTransformationsAdded");
										cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
									}
								  } catch (Exception e) {
									  LOG.error("Error in refreshSelectedSegments() method : "+e.getMessage());
									cUtil.handleError(conn, "Transformations Failed",domainId,crawlId,trackerId,requestId);
									return;
								  }
							}
						}
					}else{
						LOG.error("Crawled Segment list is NULL: for Domain:");
						errorMessage = hmMessages.getString("checkedSegmentsValueZero");
						cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
					}
					if(new File(FINAL_CONTENT_TEMP_DIR).exists()){
						cUtil.moveDirectory(FINAL_CONTENT_TEMP_DIR, domainVo.getFinal_content_directory());
					}
					if(new File(RAW_CONTENT_TEMP_DIR).exists()){
						cUtil.moveDirectory(RAW_CONTENT_TEMP_DIR, domainVo.getRaw_content_directory());
					}
					successMessage = hmMessages.getString("selectedSegmentsRefreshProcessDone");
					LOG.info(successMessage);
					cUtil.handleSuccess(conn, successMessage,domainId,crawlId,trackerId,requestId);
					
					//purge API Call for selected segments through UI Action
					String segPurgeURL = null;
					SegmentVO segVO = null;
					String isPurge = hmConfigValuesBundle.getString("makePurgeCallAfterRefresh");
					if(isPurge != null && !isPurge.isEmpty() && isPurge.equalsIgnoreCase("true")){
						for(String segId : crawlSegments){
							segVO = segmentCrud.readByPrimaryKey(segId,conn);
							if(segVO != null){
								segPurgeURL = segVO.getPurgeUrl();
								if(segPurgeURL != null && !segPurgeURL.isEmpty()){
									List<String> purgeUrlList = new ArrayList<String>();
									purgeUrlList = Arrays.asList(segPurgeURL.split(" "));
									if(purgeUrlList != null && !purgeUrlList.isEmpty()){
										for (String purgeUrl : purgeUrlList) {
											if(purgeUrl != null && !purgeUrl.isEmpty()){
												if(purgeUrl.equalsIgnoreCase("JSP")){
													List<String> urlsList = cUtil.readUrlsForSegment(segVO.getSegmentId(),crawlId);
													if(urlsList != null && !urlsList.isEmpty()){
														for(String JSPurl: urlsList){
															purgeURL(JSPurl);
														}
													}
												}else{
													purgeURL(purgeUrl);
												}
											}
										}
									}
								}
							}
						}
						LOG.info("Purge Process Successfully Completed for Selected Segments:");
					}else{
						LOG.info("Purge Process Disabled");
					}
					LOG.info("Refreshing segment completed for domain ========:"+domainVo.getDomainName());
					cUtil.setCrawlStatus(conn, hmMessages.getString("selectedSegmentsRefreshDone"), crawlId,trackerId,requestId);
				}else{
					errorMessage = hmMessages.getString("checkedSegmentsValueZero");
					cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
				}
			}else{
				LOG.error("Crawl Id is 0: Unable to Process:");
				errorMessage = hmMessages.getString("crawlIdIsZero");
				cUtil.handleError(conn, errorMessage,domainId,crawlId,trackerId,requestId);
			}
		} catch (Exception e1) {
			LOG.error("Error while processing Refreshing selected Segments: "+e1.getMessage());
		}finally {
			try {
				conn.close();
			} catch (SQLException e) {
				LOG.error("Error while closing connection in Refresh Selected Segments process: "+e.getMessage());
			}
		}
	}
	
	
	/**
	 * This method used to process Refresh URL
	 * @param domainId
	 * @param segmentId
	 * @param url
	 */
	public void refreshURL(int domainId, String segmentId, String url,int trackerId,int requestId){
		TransformationMasterCRUD transformCrud = new TransformationMasterCRUD();
		List<TransformationVO> transformations = new ArrayList<TransformationVO>();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
		SegmentMasterDAO smDAO = new SegmentMasterDAO();
		DomainVO domainVo = new DomainVO();
		CrawlUtil cUtil = new CrawlUtil();
		int crawlId = 0;
		try {
			if(url == null && url.isEmpty()){
				//errorMessage = hmMessages.getString("selectedUrlListIsNull");
				LOG.error(" URL  IS NULL in refreshURLS() method: ");
				cUtil.setCrawlStatus(conn,
						hmMessages.getString("urlIsNull"), crawlId,
						trackerId, requestId);
				return;
			}
			if (domainId == 0) {
				LOG.error("Domain id is null in refreshURLS() method: ");
				
					//errorMessage = hmMessages.getString("domainIdISNULL");
					cUtil.setCrawlStatus(conn,
							hmMessages.getString("domainIdISNULL"), crawlId,
							trackerId, requestId);
					return;
			}
		} catch (SQLException e) {
			LOG.error("Error while setting the status in refreshSelectedURLS() method: ");
		}
		RAW_CONTENT_TEMP_DIR = conf.get("raw_content_temp_dir");
		FINAL_CONTENT_TEMP_DIR = conf.get("final_content_temp_dir");
		try {
			conn = JDBCConnector.getConnection();
			//Read live crawlId
			crawlId = cUtil.getCrawlId(conn,domainId);
			if(crawlId != 0){
				cUtil.setCrawlStatus(conn, hmMessages.getString("refreshingUrlStarted"), crawlId,trackerId,requestId);
				LOG.info("Refresh URL ["+ url +"] Process Started:");
				// if it is api call we are getting segmentId from url_html_loc
				if(segmentId == null){
					segmentId = String.valueOf(cUtil.getSegmentId(conn,crawlId,url));
				}
				// Read available transformations.
				transformations = transformCrud.readTransformationsForSegment(segmentId, null);
				domainVo = domainCrud.readByPrimaryKey(domainId);
				SegmentVO segVO = segmentCrud.readByPrimaryKey(segmentId,conn);
				// Read urls of all Segments including default location
				Map<String, String> urlLocMapToReplace = new HashMap<String, String>();
				urlLocMapToReplace = smDAO.readUrlHtmlLocforAllSegment(crawlId,conn);
				ExecutorService executor = Executors.newFixedThreadPool(5);
				RefreshTransformationRunner refUrlThread = new RefreshTransformationRunner(domainVo.getUrl(), url, FINAL_CONTENT_TEMP_DIR,
						RAW_CONTENT_TEMP_DIR,urlLocMapToReplace, segVO.getUrlType(), transformations, crawlId,domainId);
				executor.execute(refUrlThread);
				executor.shutdown();
			    while(!executor.isTerminated()){
			    	//logger.info("Thread is waiting");
			    }
				LOG.info("Refreshed URL :["+url+"] Successfully");
				//cUtil.setCrawlStatus(conn, "Refreshed URL :["+url+"] Successfully", crawlId,trackerId,requestId);
				//copy rawTemp, finalTemp folders to Final folders
				if(FINAL_CONTENT_TEMP_DIR != null && new File(FINAL_CONTENT_TEMP_DIR).exists()){
					cUtil.moveDirectory(FINAL_CONTENT_TEMP_DIR, domainVo.getFinal_content_directory());
				}
				if(RAW_CONTENT_TEMP_DIR != null && new File(RAW_CONTENT_TEMP_DIR).exists()){
					cUtil.moveDirectory(RAW_CONTENT_TEMP_DIR, domainVo.getRaw_content_directory());
				}
				//purge API Call for selected URL through UI Action
				purgeURL(url);
				LOG.info("Purge Process Successfully Completed for Selected URL:");
				//set Request Status message
				cUtil.setCrawlStatus(conn, hmMessages.getString("refreshingUrlCompleted"), crawlId,trackerId,requestId);
			}else{
				 errorMessage = hmMessages.getString("crawlIdIsNull");
				 LOG.error("Unable to get the crawl id in refreshURLS() method:");
				 cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), crawlId,trackerId,requestId);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	
	/**
	 * This method used to process Refresh URL
	 * @param domainId
	 * @param segmentId
	 * @param url
	 */
	public void refreshSelectedURLS(int domainId, String segmentId, String[] urls,int trackerId,String isApi,int requestId){
		TransformationMasterCRUD transformCrud = new TransformationMasterCRUD();
		List<TransformationVO> transformations = new ArrayList<TransformationVO>();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
		SegmentMasterDAO smDAO = new SegmentMasterDAO();
		DomainVO domainVo = new DomainVO();
		SegmentVO segVO = null;
		CrawlUtil cUtil = new CrawlUtil();
		int crawlId = 0;
		
		try {
				if(urls == null && urls.length == 0){
					//errorMessage = hmMessages.getString("selectedUrlListIsNull");
					LOG.error("Selected URL LIST IS NULL in refreshSelectedURLS() method: ");
					cUtil.setCrawlStatus(conn,
							hmMessages.getString("selectedUrlListIsNull"), crawlId,
							trackerId, requestId);
					return;
				}
				if (domainId == 0) {
					LOG.error("Domain id is null in refreshSelectedURLS() method: ");
					
						//errorMessage = hmMessages.getString("domainIdISNULL");
						cUtil.setCrawlStatus(conn,
								hmMessages.getString("domainIdISNULL"), crawlId,
								trackerId, requestId);
						return;
				}
			} catch (SQLException e) {
				LOG.error("Error while setting the status in refreshSelectedURLS() method: ");
			}
		//Read live crawlId
		crawlId = cUtil.getCrawlId(conn,domainId);
		//cUtil.updateStatusTracker(crawlId);
		RAW_CONTENT_TEMP_DIR = conf.get("raw_content_temp_dir");
		FINAL_CONTENT_TEMP_DIR = conf.get("final_content_temp_dir");
		try {
			 if(crawlId != 0){
				cUtil.setCrawlStatus(conn, hmMessages.getString("refreshSelectedUrlStarted"), crawlId,trackerId,requestId);
				/*if(trackerId != 0){
				cUtil.setTrackerStatus(conn, trackerId, crawlId, "REFRESHING URL STARTED");
				}*/
				LOG.info("Refresh Selected URLS Process Started:");
				// Read available transformations.
				//segmentId = String.valueOf(cUtil.getSegmentId(conn,crawlId,url));
				if(segmentId != null){
				transformations = transformCrud.readTransformationsForSegment(segmentId, conn);
				segVO = segmentCrud.readByPrimaryKey(segmentId,conn);
				}
				domainVo = domainCrud.readByPrimaryKey(domainId);
				// Read urls of all Segments including default location
				Map<String, String> urlLocMapToReplace = new HashMap<String, String>();
				urlLocMapToReplace = smDAO.readUrlHtmlLocforAllSegment(crawlId,conn);
				ExecutorService executor = Executors.newFixedThreadPool(5);
				for ( String url : urls) {	
					// if it is api call we are getting segmentId from url_html_loc
					if(segmentId == null || isApi != null){
						segmentId = String.valueOf(cUtil.getSegmentId(conn,crawlId,url));
						transformations = transformCrud.readTransformationsForSegment(segmentId, conn);
						segVO = segmentCrud.readByPrimaryKey(segmentId,conn);
					}
				RefreshTransformationRunner refUrlThread = new RefreshTransformationRunner(domainVo.getUrl(), url, FINAL_CONTENT_TEMP_DIR,
						RAW_CONTENT_TEMP_DIR,urlLocMapToReplace, segVO.getUrlType(), transformations, crawlId,domainId);
				executor.execute(refUrlThread);			
				}
				executor.shutdown();
			    while(!executor.isTerminated()){
			    	//logger.info("Thread is waiting");
			    }
				LOG.info("Refreshed SELECTED URLS completed  Successfully");
				cUtil.setCrawlStatus(conn, hmMessages.getString("refreshingSelectedUrlsCompletedSuccessfully"), crawlId,trackerId,requestId);
				//cUtil.setCrawlStatus(conn, "Refreshed URL :["+url+"] Successfully", crawlId,trackerId);
				//copy rawTemp, finalTemp folders to Final folders
				if(FINAL_CONTENT_TEMP_DIR != null && new File(FINAL_CONTENT_TEMP_DIR).exists()){
					cUtil.moveDirectory(FINAL_CONTENT_TEMP_DIR, domainVo.getFinal_content_directory());
				}
				if(RAW_CONTENT_TEMP_DIR != null && new File(RAW_CONTENT_TEMP_DIR).exists()){
					cUtil.moveDirectory(RAW_CONTENT_TEMP_DIR, domainVo.getRaw_content_directory());
				}
				LOG.info("REFRESHING SELECTED URLS COMPLETED");
				//purge API Call for selected URL's action through HM UI 
				for(String pUrl : urls){
					purgeURL(pUrl);
				}
				LOG.info("Purge Process Successfully Completed for Selected URL's:");
				cUtil.setCrawlStatus(conn, hmMessages.getString("refreshingSelectedURLSCompleted"), crawlId,trackerId,requestId);
				/*if(trackerId != 0){
				cUtil.setTrackerStatus(conn, trackerId, crawlId, "REFRESHING URL COMPLETED");
				}*/
			 }else{
				 errorMessage = hmMessages.getString("crawlIdIsNull");
				 LOG.error("Unable to get the crawl id in refreshSelectedURLS() method:");
				 cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), crawlId,trackerId,requestId);
			 }
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
		
	//Delete URL HTML File Process
	public void deleteURLHtml(String url, int domainId, String segmentId,int trackerId,int requestId){
		LOG.info("URL to be Delete:["+url+"]");
		DomainVO domainVo = new DomainVO();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		CrawlUtil cUtil = new CrawlUtil();
		String domainPath=null;
		int crawlId = 0;
		try {
			if (domainId == 0) {
				LOG.error("Domain id is null in deleteURLHtml() method: ");
				cUtil.setCrawlStatus(conn,
						hmMessages.getString("domainIdISNULL"), crawlId,
						trackerId, requestId);
				return;
			}
		} catch (Exception e1) {
			LOG.error("Unable to get the Domain in deleteSelectedURLHtml method:" + e1.getMessage());
		}
		try{
			conn = JDBCConnector.getConnection();
			crawlId = cUtil.getCrawlId(conn,domainId);
			if(crawlId != 0){
				cUtil.setCrawlStatus(conn, hmMessages.getString("deleteUrlHtmlStarted"), crawlId,trackerId,requestId);
				domainVo = domainCrud.readByPrimaryKey(domainId);
				if(url != null && !url.isEmpty()){
					String urlLoc = cUtil.getSpecificURLHTMLLOC(url, crawlId);
					if(urlLoc != null && !urlLoc.isEmpty()){
						domainPath=domainVo.getUrl();
						if(domainPath != null){
							if(urlLoc.contains(domainPath)){
								urlLoc = urlLoc.replace(domainPath, "");
							}
						}
						File file = null;
						if(domainVo.getFinal_content_directory() != null && !domainVo.getFinal_content_directory().isEmpty()){
							file = new File(domainVo.getFinal_content_directory()+urlLoc);
							if(file.exists()){
				    		if(file.delete()){
				    			cUtil.deleteTimeStamptoURL(urlLoc,crawlId);
				    			LOG.info(file + " is deleted!");
				    		}else{
				    			LOG.error("File is Not available:"+file);
				    		}
				    		//purge API Call for deleteURLHtml file action through HM UI
				    		purgeURL(url);
				    		//updating final result message in Request Processor table
				    		cUtil.setCrawlStatus(conn, hmMessages.getString("deleteUrlHtmlCompleted"), crawlId,trackerId,requestId);	
							}
						}else{
							LOG.error("Domain Final content Directory is null in deleteURLHtml() method:");
							cUtil.setCrawlStatus(conn, hmMessages.getString("finalDirectoryIsNull"), crawlId,trackerId,requestId);
						}
					}else{
						LOG.error("URL LOCATION IS NULL in deleteURLHtml() method:");
						cUtil.setCrawlStatus(conn, hmMessages.getString("urlLocIsNull"), crawlId,trackerId,requestId);
					}
				}else{
					errorMessage = hmMessages.getString("urlIsNull");
					LOG.error("Url is null in deleteURLHtml() method:");
					cUtil.setCrawlStatus(conn, hmMessages.getString("urlIsNull"), crawlId,trackerId,requestId);
				}
			}else{
				errorMessage = hmMessages.getString("crawlIdIsNull");
				LOG.error("Unable to get the crawl id in deleteURLHtml() method:");
				cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), crawlId,trackerId,requestId);
			}
	    	
		}catch(Exception e){
			LOG.error("Error while deleting URL Html file:"+e.getMessage());
		}
	}
	
	
		//Delete Selected URLS HTML Files 
	public void deleteSelectedURLHtml(String[] urls, int domainId,
			int trackerId, int requestId) {
		DomainVO domainVo = new DomainVO();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		CrawlUtil cUtil = new CrawlUtil();
		String domainPath = null;
		String urlLoc = null;
		int crawlId = 0;
		conn = JDBCConnector.getConnection();
		try {
			if (domainId != 0) {
				domainVo = domainCrud.readByPrimaryKey(domainId);
				domainPath = domainVo.getUrl();
			}else{
				LOG.error("Domain id is null in deleteSelectedURLHtml() method: ");
				cUtil.setCrawlStatus(conn,
						hmMessages.getString("domainIdISNULL"), crawlId,
						trackerId, requestId);
				return;
			}
		} catch (Exception e1) {
			LOG.error("Unable to get the Domain in deleteSelectedURLHtml method:" + e1.getMessage());
		}
		crawlId = cUtil.getCrawlId(conn,domainId);
		if(crawlId != 0){
		if (urls != null && urls.length > 0) {
			try {
				cUtil.setCrawlStatus(conn,
						hmMessages.getString("deleteSelectedUrlHtmlStarted"), crawlId,
						trackerId, requestId);
				for (String url : urls) {
					urlLoc = cUtil.getSpecificURLHTMLLOC(url, crawlId);
					if (urlLoc != null) {
						LOG.info("URL to be Delete:[" + urlLoc + "]");
						if (urlLoc.contains(domainPath)) {
							urlLoc = urlLoc.replace(domainPath, "");
						}
						if(domainVo.getFinal_content_directory() != null && !domainVo.getFinal_content_directory().isEmpty()){
							File file = new File(
									domainVo.getFinal_content_directory() + urlLoc);
							if (file.exists()) {
								if (file.delete()) {
									cUtil.deleteTimeStamptoURL(urlLoc, crawlId);
									LOG.info(file + " is deleted!");
								} else {
									LOG.error("File is Not available:" + file);
								}
							}
						}else{
							LOG.error("Domain Final content Directory is null in deleteSelectedURLHtml() method:");
							//errorMessage = "Domain Final content Directory is null";
							cUtil.setCrawlStatus(conn, hmMessages.getString("finalDirectoryIsNull"), crawlId,trackerId,requestId);
						}

					} else {
						LOG.error("URL_LOC doesn't exist for url : " + url);
					}
				}
				
				//purge API Call for delete selected URL HTML's action through HM UI 
				for(String pUrl : urls){
					purgeURL(pUrl);
				}
				LOG.info("Purge Process Successfully Completed for delete selected URL HTML's:");
				
				cUtil.setCrawlStatus(conn,
						hmMessages.getString("deleteSelectedUrlHtmlCompleted"), crawlId,
						trackerId, requestId);
			} catch (Exception e) {
				LOG.error("Error while deleting URL Html file:"
						+ e.getMessage());
			} 
		}
		}else{
			errorMessage = "Unable to get the crawl id";
			LOG.error("Unable to get the crawl id in deleteSelectedURLHtml() method:");
			try {
				cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), 0,trackerId,requestId);
			} catch (SQLException e) {
				LOG.error("Error while setting the crawl status in deleteSelectedURLHtml method: ");
			}
		}
	}
	
	//Delete Selected Segment HTML File Process
	public void deleteSelectedSegmentHtml(int domainId, String[] checkedSegments,int trackerId,int requestId){
		LOG.info("Segments to be Delete:["+checkedSegments+"]");
		DomainVO domainVo = new DomainVO();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		CrawlUtil cUtil = new CrawlUtil();
		SegmentMasterDAO smDAO = new SegmentMasterDAO();
		String domainPath=null;
		String urlLoc=null;
		File file = null;
		int crawlId = 0;
		if(domainId == 0){
			LOG.error("DOMAIN ID IS NULL IN deleteDomainHtml method: ");
			try {
				errorMessage = hmMessages.getString("domainIdISNULL");
				cUtil.setCrawlStatus(conn, hmMessages.getString("domainIdISNULL"), crawlId,trackerId,requestId);
			} catch (SQLException e) {
				LOG.error("Error while setting the crawl status in deleteSelectedURLFiles method: ");
			}
		}
		if(requestId == 0){
			errorMessage = "Request ID is null";
			LOG.error("request id is null in deleteDomainHtml() method:");
			return;
		}
		try{
			conn = JDBCConnector.getConnection();
			crawlId = cUtil.getCrawlId(conn,domainId);
			if(crawlId != 0){
				cUtil.setCrawlStatus(conn, hmMessages.getString("deleteSelectedSegmentHtmlStarted"), crawlId,trackerId,requestId);
				domainVo = domainCrud.readByPrimaryKey(domainId);
				if(checkedSegments != null && checkedSegments.length > 0){
				//cUtil.setCrawlStatus(conn, "DELETING SEGMENTS HTML STARTED", crawlId);
				List<String> deleteSegments = Arrays.asList(checkedSegments);
				domainPath=domainVo.getUrl();
				for(String segmentId: deleteSegments){
					LOG.info("Segment to be Delete:["+segmentId+"] Process Started:");
					Map<String, String> urlLocMapToTransform = new HashMap<String, String>();
					// Read urls for current Segment
					urlLocMapToTransform = smDAO.readUrlsHtmlLocForSegment(Integer.parseInt(segmentId),crawlId);
					if(!urlLocMapToTransform.isEmpty()){
						for(Map.Entry<String, String> entry : urlLocMapToTransform.entrySet()){
							//Read each urlLoc from segment and delete Html file
							urlLoc=entry.getKey();
							if(urlLoc != null){
								if(domainPath != null){
									if(urlLoc.contains(domainPath)){
										urlLoc = urlLoc.replace(domainPath, "");
									}
								}
								if(domainVo.getFinal_content_directory() != null && !domainVo.getFinal_content_directory().isEmpty()){
									file = new File(domainVo.getFinal_content_directory()+urlLoc);
						    		if(file.delete()){
						    			cUtil.deleteTimeStamptoURL(urlLoc,crawlId);
						    			LOG.info(file + " is deleted!");
						    		}else{
						    			LOG.error("File is Not available:"+file);
						    		}
								}else{
									LOG.error("Domain Final content Directory is null in deleteSelectedSegmentHtml() method:");
									errorMessage = hmMessages.getString("finalDirectoryIsNull");
									cUtil.setCrawlStatus(conn, hmMessages.getString("finalDirectoryIsNull"), crawlId,trackerId,requestId);
								}
							}else{
								LOG.error("URL LOCATION IS NULL in deleteSelectedSegmentHtml() method:");
							}
						}
					}else{
						LOG.error("NO urls found for the segment in URL_HTML_LOC: Segment is: "+segmentId);
					}
				}
				//purge API Call for selected segments HTML files to be delete action through HM UI
				String segPurgeURL = null;
				SegmentVO segVO = null;
				SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
				String isPurge = hmConfigValuesBundle.getString("makePurgeCallAfterDelete");
				if(isPurge != null && isPurge.equalsIgnoreCase("true")){
					for(String segId : deleteSegments){
						segVO = segmentCrud.readByPrimaryKey(segId,conn);
						if(segVO != null){
							segPurgeURL = segVO.getPurgeUrl();
							if(segPurgeURL != null){
								List<String> purgeUrlList = new ArrayList<String>();
								purgeUrlList = Arrays.asList(segPurgeURL.split(" "));
								if(purgeUrlList != null && !purgeUrlList.isEmpty()){
									for (String purgeUrl : purgeUrlList) {
										if(purgeUrl != null && !purgeUrl.isEmpty()){
											if(purgeUrl.equalsIgnoreCase("JSP")){
												List<String> urlsList = cUtil.readUrlsForSegment(segVO.getSegmentId(),crawlId);
												if(urlsList != null && !urlsList.isEmpty()){
													for(String JSPurl: urlsList){
														purgeURL(JSPurl);
													}
												}
											}else{
												purgeURL(purgeUrl);
											}
										}
									}
								}else{
									LOG.error("Purge URL list is Empty in deleteSelectedSegmentHtml() method: ");
								}
							}
						}else{
							LOG.info("Purge Process Disabled");
						}
					}
				}
			}
			cUtil.setCrawlStatus(conn, "DELETE SELECTED SEGMENT HTML PROCESS COMPLETED", crawlId,trackerId,requestId);	
		}else{
			errorMessage = hmMessages.getString("crawlIdIsNull");
			cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), 0,trackerId,requestId);
			LOG.error("Unable to get the crawl id in deleteDomainHtml method:");	
			}
	  }catch(Exception e){
			LOG.error("Error while deleting Segment Html file:"+e.getMessage());
		}finally {
				try {
				    if(conn != null){   
					 conn.close();
                    } 
					}
				 catch (SQLException e) {
					LOG.info("Error while closing connection in deleteSelectedSegmentHtml process:" + e);
				}
			}
	}
	
	//Delete Domain Html File Process
	public void deleteDomainHtml(int domainId,int trackerId,int requestId) throws IOException {
		CrawlUtil cUtil = new CrawlUtil();
		int crawlId = 0;
		if(domainId == 0){
			LOG.error("DOMAIN ID IS NULL IN deleteDomainHtml method: ");
			try {
				errorMessage = hmMessages.getString("domainIdISNULL");
				cUtil.setCrawlStatus(conn, hmMessages.getString("domainIdISNULL"), crawlId,trackerId,requestId);
			} catch (SQLException e) {
				LOG.error("Error while setting the crawl status in deleteSelectedURLFiles method: ");
			}
		}
		if(requestId == 0){
			LOG.error("request id is null in deleteDomainHtml() method:");
			return;
		}
		DomainVO domainVo = new DomainVO();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();
		SegmentMasterDAO smDAO = new SegmentMasterDAO();
		String urlLoc=null;
		String domainPath=null;
		File file=null;
		try{
			conn = JDBCConnector.getConnection();
			crawlId = cUtil.getCrawlId(conn,domainId);
			if(crawlId != 0){
			cUtil.setCrawlStatus(conn, hmMessages.getString("deleteDomainHtmlStarted"), crawlId,trackerId,requestId);
		    domainVo = domainCrud.readByPrimaryKey(domainId);
		    domainPath=domainVo.getUrl();
			//getting all the segments ids (exculdes dont crawl segments) 
			List<Integer> segmentIDS = cUtil.getCrawlSegmentIds(domainId, conn);
			if(!segmentIDS.isEmpty() && segmentIDS.size() > 0){
			for (Integer segmentId : segmentIDS) {
				LOG.info("Segment to be Delete:["+segmentId+"] Process Started:");
				Map<String, String> urlLocMapToTransform = new HashMap<String, String>();
				// Read urls for current Segment
				urlLocMapToTransform = smDAO.readUrlsHtmlLocForSegment(segmentId,crawlId);
				if(!urlLocMapToTransform.isEmpty()){
					for(Map.Entry<String, String> entry : urlLocMapToTransform.entrySet()){
						//Read each urlLoc from segment and delete Html file
						urlLoc=entry.getKey();
						if(urlLoc != null){
							if(domainPath != null){
								if(urlLoc.contains(domainPath)){
									urlLoc = urlLoc.replace(domainPath, "");
								}
							}
							if(domainVo.getFinal_content_directory() != null && !domainVo.getFinal_content_directory().isEmpty()){
							file = new File(domainVo.getFinal_content_directory()+urlLoc);
					    		if(file.delete()){
					    			cUtil.deleteTimeStamptoURL(urlLoc,crawlId);
					    			LOG.info(file + " is deleted!");
					    		}else{
					    			LOG.error("File is Not available:"+file);
					    		}
							}else{
								LOG.error("Domain Final content Directory is null in deleteDomainHtml() method:");
								errorMessage = hmMessages.getString("finalDirectoryIsNull");
								cUtil.setCrawlStatus(conn, hmMessages.getString("finalDirectoryIsNull"), crawlId,trackerId,requestId);
							}
				    		file = null;
						}else{
							LOG.error("URL LOCATION IS NULL in deleteDomainHtml() method:");
						}
					}
				}else{
					LOG.error("NO urls found for the segment in URL_HTML_LOC: Segment is: "+segmentId);
				}
			}
			//purge API Call for delete Domain HTML files action through HM UI
			String segPurgeURL = null;
			SegmentVO segVO = null;
			SegmentMasterCRUD segmentCrud = new SegmentMasterCRUD();
			String isPurge = hmConfigValuesBundle.getString("makePurgeCallAfterDelete");
			if(isPurge != null && isPurge.equalsIgnoreCase("true")){
				for(Integer segId : segmentIDS){
					segVO = segmentCrud.readByPrimaryKey(segId.toString(),conn);
					if(segVO != null){
						segPurgeURL = segVO.getPurgeUrl();
						if(segPurgeURL != null){
							List<String> purgeUrlList = new ArrayList<String>();
							purgeUrlList = Arrays.asList(segPurgeURL.split(" "));
							if(purgeUrlList != null && !purgeUrlList.isEmpty()){
								for (String purgeUrl : purgeUrlList) {
									if(purgeUrl != null && !purgeUrl.isEmpty()){
										if(purgeUrl.equalsIgnoreCase("JSP")){
											List<String> urlsList = cUtil.readUrlsForSegment(segVO.getSegmentId(),crawlId);
											if(urlsList != null && !urlsList.isEmpty()){
												for(String JSPurl: urlsList){
													purgeURL(JSPurl);
												}
											}
										}else{
											purgeURL(purgeUrl);
										}
									}
								}
							}else{
								LOG.error("Purge URL list is Empty in deleteDomainHtml() method: ");
							}
						}
					}
				}
				LOG.info("Purge Process Successfully Completed for Domain Level:");
			}else{
				LOG.info("Purge Process Disabled");
			}
			cUtil.setCrawlStatus(conn, hmMessages.getString("deleteDomainHtmlCompleted"), crawlId,trackerId,requestId);
			}else{
				errorMessage = hmMessages.getString("noCrawledSegments");
				cUtil.setCrawlStatus(conn, hmMessages.getString("noCrawledSegments"), 0,trackerId,requestId);
				LOG.error("NO Crawled segments configured to this domain in deleteDomainHtml method:");
			}
			}else{
				errorMessage = hmMessages.getString("crawlIdIsNull");
				cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), 0,trackerId,requestId);
				LOG.error("Unable to get the crawl id in deleteDomainHtml method:");
			}
		}catch(Exception e){
			LOG.error("Error while deleting Domain Html file:"+e.getMessage());
		}finally {
			try {
			     if(conn != null){	
				 conn.close();
                }     
				}
			 catch (SQLException e) {
				LOG.info("Error while closing connection in deleteDomainHtml process:" + e);
			}
		}
	}
   
	public void deleteSelectedURLFiles(int domainId,String url,String all,String recursive,int trackerId,int requestId){
		CrawlUtil cUtil = new CrawlUtil();
		int crawlId = 0;
		DomainVO domainVo = new DomainVO();
		DomainMasterCRUD domainCrud = new DomainMasterCRUD();		
		String finalPath = null;		
		String domainPath=null;
		conn = JDBCConnector.getConnection();
		if(url == null || url.isEmpty()){
			try {
				cUtil.setCrawlStatus(conn, hmMessages.getString("urlIsNull"), crawlId,trackerId,requestId);
			} catch (SQLException e) {
				LOG.error("Error while setting the crawl status in deleteSelectedURLFiles method: ");
			}
			LOG.error("URL is null in deleteSelectedURLFiles() method:");
			return;
		}
		if(requestId == 0){
			LOG.error("request id is null in deleteSelectedURLFiles() method:");
			return;
		}
		
		if(all == null || all == ""){
			all = "false";
		}
		if(recursive == null || recursive == ""){
			recursive = "false";
		}
		try {
				 if(domainId == 0){
					 domainId= cUtil.getUrlDomainId(url);
				 }
				 if(domainId != 0){
					 crawlId = cUtil.getCrawlId(conn,domainId);
					 if(crawlId != 0){
						domainVo = domainCrud.readByPrimaryKey(domainId);
						//domainPath=domainVo.getUrl();
						finalPath = domainVo.getFinal_content_directory();
						if(finalPath != null && !finalPath.isEmpty()){
						/*if(url.contains(domainPath))
							url = url.replace(domainPath, "");*/
						File deleteFilePath = new File(finalPath+url);
						cUtil.setCrawlStatus(conn, hmMessages.getString("deleteFilesStarted"), crawlId,trackerId,requestId);
						File htmlFilePath = null;
						if(url.contains(".jsp")){
							url = url.replace(".jsp","");
							deleteFilePath = new File(finalPath + url+"/");
							htmlFilePath = new File(finalPath + url +".html");
							if(deleteFilePath.exists()){
								cUtil.deleteAll(deleteFilePath, finalPath, crawlId);
								cUtil.setCrawlStatus(conn, hmMessages.getString("deleteFilesCompleted"), crawlId,trackerId,requestId);
							}else if(htmlFilePath.exists()) {
								cUtil.deleteAll(htmlFilePath, finalPath, crawlId);
								cUtil.setCrawlStatus(conn, hmMessages.getString("deleteFilesCompleted"), crawlId,trackerId,requestId);
							}else{
								LOG.error("Specified file doesn't exist :  " + deleteFilePath);
								//cUtil.setTrackerStatus(conn, trackerId, crawlId, "SPECIFIED FILE DOESN'T EXIST");
							}
						}else if(deleteFilePath.exists()){
							if(all.equalsIgnoreCase("true") && recursive.equalsIgnoreCase("false")){
								//deletes all the html files with in that folder
								cUtil.deleteAll(deleteFilePath,finalPath,crawlId);
							}else if(all.equalsIgnoreCase("false") && recursive.equalsIgnoreCase("true")){
								//deletes all the index.html files in root folder as well as in all subfolders
								cUtil.deleteRecursive(deleteFilePath,finalPath,crawlId);
							}else if(all.equalsIgnoreCase("true") && recursive.equalsIgnoreCase("true")){
								//deletes all the html files in root as well as in all subfolders
								cUtil.deleteAllRecursiveFiles(deleteFilePath,finalPath,crawlId);
							}else if(all.equalsIgnoreCase("false") && recursive.equalsIgnoreCase("false")){
								cUtil.deleteIndexFile(deleteFilePath,finalPath,crawlId);
							}
							cUtil.setCrawlStatus(conn, hmMessages.getString("deleteFilesCompleted"), crawlId,trackerId,requestId);
						}else{
							LOG.error("Specified file doesn't exist :  " + deleteFilePath);
							cUtil.setCrawlStatus(conn, hmMessages.getString("fileNotExist"), crawlId,trackerId,requestId);
							//cUtil.setTrackerStatus(conn, trackerId, crawlId, "SPECIFIED FILE DOESN'T EXIST");
						}
						}else{
							 cUtil.setCrawlStatus(conn, hmMessages.getString("finalDirectoryIsNull"), crawlId,trackerId,requestId);
							LOG.error("Domain final  html directory path is null");
						}
					 }else{
						 cUtil.setCrawlStatus(conn, hmMessages.getString("crawlIdIsNull"), crawlId,trackerId,requestId);
						 LOG.error("Crawl id is null in deleteSelectedURLFiles() method:");
					 }
				 }else{
					 cUtil.setCrawlStatus(conn, hmMessages.getString("domainIdISNULL"), crawlId,trackerId,requestId);
					 LOG.error("Domain id is null in deleteSelectedURLFiles() method: ");
				 }
				 
			} catch (Exception e) {
				LOG.error("Error in deleteSelectedURLFiles method:"+e.getMessage());
			}
			 finally{
						
				try {
					if(conn != null){	
						conn.close();
					}
				} catch (SQLException e) {
					LOG.error("Error while closing connection in deleteSelectedURLFiles method: " + e.getMessage());
				}
		}
	}
	
	//Purge API Call for API actions process block
	public void purgeURL(int domainId,String siteKey, String mediaPath,int trackerId,int requestId){
		CrawlUtil cUtil = new CrawlUtil();
		int crawlId = 0;
		try{
		conn = JDBCConnector.getConnection();
		crawlId = cUtil.getCrawlId(conn,domainId);
		if(crawlId != 0){
			Map<String,String> siteNameMap = cUtil.getSiteNamesMap();
			if(siteKey!=null && !siteKey.isEmpty()){
				String siteName = siteNameMap.get(siteKey);
				mediaPath = siteName + mediaPath; 
			}
			cUtil.setCrawlStatus(conn, hmMessages.getString("purgeUrlProcessStarted"), crawlId,trackerId,requestId);
			try {
				CrawlUtil.purgeAddTimeStamptoURL(mediaPath);
		        PurgeEdgeCastData pData = new PurgeEdgeCastData();
		        String purgeId = pData.getPurgeContentId(mediaPath, MEDIA_TYPE);
		        if(purgeId!=null){
		        	CrawlUtil.purgeUpdateTimeStamptoURL(mediaPath,purgeId);
		        }
		        cUtil.setCrawlStatus(conn, hmMessages.getString("purgeUrlProcessCompleted"), crawlId,trackerId,requestId);
			} catch (Exception e) {
				LOG.error("Error while purging through API Process:"+e.getMessage());
			}
		}else{
			LOG.error("Crawl Id is 0 : Unable to Process");
		}
		} catch (Exception e1) {
			LOG.error("Error in purgeURL method"+e1.getMessage());
		}finally{
			try {
				if(conn != null){	
					conn.close();
				}
			} catch (SQLException e) {
				LOG.error("Error while closing Statement in purgeURL API Call process:" + e.getMessage());
			}
			}
		}
	
	//purge API call for UI Actions process block
	public void purgeURL(String url) {
		//String HOST_NAME = "http://qa.plantronics.com";
		HOST_NAME = conf.get("hostName");
		if(HOST_NAME != null){
			if(url != null && !url.contains(":")) {
				PurgeEdgeCastData purgeECD = new PurgeEdgeCastData();
				String mediaPath = HOST_NAME + url;
				CrawlUtil.purgeAddTimeStamptoURL(mediaPath);
				String respCode = purgeECD.getPurgeContentId(mediaPath);
				if(respCode!=null){
		        	CrawlUtil.purgeUpdateTimeStamptoURL(mediaPath,respCode);
		        }
				LOG.info("PURGE API CALL for URL ["+ mediaPath+ "] :  Response Code: "+ respCode);
			}
		}else{
			LOG.error("HOST Name is Missed to purge the url");
		}
	}
	
	/**
	 * This method is used to Htmlize  selected domains
	 * @param domainList
	 * @param trackerId
	 * @param requestId
	 */
	public void selectedHtmlizeDomain(String domainList,int trackerId,int requestId){
		DomainMasterCRUD domainMasterCrud = new DomainMasterCRUD();
		CrawlUtil cUtil = new CrawlUtil();
		DomainVO domainVO = null;
		if(domainList != null && !domainList.isEmpty()){
			String[] domain_list = domainList.split(",");
			for (String domainId : domain_list) {
				try {
					if(domainId != null && domainId != ""){
						try {
								domainVO=domainMasterCrud.readByPrimaryKey(Integer.parseInt(domainId));
						} catch (Exception e) {
								LOG.error("Error while getting DOMAINVO in selectedHtmlizeDomain() method: "+e.getMessage());
						}
						if(domainVO != null){
							LOG.info("HTMLIZATION DOMAIN STARTING TO "+domainVO.getDomainName() +" DOMAIN");
						}
							autoCrawl(Integer.parseInt(domainId), trackerId, requestId);
						/*if(domainVO != null){	
							LOG.info("HTMLIZATION DOMAIN COMPLETED TO "+domainVO.getDomainName()+" DOMAIN");
						}*/
					}else{
						LOG.error("Domain id is null or empty in selectedHtmlizeDomain() method:");
					}
				} catch (NumberFormatException e) {
					LOG.error("Error in selectedHtmlizeDomain() method: "+e.getMessage());
				} catch (IOException e) {
					LOG.error("Error in selectedHtmlizeDomain() method: "+e.getMessage());
				} catch (SQLException e) {
					LOG.error("Error in selectedHtmlizeDomain() method: "+e.getMessage());
				}
			}
		}
	}

}
