package core.support.listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.testng.IClassListener;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite.ParallelMode;

import com.google.common.base.Joiner;

import core.apiCore.ServiceManager;
import core.apiCore.driver.ApiTestDriver;
import core.helpers.Helper;
import core.helpers.ScreenRecorderHelper;
import core.support.configReader.Config;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.DeviceManager;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriverTestNG;

public class TestListener implements ITestListener, IClassListener, ISuiteListener, IConfigurationListener {

	public static boolean isTestNG = false;
	public static final String PARALLEL_TEST_TYPE = "global.parallel.type";
	
	// Before starting all tests, below method runs.
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(ITestContext iTestContext) {
		isTestNG = true;
		iTestContext.setAttribute("WebDriver", AbstractDriverTestNG.getWebDriver());
		
		// print out suite console logs  if batch logging is enabled
		String testId = getSuiteName(iTestContext.getSuite().getName().toString()) + TestObject.BEFORE_SUITE_PREFIX;
		TestLog.printBatchToConsole(testId);

		// shuts down webdriver processes
		cleanupProcessess();

		// add retry listeners to all tests
		iTestContext.setAttribute("platform", "");
		for (ITestNGMethod method : iTestContext.getAllTestMethods()) {
			method.setRetryAnalyzer(new RetryTest());
		}

		// sets parallel run for default user. overwritten by suite xml settings
		setParallelRun(iTestContext);

		// overwrite existing report
		ExtentManager.clearTestReport();

		// delete old reports
		ExtentManager.clearOldTestReports();
		
		// delete screen recorder temp directory
		ScreenRecorderHelper.deleteScreenRecorderTempDir();
	}

	/**
	 * sets parallel run count
	 * sets parallel count for Tests and Data Provider tests
	 * 
	 * @param iTestContext
	 */
	private void setParallelRun(ITestContext iTestContext) {
		
		// set parallel test type
		String parallelType =  CrossPlatformProperties.getParallelTestType();
		if(parallelType.equals("CLASSES"))
			iTestContext.getCurrentXmlTest().setParallel(ParallelMode.CLASSES);
		else 
			iTestContext.getCurrentXmlTest().setParallel(ParallelMode.METHODS);

		// set parallel thread count for tests
		int threadCount = CrossPlatformProperties.getParallelTests();
		iTestContext.getCurrentXmlTest().setThreadCount(threadCount);
		
		// set parallel thread count for data provider tests, not including service tests
		iTestContext.getCurrentXmlTest().getSuite().setDataProviderThreadCount(threadCount);
		iTestContext.getCurrentXmlTest().getSuite().setPreserveOrder(true);
	}

	/**
	 * After ending all tests, below method runs. The report is launched after the
	 * test suit finishes
	 */
	@Override
	public void onFinish(ITestContext iTestContext) {
		ExtentManager.writeTestReport();
		DriverObject.quitAllDrivers();
		ExtentManager.launchReportAfterTest();
		ExtentManager.printReportLink();
		sendReport(iTestContext);
		
		// get suite name, removing spaces
		String suitename = getSuiteName(iTestContext.getSuite().getName());

		// setup after suite driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(suitename + TestObject.AFTER_SUITE_PREFIX, driver);
		
		// if tests passed, print success autonomx logo if enabled
		if(iTestContext.getFailedTests().size() == 0)
			TestLog.printLogoOnSuccess();
	}
	
	/**
	 * send slack or email report
	 * depend on slack.notifyOnFailureOnly or email.notifyOnFailureOnly
	 * email.enableEmailReport and slack.enableSlackNotification have to be set true for send to occur
	 * @param iTestContext
	 */
	private void sendReport(ITestContext iTestContext) {
		String message = generateTestMessage(iTestContext); // generate report message
		
		boolean hasErrors = iTestContext.getFailedTests().size() > 0;
		boolean slackNotifyOnFailOnly = Config.getBooleanValue(ExtentManager.NOTIFY_SLACK_ON_FAIL_ONLY);
		boolean emailNotifyOnFailOnly = Config.getBooleanValue(ExtentManager.NOTIFY_EMAIL_ON_FAIL_ONLY);

		// send slack notification if error only is set and test fails exist, else if error only is false, send slack
		if(slackNotifyOnFailOnly && hasErrors) {
			ExtentManager.slackNotification(message); // send slack notification
		}else if(!slackNotifyOnFailOnly) {
			ExtentManager.slackNotification(message); // send slack notification
		}
		
		// send email notification if error only is set and test fails exist, else if error only is false, send slack	
		if(emailNotifyOnFailOnly && hasErrors) {
			ExtentManager.emailTestReport(message); // send email
		}else if(!emailNotifyOnFailOnly) {
			ExtentManager.emailTestReport(message); // send email
		}
	}

