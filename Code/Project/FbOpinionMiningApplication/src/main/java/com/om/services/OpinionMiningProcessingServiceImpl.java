package com.om.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.om.OMAppConfiguration;
import com.om.config.Constants;
import com.om.domain.OpinionMiningProcessingResult;
import com.om.util.SentiWordNetScoreCalculator;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * OpinionMiningProcessingServiceImpl class implements the methods used to
 * process the Facebook post comments.
 * 
 * @author Maneendra
 *
 */
@Service
public class OpinionMiningProcessingServiceImpl implements OpinionMiningProcessingService {

	@Autowired
	private OMAppConfiguration omAppConfiguration;

	private SentiWordNetScoreCalculator sentiwordnet = null;

	private DependencyParser parser = null;

	private Logger logger = LoggerFactory.getLogger(OpinionMiningProcessingServiceImpl.class);

	/**
	 * This method loads required parsers while starting up the system.
	 */
	@PostConstruct
	public void loadDependencyPaserAndSentiwordnet() {
		// Load dependency parser
		String modelPath = DependencyParser.DEFAULT_MODEL;
		parser = DependencyParser.loadFromModelFile(modelPath);

		// Load sentiwordnet
		String pathToSWN = omAppConfiguration.getSentiwordnetFilePath();
		try {
			sentiwordnet = new SentiWordNetScoreCalculator(pathToSWN);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method process the comments.
	 */
	public OpinionMiningProcessingResult doProcessingOnTaggedCommentsSentences(
			List<List<TaggedWord>> preprocessedAndTaggedSentenceList, List<String> frequentAspectList) {

		// Filter only subjective sentences
		Map<List<TaggedWord>, List<String>> subjectiveSentencesMapWithAspects = removeObjectiveSentencesFromComments(
				preprocessedAndTaggedSentenceList, frequentAspectList);

		// Find relationships of the sentence and calculate scores
		OpinionMiningProcessingResult result = findRelationsAndCalculateScores(subjectiveSentencesMapWithAspects);

		return result;
	}

	/**
	 * This method find relationships of the sentence and calculate scores.
	 * 
	 * @param subjectiveSentencesMapWithAspects
	 * @return
	 */
	private OpinionMiningProcessingResult findRelationsAndCalculateScores(
			Map<List<TaggedWord>, List<String>> subjectiveSentencesMapWithAspects) {

		Map<String, Double> sentenceScoreGlobalMap = new HashMap<>();

		// Global aspect map
		Map<String, List<Double>> globalAspectMap = new HashMap<>();

		double maximumSentenceScore = 0;
		double minimumSentenceScore = 0;

		for (List<TaggedWord> taggedSentence : subjectiveSentencesMapWithAspects.keySet()) {

			logger.info("========== Processing sentence : " + taggedSentence.toString() + " ==========");

			GrammaticalStructure gs = parser.predict(taggedSentence);

			logger.info("========== Gramatical structure of the sentence : " + gs + " ==========");

			List<String> aspects = subjectiveSentencesMapWithAspects.get(taggedSentence);

			logger.info("========== Aspects in the sentence : " + aspects + " ==========");

			List<String> adjectives;
			double aggregateAdjectiveScore = 0;
			List<String> verbs;
			double aggregateVerbScore = 0;
			double sentenceWiseAspectScore = 0;
			double sentenceScore = 0;

			for (String aspect : aspects) {

				logger.info("========== Processing aspect : " + aspect + " ==========");

				// Find adjectives
				adjectives = findAdjectives(aspect, gs.typedDependencies());
				logger.info("========== Found adjectives : " + adjectives + " ==========");
				if (adjectives != null && adjectives.size() > 0) {
					// Calculate aggregate adjective score
					aggregateAdjectiveScore = calculateAggregateAdjectiveScoreOrVerbScore(adjectives,
							gs.typedDependencies());
				}

				// Find verbs
				verbs = findVerbs(aspect, gs.typedDependencies());
				logger.info("========== Found verbs : " + verbs + " ==========");
				if (verbs != null && verbs.size() > 0) {
					// Calculate aggregate verb score
					aggregateVerbScore = calculateAggregateAdjectiveScoreOrVerbScore(adjectives,
							gs.typedDependencies());
				}

				// Calculate sentence wise score of aspect
				sentenceWiseAspectScore = calculatesentenceWiseAspectScore(aggregateAdjectiveScore, aggregateVerbScore);

				List<Double> aspectScoreList = null;
				if (globalAspectMap.containsKey(aspect)) {
					logger.info("========== Aspect is in the map : " + aspect + " ==========");
					aspectScoreList = globalAspectMap.get(aspect);
					aspectScoreList.add(sentenceWiseAspectScore);
				} else {
					logger.info("========== Aspect is not in the map : creating a new key " + aspect + " ==========");
					aspectScoreList = new ArrayList<>();
					aspectScoreList.add(sentenceWiseAspectScore);
				}

				logger.info("========== Aspect and Scores " + aspect + " : " + aspectScoreList + " ==========");
				globalAspectMap.put(aspect, aspectScoreList);

				logger.info("========== Sentence wise aspect score : " + sentenceWiseAspectScore + " ==========");
				sentenceScore = sentenceScore + sentenceWiseAspectScore;
			}
			sentenceScoreGlobalMap.put(taggedSentence.toString(), sentenceScore);

			if (sentenceScore > maximumSentenceScore) {
				maximumSentenceScore = sentenceScore;
			}

			if (sentenceScore < minimumSentenceScore) {
				minimumSentenceScore = sentenceScore;
			}
		}

		OpinionMiningProcessingResult result = new OpinionMiningProcessingResult();
		logger.info("========== Final aspect map " + globalAspectMap + " ==========");
		result.setAspectResultsMap(globalAspectMap);
		result.setSentenceResultsMap(sentenceScoreGlobalMap);
		result.setMinSentenceScore(minimumSentenceScore);
		result.setMaxSentenceScore(maximumSentenceScore);

		return result;
	}

	/**
	 * This method calculate the aggregate adjective score / aggregate verb
	 * score
	 * 
	 * @param adjectiveOrVerbList
	 * @param typedDependencies
	 * @return double score
	 */
	private double calculateAggregateAdjectiveScoreOrVerbScore(List<String> adjectiveOrVerbList,
			Collection<TypedDependency> typedDependencies) {

		double aggregate_adjective_or_verb_score = 0;
		double aggregate_adverb_adjective_or_verb_score = 0;
		double adjective_or_verb_score = 0;
		String adverb = null;
		double adverbScore = 0;
		List<String> adverbList = new ArrayList<>();
		List<Double> adverbScoreList = new ArrayList<>();

		if (adjectiveOrVerbList != null && adjectiveOrVerbList.size() > 0) {
			for (String adjectiveOrVerb : adjectiveOrVerbList) {
				// Calculate adjective / verb score using sentiwordnet
				adjective_or_verb_score = calculateScore(adjectiveOrVerb);
				logger.info("========== Adjective / Verb score : " + adjective_or_verb_score + " ==========");

				// Negation handling
				if (checkNegation(adjectiveOrVerb, typedDependencies)) {
					adjective_or_verb_score = -adjective_or_verb_score;
				}

				// Find adverbs
				adverb = findAdverbAdjectivesOrAdverbVerbs(adjectiveOrVerb, typedDependencies);

				if (adverb != null && adverb.length() > 0) {
					// Calculate adverb score using sentiwordnet
					adverbScore = calculateScore(adverb);
					logger.info("========== Adverb score : " + adverbScore + " ==========");

					adverbScoreList.add(adverbScore);

					// Negation handling
					if (checkNegation(adverb, typedDependencies)) {
						adverbScore = -adverbScore;
					}

					// Calculate aggregate score
					aggregate_adverb_adjective_or_verb_score = aggregate_adverb_adjective_or_verb_score
							+ calculateAggregateScoreForAdverbAndOpinionWord(adverbScore, adjective_or_verb_score);
				} else {
					adjective_or_verb_score = adjective_or_verb_score + adjective_or_verb_score;
				}
			}

			if (adverbList != null && adverbList.size() > 0) {
				// Calculate adverb score
				aggregate_adverb_adjective_or_verb_score = calculateAverageAdverbScore(adverbScoreList);
				logger.info("========== Avergae adverb score : " + aggregate_adverb_adjective_or_verb_score
						+ " ==========");

				adjective_or_verb_score = adjective_or_verb_score / adjectiveOrVerbList.size();

				aggregate_adjective_or_verb_score = aggregate_adverb_adjective_or_verb_score
						+ Constants.WEIGHTAGE * adjective_or_verb_score;
			} else {
				aggregate_adjective_or_verb_score = adjective_or_verb_score / adjectiveOrVerbList.size();
			}
		}
		logger.info("========== Aggregate adjective/verb score : " + aggregate_adjective_or_verb_score + " ==========");
		return aggregate_adjective_or_verb_score;

	}

	/**
	 * This method calculates average adverb score.
	 * 
	 * @param adverbScoreList
	 * @return double score
	 */
	private double calculateAverageAdverbScore(List<Double> adverbScoreList) {
		logger.info("========== Adverb scores : " + adverbScoreList + "==========");
		double totalAdverbScore = 0;
		for (Double adverbScore : adverbScoreList) {
			totalAdverbScore = totalAdverbScore + adverbScore;
		}
		return totalAdverbScore / adverbScoreList.size();
	}

	/**
	 * This method calculates aggregate score for adverb and opinion word
	 * adjective / verb.
	 * 
	 * @param adverbScore
	 * @param adjOrVerbScore
	 * @return double score
	 */
	private double calculateAggregateScoreForAdverbAndOpinionWord(double adverbScore, double adjOrVerbScore) {

		logger.info("========== Calculate aggregate score : adverb score= " + adverbScore + " adjective/verb score="
				+ adjOrVerbScore + " ==========");

		double aggregateScore = 0;

		if (adjOrVerbScore == 0) {
			return aggregateScore;
		}

		if (adverbScore > 0) {
			if (adjOrVerbScore > 0) {
				aggregateScore = adjOrVerbScore + Double.valueOf(omAppConfiguration.getWeightage()) * adverbScore;
				aggregateScore = Math.min(1, aggregateScore);
				return aggregateScore;
			}

			if (adjOrVerbScore < 0) {
				aggregateScore = adjOrVerbScore - Double.valueOf(omAppConfiguration.getWeightage()) * adverbScore;
				aggregateScore = Math.min(1, aggregateScore);
				return aggregateScore;
			}
		}

		if (adverbScore < 0) {
			if (adjOrVerbScore > 0) {
				aggregateScore = adjOrVerbScore + Double.valueOf(omAppConfiguration.getWeightage()) * adverbScore;
				aggregateScore = Math.max(-1, aggregateScore);
				return aggregateScore;
			}

			if (adjOrVerbScore < 0) {
				aggregateScore = adjOrVerbScore - Double.valueOf(omAppConfiguration.getWeightage()) * adverbScore;
				aggregateScore = Math.max(-1, aggregateScore);
				return aggregateScore;
			}
		}
		logger.info("========== Aggregate score=" + aggregateScore + " ==========");
		return aggregateScore;
	}

	/**
	 * This method calculate score for the opinion word using sentiwordnet.
	 * 
	 * @param opinionWord
	 * @return double score
	 */
	private double calculateScore(String opinionWord) {
		return sentiwordnet.extract(opinionWord);
	}

	/**
	 * This method checks the negations of the words.
	 * 
	 * @param word
	 * @param typedDependencies
	 * @return boolean
	 */
	private boolean checkNegation(String word, Collection<TypedDependency> typedDependencies) {
		logger.info("========== Checking negation for word : " + word + " in sentence : " + typedDependencies
				+ " ==========");
		boolean negationFlag = false;
		for (TypedDependency ss : typedDependencies) {
			if (ss.reln().toString().equals("neg") && ss.gov().word().equals(word)) {
				negationFlag = true;
				break;
			} else if (ss.reln().toString().equals("pobj") && ss.gov().word().equals("not")
					&& ss.dep().word().equals(word)) {
				negationFlag = true;
				break;
			}
		}
		logger.info("========== Negation found : " + negationFlag + " ==========");
		return negationFlag;
	}

	/**
	 * This method finds the adjectives related to the aspect in the sentence.
	 * 
	 * @param aspect
	 * @param typedDependencies
	 * @return
	 */
	private List<String> findVerbs(String aspect, Collection<TypedDependency> typedDependencies) {

		logger.info(
				"========== Find verbs for aspect : " + aspect + " in sentence : " + typedDependencies + " ==========");

		List<String> verbsList = new ArrayList<>();

		for (TypedDependency typedDependency : typedDependencies) {
			if (typedDependency.reln().toString().equals("root") && typedDependency.gov().equals("ROOT")) {
				for (TypedDependency s : typedDependencies) {
					if ((s.reln().toString().equals("nsubj") && typedDependency.dep().word().equals(s.gov().word())
							&& s.dep().word().equals(aspect))
							|| (s.reln().toString().equals("dobj")
									&& typedDependency.dep().word().equals(s.gov().word())
									&& s.dep().word().equals(aspect))) {
						verbsList.add((s.gov().word() + Constants.HASH + Constants.VERB));
					}
				}
			}
		}
		return verbsList;
	}

	/**
	 * This method finds adverb of adjective or verb.
	 * 
	 * @param adjectiveOrVerb
	 * @param typedDependencies
	 * @return String adverb
	 */
	private String findAdverbAdjectivesOrAdverbVerbs(String adjectiveOrVerb,
			Collection<TypedDependency> typedDependencies) {

		logger.info("========== Find adverbs for adjective/verb : " + adjectiveOrVerb + " in sentence : "
				+ typedDependencies + " ==========");

		for (TypedDependency ss : typedDependencies) {
			if (ss.reln().toString().equals("advmod") && ss.gov().word().equals(adjectiveOrVerb)
					|| ss.reln().toString().equals("advcl") && ss.gov().word().equals(adjectiveOrVerb)
					|| ss.reln().toString().equals("amod") && ss.gov().word().equals(adjectiveOrVerb)) {
				logger.info("========== Found adverb for adjective / verb " + adjectiveOrVerb + " : " + ss.dep().word()
						+ " ==========");
				return (ss.dep().word() + Constants.HASH + Constants.ADVERB);
			}
		}
		return "";
	}

	/**
	 * This method finds the subjective sentences which contains atleast an
	 * aspect.
	 */
	public Map<List<TaggedWord>, List<String>> removeObjectiveSentencesFromComments(
			List<List<TaggedWord>> preprocessedAndTaggedSentenceList, List<String> frequentAspectList) {

		boolean aspectFound = false;
		Map<List<TaggedWord>, List<String>> subjectiveSentenceMap = new HashMap<>();

		for (List<TaggedWord> taggedSentence : preprocessedAndTaggedSentenceList) {
			List<String> aspectList = new ArrayList<>();
			for (TaggedWord taggedWord : taggedSentence) {
				aspectFound = searchForAspect(taggedWord.word(), frequentAspectList);
				if (aspectFound) {
					aspectList.add(taggedWord.word());
					// subjectiveSentenceList.add(taggedSentence);
				}
				subjectiveSentenceMap.put(taggedSentence, aspectList);
			}
			logger.info("========== Found subjective sentence : " + taggedSentence + " and aspects : " + aspectList);
		}
		return subjectiveSentenceMap;

	}

	/**
	 * This method search whether the word is in frequent item set list.
	 * 
	 * @param aWordOfTheComment
	 * @param frequentAspectList
	 * @return boolean
	 */
	private boolean searchForAspect(String aWordOfTheComment, List<String> frequentAspectList) {
		if (frequentAspectList != null && frequentAspectList.size() > 0) {
			if (frequentAspectList.contains(aWordOfTheComment)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method finds the adjectives related to the aspect in the sentence.
	 * 
	 * @param aspect
	 * @param typedDependencyCollection
	 * @return List<String>
	 */
	private List<String> findAdjectives(String aspect, Collection<TypedDependency> typedDependencyCollection) {

		logger.info("========== Find adjective for aspect : " + aspect + " in sentence : " + typedDependencyCollection
				+ " ==========");

		List<String> adjectivesList = new ArrayList<>();

		for (TypedDependency typedDependency : typedDependencyCollection) {

			if ((typedDependency.reln().toString().equals("amod") && typedDependency.gov().word().equals(aspect))
					|| (typedDependency.reln().toString().equals("advmod")
							&& typedDependency.gov().word().equals(aspect))) {
				adjectivesList.add((typedDependency.dep().word() + Constants.HASH + Constants.ADJECTIVE));
			}

			if (typedDependency.reln().toString().equals("cc") && typedDependency.dep().word().equals("but")) {
				for (TypedDependency ss : typedDependencyCollection) {
					if (ss.reln().toString().equals("nsubj")
							&& (ss.dep().word().equals("it") || ss.dep().word().equals(aspect))) {
						adjectivesList.add((ss.gov().word() + Constants.HASH + Constants.ADJECTIVE));
					}
				}
			}

			if (typedDependency.reln().toString().equals("root") && typedDependency.gov().equals("ROOT")) {
				for (TypedDependency ss : typedDependencyCollection) {
					if (ss.reln().toString().equals("nsubj") && ss.gov().word().equals(typedDependency.dep().word())) {
						for (TypedDependency sss : typedDependencyCollection) {
							if (sss.reln().toString().equals("cop") && sss.gov().word().equals(ss.gov().word())) {
								adjectivesList.add((ss.gov().word() + Constants.HASH + Constants.ADJECTIVE));
							}
						}
					}
				}
			}

			if (typedDependency.reln().toString().equals("nsubj") && typedDependency.dep().word().equals(aspect)) {
				for (TypedDependency ss : typedDependencyCollection) {
					if (ss.reln().toString().equals("acomp") && ss.gov().word().equals(typedDependency.gov().word())) {
						adjectivesList.add((ss.dep().word() + Constants.HASH + Constants.ADJECTIVE));
					}
				}
			}
		}
		return adjectivesList;
	}

	/**
	 * This method calculate aspect score for the sentence.
	 * 
	 * @param aggregateAdjectiveScore
	 * @param aggregateVerbScore
	 * @return double score
	 */
	public double calculatesentenceWiseAspectScore(double aggregateAdjectiveScore, double aggregateVerbScore) {
		logger.info("========== Calculate sentence wise aspect score : aggregate adjective score="
				+ aggregateAdjectiveScore + " aggregate verb score=" + aggregateVerbScore + "==========");
		double sentenceWiseAspectScore = aggregateAdjectiveScore + Constants.WEIGHTAGE * aggregateVerbScore;
		logger.info("========== Sentence wise aspect score= " + sentenceWiseAspectScore + "==========");
		return sentenceWiseAspectScore;
	}

}
