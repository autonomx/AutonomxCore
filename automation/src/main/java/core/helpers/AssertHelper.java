package core.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import core.support.configReader.Config;
import core.support.logger.LogObject;
import core.support.logger.TestLog;
import core.support.objects.TestObject;

public class AssertHelper {

	/**
	 * assert true
	 * 
	 * @param message
	 * @param value
	 */
	public static void assertTrue(String message, boolean value) {

		try {
			Assert.assertTrue(value, TestObject.getTestId() + ": " + message);
		} catch (AssertionError e) {
			logStackTrace(e);
			message = getLogsAsString(message);
			Assert.assertTrue(value, TestObject.getTestId() + ": " + message);
		}
	}

	public static void assertFalse(String message) {	
		
		try {	
			Assert.assertTrue(false, TestObject.getTestId() + ": " + message);
		} catch (AssertionError e) {
			logStackTrace(e);
			message = getLogsAsString(message);
			Assert.assertTrue(false, TestObject.getTestId() + ": " + message);
		}
	}

	public static void assertFalse(String message, boolean value) {
		try {
			message = getLogsAsString(message);
			Assert.assertTrue(!value, TestObject.getTestId() + ": " + message);
		} catch (AssertionError e) {
			logStackTrace(e);
			message = getLogsAsString(message);
			Assert.assertTrue(!value, TestObject.getTestId() + ": " + message);
		}
	}

	public static void assertEquals(String expected, String actual) {
		TestLog.logPass("validating if expected: " + expected + " equals to actual: " + actual);
		if(expected == null)
			assertTrue("expected value: " + expected + " actual:" + actual, expected == actual);
		else
			assertTrue("expected value: " + expected + " actual:" + actual, expected.equals(actual));

	}

	public static void assertEquals(boolean expected, boolean actual) {
		assertTrue("expected value: " + expected + " actual:" + actual, java.util.Objects.equals(expected, actual));
	}

	public static void assertEquals(int expected, int actual) {
		assertTrue("expected value: " + expected + " actual:" + actual, expected == actual);

	}

	public static void softAssertTrue(String message, boolean value) {
		TestObject.getTestInfo().softAssert.assertTrue(value, message);
		if(!value) TestLog.ConsoleLogWarn("soft assert failed: " + message);
	}

	public static void softAssertEqual(String expected, String actual) {
		TestObject.getTestInfo().softAssert.assertEquals(actual, expected);
		if (expected != null && !expected.equals(actual))
			TestLog.ConsoleLogWarn("soft assert failed: expected: " + expected + " but actual was: " + actual);
		else if (expected == null && expected != actual)
			TestLog.ConsoleLogWarn("soft assert failed: expected: " + expected + " but actual was: " + actual);

	}

	public static void softAssertEqual(int expected, int actual) {
		TestObject.getTestInfo().softAssert.assertEquals(actual, expected);
		if(expected != actual)
			TestLog.ConsoleLogWarn("soft assert failed: expected: " + expected + " but actual was: " + actual);
	}
	
	public static SoftAssert softAssert() {
		return TestObject.getTestInfo().softAssert;
	}
	
	public static void softAssertAll() {
		TestObject.getTestInfo().softAssert.assertAll();
	}

	/**
	 * assert actual contains expected
	 * 
	 * @param actual
	 * @param expected
	 */
	public static void assertContains(String expected, String actual) {
		TestLog.logPass("validating if expected: " + expected + " contains actual: " + actual);
		assertTrue("actual: " + actual + " does not contain expected: " + expected, actual.contains(expected));
	}
	
	public static void logStackTrace(AssertionError e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();// stack trace as a string
		TestLog.ConsoleLog(exceptionAsString);	
	}
	
	public static void logStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = sw.toString();// stack trace as a string
		TestLog.ConsoleLog(exceptionAsString);	
	}
	
	
	/**
	 * added test steps with stack trace
	 * useful for CI test results for quick view of the test steps with errors
	 * @param message
	 * @return
	 */
	public static String getLogsAsString(String message) {
		boolean isLogWithStackTrace = Config.getBooleanValue("log.steps.with.stacktrace");
		
		if(!isLogWithStackTrace) return message;
		
		String testname = TestObject.getTestId();
		String logTrimmed = StringUtils.EMPTY;
		
		List<String> logValues = new ArrayList<String>();
		List<LogObject> logs = TestObject.getTestInfo().testLog;
		for (LogObject log : logs) {
			String[] logTrimmedArray = log.value.split(testname);
			if(logTrimmedArray.length > 1) {
				logTrimmed = logTrimmedArray[1];
				logTrimmed = logTrimmed.replaceFirst(Pattern.quote("-"), "");
				logValues.add(">" + logTrimmed);
			}else
				logValues.add(log.value);
		}
		String list = StringUtils.join(logValues, "\n");
		message = message + "\n"+ list;
		return message;
	}
}