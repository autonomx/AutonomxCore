package core.uiCore.driverProperties.globalProperties;

import core.support.configReader.Config;

public class CrossPlatformProperties {

	public static String PARALLEL_TEST_COUNT = "parallel_test_count";
	public static String RETRY_COUNT = "retry_count";
	public static String GLOBAL_TIMEOUT_SECONDS = "global_timeout_seconds";
	public static String TEST_DATA_PATH = "testDataPath";
	public static String AUDIO_COMMENTARY = "audio_commentary";
	public static String IS_SINGLE_SIGNIN = "isSingleSignIn";
	public static String LANGUAGE = "language";
	public static String AUDIO_COMMENTARY_TYPE = "audio_commentary_type";
	public static String PATH = "path";
	public static String ENABLE_BATCH_LOGGING = "enable_batch_logging";
	public static String LOCALIZATION_FILE = "localization_file";

	public static int getRetryCount() {
		return Config.getIntValue(RETRY_COUNT);
	}

	public static int getParallelTests() {
		return Config.getIntValue(PARALLEL_TEST_COUNT);
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
		return Config.getBooleanValue(ENABLE_BATCH_LOGGING);
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