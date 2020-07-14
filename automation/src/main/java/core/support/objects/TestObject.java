package core.support.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.runner.Description;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import core.apiCore.helpers.CsvReader;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.LogObject;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.drivers.AbstractDriverTestNG;

/**
 * testInfo
 * 
 * columns: testID testName isFirstRun testClass currentDriver ... 1 testA true
 * classA A DriverA 2 testB true classA B DriverB
 * 
 * @author CAEHMAT
 *
 */

public class TestObject {

	// serviceTest : api tests read from csv files through apiTestRunner
	// uiTest : non api tests
	public static enum testType {
		service, uiTest
	}

	public static enum testState {
		parent, beforeSuite, suite, testClass, testMethod, apiTestMethod, defaultState
	}

	// proxy info
	public final static String PROXY_ENABLED = "proxy.enabled";
	public final static String PROXY_HOST = "proxy.host";
	public final static String PROXY_PORT = "proxy.port";
	public final static String PROXY_USER = "proxy.username";
	public final static String PROXY_PASS = "proxy.password";
	public final static String PROXY_PROTOCOL = "proxy.maven.protocol";

	public static String BEFORE_SUITE_PREFIX = "-Beforesuite";
	public static String AFTER_SUITE_PREFIX = "-Aftersuite";
	public static String BEFORE_CLASS_PREFIX = "-Beforeclass";
	public static String AFTER_CLASS_PREFIX = "-Afterclass";
	public static String BEFORE_METHOD_PREFIX = "-Beforemethod";
	public static String BEFORE_TEST_FILE_PREFIX = "-BeforeTestFile";
	public static String AFTER_TEST_FILE_PREFIX = "-AfterTestFile";
	public static String PARENT_PREFIX = "-Parent"; // parent object of csv file

	public static String DATAPROVIDER_TEST_SUFFIX = "-test";

	public static final String DEFAULT_TEST = "Autonomx-default";
	public static final String DEFAULT_TEST_THREAD_PREFIX = "Runner";

	public static final String RANDOM_STRING = "_randomString_";
	public static final String START_TIME_STRING = "_startTimeString_";
	public static final String UUID_STATIC_STRING = "_uuidStaticString_";


	public static final String DEFAULT_APP = "auto";
	public static String SUITE_NAME = StringUtils.EMPTY; // suite name is global to all tests in the run
	public static String APP_IDENTIFIER = StringUtils.EMPTY; // app name associated with test run. If suite is default,
																// use app identifier

	public static final String TEST_APP_API = "api";

	public List<WebDriver> webDriverList = new ArrayList<WebDriver>();
	public String app = StringUtils.EMPTY;
	public testType type;
	public String testId = StringUtils.EMPTY;
	public String testName = StringUtils.EMPTY;
	public String className = StringUtils.EMPTY;
	public String deviceName = StringUtils.EMPTY; // device name for mobile devices

	public String testFileClassName; // same as class name except for api tests

	public DriverObject currentDriver;
	public Boolean isFirstRun = false; // is the test running from beginning
	public Boolean isForcedRestart = false; // incase of test failed or other situations

	public LoginObject login = new LoginObject();
	public int runCount = 0;
	public Boolean isTestPass = false;
	public Boolean isTestComplete = false;
	public Logger log;

	// api test info
	public int currentTestIndex = 0; // index for tests in csv files
	public int testCountInCsvFile = 0; // test count in csv file
	public String testCsvFileName = StringUtils.EMPTY;
	public ServiceObject serviceObject = new ServiceObject();
	public ServiceObject activeServiceObject = new ServiceObject(); // current service object, can be before/after test/suite 

	public Description description;
	public Throwable caughtThrowable = null;
	public ArrayList<String> failTrace = new ArrayList<String>();

	// extent report info
	public ExtentTest testFeature;
	public ExtentTest testScenerio; // current test scenario
	public List<ExtentTest> testSteps = new ArrayList<ExtentTest>(); // current test scenario
	public List<String> testSubSteps = new ArrayList<String>(); // current test scenario
	public List<String> missingConfigVars = new ArrayList<String>(); // keep track of missing config variables

	// screen recorder for web
	public ScreenRecorder screenRecorder = null;

	public List<LogObject> testLog = new ArrayList<LogObject>();

	public Map<String, String> languageMap = new ConcurrentHashMap<String, String>();
	public Map<String, ServiceObject> apiMap = new ConcurrentHashMap<String, ServiceObject>();// api keywords
	public Map<String, Object> config = new ConcurrentHashMap<String, Object>();
	public Multimap<String, String> configKeys = ArrayListMultimap.create();
	public List<TestObject> testObjects = new ArrayList<TestObject>(); // parent test objects keeps track of child test objects

