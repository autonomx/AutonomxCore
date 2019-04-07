package core.uiCore.driverProperties.capabilities;

import org.openqa.selenium.remote.DesiredCapabilities;

import core.support.configReader.Config;

/**
    1. enable windows development mode https://docs.microsoft.com/en-us/windows/uwp/get-started/enable-your-device-for-development
    2. Download Windows Application Driver installer from https://github.com/Microsoft/WinAppDriver/releases
	3. Run the installer on a Windows 10 machine where your application under test is installed And will be tested
	4. Run WinAppDriver.exe from the installation directory (E.g. C:\Program Files (x86)\Windows Application Driver)
	
	https://github.com/Microsoft/WinAppDriver
 * @author ehsan.matean
 *
 */
public class WinAppCapabilities {
	
	public static String WINAPP = "win.app";


	public DesiredCapabilities capabilities;


	public WinAppCapabilities() {
		capabilities = new DesiredCapabilities();
	}

	public WinAppCapabilities withCapability(DesiredCapabilities Capabilities) {
		this.capabilities = Capabilities;
		return this;
	}
	
	public DesiredCapabilities getCapability() {
		return capabilities;
	}



	/**
	 * sets win app capabilities values are from maven or properties file maven has
	 * higher priority than properties
	 * 
	 * @return
	 */
	public WinAppCapabilities withWinAppdCapability() {
		  capabilities.setCapability("platformName", "Windows");
		  capabilities.setCapability("deviceName", "WindowsPC");
		  capabilities.setCapability("app", Config.getValue(WINAPP));

		  return this;
	}

	
}