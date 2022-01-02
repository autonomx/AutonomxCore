package core.uiCore.drivers;

import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;

import core.support.listeners.TestListener;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

public class AbstractDriver {

	public AbstractDriver() {
	}

	/**
	 * global timeout in seconds
	 */
	public static int TIMEOUT_SECONDS = CrossPlatformProperties.getGlobalTimeout();
	public static int TIMEOUT_IMPLICIT_SECONDS = CrossPlatformProperties.getGlobalTimeoutImplicitWait();

	public void setupDriver(DriverObject driverObject) throws Exception {
		if (isJunit()) {
			AbstractDriverJunit abstractDriver = new AbstractDriverJunit();
			abstractDriver.setupWebDriver(driverObject);
		} else if (isTestNG()) {
			AbstractDriverTestNG.setupWebDriver(driverObject);
		}
	}

	public static WebDriver getWebDriver() {
		if (isJunit())
			return AbstractDriverJunit.getWebDriver();
		else if (isTestNG())
			return AbstractDriverTestNG.getWebDriver();
		else
			return null;
	}

	public static void setWebDriver(WebDriver webDriver) {
		if (isJunit())
			AbstractDriverJunit.setWebDriver(webDriver);
		else if (isTestNG())
			AbstractDriverTestNG.setWebDriver(webDriver);

	}

	public static ThreadLocal<ExtentTest> getStep() {
		if (isJunit()) {
			return AbstractDriverJunit.step;
		} else {
			return AbstractDriverTestNG.step;
		}
	}

	public static Boolean isFirstRun() {
		return TestObject.isFirstRun();
	}

	public static Map<WebDriver, DriverObject> getDriverList() {
		return DriverObject.driverList;
	}

	public static boolean isJunit() {
		if (isTestNG())
			return false;
		return true;

	}

	/**
	 * check if testng is the runner
	 * 
	 * @return
	 */
	public static boolean isTestNG() {
		try {
			if (TestListener.isTestNG)
				return true;
		} catch (NoClassDefFoundError e) {
			return false;
		}
		return false;
	}
}
