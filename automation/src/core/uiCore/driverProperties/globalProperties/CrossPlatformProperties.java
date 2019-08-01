package core.uiCore.driverProperties.globalProperties;

import core.support.configReader.Config;

/**
 * @author jenkins
 *
 */
public class CrossPlatformProperties {

	private static String PARALLEL_TEST_COUNT = "global.parallelTestCount";
	private static String PARALLEL_TEST_TYPE = "global.parallelTestType";

	private static String RETRY_COUNT = "global.retryCount";
	private static String GLOBAL_TIMEOUT_SECONDS = "global.timeoutSeconds";
	private static String AUDIO_COMMENTARY = "report.audioCommentary";
	private static String IS_SINGLE_SIGNIN = "global.isSingleSignIn";
	private static String LANGUAGE = "language";
	private static String AUDIO_COMMENTARY_TYPE = "report.audioCommentaryType";
	private static String PATH = "environment.path";
	private static String ENABLE_BATCH_LOGGING = "report.enableBatchLogging";
	public static String LOCALIZATION_FILE = "localize.file";

	public static int getRetryCount() {
		return Config.getIntValue(RETRY_COUNT) + 1;
	}

	public static int getParallelTests() {
		return Config.getIntValue(PARALLEL_TEST_COUNT);
	}
	
	public static String getParallelTestType() {
		return Config.getValue(PARALLEL_TEST_TYPE);
	}

	public static int getGlobalTimeout() {
		return Config.getIntValue(GLOBAL_TIMEOUT_SECONDS);
	}

	public static boolean getAudioCommentary() {
		return Config.getBooleanValue(AUDIO_COMMENTARY);
	}

	public static String getAudioCommentaryType() {
		return Config.getValue(AUDIO_COMMENTARY_TYPE);
	}

	public static String getPath() {
		return Config.getValue(PATH);
	}

	public static Boolean getEnableBatchLogging() {
		return Config.getBooleanValue(ENABLE_BATCH_LOGGING, true);
	}

	public static String getLocalizationFile() {
		return Config.getValue(LOCALIZATION_FILE);
	}

	/**
	 * localization language
	 * 
	 * @return
	 */
	public static String getLanguage() {
		return Config.getValue(LANGUAGE);
	}

	/**
	 * is single signin flag set with single signin, the app is not closed after
	 * each test
	 * 
	 * @return
	 */
	public static Boolean isSingleSignIn() {
		return Config.getBooleanValue(IS_SINGLE_SIGNIN);

	}

	/**
	 * returns a simplified driver type: web, android, ios, rest
	 * 
	 * @param driverType
	 * @return
	 */
	public String getSimpleDriverType(String driverType) {
		if (driverType.toLowerCase().contains("web")) {
			return "web";
		}
		return driverType.toLowerCase();
	}
}