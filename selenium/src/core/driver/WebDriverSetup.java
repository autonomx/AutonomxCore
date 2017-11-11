package core.driver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import core.logger.PropertiesReader;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.OperaDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

public class WebDriverSetup {

	/**
	 * get webdriver by type set in properties file
	 * 
	 * @return
	 * @throws IOException
	 */

	@SuppressWarnings("deprecation")
	public static WebDriver getWebDriverByType(DriverObject driverObject) throws IOException {
		WebDriver driver = null;
		PropertiesReader property = new PropertiesReader();
		
		DriverObject.WebDriverType type = null;
		
		if(driverObject.driverType == null) {
			type = Enum.valueOf(DriverObject.WebDriverType.class, property.getDriverType());
		}
		else
		{
			type = driverObject.driverType;
		}
			
		switch (type) {
		case FIREFOX:
			FirefoxDriverManager.getInstance().setup();
			driver = new FirefoxDriver(driverObject.capabilities);
			break;
		case INTERNET_EXPLORER:
			InternetExplorerDriverManager.getInstance().setup();
			driver = new InternetExplorerDriver(driverObject.capabilities);
			break;
		case MICROSOFT_EDGE:
			EdgeDriverManager.getInstance().setup();
			driver = new EdgeDriver(driverObject.capabilities);
			break;
		case CHROME:
  			ChromeDriverManager.getInstance().setup();
			driver = new ChromeDriver(driverObject.capabilities);
			break;
		case Opera:
			OperaDriverManager.getInstance().setup();
			driver = new OperaDriver(driverObject.capabilities);
			break;
		case PHANTOMJS:
			PhantomJsDriverManager.getInstance().setup();
			driver = new PhantomJSDriver(driverObject.capabilities);
			break;
		case REMOTE_WEBDRIVER:
			driver = new RemoteWebDriver(new URL(PropertiesReader.getGridUrl() + ":" + PropertiesReader.getGridPort() + "/wd/hub"), driverObject.capabilities);
			break;
		case IOS_DRIVER:	     
			driver = new IOSDriver(new URL(PropertiesReader.getGridUrl() + ":" + PropertiesReader.getGridPort() + "/wd/hub"), driverObject.capabilities);
			break;
		case ANDROID_DRIVER:
			driver = new AndroidDriver(new URL(PropertiesReader.getGridUrl() + ":" + PropertiesReader.getGridPort() + "/wd/hub"), driverObject.capabilities);
			break;
		default:
			throw new IllegalStateException("Unsupported browsertype " + type);
		}
		return driver;
	}
	
	public static void startAppiumServer(WebDriver driver, DriverObject driverObject) {
		AppiumDriverLocalService service;
	    
		Map<String, String> env = new HashMap<>(System.getenv());
		env.put("PATH", "/usr/local/bin:" + env.get("PATH"));
		
        AppiumServiceBuilder builder = new AppiumServiceBuilder()
        		.usingAnyFreePort()
         	.withEnvironment(env)
        		.withIPAddress("0.0.0.0")
      //    .withArgument(GeneralServerFlag.LOG_LEVEL, "warn")
        		.withLogFile(new File("target/appium.log"))
       		.withArgument(GeneralServerFlag.SESSION_OVERRIDE);
        
        service = AppiumDriverLocalService.buildService(builder);

        service.start();

        if (service == null || !service.isRunning()) {
        //   throw new AppiumServerHasNotBeenStartedLocallyException("An appium server node is not started!");
        }
        driver = new IOSDriver(service.getUrl(), driverObject.capabilities);
	}
}