	public static ThreadLocal<String> currentTestName = new ThreadLocal<String>();
	public static ThreadLocal<String> currentTestId = new ThreadLocal<String>(); // key for testObject

	public String language;

	// key: testId
	public static Map<String, TestObject> testInfo = new ConcurrentHashMap<String, TestObject>();

	public TestObject withWebDriver(WebDriver webdriver) {
		this.webDriverList.add(webdriver);
		return this;
	}

	/**
	 * once per test, initialize test object maps driver with the test associates
	 * current driver with test
	 */
	public static void initializeTest(String testId) {
		DriverObject driver = new DriverObject().withApp(TEST_APP_API);
		initializeTest(driver, testId);
	}

	/**
	 * once per test, initialize test object maps driver with the test associates
	 * current driver with test
	 */
	public static void initializeTest(DriverObject driver, String testId) {

		if (isBeforeTest(testId)) { // testobject is initiated only once
			TestObject test = new TestObject();

			// inherits test object values from parent. eg.beforeClass from test suite. test
			// method from before class
			test = inheritParent(driver, testId);

			test.withTestId(testId).withTestName(test.getTestName());
			TestObject.testInfo.put(testId, test);

			// initialize logging
			TestObject.setLogging();

			// loads all property values into config map
			// if config from inherited layer is empty ( empty for default (autonomx), and
			// before suite )
			if (test.config.isEmpty())
				Config.loadConfig(testId);

			// set random string and time per test
			String timeInstance = Helper.date.getTimeInstance();
			Config.putValue(RANDOM_STRING, Helper.generateRandomString(30), false);
			Config.putValue(START_TIME_STRING, timeInstance, false);
			Config.putValue(UUID_STATIC_STRING, Helper.generateUUID(), false);

			// loads all the keywords for api references
			CsvReader.getAllKeywords();

			TestObject.getTestInfo().type = testType.uiTest;
		}
	}

	/**
	 * Inheritance structure for test object
	 * 
	 * before suite -> before class -> test method before suite -> before class ->
	 * after class before suite -> after suite
	 * 
	 * Service test hierarchy: before suite -> before class -> csv file name object
	 * -> csv tests before suite -> before class -> after class before suite ->
	 * after suite note: before method inheritance not supported
	 * 
	 * @return
	 */
	public static TestObject inheritParent(DriverObject driver, String testId) {
		TestObject test = new TestObject();
		// add config object from previous state to new test object
		Map<String, Object> configValue = getTestObjectInheritence(driver, testId).config;
		test.config.putAll(configValue);

		return test;
	}

	/**
	 * Inheritance structure for test object
	 * 
	 * before suite -> before class -> test method before suite -> before class ->
	 * after class before suite -> after suite
	 * 
	 * @return
	 */
	public static TestObject getTestObjectInheritence(DriverObject driver, String testId) {

		// gets test state of test object: suite, testClass, testMethod
		testState testObjectState = getTestState(testId);

		// before suite does not inherit
		if (testObjectState.equals(testState.beforeSuite))
			return new TestObject();

		// name of the test to be pass inheritance
		String[] testValues = testId.split("-");
		String testName = testValues[0];

		testId = testId.toLowerCase();

		String testClassname = AbstractDriverTestNG.testClassname.get();

		// service level tests are handled in ApiTestDriver
		// except for setting inheritance of test object with csv file name from before
		// class
		// eg. ApiRunnerTest-UserValidation-beforemethod inherits from
		// ApiRunnerTest-Beforeclass
		// UserValidation: is csv file name
		if (driver.app.equals(TEST_APP_API) && !testId.contains(BEFORE_METHOD_PREFIX)) {
			return new TestObject();
		}

		// if default test, return itself. Not gaining from other test objects
		if (testId.equals(TestObject.DEFAULT_TEST.toLowerCase()))
			return new TestObject();

		// if before class, inherit test object from before suite
		if (testId.contains(BEFORE_CLASS_PREFIX.toLowerCase()))
			return TestObject.getTestInfo(TestObject.SUITE_NAME + BEFORE_SUITE_PREFIX);

		// if before test inherit test object from before class
		if (testObjectState.equals(testState.testMethod))
			return TestObject.getTestInfo(testName + BEFORE_CLASS_PREFIX);

		// if parent, inherit test object from before class
		if (testObjectState.equals(testState.parent))
			return TestObject.getTestInfo(testClassname + BEFORE_CLASS_PREFIX);

		// if after class, inherit test object from before class
		if (testId.contains(AFTER_CLASS_PREFIX.toLowerCase()))
			return TestObject.getTestInfo(testName + BEFORE_CLASS_PREFIX);

		// if after suite, inherit test object from before suite
		if (testId.contains(AFTER_SUITE_PREFIX.toLowerCase()))
			return TestObject.getTestInfo(TestObject.SUITE_NAME + BEFORE_SUITE_PREFIX);

		return new TestObject();
	}

