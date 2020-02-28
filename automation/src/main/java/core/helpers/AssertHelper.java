package core.helpers;

import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import core.support.logger.TestLog;
import core.support.objects.TestObject;

public class AssertHelper {

	/**
	 * assert true
	 * 
	 * @param message
	 * @param value
	 */
	protected static void assertTrue(String message, boolean value) {
		if(!value) TestLog.ConsoleLog("Assertion failed: " + message);
		Assert.assertTrue(value, TestObject.getTestId() + ": " +  message);
	}

	protected static void assertFalse(String message) {
		Assert.assertTrue(false, TestObject.getTestId() + ": " +  message);
	}
	
	protected static void assertFalse(String message, boolean value) {
		Assert.assertTrue(!value, TestObject.getTestId() + ": " +  message);
	}

	protected static void assertEquals(String expected, String actual) {
		TestLog.logPass("validating if expected: " + expected + " equals to actual: " + actual);
		Assert.assertEquals(actual, expected);
	}
	
	protected static void assertEquals(boolean expected, boolean actual) {
		TestLog.logPass("validating if expected: " + expected + " equals to actual: " + actual);
		Assert.assertEquals(actual, expected);
	}

	protected static void assertEquals(int expected, int actual) {
		TestLog.logPass("validating if expected: " + expected + " equals to actual: " + actual);
		Assert.assertEquals(actual, expected);
	}
	
	protected static void softAssertTrue(String message, boolean value) {
		SoftAssert softAssertion= new SoftAssert();
		softAssertion.assertTrue(value, message);
		TestLog.ConsoleLogWarn("soft assert failed: " + message);
	}
	
	protected static void softAssertEqual(String expected, String actual) {
		SoftAssert softAssertion= new SoftAssert();
		softAssertion.assertEquals(actual, expected);
		TestLog.ConsoleLogWarn("soft assert failed: expected: " + expected + " but actual was: " + actual);
	}
	
	protected static void softAssertEqual(int expected, int actual) {
		SoftAssert softAssertion= new SoftAssert();
		softAssertion.assertEquals(actual, expected);
		TestLog.ConsoleLogWarn("soft assert failed: expected: " + expected + " but actual was: " + actual);
	}


	/**
	 * assert actual contains expected
	 * 
	 * @param actual
	 * @param expected
	 */
	protected static void assertContains(String actual, String expected) {
		TestLog.logPass("validating if expected: " + expected + " contains actual: " + actual);
		assertTrue("actual: " + actual + " does not contain expected: " + expected, actual.contains(expected));
	}
}