	public void onTestStart(ITestResult iTestResult) {
		
		setTestClassName(iTestResult);
		ScreenRecorderHelper.startRecording();
	}

	@Override
	public void onTestSuccess(ITestResult iTestResult) {

		// sets the class name for logging before class
		setTestClassName(iTestResult);
		
		// set test status to pass
		TestObject.getTestInfo().withIsTestPass(true);
		TestObject.getTestInfo().withIsTestComplete(ApiTestDriver.isCsvTestComplete());

		TestLog.Then("Test is finished successfully");
		TestLog.printBatchLogsToConsole();
		
		// stop screen recording if enabled
		ScreenRecorderHelper.stopRecording();

		// if single sign in is set, Then set isFirstRun to false so new driver is not
		// created for next test
		if (CrossPlatformProperties.isSingleSignIn()) {
			// driver is now available
			if (AbstractDriverTestNG.getWebDriver() != null) {
				DriverObject.setDriverAvailabiltity(AbstractDriverTestNG.getWebDriver(), true);
			}
		}

		// mobile device is now available again
		DeviceManager.setDeviceAvailability(true);
	}

	@Override
	public void onTestFailure(ITestResult iTestResult) {
		
		// sets the class name for logging before class
		setTestClassName(iTestResult);

		// print out console logs to console if batch logging is enabled
		TestLog.printBatchLogsToConsole();
		// set forced restart to true, so new driver is created for next test
		TestObject.getTestInfo().withIsForcedRestart(true);
		TestObject.getTestInfo().isFirstRun = true;
		TestObject.getTestInfo().withIsTestPass(false);
		TestObject.getTestInfo().withIsTestComplete(ApiTestDriver.isCsvTestComplete());
		
		// mobile device is now available again
		DeviceManager.setDeviceAvailability(true);
		
		// stop screen recording if enabled
		ScreenRecorderHelper.stopRecording();
	}

	@Override
	public void onTestSkipped(ITestResult iTestResult) {
		// set forced restart to true, so new driver is created for next test
		TestObject.getTestInfo().withIsForcedRestart(true);
		TestObject.getTestInfo().isFirstRun = true;
		TestObject.getTestInfo().withIsTestPass(false);

		iTestResult.setStatus(ITestResult.SKIP);

		// mobile device is now available again
		DeviceManager.setDeviceAvailability(true);
		
		// stop screen recording if enabled
		ScreenRecorderHelper.stopRecording();

		// Extentreports log operation for skipped tests.
		// ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
		TestLog.ConsoleLog("Test failed But it is in defined success ratio " + TestObject.getTestInfo().testName);
	}

	/**
	 * kills all process for clean start
	 */
	public void cleanupProcessess() {
		if(Helper.isWindows()) {
			if (Helper.mobile.isMobile())
				Helper.killWindowsProcess("node.exe");
			Helper.killWindowsProcess("IEDriverServer.exe");
			Helper.killWindowsProcess("chromedriver.exe");
			Helper.killWindowsProcess("MicrosoftWebDriver.exe");
		}else if (Helper.isMac()) {
			Helper.killMacProcess("chromedriver");
		}
		
	}

	public String generateTestMessage(ITestContext iTestContext) {
		int testCount = iTestContext.getPassedTests().size() + iTestContext.getFailedTests().size();
		String message = iTestContext.getPassedTests().size() + " of " + testCount + " tests passed.";
		if (iTestContext.getFailedTests().size() > 0) {
			ArrayList<String> failedMessage = getAllResults(iTestContext.getFailedTests().getAllResults());
			message += " Failed test(s): " + Joiner.on(",").join(failedMessage);
		}
		return message;
	}

