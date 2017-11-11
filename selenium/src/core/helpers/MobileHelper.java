package core.helpers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.remote.RemoteWebElement;

import core.driver.AbstractDriver;
import core.logger.TestLog;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSTouchAction;

public class MobileHelper {

	public static AppiumDriver getAppiumDriver() {
		return ((AppiumDriver) AbstractDriver.getWebDriver());
	}
	
	public static AndroidDriver getAndroidDriver() {
		return ((AndroidDriver) AbstractDriver.getWebDriver());
	}
	
	public static IOSDriver getiOSDriver() {
		return ((IOSDriver) AbstractDriver.getWebDriver());
	}

	/**
	 * hides ios or android keyboard
	 * 
	 */
	public static void hideKeyboard() {
		if (isIOS()) {
				getAppiumDriver().hideKeyboard();
		}
	}
	
	/**
	 * select enter on android
	 */
	public static void pressEnterOnAndroid() {
		if (isAndroid()) {
			getAndroidDriver().pressKeyCode(AndroidKeyCode.ENTER);
		}
	}

	/**
	 * resets the app
	 */
	public static void resetApp() {
		getAppiumDriver().resetApp();
		
		TestLog.logPass("I reset the app");
	}

	/**
	 * returns if mobile driver is used
	 * @return
	 */
	public static boolean isMobile() {
		if (AbstractDriver.getWebDriver() instanceof AppiumDriver) {
			return true;
		}
		return false;
	}
	
	/**
	 * is ios driver
	 * @return
	 */
	public static boolean isIOS() {
		if (AbstractDriver.getWebDriver() instanceof IOSDriver) {
			return true;
		}
		return false;
	}
	
	/**
	 * is ios driver
	 * @return
	 */
	public static boolean isAndroid() {
		if (AbstractDriver.getWebDriver() instanceof AndroidDriver) {
			return true;
		}
		return false;
	}
	
	/**
	 * is web driver
	 * @return
	 */
	public static boolean isWeb() {
		if( isIOS() || isAndroid()) return false;
		if (AbstractDriver.getWebDriver() instanceof WebDriver) {
			return true;
		}
		return false;
	}
	
	/**
	 * sets gps location on ios simulator
	 * @param location
	 */
	public static void setLocation(Location location) {
		getAppiumDriver().setLocation(location);
	}
	
	/**
	 * ios gesture
	 * https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/ios/ios-xctest-mobile-gestures.md#mobile-scroll
	 * @param element
	 */
	public static void scrollToiOS(EnhancedBy element) {
		if (MobileHelper.isIOS()) {
			try {
				EnhancedWebElement targetElement = Element.findElements(element);
				JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
				Map<String, Object> params = new HashMap<>();
				params.put("element", ((RemoteWebElement) targetElement.get(0)).getId());
				params.put("toVisible", "true");
				js.executeScript("mobile: scroll", params);

			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}
   
	 /**
	  * swipe right on the screen
	  */
   public static void swipeRight(EnhancedBy element) {
	   if (MobileHelper.isIOS()) {
			try {
				EnhancedWebElement targetElement = Element.findElements(element);
				JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
				Map<String, Object> params = new HashMap<>();
				params.put("element", ((RemoteWebElement) targetElement.get(0)).getId());
				params.put("toVisible", "true");
				js.executeScript("mobile: swipe", params);

			} catch (Exception e) {
				e.getStackTrace();
			}
		}
   }
   
	/**
	 * swipe on the target element
	 * @param target
	 */
	 public static void swipeLeft(EnhancedBy target) {
		 EnhancedWebElement targetElement = Element.findElements(target);
		// targetElement.click();
//		 int x = targetElement.get(0).getLocation().getX();
	//	 int xEnd = 0;
	//	 int yStart = targetElement.get(0).getLocation().getY();
//		 / getAppiumDriver().swipe(x, yStart, xEnd, yStart, 500);
		// getAppiumDriver().swipe(x, yStart, xEnd, yStart, 500); 
		
		 try {
			 
			 getAppiumDriver().performTouchAction(new IOSTouchAction(getAppiumDriver())
						// .moveTo(0, yStart)
					     .press(targetElement.get(0),0,0)
					     .waitAction(Duration.ofSeconds(1))
						 .release());
			 
		 getAppiumDriver().performTouchAction(new IOSTouchAction(getAppiumDriver())
						// .moveTo(0, yStart)
				 			.press(targetElement.get(0),0,0)
				 			.waitAction(Duration.ofSeconds(1))
				 			.moveTo(targetElement.get(0), -200,100)
						 .release());
		 }catch(Exception e) {
			 e.getMessage();
			 
		 }
		 System.out.println("done");

				 
		 
	/*
	      new TouchAction(getAppiumDriver()).press(x, yStart)
          .waitAction()
          .moveTo(xEnd,yStart).release();
          */
          
	 }
	 
	 public static void swipeElementExample(EnhancedBy target) {
		  String orientation = getAppiumDriver().getOrientation().value();
		  EnhancedWebElement targetElement = Element.findElements(target);
		  
		  // get the X coordinate of the upper left corner of the element, then add the element's width to get the rightmost X value of the element
		  int leftX =targetElement.getLocation().getX();
		  int rightX = leftX + targetElement.getSize().getWidth();

		  // get the Y coordinate of the upper left corner of the element, then subtract the height to get the lowest Y value of the element
		  int upperY = targetElement.getLocation().getY();
		  int lowerY = upperY - targetElement.getSize().getHeight();
		  int middleY = (upperY - lowerY) / 2;

		  if (orientation.equals("portrait")) {
		    // Swipe from just inside the left-middle to just inside the right-middle of the element over 500ms
			 
				 getAppiumDriver().performTouchAction(new IOSTouchAction(getAppiumDriver())
				 			.press(leftX + 5,middleY)
				 			.waitAction(Duration.ofSeconds(1))
				 			.moveTo(rightX - 5,middleY)
						 .release());
			  
			 // getAppiumDriver().swipe(leftX + 5, middleY, rightX - 5, middleY, 500);
		  }
		  else if (orientation.equals("landscape")) {
		    // Swipe from just inside the right-middle to just inside the left-middle of the element over 500ms
				 getAppiumDriver().performTouchAction(new IOSTouchAction(getAppiumDriver())
				 			.press(rightX - 5,middleY)
				 			.waitAction(Duration.ofSeconds(1))
				 			.moveTo(leftX + 5,middleY)
						 .release());
			  
			 // getAppiumDriver().swipe(rightX - 5, middleY, leftX + 5, middleY, 500);
		  }
		}
}