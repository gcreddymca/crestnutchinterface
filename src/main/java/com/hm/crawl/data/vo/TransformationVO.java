package com.hm.crawl.data.vo;

/**
 * Value Object class for Transformation.
 * 
 * 
 */
public class TransformationVO {
	private String transformationType;
	private String transformationPriority;
	private String transformationId;


	public String getTransformationType() {
		return transformationType;
	}

	public void setTransformationType(String transformationType) {
		this.transformationType = transformationType;
	}

	public String getTransformationPriority() {
		return transformationPriority;
	}

	public void setTransformationPriority(String transformationPriority) {
		this.transformationPriority = transformationPriority;
	}

	/**
	 * @return the transformationId
	 */
	public String getTransformationId() {
		return transformationId;
	}

	/**
	 * @param transformationId the transformationId to set
	 */
	public void setTransformationId(String transformationId) {
		this.transformationId = transformationId;
	}

}
