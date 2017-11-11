package core.driver;

import org.openqa.selenium.remote.DesiredCapabilities;

public class DriverObject {
	public enum WebDriverType {
		INTERNET_EXPLORER, 
		REMOTE_WEBDRIVER, 
		IOS_DRIVER, 
		ANDROID_DRIVER, 
		CHROME, 
		PHANTOMJS,
		FIREFOX, MICROSOFT_EDGE, Opera, 
		API
	}
	
	public String initialURL = "";
	public String app;
	public WebDriverType driverType;
	public DesiredCapabilities capabilities;
	
	public DriverObject withApp(String app) {
		this.app = app;
		return this;
	}
	
	public DriverObject withUrl(String initialURL) {
		this.initialURL = initialURL;
		return this;
	}

	public DriverObject withDriverType(WebDriverType driverType) {
		this.driverType = driverType;
		return this;
	}
	
	public DriverObject withCapabilities(DesiredCapabilities capabilities) {
		this.capabilities = capabilities;
		return this;
	}
	
}