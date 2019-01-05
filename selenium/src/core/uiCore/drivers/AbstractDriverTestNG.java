package core.uiCore.drivers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.Scenario;

import core.apiCore.driver.ApiTestDriver;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.listeners.RetryTest;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.ApiObject;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.WebDriverSetup;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

@Listeners(core.support.listeners.TestListener.class)

public class AbstractDriverTestNG {

	public static ExtentReports extent;
	public static ThreadLocal<ExtentTest> step = new ThreadLocal<ExtentTest>();

	public static Map<String, ExtentTest> classList = new HashMap<String, ExtentTest>();

	private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();

	public RetryTest retry = new RetryTest();

	public AbstractDriverTestNG() {

	}

	public synchronized void setupApiDriver(ApiObject apiObject) throws Exception {
		new ApiTestDriver().initTest(apiObject);

		// initiallize logging
		Logger log = Logger.getLogger("");
		TestObject.getTestInfo().log = log;

		reportSetup();
	}

	public synchronized static WebDriver setupWebDriver(DriverObject driverObject) throws Exception {
		initTest(driverObject);
		reportSetup();

		// setup web driver if the test is not api
		TestLog.ConsoleLogDebug("driverObject.driverType: " + driverObject.driverType);
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
	public static void setFullScreen() {
		if (!Helper.mobile.isMobile() && Config.getValue("maximize_browser").equals("true")) {
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
	 * generates new testId from classname and test name if already generated, then
	 * return existing
	 * 
	 * @return
	 */
	@BeforeMethod(alwaysRun = true)
	public void handleTestMethodName(Method method) {
		TestObject.setTestName(method.getName());
		TestObject.setTestId(getClassName(), TestObject.currentTestName.get());

		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(getClassName() + "-" + method.getName(), driver);
		// TestLog.removeLogUtilHandler();
	}
	
	/**
	 * initialize after method
	 * @param method
	 */
	@AfterMethod(alwaysRun = true)
	public void afterMethod(Method method) {
		TestObject.setTestName(method.getName());
		TestObject.setTestId(getClassName(), TestObject.currentTestName.get());

		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(getClassName() + "-" + method.getName(), driver);
	}

	public String getClassName() {
		String className = getClass().toString().substring(getClass().toString().lastIndexOf(".") + 1);
		return className;
	}

	// TODO: set in test listener
	public static void setupReportPage() {
		// will run only once per test run
		// initializes the test report html page
		if (TestObject.getTestInfo().runCount == 0) {
			extent = ExtentManager.getReporter();
		}
	}

	public static void reportSetup() {
		synchronized (AbstractDriverTestNG.class) {
			// will run only once per test run
			// initializes the test report html page
			setupReportPage();

			// will create parent once per class
			// initializes the test instance
			String className = TestObject.getTestInfo().getClassName();
			if (!classList.containsKey(className)) {
				String testParent = className.substring(className.lastIndexOf('.') + 1).trim();
				testParent = parseTestName(testParent);
				ExtentTest feature = extent.createTest(testParent);
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

	public static WebDriver createDriver(DriverObject driverObject) throws Exception {
		int retry = 3;
		WebDriver driver = null;
		do {
			try {
				retry--;
				//Helper.killWindowsProcess("node.exe");
				driver = new WebDriverSetup().getWebDriverByType(driverObject);

			} catch (Exception e) {
				// TestLog.ConsoleLog("Driver recreated" +
				// this.getClass().getName());
				// TestLog.ConsoleLog("Error: " + e.getMessage());
				Helper.wait.waitForSeconds(3);
				if (retry == 0) {
					throw e;
				}

			}

		} while (driver == null && retry > 0);

		AssertJUnit.assertTrue("driver was not created", driver != null);

		return driver;
	}

	public static void getURL(String url) {
		if (!url.isEmpty()) {
			TestLog.logPass("I am the site '" + url + "'");
			getWebDriver().get(url);
		}
	}

	public static WebDriver getWebDriver() {
		return webDriver.get();
	}

	@AfterMethod
	public void shutdown() {
		if (!CrossPlatformProperties.isSingleSignIn())
			DriverObject.quitTestDrivers();
		letRetryKnowAboutReports();
	}

	private void letRetryKnowAboutReports() {

		retry.setExtendReport(TestObject.getTestInfo().testScenerio, step.get());
	}
}