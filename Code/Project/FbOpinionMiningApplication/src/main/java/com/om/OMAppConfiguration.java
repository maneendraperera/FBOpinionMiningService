package com.om;

public class OMAppConfiguration {

	private String stopWordFilePath;
	private String maxentTaggerFilePath;
	private String fbPostCommentsSaveFilePath;
	private String aspectsFilePath;
	private String aspectsSaveFilePath;
	private String sentiwordnetFilePath;
	private String fbCommentRetreivalUrl;
	private String defaultMinimumSupport;
	private String weightage;
	private String fbAppId;
	private String fbAppSecret;
	private String fbAppAcessToken;

	public String getStopWordFilePath() {
		return stopWordFilePath;
	}

	public void setStopWordFilePath(String stopWordFilePath) {
		this.stopWordFilePath = stopWordFilePath;
	}

	public String getMaxentTaggerFilePath() {
		return maxentTaggerFilePath;
	}

	public void setMaxentTaggerFilePath(String maxentTaggerFilePath) {
		this.maxentTaggerFilePath = maxentTaggerFilePath;
	}

	public String getFbPostCommentsSaveFilePath() {
		return fbPostCommentsSaveFilePath;
	}

	public void setFbPostCommentsSaveFilePath(String fbPostCommentsSaveFilePath) {
		this.fbPostCommentsSaveFilePath = fbPostCommentsSaveFilePath;
	}

	public String getAspectsFilePath() {
		return aspectsFilePath;
	}

	public void setAspectsFilePath(String aspectsFilePath) {
		this.aspectsFilePath = aspectsFilePath;
	}

	public String getAspectsSaveFilePath() {
		return aspectsSaveFilePath;
	}

	public void setAspectsSaveFilePath(String aspectsSaveFilePath) {
		this.aspectsSaveFilePath = aspectsSaveFilePath;
	}

	public String getSentiwordnetFilePath() {
		return sentiwordnetFilePath;
	}

	public void setSentiwordnetFilePath(String sentiwordnetFilePath) {
		this.sentiwordnetFilePath = sentiwordnetFilePath;
	}

	public String getFbCommentRetreivalUrl() {
		return fbCommentRetreivalUrl;
	}

	public void setFbCommentRetreivalUrl(String fbCommentRetreivalUrl) {
		this.fbCommentRetreivalUrl = fbCommentRetreivalUrl;
	}

	public String getDefaultMinimumSupport() {
		return defaultMinimumSupport;
	}

	public void setDefaultMinimumSupport(String defaultMinimumSupport) {
		this.defaultMinimumSupport = defaultMinimumSupport;
	}

	public String getWeightage() {
		return weightage;
	}

	public void setWeightage(String weightage) {
		this.weightage = weightage;
	}

	public String getFbAppId() {
		return fbAppId;
	}

	public void setFbAppId(String fbAppId) {
		this.fbAppId = fbAppId;
	}

	public String getFbAppSecret() {
		return fbAppSecret;
	}

	public void setFbAppSecret(String fbAppSecret) {
		this.fbAppSecret = fbAppSecret;
	}

	public String getFbAppAcessToken() {
		return fbAppAcessToken;
	}

	public void setFbAppAcessToken(String fbAppAcessToken) {
		this.fbAppAcessToken = fbAppAcessToken;
	}

}