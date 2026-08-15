package core.uiCore.drivers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import core.helpers.WaitPollingPolicy;
import core.support.configReader.Config;
import core.support.listeners.TestListener;
import core.support.objects.TestObject;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.ImpEnhancedWebElement;

public class RemoteWebDriverPerformanceTest {

	private static final String TEST_ID = "RemoteWebDriverPerformanceTest-unit";

	@BeforeMethod
	public void setUp() {
		TestListener.isTestNG = false;
		TestObject.currentTestId.set(TEST_ID);
		TestObject.testInfo.put(TEST_ID, new TestObject().withTestId(TEST_ID));
		TestObject.testInfo.put(TestObject.DEFAULT_TEST, new TestObject().withTestId(TestObject.DEFAULT_TEST));
		TestObject.getTestInfo().log = LogManager.getLogger(TEST_ID);
		Config.putValue("global.timeoutSeconds", "1", false);
		Config.putValue("global.timeout.implicit.Seconds", "2", false);
		DriverTimeoutManager.clearTrackedTimeoutsForTests();
		AbstractDriverJunit.setWebDriver(null);
	}

	@AfterMethod
	public void cleanUp() {
		System.clearProperty(WaitPollingPolicy.POLLING_MILLISECONDS);
		System.clearProperty(WaitPollingPolicy.REMOTE_APPIUM_POLLING_MILLISECONDS);
		DriverTimeoutManager.clearTrackedTimeoutsForTests();
		AbstractDriverJunit.setWebDriver(null);
		TestObject.currentTestId.remove();
		TestObject.currentTestName.remove();
		TestObject.testInfo.clear();
		TestListener.isTestNG = false;
	}

	@Test
	public void duplicateImplicitWaitWritesAreSuppressed() {
		DriverHarness driver = new DriverHarness(List.of(), null);

		assertTrue(DriverTimeoutManager.setImplicitWait(driver.driver, Duration.ofSeconds(2)));
		assertEquals(DriverTimeoutManager.setImplicitWait(driver.driver, Duration.ofSeconds(2)), false);
		assertTrue(DriverTimeoutManager.setImplicitWait(driver.driver, Duration.ofMillis(1)));
		assertEquals(DriverTimeoutManager.setImplicitWait(driver.driver, Duration.ofMillis(1)), false);

		assertEquals(driver.timeoutWrites.get(), 2,
				"same-value implicit waits must not become extra remote WebDriver commands");
	}

	@Test
	public void singleLocatorResolutionDoesNotMutateImplicitTimeout() {
		ElementBehavior element = new ElementBehavior(true);
		DriverHarness driver = new DriverHarness(List.of(List.of(element.element)), null);

		subject(driver.driver, target()).getElements();

		assertEquals(driver.findCalls.get(), 1);
		assertEquals(driver.timeoutWrites.get(), 0,
				"single-locator lookup should not restore a timeout it never changed");
	}

	@Test
	public void multiLocatorFallbackUsesOneScopedTimeoutPair() {
		ElementBehavior element = new ElementBehavior(true);
		DriverHarness driver = new DriverHarness(List.of(List.of(), List.of(element.element)), null);
		EnhancedBy selector = new EnhancedBy().byId("missing", "target").byCss(".fallback", "target");

		subject(driver.driver, selector).getElements();

		assertEquals(driver.findCalls.get(), 2);
		assertEquals(driver.timeoutWrites.get(), 2,
				"multi-locator lookup should issue one temporary timeout and one restore only");
		assertEquals(driver.timeoutDurations, List.of(Duration.ofMillis(1), Duration.ofSeconds(2)));
	}

	@Test
	public void existenceListCheckUsesOneScopedTimeoutPair() {
		ElementBehavior first = new ElementBehavior(false);
		ElementBehavior second = new ElementBehavior(true);
		DriverHarness driver = new DriverHarness(List.of(List.of(first.element, second.element)), null);

		assertTrue(subject(driver.driver, target()).isExist());

		assertEquals(driver.findCalls.get(), 1);
		assertEquals(driver.timeoutWrites.get(), 2,
				"list existence must not recursively toggle the implicit timeout for every element");
		assertEquals(first.displayChecks.get(), 1);
		assertEquals(second.displayChecks.get(), 1);
	}

	@Test
	public void nativeClickSkipsWebScrollTimeoutPrecheck() {
		ElementBehavior element = new ElementBehavior(true);
		DriverHarness driver = new DriverHarness(List.of(List.of(element.element)), null);

		subject(driver.driver, target()).click();

		assertEquals(element.clicks.get(), 1);
		assertEquals(driver.findCalls.get(), 1);
		assertEquals(driver.timeoutWrites.get(), 0,
				"native click should not mutate implicit timeout just to run a disabled web-scroll precheck");
	}

