package core.support.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.IRetryAnalyzer;
import org.testng.annotations.ITestAnnotation;

import core.apiCore.dataProvider;
import core.apiCore.helpers.csvReader;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;

@SuppressWarnings("rawtypes")
public class AnnotationTransformer implements IAnnotationTransformer {
	public static final String THREAD_COUNT = "global.parallel_test_count";
	public static final String API_TEST_RUNNER_PREFIX = "ApiRunner";

	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
		// initialize test object
		TestObject.initializeTest(new DriverObject(), TestObject.DEFAULT_TEST);
		IRetryAnalyzer retry = annotation.getRetryAnalyzer();
		if (retry == null) {
			annotation.setRetryAnalyzer(RetryTest.class);
		}

		// sets thread count and invocation count for api test runner
		setupApiRunner(annotation, testClass, testConstructor, testMethod);

		// fail test if invocation count is 0, meaning the test will not run
		if (annotation.getInvocationCount() == 0)
			Helper.assertFalse(
					"invocation count is 0. if this is a ApiRunner test, please add csv file to api test case folder");
	}

	/**
	 * sets thread count and invocation count for api test runner
	 * 
	 * @param annotation
	 * @param testClass
	 * @param testConstructor
	 * @param testMethod
	 */
	private void setupApiRunner(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
			Method testMethod) {

		// only run this method if test is a api runner test
		if (!testMethod.getName().endsWith(API_TEST_RUNNER_PREFIX))
			return;

		// set thread count for api test type
		setApiThreadCount(annotation, testClass, testConstructor, testMethod);

		int csvTestCount = csvReader.getCsvFileCount();

		// set test run count to 1, for 1 csv file to run
		if (!(Config.getValue(dataProvider.TEST_CASE_FILE).isEmpty())) {
			csvTestCount = 1;
		}
		// set test loop to the number of csv files in the folder
		// each loop, the next csv file will be executed as a test
		annotation.setInvocationCount(csvTestCount);

	}

	/**
	 * sets api thread count based on test name
	 * 
	 * @param annotation
	 * @param testClass
	 * @param testConstructor
	 * @param testMethod
	 */
	private void setApiThreadCount(ITestAnnotation annotation, Class testClass, Constructor testConstructor,
			Method testMethod) {

		String testName = testMethod.getName();

		// if parallel api test, get thread count from conf file
		// if non optimized or sequential, set thread count to 1
		if (testName.equals("parallelApiRunner")) {
			annotation.setThreadPoolSize(Integer.valueOf(Config.getValue(THREAD_COUNT)));
			dataProvider.TEST_DATA_PATH = Config.getValue(dataProvider.TEST_DATA_PARALLEL_PATH);
		} else if (testName.equals("NonOptimizedApiRunner")) {
			annotation.setThreadPoolSize(1);
			dataProvider.TEST_DATA_PATH = Config.getValue(dataProvider.TEST_DATA_NONE_OPTIMISED_PATH);

		} else if (testName.equals("SequentialApiRunner")) {
			annotation.setThreadPoolSize(1);
			dataProvider.TEST_DATA_PATH = Config.getValue(dataProvider.TEST_DATA_SEQUENTIAL_PATH);
		}
	}
}