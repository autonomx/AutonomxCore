package core.helpers;

import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;
import core.uiCore.webElement.ImpEnhancedWebElement;

/**
 * app page is parent class of different apps
 * 
 * @author ehsan matean
 *
 */
public class Element {
	
	
	public enum LocatorType {
	    css, xpath, id, classType, accessibiliy, mobileClass, name
	}

	/**
	 * finds list of elements
	 * 
	 * @param element
	 * @return
	 */
	protected static EnhancedWebElement findElements(EnhancedBy element) {

		return new ImpEnhancedWebElement(null, 0, AbstractDriver.getWebDriver(), element);
	}

	/**
	 * finds a list of elements based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	
	protected static EnhancedWebElement findElements(EnhancedBy parent, int parentIndex, EnhancedBy child) {

		return new ImpEnhancedWebElement(parent, parentIndex, AbstractDriver.getWebDriver(), child);
	}
	
	protected static EnhancedWebElement findElements(EnhancedBy parent, EnhancedBy child) {

		return new ImpEnhancedWebElement(parent, 0, AbstractDriver.getWebDriver(), child);
	}

	/**
	 * gets element by css value
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byCss(String element, String name) {

		return new EnhancedBy().byCss(element, name);
	}

	/**
	 * gets element by id
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byId(String element, String name) {

		return new EnhancedBy().byId(element, name);
	}
	
	
	/**
	 * get element by name
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byName(String element, String name) {

		return new EnhancedBy().byName(element, name);
	}

	/**
	 * gets element by xpath
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byXpath(String element, String name) {

		return new EnhancedBy().byXpath(element, name);
	}
	

	public static EnhancedBy byTextXpath(String element, String name) {

		String xpath = element.replace("content-desc", "text");
		return new EnhancedBy().byXpath(element, name).byXpath(xpath, name);
	}

	/**
	 * gets element by class (for mobile)
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byXpathContentDesc(String element, String name) {
		return new EnhancedBy().byXpath("//*[@content-desc='" + element + "']", name);
	}

	/**
	 * gets element by class name
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byClass(String element, String name) {
		return new EnhancedBy().byClass(element, name);
	}

	/**
	 * gets element by accessibility id
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byAccessibility(String element, String name) {

		String xpath = "//*[@text='" + element + "']";
		return new EnhancedBy().byAccessibility(element, name).byXpath(xpath, name);
	}
}