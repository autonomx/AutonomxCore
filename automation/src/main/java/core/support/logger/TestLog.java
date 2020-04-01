package core.support.logger;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Priority;
import org.apache.log4j.xml.DOMConfigurator;
//import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.And;
import com.aventstack.extentreports.gherkin.model.Background;
import com.aventstack.extentreports.gherkin.model.But;
import com.aventstack.extentreports.gherkin.model.Given;
import com.aventstack.extentreports.gherkin.model.Then;
import com.aventstack.extentreports.gherkin.model.When;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.objects.TestObject;
import core.support.objects.TestObject.testState;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;

@SuppressWarnings("deprecation")
public class TestLog {

	public static final String ENABLE_EXTENT_SUBSTEPS = "report.enableDetailedReport";
	public static final String ENABLE_DEBUG = "console.debug.enable";
	public static final String IS_LOG_LIMIT = "log.limit.enabled";
	public static final String LOG_MAX_LIMIT = "log.max.limit.char";

	public static String WATSON = "WATSON";
	public static String MARY = "MARY";

	enum gherkins {
		Given, When, Then, And, But, Background
	}

	static marytts.util.data.audio.AudioPlayer player;
	public static final String LOG4JPATH = Config.RESOURCE_PATH + "/log4j.xml";

	/**
	 * logs to console
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void ConsoleLog(String value, Object... args) {
		logConsoleMessage(Priority.INFO, formatMessage(value, args));
	}

	/**
	 * logs warning to console
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void ConsoleLogWarn(String value, Object... args) {
		logConsoleMessage(Priority.WARN, formatMessage(value, args));

	}

	/**
	 * debug logs to console
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void ConsoleLogDebug(String value, Object... args) {
		boolean isDebug = Config.getBooleanValue(ENABLE_DEBUG);
		if (isDebug)
			logConsoleMessage(Priority.WARN, formatMessage(value, args));

	}

	/**
	 * error logs to console
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void ConsoleLogError(String value, Object... args) {
		logConsoleMessage(Priority.ERROR, formatMessage(value, args));

	}

	/**
	 * logs to extent report as background node
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public synchronized static void Background(String value, Object... args) {
		setTestStep(gherkins.Background, value, args);
	}

	/**
	 * logs to extent report as but node
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public synchronized static void But(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "But " + formatMessage(value, args));
		setTestStep(gherkins.But, value, args);
	}

	/**
	 * logs to extent report as given node
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public synchronized static void Given(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "Given " + formatMessage(value, args));
		setTestStep(gherkins.Given, value, args);
	}

	/**
	 * logs to extent report as when node
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public synchronized static void When(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "When " + formatMessage(value, args));

		setTestStep(gherkins.When, value, args);
		playAudio(gherkins.When.name() + " " + formatMessage(value, args));
	}

	/**
	 * logs to extent report as and node
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public synchronized static void And(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "And " + formatMessage(value, args));

		setTestStep(gherkins.And, value, args);
		playAudio(gherkins.And.name() + " " + formatMessage(value, args));
	}

	/**
	 * logs to extent report as then node
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public synchronized static void Then(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "Then " + formatMessage(value, args));

		setTestStep(gherkins.Then, value, args);
		playAudio(gherkins.Then.name() + " " + formatMessage(value, args));
	}

	/**
	 * sets test step based on the gherkins language steps: given, when, then, and,
	 * but, background
	 * 
	 * @param gherkinState given, when, then, and, but, background
	 * @param value        value to log
	 * @param args         additional arguments for logging to be formatted
	 */
	public static void setTestStep(gherkins gherkinState, String value, Object... args) {
		ExtentTest testStep = null;

		testState state = TestObject.getTestState(TestObject.getTestInfo().testId);
		if (!state.equals(testState.testMethod))
			return;

		switch (gherkinState) {
		case Given:
			testStep = getTestScenario().createNode(Given.class, "Given " + formatMessage(value, args)).pass("");
			break;
		case When:
			testStep = getTestScenario().createNode(When.class, "When " + formatMessage(value, args)).pass("");
			break;
		case Then:
			testStep = getTestScenario().createNode(Then.class, "Then " + formatMessage(value, args)).pass("");
			break;
		case And:
			testStep = getTestScenario().createNode(And.class, "And " + formatMessage(value, args)).pass("");
			break;
		case But:
			testStep = getTestScenario().createNode(But.class, "But " + formatMessage(value, args)).pass("");
			break;
		case Background:
			testStep = getTestScenario().createNode(Background.class, formatMessage(value, args)).pass("");
			break;
		default:
			Helper.assertFalse("incorrect state " + gherkinState.name());
		}
		// if test step is not set, do not log. Test will be set in test method state
		// only.
		if (TestObject.getTestInfo().testSteps == null)
			return;

		TestObject.getTestInfo().testSteps.add(testStep);
		AbstractDriver.getStep().set(testStep);
	}

