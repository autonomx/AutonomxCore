package core.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;
import core.uiCore.webElement.ImpEnhancedWebElement;
import io.appium.java_client.MobileBy;

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
	 * finds element based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	protected static EnhancedWebElement findElements(EnhancedBy child, WebElement parent) {

		return new ImpEnhancedWebElement(child, AbstractDriver.getWebDriver(), parent);
	}

	/**
	 * finds list of elements
	 * 
	 * @param element
	 * @return
	 */
	protected static EnhancedWebElement findElements(EnhancedBy element) {

		return new ImpEnhancedWebElement(element, AbstractDriver.getWebDriver(), null);
	}

	/**
	 * finds a list of elements based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	protected static EnhancedWebElement findElements(EnhancedBy child, EnhancedWebElement parent) {

		return new ImpEnhancedWebElement(child, AbstractDriver.getWebDriver(), parent);
	}

	/**
	 * sets the by value with by selector and name of the element
	 * 
	 * @param by
	 * @param name
	 * @return
	 */
	public static EnhancedBy bySelector(By by, String name) {

		return new EnhancedBy(by, name);
	}

	/**
	 * gets element by css value
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byCss(String element, String name) {

		return new EnhancedBy(By.cssSelector(element), name, element, LocatorType.css);
	}

	/**
	 * gets element by id
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byId(String element, String name) {

		return new EnhancedBy(By.id(element), name, element, LocatorType.id);
	}
	
	
	/**
	 * get element by name
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byName(String element, String name) {

		return new EnhancedBy(By.name(element), name, element, LocatorType.name);
	}

	/**
	 * gets element by xpath
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byXpath(String element, String name) {
		if (element.isEmpty())
			Helper.assertTrue("element cannot be empty", false);

		return new EnhancedBy(By.xpath(element), name, element, LocatorType.xpath);
	}

	public static EnhancedBy byTextXpath(String element, String name) {
		if (element.isEmpty())
			Helper.assertTrue("element cannot be empty", false);

		String element2 = element;
		element2 = element2.replace("content-desc", "text");

		return new EnhancedBy(By.xpath(element), By.xpath(element), name, element, element2, LocatorType.xpath);
	}

	/**
	 * gets element by class (for mobile)
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byXpathContentDesc(String element, String name) {
		if (element.isEmpty())
			Helper.assertTrue("element cannot be empty", false);

		return new EnhancedBy(By.xpath("//*[@content-desc='" + element + "']"), name, element, LocatorType.xpath);
	}

	/**
	 * gets element by class name
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byClass(String element, String name) {
		if (element.isEmpty())
			Helper.assertTrue("element cannot be empty", false);

		return new EnhancedBy(By.className(element), name, element, LocatorType.classType);
	}

	/**
	 * gets element by accessibility id
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byAccessibility(String element, String name) {
		if (element.isEmpty())
			Helper.assertTrue("element cannot be empty", false);

		String xpath = "//*[@text='" + element + "']";
		return new EnhancedBy(MobileBy.AccessibilityId(element), By.xpath(xpath), name, element, xpath, LocatorType.accessibiliy);
	}

	/**
	 * gets element by class (for mobile)
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byMobileClass(String element, String name) {
		if (element.isEmpty())
			Helper.assertTrue("element cannot be empty", false);

		return new EnhancedBy(MobileBy.className(element), name, element, LocatorType.mobileClass);
	}
}