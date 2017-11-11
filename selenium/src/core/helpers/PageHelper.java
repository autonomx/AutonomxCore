package core.helpers;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;

import core.driver.AbstractDriver;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class PageHelper {

	/**
	 * maximizes web page
	 */
	public static void maximizePage() {
		AbstractDriver.getWebDriver().manage().window().maximize();
	}

	/**
	 * sets page size
	 * 
	 * @param x
	 * @param y
	 */
	public static void setPageSize(int x, int y) {
		Dimension dimension = new Dimension(x, y);
		AbstractDriver.getWebDriver().manage().window().setSize(dimension);
	}

	/**
	 * reload page
	 */
	public static void refreshPage() {
		AbstractDriver.getWebDriver().navigate().refresh();
	}

	/**
	 * switches frame to frame specified
	 * 
	 * @param frame
	 */
	public static void switchIframe(EnhancedBy frame) {
		EnhancedWebElement frameElement = Element.findElements(frame);
		AbstractDriver.getWebDriver().switchTo().defaultContent(); // you are now outside both
		// frames
		AbstractDriver.getWebDriver().switchTo().frame(frameElement);
	}

	/**
	 * switches to default frame
	 */
	public static void switchToDefaultFrame() {
		AbstractDriver.getWebDriver().switchTo().defaultContent();
	}

	/**
	 * dismisses alert by selecting ok or cancel
	 * 
	 * @param alert
	 */
	public static String dimissAlert() {
		String alertText = "";
		try {
			Alert alert = AbstractDriver.getWebDriver().switchTo().alert();
		    alertText = alert.getText();
			// update is executed
			alert.dismiss();;
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
	public static String acceptAlert() {
		String alertText = "";
		try {
			Alert alert = AbstractDriver.getWebDriver().switchTo().alert();
		    alertText = alert.getText();
			// update is executed
			alert.dismiss();;
		} catch (Exception e) {
			e.getMessage();
		}
		return alertText;
	}
	
	/**
	 * returns alert message value
	 * @return
	 */
	public static String getAlertValue() {
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
	public static String currentWindow() {
		return AbstractDriver.getWebDriver().getWindowHandle();
	}

	/**
	 * switch to the new opened window
	 * 
	 * @param defaultWindow
	 */
	public static void switchToNewWindow(String defaultWindow) {
		for (String winHandle : AbstractDriver.getWebDriver().getWindowHandles()) {
			if (!winHandle.equals(defaultWindow))
				AbstractDriver.getWebDriver().switchTo().window(winHandle);
		}
	}

	/**
	 * close the window and return to the defaultWindow
	 * 
	 * @param defaultWindow
	 */
	public static void CloseAndReturn(String defaultWindow) {
		AbstractDriver.getWebDriver().close();
		AbstractDriver.getWebDriver().switchTo().window(defaultWindow);
	}

}