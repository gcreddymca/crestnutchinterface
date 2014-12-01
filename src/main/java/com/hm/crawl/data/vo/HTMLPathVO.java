package com.hm.crawl.data.vo;

import java.util.List;

/**
 *
 */
public class HTMLPathVO {
	
	private int pattern_id;
	private int segment_id;
	private boolean isDefault;
	private String filetype;
	private String fileName;
	private String fileExt;
	private List<FolderType> folderType;
	
	public class FolderType{
		private String folderType;
		private String folderName;
		
		/**
		 * @return the folderType
		 */
		public String getFolderType() {
			return folderType;
		}
		/**
		 * @param folderType the folderType to set
		 */
		public void setFolderType(String folderType) {
			this.folderType = folderType;
		}
		/**
		 * @return the folderName
		 */
		public String getFolderName() {
			return folderName;
		}
		/**
		 * @param folderName the folderName to set
		 */
		public void setFolderName(String folderName) {
			this.folderName = folderName;
		}
	}

	/**
	 * @return the pattern_id
	 */
	public int getPattern_id() {
		return pattern_id;
	}

	/**
	 * @param pattern_id the pattern_id to set
	 */
	public void setPattern_id(int pattern_id) {
		this.pattern_id = pattern_id;
	}

	/**
	 * @return the isDefault
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @param isDefault the isDefault to set
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @return the filetype
	 */
	public String getFiletype() {
		return filetype;
	}

	/**
	 * @param filetype the filetype to set
	 */
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileExt
	 */
	public String getFileExt() {
		return fileExt;
	}

	/**
	 * @param fileExt the fileExt to set
	 */
	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	/**
	 * @return the folderType
	 */
	public List<FolderType> getFolderType() {
		return folderType;
	}

	/**
	 * @param folderType the folderType to set
	 */
	public void setFolderType(List<FolderType> folderType) {
		this.folderType = folderType;
	}

	/**
	 * @return the segment_id
	 */
	public int getSegment_id() {
		return segment_id;
	}

	/**
	 * @param segment_id the segment_id to set
	 */
	public void setSegment_id(int segment_id) {
		this.segment_id = segment_id;
	}

}
