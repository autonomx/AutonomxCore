package core.helpers;

import java.util.Arrays;

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
	public void verifyElementIsDisplayed(EnhancedBy by) {
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
	public boolean isPresent(EnhancedBy element) {
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
	public boolean isElementContainingText(EnhancedBy element, String text) {
		return Helper.list.isContainedInList(element, text);
	}
	
	/**
	 * returns true if element has exact text value
	 *
	 * @param element
	 * @param text
	 * @return
	 */
	public boolean isElementHasExactText(EnhancedBy element, int index, String text) {
		Helper.waitForElementToBeClickable(element);
		String actualText = Helper.getTextValue(element, index);
		return actualText.equals(text);
	}
	
	/**
	 * return true if element is in list of elements. eg. delete button in a table
	 * @param list
	 * @param target
	 * @return
	 */
	public boolean isElementInList(EnhancedBy list, EnhancedBy target) {
		return Helper.list.getElementIndexInList(list, target) != -1;
	}
	
	/**
	 * return true if element is in list of elements. 
	 * eg. delete button in a table with user row identified by name: bob
	 * @param list
	 * @param target
	 * @return
	 */
	public boolean isElementInList(EnhancedBy list, String option, EnhancedBy target) {
		Helper.wait.waitForElementToLoad(list);
		int index = Helper.list.getElementIndexContainByText(list, option);
		AssertHelper.assertTrue("option not found in list: " + list.name, index > -1);

		EnhancedWebElement targetElement = Element.findElements(list, index, target);
		return targetElement.isExist();
	}
	
	/**
	 * verify if element has exact text value
	 *
	 * @param element
	 * @param text
	 * @return
	 */
	public void verifyElementHasExactText(EnhancedBy element, int index, String text) {
		Helper.waitForElementToBeClickable(element);
		String actualText = Helper.getTextValue(element, index);
		Helper.assertEquals(text, actualText);
	}
	
	
	/**
	 * verify if element contains text
	 * @param element
	 * @param text
	 */
	public void verifyElementContainingText(EnhancedBy element, String text) {
		Helper.waitForElementToLoad(element);
		Helper.assertTrue("element does not contain text: " + text, isElementContainingText(element, text));
	}
	
	/**
	 * verify if text is displayed on page
	 * @param text
	 */
	public void verifyTextDisplayed(String text) {
		boolean isText = isTextDisplayed(text);
		Helper.assertTrue("text: " + "text is not displayed", isText);
	}
	
	/**
	 * is text displayed on page
	 * @param text
	 * @return
	 */
	public boolean isTextDisplayed(String text) {
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
	public void verifyElementIsNotDisplayed(EnhancedBy by) {
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
	public void verifyElementText(EnhancedBy by, String value) {
		if (!value.isEmpty()) {
			Helper.wait.waitForElementToLoad(by);
			EnhancedWebElement elements = Element.findElements(by);
			Helper.assertTrue("text value not found expected: " + value + " actual: " + elements.get(0).getText(),
					elements.get(0).getText().equals(value));
		}

	}

	/**
	 * verifies element count element must be > 0 correct value: if more elements
	 * than needed are identified. eg. 4 ids, But 3 only are download ids.
	 * correction applicable to mobile
	 *
	 * @param by
	 * @param value
	 */
	public void verifyElementCount(EnhancedBy by, int value, int... correction) {
		int correctValue = 0;
		if (correction.length > 0)
			correctValue = correction[0];
		TestLog.logPass("I verify element '" + by.name + "' " + " occurs " + (value - correctValue) + " times");
		Helper.wait.waitForElementToLoad(by, AbstractDriver.TIMEOUT_SECONDS, value);
		int count = Helper.list.getListCount(by);
		Helper.assertEquals(value, count);
	}
	
	/**
	 * verifies if text contains any of values in list
	 * @param target
	 * @param values
	 */
	public void verifyAnyTextContaining(EnhancedBy target, String... values) {
		TestLog.logPass("I verify element '" + target.name + "' " + " contains " + Arrays.toString(values));
		Helper.waitForAnyTextToLoadContaining(target, values);
		
		EnhancedWebElement elements = Element.findElements(target);
		String actualValue = elements.getText();
		
		for(String value : values) {
			if(actualValue.contains(value)) {
				TestLog.logPass("value found: " + value);
				return;
			}
		}
		Helper.assertFalse("element: " + target.name + " did not display any text, text values: " + Arrays.toString(values));
	}
	
	/**
	 * verifies if text contains any of values in list
	 * @param target
	 * @param values
	 */
	public void verifyAnyText(EnhancedBy target, String... values) {
		TestLog.logPass("I verify element '" + target.name + "' " + " contains " + Arrays.toString(values));
		Helper.waitForAnyTextToLoadContaining(target, values);
		
		EnhancedWebElement elements = Element.findElements(target);
		String actualValue = elements.getText();
		
		for(String value : values) {
			if(actualValue.equals(value)) {
				TestLog.logPass("value found: " + value);
				return;
			}
		}
		Helper.assertFalse("element: " + target.name + " did not display any text, text values: " + Arrays.toString(values));
	}
	
	/**
	 * return if element is contained in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public boolean isContainedInList(EnhancedBy list, String option) {
		return Helper.isContainedInList(list, option);
	}
	
	/**
	 * return if element is an exact match in list
	 * 
	 * @param list
	 * @param option
	 * @return
	 */
	public boolean isExactMatchInList(EnhancedBy list, String option) {
		return Helper.isExactMatchInList(list, option);
	}
}