	/**
	 * returns true if webdriver has not started else returns true if isFirstRun is
	 * set to true
	 * 
	 * @return
	 */
	public static boolean isFirstRun() {
		if (AbstractDriver.getWebDriver() == null)
			return true;
		else {
			if (getTestInfo().isFirstRun)
				return true;
		}
		return false;
	}

	/**
	 * testInfo is a static list containing hashmap of test objects with test name
	 * as key if testObject is empty for testId, Then default test is initialized
	 * And used
	 * 
	 * @return
	 */
	public static TestObject getTestInfo() {
		String testId = getTestId();

		if (testInfo.get(testId) == null) {
			return getGlobalTestInfo();
		}
		return testInfo.get(testId);
	}

	/**
	 * gets default test object if not exist, create default test object
	 * 
	 * @return
	 */
	public static TestObject getGlobalTestInfo() {
		String testId = TestObject.getDefaultTestObjectId();

		if (testInfo.get(testId) == null) {
			setupDefaultDriver();
		}
		return testInfo.get(testId);
	}

	/**
	 * get parent test object parent id is unique for each csv test file in service
	 * tests user for inheritance of config and log files
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static TestObject getParentTestInfo(ServiceObject serviceObject) {
		String parent = serviceObject.getParent();

		if (testInfo.get(parent) == null) {
			Helper.assertFalse("parent id not found: " + parent);
		}
		return testInfo.get(parent);
	}

	public static void setupDefaultDriver() {

		DriverObject driver = new DriverObject().withDriverType(DriverType.API).withApp(TestObject.DEFAULT_TEST);

		// setup default driver
		new AbstractDriverTestNG().setupWebDriver(getDefaultTestObjectId(), driver);
	}

	/**
	 * updates default test app name to driver app name default test object tracks
	 * the test run app name used for report name
	 * 
	 * @param driver
	 */
	public static void updateAppName(DriverObject driver) {
		String defaultTest = TestObject.getDefaultTestObjectId();
		if (TestObject.getTestInfo(defaultTest).app.equals(DEFAULT_APP))
			TestObject.getTestInfo(defaultTest).withApp(driver.app);
	}

	/**
	 * get the state of the test object can be suite, testClass, testMethod
	 * 
	 * @param testName
	 * @return
	 */
	public static testState getTestState(String testName) {
		testName = testName.toLowerCase();

		if (testName.contains(PARENT_PREFIX.toLowerCase()))
			return testState.parent;

		if (testName.contains(BEFORE_SUITE_PREFIX.toLowerCase()))
			return testState.beforeSuite;

		if (testName.contains(AFTER_SUITE_PREFIX.toLowerCase()))
			return testState.suite;

		if (testName.contains(BEFORE_CLASS_PREFIX.toLowerCase()) || testName.contains(AFTER_CLASS_PREFIX.toLowerCase()))
			return testState.testClass;

		if (testName.equals(TestObject.DEFAULT_TEST.toLowerCase()))
			return testState.defaultState;

		else
			return testState.testMethod;
	}

	public static void setTestName(String testName) {
		TestObject.currentTestName.set(testName);
	}

	public static void setTestId(String testId) {
		TestObject.currentTestId.set(testId);
	}

	public static void setTestId(String testclass, String testname) {
		TestObject.currentTestId.set(testclass + "-" + testname);
	}

	public static String getTestId() {
		String testId = TestObject.currentTestId.get();

		// if testId = null, set to default test
		if (testId == null || testId.isEmpty())
			testId = TestObject.getDefaultTestObjectId();

		return testId;
	}

	/**
	 * return true if testId is registered
	 * 
	 * @param testId
	 * @return
	 */
	public static boolean isTestObjectSet(String testId) {
		if (testInfo.get(testId) == null)
			return false;
		return true;
	}

	public static boolean isTestObjectSet() {
		String testId = TestObject.currentTestId.get();
		if (testInfo.isEmpty())
			return false;
		if (testId == null || testInfo.get(testId) == null)
			return false;
		return true;
	}

