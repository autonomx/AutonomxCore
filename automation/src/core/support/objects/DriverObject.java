package core.support.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.support.logger.TestLog;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.capabilities.AndroidCapability;
import core.uiCore.driverProperties.capabilities.IosCapability;
import core.uiCore.driverProperties.capabilities.WebCapability;
import core.uiCore.driverProperties.capabilities.WinAppCapabilities;
import core.uiCore.drivers.AbstractDriver;

/**
 * @author CAEHMAT
 *
 *         driverList
 * 
 *         driver testId isAvailable ...
 * 
 *         A 1 false B 2 false C 1 true
 */
public class DriverObject {

	public List<WebDriver> webdriver;
	public Boolean isAvailable = true;
	public List<String> testIdList; // keys for testObject
	public String initialURL = "";
	public String app = "";
	public DriverType driverType;
	public BrowserType browserType;
	public String driverVersion;

	public DesiredCapabilities capabilities;

	public static Map<WebDriver, DriverObject> driverList = new ConcurrentHashMap<WebDriver, DriverObject>();

	public synchronized static WebDriver getFirstAvailableDriver() {
		for (Entry<WebDriver, DriverObject> entry : driverList.entrySet()) {
			if (entry.getValue().isAvailable.equals(true)) {
				entry.getValue().withIsAvailable(false);
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * quite all drivers associated with a test
	 */
	public static void quitTestDrivers() {
		List<WebDriver> drivers = TestObject.getTestInfo().webDriverList;
		for (WebDriver driver : drivers) {
			quitWebDriver(driver);
		}
	}

	/**
	 * quits webdriver if it's running
	 */
	public static void quitWebDriver(WebDriver driver) {

		if (driver != null && driverList.get(driver) != null) {
			TestObject.getTestInfo().withIsFirstRun(true);
			TestObject.getTestInfo().withIsForcedRestart(true);
			try {
				boolean hasQuit = driver.toString().contains("(null)");
				if (!hasQuit)
					driver.quit();
				driverList.remove(driver);
				AbstractDriver.setWebDriver(null); // set driver to null so starts fresh with next run
			} catch (Exception e) {
				e.getMessage();
			}
			TestLog.ConsoleLog("quitting test: " + TestObject.getTestInfo().testName);
		}
	}

	// quites all drivers
	public static void quitAllDrivers() {
		for (Entry<WebDriver, DriverObject> entry : driverList.entrySet()) {
			try {
				boolean hasQuit = entry.getKey().toString().contains("(null)");
				if (!hasQuit)
					entry.getKey().quit();
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}

	/**
	 * add driver to driver list with test info
	 * 
	 * @param driver
	 * @param testId
	 */
	public static void initializeDriverList(DriverObject driver, String testId) {
		if (AbstractDriver.getWebDriver() != null) {
			driver.withTestId(testId);
			if (DriverObject.driverList.get(AbstractDriver.getWebDriver()) == null)
				DriverObject.driverList.put(AbstractDriver.getWebDriver(), driver); // associate driver with driver
																					// object
		}
	}

	public static void setDriverAvailabiltity(WebDriver driver, boolean isAvailable) {
		driverList.get(driver).withIsAvailable(isAvailable);
	}

	public DriverObject withTestId(String testId) {
		if (AbstractDriver.getWebDriver() == null || driverList.get(AbstractDriver.getWebDriver()) == null) {
			testIdList = new ArrayList<String>();
			this.testIdList.add(testId);
		} else
			getCurrentDriverObject().testIdList.add(testId);
		return this;
	}

	public static String getCurrentTestId() {
		List<String> testIdList = driverList.get(AbstractDriver.getWebDriver()).testIdList;

		// TestLog.ConsoleLog("getCurrentTestId: " +
		// Arrays.toString(testIdList.toArray()) + " chrome: " +
		// AbstractDriver.getWebDriver());
		return testIdList.get(testIdList.size() - 1);
	}

	/**
	 * returns the previous test id of the test ran on the driver
	 * 
	 * @return
	 */
	public static String getPreviousTestId() {
		List<String> testIdList = driverList.get(AbstractDriver.getWebDriver()).testIdList;
		if (testIdList.size() > 1)
			return testIdList.get(testIdList.size() - 2);
		return null;
	}

	public DriverObject withApp(String app) {
		this.app = app;
		return this;
	}

	public DriverObject withIsAvailable(Boolean isAvailable) {
		getCurrentDriverObject().isAvailable = isAvailable;
		return this;
	}

	public DriverObject withUrl(String initialURL) {
		this.initialURL = initialURL;
		return this;
	}

	public DriverObject withBrowserType(BrowserType browserType) {
		this.browserType = browserType;
		return this;
	}

	public DriverObject withDriverVersion(String driverVersion) {
		this.driverVersion = driverVersion;
		return this;
	}

	public DriverObject withDriverType(DriverType driverType) {
		this.driverType = driverType;
		return this;
	}

	public DriverObject withCapabilities(DesiredCapabilities capabilities) {
		this.capabilities = capabilities;
		return this;
	}

	public DriverObject getCurrentDriverObject() {
		if (AbstractDriver.getWebDriver() == null || driverList.get(AbstractDriver.getWebDriver()) == null)
			return this;
		else
			return driverList.get(AbstractDriver.getWebDriver());
	}

	public DriverObject withChromeLanguage(String locale) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--lang=" + locale);
		this.capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return this;
	}
	
	public DriverObject withWebDriver(String APP, String URL) {
		WebCapability capability = new WebCapability().withBrowserCapability();
		
		return new DriverObject().withApp(APP).withDriverType(capability.getWebDriverType())
				.withBrowserType(capability.getBrowser()).withDriverVersion(capability.getDriverVersion())
				.withUrl(capability.getUrl(APP, URL))

				.withCapabilities(capability.getCapability());
	}
	
	public DriverObject withiOSDriver(String device) {
		IosCapability capability = new IosCapability().withDevice(device).withIosCapability();
		return new DriverObject().withDriverType(DriverType.IOS_DRIVER).withCapabilities(capability.getCapability());
	}
	
	public DriverObject withAndroidDriver(String device) {
		AndroidCapability capability = new AndroidCapability().withDevice(device).withAndroidCapability();
		return new DriverObject().withDriverType(DriverType.ANDROID_DRIVER).withCapabilities(capability.getCapability());
	}
	
	public DriverObject withWinDriver() {
		WinAppCapabilities capability = new WinAppCapabilities().withWinAppdCapability();		
		return new DriverObject()
				.withDriverType(DriverType.WINAPP_DRIVER)
				.withCapabilities(capability.getCapability());	
	}

}