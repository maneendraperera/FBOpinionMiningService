package com.om;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.om.common.FileLoader;
import com.om.common.FileWriter;
import com.om.config.Constants;
import com.om.controllers.OpinionMiningController;
import com.om.domain.AspectResult;
import com.om.domain.OpinionMiningProcessingResult;
import com.om.domain.SentenceResult;
import com.om.domain.SentenceSummary;
import com.om.services.OpinionMiningPreProcessingService;
import com.om.services.OpinionMiningProcessingService;
import com.om.util.PorterStemmer;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { FbOpinionMiningApplication.class, InitConfig.class })
public class FbOpinionMiningApplicationTests {

	@Autowired
	OpinionMiningPreProcessingService opinionMiningPreProcessingService;

	@Autowired
	FileWriter fileWriter;

	@Autowired
	OpinionMiningProcessingService opinionMiningProcessingService;

	@Autowired
	PorterStemmer stemmer;

	@Autowired
	OpinionMiningController opinionMiningController;

	@Autowired
	OMAppConfiguration omAppConfiguration;

	@Autowired
	FileLoader fileLoader;

	private Logger logger = LoggerFactory.getLogger(OpinionMiningController.class);

	@Test
	public void contextLoads() {
		System.out.println("running test cases");
	}

	// Test removing question based comments
	@Test
	public void testRemoveQuestionBasedComments() {

		List<String> commentsList = new ArrayList<>();
		commentsList.add("Did I mention the note feature and how you can send notes via Bluetooth?");
		commentsList.add("I've had this beauty for nearly 2 months now and I truely love it.");
		commentsList.add("The only disappointment so far has been battery life.");

		List<String> filteredCommentsList = opinionMiningPreProcessingService.removeQuestionBasedComments(commentsList);

		assertNotNull("Filtered comment list should not be null : " + filteredCommentsList);
		assertThat("Filtered comment list should contain 2 sentences" + (filteredCommentsList.size() == 2));
	}

	// Test pre processing and processing comments
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testPreProcessingComments() {

		List<String> commentsList = new ArrayList<>();
		commentsList.add("I've had this beauty for nearly 2 months now and I truely love it.");

		Map<String, List> preprocessedMap = opinionMiningPreProcessingService.doPreprocessingOnComments(commentsList);

		// Expected output
		MaxentTagger tagger = new MaxentTagger(omAppConfiguration.getMaxentTaggerFilePath());
		String taggedString = tagger.tagString("I've had this beauty for nearly 2 months now and I truely love it.");
		String[] splittedTaggedArray = taggedString.split(" ");
		String[] firstTagArray = splittedTaggedArray[0].split("_");
		String firstTagOfExpectedString = firstTagArray[1];

		// Actual Output
		List<List<TaggedWord>> taggedSentenceList = preprocessedMap.get(Constants.TAGGED_SENTENCE_LIST);
		String firstTagOfActualString = taggedSentenceList.get(0).get(0).tag().toString();

		assertNotNull("Preprocessed and tagged sentence list should not be null : ", taggedSentenceList.get(0));
		assertEquals("First tag should be equal in the string : ", firstTagOfExpectedString, firstTagOfActualString);

		OpinionMiningProcessingResult processedResult = opinionMiningProcessingService
				.doProcessingOnTaggedCommentsSentences(preprocessedMap.get(Constants.TAGGED_SENTENCE_LIST),
						preprocessedMap.get(Constants.FREQUENT_ASPECT_LIST));

		Map<String, List<Double>> aspectScoreMap = processedResult.getAspectResultsMap();

		if (aspectScoreMap != null && aspectScoreMap.size() > 0) {
			List<AspectResult> aspectResultList = opinionMiningController.createAspectTable(aspectScoreMap);
			for (AspectResult result : aspectResultList) {
				logger.info("Aspect : " + result.getAspect());
				logger.info("Final Aspect Score: " + result.getFinalAspectScore());
				logger.info("Polarity : " + result.getPolarity());
			}
		}

		if (processedResult.getSentenceResultsMap() != null && processedResult.getSentenceResultsMap().size() > 0) {
			SentenceSummary summary = opinionMiningController.createSentenceTable(
					processedResult.getSentenceResultsMap(), processedResult.getMinSentenceScore(),
					processedResult.getMaxSentenceScore());
			for (SentenceResult result : summary.getSentenceResultList()) {
				logger.info("Sentence : " + result.getSentence());
				logger.info("Score: " + result.getScore());
				logger.info("Rating: " + result.getRating());
				logger.info("Polarity : " + result.getPolarity());
			}
		}
	}

