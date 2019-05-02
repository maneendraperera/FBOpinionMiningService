package com.om;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;

/**
 * This class loads all the configurations from external configuration file.
 * 
 * @author Maneendra
 *
 */
@Configuration
@PropertySource(value = { "file:${APP_CONFIG}/appconfig/config.properties" })
public class InitConfig {

	@Autowired
	Environment env;

	public static final String FB_COMMENT_RETREIVAL_URL = "fb.comments.retreive.url";
	public static final String FB_POST_COMMENTS_SAVE_FILE_PATH = "fb.comments.save.file.path";
	public static final String FB_APP_ID = "fb.app.id";
	public static final String FB_APP_SECRET = "fb.app.secret";
	public static final String FB_APP_ACCESS_TOKEN = "fb.app.acess.token";

	public static final String STOP_WORD_FILE_PATH = "stop.word.file.path";
	public static final String MAXENT_TAGGER_FILE_PATH = "maxent.tagger.file.path";
	public static final String SENTIWORDNET_FILE_PATH = "sentiwordnet.file.path";

	public static final String ASPECTS_FILE = "aspect.file.path";
	public static final String ASPECTS_SAVE_FILE_PATH = "aspect.save.file.path";
	public static final String DEFAULT_MINIMUM_SUPPORT = "default.minium.support";
	public static final String WEIGHTAGE = "default.weightage";

	@Bean
	public OMAppConfiguration configuration() {
		OMAppConfiguration configuration = new OMAppConfiguration();
		configuration.setStopWordFilePath(env.getProperty(STOP_WORD_FILE_PATH));
		configuration.setMaxentTaggerFilePath(env.getProperty(MAXENT_TAGGER_FILE_PATH));
		configuration.setFbPostCommentsSaveFilePath(env.getProperty(FB_POST_COMMENTS_SAVE_FILE_PATH));
		configuration.setAspectsFilePath(env.getProperty(ASPECTS_FILE));
		configuration.setAspectsSaveFilePath(env.getProperty(ASPECTS_SAVE_FILE_PATH));
		configuration.setSentiwordnetFilePath(env.getProperty(SENTIWORDNET_FILE_PATH));
		configuration.setFbCommentRetreivalUrl(env.getProperty(FB_COMMENT_RETREIVAL_URL));
		configuration.setDefaultMinimumSupport(env.getProperty(DEFAULT_MINIMUM_SUPPORT));
		configuration.setWeightage(env.getProperty(WEIGHTAGE));
		configuration.setFbAppId(env.getProperty(FB_APP_ID));
		configuration.setFbAppSecret(env.getProperty(FB_APP_SECRET));
		configuration.setFbAppAcessToken(env.getProperty(FB_APP_ACCESS_TOKEN));
		return configuration;
	}

	@Bean
	public AlgoApriori aprioriAlgorithm() {
		return new AlgoApriori();
	}

}
