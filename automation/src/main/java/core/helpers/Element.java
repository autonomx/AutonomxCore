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
		css, xpath, id, classType, accessibiliy, mobileClass, name, tagName, linkText, partialLinkText
	}

	/**
	 * Platform to which a locator applies. A locator created directly through
	 * {@link Element} is shared by all platforms.
	 */
	public enum TargetPlatform {
		ANY, ANDROID, IOS
	}

	/**
	 * Platform-specific locator namespaces. For example:
	 * <pre>
	 * Element.android.byXpath("//android.widget.Button", "Submit");
	 * Element.ios.byXpath("//XCUIElementTypeButton", "Submit");
	 * Element.byXpath("//button", "Submit");
	 * </pre>
	 */
	public static final class PlatformLocators {
		private final TargetPlatform platform;

		private PlatformLocators(TargetPlatform platform) {
			this.platform = platform;
		}

		public EnhancedBy byCss(String element, String name) {
			return new EnhancedBy(platform).byCss(element, name);
		}

		public EnhancedBy byId(String element, String name) {
			return new EnhancedBy(platform).byId(element, name);
		}

		public EnhancedBy byName(String element, String name) {
			return new EnhancedBy(platform).byName(element, name);
		}

		public EnhancedBy byXpath(String element, String name) {
			return new EnhancedBy(platform).byXpath(element, name);
		}

		public EnhancedBy byTextXpath(String element, String name) {
			String xpath = element.replace("content-desc", "text");
			return new EnhancedBy(platform).byXpath(element, name).byXpath(xpath, name);
		}

		public EnhancedBy byXpathContentDesc(String element, String name) {
			return new EnhancedBy(platform).byXpath("//*[@content-desc='" + element + "']", name);
		}

		public EnhancedBy byClass(String element, String name) {
			return new EnhancedBy(platform).byClass(element, name);
		}

		public EnhancedBy byTagName(String element, String name) {
			return new EnhancedBy(platform).byTagName(element, name);
		}

		public EnhancedBy byLinkText(String element, String name) {
			return new EnhancedBy(platform).byLinkText(element, name);
		}

		public EnhancedBy byPartialLinkText(String element, String name) {
			return new EnhancedBy(platform).byPartialLinkText(element, name);
		}

		public EnhancedBy byAccessibility(String element, String name) {
			String xpath = "//*[@text='" + element + "']";
			return new EnhancedBy(platform).byAccessibility(element, name).byXpath(xpath, name);
		}

		/**
		 * Accessibility id only, without byAccessibility's XPath text fallback.
		 * XPath is the slowest locator strategy on native mobile sessions, so
		 * elements known to expose an accessibility id should prefer this.
		 */
		public EnhancedBy byAccessibilityExact(String element, String name) {
			return new EnhancedBy(platform).byAccessibility(element, name);
		}
	}

	/** Locators used only when an Android driver is active. */
	public static final PlatformLocators android = new PlatformLocators(TargetPlatform.ANDROID);

	/** Locators used only when an iOS driver is active. */
	public static final PlatformLocators ios = new PlatformLocators(TargetPlatform.IOS);

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
	 * 
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
	 * gets element by tag name
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byTagName(String element, String name) {
		return new EnhancedBy().byTagName(element, name);
	}
	
	/**
	 * gets element by link text
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byLinkText(String element, String name) {
		return new EnhancedBy().byLinkText(element, name);
	}
	
	/**
	 * gets element by partial link text
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	public static EnhancedBy byPartialLinkText(String element, String name) {
		return new EnhancedBy().byPartialLinkText(element, name);
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

	/**
	 * Accessibility id only, without byAccessibility's XPath text fallback.
	 * XPath is the slowest locator strategy on native mobile sessions, so
	 * elements known to expose an accessibility id should prefer this.
	 */
	public static EnhancedBy byAccessibilityExact(String element, String name) {
		return new EnhancedBy().byAccessibility(element, name);
	}
}
