package core.uiCore.drivers;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.openqa.selenium.WebDriver;

/**
 * Tracks the last implicit wait applied to each driver so repeated assignments do
 * not become redundant remote WebDriver commands. The map uses weak keys so a
 * completed driver session does not remain strongly referenced by the tracker.
 */
public final class DriverTimeoutManager {

	private static final Map<WebDriver, Duration> IMPLICIT_WAITS = Collections.synchronizedMap(new WeakHashMap<>());

	private DriverTimeoutManager() {
	}

	/**
	 * Applies an implicit wait only when it differs from the currently tracked
	 * value.
	 *
	 * @return true when a WebDriver timeout command was sent, false when the write
	 *         was redundant and skipped
	 */
	public static boolean setImplicitWait(WebDriver driver, Duration timeout) {
		if (driver == null || timeout == null)
			return false;

		synchronized (driver) {
			Duration current = IMPLICIT_WAITS.get(driver);
			if (timeout.equals(current))
				return false;

			driver.manage().timeouts().implicitlyWait(timeout);
			IMPLICIT_WAITS.put(driver, timeout);
			return true;
		}
	}

	/**
	 * Returns the tracked implicit wait, or the caller supplied fallback when this
	 * driver has not yet been observed by the manager.
	 */
	public static Duration getImplicitWait(WebDriver driver, Duration fallback) {
		if (driver == null)
			return fallback;
		Duration timeout = IMPLICIT_WAITS.get(driver);
		return timeout == null ? fallback : timeout;
	}

	/**
	 * Seeds the tracked value without sending a WebDriver command. Useful when a
	 * driver was configured by code outside this manager.
	 */
	public static void rememberImplicitWait(WebDriver driver, Duration timeout) {
		if (driver != null && timeout != null)
			IMPLICIT_WAITS.put(driver, timeout);
	}

	static void clearTrackedTimeoutsForTests() {
		IMPLICIT_WAITS.clear();
	}
}
