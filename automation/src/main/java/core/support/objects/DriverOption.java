package core.support.objects;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.safari.SafariOptions;

public class DriverOption {

	/**
	 * variables
	 */
	ChromeOptions chromeOptions = new ChromeOptions();
	FirefoxOptions firefoxOptions = new FirefoxOptions();
	EdgeOptions edgeOptions = new EdgeOptions();
	SafariOptions safariOptions = new SafariOptions();
	OperaOptions operaOptions = new OperaOptions();
	InternetExplorerOptions ieOptions = new InternetExplorerOptions();
	
	public DriverOption withChromeOptions(ChromeOptions chromeOptions) {
		this.chromeOptions = chromeOptions;
		return this;
	}
	
	public DriverOption withFirefoxOptions(FirefoxOptions firefoxOptions) {
		this.firefoxOptions = firefoxOptions;
		return this;
	}
	
	public DriverOption withEdgeOptions(EdgeOptions edgeOptions) {
		this.edgeOptions = edgeOptions;
		return this;
	}
	
	public DriverOption withSafariOptions(SafariOptions safairOptions) {
		this.safariOptions = safairOptions;
		return this;
	}
	
	public DriverOption withOperaOptions(OperaOptions operaOptions) {
		this.operaOptions = operaOptions;
		return this;
	}
	
	public DriverOption withInternetExplorerOptions(InternetExplorerOptions ieOptions) {
		this.ieOptions = ieOptions;
		return this;
	}
	
	public ChromeOptions getChromeOptions() {
		return this.chromeOptions;
	}
	
	public FirefoxOptions getFirefoxOptions() {
		return this.firefoxOptions;
	}
	
	public EdgeOptions getEdgeOptions() {
		return this.edgeOptions;
	}
	
	public SafariOptions getSafariOptions() {
		return this.safariOptions;
	}
	
	public OperaOptions getOperaOptions() {
		return this.operaOptions;
	}
	
	public InternetExplorerOptions getInternetExplorerOptions() {
		return this.ieOptions;
	}
}