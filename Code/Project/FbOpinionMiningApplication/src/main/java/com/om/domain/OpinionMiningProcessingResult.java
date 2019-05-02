package com.om.domain;

import java.util.List;
import java.util.Map;

public class OpinionMiningProcessingResult {

	private Map<String, List<Double>> aspectResultsMap;

	private Map<String, Double> sentenceResultsMap;

	private Double minSentenceScore;

	private Double maxSentenceScore;

	public Map<String, List<Double>> getAspectResultsMap() {
		return aspectResultsMap;
	}

	public void setAspectResultsMap(Map<String, List<Double>> aspectResultsMap) {
		this.aspectResultsMap = aspectResultsMap;
	}

	public Map<String, Double> getSentenceResultsMap() {
		return sentenceResultsMap;
	}

	public void setSentenceResultsMap(Map<String, Double> sentenceResultsMap) {
		this.sentenceResultsMap = sentenceResultsMap;
	}

	public Double getMinSentenceScore() {
		return minSentenceScore;
	}

	public void setMinSentenceScore(Double minSentenceScore) {
		this.minSentenceScore = minSentenceScore;
	}

	public Double getMaxSentenceScore() {
		return maxSentenceScore;
	}

	public void setMaxSentenceScore(Double maxSentenceScore) {
		this.maxSentenceScore = maxSentenceScore;
	}

}
