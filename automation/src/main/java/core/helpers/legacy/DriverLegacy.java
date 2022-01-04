package core.helpers.legacy;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import core.helpers.Element;
import core.support.listeners.TestListener;
import core.support.objects.TestObject;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;

public class DriverLegacy  {

	public static EnhancedBy getEnhancedElement(WebElement target){

		String[] locator = getLocator(target);

	    String selector = locator[0].trim();
	    String value = locator[1].trim();
		
		EnhancedBy element = setEnhancedElement(selector, value);
		return element;
	}
	
	/**
	 * retrieves locator values from a webElement
	 * @param element
	 * @return
	 */
	protected static String[] getLocator(WebElement element) {
		String[] path = new String[2];
        try {
            Object proxyOrigin = FieldUtils.readField(element, "h", true);
            Object locator = FieldUtils.readField(proxyOrigin, "locator", true);
            Object findBy = FieldUtils.readField(locator, "by", true);
            if (findBy != null) {
                path = findBy.toString().split(":");
                if(path.length !=2)
                	 Helper.assertFalse("element could not be parsed: " + findBy);
                path[0] = path[0].split("By.")[1];
                return path;
            }
        } catch (Exception ignored) {
        	ignored.getMessage();
        }
        
        Helper.assertFalse("element could not be parsed: " + element.toString());
        return null;
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

		switch(selector) {
			case "cssSelector":
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
			case "className":
				element = Element.byClass(value, value);
				break;
			case "tagName":
				element = Element.byTagName(value, value);
				break;
			case "linkText":
				element = Element.byLinkText(value, value);
				break;
			case "partialLinkText":
				element = Element.byPartialLinkText(value, value);
				break;
		}
		return element;
	}
}