package core.uiCore.drivers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.Scenario;
import com.microsoft.appcenter.appium.EnhancedAndroidDriver;
import com.microsoft.appcenter.appium.Factory;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.exceptions.loginException;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.support.rules.RetryTest;
import core.uiCore.WebDriverSetup;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import io.appium.java_client.MobileElement;
import junit.framework.Assert;

@SuppressWarnings("deprecation")
public class AbstractDriverJunit {

	public static ExtentReports extent;
	public static ThreadLocal<ExtentTest> step = new ThreadLocal<ExtentTest>();
	private static boolean isBeforeTestRun = true;

	// key: classname value: feature
	public static Map<String, ExtentTest> testList = new ConcurrentHashMap<String, ExtentTest>();
	private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

	private static boolean setUpIsDone = false;

	@Rule
	public TestWatcher watcher = Factory.createWatcher();

	@Rule
	public RetryTest retry = new RetryTest(RetryTest.RETRYCOUNTER);

	// TODO set this to be configurable in properties
	@Rule
	public Timeout globalTimeout = new Timeout(6000 * 60 * 1000);

	@Rule
	public TestName testName = new TestName();

	public AbstractDriverJunit() {
	}

	public WebDriver setupWebDriver(DriverObject driverObject) throws Exception {

		initTest(driverObject);
		reportSetup();

		// setup web driver if the test is not api
		TestLog.ConsoleLog("driverObject.driverType: " + driverObject.driverType);
		if (driverObject.driverType != null && driverObject.driverType.equals(DriverType.API))
			return null;

		setWebDriver(DriverObject.getFirstAvailableDriver());

		TestLog.ConsoleLog("isForcedRestart: " + TestObject.getTestInfo().isForcedRestart + " isSingleSignIn(): "
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
		// update default test app name to current app
		TestObject.updateAppName(driverObject);

		getURL(driverObject.initialURL);
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
		// set or update test name
		TestObject.testInfo.get(testId).app = driver.app;
	}

	/**
	 * maximized web page if maximize_browser option is set to true
	 */
	public static void setFullScreen() {
		if (!Helper.mobile.isMobile() && Config.getValue("web.maximizeBrowser").equals("true")) {
			Helper.page.maximizePage();
		}
	}

	/**
	 * /** initialized testInfo with testId as key runs before test adds current
	 * driver info to test object
	 * 
	 * @param driverObject
	 */
	public synchronized void initTest(DriverObject driverObject) {
		//setTestId();

		String testId = TestObject.getTestId();

		TestLog.removeLogUtilHandler();
		// initialize once per test
		TestObject.initializeTest(driverObject, testId);

		// initiallize logging
		Logger log = Logger.getLogger(testId);
		TestObject.getTestInfo().log = log;

		// sets current driver info for the test
		TestObject.getTestInfo().withCurrentDriver(driverObject);
		DriverObject.initializeDriverList(driverObject, testId); // update existing driver with new test id

	}

	// TODO: remove
	public void setTestId() {
		TestObject.setTestName(testName.getMethodName());
		String className = getClass().toString().substring(getClass().toString().lastIndexOf(".") + 1);
		String testName = TestObject.currentTestName.get();
		if (testName.isEmpty())
			TestObject.setTestId(className);
		else
			TestObject.setTestId(className + "-" + TestObject.currentTestName.get());
		TestObject.getTestInfo().withClassName(getClassName());
		TestObject.getTestInfo().testFileClassName = getClassName();
	}
	
	@Before
	public void handleTestMethodName() {
		TestObject.setTestName(testName.getMethodName());
		TestObject.setTestId(getClassName(), TestObject.currentTestName.get());

		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverJunit().setupWebDriver(getClassName() + "-" + testName.getMethodName(), driver);
	}

	public static void setWebDriver(WebDriver webDriver) {
		AbstractDriverJunit.webDriver.set(webDriver);
	}

	/**
	 * before test run goes here
	 */
	@BeforeClass
	public static void beforeClass() {

		// before test run
		if (!setUpIsDone) {
			
			DriverObject driver = new DriverObject().withDriverType(DriverType.API).withApp(TestObject.DEFAULT_TEST);
			new AbstractDriverJunit().setupWebDriver(TestObject.DEFAULT_TEST, driver);
			
			// deletes screenshots
			ExtentManager.clearTestReport();
			setUpIsDone = true;
		}
	}

	public static void setupReportPage() {
		// will run only once per test run
		// initializes the test report html page
		if (isBeforeTestRun) {
			extent = ExtentManager.getReporter();
			isBeforeTestRun = false;
		}
	}

	public void reportSetup() {
		synchronized (AbstractDriverJunit.class) {
			// will run only once per test run
			// initializes the test report html page
			setupReportPage();

			// will create parent once per class
			// initializes the test instance
			String className = TestObject.getTestInfo().getClassName();
			if (!testList.containsKey(className)) {
				String testParent = className.substring(className.lastIndexOf('.') + 1).trim();
				testParent = parseTestName(testParent);
				ExtentTest feature = extent.createTest(testParent);
				testList.put(className, feature);
			}

			// will run once every test
			// initializes test report
			if (isBeforeTest()) {
				String testChild = TestObject.getTestInfo().testName;
				testChild = parseTestName(testChild);
				ExtentTest scenario = testList.get(className).createNode(Scenario.class, testChild);
				TestObject.getTestInfo().withTestScenario(scenario);
			}
		}
		if (retry.getTest() != null)
			TestObject.getTestInfo().withTestScenario(retry.getTest());

		TestLog.Given(TestObject.getTestInfo().testName + " test initialized successfully");
	}

	/**
	 * returns true if before test has started
	 * 
	 * @return
	 */
	public boolean isBeforeTest() {
		return TestObject.getTestInfo().testScenerio == null;
	}

	public static String parseTestName(String value) {
		String formatted = "";
		value = value.replace("_", " ");

		for (String w : value.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
			w = w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase();
			formatted = formatted + " " + w;
		}
		return formatted.trim();
	}

	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	public static WebDriver createDriver(DriverObject driverObject) throws Exception {
		int retry = 3;
		WebDriver driver = null;
		do {
			try {
				retry--;

				Helper.killWindowsProcess("node.exe");
				driver = new WebDriverSetup().getWebDriverByType(driverObject);

				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				// AbstractDriverJunit.log.get().error("Driver create error: " +
				// e.getMessage());
				TestLog.ConsoleLogError("Driver create error: " + e.getMessage());
				Helper.wait.waitForSeconds(3);
				if (retry == 0) {
					throw new loginException("driver creation failed");
				}

			}

		} while (driver == null && retry > 0);

		Assert.assertTrue("driver was not created", driver != null);

		return driver;
	}

	public void getURL(String url) {
		if (!url.isEmpty()) {
			TestLog.And("I am the site '" + url + "'");
			getWebDriver().get(url);
		}
	}

	public static WebDriver getWebDriver() {
		try {
			return webDriver.get();
		} catch (Exception e) {
			return null;
		}
	}

	@After
	public void shutdown() {
		// TestLog.ConsoleLog("page source" +
		// AbstractDriver.getWebDriver().getPageSource());
		letRetryKnowAboutReports();
	}

	@SuppressWarnings("unchecked")
	public void takeAppcenterScreenshot(String label) {
		try {
			((EnhancedAndroidDriver<MobileElement>) getWebDriver()).label(label);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void letRetryKnowAboutReports() {
		retry.setExtendReport(TestObject.getTestInfo().testScenerio, step.get(), extent);
		// retry.setLogger(log.get());
		retry.setWebDriver(getWebDriver());
	}
}
