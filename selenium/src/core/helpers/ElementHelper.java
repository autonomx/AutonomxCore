package core.helpers;

import org.openqa.selenium.Dimension;

import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

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
		EnhancedWebElement element = Element.findElements(by);
		element.setAttribute(attribute, value);
	}

	/**
	 * returns element dimension
	 * 
	 * @param by
	 * @return
	 */
	protected static Dimension getElementSize(EnhancedBy by) {
		EnhancedWebElement element = Element.findElements(by);
		return element.getSize();
	}
}
