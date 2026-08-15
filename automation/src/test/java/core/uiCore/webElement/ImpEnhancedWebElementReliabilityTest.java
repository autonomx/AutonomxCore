package core.uiCore.webElement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import core.helpers.WaitHelper;
import core.support.configReader.Config;
import core.support.listeners.TestListener;
import core.support.objects.TestObject;
import core.uiCore.drivers.AbstractDriverJunit;

public class ImpEnhancedWebElementReliabilityTest {

	private static final String TEST_ID = "ImpEnhancedWebElementReliabilityTest-unit";

	@BeforeMethod
	public void setUpUnitContext() {
		TestListener.isTestNG = false;
		TestObject.currentTestId.set(TEST_ID);
		TestObject.testInfo.put(TEST_ID, new TestObject().withTestId(TEST_ID));
		TestObject.testInfo.put(TestObject.DEFAULT_TEST, new TestObject().withTestId(TestObject.DEFAULT_TEST));
		TestObject.getTestInfo().log = LogManager.getLogger(TEST_ID);
		Config.putValue("global.timeoutSeconds", "1", false);
		Config.putValue("global.timeout.implicit.Seconds", "1", false);
		AbstractDriverJunit.setWebDriver(null);
	}

	@AfterMethod
	public void cleanUpUnitContext() {
		AbstractDriverJunit.setWebDriver(null);
		TestObject.currentTestId.remove();
		TestObject.currentTestName.remove();
		TestObject.testInfo.clear();
		TestListener.isTestNG = false;
	}