	@Test
	public void cloudAppiumUsesThreeHundredMillisecondPollingByDefault() {
		Capabilities cloudAppium = new ImmutableCapabilities(Map.of(
				"mode", "cloud",
				"platformName", "android",
				"automationName", "UiAutomator2"));
		DriverHarness driver = new DriverHarness(List.of(), cloudAppium);

		assertEquals(WaitPollingPolicy.getPollingMilliseconds(driver.driver), 300);
	}

	@Test
	public void nonCloudDriverPreservesHistoricPollingDefault() {
		Capabilities local = new ImmutableCapabilities(Map.of("platformName", "android", "automationName", "UiAutomator2"));
		DriverHarness driver = new DriverHarness(List.of(), local);

		assertEquals(WaitPollingPolicy.getPollingMilliseconds(driver.driver), 5);
	}

	@Test
	public void remoteAppiumPollingIsConfigurable() {
		Capabilities cloudAppium = new ImmutableCapabilities(Map.of(
				"appium:mode", "cloud",
				"platformName", "android",
				"appium:automationName", "UiAutomator2"));
		DriverHarness driver = new DriverHarness(List.of(), cloudAppium);
		Config.putValue(WaitPollingPolicy.REMOTE_APPIUM_POLLING_MILLISECONDS, "450", false);

		assertEquals(WaitPollingPolicy.getPollingMilliseconds(driver.driver), 450);
	}

	private static EnhancedBy target() {
		return new EnhancedBy().byId("target", "target");
	}

	private static ImpEnhancedWebElement subject(WebDriver driver, EnhancedBy target) {
		return new ImpEnhancedWebElement(null, 0, driver, target);
	}

	private static final class ElementBehavior implements InvocationHandler {
		private final boolean displayed;
		private final AtomicInteger clicks = new AtomicInteger();
		private final AtomicInteger displayChecks = new AtomicInteger();
		private final WebElement element;

		private ElementBehavior(boolean displayed) {
			this.displayed = displayed;
			this.element = proxy(new Class<?>[] { WebElement.class }, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
			case "click":
				clicks.incrementAndGet();
				return null;
			case "isDisplayed":
				displayChecks.incrementAndGet();
				return displayed;
			case "isEnabled":
				return true;
			case "toString":
				return "PerformanceWebElement";
			default:
				return defaultValue(method.getReturnType());
			}
		}
	}

	private static final class DriverHarness implements InvocationHandler {
		private final Deque<List<WebElement>> responses = new ArrayDeque<>();
		private final AtomicInteger findCalls = new AtomicInteger();
		private final AtomicInteger timeoutWrites = new AtomicInteger();
		private final List<Duration> timeoutDurations = new ArrayList<>();
		private final Capabilities capabilities;
		private final WebDriver.Timeouts timeouts;
		private final WebDriver.Options options;
		private final WebDriver driver;

		private DriverHarness(List<List<WebElement>> responses, Capabilities capabilities) {
			this.responses.addAll(responses);
			this.capabilities = capabilities;
			this.timeouts = proxy(new Class<?>[] { WebDriver.Timeouts.class }, (proxy, method, args) -> {
				if (method.getName().equals("implicitlyWait")) {
					timeoutWrites.incrementAndGet();
					timeoutDurations.add((Duration) args[0]);
					return proxy;
				}
				if (WebDriver.Timeouts.class.isAssignableFrom(method.getReturnType()))
					return proxy;
				return defaultValue(method.getReturnType());
			});
			this.options = proxy(new Class<?>[] { WebDriver.Options.class }, (proxy, method, args) -> {
				if (method.getName().equals("timeouts"))
					return timeouts;
				return defaultValue(method.getReturnType());
			});
			Class<?>[] interfaces = capabilities == null
					? new Class<?>[] { WebDriver.class }
					: new Class<?>[] { WebDriver.class, HasCapabilities.class };
			this.driver = proxy(interfaces, this);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
			case "findElements":
				findCalls.incrementAndGet();
				if (responses.isEmpty())
					return List.of();
				if (responses.size() == 1)
					return responses.peekFirst();
				return responses.removeFirst();
			case "manage":
				return options;
			case "getCapabilities":
				return capabilities;
			case "quit":
				return null;
			case "toString":
				return "PerformanceWebDriver";
			case "hashCode":
				return System.identityHashCode(proxy);
			case "equals":
				return proxy == args[0];
			default:
				return defaultValue(method.getReturnType());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<?>[] interfaces, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(RemoteWebDriverPerformanceTest.class.getClassLoader(), interfaces, handler);
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
