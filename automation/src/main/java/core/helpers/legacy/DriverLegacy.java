package core.helpers.legacy;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import core.helpers.Element;
import core.support.listeners.TestListener;
import core.support.objects.TestObject;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;

public class DriverLegacy  {

	public static EnhancedBy getEnhancedElement(WebElement target){

	    String path = target.toString();
	    String[] pathVariables = (path.split("->")[1].replaceFirst("(?s)(.*)\\]", "$1" + "")).split(":");
	   
	    if(pathVariables.length!=2) 
	    	 throw new IllegalStateException("webElement : " + path + " not containg valid by:locator format!");
  
	    String selector = pathVariables[0].trim();
	    String value = pathVariables[1].trim();
		

		EnhancedBy element = setEnhancedElement(selector, value);


		return element;
	}
	
	/**
	 * set the webDriver from webDriver or mobileDriver without relying on global flag
	 */
	public static void setDriver(WebDriver driver) {
		TestListener.isTestNG = true;
		TestObject.IS_PROPERTIES_DISABLED = true;
		AbstractDriver.TIMEOUT_SECONDS = 60;
		AbstractDriver.TIMEOUT_IMPLICIT_SECONDS = 5;
		AbstractDriver.setWebDriver(driver);
	}
	
	public static void setDriver(WebDriver driver, boolean isPropertyDisabled, int timeoutSec, int implicitSec) {
		TestListener.isTestNG = true;
		TestObject.IS_PROPERTIES_DISABLED = isPropertyDisabled;
		AbstractDriver.TIMEOUT_SECONDS = timeoutSec;
		AbstractDriver.TIMEOUT_IMPLICIT_SECONDS = implicitSec;
		AbstractDriver.setWebDriver(driver);
	}

	protected static EnhancedBy setEnhancedElement(String selector, String value){
		EnhancedBy element = null;

		System.out.println("selector: " + selector);
		switch(selector) {
			case "css selector":
			case "css":
				element = Element.byCss(value, value);
				break;
			case "xpath":
				element = Element.byXpath(value, value);
				break;
			case "name":
				element = Element.byName(value, value);
				break;
			case "id":
				element = Element.byId(value, value);
				break;
			case "class":
				element = Element.byClass(value, value);
				break;
		}
		return element;
	}
}