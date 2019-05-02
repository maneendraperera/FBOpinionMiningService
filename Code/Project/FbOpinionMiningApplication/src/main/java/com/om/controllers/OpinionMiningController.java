package com.om.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.om.OMAppConfiguration;
import com.om.config.Constants;
import com.om.domain.AspectResult;
import com.om.domain.Comment;
import com.om.domain.FbComment;
import com.om.domain.OpinionMiningProcessingResult;
import com.om.domain.Polarity;
import com.om.domain.SentenceResult;
import com.om.domain.SentenceSummary;
import com.om.services.OpinionMiningPreProcessingService;
import com.om.services.OpinionMiningProcessingService;

/**
 * Opinion mining controller. Handles the opinion mining process.
 * 
 * @author Maneendra
 *
 */
@Controller
public class OpinionMiningController {

	@Autowired
	private OpinionMiningPreProcessingService opinionMiningPreProcessingService;

	@Autowired
	private OpinionMiningProcessingService opinionMiningProcessingService;

	@Autowired
	private OMAppConfiguration omAppConfiguration;

	private Logger logger = LoggerFactory.getLogger(OpinionMiningController.class);

	/**
	 * Main entry point of the system.
	 * 
	 * @param postId
	 * @param model
	 * @param request
	 * @return String result
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/retreivecomments", method = RequestMethod.POST)
	public String retreiveComments(@RequestParam(value = "postid", required = false) String postId, Model model,
			HttpServletRequest request) {

		logger.info("========== Inside retreive comments controller. ==========");
		logger.info("========== Post Id is : ==========" + postId);

		// Retrieve comments from fb
		List<String> fbPostCommentsList = reteriveCommentsFromFbForPostId(postId);

		if (fbPostCommentsList.get(0).equals(Constants.ERROR)) {
			return "home";
		}

		// Do pre processing on comments
		Map<String, List> preprocessedMap = opinionMiningPreProcessingService
				.doPreprocessingOnComments(fbPostCommentsList);

		// Mine opinions from comments
		OpinionMiningProcessingResult processedResult = opinionMiningProcessingService
				.doProcessingOnTaggedCommentsSentences(preprocessedMap.get(Constants.TAGGED_SENTENCE_LIST),
						preprocessedMap.get(Constants.FREQUENT_ASPECT_LIST));

		// Process opinion mining results
		Map<String, List<Double>> aspectScoreMap = processedResult.getAspectResultsMap();

		// Create aspect score table
		if (aspectScoreMap != null && aspectScoreMap.size() > 0) {
			List<AspectResult> aspectResultList = createAspectTable(aspectScoreMap);
			model.addAttribute("aspectTable", aspectResultList);
		}

		// Create sentence score table
		if (processedResult.getSentenceResultsMap() != null && processedResult.getSentenceResultsMap().size() > 0) {
			SentenceSummary summary = createSentenceTable(processedResult.getSentenceResultsMap(),
					processedResult.getMinSentenceScore(), processedResult.getMaxSentenceScore());
			model.addAttribute("sentenceTable", summary.getSentenceResultList());
			model.addAttribute("noOfSentences", summary.getSentenceResultList().size());
			model.addAttribute("noOfPositiveSentences", summary.getNoOfPositiveSentences());
			model.addAttribute("noOfNegativeSentences", summary.getNoOfNegativeSentences());
			model.addAttribute("noOfNeutralSentences", summary.getNoOfNeutralSentences());
		}

		return "result";
	}

	/**
	 * This method creates the sentence level score table.
	 * 
	 * @param sentenceResultsMap
	 * @param minSentenceScore
	 * @param maxSentenceScore
	 * @return sentence result list
	 */
	public SentenceSummary createSentenceTable(Map<String, Double> sentenceResultsMap, double minSentenceScore,
			double maxSentenceScore) {
		SentenceSummary summary = new SentenceSummary();
		List<SentenceResult> sentenceResultList = new ArrayList<>();
		int noOfPositiveSentences = 0;
		int noOfNegativeSentences = 0;
		int noOfNeutralSentences = 0;

		for (Map.Entry<String, Double> mapEntry : sentenceResultsMap.entrySet()) {
			int rating = findRating(mapEntry.getValue(), minSentenceScore, maxSentenceScore);
			SentenceResult sentenceResult = new SentenceResult();
			if (mapEntry.getValue() == 0) {
				sentenceResult.setPolarity(Polarity.NEUTRAL);
				noOfNeutralSentences++;
			} else if (mapEntry.getValue() > 0) {
				sentenceResult.setPolarity(Polarity.POSITIVE);
				noOfPositiveSentences++;
			} else {
				sentenceResult.setPolarity(Polarity.NEGATIVE);
				noOfNegativeSentences++;
			}

			// Format the sentence by removing [] and /{tagger}
			String sentence = mapEntry.getKey().replaceAll(Constants.TAG_PATTERN, Constants.EMPTY_STRING)
					.replaceAll(Constants.STARTING_SQUARE_BRACKET_PATTERN, Constants.EMPTY_STRING)
					.replaceAll(Constants.ENDING_SQUARE_BRACKET_PATTERN, Constants.EMPTY_STRING)
					.replaceAll(Constants.FULL_STOP_PATTERN, Constants.EMPTY_STRING)
					.replaceAll(Constants.COMMA_PATTERN, Constants.EMPTY_STRING);
			sentenceResult.setSentence(sentence);

			sentenceResult.setScore(mapEntry.getValue());
			sentenceResult.setRating(rating);

			sentenceResultList.add(sentenceResult);
		}

		summary.setNoOfNegativeSentences(noOfNegativeSentences);
		summary.setNoOfPositiveSentences(noOfPositiveSentences);
		summary.setNoOfNeutralSentences(noOfNeutralSentences);
		summary.setSentenceResultList(sentenceResultList);
		return summary;
	}

