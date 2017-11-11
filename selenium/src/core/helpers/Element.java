package core.helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import core.driver.AbstractDriver;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;
import core.webElement.ImpEnhancedWebElement;
import io.appium.java_client.MobileBy;

/**
 * app page is parent class of different apps
 * 
 * @author ehsan matean
 *
 */
public class Element {


	/**
	 * finds element based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	public static EnhancedWebElement findElements(EnhancedBy child, WebElement parent) {

		return new ImpEnhancedWebElement(child.name, child.by, AbstractDriver.getWebDriver(), parent);
	}

	/**
	 * finds list of elements
	 * 
	 * @param element
	 * @return
	 */
	public static EnhancedWebElement findElements(EnhancedBy element) {

		return new ImpEnhancedWebElement(element.name, element.by, AbstractDriver.getWebDriver(), null);
	}

	/**
	 * finds a list of elements based on parent element
	 * 
	 * @param element
	 * @param parent
	 * @return
	 */
	public static EnhancedWebElement findElements(EnhancedBy child, EnhancedWebElement parent) {

		return new ImpEnhancedWebElement(child.name, child.by, AbstractDriver.getWebDriver(), parent);
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
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byCss(String element, String name) {

		return new EnhancedBy(By.cssSelector(element), name);
	}
	
	/**
	 * gets element by id
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byId(String element, String name) {

		return new EnhancedBy(By.id(element), name);
	}
	
	/**
	 * gets element by xpath
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byXpath(String element, String name) {

		return new EnhancedBy(By.xpath(element), name);
	}
	
	/**
	 * gets element by class name
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byClass(String element, String name) {

		return new EnhancedBy(By.className(element), name);
	}
	
	
	
	/**
	 * gets element by accessibility id
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byAccessibility(String element, String name) {

		return new EnhancedBy(MobileBy.AccessibilityId(element), name);
	}
	
	/**
	 * gets element by class (for mobile)
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byMobileClass(String element, String name) {

		return new EnhancedBy(MobileBy.className(element), name);
	}
	
}
