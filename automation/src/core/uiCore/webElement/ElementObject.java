package core.uiCore.webElement;

import org.openqa.selenium.By;

import core.helpers.Element;

/**
 * supporting 2 locator strategies: by, by2
 * 
 * @author CAEHMAT
 *
 */
public class ElementObject {

	public By by;
	public String name;
	public String locator;
	public Element.LocatorType locatorType;

	/**
	 * gets by value for elements
	 * 
	 * @param by
	 * @param name
	 */
	public ElementObject(By by, String name) {
		this.by = by;
		this.name = name;
	}

	/**
	 * gets by value for elements
	 * 
	 * @param by
	 * @param name
	 */
	public ElementObject(By by, String name, String locator, Element.LocatorType locatorType) {
		this.by = by;
		this.name = name;
		this.locator = locator;
		this.locatorType = locatorType;
	}

	public ElementObject withBy(By by) {
		this.by = by;
		return this;
	}

	public ElementObject withName(String name) {
		this.name = name;
		return this;
	}
}
