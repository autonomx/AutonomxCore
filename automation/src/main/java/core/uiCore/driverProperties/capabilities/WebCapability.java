package core.uiCore.driverProperties.capabilities;

/**
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.DriverOption;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.driverProperties.driverType.DriverType;

public class WebCapability {

	private static final String CHROME_OPTIONS_PREFIX = "chrome.options";
	private static final String FIREFOX_OPTIONS_PREFIX = "firefox.options";

	private static final String CHROME_PREF_PREFIX = "chrome.pref";
	private static final String FIREFOX_PREF_PREFIX = "firefox.pref";
	private static final String WEB_CAPABILITIES_PREFIX = "web.capabilities.";

	public DesiredCapabilities capabilities = new DesiredCapabilities();
	public DriverOption options = new DriverOption();

	DriverType driverType;

	/**
	 * sets capability for web based apps
	 * 
	 * @return
	 * @throws IOException
	 */
	public WebCapability withBrowserOption() {
 
		System.setProperty("webdriver.chrome.args", "--disable-logging");
		System.setProperty("webdriver.chrome.silentOutput", "true");

		// set web capabilities based on prefix .capabilities
		setWebCapabilties();

		// set chrome or firefox options based on prefix chrome.options or
		// firefox.options
		setOptions();

		// set chrome or firefox preferences based on prefix chrome.pref or firefox.pref
		setPreferences();
		
		// disable webdriver logging
		disableLogs();
			
		return this;
	}
	
	public void disableLogs() {
		if(isFirefox()) {
			System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
		}
	}

	/**
	 * set capabilties with prefix android.capabilties. eg.
	 * android.capabilties.fullReset="false iterates through all property values
	 * with such prefix And adds them to android desired capabilities
	 * 
	 * @return
	 */
	public void setWebCapabilties() {
		

		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;
		propertiesMap.put("web.capabilities.recordVideo", "true");
		propertiesMap.put("web.capabilities.takesScreenshot", "true");
		propertiesMap.put("web.capabilities.browserName", getBrowserName());
		propertiesMap.put("web.capabilities.name", TestObject.getTestInfo().testName);
		
		// load config/properties values from entries with "android.capabilties." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {
			boolean isWebCapability = entry.getKey().toString().startsWith(WEB_CAPABILITIES_PREFIX);
			if (isWebCapability) {
				String fullKey = entry.getKey().toString();
				String key = fullKey.substring(fullKey.lastIndexOf(".") + 1).trim();
				String value = entry.getValue().toString().trim();

				capabilities.setCapability(key, value);
				options.getChromeOptions().setCapability(key, value);
				options.getFirefoxOptions().setCapability(key, value);
				options.getEdgeOptions().setCapability(key, value);
				options.getSafariOptions().setCapability(key, value);
				options.getOperaOptions().setCapability(key, value);
				options.getInternetExplorerOptions().setCapability(key, value);
			}
		}
	}

	/**
	 * set preferences with prefix chrome.pref. or firefox.pref.
	 * 
	 * @return
	 */
	private void setPreferences() {

		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;
		Map<String, Object> chromePreferences = new HashMap<>();

		FirefoxProfile fireFoxProfile = null;
		if(isFirefox())
			fireFoxProfile = new FirefoxProfile();

		// load config/properties values from entries with "chrome.pref." or
		// "firefox.pref." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {

			// if starts with chrome.pref or firefox.pref prefix
			boolean isPref = entry.getKey().toString().startsWith(CHROME_PREF_PREFIX)
					|| entry.getKey().toString().startsWith(FIREFOX_PREF_PREFIX);

			if (isPref) {
				String fullKey = entry.getKey().toString();
				String[] split = fullKey.split("pref.");
				String key = split[1].trim();
				String value = entry.getValue().toString().trim();
				if (isChrome() && fullKey.contains(CHROME_PREF_PREFIX)) {
					chromePreferences.put(key, value);
				}

				else if (isFirefox() && fullKey.contains(FIREFOX_PREF_PREFIX)) {
					fireFoxProfile.setPreference(key, value);
				}

			}
		}

		if (isChrome()) {
			options.getChromeOptions().setExperimentalOption("prefs", chromePreferences);
		} else if (isFirefox()) {
			options.getFirefoxOptions().setProfile(fireFoxProfile);
		}
	}

	/**
	 * set chrome options with prefix chrome.options set firefox options with prefix
	 * firefox.options https://peter.sh/experiments/chromium-command-line-switches/
	 * eg. options eg: chrome.options = [--headless] [user-agent=test-user-agent]
	 * with such prefix And adds them to android desired capabilities
	 * 
	 * @return
	 */
	private void setOptions() {

		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;

		// load config/properties values from entries with "chrome.options." or
		// "firefox.options." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {

			// if starts with chrome.options or firefox.options prefix
			boolean isOption = entry.getKey().toString().startsWith(CHROME_OPTIONS_PREFIX)
					|| entry.getKey().toString().startsWith(FIREFOX_OPTIONS_PREFIX);

			// options eg: chrome.options = [--headless] [user-agent=test-user-agent]
			if (isOption) {
				List<String> optionsList = new ArrayList<String>();
				String fullKey = entry.getKey().toString();
				String[] keywords = StringUtils.substringsBetween(entry.getValue().toString(), "[", "]");
				for(String keyword : keywords) {		
					if (isChrome() && fullKey.contains(CHROME_OPTIONS_PREFIX)) {
						options.getChromeOptions().addArguments(keyword);
						optionsList.add(keyword);
					}
					else if (isFirefox() && fullKey.contains(FIREFOX_OPTIONS_PREFIX)) {
						options.getFirefoxOptions().addArguments(keyword);
						optionsList.add(keyword);					
					}
				}
				
				// log options
				if( keywords.length > 0)
					TestLog.ConsoleLog("browser options" + Arrays.toString(optionsList.toArray()));
			}
		}

	}
	
	public DesiredCapabilities getCapability() {
		return capabilities;
	}
	
	public DriverOption getDriverOption() {
		return options;
	}

	/**
	 * set url through maven using -D command eg. mvn test -DcraigslistSite =
	 * "www.test.com" priority: 1) maven 2) properties 3) default
	 * 
	 * @return
	 */
	public String getUrl(String app, String defaultUrl) {
		String value = Config.getValue(app);
		if(defaultUrl.isEmpty())
			return value;
		return defaultUrl;
	}

	/**
	 * set browser through maven using -D command eg. mvn test -DbrowserType =
	 * "chrome" priority: 1) maven 2) properties 3) default
	 * 
	 * @return
	 */
	public static BrowserType getBrowser() {
		String value = Config.getValue("web.browserType");
		return Enum.valueOf(BrowserType.class, value);
	}

	public static String getBrowserName() {
		String browsername = getBrowser().toString().toLowerCase();
		browsername = browsername.replace("_", "");

		// account for headless browsers
		if (browsername.contains("chrome"))
			browsername = "chrome";
		else if (browsername.contains("firefox"))
			browsername = "firefox";
		else if (browsername.contains("microsoftedge"))
			browsername = "MicrosoftEdge";

		return browsername;
	}

	public static boolean isChrome() {
		return getBrowserName().equals("chrome");
	}

	public static boolean isFirefox() {
		return getBrowserName().equals("firefox");
	}
	
	public static boolean isInternetExplorer() {
		return getBrowserName().equals("internet explorer");
	}
	
	public static boolean isSafari() {
		return getBrowserName().equals("safari");
	}
	
	public static boolean isMicrosoftEdge() {
		return getBrowserName().equals("MicrosoftEdge");
	}

	public String getDriverVersion() {
		String value = Config.getValue("web.driver.manager.version");
		return value;
	}

	/**
	 * set driver through maven using -D command eg. mvn test -DdriverType = "local
	 * webdriver" priority: 1) maven 2) properties 3) default
	 * 
	 * @return
	 */
	public DriverType getWebDriverType() {
		String value = Config.getValue("web.webdriverType");
		return Enum.valueOf(DriverType.class, value);
	}
	
	/**
	 * catch errors where browser version is not detected properly 
	 * @param e
	 * @return
	 */
	public static boolean printWebDriverVersionHelp(Exception e) {
		if(e.getMessage().contains("This version of")
				&& e.getMessage().contains("only supports")
				&& e.getMessage().contains("version")) {
			
			// get shortened error message
			String[] message = e.getMessage().split("\n");
			String shortErrorMessage = StringUtils.EMPTY;
			if(message.length >1)
				shortErrorMessage = message[0];
			
			TestLog.logWarning("\n\n" + shortErrorMessage + "\n\nLooks like webdriver manager is not able to find the new browser version \n\n"
					+ "*** Please try setting browser version manually at web.driver.manager.version at web.property ***\n\n"
					+ "Alternatively, you can set the webdriver manual path at web.driver.manual.path at web.property ***\n\n");			
			return true;
		}
		return false;
	}
}