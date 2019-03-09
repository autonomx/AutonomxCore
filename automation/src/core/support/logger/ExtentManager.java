package core.support.logger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.aventstack.extentreports.AnalysisStrategy;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

import core.helpers.Helper;
import core.helpers.UtilityHelper;
import core.helpers.emailHelper.EmailObject;
import core.support.configReader.Config;
import core.support.objects.TestObject;

//OB: ExtentReports extent instance created here. That instance can be reachable by getReporter() method.

public class ExtentManager {
	public static final String LAUNCH_AFTER_REPORT = "report.launchReportAfterTest";
	public static final String ENABLE_SLACK_NOTIFICATION = "slack.enableSlackNotification";
	public static final String ENABLE_EMAIL_REPORT = "email.enableEmailReport";
	public static final String REPORT_EXPIRE_DAYS = "report.reportExpireDays";
	

	private static ExtentReports extent;
	public static String REPORT_DEFAULT_NAME = "extent";

	public static String TEST_OUTPUT_PATH = "/test-output/";
	public static String TEST_OUTPUT_FULL_PATH = Helper.getCurrentDir() + "/test-output/";

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

	public static String getReportHTMLFullPath() {
		return getReportRootFullPath() + getReportName() + ".html";
	}

	public static String getReportRootFullPath() {
		return Helper.getCurrentDir() + getReportRootRelativePath();
	}

	/**
	 * gets report folder path
	 * eg.selenium/test-output/testReports/20181124/core/
	 * @return
	 */
	public static String getReportRootRelativePath() {
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		String folderName =  dateFormat.format(date);
		return TEST_OUTPUT_PATH + "testReports/" + folderName + "/"
				+ TestObject.getTestInfo(TestObject.DEFAULT_TEST).app + "/";
	}

	public static ExtentReports createInstance(String fileName) {
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(fileName);
		htmlReporter.config().setAutoCreateRelativePathMedia(true);
	//	htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
	//	htmlReporter.config().setChartVisibilityOnOpen(true);
		htmlReporter.config().setTheme(Theme.STANDARD);
		htmlReporter.config().setDocumentTitle(fileName);
		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setJS("js-string");
		htmlReporter.config().setCSS("css-string");
		htmlReporter.config().setProtocol(Protocol.HTTPS);
		htmlReporter.config().setReportName(fileName);
		htmlReporter.config().enableTimeline(true);
		htmlReporter.setAnalysisStrategy(AnalysisStrategy.TEST);
		

		extent = new ExtentReports();
		extent.attachReporter(htmlReporter);
		return extent;
	}

	/**
	 * launches the report html page after test run
	 */
	public static void launchReportAfterTest() {
		if (Config.getValue(LAUNCH_AFTER_REPORT).equals("true")) {

			File htmlFile = new File(getReportHTMLFullPath());

			// open the default web browser for the HTML page
			try {
				Desktop.getDesktop().browse(htmlFile.toURI());
			} catch (IOException e) {
				e.getMessage();
			}
		}
	}

	public static void writeTestReport() {
		removeEmptyTestNodesFromReport();
		try {
			new File(getReportRootFullPath()).mkdirs();
			new File(getReportHTMLFullPath()).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ExtentManager.getReporter().flush();
	}

	/**
	 * removes empty logs from the test report these are logs that are initialized,
	 * but no test steps have been added to them note: test suite is removed, cause
	 * the feature when empty cannot be removed. feature in code is not associated
	 * with test steps TODO: find a way to preserve suite logs
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
			// TODO: find way to remove empty before suitfrom report
			for (Entry<String, TestObject> entry : TestObject.testInfo.entrySet()) {
				if (entry.getValue().testName.contains("Beforesuite") || entry.getValue().testName.contains("Aftersuite")) {
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
				if (entry.getValue().testScenerio != null && entry.getValue().testSubSteps.size() == 0) {
					try {
						ExtentManager.getReporter().removeTest(entry.getValue().testScenerio);
					} catch (Exception e) {
						e.getMessage();
					}
				}
			}
		}

	/**
	 * prints the test report link
	 */
	public static void printReportLink() {
		File htmlFile = new File(getReportHTMLFullPath());
		System.out.println("Extent test report link: " + htmlFile.toURI());
	}

	/**
	 * returns the test report name the report name is stored in default test object
	 * if test suit runs, then suit name is the report name if test is run without
	 * suit, then app name is used for report name
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
	 * zips up and emails test report with screenshot to specified email address
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
				.withSmtpPort(smtpPort).withSmtpHost(smtpHost).withSmtpStarttlsEnabled(smtpStarttlsEnabled).withSmtpAuth(smtpAuth)
				.withRecipientEmail(toEmail).withBody(comment).withSubject(getReportName() + " test report")
				.withAttachmentPath(zipFilePath).withAttachmentFile("testReport.zip");

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
						e.printStackTrace();
					}
				}
			}
		}
	}
}