	/**
	 * TODO: Remove is test runner returns true if test is running, not before
	 * class, or after class based on testid: classname - testname
	 * 
	 * @return
	 */
	public static boolean hasTestStarted() {
		String testId = getTestId();

		// indicates class - testname are set
		if (testId != null && testId.contains("-"))
			return true;

		if (testId == null || testInfo.get(testId) == null)
			return false;

		// applied to api test runner reading csv file
		if (TestObject.getTestInfo().testCountInCsvFile > 0)
			return true;
		return false;
	}

	public static void setLogging() {
		Logger log = Logger.getLogger("");
		TestObject.getTestInfo().log = log;
	}

	public static TestObject getTestInfo(String testId) {
		TestObject test = testInfo.get(testId);
		Helper.assertTrue("test id not found. testId: " + testId + " testInfo: " + testInfo.toString(), test != null);
		return test;
	}

	public static boolean isBeforeTest(String testId) {
		TestObject test = testInfo.get(testId);
		return (test == null);
	}

	public static boolean isValidTestId(String testId) {
		TestObject test = testInfo.get(testId);
		return (test != null);
	}

	/**
	 * returns the invocation count for the data provider test format
	 * class-testname-test1
	 * 
	 * @param testname
	 * @return
	 */
	public static int getTestInvocationCount(String testname) {
		String tempTestname = testname;
		int invocationCount = 0;

		// check next invocation count
		do {
			invocationCount++;
			tempTestname = testname + DATAPROVIDER_TEST_SUFFIX + invocationCount;
		} while (isTestObjectSet(tempTestname));

		// set invocation count to the previous value where it is set
		invocationCount--;
		return invocationCount;
	}

	/**
	 * gets api object from api keywords each keyword is associated with a api call
	 * use api keywords csv files for definitions
	 * 
	 * @param key
	 * @return
	 */
	public static ServiceObject getApiDef(String key) {
		CsvReader.getAllKeywords();
		return TestObject.getTestInfo().apiMap.get(key);
	}

	public TestObject withIsFirstRun(Boolean isFirstRun) {
		this.isFirstRun = isFirstRun;
		return this;
	}

	public TestObject withIsForcedRestart(Boolean isForcedRestart) {
		this.isForcedRestart = isForcedRestart;
		return this;
	}

	public TestObject withCurrentDriver(DriverObject currentDriver) {
		this.currentDriver = currentDriver;
		return this;
	}

	public TestObject withTestId(String testId) {
		this.testId = testId;
		this.className = testId.contains("-") ? testId.split("-")[0] : testId;
		return this;
	}

	public TestObject withTestName(String testName) {
		this.testName = testName;
		return this;
	}

	public TestObject withClassName(String className) {
		this.className = className;
		return this;
	}

	public TestObject withIsTestComplete(boolean isTestComplete) {
		this.isTestComplete = isTestComplete;
		return this;
	}

	public TestObject withTestFileClassName(String testFileClassName) {
		this.testFileClassName = testFileClassName;
		return this;
	}

	public TestObject withWebDriverList(List<WebDriver> webDriverList) {
		this.webDriverList = webDriverList;
		return this;
	}

	/**
	 * * testname is grabbed from test id test id is in format of "class - testname"
	 * if is before class And no testname exists, empty string is returned
	 * 
	 * @return
	 */
	public String getTestName() {
		String testName = testId.substring(testId.indexOf("-") + 1);
		// String testName = testId.contains("-") ? testId.split("-")[1].trim() :
		// testId;

		return testName;
	}

	public String getClassName() {
		String className = testId.split("-")[0];
		return className;
	}

	public TestObject withRunCount(int rerunCount) {
		this.runCount = rerunCount;
		return this;
	}

	public TestObject withLog(Logger log) {
		this.log = log;
		return this;
	}

	public TestObject incremenetRunCount() {
		this.runCount++;
		return this;
	}

	public TestObject withIsTestPass(Boolean isTestPass) {
		this.isTestPass = isTestPass;
		return this;
	}

	public TestObject withDescription(Description description) {
		this.description = description;
		return this;
	}

	public TestObject withCaughtThrowable(Throwable caughtThrowable) {
		this.caughtThrowable = caughtThrowable;
		return this;
	}

	public TestObject withFailTrace(ArrayList<String> failTrace) {
		this.failTrace = failTrace;
		return this;
	}

	public TestObject withTestScenario(ExtentTest testScenerio) {
		this.testScenerio = testScenerio;
		return this;
	}

	public TestObject withApp(String app) {
		this.app = app;
		return this;
	}

	public static String getDefaultTestObjectId() {
		return TestObject.DEFAULT_TEST;
	}
}