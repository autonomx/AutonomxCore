package core.uiCore.driverProperties.capabilities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.logger.TestLog;
import core.support.objects.DeviceManager;
import core.support.objects.DeviceObject.DeviceType;
import core.support.objects.TestObject;
import core.uiCore.AppiumServer;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;


import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * @author ehsan.matean
 *
 */
public class AndroidCapability {

	public DesiredCapabilities capabilities;
	public static String APP_DIR_PATH = "android.appDir";
	public static String APP_NAME = "android.app";
	public static String ANDROID_ENGINE = "android.capabilties.automationName";
	public static String UIAUTOMATOR2 = "UiAutomator2";
	public static boolean ANDROID_INIT = false; // first time android is setup
	public static String ANDROID_HOME = "android.home";
	public static String ANDROID_UDID = "android.UDID";

	public static String IS_HYBRID_APP = "appium.isHybridApp";
	public static String CHROME_VERSION = "appium.chromeVersion";
	/** Set true only when recovering a device with a broken UIAutomator2 install. */
	public static String REINSTALL_UIAUTOMATOR2 = "appium.reinstallUiAutomator2";

	private static final String CAPABILITIES_PREFIX = "android.capabilties.";

	public List<String> simulatorList = new ArrayList<String>();
	public static int SYSTEM_PORT = 8200;

	public AndroidCapability() {
		capabilities = new DesiredCapabilities();
	}

	public AndroidCapability withCapability(DesiredCapabilities Capabilities) {
		this.capabilities = Capabilities;
		return this;
	}

	/**
	 * device: property name from property file. eg. device1, device2
	 * 
	 * @param device
	 * @return
	 */
	public AndroidCapability withDevice(String device) {
		this.simulatorList = Config.getValueList(device);
		return this;
	}

	public DesiredCapabilities getCapability() {
		return capabilities;
	}

	public String getAppPath() {
		String appRootPath = Helper.getFullPath(Config.getValue(APP_DIR_PATH));
		String appName = Config.getValue(APP_NAME);
		File appPath = new File(appRootPath, Config.getValue(APP_NAME));
		String app = Config.getValue("android.capabilties.app");

		// no local app configured: use the app capability directly (eg. a cloud
		// device farm app id such as lt://APP...)
		if (appName.isEmpty() && !app.isEmpty())
			return app;

		if (!appPath.exists())
			TestLog.ConsoleLogWarn("app not found at: " + appPath.getAbsolutePath());

		return appPath.getAbsolutePath();
	}

	/**
	 * sets android capabilities values are from maven or properties file maven has
	 * higher priority than properties
	 * 
	 * @return
	 */
	public AndroidCapability withAndroidCapability() {

		// run only once per test run
		uninstallUiAutomator2();

		// sets capabilities from properties files
		capabilities = setAndroidCapabilties();

		// set app path
		capabilities.setCapability("appium:app", getAppPath());

		// download chrome driver if hybrid
		setChromeDriver();

		// sets android home value if not already set in properties
		setAndroidHome();

		// set device using device manager. device manager handles multiple devices in
		// parallel
		setAndroidDevice();

		// set port for appium
		setPort(TestObject.getTestInfo().deviceName);

		// if single signin is set, Then do not reset the app after each test
		setSingleSignIn();

		return this;
	}

	/**
	 * set capabilties with prefix android.capabilties. eg.
	 * android.capabilties.fullReset="false iterates through all property values
	 * with such prefix And adds them to android desired capabilities
	 * 
	 * @return
	 */
	public DesiredCapabilities setAndroidCapabilties() {

		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;

		boolean isCloud = Config.getBooleanValue("appium.isCloud");
		Map<String, Object> cloudOptions = new HashMap<String, Object>();

		// load config/properties values from entries with "android.capabilties." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {
			boolean isAndroidCapability = entry.getKey().toString().startsWith(CAPABILITIES_PREFIX);
			if (isAndroidCapability) {
				String fullKey = entry.getKey().toString();
				String key = fullKey.substring(fullKey.lastIndexOf(".") + 1).trim();
				String value = entry.getValue().toString().trim();

				// on cloud device farms, non-standard capabilities (deviceName, video,
				// isRealMobile ...) go into the vendor options bucket, which the farm
				// interprets the same way as the pre-w3c flat capabilities
				if (isCloud && !isW3cStandardCapability(key) && !key.contains(":")) {
					if (key.equals("deviceName"))
						putCloudDeviceCapability(cloudOptions, value);
					else
						cloudOptions.put(key, value);
				} else
					capabilities.setCapability(toW3cCapabilityName(key), value);
			}
		}

		if (isCloud && !cloudOptions.isEmpty()) {
			setCloudJobNames(cloudOptions);
			capabilities.setCapability(CLOUD_OPTIONS_CAPABILITY, cloudOptions);
		}

