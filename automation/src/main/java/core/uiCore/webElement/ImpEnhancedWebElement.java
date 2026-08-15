package core.uiCore.webElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import core.helpers.Element;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.drivers.DriverTimeoutManager;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;

@SuppressWarnings("deprecation")
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
				if (isDisplayed(index)) {
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
		List<WebElement> elements = getElements();
		return elements.size();
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
		if (!Helper.mobile.isWebDriver())
			return;
		if(isElementFound(index))
			scrollToView_Web(index);
	}

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
			Duration previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
			try {
				WebElement element = getElement(index);
				int x = element.getLocation().getX();
				int y = element.getLocation().getY();
				TouchAction action = new TouchAction(Helper.mobile.getAndroidDriver());
				action.press(PointOption.point(x, y)).moveTo(PointOption.point(x + 90, y)).release().perform();
			} finally {
				restoreTimeout(previousTimeout);
			}
		}
	}

	public void scrollToView_Web(int index) {
		if (!Helper.mobile.isWebDriver())
			return;
		if (Helper.isVisibleInViewport(element, index))
			return;
		scrollToView_Web_Action(index);
		if (Helper.isVisibleInViewport(element, index))
			return;
		scrollToView_Web_JS(index);
	}

	public void scrollToView_Web_Action(int index) {
		if (!Helper.mobile.isWebDriver())
			return;
		if (Helper.isVisibleInViewport(element, index))
			return;
		Duration previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		try {
			WebElement element = getElement(index);
			Actions action = new Actions(AbstractDriver.getWebDriver());
			action.moveToElement(element);
		} finally {
			restoreTimeout(previousTimeout);
		}
	}

	public void scrollToView_Web_JS(int index) {
		if (!Helper.mobile.isWebDriver())
			return;
		if (Helper.isVisibleInViewport(element, index))
			return;
		Duration previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		try {
			WebElement element = getElement(index);
			((JavascriptExecutor)AbstractDriver.getWebDriver()).executeScript("arguments[0].scrollIntoView();", element);
		} finally {
			restoreTimeout(previousTimeout);
		}
	}

	@Override
	public boolean isExist(int... index) {
		if(webDriver == null) return false;
		Duration previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		boolean isExist = false;
		try {
			if (index.length > 0)
				isExist = isElementExist(index[0]);
			else
				isExist = isListExist();
			if (!isExist)
				resetElement();
			return isExist;
		} catch(Exception e) {
			resetElement();
			return false;
		} finally {
			restoreTimeout(previousTimeout);
		}
	}

	public boolean isListExist() {
		try {
			List<WebElement> elements = getElements();
			if (elements == null || elements.isEmpty())
				return false;
			for (WebElement element : elements) {
				try {
					if (element.isDisplayed())
						return true;
				} catch (Exception e) {
				}
			}
		} catch(Exception e) {
			return false;
		}
		return false;
	}

	public boolean isElementExist(int index) {
		boolean isElementExists = false;
		try {
			WebElement element = getElement(index);
			if (element.isDisplayed())
				isElementExists = true;
		} catch (Exception e) {
			isElementExists = false;
		}
		return isElementExists;
	}

	public boolean isElementFound(int index) {
		Duration previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		try {
			return getElement(index) != null;
		} catch (Exception e) {
			return false;
		} finally {
			restoreTimeout(previousTimeout);
		}
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
				if (isDisplayed(index)) {
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
		return get(index, true);
	}

	@Override
	public WebElement get(int index, boolean isFail) {
		WebElement element = null;
		try {
			element = getElement(index);
		}catch(Exception e) {
			if(isFail)
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
				if (value.isEmpty()) value = getAttribute("textContent", index);
				if (value == null || value.isEmpty()) value = getAttribute("value", index);
				if (value == null || value.isEmpty()) value = getAttribute("innerText", index);
				if (!value.isEmpty()) isSuccess = true;
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
		for (int i = 0; i < listSize; i++)
			stringList.add(getText(i).trim());
		return stringList;
	}

	private void resetElement() {
		this.current = new ArrayList<WebElement>();
	}

	@Override
	public String getElementName() {
		return elementName;
	}

	@Override
	public Select getSelect(int index) {
		scrollToView_Web(index);
		int retry = 1;
		Select selectElement = null;
		boolean isSuccess = false;
		do {
			retry--;
			try {
				WebElement element = getElement(index);
				selectElement = new Select(element);
			} catch (Exception e) {
				e.getMessage();
				selectElement = null;
			}
		} while (!isSuccess && retry > 0);
		return selectElement;
	}

	public WebElement getElement(int index) {
		WebElement element;
		if (index == 0)
			element = getElement().get(0);
		else
			element = getElements().get(index);
		return element;
	}

	public void getParentElement() {
		if (parent == null || !this.parentElements.isEmpty())
			return;
		Duration previousTimeout = null;
		if (this.parent.elementObject.size() > 1)
			previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		try {
			for (ElementObject elementObject : this.parent.elementObject) {
				this.by = elementObject.by;
				this.locatorType = elementObject.locatorType;
				try {
					this.current = new ArrayList<WebElement>();
					this.parentElements = webDriver.findElements(by);
					if (this.parentElements.isEmpty())
						continue;
					break;
				} catch (Exception e) {
					e.getMessage();
				}
			}
		} finally {
			if (previousTimeout != null)
				restoreTimeout(previousTimeout);
		}
	}

	public List<WebElement> getElement() {
		List<WebElement> elements = new ArrayList<WebElement>();
		if (current != null && !current.isEmpty())
			return this.current;
		getParentElement();
		Duration previousTimeout = null;
		if (this.element.elementObject.size() > 1)
			previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		try {
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
					if (elements.isEmpty())
						continue;
					WebElement element = getFirstVisibleElement(elements);
					if (element != null)
						this.current.add(element);
				} catch (Exception e) {
					e.getMessage();
				}
			}
			return this.current;
		} finally {
			if (previousTimeout != null)
				restoreTimeout(previousTimeout);
		}
	}

	public List<WebElement> getElements() {
		if (current != null && !current.isEmpty())
			return this.current;
		getParentElement();
		Duration previousTimeout = null;
		if (this.element.elementObject.size() > 1)
			previousTimeout = setTemporaryTimeout(1, TimeUnit.MILLISECONDS);
		try {
			for (ElementObject elementObject : this.element.elementObject) {
				try {
					this.by = elementObject.by;
					this.locatorType = elementObject.locatorType;
					if (!this.parentElements.isEmpty())
						this.current = parentElements.get(parentIndex).findElements(by);
					else if (current == null || current.isEmpty())
						this.current = webDriver.findElements(by);
					if (!this.current.isEmpty())
						break;
				} catch (Exception e) {
					e.getMessage();
				}
			}
			return this.current;
		} finally {
			if (previousTimeout != null)
				restoreTimeout(previousTimeout);
		}
	}

	public WebElement getFirstVisibleElement(List<WebElement> elements) {
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
		return element;
	}

	@Override
	public boolean isSelected() { return false; }

	@Override
	public By getElementCssSelector() { return null; }

	@Override
	public Rectangle getRect() { return null; }

	@Override
	public <X> X getScreenshotAs(OutputType<X> arg0) throws WebDriverException { return null; }

	@Override
	public List<WebElement> findElements(By by) { return null; }

	@Override
	public By getElementCssSelector(int index) { return null; }

	@Override
	public String getAttribute(int index, String name) { return null; }

	@Override
	public String getElementName(int index) { return null; }

	@Override
	public WebElement findElement(By arg0) { return null; }

	private String getCause(Exception e) {
		String cause = StringUtils.EMPTY;
		try {
			cause = e.getMessage().toString();
		}catch(Exception ex) {
			return cause;
		}
		return cause;
	}

	private Duration setTemporaryTimeout(long time, TimeUnit unit) {
		Duration previousTimeout = DriverTimeoutManager.getImplicitWait(webDriver,
				Duration.ofSeconds(AbstractDriver.TIMEOUT_IMPLICIT_SECONDS));
		setTimeout(time, unit);
		return previousTimeout;
	}

	private void restoreTimeout(Duration timeout) {
		if (webDriver == null || timeout == null)
			return;
		try {
			DriverTimeoutManager.setImplicitWait(webDriver, timeout);
		}catch(NoSuchSessionException e) {
			Helper.page.printStackTrace(e);
		}
	}

	private void setTimeout(long time, TimeUnit unit) {
		if(webDriver == null ) return;
		try {
			DriverTimeoutManager.setImplicitWait(webDriver, Duration.ofNanos(unit.toNanos(time)));
		}catch(NoSuchSessionException e) {
			Helper.page.printStackTrace(e);
		}
	}

	@Override
	public void setValue(int index, CharSequence... value) {
	}
}
