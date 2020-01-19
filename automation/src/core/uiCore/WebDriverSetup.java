package core.uiCore;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.microsoft.appcenter.appium.EnhancedAndroidDriver;
import com.microsoft.appcenter.appium.Factory;

import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.uiCore.driverProperties.browserType.BrowserType;
import core.uiCore.driverProperties.driverType.DriverType;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.windows.WindowsDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

public class WebDriverSetup {

	public static String SERVER_URL = "web.remote.server.url";
	public static String SERVER_PORT = "web.remote.server.port";
	public static String LATEST_BROWSER_VERSION = "LATEST";

	/**
	 * get webdriver by type set in properties file
	 * 
	 * @return
	 * @throws IOException
	 */

	@SuppressWarnings("rawtypes")
	public WebDriver getWebDriverByType(DriverObject driverObject) throws IOException {
		WebDriver driver = null;

		DriverType type = driverObject.driverType;

		switch (type) {

		case LOCAL_WEBDRIVER:
			driver = getBrowserDriverByType(driverObject);
			break;
		case REMOTE_WEBDRIVER:
			driver = new RemoteWebDriver(new URL(getServerUrl() + ":" + getServerPort() + "/wd/hub"),
					driverObject.capabilities);
			break;
		case IOS_DRIVER:
			// if external server is used
			AppiumDriverLocalService service;			
			if(Config.getBooleanValue("appium.useExternalAppiumServer"))
			{
				int port = Config.getIntValue("appium.externalPort");
				TestLog.ConsoleLog("Connecting to external appium server at port " + port);
				driver = new IOSDriver(new URL("http://localhost:"+ port + "/wd/hub"), driverObject.capabilities);
			}
			else {
				TestLog.ConsoleLog("Connecting to internal appium server");
				service = AppiumServer.startAppiumServer(driverObject);
			//	driver = new IOSDriver(new URL("http://localhost:4723/wd/hub"), driverObject.capabilities);
				driver = new IOSDriver(service.getUrl(), driverObject.capabilities);
			}
			break;
		case ANDROID_DRIVER:
			
			// if external server is used
			if(Config.getBooleanValue("appium.useExternalAppiumServer"))
			{
				int port = Config.getIntValue("appium.externalPort");
				String server = Config.getValue("appium.externalServer");
				driver = new AndroidDriver(new URL("http://" +server + ":"+ port + "/wd/hub"), driverObject.capabilities);
			}
			// if microsoft app center
			else if (PropertiesReader.isUsingCloud()) {
				EnhancedAndroidDriver<MobileElement> appcenterDriver = Factory
						.createAndroidDriver(new URL("http://localhost:8001/wd/hub"), driverObject.capabilities);
				return appcenterDriver;
			// if internal server is used	
			} else {
				service = AppiumServer.startAppiumServer(driverObject);
				driver = new AndroidDriver(service.getUrl(), driverObject.capabilities);
			}
			break;
		case WINAPP_DRIVER:
		    service = AppiumServer.startAppiumServer(driverObject);
			driver = new WindowsDriver(service.getUrl(),  driverObject.capabilities);
			break;
		default:
			throw new IllegalStateException("Unsupported driverype " + type);
		}
		return driver;
	}

	@SuppressWarnings("deprecation")
	public WebDriver getBrowserDriverByType(DriverObject driverObject) throws IOException {
		WebDriver driver = null;

		BrowserType browserType = driverObject.browserType;

		// set browser version to empty When latest to download the latest version
		if (driverObject.driverVersion != null && driverObject.driverVersion.equals(LATEST_BROWSER_VERSION))
			driverObject.driverVersion = null;

		// print the browser capabilities
		Map<String, Object> cap = driverObject.capabilities.asMap();
		TestLog.ConsoleLog("capabilities: " + Arrays.toString(cap.entrySet().toArray()));
		
		switch (browserType) {
		case FIREFOX:
			setDriverManager(driverObject, WebDriverManager.firefoxdriver());
			driver = new FirefoxDriver(driverObject.capabilities);
			break;
		case FIREFOX_HEADLESS:
			setDriverManager(driverObject, WebDriverManager.firefoxdriver());
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setHeadless(true);
			driverObject.capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
			driver = new FirefoxDriver(driverObject.capabilities);
			break;
		case INTERNET_EXPLORER:
			setDriverManager(driverObject, WebDriverManager.iedriver());
			driver = new InternetExplorerDriver(driverObject.capabilities);
			break;
		case MICROSOFT_EDGE:
			setDriverManager(driverObject, WebDriverManager.edgedriver());
			driver = new EdgeDriver(driverObject.capabilities);
			break;
		case CHROME:
			setDriverManager(driverObject, WebDriverManager.chromedriver());
			driver = new ChromeDriver(driverObject.capabilities);
			break;
		case CHROME_HEADLESS:
			setDriverManager(driverObject, WebDriverManager.chromedriver());
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setHeadless(true);
			driverObject.capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			driver = new ChromeDriver(driverObject.capabilities);
			break;
		case OPERA:
			setDriverManager(driverObject, WebDriverManager.operadriver());
			driver = new OperaDriver(driverObject.capabilities);
			break;
		case SAFARI:
			driver = new SafariDriver(driverObject.capabilities); 
			break;
		default:
			throw new IllegalStateException("Unsupported browsertype " + browserType);
		}
		
		printBrowserVersion(driver);
		return driver;
	}
	
	/**
	 * set driver manager options
	 * values found in web.property config file
	 * @param driverObject
	 * @param manager
	 */
	private void setDriverManager(DriverObject driverObject, WebDriverManager manager) {
		String proxyServer = Config.getValue("proxy.host");
		String proxyPort = Config.getValue("proxy.port");
		String proxyUser = Config.getValue("proxy.user");
		String proxyPassword = Config.getValue("proxy.password");
		boolean isForceCache = Config.getBooleanValue("web.driver.manager.proxy.forceCache");
		int timeout_seconds = Config.getIntValue("web.driver.manager.timeoutSeconds");
		
		// force cache, not checking online
		if(isForceCache)
			manager = manager.forceCache();
		
		manager.proxy(proxyServer + ":" + proxyPort)
		.proxyUser(proxyUser)
		.proxyUser(proxyPassword)
		.version(driverObject.driverVersion)
		.timeout(timeout_seconds)
		.setup();
		
		TestLog.ConsoleLog("using driver version: " + manager.getDownloadedVersion());
	}

	public String getServerUrl() {
		return "http://" + Config.getValue(SERVER_URL);
	}

	public String getServerPort() {
		return Config.getValue(SERVER_PORT);
	}
	
	public void printBrowserVersion(WebDriver driver) {
		if(driver == null) return;
		
		Capabilities caps = ((RemoteWebDriver) driver).getCapabilities();
		String browserName = caps.getBrowserName();
		String browserVersion = caps.getVersion();
		TestLog.ConsoleLog("browser name: '" + browserName + "' browser version: "+ browserVersion);	
	}
}