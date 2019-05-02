package com.om.services;

import java.util.List;
import java.util.Map;

public interface OpinionMiningPreProcessingService {

	public List<String> removeQuestionBasedComments(List<String> commentsList);

	@SuppressWarnings("rawtypes")
	public Map<String, List> doPreprocessingOnComments(List<String> fbPostCommentsList);

	public List<String> findFrequentAspects(Map<String, String> extractedAspectsMap);

}
