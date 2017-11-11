package core.helpers;

import core.driver.AbstractDriver;
import core.logger.TestLog;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class ClickHelper {

	/**
	 * clicks target and waits for expected element to display retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndExpect(EnhancedBy target, EnhancedBy expected) {
		clickAndExpect(target, 0, expected);
	}

	/**
	 * clicks element based on index and waits for expected element to be displayed
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 */
	public static void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected) {

		EnhancedWebElement targetElement = Element.findElements(target);
	//	WaitHelper.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			targetElement.click(index);
			isExpectedFound = WaitHelper.waitForElementToLoad(expected, targetWaitTimeInSeconds);
		} while (!isExpectedFound && retry > 0);

		AssertHelper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
		TestLog.logPass("I click " + target.name);
	}

	/**
	 * clicks target and waits for expected element to show up also waits for
	 * spinner element to be removed from display
	 * 
	 * @param target
	 * @param expected
	 * @param spinner
	 */
	public static void clickAndExpect(EnhancedBy target, EnhancedBy expected, EnhancedBy spinner) {
		EnhancedWebElement targetElement = Element.findElements(target);
		WaitHelper.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			targetElement.click();
			WaitHelper.waitForElementToBeRemoved(spinner);
			isExpectedFound = WaitHelper.waitForElementToLoad(expected, targetWaitTimeInSeconds);
		} while (!isExpectedFound && retry > 0);

		AssertHelper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
		TestLog.logPass("I click " + target.name);
	}

	public static void clickAndNotExpect(EnhancedBy target, EnhancedBy expected) {
		clickAndNotExpect(target, 0, expected);
	}

	/**
	 * clicks target and waits for expected to not be displayed retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public static void clickAndNotExpect(EnhancedBy target, int index, EnhancedBy expected) {
		EnhancedWebElement targetElement = Element.findElements(target);
		WaitHelper.waitForElementToLoad(target);

		EnhancedWebElement expectedElement = null;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			targetElement.click(index);
			WaitHelper.waitForElementToBeRemoved(expected, targetWaitTimeInSeconds);
			expectedElement = Element.findElements(expected);
		} while (expectedElement.isExist() && retry > 0);

		AssertHelper.assertTrue("expected element found", !expectedElement.isExist());
		TestLog.logPass("I click " + target.name);
	}
}