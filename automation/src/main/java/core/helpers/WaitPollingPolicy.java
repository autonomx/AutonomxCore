package core.helpers;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

import core.support.configReader.Config;

/**
 * Chooses explicit-wait polling intervals without slowing local/browser runs.
 *
 * <p>Defaults preserve the historic 5 ms cadence for non-cloud drivers, while
 * Appium sessions that advertise {@code mode=cloud} use 300 ms to avoid flooding
 * remote providers with commands. Both values are configurable through the normal
 * Core config map or matching JVM system properties.</p>
 */
public final class WaitPollingPolicy {

	public static final String POLLING_MILLISECONDS = "global.wait.pollingMilliseconds";
	public static final String REMOTE_APPIUM_POLLING_MILLISECONDS = "global.wait.remoteAppiumPollingMilliseconds";
	static final int DEFAULT_POLLING_MILLISECONDS = 5;
	static final int DEFAULT_REMOTE_APPIUM_POLLING_MILLISECONDS = 300;

	private WaitPollingPolicy() {
	}

	public static int getPollingMilliseconds(WebDriver driver) {
		Integer globalOverride = configuredPositiveInt(POLLING_MILLISECONDS);
		if (globalOverride != null)
			return globalOverride;

		if (isRemoteAppium(driver)) {
			Integer remoteOverride = configuredPositiveInt(REMOTE_APPIUM_POLLING_MILLISECONDS);
			return remoteOverride == null ? DEFAULT_REMOTE_APPIUM_POLLING_MILLISECONDS : remoteOverride;
		}

		return DEFAULT_POLLING_MILLISECONDS;
	}

	static boolean isRemoteAppium(WebDriver driver) {
		if (!(driver instanceof HasCapabilities))
			return false;

		Capabilities capabilities = ((HasCapabilities) driver).getCapabilities();
		if (capabilities == null)
			return false;

		String mode = capability(capabilities, "mode");
		if (!"cloud".equalsIgnoreCase(mode))
			return false;

		String automationName = capability(capabilities, "automationName");
		String platformName = capability(capabilities, "platformName");
		boolean mobilePlatform = "android".equalsIgnoreCase(platformName) || "ios".equalsIgnoreCase(platformName);
		return mobilePlatform || !automationName.isEmpty();
	}

	private static String capability(Capabilities capabilities, String name) {
		Object value = capabilities.getCapability(name);
		if (value == null)
			value = capabilities.getCapability("appium:" + name);
		return value == null ? "" : value.toString().trim();
	}

	private static Integer configuredPositiveInt(String key) {
		String systemValue = System.getProperty(key);
		Integer parsedSystemValue = parsePositiveInt(systemValue);
		if (parsedSystemValue != null)
			return parsedSystemValue;

		Object configValue = Config.getObjectValue(key);
		return parsePositiveInt(configValue == null ? null : configValue.toString());
	}

	private static Integer parsePositiveInt(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			int parsed = Integer.parseInt(value.trim());
			return parsed > 0 ? parsed : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
