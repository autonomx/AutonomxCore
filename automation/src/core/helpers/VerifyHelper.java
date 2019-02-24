package core.helpers;

import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class VerifyHelper {

	/**
	 * verifies if element(s) is (are) displayed
	 *
	 * @param by
	 */
	protected static void verifyElementIsDisplayed(EnhancedBy by) {
		Helper.wait.waitForElementToLoad(by, 3); // initially waits up to 3 seconds before scrolling attempt
		Helper.mobile.mobile_scrollToElement(by);
		TestLog.logPass("I verify '" + by.name + "' " + "is displayed");
		Helper.wait.waitForElementToLoad(by);
		EnhancedWebElement elements = Element.findElements(by);
		AssertHelper.assertTrue("element '" + by.name + "' is not displayed", elements.count() > 0);
	}

	/**
	 * returns true if element is displayed
	 *
	 * @param element
	 * @return
	 */
	protected static boolean isPresent(EnhancedBy element) {
		EnhancedWebElement expectedElement = Element.findElements(element);
		expectedElement.scrollToView();
		TestLog.ConsoleLog("isPresent:  " + element.name + " :" + expectedElement.isExist());
		return expectedElement.isExist();
	}

	/**
	 * returns true if element contains text
	 *
	 * @param element
	 * @param text
	 * @return
	 */
	protected static boolean isElementContainingText(EnhancedBy element, String text) {
		return Helper.list.isContainedInList(element, text);
	}
	
	/**
	 * verify if element contains text
	 * @param element
	 * @param text
	 */
	protected static void verifyElementContainingText(EnhancedBy element, String text) {
		Helper.waitForElementToLoad(element);
		Helper.assertTrue("element does not contain text: " + text, isElementContainingText(element, text));
	}
	
	/**
	 * verify if text is displayed on page
	 * @param text
	 */
	protected static void verifyTextDisplayed(String text) {
		boolean isText = isTextDisplayed(text);
		Helper.assertTrue("text: " + "text is not displayed", isText);
	}
	
	/**
	 * is text displayed on page
	 * @param text
	 * @return
	 */
	protected static boolean isTextDisplayed(String text) {
		try {
			boolean b = AbstractDriver.getWebDriver().getPageSource().contains(text);
			return b;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * verifies if element(s) is (are) not displayed
	 *
	 * @param by
	 */
	protected static void verifyElementIsNotDisplayed(EnhancedBy by) {
		EnhancedWebElement elements = Element.findElements(by);
		TestLog.logPass("I verify element '" + by.name + "' " + "is not displayed");
		Helper.wait.waitForElementToBeRemoved(by);
		AssertHelper.assertTrue("element '" + by.name + "' is displayed", !elements.isExist());
	}

	/**
	 * verifies element text equals provided value
	 *
	 * @param by
	 * @param value
	 */
	protected static void verifyElementText(EnhancedBy by, String value) {
		if (!value.isEmpty()) {
			Helper.wait.waitForElementToLoad(by);
			EnhancedWebElement elements = Element.findElements(by);
			Helper.assertTrue("text value not found expected: " + value + " actual: " + elements.get(0).getText(),
					elements.get(0).getText().equals(value));
		}

	}

	/**
	 * verifies element count element must be > 0 correct value: if more elements
	 * than needed are identified. eg. 4 ids, but 3 only are download ids.
	 * correction applicable to mobile
	 *
	 * @param by
	 * @param value
	 */
	protected static void verifyElementCount(EnhancedBy by, int value, int... correction) {
		int correctValue = 0;
		if (correction.length > 0)
			correctValue = correction[0];
		TestLog.logPass("I verify element '" + by.name + "' " + " occurs " + (value - correctValue) + " times");
		Helper.wait.waitForElementToLoad(by, AbstractDriver.TIMEOUT_SECONDS, value);
		int count = Helper.list.getListCount(by);
		Helper.assertEquals(value, count);
	}
}