	/**
	 * This method finds rating of the sentence. Rating is calculated only for
	 * both positive and negative sentences
	 * 
	 * @param score
	 * @param minSentenceScore
	 * @param maxSentenceScore
	 * @return int rating
	 */
	private int findRating(double score, double minSentenceScore, double maxSentenceScore) {
		int rating = 0;
		double gap = 0;
		if (score >= 0) {
			gap = (maxSentenceScore - 0) / 5;
			if (score == 0) {
				return 0;
			}
			if (score >= 0 && score <= (gap)) {
				return 1;
			}
			if (score >= (gap) && score <= (2 * gap)) {
				return 2;
			}
			if (score >= (2 * gap) && score <= (3 * gap)) {
				return 3;
			}
			if (score >= (3 * gap) && score <= (4 * gap)) {
				return 4;
			}
			if (score >= (4 * gap) && score <= (maxSentenceScore)) {
				return 5;
			}
		} else {
			gap = (minSentenceScore) / 5;
			if (score < 0 && score >= (gap)) {
				return -1;
			}
			if (score < (gap) && score >= (2 * gap)) {
				return -2;
			}
			if (score < (2 * gap) && score >= (3 * gap)) {
				return -3;
			}
			if (score < (3 * gap) && score >= (4 * gap)) {
				return -4;
			}
			if (score < (4 * gap) && score >= (maxSentenceScore)) {
				return -5;
			}
		}
		return rating;
	}

	/**
	 * This method creates the aspect score table.
	 * 
	 * @param aspectScoreMap
	 * @return aspect result list
	 */
	public List<AspectResult> createAspectTable(Map<String, List<Double>> aspectScoreMap) {
		List<AspectResult> resultList = new ArrayList<>();
		for (Map.Entry<String, List<Double>> mapEntry : aspectScoreMap.entrySet()) {
			double aggregatePositiveScore = 0;
			double aggregateNegativeScore = 0;
			AspectResult result = new AspectResult();
			result.setAspect(mapEntry.getKey());
			for (Double value : mapEntry.getValue()) {
				if (value >= 0) {
					aggregatePositiveScore = aggregatePositiveScore + value;
				} else {
					aggregateNegativeScore = aggregateNegativeScore + value;
				}
			}
			result = findPolarity(result, aggregatePositiveScore, aggregateNegativeScore);
			resultList.add(result);
		}
		return resultList;
	}

	/**
	 * This method finds the polarity of aspect.
	 * 
	 * @param aspectResult
	 * @param aggregatePositiveScore
	 * @param aggregateNegativeScore
	 * @return aspect result
	 */
	private AspectResult findPolarity(AspectResult aspectResult, double aggregatePositiveScore,
			double aggregateNegativeScore) {

		logger.info("========== Aggrigate scores are : " + aggregatePositiveScore + " : " + aggregateNegativeScore
				+ " ==========");

		if (aggregatePositiveScore == 0.0 && aggregateNegativeScore == 0.0) {
			aspectResult.setFinalAspectScore(aggregatePositiveScore);
			aspectResult.setPolarity(Polarity.NEUTRAL);
			return aspectResult;
		}

		if (aggregatePositiveScore > Math.abs(aggregateNegativeScore)) {
			aspectResult.setFinalAspectScore(aggregatePositiveScore);
			aspectResult.setPolarity(Polarity.POSITIVE);
			return aspectResult;
		}

		aspectResult.setFinalAspectScore(aggregateNegativeScore);
		aspectResult.setPolarity(Polarity.NEGATIVE);
		return aspectResult;
	}

	/**
	 * This method retrieves the Facebook post comments via Graph API
	 * 
	 * @param postId
	 * @return fbcomment string list
	 */
	private List<String> reteriveCommentsFromFbForPostId(String postId) {

		List<String> fbPostCommentsList = new ArrayList<>();

		String accessToken = omAppConfiguration.getFbAppAcessToken();
		logger.info("========== Fb access token is : ==========" + accessToken);

		Facebook facebook = new FacebookTemplate(accessToken);
		String fbCommentsUrl = "https://graph.facebook.com/v2.7/" + postId + "/comments?";
		logger.info("========== Fb comments url is : ==========" + fbCommentsUrl);
		FbComment fbcomments = null;

		try {
			fbcomments = facebook.restOperations().getForObject(fbCommentsUrl, FbComment.class);
		} catch (Exception e) {
			fbPostCommentsList.add(Constants.ERROR);
			fbPostCommentsList.add(e.getMessage());
			return fbPostCommentsList;
		}

		if (fbcomments.getData() != null) {
			List<Comment> comments = fbcomments.getData();

			// Set<String> idSet = new HashSet<>();
			for (Comment comment : comments) {

				// Check for opinion spamming, check whether the id is unique
				// For testing purpose initialize here, because all the fb
				// comments are posted by a single user. In real scenario
				// initialize the variable above. uncomment the line above for
				// loop.
				Set<String> idSet = new HashSet<>();
				logger.info("========== From id : ==========" + comment.getFrom().getId());
				if (idSet.add(comment.getFrom().getId())) {
					logger.info("========== Message : ==========" + comment.getMessage());
					String commentFb = comment.getMessage();
					commentFb = commentFb.toLowerCase();
					// Add a full stop if comment does not ends with . or ?
					if (commentFb.endsWith(Constants.QUESTION_MARK) || commentFb.endsWith(Constants.FULL_STOP)) {
						fbPostCommentsList.add(commentFb);
					} else {
						String commentString = commentFb + Constants.FULL_STOP;
						fbPostCommentsList.add(commentString);
					}
				}
			}
		}
		return fbPostCommentsList;
	}

}
