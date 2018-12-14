package core.uiCore.driverProperties.capabilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.remote.DesiredCapabilities;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.logger.TestLog;
import core.support.objects.DeviceObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;

public class IosCapability {

	public DesiredCapabilities capabilities;
	public static String APP_DIR_PATH = "ios_app_dir";
	public static String APP_NAME = "iosApp";
	public static String DEVICES1 = "iosDevices1";
	public static String DEVICES2 = "iosDevices2";

	public static String DEVICE_VERSION = "iosDeviceVersion";
	public static String DEVICE_ORIENTATION = "iosOrientation";
	public static String FULL_RESET = "iosFullReset";
	public static String NO_RESET = "iosNoReset";
	public static String WAIT_FOR_QUIESCENCE = "iosWaitForQuiescence";
	public static String USE_NEW_WDA = "iosUseNewWDA";
	public static String CLEAR_SYSTEM_FILES = "iosclearSystemFiles";
	public static String SHOULD_USE_SINGLETON_TEST_MANAGEMENT = "iosShouldUseSingletonTestManager";
	public static String SHOULD_USE_TEST_MANAGER_FOR_VISIBILITY_DETECTION = "iosShouldUseTestManagerForVisibilityDetection";
	public static String CHROME_VERSION = "appiumChromeVersion";
	public List<String> simulatorList = new ArrayList<String>();;

	public static AtomicInteger wdaLocalPort = new AtomicInteger(8100);

	public IosCapability() {
		capabilities = new DesiredCapabilities();
	}

	public String getDriverVersion() {
		String value = Config.getValue(CHROME_VERSION);
		return value;
	}

	public IosCapability withCapability(DesiredCapabilities Capabilities) {
		this.capabilities = Capabilities;
		return this;
	}

	public DesiredCapabilities getCapability() {
		return capabilities;
	}

	public IosCapability withDevice1() {
		this.simulatorList = Config.getValueList(DEVICES1);
		// setAndroidDevice();
		return this;
	}

	public IosCapability withDevice2() {
		this.simulatorList = Config.getValueList(DEVICES2);
		return this;
	}

	public String getAppPath() {
		String appRootPath = PropertiesReader.getLocalRootPath() + Config.getValue(APP_DIR_PATH);
		File appPath = new File(appRootPath, Config.getValue(APP_NAME));
		
		if(!appPath.exists())
			TestLog.ConsoleLogWarn("app not found at: " + appPath.getAbsolutePath());
		
		return appPath.getAbsolutePath();
	}

	/**
	 * sets ios capabilities values are from maven or properties file maven has
	 * higher priority than properties
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public IosCapability withIosCapability() {

		// https://github.com/appium/appium
		// user appium desktop app for locator

		capabilities.setCapability(MobileCapabilityType.APP, getAppPath());

		capabilities.setCapability(MobileCapabilityType.PLATFORM, "iOS");
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, Config.getValue(DEVICE_VERSION));
		capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);

		// capabilities.setCapability(MobileCapabilityType.ORIENTATION,
		// config.getStringProperty(DEVICE_ORIENTATION));

		// app reset controls
	
		capabilities.setCapability("fullReset", Config.getValue(FULL_RESET));
		
		capabilities.setCapability("waitForQuiescence", Config.getValue(WAIT_FOR_QUIESCENCE));
		capabilities.setCapability("shouldUseSingletonTestManager", Config.getValue(SHOULD_USE_SINGLETON_TEST_MANAGEMENT));
		capabilities.setCapability("clearSystemFiles", Config.getValue(CLEAR_SYSTEM_FILES));
		capabilities.setCapability("shouldUseTestManagerForVisibilityDetection",
				Config.getValue(SHOULD_USE_TEST_MANAGER_FOR_VISIBILITY_DETECTION));
	
		//TODO: does not work with ios 11.4. try again in the future
		//	capabilities.setCapability("locationServicesEnabled", true);
		//	capabilities.setCapability("locationServicesAuthorized", true);
	
		capabilities.setCapability("useNewWDA", Config.getValue(USE_NEW_WDA));
	
		capabilities.setCapability("wdaLocalPort", wdaLocalPort.incrementAndGet());
		TestLog.ConsoleLog(wdaLocalPort.get() + "");

		// set chrome version if value set in properties file
		if (!getDriverVersion().equals("DEFAULT")) {
			WebDriverManager.chromedriver().version(getDriverVersion()).setup();
			String chromePath = WebDriverManager.chromedriver().getBinaryPath();
			capabilities.setCapability("chromedriverExecutable", chromePath);
		}

		setIosDevice();
		// does not reset app between tests. on failed tests, it resets app
		setSingleSignIn();

		return this;
	}

	/**
	 * runs subsequent tests without restarting the app removes the need to sign in
	 * on every test if tests fail, it will restart the app
	 */
	public void setSingleSignIn() {
		if (CrossPlatformProperties.isSingleSignIn()) {
			if (AbstractDriver.isFirstRun()) {
				capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
			} else {
				capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
			}
		}
	}