	/**
	 * gets the list of tests names based on results
	 * 
	 * @param results
	 * @return
	 */
	private ArrayList<String> getAllResults(Collection<ITestResult> results) {
		ArrayList<String> tests = new ArrayList<String>();
		for (ITestResult result : results) {
			String className = result.getInstanceName().substring(result.getInstanceName().lastIndexOf(".") + 1);
			tests.add(className + "." + result.getMethod().getMethodName());
		}
		return tests;
	}

	@Override
	public void onBeforeClass(ITestClass testClass) {
		
		String classname = getClassName(testClass.getName());

		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(classname + TestObject.BEFORE_CLASS_PREFIX, driver);
	}

	@Override
	public void onAfterClass(ITestClass testClass) {
		String classname = getClassName(testClass.getName());

		// setup after class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(classname + TestObject.AFTER_CLASS_PREFIX, driver);
	}

	@Override
	public void onConfigurationSuccess(ITestResult itr) {
	}

	@Override
	public void onConfigurationFailure(ITestResult itr) {
	}

	@Override
	public void onConfigurationSkip(ITestResult itr) {
	}

	public void setTestClassName(ITestResult iTestResult) {
		String classname = iTestResult.getMethod().getTestClass().getName();
		classname = classname.substring(classname.lastIndexOf(".") + 1);
		TestObject.getTestInfo().testFileClassName = classname;
	}

	/**
	 * onStart (suite) runs before onStart(ItestContext)
	 */
	@Override
	public void onStart(ISuite suite) {

		TestLog.setupLog4j();
		
		// setup default drivers
		TestObject.setupDefaultDriver();
		
		TestLog.ConsoleLog("Autonomx initiating...");

		// get suite name, remove spaces
		String suitename = getSuiteName(suite.getName());

		// global identified for the app. if suite is default, Then app_indentifier is
		// used for test run id
		TestObject.APP_IDENTIFIER = getTestPackage(suite);
		TestObject.SUITE_NAME = suitename;

		// setup before suite driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(TestObject.SUITE_NAME + TestObject.BEFORE_SUITE_PREFIX, driver);
		
		// run service before suite if test method is serviceRunner
		if(isServiceSuite(suite))
			ServiceManager.runServiceBeforeSuite();
	}
	
	/**
	 * return true if service runner method is the only method is service runner
	 * @param suite
	 * @return
	 */
	private boolean isServiceSuite(ISuite suite) {
		List<ITestNGMethod> methods = suite.getAllMethods();
		if(methods.size() == 0 || methods.size() > 1) return false;
		if(methods.get(0).getMethodName().contains("serviceRunner"))
			return true;
		return false;
	}

	/**
	 * gets package name excluding the first item before And after . eg.
	 * Module.web.test.LoginTest becomes web.tests
	 * 
	 * @param suite
	 * @return
	 */
	private String getTestPackage(ISuite suite) {
		String testPackageName = "";
		try {
			testPackageName = suite.getAllMethods().get(0).getInstance().toString();
			testPackageName = testPackageName.substring(testPackageName.indexOf(".") + 1);
			int lastIndex = testPackageName.lastIndexOf('.');
			testPackageName = testPackageName.substring(0, lastIndex);
		} catch (Exception e) {
			e.getMessage();
		}
		return testPackageName;
	}

	@Override
	public void onFinish(ISuite suite) {
		// print out suite console logs  if batch logging is enabled
		String testId = getSuiteName(suite.getName()) + TestObject.AFTER_SUITE_PREFIX;
		TestLog.printBatchToConsole(testId);
		
		// run service before suite if test method is serviceRunner
		if(isServiceSuite(suite))
			ServiceManager.runServiceAfterSuite();
		
		// print list of missing config variables
		Config.printMissingConfigVariables();
	}
	
	private String getSuiteName(String suitename) {
		suitename = suitename.replaceAll("\\s", "");
		return suitename;
	}
	
	private String getClassName(String className) {
		className = className.substring(className.lastIndexOf(".") + 1);
		return className;
	}
}