	/**
	 * sets substep as pass
	 * 
	 * @param subStep the substep node value
	 */
	public static void setPassSubTestStep(String subStep) {
		if (getTestStep() == null)
			return;
		testState state = TestObject.getTestState(TestObject.getTestInfo().testId);
		if (!state.equals(testState.testMethod))
			return;

		TestObject.getTestInfo().testSubSteps.add(subStep);
		Markup m = MarkupHelper.createCodeBlock(subStep);
		getTestStep().pass(m);
	}

	/**
	 * adds video and video link to the sub step
	 * 
	 * @param path            relative path to the video file
	 * @param isVideoAttached if the video is going to be attached to the extent
	 *                        report path : relative path to the video file
	 */
	public static void attachVideoLog(String path, boolean isVideoAttached) {
		testState state = TestObject.getTestState(TestObject.getTestInfo().testId);
		if (!state.equals(testState.testMethod))
			return;

		// for hmtl report, use relative path (we need to be able to email the report)
		// for klov we need absolute path
		if (!Config.getValue(ExtentManager.REPORT_TYPE).equals(ExtentManager.HTML_REPORT_TYPE))
			path = ExtentManager.getReportRootFullPath() + path;

		String videoLog = "<video width=\"320\" height=\"240\" controls>\r\n" + "  <source src=" + path
				+ " type=\"video/mp4\">\r\n" + "  Your browser does not support the video tag.\r\n" + "</video>";

		if (isVideoAttached)
			getTestStep().pass(videoLog);

		getTestStep().pass("<a href='" + path + "'>screen recording Link</a>");
		TestObject.getTestInfo().testSubSteps.add("screen recording relative path: " + path);

	}

	/**
	 * log pass is used for test steps for extend report, enable if properties
	 * option enableDetailedReport is true
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void logPass(String value, Object... args) {
		logConsoleMessage(Priority.INFO, formatMessage(value, args));
		if (Config.getValue(ENABLE_EXTENT_SUBSTEPS).equals("true")) {
			// this will only throw exception with before suite
			setPassSubTestStep(formatMessage(value, args));
		}
	}

	/**
	 * sets fail log for extent report and console
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void logFail(String value, Object... args) {
		logConsoleMessage(Priority.ERROR, formatMessage(value, args));

		// AbstractDriver.getLog().get().error(formatMessage(value, args));
	}

	/**
	 * sets warning log for extent report and console
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 */
	public static void logWarning(String value, Object... args) {
		logConsoleMessage(Priority.WARN, formatMessage(value, args));
	}

	/**
	 * @return gets the test scenario node
	 */
	public static ExtentTest getTestScenario() {
		return TestObject.getTestInfo().testScenerio;

	}

	/**
	 * 
	 * @param value value to log
	 * @param args  additional arguments for logging to be formatted
	 * @return formatted message based on value and arguments
	 */
	public static String formatMessage(String value, Object... args) {
		 value = setMaxLength(value);

		if (args == null || args.length == 0) {
			return value;
		} else {
			try {
				return new MessageFormat(value).format(args);
			} catch (Exception e) {
				return value;
			}
		}
	}