	/**
	 * if adb device contains emulator, then its an emulator otherwise, its a
	 * connected real device
	 * 
	 * @param devices
	 * @return
	 */
	public boolean isRealDeviceConnected() {
		ArrayList<String> devices = getIosDeviceList();
		if (!devices.isEmpty())
			return true;
		return false;
	}

	/**
	 * gets the list of android devices including real devices + emulators require
	 * idevice installed: brew install ideviceinstaller
	 * 
	 * @return
	 */
	public static ArrayList<String> getIosDeviceList() {
		String cmd = "idevice_id -l";
		// TODO: test out
		// "xcrun simctl list | grep Booted"
		// system_profiler SPUSBDataType | sed -n -E -e '/(iPhone|iPad)/,/Serial/s/
		// *Serial Number: *(.+)/\1/p'

		ArrayList<String> results = Helper.runShellCommand(cmd);
		if (!results.isEmpty() && results.get(0).contains("command not found"))
			Helper.assertFalse("idevice not installed. install: brew install ideviceinstaller");
		TestLog.ConsoleLog("ios device list: " + Arrays.toString(results.toArray()));
		return results;
	}

	/**
	 * sets ios device number of devices must be equal or greater than number of
	 * threads for parallel run
	 */
	public void setSimulator() {
		List<String> devices = this.simulatorList;

		// set default devices from properties
		if (devices.isEmpty())
			devices = Config.getValueList(DEVICES1);

		if (devices == null || devices.isEmpty())
			Helper.assertFalse("set device first");

		// set default devices from properties
		if (devices.isEmpty())
			devices = Config.getValueList(DEVICES1);

		int threads = CrossPlatformProperties.getParallelTests();
		if (threads > devices.size())
			Helper.assertFalse(
					"there are more threads than devices. thread count: " + threads + " devices: " + devices.size());

		// adds all devices
		DeviceObject.loadDevices(devices);
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceObject.getFirstAvailableDevice());
	}

	/**
	 * sets real device
	 * 
	 */
	public void setRealDevices() {
		List<String> devices = getIosDeviceList();

		int threads = CrossPlatformProperties.getParallelTests();
		if (threads > devices.size())
			Helper.assertFalse(
					"there are more threads than devices. thread count: " + threads + " devices: " + devices.size());

		// adds all devices
		DeviceObject.loadDevices(devices);
		capabilities.setCapability("udid", DeviceObject.getFirstAvailableDevice());
	}

	/**
	 * sets device by following strategy: if device is connected, selects device if
	 * emulator is not specified with "withDevice1() or with Device2() function,
	 * select device 1 by default from properties else select emulator from
	 * properties specified in panel config page
	 */
	public void setIosDevice() {
		if (!PropertiesReader.isUsingCloud()) {
			if (isRealDeviceConnected()) {
				setRealDevices();
			} else {
				setSimulator();
			}
		}
	}
}