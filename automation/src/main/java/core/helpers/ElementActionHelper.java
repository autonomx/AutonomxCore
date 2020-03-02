package core.helpers;

import org.openqa.selenium.interactions.Actions;

import core.support.logger.TestLog;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class ElementActionHelper {

	/**
	 * hover over element
	 * 
	 * @param by
	 */
	protected static void hoverBy(EnhancedBy by, int index) {

		Actions actions = new Actions(AbstractDriver.getWebDriver());
		EnhancedWebElement targetElement = Element.findElements(by);
		actions.moveToElement(targetElement.get(index)).build().perform();
		Helper.waitForSeconds(0.5);
	}

	/*
	 * Enter text to an element by action
	 */
	protected static void inputTextByAction(EnhancedBy by, String text) {
		EnhancedWebElement targetElement = Element.findElements(by);
		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.moveToElement(targetElement.get(0)).click().sendKeys(text).build().perform();
	}

	/*
	 * Double click an element
	 */
	protected static void doubleClickBy(EnhancedBy by) {
		EnhancedWebElement targetElement = Element.findElements(by);
		Helper.wait.waitForElementToBeClickable(by);
		Actions action = new Actions(AbstractDriver.getWebDriver());
		action.doubleClick(targetElement.get(0)).perform();
	}

	/**
	 * Hover on the x,y points Reset mouse position to 0,0 after hover action is
	 * complete with Helper.click.resetMouse(x, y);
	 * 
	 * @param x
	 * @param y
	 */
	protected static void hoverPoints(int x, int y) {
		Actions action = new Actions(AbstractDriver.getWebDriver());

		TestLog.ConsoleLog("Hovering at: point x: " + x + " point y: " + y);

		action.moveByOffset(x, y).build().perform();
		Helper.waitForSeconds(0.5);
	}

	/**
	 * move to element by using action
	 * 
	 * @param target
	 * @param index
	 */
	protected static void moveToElement(EnhancedBy target, int index) {
		Actions action = new Actions(AbstractDriver.getWebDriver());
		EnhancedWebElement targetElement = Element.findElements(target);
		action.moveToElement(targetElement.get(index));
	}

	/**
	 * move to element by using action
	 * 
	 * @param target
	 * @param index
	 */
	protected static void moveToElement(EnhancedBy target, int index, int xOffset, int yOffset) {
		Actions action = new Actions(AbstractDriver.getWebDriver());
		EnhancedWebElement targetElement = Element.findElements(target);
		action.moveToElement(targetElement.get(index), xOffset, yOffset);
	}
}