		setPerformanceDefaults();

		return capabilities;
	}

	/**
	 * Defaults aimed at stable, already-installed local Appium environments. Every
	 * value remains overridable through the normal android.capabilties.* config
	 * namespace (or by passing a preconfigured DesiredCapabilities instance).
	 * skipServerInstallation/skipDeviceInitialization are intentionally not
	 * defaulted: a fresh device or emulator has no UIAutomator2 server yet, so
	 * skipping installation would fail every session until manually recovered.
	 */
	private void setPerformanceDefaults() {
		setDefaultCapability(capabilities, "appium:waitForIdleTimeout", 0);
		setDefaultCapability(capabilities, "appium:disableWindowAnimation", true);
		setDefaultCapability(capabilities, "appium:skipLogCapture", true);
	}

	/**
	 * Sets a capability only when neither the prefixed nor the unprefixed name was
	 * already supplied by config or a preconfigured capabilities instance.
	 */
	static void setDefaultCapability(DesiredCapabilities capabilities, String name, Object value) {
		String unprefixedName = name.substring(name.indexOf(':') + 1);
		if (capabilities.getCapability(name) == null && capabilities.getCapability(unprefixedName) == null)
			capabilities.setCapability(name, value);
	}

	// vendor options capability for cloud device farm runs (lambdatest)
	public static final String CLOUD_OPTIONS_CAPABILITY = "lt:options";

	// one build name per run, so the farm groups all of the run's sessions
	private static String cloudBuildName = "";

	/**
	 * names the device farm job: the session is named after the running test and
	 * grouped under one build per run (app name + environment + run start time,
	 * eg. "SmartHome-E2E-Android qa 2026-08-15 14:40"). property-file values for
	 * name/build take precedence when declared
	 *
	 * @param cloudOptions vendor options map under construction
	 */
	public static synchronized void setCloudJobNames(Map<String, Object> cloudOptions) {
		cloudOptions.putIfAbsent("name", TestObject.getTestInfo().testName);

		if (cloudBuildName.isEmpty()) {
			Object appName = cloudOptions.get("appname");
			String prefix = (appName == null || appName.toString().isEmpty()) ? "automation" : appName.toString();

			String environment = getEnvironmentName();
			if (!environment.isEmpty())
				prefix += " " + environment;

			cloudBuildName = prefix + " " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
		}
		cloudOptions.putIfAbsent("build", cloudBuildName);
	}

	/**
	 * environment name derived from the config root path, eg.
	 * ./resources/properties/qa -> qa
	 *
	 * @return environment name, or empty if config.root is not set
	 */
	private static String getEnvironmentName() {
		String configRoot = Config.getValue("config.root").replace("\"", "").trim();
		if (configRoot.isEmpty())
			return "";
		String[] segments = configRoot.replace("\\", "/").split("/");
		return segments[segments.length - 1].trim();
	}

	/**
	 * translates the legacy combined device list format "name:version, name:version"
	 * into w3c cloud options: deviceName + platformVersion from the first entry.
	 * the w3c matcher has no working fallback-list equivalent (deviceNames arrays
	 * are accepted but ignored, and the farm then allocates an arbitrary device;
	 * platformVersion arrays are rejected), so remaining entries are dropped
	 *
	 * @param cloudOptions vendor options map under construction
	 * @param value        deviceName value from the property file
	 */
	public static void putCloudDeviceCapability(Map<String, Object> cloudOptions, String value) {
		String[] entries = value.split(",");
		String[] parts = entries[0].trim().split(":");
		cloudOptions.put("deviceName", parts[0].trim());
		if (parts.length > 1)
			cloudOptions.put("platformVersion", parts[1].trim());

		if (entries.length > 1)
			TestLog.ConsoleLog("device fallback list is not supported on w3c cloud sessions. using: "
					+ entries[0].trim());
	}

	/**
	 * @param key capability name from the property file
	 * @return true if the capability is part of the w3c webdriver standard
	 */
	public static boolean isW3cStandardCapability(String key) {
		return key.equals("platformName") || key.equals("browserName") || key.equals("browserVersion")
				|| key.equals("acceptInsecureCerts") || key.equals("pageLoadStrategy") || key.equals("proxy")
				|| key.equals("setWindowRect") || key.equals("timeouts") || key.equals("strictFileInteractability")
				|| key.equals("unhandledPromptBehavior");
	}

	/**
	 * selenium 4 w3c sessions reject capabilities that are neither standard nor
	 * vendor prefixed. capability names from property files are declared without a
	 * prefix, so non-standard names are prefixed with appium:
	 *
	 * @param key capability name from the property file
	 * @return w3c-valid capability name
	 */
	public static String toW3cCapabilityName(String key) {
		if (isW3cStandardCapability(key) || key.contains(":"))
			return key;
		return "appium:" + key;
	}

	/**
	 * download chrome driver if hybrid app is enabled if Version is LATEST,
	 * download latest driver unless set in config
	 */
	public void setChromeDriver() {

		boolean isHybridApp = Config.getBooleanValue(IS_HYBRID_APP);

		if (isHybridApp) {
			String chromeVersion = Config.getValue(CHROME_VERSION);
			
			try {
				// if version is LATEST, download latest
				if (chromeVersion.equals("LATEST"))
					WebDriverManager.chromedriver().setup();
				else {
					WebDriverManager.chromedriver().driverVersion(chromeVersion).setup();
					String chromePath = WebDriverManager.chromedriver().getDownloadedDriverPath();
					capabilities.setCapability("chromedriverExecutable", chromePath);
				}
			} catch (java.lang.NoSuchMethodError er) {
				er.getMessage();
			} catch (Exception e) {
				e.getMessage();
			}

			TestLog.ConsoleLog("setting chrome version: " + chromeVersion);
		}
	}

	/**
	 * runs subsequent tests without restarting the app removes the need to sign in
	 * on every test if tests fail, it will restart the app
	 */
	public void setSingleSignIn() {
		if (CrossPlatformProperties.isSingleSignIn()) {
			if (AbstractDriver.isFirstRun()) {
				capabilities.setCapability("appium:noReset", false);
			} else {
				capabilities.setCapability("appium:noReset", true);
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
		List<String> devices = getAndroidDeviceList();
		for (String device : devices) {
			if (!device.contains("emulator")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * gets the name of the first real device connected
	 * 
	 * @param devices
	 * @return
	 */
	public static List<String> getRealDevices(List<String> devices) {
		ArrayList<String> realDeviceList = new ArrayList<String>();
		for (String device : devices) {
			if (!device.contains("emulator")) {
				realDeviceList.add(device);
			}
		}
		return realDeviceList;
	}

	/**
	 * gets the list of android devices including real devices + emulators skips the
	 * first item, as it is not a device
	 * 
	 * @return device list
	 */
	public static List<String> getAndroidDeviceList() {
		String cmd = "adb devices | tail -n +2 | cut -sf 1";

		if (!Config.getValue(AppiumServer.ANDROID_HOME).isEmpty())
			cmd = Config.getValue(AppiumServer.ANDROID_HOME) + "/platform-tools/" + cmd;

		// get list of device udid
		List<String> deviceList = new ArrayList<String>();
		deviceList = Config.getValueList(ANDROID_UDID);
		// if no device is set in properties, attempt to auto detect
		if (deviceList.isEmpty()) {
			deviceList = Helper.executeCommand(cmd);
		}

		// log device list
		if (!deviceList.isEmpty())
			TestLog.ConsoleLogDebug("Android device list: " + Arrays.toString(deviceList.toArray()));

		return deviceList;
	}

	public List<String> getAndroidRealDeviceList() {
		List<String> devices = getAndroidDeviceList();
		return getRealDevices(devices);
	}

	/**
	 * sets ios device number of devices must be equal or greater than number of
	 * threads for parallel run
	 */
	public void setSimulator() {
		List<String> devices = this.simulatorList;

		if (devices == null || devices.isEmpty())
			Helper.assertFalse("set device first");

		// check if more threads are called than devices under test
		int threads = CrossPlatformProperties.getParallelTests();
		if (threads > devices.size())
			Helper.assertFalse("there are more threads than devices. global.parallelTestCount: " + threads
					+ " devices: " + devices.size());

		// adds all devices
		DeviceManager.loadDevices(devices, DeviceType.Android);
		capabilities.setCapability("avd", DeviceManager.getFirstAvailableDevice(DeviceType.Android));
	}

	/**
	 * if device has port assigned, use assigned port else generate new port number
	 * 
	 * @param deviceName
	 */
	public synchronized void setPort(String deviceName) {

		// cloud device farms manage their own appium ports, and no local device is
		// registered with the device manager
		if (Config.getBooleanValue("appium.isCloud"))
			return;

		// if device port is already set
		if (DeviceManager.devices.get(deviceName) != null && (DeviceManager.devices.get(deviceName).devicePort != -1))
			capabilities.setCapability("appium:systemPort",
					DeviceManager.devices.get(deviceName).devicePort);
		else {
			int systemPort = ++SYSTEM_PORT;
			capabilities.setCapability("appium:systemPort", systemPort);
			DeviceManager.devices.get(deviceName).withDevicePort(systemPort);
		}

		TestLog.ConsoleLog(
				"deviceName " + deviceName + " systemPort: " + DeviceManager.devices.get(deviceName).devicePort);
	}

	public static void restartAdb() {
		Helper.executeCommand("adb kill-server");
		Helper.executeCommand("adb start-server");
	}

	/**
	 * Uninstalls the UIAutomator2 server only when the recovery flag is enabled.
	 * A device or emulator must be connected for the recovery operation.
	 */
	public static void uninstallUiAutomator2() {
		if (!Config.getBooleanValue(REINSTALL_UIAUTOMATOR2))
			return;

		// runs the first time android test is run
		if (!ANDROID_INIT) {
			ANDROID_INIT = true;
			boolean isAndroidConnected = !CollectionUtils.isEmpty(getAndroidDeviceList());

			if (isAndroidConnected && Config.getValue(ANDROID_ENGINE).equals(UIAUTOMATOR2)) {
				TestLog.ConsoleLog("Uninstalling uiautomator2.server");
				TestLog.ConsoleLog("Uninstalling uiautomator2.server.test");

				Helper.executeCommand("adb uninstall io.appium.uiautomator2.server");
				Helper.executeCommand("adb uninstall io.appium.uiautomator2.server.test");
			}
		}
	}

	/**
	 * sets real device
	 * 
	 */
	public void setRealDevices() {
		List<String> devices = getAndroidRealDeviceList();
		int threads = CrossPlatformProperties.getParallelTests();
		if (threads > devices.size())
			Helper.assertFalse(
					"there are more threads than devices. thread count: " + threads + " devices: " + devices.size());

		// adds all devices
		DeviceManager.loadDevices(devices, DeviceType.Android);
		capabilities.setCapability("udid", DeviceManager.getFirstAvailableDevice(DeviceType.Android));
	}

	/**
	 * sets device by following strategy: if device is connected, selects device if
	 * emulator is not specified with "withDevice1() or with Device2() function,
	 * select device 1 by default from properties else select emulator from
	 * properties specified in panel config page
	 */
	public void setAndroidDevice() {
		if (!PropertiesReader.isUsingCloud()) {
			if (isRealDeviceConnected()) {
				setRealDevices();
			} else {
				setSimulator();
			}
		}
	}

	public static void printAndroidHelp(Exception e) {
		String androidError = "It is impossible to create a new session";
		String androidSolution = "*******************************************************************\r\n" + "\r\n"
				+ "\r\n" + "\r\n" + "*******************************************************************\r\n" + "\r\n"
				+ " This could be an environment issue. Try the following solutions:\r\n"
				+ "    1. Try running against appium desktop server\r\n"
				+ "        1. Download And run appium desktop\r\n" + " " + "        2. Start the server\r\n"
				+ "        3. In resources/properties/appium.property, set values\r\n"
				+ "            1. appium.useExternalAppiumServer = true\r\n"
				+ "            2. appium.externalPort  = 4723\r\n" + "            3. run test\r\n"
				+ "    2. Is appium terminal installation correct?\r\n" + " "
				+ "            1. command line: appium\r\n"
				+ "            2. Does it start. If not install: “npm install -g appium”  or “sudo npm install -g appium --unsafe-perm=true --allow-root”\r\n"
				+ "            3. Run against appium terminal\r\n" + "                1. In properties set:\r\n"
				+ "                    1. useExternalAppiumServer = true\r\n"
				+ "                    2. appiumExternalPort = 4723\r\n"
				+ "                2. run test And see if it passes\r\n"
				+ "    3. Turn on debugging in properties at resource folder for more info:\r\n"
				+ "        1. appiumLogging = true\r\n" + "    2. set android home environment in properties\r\n"
				+ "        1. androidHome = \"/Users/username/Library/Android/sdk\"\r\n"
				+ "    4. please download appium doctor https://github.com/appium/appium-doctor\r\n"
				+ "        1. download with command: npm install appium-doctor -g\r\n"
				+ "        2. Run: appium-doctor -android\r\n"
				+ "        3. Ensure the environment is setup properly\r\n" + "        4. Restart eclipse\r\n"
				+ "        *******************************************************************\r\n" + "\r\n" + "\r\n"
				+ "\r\n" + "*******************************************************************";
		if (e.getMessage().contains(androidError)) {
			System.out.println(androidSolution);
		}
	}

	/**
	 * sets the value for android home based on its default location sets android
	 * home based on user.home location on mac checks if the generated android home
	 * location exists, if true, adds to android.home config value
	 */
	public static void setAndroidHome() {

		// return if not on osx
		if (!Helper.isMac())
			return;

		String userHome = System.getProperty("user.home");
		String androidHome = Config.getValue(ANDROID_HOME);
		String javaHomePath = "";
		if (androidHome.isEmpty()) {
			javaHomePath = userHome + File.separator + "Library" + File.separator + "Android" + File.separator + "sdk/";
			boolean isAndroidHome = new File(javaHomePath).exists();
			if (isAndroidHome) {
				Config.putValue(ANDROID_HOME, javaHomePath);
			}
		}
	}
}
