package core.helpers;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import io.appium.java_client.Location;
import org.openqa.selenium.remote.RemoteWebElement;

import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.HidesKeyboard;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.MultiTouchAction;
import io.appium.java_client.PerformsTouchActions;

import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.imagecomparison.OccurrenceMatchingResult;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.SupportsContextSwitching;
import io.appium.java_client.remote.SupportsLocation;
import io.appium.java_client.touch.LongPressOptions;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;
import io.appium.java_client.windows.WindowsDriver;

@SuppressWarnings({ "rawtypes",  "deprecation" })
public class MobileHelper {

	public enum DIRECTION {
		DOWN, UP, LEFT, RIGHT;
	}

	private static final String DISMISS_IF_BLOCKING = "ios.keyboard.dismissIfBlocking";
	private static final String DISMISS_BY_KEY_PRESS = "ios.keyboard.dismissByKeyPress";
	private static final String DISMISS_STRATEGY = "ios.keyboard.dismiss.Strategy";

	// Device dimensions are stable for the lifetime of a driver session. Keep one
	// value per session so gestures do not issue a window-size command repeatedly.
	// Static: MobileHelper is instantiated by more than one Helper facade.
	private static final Map<WebDriver, Dimension> screenSizes = Collections.synchronizedMap(new WeakHashMap<>());

	// mobile: gesture scripts a session's server rejected as unsupported, so the
	// failed probe is paid once per session instead of on every gesture
	private static final Map<WebDriver, Set<String>> unsupportedGestureScripts = Collections
			.synchronizedMap(new WeakHashMap<>());

	public AppiumDriver getAppiumDriver() {
		return ((AppiumDriver) AbstractDriver.getWebDriver());
	}

	public AndroidDriver getAndroidDriver() {
		return ((AndroidDriver) AbstractDriver.getWebDriver());
	}

	public IOSDriver getiOSDriver() {
		return ((IOSDriver) AbstractDriver.getWebDriver());
	}

	private Dimension getScreenSize() {
		return screenSizes.computeIfAbsent(AbstractDriver.getWebDriver(), driver -> driver.manage().window().getSize());
	}

	/**
	 * hides ios or android keyboard
	 * 
	 */
	public void hideKeyboard() {
		if (isIOS()) {
			dimissIosKeyboard();
		}
	}

	/**
	 * if element is not visible, attempt to hide keyboard
	 * 
	 * @param element
	 */
	public void smartHideKeyboard(EnhancedBy element) {

		// if dismiss keyboard when blocking is enable, proceed
		boolean enableSmartDismissKeyboard = Config.getBooleanValue(DISMISS_IF_BLOCKING);
		if (!enableSmartDismissKeyboard)
			return;

		// if element is not visible, attempt to dismiss keyboard
		if (!Helper.isPresent(element))
			dimissIosKeyboard();
	}

	/**
	 * select enter on android
	 */

	public void pressEnterOnAndroid() {
		if (isAndroid()) {
			getAndroidDriver().pressKey(new KeyEvent(AndroidKey.ENTER));
		}
	}

	/**
	 * resets the app
	 */
	public void resetApp() {
		AppiumDriver driver = getAppiumDriver();
		Object appIdValue = null;
		if (isAndroid()) {
			appIdValue = driver.getCapabilities().getCapability("appium:appPackage");
			if (appIdValue == null)
				appIdValue = driver.getCapabilities().getCapability("appPackage");
		} else if (isIOS()) {
			appIdValue = driver.getCapabilities().getCapability("appium:bundleId");
			if (appIdValue == null)
				appIdValue = driver.getCapabilities().getCapability("bundleId");
		}
		if (appIdValue == null)
			throw new IllegalStateException("Unable to restart app: appPackage/bundleId capability is not available");
		String appId = appIdValue.toString();
		InteractsWithApps appManagement = (InteractsWithApps) driver;
		appManagement.terminateApp(appId);
		appManagement.activateApp(appId);

		TestLog.logPass("I reset the app");
	}

	/**
	 * places the app in background And Then relaunches it
	 */
	public void refreshMobileApp() {
		if (isMobile()) {
			((InteractsWithApps) getAppiumDriver()).runAppInBackground(Duration.ofSeconds(1));
		}
	}

