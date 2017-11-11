package core.helpers;

import core.logger.TestLog;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class VerifyHelper {

	/**
	 * verifies if element(s) is (are) displayed
	 * 
	 * @param by
	 */
	public static void verifyElementIsDisplayed(EnhancedBy by) {
		EnhancedWebElement elements = Element.findElements(by);
		TestLog.logPass("I verify '" + by.name + "' " + "is displayed");
		AssertHelper.assertTrue("element '" + by.name + "' is not displayed", elements.count() > 0);
	}

	/**
	 * returns true if element is displayed
	 * 
	 * @param element
	 * @return
	 */
	public static boolean isPresent(EnhancedBy element) {
		EnhancedWebElement expectedElement = Element.findElements(element);
		return expectedElement.isExist();
	}

	/**
	 * verifies if element(s) is (are) not displayed
	 * 
	 * @param by
	 */
	public static void verifyElementIsNotDisplayed(EnhancedBy by) {
		EnhancedWebElement elements = Element.findElements(by);
		TestLog.logPass("I verify element '" + by.name + "' " + "is not displayed");
		AssertHelper.assertTrue("element '" + by.name + "' is displayed", !elements.isExist());
	}
}