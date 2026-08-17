package core.uiCore.webElement;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import core.helpers.Element;
import core.helpers.Element.LocatorType;
import core.helpers.Helper;
import io.appium.java_client.AppiumBy;

/**
 * Elements are stored in list
 *
 * @author CAEHMAT
 */
public class EnhancedBy {
	public List<ElementObject> elementObject;
	public String name = "";
	private final Element.TargetPlatform targetPlatform;

	public EnhancedBy() {
		this(Element.TargetPlatform.ANY);
	}

	public EnhancedBy(Element.TargetPlatform targetPlatform) {
		elementObject = new ArrayList<ElementObject>();
		this.targetPlatform = targetPlatform;
	}

	private void addLocator(ElementObject locatorObject) {
		locatorObject.targetPlatform = targetPlatform;
		elementObject.add(locatorObject);
	}

	public EnhancedBy byCss(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.cssSelector(element), name, element, LocatorType.css);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byCss(String element) {
		return byCss(element, name);
	}

	public EnhancedBy byXpath(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.xpath(element), name, element, LocatorType.xpath);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byXpath(String element) {
		return byXpath(element, name);
	}

	public EnhancedBy byId(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.id(element), name, element, LocatorType.id);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byId(String element) {
		return byId(element, name);
	}

	public EnhancedBy byName(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.name(element), name, element, LocatorType.name);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byName(String element) {
		return byName(element, name);
	}

	public EnhancedBy byClass(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.className(element), name, element, LocatorType.classType);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byClass(String element) {
		return byClass(element, name);
	}

	public EnhancedBy byTagName(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.tagName(element), name, element, LocatorType.tagName);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byTagName(String element) {
		return byTagName(element, name);
	}

	/**
	 * Backwards-compatible alias for the historic method spelling.
	 */
	public EnhancedBy byTagname(String element) {
		return byTagName(element, name);
	}

	public EnhancedBy byLinkText(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.linkText(element), name, element, LocatorType.linkText);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byLinkText(String element) {
		return byLinkText(element, name);
	}

	public EnhancedBy byPartialLinkText(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(By.partialLinkText(element), name, element,
				LocatorType.partialLinkText);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byPartialLinkText(String element) {
		return byPartialLinkText(element, name);
	}

	public EnhancedBy byAccessibility(String element, String name) {
		Helper.assertTrue("element cannot be empty", !element.isEmpty());

		ElementObject locatorObject = new ElementObject(AppiumBy.accessibilityId(element), name, element,
				LocatorType.accessibiliy);
		addLocator(locatorObject);
		this.name = name;
		return this;
	}

	public EnhancedBy byAccessibility(String element) {
		return byAccessibility(element, name);
	}
}