	/**
	 * returns if mobile driver is used
	 * 
	 * @return
	 */
	public boolean isMobile() {
		if (AbstractDriver.getWebDriver() instanceof AppiumDriver) {
			return true;
		}
		return false;
	}

	/**
	 * is ios driver
	 * 
	 * @return
	 */
	public boolean isIOS() {
		if (AbstractDriver.getWebDriver() instanceof IOSDriver) {
			return true;
		}
		return false;
	}

	/**
	 * is ios driver
	 * 
	 * @return
	 */
	public boolean isAndroid() {
		if (AbstractDriver.getWebDriver() instanceof AndroidDriver) {
			return true;
		}
		return false;
	}
	
	/**
	 * is win app driver
	 * 
	 * @return
	 */
	public boolean isWinApp() {
		if (AbstractDriver.getWebDriver() instanceof WindowsDriver) {
			return true;
		}
		return false;
	}

	/**
	 * is web driver
	 * 
	 * @return
	 */
	public boolean isWebDriver() {
		if (isIOS() || isAndroid())
			return false;
		if (AbstractDriver.getWebDriver() instanceof WebDriver) {
			return true;
		}
		return false;
	}

	/**
	 * returns true if browser is ie
	 * 
	 * @return
	 */
	public boolean isIeExplorer() {
		if (isIOS() || isAndroid())
			return false;

		if (!isWebDriver())
			return false;

		String browser = Config.getValue("web.browserType");
		return browser.equals(BrowserType.INTERNET_EXPLORER.name());
	}

	/**
	 * sets gps location on ios simulator
	 * 
	 * @param location
	 */
	public void setLocation(Location location) {
		((SupportsLocation) getAppiumDriver()).setLocation(location);
	}

