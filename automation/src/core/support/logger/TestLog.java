package core.support.logger;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

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
	public static int MAX_LENGTH = 400; // in chars. currently disabled
	public static String WATSON = "WATSON";
	public static String MARY = "MARY";
	
	enum gherkins { Given, When, Then, And, But, Background }

	static marytts.util.data.audio.AudioPlayer player;
	public static final String LOG4JPATH = Config.RESOURCE_PATH + "/log4j.xml";

	public static void ConsoleLog(String value, Object... args) {
		logConsoleMessage(Priority.INFO, formatMessage(value, args));
	}

	public static void ConsoleLogWarn(String value, Object... args) {
		logConsoleMessage(Priority.WARN, formatMessage(value, args));

	}

	public static void ConsoleLogDebug(String value, Object... args) {
		logConsoleMessage(Priority.DEBUG, formatMessage(value, args));

	}

	public static void ConsoleLogError(String value, Object... args) {
		logConsoleMessage(Priority.ERROR, formatMessage(value, args));

	}

	public static void Background(String value, Object... args) {
		setTestStep(gherkins.Background, value, args);
	}

	public static void But(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "But " + formatMessage(value, args));
		setTestStep(gherkins.But, value, args);
	}

	public static void Given(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "Given " + formatMessage(value, args));
		setTestStep(gherkins.Given, value, args);
	}

	public static void When(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "When " + formatMessage(value, args));

		setTestStep(gherkins.When, value, args);
		playAudio(gherkins.When.name() + " "+ formatMessage(value, args));
	}

	public static void And(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "And " + formatMessage(value, args));

		setTestStep(gherkins.And, value, args);
		playAudio(gherkins.And.name() + " "+ formatMessage(value, args));
	}

	public static void Then(String value, Object... args) {
		logConsoleMessage(Priority.INFO, "Then " + formatMessage(value, args));

		setTestStep(gherkins.Then, value, args);
		playAudio(gherkins.Then.name() + " "+ formatMessage(value, args));
	}

	/**
	 * sets test step: Given, When, Then, And
	 * 
	 * @param testStep
	 */
	public static void setTestStep1(ExtentTest testStep) {
		// if test step is not set, do not log. Test will be set in test method state only.
		if(TestObject.getTestInfo().testSteps == null) return;
		
		TestObject.getTestInfo().testSteps.add(testStep);
		AbstractDriver.getStep().set(testStep);
	}
	
	public static void setTestStep(gherkins gherkinState, String value, Object... args ) {
		ExtentTest testStep = null;
		
		testState state = TestObject.getTestState(TestObject.getTestInfo().testId);
		if (!state.equals(testState.testMethod))
			return;
		
		switch(gherkinState) {
		  case Given:
			  testStep = getTestScenario().createNode(Given.class, "Given " + formatMessage(value, args)).pass("");
		    break;
		  case When:
			  testStep = getTestScenario().createNode(When.class,"When " + formatMessage(value, args)).pass("");
			  break;
		  case Then:
			  testStep = getTestScenario().createNode(Then.class,"Then " + formatMessage(value, args)).pass("");
		    break;   
		  case And:
			  testStep = getTestScenario().createNode(And.class, "And " + formatMessage(value, args)).pass("");
			  break;
		  case But:
			  testStep =  getTestScenario().createNode(But.class, "But " +formatMessage(value, args)).pass("");
			  break;
		  case Background:
			  testStep =  getTestScenario().createNode(Background.class, formatMessage(value, args)).pass("");
			  break;
		  default:
		   	  Helper.assertFalse("incorrect state " + gherkinState.name());
		}
		// if test step is not set, do not log. Test will be set in test method state only.
		if(TestObject.getTestInfo().testSteps == null) return;
		
		TestObject.getTestInfo().testSteps.add(testStep);
		AbstractDriver.getStep().set(testStep);
	}

	/**
	 * sets substep as pass
	 * 
	 * @param subStep
	 */
	public static void setPassSubTestStep(String subStep) {
		testState state = TestObject.getTestState(TestObject.getTestInfo().testId);
		if (!state.equals(testState.testMethod))
			return;
		
		TestObject.getTestInfo().testSubSteps.add(subStep);
		Markup m = MarkupHelper.createCodeBlock(subStep);
		AbstractDriver.getStep().get().pass(m);
	}

	/**
	 * logpass is used for test steps for extend report, enable if properties option
	 * enableDetailedReport is true
	 * 
	 * @param value
	 * @param args
	 */
	public static void logPass(String value, Object... args) {
		logConsoleMessage(Priority.INFO, formatMessage(value, args));
		if (Config.getValue(ENABLE_EXTENT_SUBSTEPS).equals("true")) {
			// this will only throw exception with before suite
			setPassSubTestStep(formatMessage(value, args));
		}
	}

	public static void logFail(String value, Object... args) {
		logConsoleMessage(Priority.ERROR, formatMessage(value, args));

		// AbstractDriver.getLog().get().error(formatMessage(value, args));
	}

	public static void logWarning(String value, Object... args) {
		logConsoleMessage(Priority.WARN, formatMessage(value, args));
		AbstractDriver.getStep().get().warning(formatMessage(value, args));
	}

	public static ExtentTest getTestScenario() {
		return TestObject.getTestInfo().testScenerio;

	}

	public static String formatMessage(String value, Object... args) {
		// disabled. enable if we ever want to limit the value length
		//value = setMaxLength(value); 

		if (args == null || args.length == 0) {
			return value;
		} else {
			return new MessageFormat(value).format(args);
		}
	}

	/**
	 * limites the max value size
	 * 
	 * @param value
	 * @return
	 */
	public static String setMaxLength(String value) {
		return setMaxLength(value,MAX_LENGTH );	
	}
	
	public static String setMaxLength(String value, int length) {
		// limit the max size of string
		int maxLength = (value.length() < length) ? value.length() : length;
		if (maxLength == length)
			return value.substring(0, maxLength) + " ...";
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

	public static void setupLog4j() {
		DOMConfigurator.configure(TestLog.LOG4JPATH);
	}

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
	 * @param value
	 */
	public static void playMaryAudio(String value) {

		try {
			MaryInterface marytts = new LocalMaryInterface();
			Set<String> voices = marytts.getAvailableVoices();
			String[] voiceList = voices.toArray(new String[voices.size()]);

			marytts.setVoice(voiceList[0]);

			AudioInputStream audio = marytts.generateAudio(value);
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
	 * @param value
	 */
	public synchronized static void playWatsonAudio(String value) {
		TextToSpeech textToSpeech = new TextToSpeech();
		textToSpeech.setUsernameAndPassword("55fffe86-bf08-4e28-82d3-c934fc8dff38", "0PQpBf8XIWCj");
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
	 * @param priority
	 * @param value
	 */
	private static void logConsoleMessage(Priority priority, String value) {

		// if batch logging is disabled, log to console
		Boolean enableBatchLogging = CrossPlatformProperties.getEnableBatchLogging();
		value = getTestLogPrefix() + value;
		if (!enableBatchLogging)
			TestObject.getTestInfo().log.log(priority, value);

		// if bathch logging is enabled, keep track of all logs for bu
		if (!TestObject.getTestInfo().isTestComplete && enableBatchLogging) {
			LogObject log = new LogObject(value, priority);
			TestObject.getTestInfo().testLog.add(log);
		}
	}

	private static String getTestLogPrefix() {
		return TestObject.getTestInfo().className + "-" + TestObject.getTestInfo().testName + " - ";
	}

	/**
	 * prints out the entire test log for csv file at once Occurs When number of csv
	 * files are greater than 1
	 */
	public synchronized static void printLogsToConsole() {
		Boolean enableBatchLogging = CrossPlatformProperties.getEnableBatchLogging();

		if (TestObject.getTestInfo().isTestComplete && enableBatchLogging) {
			// print before class logs
			printClassLevelLogsToConsole("Beforeclass");

			// print test logs
			printLogs(TestObject.getTestInfo().testLog);
		}
	}

	/**
	 * prints class level logs to console prints only once
	 */
	public static void printClassLevelLogsToConsole(String classType) {
		String testId = TestObject.getTestInfo().testFileClassName + "-" + classType;
		// if test class object does not exist, return
		if (TestObject.testInfo.get(testId) == null)
			return;

		List<LogObject> logs = TestObject.getTestInfo(testId).testLog;
		printLogs(logs, testId);

	}

	/**
	 * prints current test logs
	 * 
	 * @param logs
	 */
	public static void printLogs(List<LogObject> logs) {
		printLogs(logs, "");
	}

	/**
	 * prints logs to console
	 * 
	 * @param logs
	 */
	public static void printLogs(List<LogObject> testLog, String testId) {
		if (testLog.isEmpty())
			return;

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
}