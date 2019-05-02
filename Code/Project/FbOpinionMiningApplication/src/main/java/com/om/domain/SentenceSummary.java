package com.om.domain;

import java.util.List;

public class SentenceSummary {

	private List<SentenceResult> sentenceResultList;

	private int noOfPositiveSentences;

	private int noOfNegativeSentences;

	private int noOfNeutralSentences;

	public List<SentenceResult> getSentenceResultList() {
		return sentenceResultList;
	}

	public void setSentenceResultList(List<SentenceResult> sentenceResultList) {
		this.sentenceResultList = sentenceResultList;
	}

	public int getNoOfPositiveSentences() {
		return noOfPositiveSentences;
	}

	public void setNoOfPositiveSentences(int noOfPositiveSentences) {
		this.noOfPositiveSentences = noOfPositiveSentences;
	}

	public int getNoOfNegativeSentences() {
		return noOfNegativeSentences;
	}

	public void setNoOfNegativeSentences(int noOfNegativeSentences) {
		this.noOfNegativeSentences = noOfNegativeSentences;
	}

	public int getNoOfNeutralSentences() {
		return noOfNeutralSentences;
	}

	public void setNoOfNeutralSentences(int noOfNeutralSentences) {
		this.noOfNeutralSentences = noOfNeutralSentences;
	}

}
