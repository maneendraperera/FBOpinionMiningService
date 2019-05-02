package com.om.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.om.OMAppConfiguration;
import com.om.common.FileLoader;
import com.om.common.FileWriter;
import com.om.config.Constants;
import com.om.util.PorterStemmer;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * OpinionMiningPreProcessingServiceImpl class implements the methods used to
 * pre process the Facebook post comments
 * 
 * @author Maneendra
 *
 */
@Service
public class OpinionMiningPreProcessingServiceImpl implements OpinionMiningPreProcessingService {

	private Logger logger = LoggerFactory.getLogger(OpinionMiningPreProcessingServiceImpl.class);

	@Autowired
	private FileWriter fileWriter;

	@Autowired
	private PorterStemmer stemmer;

	@Autowired
	private OMAppConfiguration omAppConfiguration;

	@Autowired
	private FileLoader fileLoader;

	@Autowired
	private AlgoApriori apriori;

	private MaxentTagger tagger;

	/**
	 * This method removes the questions from the comments.
	 */
	@Override
	public List<String> removeQuestionBasedComments(List<String> commentsList) {
		for (Iterator<String> iterator = commentsList.iterator(); iterator.hasNext();) {
			String comment = iterator.next();
			if (comment.endsWith(Constants.QUESTION_MARK)) {
				iterator.remove();
				logger.info("========== Removed the question : " + comment + " ==========");
			}

			// Remove - signs in the comment
			if (comment.contains(Constants.DASH)) {
				comment.replaceAll(Constants.DASH, Constants.EMPTY_STRING);
			}
		}
		return commentsList;
	}

	/**
	 * This method reads stop words from stop word file.
	 */

	private List<String> readStopWordsFromStopWordFile(String stopWordsFilename) {
		List<String> stopWords = null;
		try {
			stopWords = fileLoader.readFileAsWords(stopWordsFilename);
		} catch (IOException e) {
			logger.error("Exception occurred while readind the file : " + e.getMessage());
			e.printStackTrace();
		}
		return stopWords;
	}

	private boolean searchForStopWord(String aAWordOfTheComment, List<String> stopWordsList) {
		if (stopWordsList.contains(aAWordOfTheComment)) {
			return true;
		}
		return false;

	}

	/**
	 * This method pre process the comments retrieved from fb.
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, List> doPreprocessingOnComments(List<String> fbPostCommentsList) {

		boolean flag = false;
		List<HasWord> removeWordList = null;
		List<List<TaggedWord>> preprocessedAndTaggedSentenceList = new ArrayList<>();
		Map<String, String> extractedAspectsMap = new HashMap<>();
		Integer mapKey = 1;
		Map<String, List> preprocessedMap = new HashMap<>();
		Set<String> aspectSet = new HashSet<>();

		// Remove question bases comments
		List<String> commentsListWithoutQuestions = removeQuestionBasedComments(fbPostCommentsList);

		// Write filtered comments to the file
		fileWriter.writeToFile(omAppConfiguration.getFbPostCommentsSaveFilePath(), commentsListWithoutQuestions);

		// Tokenize the input before tagging
		List<List<HasWord>> tokenizedSentences = null;
		try {
			tokenizedSentences = MaxentTagger.tokenizeText(
					new BufferedReader(new FileReader(omAppConfiguration.getFbPostCommentsSaveFilePath())));
			logger.info("========== Tokenized sentences : " + tokenizedSentences + " ==========");
		} catch (FileNotFoundException e) {
			logger.error("Error occurred while reading the file. " + e.getMessage());
		}

		// Load stop words list
		List<String> stopWords = readStopWordsFromStopWordFile(omAppConfiguration.getStopWordFilePath());

		// Load Maxent tagger
		tagger = new MaxentTagger(omAppConfiguration.getMaxentTaggerFilePath());

		for (List<HasWord> sentence : tokenizedSentences) {
			removeWordList = new ArrayList<HasWord>();
			for (HasWord word : sentence) {
				// Convert to lower case
				String wordString = word.toString().toLowerCase();
				// Check whether the sentence contains stop words
				flag = searchForStopWord(wordString, stopWords);
				if (flag) {
					removeWordList.add(word);
				}
				flag = false;
			}

			// Remove stopwords
			sentence.removeAll(removeWordList);
			logger.info("========== Removed words in the sentence :" + removeWordList + " ==========");
			removeWordList = null;

			// Stem the sentence
			stemmer.stem(sentence.toString());
			logger.info("========== Stemmed sentence :" + sentence.toString() + " ==========");

			// Tag the sentence
			List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
			logger.info("========== Tagged sentence : " + taggedSentence + " ==========");

			// Find aspects in the sentence
			// Noun or pro nouns are marked as aspects
			for (TaggedWord taggedWord : taggedSentence) {
				if (taggedWord.tag().startsWith(Constants.NOUN_OR_PRONOUN_PREFIX)) {
					if (extractedAspectsMap.values().contains(taggedWord.word())) {
						for (Map.Entry<String, String> e : extractedAspectsMap.entrySet()) {
							if (e.getValue().equals(taggedWord.word())) {
								String key = e.getKey();
								aspectSet.add(key);
								break;
							}
						}
					} else {
						extractedAspectsMap.put(mapKey.toString(), taggedWord.word());
						aspectSet.add(mapKey.toString());
						mapKey++;
					}
				}
			}

			preprocessedAndTaggedSentenceList.add(taggedSentence);
		}

		// Write extracted aspects to the file
		List<String> aspectList = new ArrayList<>();
		aspectList.addAll(aspectSet);
		fileWriter.writeToFile(omAppConfiguration.getAspectsFilePath(), aspectList);

		// Find frequent aspects
		List<String> frequentItemSetList = findFrequentAspects(extractedAspectsMap);
		logger.info("========== Frequent aspects : " + frequentItemSetList + " ==========");

		preprocessedMap.put(Constants.TAGGED_SENTENCE_LIST, preprocessedAndTaggedSentenceList);
		preprocessedMap.put(Constants.FREQUENT_ASPECT_LIST, frequentItemSetList);

		return preprocessedMap;
	}

	/**
	 * This method finds frequent aspects from the comments with the user given
	 * minimum support value.
	 * 
	 * @param extractedAspectsMap
	 * 
	 * @return List<String>
	 */
	public List<String> findFrequentAspects(Map<String, String> extractedAspectsMap) {

		List<String> frequentAspectList = null;

		// means a minsup of 4 transaction (we used a relative support)
		// double minsup = 0.4;
		double minsup = Double.valueOf(omAppConfiguration.getDefaultMinimumSupport());

		// Applying the Apriori algorithm
		Itemsets itemsets = null;
		try {
			itemsets = apriori.runAlgorithm(minsup, omAppConfiguration.getAspectsFilePath(), null);
		} catch (Exception e) {
			logger.info("Error occurred while running Apriori algorithm. " + e.getMessage());
		}

		List<Itemset> itemsetList = null;

		if (itemsets != null && itemsets.getItemsetsCount() > 0) {
			itemsetList = itemsets.getLevels().get(1);

			if (itemsetList != null) {
				frequentAspectList = new ArrayList<>();
				for (Itemset itemset : itemsetList) {
					frequentAspectList.add(extractedAspectsMap.get(itemset.get(0).toString()));
				}
				fileWriter.writeToFile(omAppConfiguration.getAspectsSaveFilePath(), frequentAspectList);

			}
		}
		return frequentAspectList;
	}
}