	// Test Facebook comments are successfully saved in the file
	@Test
	public void testSaveFbCommentsToTheFile() {

		List<String> commentsList = new ArrayList<>();
		commentsList.add("Did I mention the note feature and how you can send notes via Bluetooth?");
		commentsList.add("I've had this beauty for nearly 2 months now and I truely love it.");
		commentsList.add("The only disappointment so far has been battery life.");

		fileWriter.writeToFile(omAppConfiguration.getFbPostCommentsSaveFilePath(), commentsList);

		String fileContent = null;
		try {
			fileContent = fileLoader.readFileAsString(omAppConfiguration.getFbPostCommentsSaveFilePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertNotNull("Facebook comments cannot be null.", fileContent);
	}

	// Test extracting frequent aspects
	@Test
	public void testAspectExtraction() {

		List<String> commentsList = new ArrayList<>();
		commentsList.add("I've had this beauty for nearly 2 months now and I truely love it.");
		commentsList.add("The only disappointment so far has been battery life.");

		Map<String, String> extractedAspectsMap = new HashMap<>();
		extractedAspectsMap.put("", "");
		extractedAspectsMap.put("", "");

		List<String> frequnetAspectsList = opinionMiningPreProcessingService.findFrequentAspects(extractedAspectsMap);
		assertTrue("Frequent aspect list should not be empty.", frequnetAspectsList.size() > 0);
	}

	// Test processing and results
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testProcessingAndResults() {

		List<String> commentsList = null;
		try {
			// To test positive comments
			// commentsList =
			// fileLoader.readFileAsWords("..//FbOpinionMiningApplication//src//test//resources//test_comments_positive.txt");

			// To test negative comments
			commentsList = fileLoader.readFileAsWords(
					"..//FbOpinionMiningApplication//src//test//resources//test_comments_negative.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, List> preprocessedMap = opinionMiningPreProcessingService.doPreprocessingOnComments(commentsList);

		OpinionMiningProcessingResult processedResult = opinionMiningProcessingService
				.doProcessingOnTaggedCommentsSentences(preprocessedMap.get(Constants.TAGGED_SENTENCE_LIST),
						preprocessedMap.get(Constants.FREQUENT_ASPECT_LIST));

		Map<String, List<Double>> aspectScoreMap = processedResult.getAspectResultsMap();

		if (aspectScoreMap != null && aspectScoreMap.size() > 0) {
			List<AspectResult> aspectResultList = opinionMiningController.createAspectTable(aspectScoreMap);
			for (AspectResult result : aspectResultList) {
				logger.info("Aspect : " + result.getAspect() + " Final Aspect Score: " + result.getFinalAspectScore()
						+ " Polarity : " + result.getPolarity());
			}
		}

		if (processedResult.getSentenceResultsMap() != null && processedResult.getSentenceResultsMap().size() > 0) {
			SentenceSummary summary = opinionMiningController.createSentenceTable(
					processedResult.getSentenceResultsMap(), processedResult.getMinSentenceScore(),
					processedResult.getMaxSentenceScore());
			for (SentenceResult result : summary.getSentenceResultList()) {
				logger.info("Final Sentence : " + result.getSentence() + " Score: " + result.getScore() + " Rating: "
						+ result.getRating() + " Polarity : " + result.getPolarity());
			}
		}
	}

}
