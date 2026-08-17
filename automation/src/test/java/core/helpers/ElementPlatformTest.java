package core.helpers;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.ElementObject;
import core.uiCore.webElement.ImpEnhancedWebElement;

public class ElementPlatformTest {

	@Test
	public void platformNamespacesTagEverySupportedElementFactory() {
		EnhancedBy[] androidLocators = {
				Element.android.byCss(".button", "button"),
				Element.android.byId("button", "button"),
				Element.android.byName("button", "button"),
				Element.android.byXpath("//button", "button"),
				Element.android.byTextXpath("//*[@content-desc='button']", "button"),
				Element.android.byXpathContentDesc("button", "button"),
				Element.android.byClass("android.widget.Button", "button"),
				Element.android.byTagName("button", "button"),
				Element.android.byLinkText("Submit", "button"),
				Element.android.byPartialLinkText("Sub", "button"),
				Element.android.byAccessibility("button", "button")
		};

		for (EnhancedBy locator : androidLocators) {
			for (ElementObject object : locator.elementObject)
				assertEquals(object.targetPlatform, Element.TargetPlatform.ANDROID);
		}

		EnhancedBy iosLocator = Element.ios.byXpath("//XCUIElementTypeButton", "button");
		assertEquals(iosLocator.elementObject.get(0).targetPlatform, Element.TargetPlatform.IOS);

		EnhancedBy sharedLocator = Element.byXpath("//button", "button");
		assertEquals(sharedLocator.elementObject.get(0).targetPlatform, Element.TargetPlatform.ANY);
	}

	@Test
	public void resolutionUsesSharedAndActivePlatformLocatorsOnly() {
		DriverProbe androidDriver = new DriverProbe("android");

		assertEquals(new ImpEnhancedWebElement(null, 0, androidDriver.driver,
				Element.android.byId("android-button", "button")).count(), 1);
		assertEquals(new ImpEnhancedWebElement(null, 0, androidDriver.driver,
				Element.ios.byId("ios-button", "button")).count(), 0);
		assertEquals(new ImpEnhancedWebElement(null, 0, androidDriver.driver,
				Element.byId("shared-button", "button")).count(), 1);
		assertEquals(androidDriver.findCalls.get(), 2);
	}

	private static final class DriverProbe implements InvocationHandler {
		private final AtomicInteger findCalls = new AtomicInteger();
		private final WebDriver driver;
		private final String platformName;

		private DriverProbe(String platformName) {
			this.platformName = platformName;
			this.driver = proxy(new Class<?>[] { WebDriver.class, HasCapabilities.class }, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
			case "getCapabilities":
				return new ImmutableCapabilities(Map.of("platformName", platformName));
			case "findElements":
				findCalls.incrementAndGet();
				WebElement element = proxy(WebElement.class, (ignored, unused, ignoredArgs) -> null);
				return List.of(element);
			case "hashCode":
				return System.identityHashCode(proxy);
			case "equals":
				return proxy == args[0];
			case "toString":
				return "ElementPlatformTestDriver";
			default:
				return defaultValue(method.getReturnType());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<?> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(ElementPlatformTest.class.getClassLoader(), new Class<?>[] { type }, handler);
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<?>[] types, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(ElementPlatformTest.class.getClassLoader(), types, handler);
	}

	private static Object defaultValue(Class<?> type) {
		if (!type.isPrimitive())
			return null;
		if (type == boolean.class)
			return false;
		if (type == char.class)
			return '\0';
		if (type == byte.class)
			return (byte) 0;
		if (type == short.class)
			return (short) 0;
		if (type == int.class)
			return 0;
		if (type == long.class)
			return 0L;
		if (type == float.class)
			return 0F;
		if (type == double.class)
			return 0D;
		return null;
	}
}
