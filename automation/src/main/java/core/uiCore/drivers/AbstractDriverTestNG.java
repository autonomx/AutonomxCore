package core.uiCore.drivers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import core.apiCore.driver.ApiTestDriver;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.listeners.RetryTest;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import core.uiCore.WebDriverSetup;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.capabilities.AndroidCapability;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

@Listeners({ core.support.listeners.TestListener.class })

public class AbstractDriverTestNG implements ITest {

	public static ExtentReports extent;
	public static ThreadLocal<ExtentTest> step = new ThreadLocal<ExtentTest>();

	private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

	public static ThreadLocal<String> testClassname = new ThreadLocal<String>();
	public static ThreadLocal<String> testName = new ThreadLocal<String>();

	public RetryTest retry = new RetryTest();

	public AbstractDriverTestNG() {

	}

	public void setupApiDriver(ServiceObject apiObject) throws Exception {
		new ApiTestDriver().initTest(apiObject);

		// initiallize logging
		Logger log = Logger.getLogger("");
		TestObject.getTestInfo().log = log;

		ExtentManager.reportSetup();
	}

	/**
	 * setup driver for web and mobile testing if single sign in is enabled, we try
	 * to reuse the existing drivers if available
	 * 
	 * @param driverObject
	 * @return
	 * @throws Exception
	 */
	public static WebDriver setupWebDriver(DriverObject driverObject) throws Exception {

		initTest(driverObject);
		ExtentManager.reportSetup();

		// setup web driver if the test is not api
		TestLog.ConsoleLogDebug("driverObject.driverType: " + driverObject.driverType);
		if (driverObject.driverType != null && driverObject.driverType.equals(DriverType.API))
			return null;

		setWebDriver(DriverObject.getFirstAvailableDriver());

		TestLog.ConsoleLogDebug("isForcedRestart: " + TestObject.getTestInfo().isForcedRestart + " isSingleSignIn(): "
				+ CrossPlatformProperties.isSingleSignIn() + " webDriver: " + AbstractDriver.getWebDriver());

		boolean condition1 = TestObject.getTestInfo().isForcedRestart && CrossPlatformProperties.isSingleSignIn();
		boolean condition2 = !CrossPlatformProperties.isSingleSignIn();
		boolean condition3 = AbstractDriver.getWebDriver() == null;

		if (condition1 || condition2 || condition3) {
			setWebDriver(createDriver(driverObject));
			driverObject.withIsAvailable(false);
			DriverObject.initializeDriverList(driverObject, TestObject.getTestInfo().testId); // driver is not available
			TestObject.getTestInfo().withIsFirstRun(true);
		}
		// associate current driver with test object
		TestObject.getTestInfo().withWebDriver(AbstractDriver.getWebDriver());
		// get url if is web test
		getURL(driverObject.initialURL);
		// set full screen for browser if set true in properties
		setFullScreen();

		return AbstractDriver.getWebDriver();
	}

