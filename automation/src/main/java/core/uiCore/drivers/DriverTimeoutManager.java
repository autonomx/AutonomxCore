package core.uiCore.drivers;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.openqa.selenium.WebDriver;

import io.appium.java_client.AppiumDriver;

/**
 * Tracks the last implicit wait applied to each driver so repeated assignments do
 * not become redundant remote WebDriver commands. The map uses weak keys so a
 * completed driver session does not remain strongly referenced by the tracker.
 */
public final class DriverTimeoutManager {

	private static final Map<WebDriver, Duration> IMPLICIT_WAITS = Collections.synchronizedMap(new WeakHashMap<>());

	// restores recorded by deferRestore, applied lazily by flushPendingRestore
	private static final Map<WebDriver, Duration> PENDING_RESTORES = Collections.synchronizedMap(new WeakHashMap<>());

	// nesting depth of temporary-timeout blocks per driver
	private static final Map<WebDriver, Integer> TEMPORARY_DEPTH = Collections.synchronizedMap(new WeakHashMap<>());

	private DriverTimeoutManager() {
	}

	/**
	 * Appium element lookup is already driven by the framework's explicit polling.
	 * Keeping an implicit wait on a mobile session makes every failed lookup block
	 * inside the driver before the next poll can run.
	 */
	public static boolean isMobileDriver(WebDriver driver) {
		return driver instanceof AppiumDriver;
	}

	/**
	 * Configures the ambient implicit wait for a new session. Browser sessions keep
	 * their caller-supplied value; Appium sessions are always pinned to zero.
	 */
	public static void configureImplicitWait(WebDriver driver, Duration browserTimeout) {
		if (driver == null)
			return;
		setImplicitWait(driver, isMobileDriver(driver) ? Duration.ZERO : browserTimeout);
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

	/**
	 * The implicit wait the driver should return to once temporary overrides are
	 * restored: the deferred restore when one is pending, otherwise the tracked
	 * value.
	 */
	public static Duration getDesiredImplicitWait(WebDriver driver, Duration fallback) {
		if (driver == null)
			return fallback;
		Duration pending = PENDING_RESTORES.get(driver);
		if (pending != null)
			return pending;
		return getImplicitWait(driver, fallback);
	}

	/**
	 * Enters a temporary-timeout block: clears any pending restore (the temporary
	 * value is now the desired driver state) and applies the override. Each call
	 * must be balanced by deferRestore.
	 */
	public static void enterTemporaryTimeout(WebDriver driver, Duration timeout) {
		if (driver == null || timeout == null)
			return;
		PENDING_RESTORES.remove(driver);
		TEMPORARY_DEPTH.merge(driver, 1, Integer::sum);
		setImplicitWait(driver, timeout);
	}

	/**
	 * Records the restore without sending a WebDriver command. It is applied by
	 * flushPendingRestore before the next find that relies on the ambient
	 * implicit wait, so back-to-back temporary overrides cost no remote commands.
	 */
	public static void deferRestore(WebDriver driver, Duration timeout) {
		if (driver == null)
			return;
		TEMPORARY_DEPTH.merge(driver, -1, (a, b) -> Math.max(0, a + b));
		if (timeout != null)
			PENDING_RESTORES.put(driver, timeout);
	}

	/**
	 * Applies a deferred restore, unless a temporary-timeout block is still open.
	 * Call before operations that depend on the ambient implicit wait.
	 */
	public static void flushPendingRestore(WebDriver driver) {
		if (driver == null)
			return;
		Integer depth = TEMPORARY_DEPTH.get(driver);
		if (depth != null && depth > 0)
			return;
		Duration pending = PENDING_RESTORES.remove(driver);
		if (pending != null)
			setImplicitWait(driver, pending);
	}

	static void clearTrackedTimeoutsForTests() {
		IMPLICIT_WAITS.clear();
		PENDING_RESTORES.clear();
		TEMPORARY_DEPTH.clear();
	}
}
