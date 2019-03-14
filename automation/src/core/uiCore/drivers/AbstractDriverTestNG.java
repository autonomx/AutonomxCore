package core.uiCore.drivers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
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

		ExtentManager.reportSetup();
	}

	public synchronized static WebDriver setupWebDriver(DriverObject driverObject) throws Exception {
		initTest(driverObject);
		ExtentManager.reportSetup();

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

	public static WebDriver createDriver(DriverObject driverObject) throws Exception {
		int retry = 3;
		WebDriver driver = null;
		do {
			try {
				retry--;
				//Helper.killWindowsProcess("node.exe");
				driver = new WebDriverSetup().getWebDriverByType(driverObject);

			} catch (Exception e) {
				if(retry > 0)
					TestLog.ConsoleLog("driver failed to start. retrying " + retry + " more time(s) ...");
				
				Helper.wait.waitForSeconds(3);
				if (retry == 0) {
					// print out android help
					if(driverObject.driverType.equals(DriverType.ANDROID_DRIVER))
						printAndroidHelp(e);				
					throw e;
				}

			}

		} while (driver == null && retry > 0);

		TestLog.ConsoleLog("driver created successfully");
		Helper.assertTrue("driver was not created", driver != null);
		return driver;
	}
	
	public static void printAndroidHelp(Exception e) {
		String androidError = "It is impossible to create a new session";
		String androidSolution = "*******************************************************************\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"*******************************************************************\r\n" + 
				"\r\n" + 
				"1. this could be an environment issue. Try the following solutions:\r\n" + 
				"    1. Turn on debugging in properties at resource folder for more info:\r\n" + 
				"        1. appiumLogging = true\r\n" + 
				"    2. set android home environment in properties\r\n" + 
				"        1. androidHome = \"/Users/username/Library/Android/sdk\"\r\n" + 
				"    3. please download appium doctor https://github.com/appium/appium-doctor\r\n" + 
				"        1. download with command: npm install appium-doctor -g\r\n" + 
				"        2. Run: appium-doctor -android\r\n" + 
				"        3. Ensure the environment is setup properly\r\n" + 
				"        4. Restart eclipse\r\n" + 
				"    4. is appium terminal installation correct?\r\n" + 
				"        1. command line: appium\r\n" + 
				"            1. Does it start. If not install: “npm install -g appium”  or “sudo npm install -g appium --unsafe-perm=true --allow-root”\r\n" + 
				"            2. Run against appium terminal\r\n" + 
				"                1. In properties set:\r\n" + 
				"                    1. useExternalAppiumServer = true\r\n" + 
				"                    2. appiumExternalPort = 4723\r\n" + 
				"                2. run test and see if it passes\r\n" + 
				"    5. is simulator working correctly: Run\r\n" + 
				"        1. adb uninstall io.appium.uiautomator2.server\r\n" + 
				"        2. adb uninstall io.appium.uiautomator2.server.test \r\n" + 
				"    6. Try running against appium desktop server\r\n" + 
				"        1. Download and run appium desktop\r\n" + 
				"        2. Start the server\r\n" + 
				"        3. In properties at resource folder, set values\r\n" + 
				"            1. useExternalAppiumServer = true\r\n" + 
				"            2. appiumExternalPort = 4723\r\n" + 
				"            3. \r\n" + 
				"*******************************************************************\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"*******************************************************************";
		if(e.getMessage().contains(androidError)) {
			System.out.println(androidSolution);
		}
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