	/**
	 * limites the max value size
	 * 
	 * @param value value to log
	 * @return truncated message to maximum length
	 */
	public static String setMaxLength(String value) {
		boolean isLogLimit = Config.getBooleanValue(IS_LOG_LIMIT);
		int logMaxLimit = Config.getIntValue(LOG_MAX_LIMIT);
		if(logMaxLimit == -1) logMaxLimit = value.length();
		if(isLogLimit)
			return setMaxLength(value, logMaxLimit);
		else
			return value;
	}

	/**
	 * 
	 * @param value  value to log
	 * @param length length to truncate message to
	 * @return truncated message to maximum length
	 */
	public static String setMaxLength(String value, int length) {
		// limit the max size of string
		int maxLength = (value.length() < length) ? value.length() : length;
		if (maxLength == length)
			return value.substring(0, maxLength) + "...";
		return value.substring(0, maxLength);
	}

	/**
	 * removes handler for java.util.logging removes logs from third party jars such
	 * as webdriver
	 */
	// TODO: temporarily disabled the handler as it conflicts with another log4j
	public static void removeLogUtilHandler() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	/**
	 * sets up log4j loggin
	 */
	public static void setupLog4j() {
		DOMConfigurator.configure(TestLog.LOG4JPATH);
	}

	/**
	 * plays audio file based on text value
	 * 
	 * @param value text value to play back
	 */
	public static void playAudio(String value) {
		Boolean isAudioCommentaryEnabled = CrossPlatformProperties.getAudioCommentary();
		String type = CrossPlatformProperties.getAudioCommentaryType();
		if (isAudioCommentaryEnabled) {
			if (type.equals(WATSON)) {
				playWatsonAudio(value);
			} else if (type.equals(MARY)) {
				playMaryAudio(value);

			}
		}
	}

