package com.om.domain;

public class AspectResult {

	private String aspect;

	private Polarity polarity;

	private Double finalAspectScore;

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

	public Double getFinalAspectScore() {
		return finalAspectScore;
	}

	public void setFinalAspectScore(Double finalAspectScore) {
		this.finalAspectScore = finalAspectScore;
	}

}
