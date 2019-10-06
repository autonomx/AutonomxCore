package core.helpers;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import core.support.logger.TestLog;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;
import module.framework.panel.LoginPanel;

/**
 * app page is parent class of different apps
 * 
 * @author ehsan matean
 *
 */
public class ElementHelper {

	/**
	 * gets specified attribute of the element
	 * 
	 * @param byValue
	 * @param index
	 * @param attribute
	 */
	protected static String getAttribute(EnhancedBy byValue, String attribute) {
		return getAttribute(byValue, 0, attribute);
	}

	/**
	 * gets specified attribute of the element based on index
	 * 
	 * @param byValue
	 * @param index
	 * @param attribute
	 */
	protected static String getAttribute(EnhancedBy byValue, int index, String attribute) {
		Helper.waitForElementToLoad(byValue);

		EnhancedWebElement element = Element.findElements(byValue);
		return element.getAttribute(attribute, index);
	}

	protected static boolean isElementContainingClass(EnhancedBy by, String value) {
		return isAttributeContaining(by, "class", value);
	}

	/**
	 * returns true if element contains class value
	 * 
	 * @param by
	 * @param classValue
	 * @return
	 */
	protected static boolean isAttributeContaining(EnhancedBy by, String attribute, String value) {
		String attributeValues = getAttribute(by, 0, attribute);
		return attributeValues.contains(value);
	}

	/**
	 * sets attribute value of an element
	 * 
	 * @param by
	 * @param attribute
	 * @param value
	 */
	protected static void setAttribute(EnhancedBy by, String attribute, String value) {
		setAttribute(by, 0, attribute, value);
	}
	
	/**
	 * sets attribute value of an element
	 * 
	 * @param by
	 * @param index
	 * @param attribute
	 * @param value
	 */
	protected static void setAttribute(EnhancedBy by, int index, String attribute, String value) {
		Helper.waitForElementToLoad(by);
		String attributeValue = StringUtils.EMPTY;
		int retry = 3;
		int currentRetryCount = 0;
		do {
			currentRetryCount++;
			EnhancedWebElement element = Element.findElements(by);
			element.setAttribute(attribute, index, value);
			attributeValue = Helper.getAttribute(by, index, attribute);
			if(currentRetryCount > 1) Helper.waitForSeconds(0.5);
		} while ((attributeValue == null || attributeValue.isEmpty()) && currentRetryCount <= retry);
	}

	/**
	 * returns element dimension
	 * 
	 * @param by
	 * @return
	 */
	protected static Dimension getElementSize(EnhancedBy by) {
		Helper.waitForElementToLoad(by);
		EnhancedWebElement element = Element.findElements(by);
		return element.getSize();
	}
	
	/**
	 * 	get element position on display

	 * @param by
	 * @return
	 */
	protected static Point getElementPosition(EnhancedBy by) {
		return getElementPosition(by, 0);
	}
	
	/**
	 * get element position on display
	 * @param by
	 * @param index
	 * @return
	 */
	protected static Point getElementPosition(EnhancedBy by, int index) {
		Helper.waitForElementToLoad(by);
		EnhancedWebElement element = Element.findElements(by);
		return element.getLocation(index);
	}
	
	/**
	 * returns the center coordinates of the target element
	 * @param target
	 * @return
	 */
	protected static int[] findMiddleOfElement(EnhancedBy target) {
		Helper.waitForElementToLoad(target);

		EnhancedWebElement targetElement = Element.findElements(target);
		int x = targetElement.get(0).getLocation().x;
		int y =  targetElement.get(0).getLocation().y;
		TestLog.ConsoleLog("Center at: point x: " + x + " point y: " + y);
		return new int[] { x, y };
	}
}
