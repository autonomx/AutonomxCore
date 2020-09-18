package core.uiCore.webElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import core.helpers.Element;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;

public class ImpEnhancedWebElement implements EnhancedWebElement {

	private final String elementName;
	private final EnhancedBy element;
	private By by;
	private final WebDriver webDriver;
	private final EnhancedBy parent;
	private final int parentIndex;
	private List<WebElement> parentElements;
	private List<WebElement> current;
	public Element.LocatorType locatorType;

	public ImpEnhancedWebElement(EnhancedBy parent, int parentIndex, WebDriver webDriver, EnhancedBy element) {
		this.elementName = element.name;
		this.element = element;
		this.webDriver = webDriver;
		this.parent = parent;
		this.parentIndex = parentIndex;
		this.current = new ArrayList<WebElement>();
		parentElements = new ArrayList<WebElement>();
	}

	@Override
	public EnhancedWebElement findElement(EnhancedBy parentElement, int parentIndex, EnhancedBy element) {

		return new ImpEnhancedWebElement(parentElement, parentIndex, webDriver, element);
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
		TestLog.ConsoleLog("getSize: " + elementName);
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
		int retry = 3;

		boolean success = false;
		do {
			retry--;
			try {
				scrollToView(index);				
				// highlight the element if enabled
				Helper.highLightWebElement(element, index);
				WebElement toClick = getElement(index);
				toClick.click();
				success = true;
			
			} catch (Exception e) {
				resetElement();
				String cause = getCause(e);
				if(!cause.isEmpty())
					TestLog.ConsoleLog("click failed for element: " + elementName + ": " + cause);
				Helper.page.printStackTrace(e);
			}
		} while (!success && retry > 0);
	}

	@Override
	public void scrollToView() {
		scrollToView(0);
	}

	@Override
	public void scrollToView(int index) {
		if (isExist(index)) {
			scrollToView_Web(index);
			// TODO: currently disable, since scroll is only from center
			// mobileScroll(index);
		}
	}

