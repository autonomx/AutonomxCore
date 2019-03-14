package core.uiCore.driverProperties.capabilities;


/**
 */
import java.io.IOException;
import java.util.logging.Level;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.support.configReader.Config;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.driverProperties.driverType.DriverType;

public class WebCapability {

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

		capabilities.setCapability("recordVideo", false);
		capabilities.setCapability("takesScreenshot", true);

		LoggingPreferences logs = new LoggingPreferences();
		logs.enable(LogType.DRIVER, Level.SEVERE);

		capabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);

		return this;
	}

	public DesiredCapabilities getCapability() {
		return capabilities;
	}

	public WebCapability setChromeBrowserLanguage(String lang) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--lang=" + lang);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return this;
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