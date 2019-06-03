package core.uiCore.driverProperties.capabilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.remote.DesiredCapabilities;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.logger.TestLog;
import core.support.objects.DeviceManager;
import core.support.objects.DeviceObject.DeviceType;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;


/**
 * @author ehsan.matean
 *
 */
public class IosCapability {

	public DesiredCapabilities capabilities;
	public static String APP_DIR_PATH = "ios.appDir";
	public static String APP_NAME = "ios.app";

	public static String DEVICE_VERSION = "ios.deviceVersion";
	public static String CHROME_VERSION = "appium.chromeVersion";
	public List<String> simulatorList = new ArrayList<String>();

	private static final String CAPABILITIES_PREFIX = "ios.capabilties.";

	public static int WDA_LOCAL_PORT = 8100;

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
	
	/**
	 * device: property name from property file. eg. device1, device2
	 * @param device
	 * @return
	 */
	public IosCapability withDevice(String device) {
		this.simulatorList = Config.getValueList(device);
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
	public IosCapability withIosCapability() {

		// https://github.com/appium/appium
		// user appium desktop app for locator
		
		// sets capabilties from properties files
		capabilities = setiOSCapabilties();

		capabilities.setCapability(MobileCapabilityType.APP, getAppPath());

		//TODO: does not work with ios 11.4. try again in the future
		//	capabilities.setCapability("locationServicesEnabled", true);
		//	capabilities.setCapability("locationServicesAuthorized", true);

		// set chrome version if value set in properties file
		if (!getDriverVersion().equals("DEFAULT")) {
			WebDriverManager.chromedriver().version(getDriverVersion()).setup();
			String chromePath = WebDriverManager.chromedriver().getBinaryPath();
			capabilities.setCapability("chromedriverExecutable", chromePath);
		}

		setIosDevice();
		
		// set port for appium 
		setPort(TestObject.getTestInfo().deviceName);
		
		// does not reset app between tests. on failed tests, it resets app
		setSingleSignIn();

		return this;
	}
	
	/**
	 * set capabilties with prefix ios.capabilties.
	 * eg. ios.capabilties.fullReset="false
	 * iterates through all property values with such prefix And adds them to android desired capabilities
	 * @return 
	 */
	public DesiredCapabilities setiOSCapabilties() {

		// get all keys from config 
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;

		// load config/properties values from entries with "ios.capabilities." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {
			boolean isAndroidCapability = entry.getKey().toString().startsWith(CAPABILITIES_PREFIX);
			if (isAndroidCapability) {
				String fullKey = entry.getKey().toString();
				String key = fullKey.substring(fullKey.lastIndexOf(".") + 1).trim();
				String value = entry.getValue().toString().trim();
				
				capabilities.setCapability(key, value);
			}
		}
		return capabilities;
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
	 * if adb device contains emulator, Then its an emulator otherwise, its a
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

		if (devices == null || devices.isEmpty())
			Helper.assertFalse("set device first");

		int threads = CrossPlatformProperties.getParallelTests();
		if (threads > devices.size())
			Helper.assertFalse(
					"there are more threads than devices. thread count: " + threads + " devices: " + devices.size());

		// adds all devices
		DeviceManager.loadDevices(devices, DeviceType.iOS);
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceManager.getFirstAvailableDevice(DeviceType.iOS));
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
		DeviceManager.loadDevices(devices, DeviceType.iOS);
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, DeviceManager.getFirstAvailableDevice(DeviceType.iOS));
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
	
	/**
	 * if device has port assigned, use assigned port
	 * else generate new port number
	 * @param deviceName
	 */
	public void setPort(String deviceName) {
		
		// if device port is already set
		if(DeviceManager.devices.get(deviceName) != null && (DeviceManager.devices.get(deviceName).devicePort != -1))
			capabilities.setCapability("wdaLocalPort", DeviceManager.devices.get(deviceName).devicePort);
		else {
			int wdaLocalPort = ++WDA_LOCAL_PORT;
			capabilities.setCapability("wdaLocalPort", wdaLocalPort);
			DeviceManager.devices.get(deviceName).withDevicePort(wdaLocalPort);
		}
		
		TestLog.ConsoleLog("deviceName " + deviceName + " wdaLocalPort: " + DeviceManager.devices.get(deviceName).devicePort);
	}
}