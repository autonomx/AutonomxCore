package core.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import core.logger.PropertiesReader;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;

public class Capabilities {

	public DesiredCapabilities capabilities;
	
	public Capabilities() {
		capabilities = new DesiredCapabilities();
	}
	
	public DesiredCapabilities getCapability() {
		return capabilities;
	}
	
	public Capabilities withCapability(DesiredCapabilities Capabilities) {
		this.capabilities = Capabilities;
		return this;
	}
	
	public Capabilities withBrowserName(String browser) {
		capabilities.setBrowserName(browser);
		return this;
	}
	
	public Capabilities withApp(String app) {
		File classpathRoot = new File(System.getProperty("user.dir"));
		File appDir = new File(classpathRoot, "../apps");
		File appPath = new File(appDir, app);
		capabilities.setCapability(MobileCapabilityType.APP, appPath.getAbsolutePath());
		return this;
	}
	
	public Capabilities withDeviceName(String device) {
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, device);
		return this;
	}
	
	public Capabilities withPlatformVersion(String device) {
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, device);
		return this;
	}
	
	public Capabilities withPlatformName(String name) {
		capabilities.setCapability("platformName", name);
		return this;
	}
	
	public Capabilities withFullReset(boolean isReset) {
		capabilities.setCapability("fullReset", isReset);
		return this;
	}
	
	public Capabilities withNoReset(boolean isReset) {
		capabilities.setCapability("noReset", isReset);
		return this;
	}
	
	public Capabilities withAvd(String avd) {
		capabilities.setCapability("avd", avd);
		return this;
	}
	
		
	/**
	 * sets capability for web based apps
	 * 
	 * @return
	 * @throws IOException
	 */
	public Capabilities withDefaultBrowserCapability() {

		System.setProperty("webdriver.chrome.args", "--disable-logging");
		System.setProperty("webdriver.chrome.silentOutput", "true");

		// set phantomjs capabilities
		ArrayList<String> cliArgsCap = new ArrayList<String>();
		cliArgsCap.add("--web-security=false");
		cliArgsCap.add("--ssl-protocol=any");
		cliArgsCap.add("--ignore-ssl-errors=true");
		capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);
		capabilities.setCapability("recordVideo", false);
		capabilities.setCapability("takesScreenshot", true);

		LoggingPreferences logs = new LoggingPreferences();
		logs.enable(LogType.DRIVER, Level.SEVERE);

		capabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);

		try {
			capabilities.setBrowserName(PropertiesReader.getBrowser());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * sets capability of ios based apps
	 * 
	 * @return
	 */
	public Capabilities withDefaultIosCapability() {
		// https://github.com/appium/appium
		// user appium desktop app for locator

		capabilities.setCapability("platformName", "iOS");
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "11.0");
		capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "iPhone 7");
		//capabilities.setCapability(MobileCapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
		//capabilities.setCapability("orientation", ScreenOrientation.LANDSCAPE);
		//capabilities.setCapability("bundleId", "com.conquermobile.HeadCheck");
		capabilities.setCapability("fullReset", false);
		capabilities.setCapability("noReset", false);

		return this;
	}
	
	public Capabilities withDefaultAndroidCapability() {


		capabilities.setCapability("device", "Android");

		// mandatory capabilities
		capabilities.setCapability("deviceName", "Android");
		capabilities.setCapability("platformName", "Android");
		capabilities.setCapability("automationName", "UiAutomator2");
		capabilities.setCapability("fullReset", false);
		capabilities.setCapability("noReset", false);
		capabilities.setCapability("session-override", true);
		capabilities.setCapability("avd","Pixel_XL_API_25");
		capabilities.setCapability("unicodeKeyboard", true);
		capabilities.setCapability("resetKeyboard", true);

		return this;
	}
}