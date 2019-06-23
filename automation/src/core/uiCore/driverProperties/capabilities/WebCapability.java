package core.uiCore.driverProperties.capabilities;


/**
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.support.configReader.Config;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.driverProperties.driverType.DriverType;

public class WebCapability {

	private static final String CHROME_OPTIONS_PREFIX = "chrome.options";
	private static final String FIREFOX_OPTIONS_PREFIX = "firefox.options";

	private static final String CHROME_PREF_PREFIX = "chrome.pref";
	private static final String FIREFOX_PREF_PREFIX = "firefox.pref";


	public DesiredCapabilities capabilities;

	DriverType driverType;

	public WebCapability() {
		capabilities = new DesiredCapabilities();
	}

	public WebCapability withCapability(DesiredCapabilities Capabilities) {
		this.capabilities = Capabilities;
		return this;
	}

	/**
	 * sets capability for web based apps
	 * 
	 * @return
	 * @throws IOException
	 */
	public WebCapability withBrowserCapability() {

		System.setProperty("webdriver.chrome.args", "--disable-logging");
		System.setProperty("webdriver.chrome.silentOutput", "true");

		capabilities.setCapability("recordVideo", true);
		capabilities.setCapability("takesScreenshot", true);
		capabilities.setBrowserName(getBrowserName());
		
		// set chrome or firefox options based on prefix
		setOptions();
		
		// set chrome or firefox preferences based on prefix
		setPreferences();
		
		// set logging for browser to severe only
		LoggingPreferences logs = new LoggingPreferences();
		logs.enable(LogType.DRIVER, Level.SEVERE);

		capabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);		
		
		return this;
	}
	
	/**
	 * set preferences with prefix chrome.pref. or firefox.pref.
	 * @return
	 */
	private DesiredCapabilities setPreferences() {

		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;
		ChromeOptions chromeOptions = new ChromeOptions();
		FirefoxOptions firefoxOptions = new FirefoxOptions();
		Map<String, Object> chromePreferences = new HashMap<>();
		
		FirefoxProfile fireFoxProfile = new FirefoxProfile();
		
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
					fireFoxProfile.setPreference(key,value);
				}

			}
		}
		
		
		if (isChrome()) {
			chromeOptions.setExperimentalOption("prefs", chromePreferences);
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		}
		else if (isFirefox()) {
			firefoxOptions.setProfile(fireFoxProfile);
			capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
		}

		return capabilities;
	}

	
	/**
	 * set chrome options with prefix chrome.options
	 * set firefox options with prefix firefox.options
	 * https://peter.sh/experiments/chromium-command-line-switches/
	 * eg. chrome.options="--start-maximized"
	 * iterates through all property values with such prefix And adds them to android desired capabilities
	 * @return 
	 */
	private DesiredCapabilities setOptions() {

		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;
		ChromeOptions chromeOptions = new ChromeOptions();
		FirefoxOptions firefoxOptions = new FirefoxOptions();

		// load config/properties values from entries with "chrome.options." or
		// "firefox.options." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {
			
			// if starts with chrome.options or firefox.options prefix
			boolean isOption = entry.getKey().toString().startsWith(CHROME_OPTIONS_PREFIX) 
					|| entry.getKey().toString().startsWith(FIREFOX_OPTIONS_PREFIX);
			
			if (isOption) {
				String fullKey = entry.getKey().toString();
				String[] split = fullKey.split("options.");
				String key = "--" + split[1].trim();
				boolean isEnable = Boolean.valueOf(entry.getValue().toString().trim());
				if (isEnable && isChrome() && fullKey.contains(CHROME_OPTIONS_PREFIX))
					chromeOptions.addArguments(key);
				else if (isEnable && isFirefox() && fullKey.contains(FIREFOX_OPTIONS_PREFIX)) {
					firefoxOptions.addArguments(key);
				}

			}
		}

		if (isChrome())
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
		else if (isFirefox())
			capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);

		return capabilities;
	}

	public DesiredCapabilities getCapability() {
		return capabilities;
	}

	/**
	 * set url through maven using -D command eg. mvn test -DcraigslistSite =
	 * "www.test.com" priority: 1) maven 2) properties 3) default
	 * 
	 * @return
	 */
	public String getUrl(String app, String defaultUrl) {
		String value = Config.getValue(app);
		if (value.isEmpty())
			value = defaultUrl;
		return value;
	}

	/**
	 * set browser through maven using -D command eg. mvn test -DbrowserType =
	 * "chrome" priority: 1) maven 2) properties 3) default
	 * 
	 * @return
	 */
	public BrowserType getBrowser() {
		String value = Config.getValue("web.browserType");
		return Enum.valueOf(BrowserType.class, value);
	}
	
	public String getBrowserName() {
		String browsername = getBrowser().toString().toLowerCase();
		browsername =  browsername.replace("_", "");
		
		// account for headless browsers
		if(browsername.contains("chrome")) browsername = "chrome";
		if(browsername.contains("firefox")) browsername = "firefox";

		return browsername;
	}
	
	public boolean isChrome() {
		return getBrowserName().equals("chrome");
	}
	
	public boolean isFirefox() {
		return getBrowserName().equals("firefox");
	}

	public String getDriverVersion() {
		String value = Config.getValue("web.driverVersion");
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
}