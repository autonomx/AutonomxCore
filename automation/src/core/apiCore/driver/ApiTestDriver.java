package core.apiCore.driver;

import core.apiCore.helpers.CsvReader;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import core.support.objects.TestObject.testType;
import core.uiCore.drivers.AbstractDriverTestNG;

public class ApiTestDriver {
	// public static ThreadLocal<Logger> log = new ThreadLocal<Logger>();

	public static void setTestId(ServiceObject apiObject) {
		String csvFileName = getTestClass(apiObject);

		TestObject.setTestName(apiObject.getTestCaseID());
		TestObject.setTestId(csvFileName + "-" + apiObject.getTestCaseID());
	}

	/**
	 * gets the test class based on the csv file name eg. TestCases_company has
	 * testId = company
	 * 
	 * @param apiObject
	 * @return
	 */
	public static String getTestClass(ServiceObject apiObject) {
		String testClass = apiObject.getTcName();
		return  getTestClass(testClass);
	}
	
	/**
	 * get test class name based on csv file name
	 * @param csvFilename
	 * @return
	 */
	public static String getTestClass(String csvFilename) {
		String testClass = csvFilename.split("\\.")[0];
		testClass = testClass.replace(CsvReader.SERVICE_CSV_FILE_PREFIX, "");
		return testClass;
	}

	/**
	 * initialized testInfo with testId as key runs before test adds current driver
	 * info to test object note: each row in csv is treated as separte test, with
	 * separte test object test logs And config are shared by test class object,
	 * which is the csv file name
	 * 
	 * @param driverObject
	 */
	public void initTest(ServiceObject apiObject) {
		String APP = "ServiceManager";

		
		setTestId(apiObject);
		String testId = TestObject.currentTestId.get();
		TestLog.removeLogUtilHandler();

		// initialize class object for api test. test config is passed on to each test
		// in the test class
		// all api tests in the same class share the same config. each csv file is one
		// class based on csv file name
		String classname = getTestClass(apiObject);
		classname = apiObject.getParentClass() + "-" + classname + TestObject.BEFORE_METHOD_PREFIX;
		TestObject.initializeTest(classname);

		// initialize once per test
		TestObject.initializeTest(testId);

		// pass the class config And logs to new test. parameters are passed from one
		// test to another this way
		TestObject.getTestInfo().config = TestObject.getTestInfo(classname).config;
		TestObject.getTestInfo().testLog = TestObject.getTestInfo(classname).testLog;

		TestObject.getTestInfo().type = testType.service;
		TestObject.getTestInfo().app = APP;
		TestObject.getTestInfo().testCsvFileName = apiObject.getTcName();

		TestObject.getTestInfo().className = getTestClass(apiObject);

		TestObject.getTestInfo().testName = apiObject.getTestCaseID();
		TestObject.getTestInfo().currentTestIndex = Integer.valueOf(apiObject.getTcIndex());

		// initialize per test run
		if (TestObject.getTestInfo().testCountInCsvFile == 0)
			TestObject.getTestInfo().testCountInCsvFile = CsvReader.getCsvTestListForTestRunner(apiObject.getTcName())
					.size();

	}

	public static boolean isTestComplete() {
		if (TestObject.getTestInfo().currentTestIndex == CsvReader.getCsvTestcount()) {
			TestObject.getTestInfo().isTestComplete = true;
			return true;
		}
		return false;
	}

	public String getClassName() {
		String className = getClass().toString().substring(getClass().toString().lastIndexOf(".") + 1);
		return className;
	}
	
	/**
	 * is service test running
	 * @return
	 */
	public static boolean isRunningServiceTest() {
		return TestObject.getTestInfo().type.equals(TestObject.testType.service);
	}
	
	/* is service test running
	 * @return
	 */
	public static boolean isRunningServiceTest(Object[] testData) {
		if(testData.length != CsvReader.SERVICE_CSV_COLUMN_COUNT) return false;
		if(testData[testData.length - 1] == null) return false;
		return testData[testData.length -1].equals(TestObject.testType.service.name());
	}
	
	/**
	 * set service test name based on test name specified in test data
	 * @param testData
	 */
	public static void setServiceTestName(Object[] testData) {
		if (ApiTestDriver.isRunningServiceTest(testData)) {
			ServiceObject apiObject = CsvReader.mapToApiObject(testData);
			String testClass = ApiTestDriver.getTestClass(apiObject.getTcName());
			AbstractDriverTestNG.testName.set(testClass + "-" + apiObject.getTestCaseID());
		}
	}
}