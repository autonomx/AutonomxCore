package core.support.listeners;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.xml.DOMConfigurator;
import org.testng.IClassListener;
import org.testng.IConfigurationListener2;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite.ParallelMode;

import com.google.common.base.Joiner;

import core.helpers.Helper;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.DeviceManager;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriverTestNG;

public class TestListener implements ITestListener, IClassListener, ISuiteListener, IConfigurationListener2 {

	public static boolean isTestNG = false;

	// Before starting all tests, below method runs.
	@Override
	public void onStart(ITestContext iTestContext) {
		isTestNG = true;
		iTestContext.setAttribute("WebDriver", AbstractDriverTestNG.getWebDriver());
		TestLog.setupLog4j();

		// initialize logging
		TestObject.setLogging();

		DOMConfigurator.configure(TestLog.LOG4JPATH);

		// shuts down webdriver processes
		cleanupProcessess();

		// add retry listeners to all tests
		iTestContext.setAttribute("platform", "");
		for (ITestNGMethod method : iTestContext.getAllTestMethods()) {
			method.setRetryAnalyzer(new core.support.listeners.RetryTest(CrossPlatformProperties.getRetryCount()));
		}

		// sets parallel run for default user. overwritten by suite xml settings
		setParallelRun(iTestContext);

		// overwrite existing report
		ExtentManager.clearTestReport();

		// delete old reports
		ExtentManager.clearOldTestReports();
	}

	/**
	 * initialize default driver with suit name
	 * 
	 * @param iTestContext
	 */
	private void initializeDefaultTest(ISuite suite) {
		DriverObject driver = new DriverObject().withDriverType(DriverType.API).withApp(TestObject.DEFAULT_TEST);

		// set suite name as app name
		if (!suite.getName().contains("Default")) {
			driver.app = suite.getName();
		}
		TestObject.initializeTest(driver, TestObject.DEFAULT_TEST);
		// update default test app name. only time it will be updated
		TestObject.getTestInfo().withApp(driver.app);
	}

	/**
	 * sets parallel run count
	 * 
	 * @param iTestContext
	 */
	private void setParallelRun(ITestContext iTestContext) {
		iTestContext.getCurrentXmlTest().setParallel(ParallelMode.METHODS);
		int threadCount = CrossPlatformProperties.getParallelTests();
		iTestContext.getCurrentXmlTest().setThreadCount(threadCount);
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
		String message = generateTestMessage(iTestContext); // generate report message
		ExtentManager.slackNotification(message); // send slack notification
		ExtentManager.emailTestReport(message); // send test report
	}

	public void onTestStart(ITestResult iTestResult) {

		setTestClassName(iTestResult);
	}

	@Override
	public void onTestSuccess(ITestResult iTestResult) {
		TestObject.getTestInfo().isTestComplete = TestObject.isTestcomplete();

		// sets the class name for logging before class
		setTestClassName(iTestResult);

		TestLog.Then("Test is finished successfully");
		TestLog.printLogsToConsole();

		// if single signin is set, then set isFirstRun to false so new driver is not
		// created for next test
		if (CrossPlatformProperties.isSingleSignIn()) {
			// driver is now available
			if (AbstractDriverTestNG.getWebDriver() != null) {
				DriverObject.setDriverAvailabiltity(AbstractDriverTestNG.getWebDriver(), true);
			}
		}

		// mobile device is now available again
		DeviceManager.setDeviceAvailability(true);

		// reset test object
		TestObject.getTestInfo().resetTestObject();
	}

	@Override
	public void onTestFailure(ITestResult iTestResult) {
		TestObject.getTestInfo().isTestComplete = TestObject.isTestcomplete();

		// sets the class name for logging before class
		setTestClassName(iTestResult);

		// print out console logs to console if batch logging is enabled
		TestLog.printLogsToConsole();
		// set forced restart to true, so new driver is created for next test
		TestObject.getTestInfo().withIsForcedRestart(true);
		TestObject.getTestInfo().isFirstRun = true;

		// mobile device is now available again
		DeviceManager.setDeviceAvailability(true);

		// quits web driver no matter the situation, as new browser will be launched
		DriverObject.quitTestDrivers();

		// reset test object
		TestObject.getTestInfo().resetTestObject();
	}

	@Override
	public void onTestSkipped(ITestResult iTestResult) {
		// set forced restart to true, so new driver is created for next test
		TestObject.getTestInfo().withIsForcedRestart(true);
		TestObject.getTestInfo().isFirstRun = true;

		iTestResult.setStatus(ITestResult.SKIP);

		// mobile device is now available again
		DeviceManager.setDeviceAvailability(true);

		DriverObject.quitTestDrivers();
		// Extentreports log operation for skipped tests.
		// ExtentTestManager.getTest().log(LogStatus.SKIP, "Test Skipped");
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
		TestLog.ConsoleLog("Test failed but it is in defined success ratio " + TestObject.getTestInfo().testName);
	}

	/**
	 * kills all process for clean start
	 */
	public void cleanupProcessess() {
		if (Helper.mobile.isMobile())
			Helper.killWindowsProcess("node.exe");
		Helper.killWindowsProcess("IEDriverServer.exe");
		Helper.killWindowsProcess("chromedriver.exe");
		Helper.killWindowsProcess("MicrosoftWebDriver.exe");
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
		String classname = testClass.getName();
		classname = classname.substring(classname.lastIndexOf(".") + 1);

		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(classname + TestObject.BEFORE_CLASS_PREFIX, driver);
	}

	@Override
	public void onAfterClass(ITestClass testClass) {
		String classname = testClass.getName();
		classname = classname.substring(classname.lastIndexOf(".") + 1);

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

	@Override
	public void beforeConfiguration(ITestResult tr) {
	}

	public void setTestClassName(ITestResult iTestResult) {
		String classname = iTestResult.getMethod().getTestClass().getName();
		classname = classname.substring(classname.lastIndexOf(".") + 1);
		TestObject.getTestInfo().testFileClassName = classname;
	}

	@Override
	public void onStart(ISuite suite) {

		// initialize default driver with suit testname
		initializeDefaultTest(suite);

		String suitename = suite.getName();
		suitename = suitename.replaceAll("\\s", "");

		// global identified for the app. if suite is default, then app_indentifier is
		// used for test run id
		TestObject.APP_IDENTIFIER = getTestPackage(suite);
		TestObject.SUITE_NAME = suitename;

		// setup before suite driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(TestObject.SUITE_NAME + TestObject.BEFORE_SUITE_PREFIX, driver);
	}

	/**
	 * gets package name excluding the first item before and after . eg.
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
		String suitename = suite.getName();
		suitename = suitename.replaceAll("\\s", "");

		// setup before suite driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(suitename + TestObject.AFTER_SUITE_PREFIX, driver);
	}
}