	/**
	 * @param element
	 */
	public void mobileScroll(int index) {
		if (Helper.mobile_isMobile()) {
			int scrollCount = 5;
			while (!isExist() && scrollCount > 0) {
				Helper.scrollDown();
				Helper.refreshMobileApp();
				scrollCount--;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void scrollTo_mobile(int index) {
		if (Helper.mobile.isAndroid()) {
			setTimeout(1, TimeUnit.MILLISECONDS);
			WebElement element = getElement(index);
			int x = element.getLocation().getX();
			int y = element.getLocation().getY();

			TouchAction action = new TouchAction(Helper.mobile.getAndroidDriver());
			action.press(PointOption.point(x, y)).moveTo(PointOption.point(x + 90, y)).release().perform();
			setTimeout(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
		}
	}

	/**
	 * scroll to element on web
	 */
	public void scrollToView_Web(int index) {
		if (!Helper.mobile.isWebDriver())
			return;

		// if visible in view, return
		if (Helper.isVisibleInViewport(element, index))
			return;

		setTimeout(1, TimeUnit.MILLISECONDS);

		WebElement element = getElement(index);
		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.moveToElement(element);

		setTimeout(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
	}

	/**
	 * returns if element exists
	 */
	@Override
	public boolean isExist(int... index) {
		if(webDriver == null) return false;
		
		setTimeout(1, TimeUnit.MILLISECONDS);
		boolean isExist = false;

		if (index.length > 0) {
			isExist = isElementExist(index[0]);
		} else {
			isExist = isListExist();
		}
		// reset element if no element found.
		if (!isExist)
			resetElement();

		setTimeout(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
		return isExist;
	}

	/**
	 * returns if any element in a list is displayed
	 */
	public boolean isListExist() {
		List<WebElement> elements = getElements();

		if (elements == null || elements.isEmpty())
			return false;

		int size = elements.size();
		for (int i = 0; i < size; i++) {
			if (isExist(i))
				return true;
		}
		return false;
	}

	/**
	 * returns true if element is displayed sets timeout to minimum to get the value
	 * quickly
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
		return byValue.substring(byValue.indexOf(":") + 1);
	}

	@Override
	public void setValue(int index, CharSequence... keysToSend) {
		MobileElement element = (MobileElement) getElement(index);
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
		List<String> exception = new ArrayList<String>();
		boolean success = false;
		do {
			retry--;
			try {
				scrollToView(index);
				isDisplayed(index);
				Helper.highLightWebElement(element, index);
				WebElement element = getElement(index);
				element.sendKeys(keysToSend);
				success = true;
			} catch (Exception e) {
				resetElement();
				String cause = getCause(e);
				exception.add(cause);
				if(retry == 0)
				 TestLog.ConsoleLog("send keys failed for element: " + elementName + ": " + Arrays.toString(exception.toArray()));
				
			}
		} while (!success && retry > 0);
	}

	@Override
	public void sendKeysByAction(int index, CharSequence... keysToSend) {
		int retry = 3;

		boolean success = false;
		do {
			retry--;
			try {
				scrollToView(index);
				isDisplayed(index);
				if (isExist()) {
					sendKeyByAction(index, keysToSend);
					success = true;
				}
			} catch (Exception e) {
				resetElement();
				String cause = getCause(e);
				TestLog.ConsoleLog("sendkey failed: " + cause);
				Helper.page.printStackTrace(e);
			}
		} while (!success && retry > 0);

		Helper.assertTrue("send key was not successful", success);
	}

	@Override
	public void sendKeyByJs(int index, CharSequence[] keysToSend) {

		WebElement element = getElement(index);
		Helper.executeJs("arguments[0].setAttribute('value', '" + String.valueOf(keysToSend[0]) + "')", element);
	}

	/*
	 * Enter text to an element by action
	 */
	public void sendKeyByAction(int index, CharSequence[] keysToSend) {
       
		WebElement element = getElement(index);
		Actions action = new Actions(webDriver);
		action.moveToElement(element).click().sendKeys(keysToSend).build().perform();
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

	@Override
	public void setAttribute(String attribute, String value) {
		setAttribute(attribute, 0, value);
	}

	public void setAttribute(String attribute, int index, String value) {
		WebElement element = getElement(index);
		Helper.executeJs("arguments[0].setAttribute('" + attribute + "', '" + value + "')", element);
	}

	@Override
	public WebElement get(int index) {
		WebElement element = null;
		try {
			element = getElement(index);
		}catch(Exception e) {
			Helper.assertFalse("element: " + getElementName() + " at index: " + index + " was not found");
		}
		return element;
	}

	@Override
	public String getText() {
		return getText(0);
	}

	@Override
	public String getText(int index) {
		scrollToView_Web(index);
		int retry = 1;
		String value = StringUtils.EMPTY;
		boolean isSuccess = false;
		do {
			retry--;
			try {
				WebElement element = getElement(index);
				value = element.getText();
				if (value.isEmpty())
					value = getAttribute("textContent", index);
				if (value == null || value.isEmpty())
					value = getAttribute("value", index);
				if (value == null || value.isEmpty())
					value = getAttribute("innerText", index);

				if (!value.isEmpty())
					isSuccess = true;
			} catch (Exception e) {
				e.getMessage();
				value = StringUtils.EMPTY;
			}
		} while (!isSuccess && retry > 0);

		return value;
	}

	@Override
	public List<String> getTextList() {
		List<String> stringList = new ArrayList<String>();
		List<WebElement> elementList = getElements();
		
		if(elementList == null)
			return stringList;

		int listSize = elementList.size();
		for (int i = 0; i < listSize; i++) {
			stringList.add(getText(i).trim());
		}

		return stringList;
	}

	private void resetElement() {
		this.current = new ArrayList<WebElement>();
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	/**
	 * gets elements based on index location
	 * 
	 * @param index
	 * @return
	 */
	public WebElement getElement(int index) {

		WebElement element;

		if (index == 0) {
			element = getElement().get(0);
		}else {
			element = getElements().get(index);
		}
		return element;
	}

	/**
	 * gets parent elements and stores in parentElements list
	 */
	public void getParentElement() {

		// if parent locator is not set, or parent elements have already been found, do
		// not proceed
		if (parent == null || !this.parentElements.isEmpty())
			return;

		for (ElementObject elementObject : this.parent.elementObject) {
			this.by = elementObject.by;
			this.locatorType = elementObject.locatorType;

			try {
				this.current = new ArrayList<WebElement>();
				this.parentElements = webDriver.findElements(by);

				// if no element found, go to next locator
				if (this.parentElements.isEmpty())
					continue;

			} catch (Exception e) {
				e.getMessage();
			}
		}
	}

	/**
	 * gets the list of elements, then selects the first visible element from the
	 * list in situation where the first elements are not visible, out of view
	 * 
	 * @return
	 */
	public List<WebElement> getElement() {
		List<WebElement> elements = new ArrayList<WebElement>();
		if (current != null && !current.isEmpty())
			return this.current;

		// get parent elements if applicable
		getParentElement();

		// if multiple element objects, we need to iterate through them quickly
		if (this.element.elementObject.size() > 1) {
			setTimeout(1, TimeUnit.MILLISECONDS);
		}

		for (ElementObject elementObject : this.element.elementObject) {
			this.by = elementObject.by;
			this.locatorType = elementObject.locatorType;

			try {
				if (!this.parentElements.isEmpty()) {
					this.current = new ArrayList<WebElement>();
					elements = parentElements.get(parentIndex).findElements(by);
				} else if (current == null || current.isEmpty()) {
					this.current = new ArrayList<WebElement>();
					elements = webDriver.findElements(by);
				}
				// if no element found, go to next locator
				if (elements.isEmpty())
					continue;
				// get first visible element
				WebElement element = getFirstVisibleElement(elements);
				this.current.add(element);
			} catch (Exception e) {
				e.getMessage();
			}
		}
		setTimeout(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
		return this.current;
	}

	public List<WebElement> getElements() {
		if (current != null && !current.isEmpty())
			return this.current;

		// get parent elements if applicable
		getParentElement();

		// if multiple element objects, we need to iterate through them quickly
		if (this.element.elementObject.size() > 1) {
			setTimeout(1, TimeUnit.MILLISECONDS);
		}

		for (ElementObject elementObject : this.element.elementObject) {
			try {
				this.by = elementObject.by;
				this.locatorType = elementObject.locatorType;

				if (!this.parentElements.isEmpty()) {
					this.current = parentElements.get(parentIndex).findElements(by);
				} else if (current == null || current.isEmpty()) {
					this.current = webDriver.findElements(by);
				}
				// if element is found, exit loop
				if (!this.current.isEmpty())
					break;
			} catch (Exception e) {
				e.getMessage();
			}
		}
		setTimeout(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
		return this.current;
	}

	/**
	 * gets the first visible element filters hidden elements
	 * 
	 * @param elements
	 * @return
	 */
	public WebElement getFirstVisibleElement(List<WebElement> elements) {
		setTimeout(1, TimeUnit.MILLISECONDS);
		WebElement element = null;
		int count = elements.size();
		if (count > 1) {
			for (int i = 0; i < elements.size(); i++) {
				if (elements.get(i).isDisplayed()) {
					element = elements.get(i);
					break;
				}
			}
		} else {
			element = elements.get(0);
		}

		setTimeout(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS, TimeUnit.SECONDS);
		return element;
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

	@Override
	public WebElement findElement(By arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String getCause(Exception e) {
		String cause = StringUtils.EMPTY;
		try {
			cause = e.getMessage().toString();
		}catch(Exception ex) {
			return cause;
		}
		return cause;
	}
	
	private void setTimeout(long time, TimeUnit unit) {
		if(webDriver == null ) return;
		webDriver.manage().timeouts().implicitlyWait(time, unit);
	}
}
