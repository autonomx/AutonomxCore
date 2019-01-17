package core.helpers;

import org.junit.Assert;

import core.support.logger.TestLog;

public class AssertHelper {

	/**
	 * assert true
	 * 
	 * @param message
	 * @param value
	 */
	protected static void assertTrue(String message, boolean value) {
		Assert.assertTrue(message, value);
	}

	protected static void assertFalse(String message) {
		Assert.assertTrue(message, false);
	}

	protected static void assertEquals(String expected, String actual) {
		TestLog.logPass("validating if expected: " + expected + " equals to actual: " + actual);
		Assert.assertEquals(expected, actual);
	}

	protected static void assertEquals(int expected, int actual) {
		TestLog.logPass("validating if expected: " + expected + " equals to actual: " + actual);
		Assert.assertEquals(expected, actual);
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