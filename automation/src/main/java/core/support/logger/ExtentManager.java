package core.support.logger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.Feature;
import com.aventstack.extentreports.gherkin.model.Scenario;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentKlovReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

import core.apiCore.ServiceManager;
import core.helpers.Helper;
import core.helpers.UtilityHelper;
import core.helpers.emailHelper.EmailObject;
import core.support.configReader.Config;
import core.support.objects.TestObject;
import core.support.objects.TestObject.testState;

//OB: ExtentReports extent instance created here. That instance can be reachable by getReporter() method.

public class ExtentManager {

	public static final String LAUNCH_AFTER_REPORT = "report.launchReportAfterTest";
	public static final String NOTIFY_SLACK_ON_FAIL_ONLY = "slack.notifyOnFailureOnly";
	public static final String NOTIFY_EMAIL_ON_FAIL_ONLY = "email.notifyOnFailureOnly";

	public static final String ENABLE_SLACK_NOTIFICATION = "slack.enableSlackNotification";
	public static final String ENABLE_EMAIL_REPORT = "email.enableEmailReport";
	public static final String REPORT_EXPIRE_DAYS = "report.reportExpireDays";
	public static final String REPORT_TYPE = "report.reporterType";
	public static final String HTML_REPORT_TYPE = "html";
	public static final String KLOV_REPORT_TYPE = "klov";
	public static final String KLOV_SERVER_URL = "klov.server.url";
	public static final String KLOV_MONGODB_URL = "klov.mongodb.url";

	// list of classes (features)
	public static Map<String, ExtentTest> classList = new HashMap<String, ExtentTest>();

	private static ExtentReports extent;

	public static String REPORT_DEFAULT_NAME = "extent";
	public static ExtentKlovReporter klovReporter;

	public static String TEST_OUTPUT_PATH = File.separator + "test-output" + File.separator;
	public static String TEST_OUTPUT_FULL_PATH = Helper.getRootDir() + TEST_OUTPUT_PATH;

	public synchronized static ExtentReports getReporter() {
		if (extent == null)
			createInstance(getReportHTMLFullPath());

		return extent;
	}

	public static String getScreenshotsFolderFullPath() {
		return getReportRootFullPath() + "screenshots/";
	}

	public static String getScreenshotsFolderRelativePath() {
		return "screenshots/";
	}

	public static String getMediaFolderFullPath() {
		return getReportRootFullPath() + "media/";
	}

	public static String getMediaFolderRelativePathFromHtmlReport() {
		return "media/";
	}

	public static String getMediaFolderRelativePathFromRoot() {
		return getReportRootRelativePath() + getMediaFolderRelativePathFromHtmlReport();
	}

	public static String getReportHTMLFullPath() {
		return getReportRootFullPath() + getReportName() + ".html";
	}

	public static String getReportRootFullPath() {
		return Helper.getRootDir() + getReportRootRelativePath();
	}

