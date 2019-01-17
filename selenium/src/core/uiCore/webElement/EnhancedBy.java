package core.uiCore.webElement;

import org.openqa.selenium.By;

import core.helpers.Element;

/**
 * supporting 2 locator strategies: by, by2
 * 
 * @author CAEHMAT
 *
 */
public class EnhancedBy {

	public By by;
	public By by2;
	public String name;
	public String locator;
	public String locator2;
	public Element.LocatorType locatorType;

	/**
	 * gets by value for elements
	 * 
	 * @param by
	 * @param name
	 */
	public EnhancedBy(By by, String name) {
		this.by = by;
		this.name = name;
	}

	public EnhancedBy(By by, By by2, String name) {
		this.by = by;
		this.by2 = by2;
		this.name = name;
	}

	/**
	 * gets by value for elements
	 * 
	 * @param by
	 * @param name
	 */
	public EnhancedBy(By by, String name, String locator, Element.LocatorType locatorType) {
		this.by = by;
		this.name = name;
		this.locator = locator;
		this.locatorType = locatorType;
	}

	public EnhancedBy(By by, By by2, String name, String locator, String locator2, Element.LocatorType locatorType) {
		this.by = by;
		this.by2 = by2;
		this.name = name;
		this.locator = locator;
		this.locator2 = locator2;
		this.locatorType = locatorType;

	}

	public EnhancedBy withBy(By by) {
		this.by = by;
		return this;
	}

	public EnhancedBy withName(String name) {
		this.name = name;
		return this;
	}
}
