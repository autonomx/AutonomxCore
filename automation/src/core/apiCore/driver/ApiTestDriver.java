package core.apiCore.driver;

import org.apache.commons.lang3.StringUtils;

import core.apiCore.helpers.CsvReader;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import core.support.objects.TestObject.testType;
import core.uiCore.drivers.AbstractDriverTestNG;

public class ApiTestDriver {
	// public static ThreadLocal<Logger> log = new ThreadLocal<Logger>();

	public static void setTestId(ServiceObject serviceObject) {
		String csvFileName = getTestClass(serviceObject);

		TestObject.setTestName(serviceObject.getTestCaseID());
		TestObject.setTestId(csvFileName + "-" + serviceObject.getTestCaseID());
	}

	/**
	 * gets the test class based on the csv file name eg. TestCases_company has
	 * testId = company
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static String getTestClass(ServiceObject serviceObject) {
		String testClass = serviceObject.getTcName();
		return  getTestClass(testClass);
	}
	
	/**
	 * get test class name based on csv file name
	 * @param csvFilename
	 * @return
	 */
	public static String getTestClass(String csvFilename) {
		if(StringUtils.isBlank(csvFilename)) return csvFilename;
		
		String testClass = csvFilename.split("\\.")[0];
		testClass = testClass.replace(CsvReader.SERVICE_CSV_FILE_PREFIX, "");
		return testClass;
	}

	/**
	 * initialized testInfo with testId as key runs before test adds current driver
	 * info to test object note: each row in csv is treated as separate test, with
	 * separate test object test logs And config are shared by test class object,
	 * which is the csv file name
	 * 
	 * @param driverObject
	 */
	public void initTest(ServiceObject serviceObject) {
		String APP = "ServiceManager";

		
		setTestId(serviceObject);
		String testId = TestObject.currentTestId.get();
		TestLog.removeLogUtilHandler();

		// initialize parent class for each csv file. once per csv
		String classname = initializeParentClass(serviceObject);

		// initialize once per test in csv file
		TestObject.initializeTest(testId);

		// pass the class config And logs to new test. parameters are passed from one
		// test to another this way
		TestObject.getTestInfo().config = TestObject.getTestInfo(classname).config;
		TestObject.getTestInfo().testLog = TestObject.getTestInfo(classname).testLog;

		TestObject.getTestInfo().type = testType.service;
		TestObject.getTestInfo().app = APP;
		TestObject.getTestInfo().testCsvFileName = serviceObject.getTcName();

		TestObject.getTestInfo().className = getTestClass(serviceObject);

		TestObject.getTestInfo().testName = serviceObject.getTestCaseID();
		TestObject.getTestInfo().currentTestIndex = Integer.valueOf(serviceObject.getTcIndex());

		TestObject.getTestInfo().testCountInCsvFile = Integer.valueOf(serviceObject.getTcCount());

	}
	
	/**
	 * initialize parent class for each csv file. once per csv
	 * @param serviceObject
	 * @return
	 */
	private String initializeParentClass(ServiceObject serviceObject) {
		// initialize class object for service test. test config is passed on to each test
		// in the test class
		// all api tests in the same class share the same config. each csv file is one
		// class based on csv file name. 
		// this is the test object for the csv file
		String classname = getTestClass(serviceObject);
		classname = serviceObject.getParentClass() + "-" + classname + TestObject.BEFORE_METHOD_PREFIX;
		TestObject.initializeTest(classname);
		
		// set random string and time per test
		if(TestObject.getTestInfo(classname).config.get(TestObject.RANDOM_STRING).toString().isEmpty()) {
			TestObject.getTestInfo(classname).config.put(TestObject.RANDOM_STRING, Helper.generateRandomString(30));
			TestObject.getTestInfo(classname).config.put(TestObject.START_TIME_STRING, Helper.date.getTimestampMiliseconds());
		}
		return classname;
	}

	/**
	 * returns true if all tests in current csv file are completed
	 * @return
	 */
	public static boolean isCsvTestComplete() {
		if (TestObject.getTestInfo().currentTestIndex == TestObject.getTestInfo().testCountInCsvFile - 1) {
			TestObject.getTestInfo().isTestComplete = true;
			return true;
		}
		return false;
	}
	
	/**
	 * returns true if all tests in current csv file are completed
	 * @return
	 */
	public static boolean isCsvTestComplete(ServiceObject service) {
		if (Integer.valueOf(service.getTcIndex()) == Integer.valueOf(service.getTcCount()) -1) {
			return true;
		}
		return false;
	}
	
	/**
	 * returns true if tests in csv file are starting
	 * @return
	 */
	public static boolean isCsvTestStarted() {
		if (TestObject.getTestInfo().currentTestIndex == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * returns true if tests in csv file are starting
	 * @return
	 */
	public static boolean isCsvTestStarted(int index) {
		if (index == 0) {
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
		return testData[testData.length - 2].equals(TestObject.testType.service.name());
	}
	
	/**
	 * set service test name based on test name specified in test data
	 * @param testData
	 */
	public static void setServiceTestName(Object[] testData) {
		if (ApiTestDriver.isRunningServiceTest(testData)) {
			ServiceObject apiObject = CsvReader.mapToServiceObject(testData);
			String testClass = ApiTestDriver.getTestClass(apiObject.getTcName());
			AbstractDriverTestNG.testName.set(testClass + "-" + apiObject.getTestCaseID());
		}
	}
}