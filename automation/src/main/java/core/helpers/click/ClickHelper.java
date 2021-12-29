package core.helpers.click;

import java.time.Duration;

import org.openqa.selenium.interactions.Actions;

import core.helpers.Element;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class ClickHelper extends Element {

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
		Helper.wait.waitForElementToLoad(target);

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
	 * click And expect based on the text value on the element. eg. button with "OK"
	 * text
	 * 
	 * @param target
	 * @param text
	 * @param expected
	 */
	public void clickAndExpectContainsByText(EnhancedBy target, String text, EnhancedBy expected) {
		TestLog.logPass("I click " + target.name);
		Helper.wait.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				Helper.list.selectListItemContainsByName(target, text);
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
	 * @param mobileRefresh TODO
	 */
	public void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected, boolean mobileRefresh) {

		TestLog.logPass("I click " + target.name);
		EnhancedWebElement targetElement = Element.findElements(target);
		Helper.wait.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				targetElement.click(index);
			isExpectedFound = Helper.wait.waitForElementToLoad(expected, targetWaitTimeInSeconds);
			TestLog.ConsoleLog("clickAndExpect: expected: " + expected.name + " : " + isExpectedFound);
		} while (!isExpectedFound && retry > 0);

		Helper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
	}

	/**
	 * click and expect with retrying to click the elmeent on failure
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 */
	public void clickAndExpectNoRetry(EnhancedBy target, int index, EnhancedBy expected) {

		TestLog.logPass("I click " + target.name);
		EnhancedWebElement targetElement = Element.findElements(target);
		Helper.wait.waitForElementToLoad(target);

		boolean isExpectedFound = false;

		targetElement.click(index);
		isExpectedFound = Helper.wait.waitForElementToLoad(expected);
		TestLog.ConsoleLog("clickAndExpect: expected: " + expected.name + " : " + isExpectedFound);

		Helper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
	}

	/**
	 * click And expect using action click
	 * 
	 * @param target
	 * @param expected
	 */
	public void clickAndExpectByAction(EnhancedBy target, EnhancedBy expected) {
		clickAndExpectByAction(target, 0, expected);
	}

	/**
	 * click And expect using action click
	 * 
	 * @param target
	 * @param index
	 * @param expected
	 */
	public void clickAndExpectByAction(EnhancedBy target, int index, EnhancedBy expected) {

		TestLog.logPass("I click " + target.name);
		EnhancedWebElement targetElement = Element.findElements(target);
		Helper.wait.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target)) {
				Actions action = new Actions(AbstractDriver.getWebDriver());
				action.click(targetElement.get(0)).perform();
			}
			isExpectedFound = Helper.wait.waitForElementToLoad(expected, targetWaitTimeInSeconds);
			TestLog.ConsoleLog("clickAndExpect: expected: " + expected.name + " : " + isExpectedFound);
		} while (!isExpectedFound && retry > 0);

		Helper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
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
	 * 
	 * @param target
	 * @param index
	 * @param expected1
	 * @param expected2
	 */
	public void clickAndExpect(EnhancedBy target, int index, EnhancedBy expected1, EnhancedBy expected2) {

		TestLog.logPass("I click " + target.name);
		EnhancedWebElement targetElement = Element.findElements(target);
		Helper.wait.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				targetElement.click(index);
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

		EnhancedWebElement targetElement = Element.findElements(target);
		Helper.wait.waitForElementToLoad(target);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				targetElement.click();
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

		EnhancedWebElement targetElement = Element.findElements(target);
		Helper.wait.waitForElementToLoad(target);

		EnhancedWebElement expectedElement = null;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			if (Helper.isPresent(target))
				targetElement.click(index);
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
	   	Helper.wait.waitForElementToLoad(target);
		EnhancedWebElement targetElement = Element.findElements(target);
		targetElement.click(index);
		Helper.wait.waitForSeconds(timeInSeconds);
	}

	/**
	 * Click on an element's specific x,y location
	 * 
	 * @param by by element
	 * @param x  x offset coordinate
	 * @param y  y offset coordinate
	 */
	public void clickElementLocationBy(EnhancedBy by, int x, int y) {
		TestLog.logPass("I click element at position offset by x: " + x + " y: " + y);

		EnhancedWebElement targetElement = Element.findElements(by);
		Actions actions = new Actions(AbstractDriver.getWebDriver());
		Helper.wait.waitForElementToLoad(by);
		actions.moveToElement(targetElement.get(0), x, y).click().perform();
	}

	/**
	 * click at position x, y
	 * 
	 * @param x
	 * @param y
	 */
	public void clickPoints(int x, int y) {

		TestLog.logPass("I click point at x: " + x + " y: " + y);
		Actions action = new Actions(AbstractDriver.getWebDriver());

		// offset from 0,0 position
		action.moveByOffset(x, y).click().build().perform();

		// return back to 0,0 position
		resetMouse(x, y);
	}

	/**
	 * click point at x,y coordinates and expect and element to be present retry
	 * every 5 seconds for duration of explicit timeout
	 * 
	 * @param x
	 * @param y
	 * @param expected
	 */
	public void clickPointsAndExpect(int x, int y, EnhancedBy expected) {

		EnhancedWebElement expectedElement = null;
		int targetWaitTimeInSeconds = 5;
		int retry = AbstractDriver.TIMEOUT_SECONDS / targetWaitTimeInSeconds;

		do {
			retry--;
			clickPoints(x, y);
			Helper.wait.waitForSeconds(0.5);
			Helper.wait.waitForElementToLoad(expected, targetWaitTimeInSeconds);
			expectedElement = Element.findElements(expected);
		} while (!expectedElement.isExist() && retry > 0);

		Helper.assertTrue("expected element not found", expectedElement.isExist());
	}

	/**
	 * moves mouse back to original position
	 * 
	 * @param x
	 * @param y
	 */
	public void resetMouse(int x, int y) {
		Actions action = new Actions(AbstractDriver.getWebDriver());

		// return back to 0,0 position
		action.moveByOffset(-x, -y).build().perform();
	}

	/**
	 * double click at position
	 * 
	 * @param x
	 * @param y
	 */
	public void doubleClickPoints(int x, int y) {
		TestLog.logPass("I double click point at x: " + x + " y: " + y);

		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.moveByOffset(x, y).click().release().pause(Duration.ofMillis(250)).click().release().build().perform();

		// return back to 0,0 position
		resetMouse(x, y);
	}

	/**
	 * click element with text containing
	 * 
	 * @param by
	 * @param text
	 */
	public void clickElementContinsByText(EnhancedBy by, String text) {
		TestLog.ConsoleLog("I click element " + by.name + " with text containing: " + text);
		Helper.list.selectListItemContainsByName(by, text);
	}

	/**
	 * click And hold element
	 * 
	 * @param target
	 * @param seconds
	 */
	public void clickAndHold(EnhancedBy target, double seconds) {
		clickAndHold(target, 0, seconds);
	}

	/**
	 * click And hold based on element index
	 * 
	 * @param target
	 * @param index
	 * @param seconds
	 */
	public void clickAndHold(EnhancedBy target, int index, double seconds) {
		Helper.wait.waitForElementToBeClickable(target);

		EnhancedWebElement targetElement = Element.findElements(target);

		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.clickAndHold(targetElement.get(index)).perform();
		Helper.wait.waitForSeconds(seconds);
		action.release(targetElement.get(index)).perform();
	}
	
	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public void dragAndDrop(EnhancedBy src, EnhancedBy target) {
		dragAndDrop(src,0,target, 0);
	}

	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public void dragAndDrop(EnhancedBy src, int srcIndex, EnhancedBy target, int targetIndex) {
		Helper.wait.waitForElementToLoad(src);

		EnhancedWebElement srcElement = Element.findElements(src);
		EnhancedWebElement targetElement = Element.findElements(target);

		Actions actions = new Actions(AbstractDriver.getWebDriver());
		actions.dragAndDrop(srcElement.get(srcIndex), targetElement.get(targetIndex));
		actions.build().perform();
	}
	
	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public void dragAndDrop(EnhancedBy srcParent, int srcParentIndex, EnhancedBy srcChild, int scrChildIndex, EnhancedBy targetParent, int targeParenttIndex, EnhancedBy targetChild, int targetChildIndex) {
		Helper.wait.waitForElementToLoad(srcParent);

		EnhancedWebElement srcElement = Element.findElements(srcParent, srcParentIndex, srcChild);		
		EnhancedWebElement targetElement =  Element.findElements(targetParent, targeParenttIndex, targetChild);

		Actions actions = new Actions(AbstractDriver.getWebDriver());
		actions.dragAndDrop(srcElement.get(scrChildIndex), targetElement.get(targetChildIndex));
		actions.build().perform();
	}

	/**
	 * drag And drop from src element to target element
	 * 
	 * @param src
	 * @param target
	 */
	public void dragAndDrop(EnhancedBy src, int xOffset, int yOffset) {
		Helper.wait.waitForElementToLoad(src);

		EnhancedWebElement srcElement = Element.findElements(src);

		Actions actions = new Actions(AbstractDriver.getWebDriver());
		actions.dragAndDropBy(srcElement.get(0), xOffset, yOffset);
		actions.build().perform();
	}
}