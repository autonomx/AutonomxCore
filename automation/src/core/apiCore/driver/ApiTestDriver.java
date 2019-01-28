package core.apiCore.driver;

import core.apiCore.helpers.csvReader;
import core.support.logger.TestLog;
import core.support.objects.ApiObject;
import core.support.objects.TestObject;
import core.support.objects.TestObject.testType;

public class ApiTestDriver {
	// public static ThreadLocal<Logger> log = new ThreadLocal<Logger>();

	public static void setTestId(ApiObject apiObject) {
		String csvFileName = getTestClass(apiObject);

		TestObject.setTestName(apiObject.TestCaseID);
		TestObject.setTestId(csvFileName + "-" + apiObject.TestCaseID);

		// log.set(Logger.getLogger(""));
	}

	/**
	 * gets the test class based on the csv file name eg. TestCases_company has
	 * testId = company
	 * 
	 * @param apiObject
	 * @return
	 */
	public static String getTestClass(ApiObject apiObject) {
		String testClass = apiObject.tcName;
		testClass = testClass.split("\\.")[0];
		testClass = testClass.replace("TestCases_", "");
		return testClass;
	}

	/**
	 * initialized testInfo with testId as key runs before test adds current driver
	 * info to test object note: each row in csv is treated as separte test, with
	 * separte test object test logs and config are shared by test class object,
	 * which is the csv file name
	 * 
	 * @param driverObject
	 */
	public void initTest(ApiObject apiObject) {
		String APP = "apiRunner";

		setTestId(apiObject);
		String testId = TestObject.currentTestId.get();
		TestLog.removeLogUtilHandler();

		// initialize class object for api test. test config is passed on to each test
		// in the test class
		// all api tests in the same class share the same config. each csv file is one
		// class based on csv file name
		String classname = getTestClass(apiObject);
		TestObject.initializeTest(classname);

		// initialize once per test
		TestObject.initializeTest(testId);

		// pass the class config and logs to new test. parameters are passed from one
		// test to another this way
		TestObject.getTestInfo().config = TestObject.getTestInfo(classname).config;
		TestObject.getTestInfo().testLog = TestObject.getTestInfo(classname).testLog;

		TestObject.getTestInfo().type = testType.apiTest;
		TestObject.getTestInfo().app = APP;
		TestObject.getTestInfo().testCsvFileName = apiObject.tcName;

		TestObject.getTestInfo().className = getTestClass(apiObject);

		TestObject.getTestInfo().testName = apiObject.TestCaseID;
		TestObject.getTestInfo().currentTestIndex = Integer.valueOf(apiObject.tcIndex);

		// initialize per test run
		if (TestObject.getTestInfo().testCountInCsvFile == 0)
			TestObject.getTestInfo().testCountInCsvFile = csvReader.getCsvTestListForTestRunner(apiObject.tcName)
					.size();
	}

	public static boolean isTestComplete() {
		if (TestObject.getTestInfo().currentTestIndex == csvReader.getCsvTestcount()) {
			TestObject.getTestInfo().isTestComplete = true;
			return true;
		}
		return false;
	}

	public String getClassName() {
		String className = getClass().toString().substring(getClass().toString().lastIndexOf(".") + 1);
		return className;
	}
}