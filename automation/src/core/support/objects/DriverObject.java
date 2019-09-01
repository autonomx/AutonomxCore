package core.support.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.helpers.Helper;
import core.support.logger.TestLog;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.driverProperties.capabilities.AndroidCapability;
import core.uiCore.driverProperties.capabilities.IosCapability;
import core.uiCore.driverProperties.capabilities.WebCapability;
import core.uiCore.driverProperties.capabilities.WinAppCapabilities;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
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
	public String initialURL = StringUtils.EMPTY;
	public String app = StringUtils.EMPTY;
	public DriverType driverType;
	public BrowserType browserType;
	public String driverVersion;
	public LoginObject login = new LoginObject();	

	public DesiredCapabilities capabilities;

	public static Map<WebDriver, DriverObject> driverList = new ConcurrentHashMap<WebDriver, DriverObject>();

	/**
	 * keeps track of all the drivers and selects the first available driver
	 * used when single sign in is used
	 * @return
	 */
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
		List<WebDriver> drivers =  new ArrayList<>(TestObject.getTestInfo().webDriverList);
		for (WebDriver driver : drivers) {
			quitWebDriver(driver);	
		}
		// reset driver list
		TestObject.getTestInfo().withWebDriverList(new ArrayList<WebDriver>());
	}

	/**
	 * quits webdriver if it's running
	 */
	public static void quitWebDriver(WebDriver driver) {
		TestLog.ConsoleLog("quitting test: " + TestObject.getTestInfo().testName);

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
		}
		
		// remove from testObject driver list
		List<WebDriver> currentTestDrivers = new ArrayList<>(TestObject.getTestInfo().webDriverList);
		if(currentTestDrivers.contains(driver)) {
			TestObject.getTestInfo().webDriverList.remove(driver);
		}
	}
	
	public static void shutDownDriver(boolean isTestPass) {
		if(isTestPass) {
			// shutdown drivers if single sign in is false, else shutdown all except active driver
			if (!CrossPlatformProperties.isSingleSignIn())
				DriverObject.quitTestDrivers();
			else
				shutdownSingleSignInDrivers();
		}else {
			// quits web driver no matter the situation, as new browser will be launched
			DriverObject.quitTestDrivers();	
		}
	}
	
	/**
	 * will quite all drivers except for the current driver
	 * a test could have multiple drivers initiated.
	 * we will only take the active driver to be used for next test
	 */
	private static void shutdownSingleSignInDrivers() {
		if (!CrossPlatformProperties.isSingleSignIn()) return;
		List<WebDriver> currentTestDrivers = new ArrayList<>(TestObject.getTestInfo().webDriverList);
		WebDriver activeDriver = AbstractDriver.getWebDriver();
		for(WebDriver driver : currentTestDrivers) {
			if(!driver.equals(activeDriver))
				quitWebDriver( driver);
		}
	}

	// quites all drivers
	public static void quitAllDrivers() {
		for (Entry<WebDriver, DriverObject> entry : driverList.entrySet()) {
			try {
				boolean hasQuit = entry.getKey().toString().contains("(null)");
				if (!hasQuit)
					quitWebDriver(entry.getKey());
			} catch (Exception e) {
				e.getMessage();
			}
		}
		// reset driver list
		driverList = new ConcurrentHashMap<WebDriver, DriverObject>();
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
		this.isAvailable = isAvailable;
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

	public static DriverObject getCurrentDriverObject() {
		if (AbstractDriver.getWebDriver() == null || driverList.get(AbstractDriver.getWebDriver()) == null) {
			Helper.assertFalse("driver object not available");
			return null;
		}
		else
			return driverList.get(AbstractDriver.getWebDriver());
	}

	public DriverObject withChromeLanguage(String locale) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--lang=" + locale);
		this.capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return this;
	}
	
	public DriverObject withWebDriver(String App, String URL) {
		WebCapability capability = new WebCapability().withBrowserCapability();
		
		return new DriverObject().withApp(App).withDriverType(capability.getWebDriverType())
				.withBrowserType(capability.getBrowser()).withDriverVersion(capability.getDriverVersion())
				.withUrl(capability.getUrl(App, URL))

				.withCapabilities(capability.getCapability());
	}
	
	public DriverObject withiOSDriver(String app, String device) {
		IosCapability capability = new IosCapability().withDevice(device).withIosCapability();
		return new DriverObject().withApp(app).withDriverType(DriverType.IOS_DRIVER).withCapabilities(capability.getCapability());
	}
	
	public DriverObject withAndroidDriver(String app, String device) {
		AndroidCapability capability = new AndroidCapability().withDevice(device).withAndroidCapability();
		return new DriverObject().withApp(app).withDriverType(DriverType.ANDROID_DRIVER).withCapabilities(capability.getCapability());
	}
	
	public DriverObject withWinDriver(String app) {
		WinAppCapabilities capability = new WinAppCapabilities().withWinAppdCapability();		
		return new DriverObject()
				.withApp(app)
				.withDriverType(DriverType.WINAPP_DRIVER)
				.withCapabilities(capability.getCapability());	
	}
	
	public DriverObject withApiDriver(String app) {
		return new DriverObject().withApp(app).withDriverType(DriverType.API);
	}
	
	public DriverObject withGenericDriver(String app) {
		return new DriverObject().withApp(app).withDriverType(DriverType.API);
	}

}