	@Test
	public void clickRequeriesDomAfterStaleElementAndSucceeds() {
		ElementBehavior stale = new ElementBehavior(true, 1, 0, 0);
		ElementBehavior fresh = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(stale.element), List.of(fresh.element)));
		ImpEnhancedWebElement subject = subject(driver.driver, target());

		subject.click();

		assertEquals(stale.clicks.get(), 1);
		assertEquals(fresh.clicks.get(), 1);
		assertEquals(driver.findCalls.get(), 2, "stale click should invalidate the cached element and re-query the DOM");
	}

	@Test
	public void sendKeysRequeriesDomAfterStaleElementAndSucceeds() {
		ElementBehavior stale = new ElementBehavior(true, 0, 1, 0);
		ElementBehavior fresh = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(stale.element), List.of(fresh.element)));
		ImpEnhancedWebElement subject = subject(driver.driver, target());

		subject.sendKeys("hello");

		assertEquals(stale.sendKeys.get(), 1);
		assertEquals(fresh.sendKeys.get(), 1);
		assertEquals(driver.findCalls.get(), 2, "stale sendKeys should invalidate the cached element and re-query the DOM");
	}

	@Test
	public void existenceCheckRequeriesAfterStaleDisplayFailure() {
		ElementBehavior stale = new ElementBehavior(true, 0, 0, 1);
		ElementBehavior fresh = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(stale.element), List.of(fresh.element)));
		ImpEnhancedWebElement subject = subject(driver.driver, target());

		assertFalse(subject.isExist(0));
		assertTrue(subject.isExist(0));

		assertEquals(driver.findCalls.get(), 2, "failed display lookup should clear the cached stale element");
		assertEquals(stale.displayChecks.get(), 1);
		assertEquals(fresh.displayChecks.get(), 1);
	}

	@Test
	public void getElementChoosesFirstVisibleCandidate() {
		ElementBehavior hidden = new ElementBehavior(false, 0, 0, 0);
		ElementBehavior visible = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(hidden.element, visible.element)));
		ImpEnhancedWebElement subject = subject(driver.driver, target());

		WebElement resolved = subject.getElement(0);

		assertSame(resolved, visible.element);
		assertEquals(hidden.displayChecks.get(), 1);
		assertEquals(visible.displayChecks.get(), 1);
		assertEquals(driver.findCalls.get(), 1);
	}

	@Test
	public void getElementsFallsBackToNextLocatorWhenPrimaryDoesNotMatch() {
		ElementBehavior fallback = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(), List.of(fallback.element)));
		EnhancedBy selector = new EnhancedBy().byId("missing", "target").byCss(".fallback", "target");
		ImpEnhancedWebElement subject = subject(driver.driver, selector);

		List<WebElement> resolved = subject.getElements();

		assertEquals(resolved.size(), 1);
		assertSame(resolved.get(0), fallback.element);
		assertEquals(driver.findCalls.get(), 2);
		assertEquals(driver.locators.get(0).toString(), By.id("missing").toString());
		assertEquals(driver.locators.get(1).toString(), By.cssSelector(".fallback").toString());
	}

	@Test
	public void repeatedCountUsesCachedDomResolution() {
		ElementBehavior first = new ElementBehavior(true, 0, 0, 0);
		ElementBehavior second = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(first.element, second.element)));
		ImpEnhancedWebElement subject = subject(driver.driver, target());

		assertEquals(subject.count(), 2);
		assertEquals(subject.count(), 2);

		assertEquals(driver.findCalls.get(), 1, "stable element lists should not re-query the DOM on every count");
	}

	@Test
	public void waitForElementToLoadRetriesUntilRequiredCountArrives() {
		ElementBehavior first = new ElementBehavior(true, 0, 0, 0);
		ElementBehavior second = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(first.element), List.of(first.element, second.element)));
		AbstractDriverJunit.setWebDriver(driver.driver);
		WaitHelper wait = new TestWaitHelper();

		boolean loaded = wait.waitForElementToLoad(target(), 1, 2);

		assertTrue(loaded);
		assertTrue(driver.findCalls.get() >= 2, "wait should poll again while the required element count has not arrived");
	}

	@Test
	public void waitForElementToLoadReturnsFalseWhenRequiredCountNeverArrives() {
		ElementBehavior only = new ElementBehavior(true, 0, 0, 0);
		DriverHarness driver = new DriverHarness(List.of(List.of(only.element)));
		AbstractDriverJunit.setWebDriver(driver.driver);
		WaitHelper wait = new TestWaitHelper();

		assertFalse(wait.waitForElementToLoad(target(), 0, 2));
	}

	private static EnhancedBy target() {
		return new EnhancedBy().byId("target", "target");
	}

	private static ImpEnhancedWebElement subject(WebDriver driver, EnhancedBy target) {
		return new ImpEnhancedWebElement(null, 0, driver, target);
	}

	private static final class TestWaitHelper extends WaitHelper {
		@Override
		public void waitAllJSRequests(int time) {
			// Unit tests exercise Selenium polling only; framework JS readiness is covered by consumer tests.
		}
	}

	private static final class ElementBehavior implements InvocationHandler {
		private final boolean displayed;
		private final int staleClickFailures;
		private final int staleSendKeyFailures;
		private final int staleDisplayFailures;
		private final AtomicInteger clicks = new AtomicInteger();
		private final AtomicInteger sendKeys = new AtomicInteger();
		private final AtomicInteger displayChecks = new AtomicInteger();
		private final WebElement element;

		private ElementBehavior(boolean displayed, int staleClickFailures, int staleSendKeyFailures,
				int staleDisplayFailures) {
			this.displayed = displayed;
			this.staleClickFailures = staleClickFailures;
			this.staleSendKeyFailures = staleSendKeyFailures;
			this.staleDisplayFailures = staleDisplayFailures;
			this.element = proxy(WebElement.class, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
			case "click":
				if (clicks.incrementAndGet() <= staleClickFailures)
					throw new StaleElementReferenceException("stale click");
				return null;
			case "sendKeys":
				if (sendKeys.incrementAndGet() <= staleSendKeyFailures)
					throw new StaleElementReferenceException("stale sendKeys");
				return null;
			case "isDisplayed":
				if (displayChecks.incrementAndGet() <= staleDisplayFailures)
					throw new StaleElementReferenceException("stale display");
				return displayed;
			case "isEnabled":
				return true;
			case "clear":
			case "submit":
				return null;
			case "toString":
				return "TestWebElement";
			default:
				return defaultValue(method.getReturnType());
			}
		}
	}

	private static final class DriverHarness implements InvocationHandler {
		private final Deque<List<WebElement>> responses = new ArrayDeque<>();
		private final AtomicInteger findCalls = new AtomicInteger();
		private final List<By> locators = new ArrayList<>();
		private final WebDriver.Timeouts timeouts;
		private final WebDriver.Options options;
		private final WebDriver driver;

		private DriverHarness(List<List<WebElement>> responses) {
			this.responses.addAll(responses);
			this.timeouts = proxy(WebDriver.Timeouts.class, (proxy, method, args) -> {
				if (WebDriver.Timeouts.class.isAssignableFrom(method.getReturnType()))
					return proxy;
				return defaultValue(method.getReturnType());
			});
			this.options = proxy(WebDriver.Options.class, (proxy, method, args) -> {
				if (method.getName().equals("timeouts"))
					return timeouts;
				return defaultValue(method.getReturnType());
			});
			this.driver = proxy(WebDriver.class, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
			case "findElements":
				findCalls.incrementAndGet();
				locators.add((By) args[0]);
				if (responses.isEmpty())
					return List.of();
				if (responses.size() == 1)
					return responses.peekFirst();
				return responses.removeFirst();
			case "manage":
				return options;
			case "quit":
				return null;
			case "toString":
				return "TestWebDriver";
			default:
				return defaultValue(method.getReturnType());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
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
