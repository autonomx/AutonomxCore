package core.helpers;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.uiCore.driverProperties.capabilities.WebCapability;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.drivers.AbstractDriverJunit;
import core.uiCore.drivers.AbstractDriverTestNG;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class PageHelper {

	/**
	 * maximizes web page
	 */
	public void maximizePage() {
		try {
			AbstractDriver.getWebDriver().manage().window().maximize();
		} catch (Exception e) {
			TestLog.logWarning("Page was not maximized");
			printStackTrace(e);
		}
	}

	/**
	 * sets page size
	 * 
	 * @param x
	 * @param y
	 */
	public void setPageSize(int x, int y) {
		Dimension dimension = new Dimension(x, y);
		AbstractDriver.getWebDriver().manage().window().setSize(dimension);
	}

	/**
	 * reload page
	 */
	public void refreshPage() {
		if(AbstractDriver.getWebDriver() == null) return;
		
		try {
			if (Helper.isWebDriver())
				AbstractDriver.getWebDriver().navigate().refresh();
			if (Helper.mobile_isMobile())
				Helper.refreshMobileApp();
		}catch(Exception e) {
			e.getMessage();
		}

		Helper.wait.waitForSeconds(1);
	}

	/**
	 * switches frame to frame specified
	 * 
	 * @param frame
	 */
	public void switchIframe(EnhancedBy frame) {
		EnhancedWebElement frameElement = Element.findElements(frame);
		AbstractDriver.getWebDriver().switchTo().defaultContent(); // you are now outside both
		// frames
		AbstractDriver.getWebDriver().switchTo().frame(frameElement.get(0));
	}

	/**
	 * switches to default frame
	 */
	public void switchToDefaultFrame() {
		AbstractDriver.getWebDriver().switchTo().defaultContent();
	}

	/**
	 * switch windows handle based on index
	 * 
	 * @param index
	 */
	public static void switchWindowHandle(int index) {
		// wait for the window handle to be available
		waitForWindowHandle(index);

		// switch window handle
		Set<String> handles = Helper.mobile.getAppiumDriver().getWindowHandles();
		List<String> handleList = new ArrayList<>(handles);
		Helper.mobile.getAppiumDriver().switchTo().window(handleList.get(index));
	}

	/**
	 * waits for the window handle at index to be available
	 * 
	 * @param index index of window handle
	 */
	private static void waitForWindowHandle(int index) {
		int retryWaitInSeconds = 3;
		int retry = AbstractDriver.TIMEOUT_SECONDS / retryWaitInSeconds;
		Set<String> handles = Helper.mobile.getAppiumDriver().getWindowHandles();
		List<String> handleList = new ArrayList<>(handles);

		while (index >= handleList.size() && retry > 0) {
			retry--;
			handles = Helper.mobile.getAppiumDriver().getWindowHandles();
			handleList = new ArrayList<>(handles);
			Helper.waitForSeconds(retryWaitInSeconds);
		}

		if (index >= handleList.size()) {
			Helper.assertFalse("window handle not available. size: " + handleList.size() + "index: " + index);
		}
	}

	/**
	 * dismisses alert by selecting ok or cancel
	 * 
	 * @param alert
	 */
	public String dimissAlert() {
		String alertText = "";
		try {
			Alert alert = AbstractDriver.getWebDriver().switchTo().alert();
			alertText = alert.getText();
			TestLog.ConsoleLog("dismissed alert");
			alert.dismiss();
		} catch (Exception e) {
			e.getMessage();
		}
		return alertText;
	}

	/**
	 * accepts alert by selecting ok or cancel
	 * 
	 * @param alert
	 */
	public String acceptAlert() {
		String alertText = "";
		try {
			Alert alert = AbstractDriver.getWebDriver().switchTo().alert();
			alertText = alert.getText();
			TestLog.ConsoleLog("accepted alert");
			alert.accept();
		} catch (Exception e) {
			e.getMessage();
		}
		return alertText;
	}

	/**
	 * returns alert message value
	 * 
	 * @return
	 */
	public String getAlertValue() {
		String alertText = "";
		try {
			Alert alert = AbstractDriver.getWebDriver().switchTo().alert();
			alertText = alert.getText();
		} catch (Exception e) {
			e.getMessage();
		}
		return alertText;
	}

	/**
	 * return the current window handle
	 * 
	 * @return
	 */
	public String currentWindow() {
		return AbstractDriver.getWebDriver().getWindowHandle();
	}

	/**
	 * switch to the new opened window
	 * 
	 * @param defaultWindow
	 */
	public void switchToNewWindow(String defaultWindow) {
		for (String winHandle : AbstractDriver.getWebDriver().getWindowHandles()) {
			if (!winHandle.equals(defaultWindow))
				AbstractDriver.getWebDriver().switchTo().window(winHandle);
		}
	}

	/**
	 * close the window And return to the defaultWindow
	 * 
	 * @param defaultWindow
	 */
	public void CloseAndReturn(String defaultWindow) {
		AbstractDriver.getWebDriver().close();
		AbstractDriver.getWebDriver().switchTo().window(defaultWindow);
	}

	/**
	 * gets page source
	 */
	public void refreshPageSource() {
		AbstractDriver.getWebDriver().getPageSource();
	}

	/**
	 * verify page title
	 * 
	 * @param appTitle
	 */
	public void verifyTitle(String appTitle) {
		Helper.assertEquals(AbstractDriver.getWebDriver().getTitle(), appTitle);
	}

	/**
	 * switch webdriver use for switching between different drivers
	 * 
	 * @param driver
	 */
	public void switchDriver(WebDriver driver) {

		if (driver.getTitle() != null || !driver.getTitle().isEmpty())
			TestLog.logPass("swtiching to " + driver.getTitle());

		if (AbstractDriver.isJunit()) {
			AbstractDriverJunit.setWebDriver(driver);
		} else if (AbstractDriver.isTestNG()) {
			AbstractDriverTestNG.setWebDriver(driver);
		}
	}

	/**
	 * switch to next tab circles back to initial tab if it reaches the last tab
	 */
	public void switchToNextTab() {
		ArrayList<String> tabs = new ArrayList<String>(AbstractDriver.getWebDriver().getWindowHandles());
		int currentIndex = tabs.indexOf(AbstractDriver.getWebDriver().getWindowHandle());

		// circle back to the first tab
		if (currentIndex == (tabs.size() - 1))
			currentIndex = -1;

		try {
			AbstractDriver.getWebDriver().switchTo().window(tabs.get(currentIndex + 1));
		} catch (Exception e) {
			Helper.assertFalse("tab does not exist");
		}
	}

	/**
	 * switch to previous tab circle back to the last tab
	 */
	public void switchToPreviousTab() {
		ArrayList<String> tabs = new ArrayList<String>(AbstractDriver.getWebDriver().getWindowHandles());
		int currentIndex = tabs.indexOf(AbstractDriver.getWebDriver().getWindowHandle());

		// circle back to last index
		if (currentIndex == 0)
			currentIndex = tabs.size();

		try {
			AbstractDriver.getWebDriver().switchTo().window(tabs.get(currentIndex - 1));
		} catch (Exception e) {
			Helper.assertFalse("tab does not exist");
		}
	}

	/**
	 * switch to tab by index
	 */
	public void switchToTab(int index) {
		ArrayList<String> tabs = new ArrayList<String>(AbstractDriver.getWebDriver().getWindowHandles());

		try {
			AbstractDriver.getWebDriver().switchTo().window(tabs.get(index));
		} catch (Exception e) {
			Helper.assertFalse("tab does not exist");
		}
	}

	/**
	 * switch to tab by index
	 */
	public void verifyNumberOfTabs(int tabs) {
		ArrayList<String> tabCount = new ArrayList<String>(AbstractDriver.getWebDriver().getWindowHandles());
		Helper.assertEquals(tabs, tabCount.size());
	}

	/**
	 * returns the title of the page
	 * 
	 * @return
	 */
	public String getPageTitle() {
		if(AbstractDriver.getWebDriver() == null) return StringUtils.EMPTY;
		try {
			return AbstractDriver.getWebDriver().getTitle();
		}catch(Exception e) {
			printStackTrace(e);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * returns the current url
	 * 
	 * @return
	 */
	public String getCurrentUrl() {
		if(AbstractDriver.getWebDriver() == null) return StringUtils.EMPTY;
		try {
			return AbstractDriver.getWebDriver().getCurrentUrl();
		}catch(Exception e) {
			printStackTrace(e);
		}
		return StringUtils.EMPTY;
	}

	/**
	 * gets page source
	 * 
	 * @return
	 */
	public String getPageSource() {
		if(AbstractDriver.getWebDriver() == null) return StringUtils.EMPTY;
		return AbstractDriver.getWebDriver().getPageSource().toString();
	}

	/**
	 * navigates back
	 */
	public void navigateBack() {
		if(AbstractDriver.getWebDriver() == null) return;
		AbstractDriver.getWebDriver().navigate().back();
	}

	/**
	 * navigate forward
	 */
	public void navigateForward() {
		if(AbstractDriver.getWebDriver() == null) return;
		AbstractDriver.getWebDriver().navigate().forward();
	}

	/**
	 * delete all cookies
	 */
	public void deleteAllCookies() {
		if(AbstractDriver.getWebDriver() == null) return;
		AbstractDriver.getWebDriver().manage().deleteAllCookies();
	}

	/**
	 * delete cookie named
	 * 
	 * @param name
	 */
	public void deleteCookieNamed(String name) {
		if(AbstractDriver.getWebDriver() == null) return;
		AbstractDriver.getWebDriver().manage().deleteCookieNamed(name);
	}

	/**
	 * brings current browser to front
	 * webdriver only
	 */
	public void bringPageToFront() {
		if(AbstractDriver.getWebDriver() == null) return;
		if(!Helper.isWebDriver()) return;
		
		try {
			String currentWindowHandle = AbstractDriver.getWebDriver().getWindowHandle();
			AbstractDriver.getWebDriver().switchTo().window(currentWindowHandle);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * navigate to a different url
	 * 
	 * @param url destination url
	 */
	public void swtichUrl(String url) {
		navigateToUrl(url);
	}

	/**
	 * navigate to a different url
	 * 
	 * @param url destination url
	 */
	public void navigateToUrl(String url) {
		if(AbstractDriver.getWebDriver() == null) return;
		
		int retry = 3;
		boolean success = false;
		do {
			retry--;
			try {
				AbstractDriver.getWebDriver().get(url);
				success = true;
			}catch(Exception e) {
		    	Helper.page.printStackTrace(e);
				TestLog.ConsoleLog("get url failed, retrying: " + url);
			}
			
			retry--;
		}while(!success && retry >= 0);
	}

	/**
	 * retrieves the clip board data
	 * 
	 * @return
	 */
	public String getClipboardData() {
		String myText = "";
		try {
			myText = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			e.getMessage();
		}
		return myText;
	}

	/**
	 * quits the current web driver
	 */
	public void quitCurrentDriver() {
		DriverObject.quitWebDriver(AbstractDriver.getWebDriver());
	}

	/**
	 * quit driver
	 * 
	 * @param driver
	 */
	public void quitDriver(WebDriver driver) {
		DriverObject.quitWebDriver(driver);
	}

	/**
	 * quits all drivers in the current test
	 */
	public void quitAllCurrentTestDrivers() {
		DriverObject.quitTestDrivers();
	}

	/**
	 * returns true if the element is visible in the current page only for web
	 * applications
	 * 
	 * @param by
	 * @return
	 */
	public Boolean isVisibleInViewport(EnhancedBy by) {
		return isVisibleInViewport(by, 0);
	}

	/**
	 * returns true if the element is visible in the current page only for web
	 * applications
	 * 
	 * @param by
	 * @return
	 */
    public Boolean isVisibleInViewport(EnhancedBy by, int index) {
  	  if(!Helper.isWebDriver()) return false;
  	  Boolean isVisible = false;
		  EnhancedWebElement targetElement = Element.findElements(by);
		  
		  try {
			  isVisible = (Boolean)((JavascriptExecutor) AbstractDriver.getWebDriver()).executeScript(
		    	      "var elem = arguments[0],                 " +
		    	    	      "  box = elem.getBoundingClientRect(),    " +
		    	    	      "  cx = box.left + box.width / 2,         " +
		    	    	      "  cy = box.top + box.height / 2,         " +
		    	    	      "  e = document.elementFromPoint(cx, cy); " +
		    	    	      "for (; e; e = e.parentElement) {         " +
		    	    	      "  if (e === elem)                        " +
		    	    	      "    return true;                         " +
		    	    	      "}                                        " +
		    	    	      "return false;                            "
		    	    	      , targetElement.get(index, false));
		  }catch(StaleElementReferenceException st) {
			  st.getMessage();
		  }catch(Exception e) {
			  e.getMessage();
		  }
		  
  	  return isVisible;
  }
  
	/**
	 * get current webdriver
	 * @return
	 */
	public WebDriver getWebdriver() {
	    return AbstractDriver.getWebDriver();
	}
	
	/**
	 * prints stack trace
	 * @param trace
	 */
	public void printStackTrace(Throwable throwable) {
    	TestLog.ConsoleLogNoLimit("stack trace: " + Helper.stringNormalize(ExceptionUtils.getStackTrace(throwable)), false);

	}/**
	 * scroll to bottom of browser
	 */
	public void scrollBottomPageBrowser() {
		// only for web browser
		if(!Helper.isWebDriver()) return;
		
		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.sendKeys(Keys.END).build().perform();
		Helper.waitForSeconds(0.5);
	}
	
	
	/**
	 * scroll up browser
	 */
	public void scrollUpBrowser(int count) {
		
		// only for web browser
		if(!Helper.isWebDriver()) return;
		
		Actions action = new Actions(AbstractDriver.getWebDriver());
		
		for(int i=0; i<count; i++) {
			action.sendKeys(Keys.PAGE_UP).build().perform();
		}
	}
	
	/**
	 * scroll up the browser
	 */
	public void scrollUpBrowser() {
		// only for web browser
		if(!Helper.isWebDriver()) return;
		
		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.sendKeys(Keys.UP).build().perform();
	}
	
	
	/**
	 * scroll down browser
	 */
	public void scrollDownBrowser(int count) {
		
		// only for web browser
		if(!Helper.isWebDriver()) return;
		
		Actions action = new Actions(AbstractDriver.getWebDriver());
		
		for(int i=0; i<count; i++) {
			action.sendKeys(Keys.PAGE_DOWN).build().perform();
		}
	}
	
	/**
	 * scroll down the browser
	 */
	public void scrollDownBrowser() {
		
		// only for web browser
		if(!Helper.isWebDriver()) return;
		
		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.sendKeys(Keys.DOWN).build().perform();
	}

	/**
	 * scroll to web element
	 * @param element
	 */
	public void scrollToWebElement(EnhancedBy element) {
		// only for web browser
		if(!Helper.isWebDriver()) return;
		int retry = 5;
		do {
			retry--;
			EnhancedWebElement targetElement = Element.findElements(element);
			targetElement.scrollToView(0);
			Helper.waitForSeconds(0.5);
		}while(!Helper.isDisplayed(element) && retry > 0);
	}
	
	/**
	 * scroll to element
	 * @param element
	 * @param index
	 */
	public void scrollToWebElement(EnhancedBy element, int index) {
		// only for web browser
		if(!Helper.isWebDriver()) return;
		
		EnhancedWebElement targetElement = Element.findElements(element);
		targetElement.scrollToView(index);
		Helper.waitForSeconds(0.5);
	}
	
	public boolean isFirefox() {
		return WebCapability.isFirefox();
	}
	
	public boolean isChrome() {
		return WebCapability.isChrome();
	}
	
	public boolean isSafari() {
		return WebCapability.isSafari();
	}
	
	public boolean isMicrosoftEdge() {
		return WebCapability.isMicrosoftEdge();
	}
}