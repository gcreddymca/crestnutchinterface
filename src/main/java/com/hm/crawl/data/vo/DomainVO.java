package com.hm.crawl.data.vo;

public class DomainVO {
	private int domainId;
	private String domainName;
	private String url;
	private String seedUrl;
	private String raw_content_directory;
	private String final_content_directory;
	private String final_content_temp_dir;
	private String crawlTempDirectory;
	private String raw_content_temp_dir;

	public int getDomainId() {
		return domainId;
	}

	public void setDomainId(int domainId) {
		this.domainId = domainId;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSeedUrl() {
		return seedUrl;
	}

	public void setSeedUrl(String seedUrl) {
		this.seedUrl = seedUrl;
	}

	public String getRaw_content_directory() {
		return raw_content_directory;
	}

	public void setRaw_content_directory(String raw_content_directory) {
		this.raw_content_directory = raw_content_directory;
	}

	public String getFinal_content_directory() {
		return final_content_directory;
	}

	public void setFinal_content_directory(String final_content_directory) {
		this.final_content_directory = final_content_directory;
	}

	public String getCrawlTempDirectory() {
		return crawlTempDirectory;
	}

	public void setCrawlTempDirectory(String crawlTempDirectory) {
		this.crawlTempDirectory = crawlTempDirectory;
	}

	public String getFinalContenttempDir() {
		return final_content_temp_dir;
	}

	public void setFinalContenttempDir(String final_content_temp_dir) {
		this.final_content_temp_dir = final_content_temp_dir;
	}

	public String getRawContenttempDir() {
		return raw_content_temp_dir;
	}

	public void setRawContenttempDir(String raw_content_temp_dir) {
		this.raw_content_temp_dir = raw_content_temp_dir;
	}
	
}
