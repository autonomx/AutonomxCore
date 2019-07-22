package core.uiCore.webElement;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public interface EnhancedWebElement extends WebElement {

	void click();

	void click(int index);

	void moveMouse();

	void moveMouse(int index);

	EnhancedWebElement findElement(EnhancedBy enhanceBy, EnhancedWebElement parentElement);

	By getElementCssSelector();

	By getElementCssSelector(int index);

	void sendKeys(CharSequence... keysToSend);

	void sendKeys(int index, CharSequence... keysToSend);
	
	void sendKeysByAction(int index, CharSequence... keysToSend);

	void scrollToView();

	void scrollToView_Web(int index);

	String getAttribute(String name);

	String getAttribute(int index, String name);

	String getText();

	String getText(int index);

	String getElementName();

	String getElementName(int index);

	int count();

	boolean isEmpty();

	List<WebElement> getElements();

	By getBy();

	Point getLocation(int index);

	String getCssValue(String arg0, int index);

	void clear(int index);

	boolean isEnabled(int index);

	boolean isDisplayed(int index);

	String getAttribute(String name, int index);

	void setAttribute(String attribute, String value);

	WebElement get(int index);

	boolean isExist(int... index);

	void setValue(int index, CharSequence... value);

	List<String> getTextList();

	void scrollToView(int index);

	void sendKeyByJs(int index, CharSequence[] keysToSend);
}
