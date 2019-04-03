package core.helpers.click;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import core.helpers.Element;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class ClickHelperJs extends Element {

	/**
	 * clicks target And waits for expected element to display retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public void clickAndExpect(EnhancedBy target, EnhancedBy expected) {
		clickAndExpect(target, 0, expected, true);
	}

	public void clickAndExpect(EnhancedBy target, EnhancedBy expected, boolean mobileRefresh) {
		clickAndExpect(target, 0, expected, mobileRefresh);
	}

	/**
	 * click And expect based on the text value on the element. eg. button with "OK"
	 * text
	 * 
	 * @param target
	 * @param text
	 * @param expected
	 */
	public void clickAndExpectByText(EnhancedBy target, String text, EnhancedBy expected) {
		TestLog.logPass("I click " + target.name);
		Helper.wait.waitForElementToBeClickable(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				Helper.list.selectListItemEqualsByName(target, text);
			isExpectedFound = Helper.wait.waitForElementToLoad(expected, targetWaitTimeInSeconds);
		} while (!isExpectedFound && retry > 0);

		Helper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
	}

	/**
	 * clicks element based on index And waits for expected element to be displayed
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 * @param mobileRefresh
	 *            TODO
	 */
	public void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected, boolean mobileRefresh) {

		TestLog.logPass("I click " + target.name);
		Helper.wait.waitForElementToBeClickable(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				clickJs(target, index);
			isExpectedFound = Helper.wait.waitForElementToLoad(expected, targetWaitTimeInSeconds);
			if (!isExpectedFound && mobileRefresh)
				Helper.mobile.refreshMobileApp();
			TestLog.ConsoleLog("clickAndExpect: expected: " + expected.name + " : " + isExpectedFound);
		} while (!isExpectedFound && retry > 0);

		Helper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
	}
	
	public void clickJs(EnhancedBy target, int index) {
		EnhancedWebElement targetElement = Element.findElements(target);

		WebElement element = targetElement.get(index);
		((JavascriptExecutor) AbstractDriver.getWebDriver()).executeScript("arguments[0].click()= true;", element);
	}
	
	/**
	 * clicks element based on index And waits for expected element to be displayed
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 */
	public void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected) {
		clickAndExpect(target, index, expected, true);
	}
    
	/**
	 * click And expect for either element
	 * @param target
	 * @param index
	 * @param expected1
	 * @param expected2
	 */
	public void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected1, EnhancedBy expected2) {

		TestLog.logPass("I click " + target.name);
		Helper.wait.waitForElementToBeClickable(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				clickJs(target, index);
			isExpectedFound = Helper.wait.waitForFirstElementToLoad(expected1, expected2, targetWaitTimeInSeconds);
			if (!isExpectedFound)
				Helper.mobile.refreshMobileApp();
			TestLog.ConsoleLog("clickAndExpect: expected: " + expected1.name + " : or: " + expected2.name + " : "
					+ isExpectedFound);
		} while (!isExpectedFound && retry > 0);

		Helper.assertTrue("expected element not found: " + expected1.name, isExpectedFound);
	}

	/**
	 * clicks target And waits for expected element to show up also waits for
	 * spinner element to be removed from display
	 * 
	 * @param target
	 * @param expected
	 * @param spinner
	 */
	public void clickAndExpect(EnhancedBy target, EnhancedBy expected, EnhancedBy spinner) {
		TestLog.logPass("I click " + target.name);

		Helper.wait.waitForElementToBeClickable(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				clickJs(target, 0);
			Helper.wait.waitForSeconds(0.5);
			Helper.wait.waitForElementToBeRemoved(spinner);
			isExpectedFound = Helper.wait.waitForElementToLoad(expected, targetWaitTimeInSeconds);
		} while (!isExpectedFound && retry > 0);

		Helper.assertTrue("expected element not found: " + expected.name, isExpectedFound);

	}

	public void clickAndNotExpect(EnhancedBy target, EnhancedBy expected) {
		clickAndNotExpect(target, 0, expected);
	}

	/**
	 * clicks target And waits for expected to not be displayed retries 10 times
	 * 
	 * @param target
	 * @param expected
	 */
	public void clickAndNotExpect(EnhancedBy target, int index, EnhancedBy expected) {
		TestLog.logPass("I click " + target.name);

		Helper.wait.waitForElementToBeClickable(target);

		EnhancedWebElement expectedElement = null;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				clickJs(target, index);
			Helper.wait.waitForSeconds(0.5);
			Helper.wait.waitForElementToBeRemoved(expected, targetWaitTimeInSeconds);
			expectedElement = Element.findElements(expected);
		} while (expectedElement.isExist() && retry > 0);

		Helper.assertTrue("expected element found", !expectedElement.isExist());

	}

	/**
	 * clicks target And waits for seconds
	 * 
	 * @param target
	 * @param expected
	 */
	public void clickAndWait(EnhancedBy target, double timeInSeconds) {
		clickAndWait(target, 0, timeInSeconds);
	}

	/**
	 * clicks target And waits for seconds
	 * 
	 * @param target
	 * @param expected
	 */
	public void clickAndWait(EnhancedBy target, int index, double timeInSeconds) {
		TestLog.logPass("I click " + target.name);
		Helper.wait.waitForElementToBeClickable(target);
		clickJs(target, index);
		Helper.wait.waitForSeconds(timeInSeconds);
	}
	
    /**
     * click element with text containing
     * @param by
     * @param text
     */
	public void clickElementContinsByText(EnhancedBy by,String text) {
		TestLog.ConsoleLog("I click element " + by.name + " with text containing: " +  text);
		Helper.list.selectListItemContainsByName(by,text);
	}
}