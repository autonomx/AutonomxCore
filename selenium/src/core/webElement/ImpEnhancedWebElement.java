package core.webElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;

import core.driver.AbstractDriver;
import core.helpers.AssertHelper;
import core.helpers.MobileHelper;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;



public class ImpEnhancedWebElement implements EnhancedWebElement {

	private final String elementName;
	private final By by;
	private final WebDriver webDriver;
	private final WebElement parent;
	private List<WebElement> current;
	private List<MobileElement> mobileElement;
	

	public ImpEnhancedWebElement(String elementName, By by, WebDriver webDriver, WebElement parent) {
		this.elementName = elementName;
		this.by = by;
		this.webDriver = webDriver;
		this.parent = parent;
		this.current = null;
		this.mobileElement = null;
	}

	@Override
	public EnhancedWebElement findElement(By by, String elementName, EnhancedWebElement parentElement) {

		return new ImpEnhancedWebElement(elementName, by, webDriver, parentElement);

	}
	
	@Override
	public void clear() {
		clear(0);
	}

	@Override
	public void clear(int index) {
		int retry = 1;

		boolean success = false;
		do {
			retry--;
			try {
				isDisplayed(index);
				if (isExist()) {
					scrollToView_Web(index);
					WebElement element = getElement(index);
					element.clear();
					success = true;
				}
			} catch (Exception e) {
				resetElement();
				e.getMessage();
			}
		} while (!success && retry > 0);

	}

	@Override
	public String getCssValue(String arg0) {
		return getCssValue(arg0, 0);
	}

	@Override
	public String getCssValue(String arg0, int index) {
		WebElement element = getElement(index);
		return element.getCssValue(arg0);
	}

	@Override
	public Point getLocation() {
		return getLocation(0);
	}

	@Override
	public Point getLocation(int index) {
		WebElement element = getElement(index);
		return element.getLocation();
	}

	@Override
	public Dimension getSize() {
		return getSize(0);
	}

	public Dimension getSize(int index) {
		System.out.println("getSize: " + elementName);
		WebElement element = getElement(index);
		return element.getSize();
	}

	@Override
	public int count() {
		if (isExist()) {
			List<WebElement> elements = getElements();
			return elements.size();
		}
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return (count() == 0);
	}

	@Override
	public String getTagName() {
		return getTagName(0);
	}

	public String getTagName(int index) {
		WebElement element = getElement(index);
		return element.getTagName();
	}

	@Override
	public boolean isDisplayed() {
		return isDisplayed(0);
	}

	@Override
	public boolean isDisplayed(int index) {
		WebElement element = getElement(index);
		return element.isDisplayed();
	}

	@Override
	public boolean isEnabled() {
		return isEnabled(0);
	}

	@Override
	public boolean isEnabled(int index) {
		WebElement element = getElement(index);
		return element.isEnabled();
	}

	@Override
	public void submit() {
		submit(0);
	}

	public void submit(int index) {
		WebElement element = getElement(index);
		element.submit();

	}

	@Override
	public void click() {
		click(0);
	}
	
	@Override
	public void click(int index) {
		int retry = 1;

		boolean success = false;
		do {
			retry--;
			try {
				 scrollToView(index);
				 isDisplayed(index);
				if (isExist()) {
				    	WebElement toClick = getElement(index);
				    	toClick.click();
					success = true;
				}
			} catch (Exception e) {
				resetElement();
				e.getMessage();
			}
		} while (!success && retry > 0);
	}

	@Override
	public void scrollToView() {
		scrollToView_Web(0);
		scrollTo_iOS(0);
	}
	
	@Override
	public void scrollToView(int index) {
	  if(!isExist()) {
		scrollToView_Web(index);
		scrollTo_iOS(index);
	  }
	}
	
