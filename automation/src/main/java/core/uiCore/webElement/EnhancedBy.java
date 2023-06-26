package core.uiCore.webElement;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import core.helpers.Element.LocatorType;
import core.helpers.Helper;
import io.appium.java_client.MobileBy;

/**
 * Elements are stored in list
 * 
 * @author CAEHMAT
 *
 */
@SuppressWarnings("deprecation")
public class EnhancedBy {
	public List<ElementObject> elementObject;
	public String name = "";

	public EnhancedBy() {
		elementObject = new ArrayList<ElementObject>();
	}

	public EnhancedBy byCss(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.cssSelector(element), name, element, LocatorType.css);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byCss(String element) {
		return byCss(element, name);
	}

	public EnhancedBy byXpath(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.xpath(element), name, element, LocatorType.xpath);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byXpath(String element) {
		return byXpath(element, name);
	}

	public EnhancedBy byId(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.id(element), name, element, LocatorType.id);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byId(String element) {
		return byId(element, name);
	}

	public EnhancedBy byName(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.name(element), name, element, LocatorType.name);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byName(String element) {
		return byName(element, name);
	}

	public EnhancedBy byClass(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.className(element), name, element, LocatorType.classType);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byClass(String element) {
		return byClass(element, name);
	}
	
	public EnhancedBy byTagName(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.tagName(element), name, element, LocatorType.tagName);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byTagname(String element) {
		return byClass(element, name);
	}
	
	public EnhancedBy byLinkText(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.linkText(element), name, element, LocatorType.linkText);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byLinkText(String element) {
		return byClass(element, name);
	}
	
	public EnhancedBy byPartialLinkText(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.partialLinkText(element), name, element, LocatorType.partialLinkText);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byPartialLinkText(String element) {
		return byClass(element, name);
	}

	public EnhancedBy byAccessibility(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(MobileBy.AccessibilityId(element), name, element,
				LocatorType.accessibiliy);
		elementObject.add(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byAccessibility(String element) {
		return byAccessibility(element, name);
	}
}