	/**
	 * initialize test based on testId
	 * 
	 * @param testId
	 * @param driver
	 */
	public void setupWebDriver(String testId, DriverObject driver) {

		TestObject.setTestId(testId);

		// setup driver
		try {
			setupWebDriver(driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * maximized web page if maximize_browser option is set to true
	 */
	private static void setFullScreen() {
		if (!Helper.mobile.isMobile() && Config.getValue("web.maximizeBrowser").equals("true")) {
			Helper.page.maximizePage();
		}
	}

	/**
	 * initialized testInfo with testId as key runs before test adds current driver
	 * info to test object
	 * 
	 * @param driverObject
	 */
	public static void initTest(DriverObject driverObject) {

		String testId = TestObject.getTestId();

		TestLog.removeLogUtilHandler();
		// initialize once per test
		TestObject.initializeTest(driverObject, testId);

		// initiallize logging
		TestObject.setLogging();

		// sets current driver info for the test
		TestObject.getTestInfo().withCurrentDriver(driverObject);
		DriverObject.initializeDriverList(driverObject, testId); // update existing driver with new test id
	}

	public static void setWebDriver(WebDriver webDriver) {
		AbstractDriverTestNG.webDriver.set(webDriver);
	}

	/**
	 * generates new testId from class name And test name if already generated, Then
	 * return existing
	 * 
	 * @return
	 */
	@BeforeMethod(alwaysRun = true)
	public synchronized void handleTestMethodName(Method method, ITestResult iTestResult, Object[] testData) {
		TestObject.setTestName(method.getName());
		TestObject.setTestId(getClassName(), TestObject.currentTestName.get());

		// set the test name for service tests based on test data values
		ApiTestDriver.setServiceTestName(testData);

		// append test invocation count to test name if data provider is running
		// increments the invocation count
		// not applicable to service tests
		setAndIncremenetDataProviderTestExtention(method, testData);

		setUiTestname(method, testData);

		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		
		new AbstractDriverTestNG().setupWebDriver(TestObject.getTestId(), driver);
		
		// set test name for reports. eg. junit report
		setResultTestName(testData, iTestResult);
	}

	/**
	 * // set test name for reports. eg. junit report
	 * 
	 * @param result
	 */
	public void setResultTestName(Object[] testData, ITestResult result) {
		if (!ApiTestDriver.isRunningServiceTest(testData))
			return;

		try {
			Field methodName = org.testng.internal.BaseTestMethod.class.getDeclaredField("m_methodName");
			methodName.setAccessible(true);
			methodName.set(result.getMethod(), testName.get());

		} catch (Exception e) {
			e.printStackTrace();
			Reporter.log("Exception : " + e.getMessage());
		}
	}

	/**
	 * append test invocation count to test name if data provider is running
	 * increments the invocation count
	 * 
	 * @param method
	 */
	private void setAndIncremenetDataProviderTestExtention(Method method, Object[] testData) {

		// return if service test
		if (ApiTestDriver.isRunningServiceTest(testData))
			return;

		if (isDataProviderRunning(method)) {
			int invocationCount = TestObject.getTestInvocationCount(TestObject.getTestId());
			invocationCount++;
			TestObject.setTestName(method.getName() + TestObject.DATAPROVIDER_TEST_SUFFIX + invocationCount);
			TestObject.setTestId(getClassName(), TestObject.currentTestName.get());
			testName.set(TestObject.getTestId());
		}
	}

	private void setUiTestname(Method method, Object[] testData) {
		// return if service test
		if (ApiTestDriver.isRunningServiceTest(testData))
			return;
		if (isDataProviderRunning(method))
			return;
		testName.set(TestObject.getTestId());
	}

	private boolean isDataProviderRunning(Method method) {
		return method.getParameterCount() > 0;
	}

	private String getClassName() {
		String className = getClass().toString().substring(getClass().toString().lastIndexOf(".") + 1);
		testClassname.set(className);
		return className;
	}

	private static WebDriver createDriver(DriverObject driverObject) throws Exception {
		int retry = 3;
		WebDriver driver = null;
		do {
			try {
				retry--;
				driver = new WebDriverSetup().getWebDriverByType(driverObject);

				// set implicit Wait wait to be the minimum of our explicit wait
				driver.manage().timeouts().implicitlyWait(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
				driver.manage().timeouts().pageLoadTimeout(AbstractDriver.TIMEOUT_SECONDS, TimeUnit.SECONDS);

			} catch (Exception e) {
				if (retry > 0)
					TestLog.ConsoleLog("driver failed to start. retrying " + retry + " more time(s) ...");

				Helper.wait.waitForSeconds(3);
				if (retry == 0) {
					// print out android help
					if (driverObject.driverType.equals(DriverType.ANDROID_DRIVER))
						AndroidCapability.printAndroidHelp(e);
					throw e;
				}

			}

		} while (driver == null && retry > 0);

		TestLog.ConsoleLog("driver created successfully");
		Helper.assertTrue("driver was not created", driver != null);
		return driver;
	}

	private static void getURL(String url) {
		if (!url.isEmpty()) {
			TestLog.logPass("I am the site '" + url + "'");
			getWebDriver().get(url);
		}
	}

	public static WebDriver getWebDriver() {
		return webDriver.get();
	}

	@AfterMethod
	public void shutdown(ITestResult iTestResult) {

		// print after method
		TestLog.printBatchLogsToConsole();

		letRetryKnowAboutReports();

		// shut down drivers after test
		DriverObject.shutDownDriver(iTestResult.isSuccess());

	}

	/**
	 * After class batch log print is called here, cause this after class method is
	 * called after all other after class methods
	 * 
	 * @param iTestContext
	 */
	@AfterClass
	public synchronized void afterClassMethod(ITestContext iTestContext) {
		String name = getClassName();
		// print after class logs
		String testId = name + TestObject.AFTER_CLASS_PREFIX;
		TestLog.printBatchToConsole(testId);
	}

	private void letRetryKnowAboutReports() {

		retry.setExtendReport(TestObject.getTestInfo().testScenerio, step.get());
	}

	@Override
	public String getTestName() {
		if (testName.get() == null)
			testName.set("");
		return testName.get();

	}
}