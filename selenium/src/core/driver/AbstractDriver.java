package core.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.Scenario;

import core.driver.DriverObject.WebDriverType;
import core.helpers.WaitHelper;
import core.logger.ExtentManager;
import core.logger.PropertiesReader;
import core.logger.TestLog;
import core.rules.RetryTest;
import junit.framework.Assert;

@SuppressWarnings("deprecation")
public class AbstractDriver {

	public static ExtentReports extent;

	public static ThreadLocal<ExtentTest> test = new ThreadLocal<ExtentTest>();
	public static ThreadLocal<ExtentTest> step = new ThreadLocal<ExtentTest>();
	private static boolean isBeforeTestRun = true;

	protected static Map<String, ExtentTest> testList = new HashMap<String, ExtentTest>();

	private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<WebDriver>();
	public static ThreadLocal<Logger> log = new ThreadLocal<Logger>();
	
	/**
	 * global timeout in seconds
	 */
	public static final int TIMEOUT_SECONDS = PropertiesReader.getGlobalTimeout();

	
	@Rule
	public RetryTest retry = new RetryTest(RetryTest.RETRYCOUNTER);

	//TODO set this to be configurable in properties
	@Rule 
	public Timeout globalTimeout = new Timeout(6000 * 60 * 1000);

	@Rule
	public TestName testName = new TestName();

	public AbstractDriver() {
	}

	public void setupWebDriver(DriverObject driverObject) throws Exception {
		log.set(Logger.getLogger(getClass() + "-" + testName.getMethodName()));
		TestLog.removeLogUtilHandler();
		
		reportSetup();
		
		// setup web driver if the test is not api
		if(driverObject.driverType != null && driverObject.driverType.equals(WebDriverType.API))
		   return;
		
		setWebdriver(createDriver(driverObject));
		getURL(driverObject.initialURL);
		
	}

	private void setWebdriver(WebDriver webDriver) {
		AbstractDriver.webDriver.set(webDriver);
	}

	@BeforeClass
	public static void beforeClass() {

	}

	public static void setupReportPage() {
		// will run only once per test run
		// initializes the test report html page
		if (isBeforeTestRun) {
			extent = ExtentManager.createInstance("extent.html");
			isBeforeTestRun = false;
		}
	}

	public void reportSetup() {
		synchronized (AbstractDriver.class) {
			// will run only once per test run
			// initializes the test report html page
			setupReportPage();

			// will create parent once per class
			// initializes the test instance
			String className = getClassName();
			if (!testList.containsKey(className)) {
				String testParent = className.substring(className.lastIndexOf('.') + 1).trim();
				testParent = parseTestName(testParent);
				ExtentTest feature = extent.createTest(testParent);
				testList.put(className, feature);
			}

			// will run once every test
			// initializes test report
			if (retry.getCurrentTestRun() == 1) {
				String testChild = testName.getMethodName();
				testChild = parseTestName(testChild);
				ExtentTest scenario = testList.get(className).createNode(Scenario.class, testChild);
				test.set(scenario);
			}
		}
		if(retry.getTest() !=null)
			test.set(retry.getTest());
		
		TestLog.Given("Test setup has been successful");
	}

	public static String parseTestName(String value) {
		String formatted = "";
		value = value.replace("_", " ");

		for (String w : value.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
			formatted = formatted + " " + w.toLowerCase();
		}
		return formatted;
	}

	public String getClassName() {
		return this.getClass().getName();
	}

	public static WebDriver createDriver(DriverObject driverObject) throws Exception {
		int retry = 3;
		WebDriver driver = null;
		do {
			try {
				retry--;

				driver = WebDriverSetup.getWebDriverByType(driverObject);

				driver.manage().timeouts().implicitlyWait(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				// setting timeout causes slow down on mobile
				// driver.manage().timeouts().pageLoadTimeout(AppPage.TIMEOUT_SECONDS,
				// TimeUnit.SECONDS);
				// driver.manage().timeouts().setScriptTimeout(AppPage.TIMEOUT_SECONDS,
				// TimeUnit.SECONDS);

			} catch (Exception e) {
				// System.out.println("Driver recreated" +
				// this.getClass().getName());
				// System.out.println("Error: " + e.getMessage());
				WaitHelper.waitForSeconds(3);
				if (retry == 0) {
					throw e;
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
		return webDriver.get();
	}

	@After
	public void shutdown() {
		letRetryKnowAboutReports();
	}

	private void letRetryKnowAboutReports() {
		retry.setExtendReport(test.get(), step.get(), extent);
		retry.setLogger(log.get());
		retry.setWebDriver(getWebDriver());
	}
}