	/**
	 * gets report folder path eg.selenium/test-output/testReports/20181124/core/
	 * 
	 * @return
	 */
	public static String getReportRootRelativePath() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String folderName = dateFormat.format(date);
		return TEST_OUTPUT_PATH + "testReports" + File.separator + folderName + File.separator
				+ TestObject.getTestInfo(TestObject.getDefaultTestObjectId()).app + File.separator;
	}

	public static ExtentReports createInstance(String fileName) {

		extent = new ExtentReports();
		extent.setAnalysisStrategy(AnalysisStrategy.BDD);

		// setup html reporter
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(fileName);
		htmlReporter.config().setAutoCreateRelativePathMedia(true);
		htmlReporter.config().setTheme(Theme.STANDARD);
		htmlReporter.config().setDocumentTitle(fileName);
		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setProtocol(Protocol.HTTPS);
		htmlReporter.config().setReportName(fileName);
		htmlReporter.config().enableTimeline(true);
		htmlReporter.setAnalysisStrategy(AnalysisStrategy.BDD);

		if (Config.getValue(REPORT_TYPE).equals(HTML_REPORT_TYPE))
			extent.attachReporter(htmlReporter);

		// setup klov reporter
		setKlovReportReporter();

		return extent;
	}

	// TODO: set in test listener
	public static void setupReportPage() {
		// will run only once per test run
		// initializes the test report html page
		if (TestObject.getTestInfo().runCount == 0) {
			extent = ExtentManager.getReporter();
		}
	}

	/**
	 * setup report only for test methods, including before and after test not
	 * including: before suite, after suite, before class, after class this means
	 * only logs for test methods will show up in the reports reason: test report
	 * treats before/after suite, before/after class as tests and increases test
	 * count
	 */
	public static void reportSetup() {

		// Only setup report for test method. Ignores before suite, after suite, before
		// class, after class
		String testId = TestObject.getTestInfo().testId;
		testState state = TestObject.getTestState(testId);
		if (!state.equals(testState.testMethod))
			return;

		// will run only once per test run
		// initializes the test report html page
		setupReportPage();

		// will create parent once per class
		// initializes the test instance
		String className = TestObject.getTestInfo().getClassName();

		// if service test runner, return. Service tests have different test names once
		// the test starts, based on csv data
		if (className.equals(ServiceManager.SERVICE_TEST_RUNNER_ID))
			return;

		if (!classList.containsKey(className)) {
			String testParent = className.substring(className.lastIndexOf('.') + 1).trim();
			testParent = parseTestName(testParent);
			ExtentTest feature = extent.createTest(Feature.class, testParent);
			classList.put(className, feature);
			TestObject.getTestInfo().testFeature = feature;
		}

		// will run once every test
		// initializes test report
		if (TestObject.getTestInfo().runCount == 0) {
			TestObject.getTestInfo().incremenetRunCount();
			String testChild = TestObject.getTestInfo().testName;
			testChild = parseTestName(testChild);
			ExtentTest scenario = classList.get(className).createNode(Scenario.class, testChild);
			TestObject.getTestInfo().withTestScenario(scenario);
			TestLog.Background(TestObject.getTestInfo().testName + " initialized successfully");
		}
	}

	/**
	 * if test are run through suite, set project name as suite if test are run
	 * outside of suite, use the module/app name
	 */
	public static void setKlovReportReporter() {

		if (!Config.getValue(REPORT_TYPE).equals(KLOV_REPORT_TYPE))
			return;

		// setup klov reporter
		klovReporter = new ExtentKlovReporter();
		klovReporter.initMongoDbConnection(Config.getValue(KLOV_MONGODB_URL));
		klovReporter.initKlovServerConnection(Config.getValue(KLOV_SERVER_URL));

		klovReporter.setAnalysisStrategy(AnalysisStrategy.BDD);

		// set project name. if suite name is set (from suite file) Then use, else get
		// test project name
		if (TestObject.SUITE_NAME.contains("Default"))
			klovReporter.setProjectName(TestObject.APP_IDENTIFIER);
		else
			klovReporter.setProjectName(TestObject.SUITE_NAME);

		// set report name as current date time
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formatDateTime = now.format(formatter);
		klovReporter.setReportName(formatDateTime);

		if (Config.getValue(REPORT_TYPE).equals(KLOV_REPORT_TYPE))
			extent.attachReporter(klovReporter);
	}

	/**
	 * launches the report html page after test run
	 * 
	 * @throws Exception
	 */
	public static void launchReportAfterTest() {
		if (Config.getBooleanValue(LAUNCH_AFTER_REPORT)) {

			URI link = null;
			try {
				if (Config.getValue(REPORT_TYPE).equals(KLOV_REPORT_TYPE)) {
					link = new URI(Config.getValue(KLOV_SERVER_URL));
				}
				if (Config.getValue(REPORT_TYPE).equals(HTML_REPORT_TYPE)) {
					link = new File(getReportHTMLFullPath()).toURI();
				}

				// open the default web browser for the HTML page

				Desktop.getDesktop().browse(link);
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}

	public static void writeTestReport() {
		// removeEmptyTestNodesFromReport();
		try {
			new File(getReportRootFullPath()).mkdirs();
			new File(getReportHTMLFullPath()).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ExtentManager.getReporter().flush();
	}

	/**
	 * Note: currently disabled as we're only adding test nodes to report removes
	 * empty logs from the test report these are logs that are initialized, But no
	 * test steps have been added to them note: test suite is removed, cause the
	 * feature When empty cannot be removed. feature in code is not associated with
	 * test steps TODO: find a way to preserve suite logs
	 */
	public static void removeEmptyTestNodesFromReport() {

		// remove default test
		/*
		 * boolean hasChild =
		 * TestObject.getTestInfo(TestObject.DEFAULT_TEST).testFeature.getModel().
		 * isChildNode(); ExtentTest test =
		 * TestObject.getTestInfo(TestObject.DEFAULT_TEST).testFeature;
		 * ExtentManager.getReporter().removeTest(test);
		 */
		// remove suite logs
		// TODO: find way to remove empty before suite from report
		for (Entry<String, TestObject> entry : TestObject.testInfo.entrySet()) {
			if (entry.getValue().testName.contains("Beforesuite") || entry.getValue().testName.contains("Aftersuite")) {
				try {
					ExtentManager.getReporter().removeTest(entry.getValue().testFeature);
				} catch (Exception e) {
					e.getMessage();
				}
			}
		}

		for (Entry<String, TestObject> entry : TestObject.testInfo.entrySet()) {
			if (entry.getValue().testName.contains("Beforeclass") || entry.getValue().testName.contains("Aftersuite")) {
				try {
					ExtentManager.getReporter().removeTest(entry.getValue().testFeature);
				} catch (Exception e) {
					e.getMessage();
				}
			}
		}

		// remove all tests with no test substeps. this means, no logging was done for
		// the test
		for (Entry<String, TestObject> entry : TestObject.testInfo.entrySet()) {
			if (entry.getValue().testScenerio != null && entry.getValue().testSubSteps.size() <= 1) {
				try {
					ExtentManager.getReporter().removeTest(entry.getValue().testScenerio);
				} catch (Exception e) {
					e.getMessage();
				}
			}
		}
	}

	/**
	 * prints the test report link for klov or html report type
	 */
	public static void printReportLink() {
		String link = "";
		if (Config.getValue(REPORT_TYPE).equals(KLOV_REPORT_TYPE)) {
			link = Config.getValue(KLOV_SERVER_URL);
		}
		if (Config.getValue(REPORT_TYPE).equals(HTML_REPORT_TYPE)) {
			link = new File(getReportHTMLFullPath()).toURI().toString();
		}
		System.out.println("Extent test report link: " + link);
	}

	/**
	 * returns the test report name the report name is stored in default test object
	 * if test suit runs, Then suit name is the report name if test is run without
	 * suit, Then app name is used for report name
	 * 
	 * @return
	 */
	public static String getReportName() {
		return REPORT_DEFAULT_NAME;
	}

	/**
	 * sends test report to slack slack notification must be enabled in properties
	 * file
	 * 
	 * @param message
	 */
	public static void slackNotification(String message) {
		// return if slack is not enabled
		if (!Config.getValue(ENABLE_SLACK_NOTIFICATION).equals("true"))
			return;

		TestLog.ConsoleLog("sending slack notification");

		// zip the test report directory
		String zipFile = UtilityHelper.zipDir(getReportRootFullPath(), TEST_OUTPUT_FULL_PATH + getReportName());

		// notify slack
		String comment = getReportName() + " automated tests complete. " + message;
		UtilityHelper.slackNotificationWithFile("test report", comment, zipFile);

	}

	/**
	 * zips up And emails test report with screenshot to specified email address
	 * 
	 * @throws Exception
	 */
	public static void emailTestReport(String message) {

		// return if email report not enabled
		if (!Config.getValue(ENABLE_EMAIL_REPORT).equals("true"))
			return;

		String fromEmail = Config.getValue("email.fromEmail");
		String toEmail = Config.getValue("email.toEmail");
		String password = Config.getValue("email.emailPassword");
		String smtpPort = Config.getValue("email.smtpPort");
		String smtpHost = Config.getValue("email.smtpHost");
		Boolean smtpStarttlsEnabled = Config.getBooleanValue("email.smtpStarttlsEnable");
		Boolean smtpAuth = Config.getBooleanValue("email.smtpAuth");

		TestLog.ConsoleLog("sending email report");

		// zip the test report directory
		String zipFilePath = UtilityHelper.zipDir(getReportRootFullPath(), TEST_OUTPUT_FULL_PATH + getReportName());
		// notify slack
		String comment = getReportName() + " automated tests complete. " + message;

		EmailObject email = new EmailObject().withToEmail(toEmail).withPassword(password).withFromEmail(fromEmail)
				.withSmtpPort(smtpPort).withSmtpHost(smtpHost).withSmtpStarttlsEnabled(smtpStarttlsEnabled)
				.withSmtpAuth(smtpAuth).withRecipientEmail(toEmail).withBody(comment)
				.withSubject(getReportName() + " test report").withAttachmentPath(zipFilePath)
				.withAttachmentFile("testReport.zip");

		Helper.sendMail(email);
	}

	/**
	 * deletes test report for particular run based on test run name
	 */
	public static void clearTestReport() {
		try {
			FileUtils.deleteDirectory(new File(getReportRootFullPath()));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearOldTestReports() {
		int maxDays = Config.getIntValue(REPORT_EXPIRE_DAYS);
		if (maxDays < 1)
			return;

		File folder = new File(TEST_OUTPUT_FULL_PATH + "testReports/");

		// return if report folder does not exist
		if (!folder.exists())
			return;

		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				long diff = new Date().getTime() - file.lastModified();
				long diffDays = diff / (24 * 60 * 60 * 1000);
				if (diffDays > maxDays) {
					try {
						FileUtils.deleteDirectory(file);
					} catch (IOException e) {
						e.getMessage();
					}
				}
			}
		}
	}

	/**
	 * formats test name to format from: "loginTest" to "Login Test"
	 * 
	 * @param value
	 * @return
	 */
	public static String parseTestName(String value) {
		String formatted = "";
		value = value.replace("_", " ");

		for (String w : value.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
			w = w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase();
			formatted = formatted + " " + w;
		}
		return formatted.trim();
	}
}