	/**
	 * plays audio using marytts library
	 * 
	 * @param value text value to play back
	 */
	public static void playMaryAudio(String value) {

		try {
			MaryInterface marytts = new LocalMaryInterface();
			Set<String> voices = marytts.getAvailableVoices();
			String[] voiceList = voices.toArray(new String[voices.size()]);

			marytts.setVoice(voiceList[0]);

			// '!' results in playback being a bit faster, more natural
			AudioInputStream audio = marytts.generateAudio(value + "!");
			player = new marytts.util.data.audio.AudioPlayer(audio);
			player.start();
			player.join();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * watson audio
	 * 
	 * @param value text value to play back
	 */
	public synchronized static void playWatsonAudio(String value) {
		TextToSpeech textToSpeech = new TextToSpeech();
		// set username and password from IBM account
		textToSpeech.setUsernameAndPassword("", "");
		textToSpeech.setEndPoint("https://stream.watsonplatform.net/text-to-speech/api");

		try {
			SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder().text(value).accept("audio/wav")
					.voice("en-US_AllisonVoice").build();

			InputStream inputStream = textToSpeech.synthesize(synthesizeOptions).execute();
			InputStream in = WaveUtils.reWriteWaveHeader(inputStream);

			AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
			player = new marytts.util.data.audio.AudioPlayer(audio);
			player.start();
			player.join();

			in.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * logs to console if batch logging is enabled, Then logs are stored And printed
	 * to console once the test is complete
	 * 
	 * @param priority priority of the message. eg. info, error, warn
	 * @param value    string value to log
	 */
	private static void logConsoleMessage(Priority priority, String value) {

		value = Helper.date.getTimestampSeconds() + " : " + getTestLogPrefix() + value;

		// if batch logging is disabled, log to console
		Boolean enableBatchLogging = CrossPlatformProperties.getEnableBatchLogging();
		if (!enableBatchLogging) {
			TestObject.getTestInfo().log.log(priority, value);
		}
		
		// keep track of the logs
		LogObject log = new LogObject(value, priority);
		TestObject.getTestInfo().testLog.add(log);
	}


	/**
	 * sets the logging prefix
	 * 
	 * @return the logging prefix
	 */
	private static String getTestLogPrefix() {
		return TestObject.getTestInfo().className + "-" + TestObject.getTestInfo().testName + " - ";
	}

	private synchronized static ExtentTest getTestStep() {
		return AbstractDriver.getStep().get();
	}

	/**
	 * prints out the entire test log for csv file at once Occurs When number of csv
	 * files are greater than 1
	 */
	public synchronized static void printBatchLogsToConsole() {
		Boolean enableBatchLogging = CrossPlatformProperties.getEnableBatchLogging();
		if (!enableBatchLogging)
			return;

		if (TestObject.getTestInfo().isTestComplete) {
			// print before class logs
			printBatchClassToConsole(TestObject.BEFORE_CLASS_PREFIX);

			// print test logs
			printLogs(TestObject.getTestInfo().testLog);
		}
	}

	/**
	 * prints class level logs to console prints only once
	 * 
	 * @param classType
	 */
	public static void printBatchClassToConsole(String classType) {
		String testId = TestObject.getTestInfo().testFileClassName + classType;
		printBatchToConsole(testId);

	}

	/**
	 * prints batch class to console based on testId
	 * 
	 * @param testId
	 */
	public static void printBatchToConsole(String testId) {
		// if batch login is disabled, return
		if (!CrossPlatformProperties.getEnableBatchLogging())
			return;

		// if test class object does not exist, return
		if (TestObject.testInfo.get(testId) == null)
			return;

		List<LogObject> logs = TestObject.getTestInfo(testId).testLog;
		if(!logs.isEmpty())
			printLogs(logs, testId);

	}

	/**
	 * prints current test logs
	 * 
	 * @param logs list of log objects
	 */
	public static void printLogs(List<LogObject> logs) {
		printLogs(logs, "");
	}

	/**
	 * prints logs to console removes logs from list after printing to console
	 * 
	 * @param testLog list of logs
	 * @param testId  id of the test
	 */
	public static void printLogs(List<LogObject> testLog, String testId) {

		for (LogObject log : testLog) {
			if (testId.isEmpty()) {
				TestObject.getTestInfo().log.log(log.priority, log.value);
			} else
				TestObject.getTestInfo(testId).log.log(log.priority, log.value);
		}
		if (testId.isEmpty())
			TestObject.getTestInfo().testLog = new ArrayList<LogObject>();
		else
			TestObject.getTestInfo(testId).testLog = new ArrayList<LogObject>();

	}

	public static void printLogoOnSuccess() {
		if (!Config.getBooleanValue("console.printLogoOnSuccess"))
			return;
		TestLog.ConsoleLog(
				"\r\n" + "                   #                   \r\n" + "               /##  /##                \r\n"
						+ "           ####    #    ###,           \r\n" + "(########      ########      (######## \r\n"
						+ "#        *###################         #\r\n" + "#  ############### # ############### .#\r\n"
						+ "#  ############# ##### ############/ /#\r\n" + "#  ########### ###  .###.##########  ##\r\n"
						+ "#( ######### ###       ### ########  # \r\n" + "##  ###### ###          .### ######  # \r\n"
						+ " #       ### #############.###      #( \r\n" + " ##    ### #################.###    #  \r\n"
						+ "  ##  ## ##################### #(  #   \r\n" + "   #(  #########################  #.   \r\n"
						+ "    #(  #######################  #/    \r\n" + "     ##  ####################   #      \r\n"
						+ "      *#   #################  ,#       \r\n" + "        ##   #############   #.        \r\n"
						+ "          #,   ########*   ##          \r\n" + "            ##    .#     #(            \r\n"
						+ "              ,##    ,##               \r\n" + "                  ##, ");

	}
}