	/**
	 * ios gesture
	 * https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/ios/ios-xctest-mobile-gestures.md#mobile-scroll
	 * 
	 * @param element
	 */
	@SuppressWarnings("unused")
	@Deprecated
	private void scrollToiOS(EnhancedBy element) {
		if (isIOS()) {
			try {
				EnhancedWebElement targetElement = Element.findElements(element);
				JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
				Map<String, Object> params = new HashMap<>();
				params.put("element", ((RemoteWebElement) targetElement.get(0)).getId());
				params.put("toVisible", "true");
				js.executeScript("mobile: scroll", params);

			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}

	// iOS scroll by object
	@Deprecated
	@SuppressWarnings("unused")
	private boolean swipeToDirection_iOS_XCTest(EnhancedBy element, String direction) {
		try {
			EnhancedWebElement targetElement = Element.findElements(element);
			JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
			HashMap<String, String> swipeObject = new HashMap<String, String>();
			if (direction.equals("d")) {
				swipeObject.put("direction", "down");
			} else if (direction.equals("u")) {
				swipeObject.put("direction", "up");
			} else if (direction.equals("l")) {
				swipeObject.put("direction", "left");
			} else if (direction.equals("r")) {
				swipeObject.put("direction", "right");
			}
			swipeObject.put("element", ((RemoteWebElement) targetElement.get(0)).getId());
			js.executeScript("mobile:swipe", swipeObject);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * does not function properly
	 * 
	 * @param element
	 * @param direction
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private boolean scrollToDirection_iOS_XCTest(EnhancedBy element, String direction) {
		// The main difference from swipe call with the same argument is that scroll
		// will try to move
		// the current viewport exactly to the next/previous page (the term "page" means
		// the content,
		// which fits into a single device screen)
		try {
			EnhancedWebElement targetElement = Element.findElements(element);
			JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
			HashMap<String, String> scrollObject = new HashMap<String, String>();
			if (direction.equals("d")) {
				scrollObject.put("direction", "down");
			} else if (direction.equals("u")) {
				scrollObject.put("direction", "up");
			} else if (direction.equals("l")) {
				scrollObject.put("direction", "left");
			} else if (direction.equals("r")) {
				scrollObject.put("direction", "right");
			}
			scrollObject.put("element", ((RemoteWebElement) targetElement.get(0)).getId());
			scrollObject.put("toVisible", "true"); // optional But needed sometimes
			js.executeScript("mobile:scroll", scrollObject);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * swipe right on the screen
	 */
	public void mobile_swipeRight(double durationInSeconds) {
		swipe(null, 0, DIRECTION.RIGHT, durationInSeconds);
	}

	/**
	 * swipe right on the screen
	 */
	public void mobile_swipeRight(EnhancedBy element, int index, double durationInSeconds) {
		swipe(element, 0, DIRECTION.RIGHT, durationInSeconds);
	}

	/**
	 * swipe right using actions
	 * 
	 * @param durationInSeconds
	 */
	public void mobile_swipeLeft(double durationInSeconds) {
		swipe(null, 0, DIRECTION.LEFT, durationInSeconds);
	}

	/**
	 * swipe left on the screen
	 */
	public void mobile_swipeLeft(EnhancedBy element, int index, double durationInSeconds) {
		swipe(element, 0, DIRECTION.LEFT, durationInSeconds);
	}

	/**
	 * swipe up using actions
	 * 
	 * @param durationInSeconds
	 */
	public void mobile_swipeUp(double durationInSeconds) {
		swipe(null, 0, DIRECTION.UP, durationInSeconds);
	}

	/**
	 * swipe up on the screen
	 */
	public void mobile_swipeUp(EnhancedBy element, int index, double durationInSeconds) {
		swipe(element, 0, DIRECTION.UP, durationInSeconds);
	}

	/**
	 * swipe down using actions
	 * 
	 * @param durationInSeconds
	 */
	public void mobile_swipeDown(double durationInSeconds) {
		swipe(null, 0, DIRECTION.DOWN, durationInSeconds);
	}

	/**
	 * swipe down on the screen
	 */
	public void mobile_swipeDown(EnhancedBy element, int index, double durationInSeconds) {
		swipe(element, 0, DIRECTION.DOWN, durationInSeconds);
	}

	/**
	 * returns the occurrences of image based on partial image
	 * 
	 * @param fullImage
	 * @param partialImage
	 * @return
	 */
	public OccurrenceMatchingResult findImageOccurrence(File fullImage, File partialImage) {
		try {
			return getAppiumDriver().findImageOccurrence(fullImage, partialImage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * set context for android And ios apps
	 * 
	 * @param context
	 */
	public void setAppiumContexts(String context) {
		TestLog.logPass("I set context to '" + context + "'");
		Set<String> contextNames = ((SupportsContextSwitching) getAppiumDriver()).getContextHandles();
		for (String contextName : contextNames) {
			if (contextName.contains(context)) {
				((SupportsContextSwitching) getAppiumDriver()).context(contextName);
				break;
			}
		}
	}

	/**
	 * returns the list of mobile context. eg. webview, native view
	 * 
	 * @return
	 */
	public Set<String> mobile_getContextList() {
		Set<String> contextNames = ((SupportsContextSwitching) getAppiumDriver()).getContextHandles();
		return contextNames;
	}

	public void mobile_switchToNativeView() {
		setAppiumContexts("NATIVE");
	}

	public void mobile_switchToWebView() {
		setAppiumContexts("WEBVIEW");
	}

	public void mobile_switchToView(String view) {
		setAppiumContexts(view);
	}
	
	public void switchWindowsHandle(int index) {
		Set<String> windowHandles = getAppiumDriver().getWindowHandles();
		List<String> windowStrings = new ArrayList<>(windowHandles);
		getAppiumDriver().switchTo().window(windowStrings.get(index));
	}

	public void mobile_longPress(EnhancedBy target, long miliSeconds) {
		int retry = 0;
		// EnhancedWebElement targetElement = Element.findElements(target);
		retry++;
		Helper.wait.waitForElementToLoad(target, 5);
		longPress(target, miliSeconds + (retry * 2));
		Helper.wait.waitForElementToBeRemoved(target, 5);
	}

	/**
	 * long press And expect element
	 * 
	 * @param target
	 * @param miliSeconds
	 * @param expected
	 */
	public void mobile_longPressAndExpect(EnhancedBy target, long miliSeconds, EnhancedBy expected) {
		TestLog.logPass("I click " + target.name);

		Helper.wait.waitForElementToBeClickable(target, 10);

		boolean isExpectedFound = false;
		int targetWaitTimeInSeconds = 5;
		int retry = 0;

		do {
			retry++;
			if (Helper.isPresent(target))
				longPress(target, miliSeconds + (retry * 2));
			Helper.wait.waitForSeconds(0.5);
			isExpectedFound = Helper.wait.waitForElementToBeClickable(expected, targetWaitTimeInSeconds);
		} while (!isExpectedFound && retry < 3);

		AssertHelper.assertTrue("expected element not found: " + expected.name, isExpectedFound);
	}

	/**
	 * TODO: enhance to become longpress And expect presses the target element
	 * 
	 * @param target
	 * @param index
	 * @param miliSeconds
	 * @param expected
	 */
	public void longPress(EnhancedBy target, long miliSeconds) {
		try {
			EnhancedWebElement targetElement = Element.findElements(target);
			if (tryServerSideLongPress(targetElement.get(0), miliSeconds))
				return;
			TouchAction action = new TouchAction((PerformsTouchActions) getAppiumDriver());
			action.longPress(LongPressOptions.longPressOptions()
					.withElement(io.appium.java_client.touch.offset.ElementOption.element(targetElement.get(0)))
					.withDuration(Duration.ofMillis(miliSeconds))).release().perform();
		} catch (Exception e) {
			e.getMessage();
		}
	}
	public void longPress(int x, int y, long miliSeconds) {
		try {
			if (tryServerSideLongPress(x, y, miliSeconds))
				return;
			PointOption point = new PointOption().withCoordinates(x, y);
			TouchAction action = new TouchAction((PerformsTouchActions) getAppiumDriver());
			action.longPress(LongPressOptions.longPressOptions().withPosition(point).withDuration(Duration.ofMillis(miliSeconds))).release().perform();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public void mobile_zoomOut(int zoomLevel) {

		for (int i = 0; i < zoomLevel; i++) {
			mobile_zoom("out");
		}
	}

	/**
	 * zooms out And checks if target level indicator has been reached
	 * 
	 * @param zoomLevel
	 * @param indicator
	 */
	public void mobile_zoomOut(EnhancedBy indicator) {
		int retry = 15;
		while (!Helper.isPresent(indicator) && retry > 0) {
			retry--;
			mobile_zoomOut(1);
			Helper.wait.waitForSeconds(1);
		}
		Helper.wait.waitForSeconds(1);
		TestLog.logPass("I zoom out to " + indicator.name);
	}

	/**
	 * zooms in And checks if target level indicator has been reached
	 * 
	 * @param zoomLevel
	 * @param indicator
	 */
	public void mobile_zoomIn(EnhancedBy indicator) {
		int retry = 10;

		while (!Helper.isPresent(indicator) && retry > 0) {
			retry--;
			mobile_zoomIn(1);
			Helper.wait.waitForSeconds(0.3);
		}
		TestLog.logPass("I zoom in to " + indicator.name);
	}

	public void mobile_zoomIn(int zoomLevel) {

		for (int i = 0; i < zoomLevel; i++) {
			mobile_zoom("in");
		}
	}

	/**
	 * pass in "in" or "out" to zoom in or out
	 * 
	 * @param inOut
	 */
	public void mobile_zoom(String inOut) {
		Dimension size = getScreenSize();
		int screenHeight = size.getHeight();
		int screenWidth = size.getWidth();

		MultiTouchAction multiTouchAction = new MultiTouchAction((PerformsTouchActions) getAppiumDriver());
		TouchAction touchAction0 = new TouchAction((PerformsTouchActions) getAppiumDriver());
		TouchAction touchAction1 = new TouchAction((PerformsTouchActions) getAppiumDriver());

		switch (inOut) {
		case "out":
			touchAction0.press(PointOption.point(screenWidth / 2 + 100, screenHeight / 2 + 100))
					.moveTo(PointOption.point(screenWidth / 2 + 95, screenWidth / 2 + 95)).release();
			touchAction1.press(PointOption.point(screenWidth / 2 - 100, (screenHeight / 2) - 100))
					.moveTo(PointOption.point(screenWidth / 2 - 95, screenWidth / 2 - 95)).release();
			break;
		case "in":
			touchAction0.press(PointOption.point(screenWidth / 2 - 5, screenHeight / 2 - 5))
					.moveTo(PointOption.point(screenWidth / 2 - 10, screenWidth / 2 - 10)).release();
			touchAction1.press(PointOption.point(screenWidth / 2 + 5, (screenHeight / 2) + 5))
					.moveTo(PointOption.point(screenWidth / 2 + 10, screenWidth / 2 + 10)).release();
			break;
		default:
			break;
		}

		multiTouchAction.add(touchAction0).add(touchAction1).perform();
		Helper.wait.waitForSeconds(0.5);
	}

	/** Post-scroll settle time; set 0 to disable. Default 0.5s. */
	public static final String SCROLL_SETTLE_TIME_SECONDS = "mobile.scroll.settleTimeSeconds";

	/**
	 * scrolls down android left side of display
	 */
	public void scrollDown() {
		Dimension size = getScreenSize();
		int pressX = size.width / 8;
		int bottomY = size.height * 3 / 6;
		int topY = size.height / 6;
		scroll(pressX, bottomY, pressX, topY);
		scrollSettle();
	}

	public void scrollDownFromCenter() {
		Dimension size = getScreenSize();
		int pressX = size.width / 2;
		int bottomY = size.height * 4 / 6;
		int topY = size.height / 6;
		scroll(pressX, bottomY, pressX, topY);
		scrollSettle();
	}

	/**
	 * Brief settle so the next hierarchy query does not read a mid-animation UI.
	 * Scroll-until-found loops (eg. mobileScroll, mobile_scrollToElement) check
	 * existence right after each scroll, and with waitForIdleTimeout=0 the driver
	 * no longer waits for idle itself. Shorter than the historic 1s.
	 */
	private void scrollSettle() {
		double settleSeconds = Config.getDoubleValue(SCROLL_SETTLE_TIME_SECONDS);
		if (settleSeconds < 0)
			settleSeconds = 0.5;
		if (settleSeconds > 0)
			Helper.wait.waitForSeconds(settleSeconds);
	}

	/*
	 * don't forget that it's "natural scroll" where fromY is the point where you
	 * press the And toY where you release it
	 */
	public void scroll(int fromX, int fromY, int toX, int toY) {
		if (tryServerSideSwipe(fromX, fromY, toX, toY, 0.5))
			return;

		TouchAction touchAction = new TouchAction((PerformsTouchActions) AbstractDriver.getWebDriver());
		touchAction.longPress(LongPressOptions.longPressOptions().withPosition(PointOption.point(fromX, fromY)))
				.moveTo(PointOption.point(toX, toY)).release().perform();

	}

	/**
	 * Uses Appium's server-side gesture endpoints where available. They perform
	 * the full gesture in one command and avoid the client-side TouchAction chain.
	 * Returning false keeps older Appium/XCUITest versions on the compatible
	 * TouchAction fallback.
	 */
	private boolean tryServerSideLongPress(WebElement element, long durationMillis) {
		if (!(element instanceof RemoteWebElement))
			return false;

		Map<String, Object> args = new HashMap<>();
		args.put("elementId", ((RemoteWebElement) element).getId());
		return dispatchLongPress(args, durationMillis);
	}

	private boolean tryServerSideLongPress(int x, int y, long durationMillis) {
		Map<String, Object> args = new HashMap<>();
		args.put("x", x);
		args.put("y", y);
		return dispatchLongPress(args, durationMillis);
	}

	// platform dispatch lives once: Android takes milliseconds, iOS fractional seconds
	private boolean dispatchLongPress(Map<String, Object> args, long durationMillis) {
		if (isAndroid()) {
			args.put("duration", durationMillis);
			return executeMobileScript("mobile: longClickGesture", args);
		}
		if (isIOS()) {
			args.put("duration", durationMillis / 1000.0);
			return executeMobileScript("mobile: touchAndHold", args);
		}
		return false;
	}

	/**
	 * Coordinate-exact server-side drag: the gesture starts and ends exactly where
	 * the caller asked, so element-anchored swipes and edge-of-screen scrolls keep
	 * their anchor (the direction-only swipe endpoints act on the screen center).
	 */
	private boolean tryServerSideSwipe(int fromX, int fromY, int toX, int toY, double durationSec) {
		if (fromX == toX && fromY == toY)
			return false;

		if (isAndroid()) {
			Map<String, Object> args = new HashMap<>();
			args.put("startX", fromX);
			args.put("startY", fromY);
			args.put("endX", toX);
			args.put("endY", toY);
			return executeMobileScript("mobile: dragGesture", args);
		}
		if (isIOS()) {
			Map<String, Object> args = new HashMap<>();
			args.put("fromX", fromX);
			args.put("fromY", fromY);
			args.put("toX", toX);
			args.put("toY", toY);
			// duration is the initial press-and-hold; XCUITest needs >= 0.5s to
			// recognize a drag rather than a tap
			args.put("duration", Math.max(0.5, durationSec));
			return executeMobileScript("mobile: dragFromToForDuration", args);
		}
		return false;
	}

	private boolean executeMobileScript(String script, Map<String, Object> args) {
		WebDriver driver = AbstractDriver.getWebDriver();
		Set<String> unsupported = unsupportedGestureScripts.computeIfAbsent(driver,
				key -> Collections.synchronizedSet(new HashSet<>()));
		if (unsupported.contains(script))
			return false;
		try {
			((JavascriptExecutor) driver).executeScript(script, args);
			return true;
		} catch (UnsupportedCommandException e) {
			unsupported.add(script);
			return false;
		} catch (RuntimeException e) {
			// Appium reports a missing endpoint as a generic WebDriverException;
			// anything else is a genuine gesture failure worth surfacing before the
			// TouchAction fallback retries it
			String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
			if (message.contains("unknown mobile command") || message.contains("not yet been implemented")
					|| message.contains("unknown command") || message.contains("unsupported")) {
				unsupported.add(script);
			} else {
				TestLog.ConsoleLog(script + " failed, falling back to TouchAction: " + e.getMessage());
			}
			return false;
		}
	}

	/**
	 * scrolls to mobile element until element becomes visible
	 * 
	 * @param target
	 */
	public void mobile_scrollToElement(EnhancedBy target) {

		if (isMobile()) {
			int scrollCount = 5;
			while (!Helper.isPresent(target) && scrollCount > 0) {
				scrollDown();
				Helper.wait.waitForElementToLoad(target, 3);
				scrollCount--;
			}
		}
	}

	public void mobile_scrollToElementWithRefresh(EnhancedBy target) {

		if (isMobile()) {
			int scrollCount = 5;
			while (!Helper.isPresent(target) && scrollCount > 0) {
				scrollDown();
				tapAtCenterLeft();
				refreshMobileApp();
				Helper.wait.waitForElementToLoad(target, 3);
				scrollCount--;
			}
		}
	}

	public void mobile_takePicture() {
		getAndroidDriver().pressKey(new KeyEvent(AndroidKey.CAMERA));

	}

	public void mobile_keyCode(int code) {
		getAndroidDriver().pressKey(new KeyEvent(AndroidKey.BACK));
	}

	public void mobile_backButton() {
		getAndroidDriver().pressKey(new KeyEvent(AndroidKey.BACK));
	}
	public void tapAtCenterLeft() {
		Dimension size = getScreenSize();
		int leftX = size.width / 8;
		int centerY = size.height / 2;

		TouchAction touchAction = new TouchAction((PerformsTouchActions) AbstractDriver.getWebDriver());
		touchAction.tap(PointOption.point(leftX, centerY)).perform();
	}
	public void tapAtCenterRight() {
		Dimension size = getScreenSize();
		int leftX = (int) (size.width * 0.95);
		int centerY = size.height / 2;

		TouchAction touchAction = new TouchAction((PerformsTouchActions) AbstractDriver.getWebDriver());
		touchAction.tap(PointOption.point(leftX, centerY)).perform();
	}

	/**
	 * returns the starting position for element based on element being set or not
	 * if element is null, returns default start position
	 * 
	 * @param element
	 * @param index
	 * @param startX
	 * @param startY
	 * @return
	 */
	private Map<String, Integer> setStarterPositionForSwipe(EnhancedBy element, int index, int startX, int startY) {
		Map<String, Integer> coordinates = new HashMap<String, Integer>();
		if (element == null) {
			coordinates.put("x", startX);
			coordinates.put("y", startY);
			return coordinates;
		}

		EnhancedWebElement targetElement = Element.findElements(element);
		Point p = targetElement.get(index).getLocation();
		coordinates.put("x", p.getX());
		coordinates.put("y", p.getY());
		return coordinates;

	}

	/**
	 * swipes to direction specified either from element or from a starting position
	 * in the app
	 * 
	 * @param element
	 * @param index
	 * @param direction
	 * @param durationSec
	 */
	private void swipe(EnhancedBy element, int index, DIRECTION direction, double durationSec) {
		Dimension size = getScreenSize();

		int startX = 0;
		int endX = 0;
		int startY = 0;
		int endY = 0;

		switch (direction) {
		case RIGHT:
			startY = size.height / 2;
			startX = (int) (size.width * 0.90);
			endX = (int) (size.width * 0.05);
			break;

		case LEFT:
			startY = size.height / 2;
			startX = (int) (size.width * 0.05);
			endX = (int) (size.width * 0.90);
			break;

		case UP:
			endY = (int) (size.height * 0.70);
			startY = (int) (size.height * 0.30);
			startX = size.width / 2;
			break;

		case DOWN:
			startY = (int) (size.height * 0.70);
			endY = (int) (size.height * 0.30);
			startX = size.width / 2;
			break;

		default:
			return;
		}

		Map<String, Integer> startPoint = setStarterPositionForSwipe(element, index, startX, startY);
		// horizontal swipes keep the anchor's y for the whole gesture; vertical
		// swipes keep the computed x
		boolean isHorizontal = direction == DIRECTION.LEFT || direction == DIRECTION.RIGHT;
		int targetX = isHorizontal ? endX : startX;
		int targetY = isHorizontal ? startPoint.get("y") : endY;

		if (tryServerSideSwipe(startPoint.get("x"), startPoint.get("y"), targetX, targetY, durationSec))
			return;

		new TouchAction((PerformsTouchActions) getAppiumDriver())
				.press(PointOption.point(startPoint.get("x"), startPoint.get("y")))
				.waitAction(WaitOptions.waitOptions(Duration.ofSeconds((long) durationSec)))
				.moveTo(PointOption.point(targetX, targetY)).release().perform();
	}

	/**
	 * strategies: if no keyboard displayed, return Strategy1: tap outside the
	 * keyboard. just above the keyboard, left side Strategy2: if keys: "Hide
	 * keyboard", "DONE", "Done", "Return", "Next" displayed, click them Strategy3:
	 * if keyboard still exists, use appium.hideKeyboard()
	 */
	private void dimissIosKeyboard() {
		if (isIOS()) {

			// if no keyboard displayed, return
			EnhancedBy KEYBOARD_IOS = Element.byClass("XCUIElementTypeKeyboard", "Keyboard");
			if (!Element.findElements(KEYBOARD_IOS).isExist())
				return;

			String dismissStrategy = Config.getValue(DISMISS_STRATEGY);

			switch (dismissStrategy) {
			case "tapOutside":

				// Strategy1: tap outside the keyboard. just above the keyboard, left side
				EnhancedWebElement targetElement = Element.findElements(KEYBOARD_IOS);
				Point p = targetElement.get(0).getLocation();
				int xPosition = 1;
				int topY = p.getY() - 10;

				// Strategy1: implementation
				TouchAction touchAction = new TouchAction((PerformsTouchActions) AbstractDriver.getWebDriver());
				touchAction.tap(PointOption.point(xPosition, topY)).perform();
				break;

			case "keyPress":

				// TODO: Strategy2 is too slow. isExist takes too long
				// Strategy2: if keys: "Hide keyboard", "DONE", "Done", "Return", "Next"
				// displayed, click them
				List<String> keys = Config.getValueList(DISMISS_BY_KEY_PRESS);
				for (String key : keys) {
					EnhancedBy ios_keys = Element.byAccessibility(key, "keyboard key: " + key);
					if (Element.findElements(ios_keys).isExist()) {
						Helper.clickAndWait(ios_keys, 0);
						break;
					}
				}
				break;

			default:
				((HidesKeyboard) getAppiumDriver()).hideKeyboard();
			}

			if (!Element.findElements(KEYBOARD_IOS).isExist())
				return;

			// Strategy3: if keyboard still exists, use appium.hideKeyboard()
			((HidesKeyboard) getAppiumDriver()).hideKeyboard();
		}
	}
}
