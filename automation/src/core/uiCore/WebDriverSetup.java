package core.uiCore;

import java.io.IOException;
import java.net.URL;

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
				driver = new AndroidDriver(new URL("http://localhost:"+ port + "/wd/hub"), driverObject.capabilities);
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

		switch (browserType) {
		case FIREFOX:
			WebDriverManager.firefoxdriver().version(driverObject.driverVersion).setup();
			driver = new FirefoxDriver(driverObject.capabilities);
			break;
		case FIREFOX_HEADLESS:
			WebDriverManager.firefoxdriver().version(driverObject.driverVersion).setup();
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setHeadless(true);
			driverObject.capabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
			driver = new FirefoxDriver(driverObject.capabilities);
			break;
		case INTERNET_EXPLORER:
			WebDriverManager.iedriver().version(driverObject.driverVersion).setup();
			driver = new InternetExplorerDriver(driverObject.capabilities);
			break;
		case MICROSOFT_EDGE:
			WebDriverManager.edgedriver().version(driverObject.driverVersion).setup();
			driver = new EdgeDriver(driverObject.capabilities);
			break;
		case CHROME:
			WebDriverManager.chromedriver().version(driverObject.driverVersion).setup();
			driver = new ChromeDriver(driverObject.capabilities);
			break;
		case CHROME_HEADLESS:
			WebDriverManager.chromedriver().version(driverObject.driverVersion).setup();
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setHeadless(true);
			driverObject.capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			driver = new ChromeDriver(driverObject.capabilities);
			break;
		case OPERA:
			WebDriverManager.operadriver().version(driverObject.driverVersion).setup();
			driver = new OperaDriver(driverObject.capabilities);
			break;
		case SAFARI:
			driver = new SafariDriver(driverObject.capabilities); 
			break;
		default:
			throw new IllegalStateException("Unsupported browsertype " + browserType);
		}
		return driver;
	}

	public String getServerUrl() {
		return "http://" + Config.getValue(SERVER_URL);
	}

	public String getServerPort() {
		return Config.getValue(SERVER_PORT);
	}
}