	/**
	 * ios gesture
	 * https://github.com/appium/appium/blob/master/docs/en/writing-running-appium/ios/ios-xctest-mobile-gestures.md#mobile-scroll
	 * @param element
	 */
	public void scrollTo_iOS(int index) {
		if (MobileHelper.isIOS()) {
			try {
				WebElement element = getElement(index);
				JavascriptExecutor js = (JavascriptExecutor) AbstractDriver.getWebDriver();
				Map<String, Object> params = new HashMap<>();
				params.put("element", ((RemoteWebElement) element).getId());
				params.put("toVisible", "true");
				js.executeScript("mobile: scroll", params);

			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}

	/**
	 * scroll element to center of view on web
	 */
	public void scrollToView_Web(int index) {
		if (MobileHelper.isWeb()) {
			WebElement element = getElement(index);

			String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
					+ "var elementTop = arguments[0].getBoundingClientRect().top;"
					+ "window.scrollBy(0, elementTop-(viewPortHeight/2));";
			try {
				JavascriptExecutor js = (JavascriptExecutor) webDriver;
				js.executeScript(scrollElementIntoMiddle, element);
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}
	
  
	/**
	 * returns if element exists
	 */
	@Override
	public boolean isExist(int... index) {
		webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.MILLISECONDS);
		boolean isExist = false;
	   
		if(index.length > 0) {
			 isExist = isElementExist(index[0]);
		 } else{
			 isExist = isListExist();
		 }
		 webDriver.manage().timeouts().implicitlyWait(AbstractDriver.TIMEOUT_SECONDS, TimeUnit.SECONDS);
		 return isExist;
	}
	
	/**
	 * returns if any element in a list is displayed
	 */
	public boolean isListExist() {
		List<WebElement> elements = getElements();

		if (elements ==  null || elements.isEmpty())
			return false;
		
		int size = elements.size();
		for (int i = 0; i < size; i++) {
			if (isExist(i))
				return true;
		}
		return false;
	}

	/**
	 * returns true if element is displayed sets timeout to minimum to get the
	 * value quickly
	 */
	public boolean isElementExist(int index) {

		boolean isElementExists = false;
		try {
			WebElement element = getElement(index);
			if (element.isDisplayed()) {
				isElementExists = true;
			}
		} catch (Exception e) {
			isElementExists = false;
		}
		return isElementExists;
	}

	@Override
	public void moveMouse() {
		moveMouse(0);
	}

	@Override
	public void moveMouse(int index) {
		WebElement element = getElement(index);
		element.getLocation();
	}

	public By getBy() {
		return by;
	}
	
	public String getCssSelectorValue() {
		String byValue = by.toString();
		return byValue.substring(byValue.lastIndexOf(":") + 1);
	}
	
	@Override
	public void setValue(int index, CharSequence... keysToSend) {
		MobileElement element = getMobileElement().get(index);
		String value = keysToSend[0].toString();
		element.setValue(value);	
	}
	
	@Override
	public void sendKeys(CharSequence... keysToSend) {
		sendKeys(0, keysToSend);
	}

	@Override
	public void sendKeys(int index, CharSequence... keysToSend) {
		int retry = 3;

		boolean success = false;
		do {
			retry--;
			try {
				scrollToView(index);
				isDisplayed(index);
				if (isExist()) {
					WebElement element = getElement(index);
					element.sendKeys(keysToSend);
					success = true;
				}
			} catch (Exception e) {
				resetElement();
				e.getMessage();
			}
		} while (!success && retry > 0);
		AssertHelper.assertTrue("send key was not successful", success);
	}

	@Override
	public String getAttribute(String name) {
		return getAttribute(name, 0);
	}

	@Override
	public String getAttribute(String name, int index) {
		
		int retry = 3;
		String value = "";
		boolean isSuccess = false;
		do {
			retry--;
			try {
				 WebElement element = getElement(index);
				 value = element.getAttribute(name);
				 isSuccess = true;
			} catch (Exception e) {
				e.getMessage();
			}
		} while (!isSuccess && retry > 0);
	    return value;
	}

	public void setAttribute(String attribute, String value) {
		JavascriptExecutor js = (JavascriptExecutor) webDriver;
		js.executeScript(
				"document.querySelector('" + getCssSelectorValue() + "').setAttribute('" + attribute + "', '" + value + "')");
	}

	@Override
	public WebElement get(int index) {
		return getElement(index);
	}

	@Override
	public String getText() {
		return getText(0);
	}

	@Override
	public String getText(int index) {
	    scrollToView_Web(index);
		int retry = 3;
		String value = "";
	    boolean isSuccess = false;
		do {
			retry--;
			try {
				WebElement element = getElement(index);
				value = element.getText();
				 isSuccess = true;
			} catch (Exception e) {
				e.getMessage();
			}
		} while (!isSuccess && retry > 0);
	    
		return value;
	}
	
	@Override
	public List<String> getTextList() {
		List<String> stringList = new ArrayList<String>();
		
		if (isMobile()) {
			List<WebElement> elementList = getElements();
			int listSize = elementList.size();
			for (int i = 0; i < listSize; i++) {
					stringList.add(elementList.get(i).getText());
			}
		} else stringList = getTextJs();

		return stringList;
	}

	private List<String> getTextJs() {
		List<String> value = new ArrayList<String>();
		JavascriptExecutor js = (JavascriptExecutor) webDriver;

		String script = "var queryList = document.querySelectorAll(\"" + getCssSelectorValue() + "\"); "
				+ "var finalList= [];" + " for (var i=0; i<queryList.length; i++) {"
				+ "  finalList.push(queryList[i].textContent);" + "}; return finalList;";

		value = (List<String>) js.executeScript(script);
		if (!value.isEmpty())
			return value;
		
		script = "var queryList = document.querySelectorAll(\"" + getCssSelectorValue() + "\"); "
				+ "var finalList= [];" + " for (var i=0; i<queryList.length; i++) {"
				+ "  finalList.push(queryList[i].innerText);" + "}; return finalList;";

		value = (List<String>) js.executeScript(script);
		if (!value.isEmpty())
			return value;

		return value;
	}
	
	private void resetElement() {
		this.current = null;
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	
	/**
	 * gets elements based on index location
	 * @param index
	 * @return
	 */
	public WebElement getElement(int index) {
		
		if(index == 0)
			return getElement().get(0);
		else 
			return getElements().get(index);	
	}
	
	/**
	 * gets a single element
	 * @return
	 */
	public List<WebElement> getElement() {

		try {
			if (parent != null) {
				this.current = new ArrayList<WebElement>();
				this.current.add(parent.findElement(by));
			} else if (current == null) {
				this.current = new ArrayList<WebElement>();
				this.current.add(webDriver.findElement(by));
				// System.out.println("getElements() called: " + by);
			}

		} catch (Exception e) {
			e.getMessage();
		}
		return this.current;
	}
	
	public List<WebElement> getElements() {

			try {
				if (parent != null) {
					this.current = parent.findElements(by);
				} else if(current == null) {
					this.current = webDriver.findElements(by);
				//    System.out.println("getElements() called: " + by);
				}

			} catch (Exception e) {
				e.getMessage();
			}
		return this.current;
	}
	
	public List<MobileElement> getMobileElement() {
		int retry = 1;
		boolean success = false;
		
		do {
			retry--;
			try {
				if (parent != null) {
					this.mobileElement = ((AppiumDriver) parent).findElements(by);
				} else {
					this.mobileElement =((AppiumDriver) webDriver).findElements(by);
				//	System.out.println("getMobileElement() called: " + by);
				}
				success = true;

			} catch (Exception e) {
				e.getMessage();
			}
		} while (!success && retry > 0);
		
		return this.mobileElement;
	}

	@Override
	public boolean isSelected() {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public By getElementCssSelector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getRect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> X getScreenshotAs(OutputType<X> arg0) throws WebDriverException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<WebElement> findElements(By by) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebElement findElement(By by) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public By getElementCssSelector(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttribute(int index, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getElementName(int index) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isMobile() {
		if (AbstractDriver.getWebDriver() instanceof AppiumDriver) {
			return true;
		}
		return false;
	}
}
