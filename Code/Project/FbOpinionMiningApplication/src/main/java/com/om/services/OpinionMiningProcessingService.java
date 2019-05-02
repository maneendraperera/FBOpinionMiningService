package com.om.services;

import java.util.List;
import java.util.Map;

import com.om.domain.OpinionMiningProcessingResult;

import edu.stanford.nlp.ling.TaggedWord;

public interface OpinionMiningProcessingService {

	public OpinionMiningProcessingResult doProcessingOnTaggedCommentsSentences(
			List<List<TaggedWord>> preprocessedAndTaggedSentenceList, List<String> frequentAspectList);

	public Map<List<TaggedWord>, List<String>> removeObjectiveSentencesFromComments(
			List<List<TaggedWord>> preprocessedAndTaggedSentenceList, List<String> frequentAspectList);
}
