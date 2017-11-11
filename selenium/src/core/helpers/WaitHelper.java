package core.helpers;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

import core.driver.AbstractDriver;
import core.webElement.EnhancedBy;
import core.webElement.EnhancedWebElement;

public class WaitHelper {

	/**
	 * waits for element to be displayed for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForElementToLoad(final EnhancedBy target) {

		waitForElementToLoad(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to load count() checks if the element is displayed then
	 * gets the count number
	 * 
	 * @param target
	 * @param time
	 */
	public static boolean waitForElementToLoad(final EnhancedBy target, int time) {
		return waitForElementToLoad(target, time, 1);
	}

	/**
	 * waits for element to load
	 * If mobile device, scrolls down the page until element is visible
	 * @param target:
	 *            element to wait for
	 * @param time:
	 *            max time to wait
	 * @param count:
	 *            minimum count of elements to wait for in list
	 * @return
	 */
	public static boolean waitForElementToLoad(final EnhancedBy target, int time, int count) {

		FluentWait wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(time, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					boolean isFound = Element.findElements(target).count() >= count;
					return isFound;
				}
			});
		} catch (Exception e) {
			if (time == AbstractDriver.TIMEOUT_SECONDS)
				AssertHelper.assertTrue("element: " + target.name + " did not display in allowed time (s) " + time,
						false);
			e.getMessage();
			return false;
		}
		return true;
	}

	/**
	 * waits for element count to increase from the originalCount Usefull when
	 * waiting for a list to expand with additional items
	 * 
	 * @param target
	 * @param originalCount
	 */
	public static void waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount) {

		FluentWait wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(AbstractDriver.TIMEOUT_SECONDS, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		
		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				AssertHelper.assertTrue("element: " + target.name + " did not display in allowed time (s) "
						+ AbstractDriver.TIMEOUT_SECONDS, false);

				return ListHelper.getListCount(target) > originalCount;
			}
		});
	}

	/**
	 * waits for element to not be displayed wait for maximum of 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForElementToBeRemoved(final EnhancedBy target) {
		waitForElementToBeRemoved(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to not be displayed
	 * 
	 * @param target
	 * @param time
	 *            : maximum amount of time in seconds to wait
	 */
	public static boolean waitForElementToBeRemoved(final EnhancedBy target, int time) {

		FluentWait wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(time, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					EnhancedWebElement elements = Element.findElements(target);
					try {
						for (int x = 0; x < elements.count(); x++) {
							if (elements.isExist(x)) {
								return false;
							}
						}
					} catch (Exception e) {
						e.getMessage();
					}
					return true;
				}
			});
		} catch (Exception e) {
			e.getMessage();
			return false;
		}
		return true;
	}

	/**
	 * waits for number of seconds
	 * 
	 * @param seconds
	 */
	public static void waitForSeconds(double seconds) {
		try {
			Thread.sleep((long) (seconds * 1000));
		} catch (InterruptedException e) {
			e.getMessage();
		}
		// TestLogger.logPass("Then I wait for '" + seconds + "' seconds");
	}

	/**
	 * waits for webpage to load
	 */
	public static void waitForPageToLoad() {
		
		FluentWait wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(AbstractDriver.TIMEOUT_SECONDS, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		});
	}


	/**
	 * waits for item containing in list to load
	 * 
	 * @param list
	 * @param option
	 * @param time
	 */
	public static void waitForListItemToLoad_Contains(final EnhancedBy list, String option) {
		FluentWait wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(AbstractDriver.TIMEOUT_SECONDS, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return ListHelper.isContainedInList(list, option);
				}
			});
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * waits for text to be loaded for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public static void waitForTextToLoad(final EnhancedBy target, String text) {

		waitForTextToLoad(target, AbstractDriver.TIMEOUT_SECONDS, text);
	}

	/**
	 * make sure only one element and caller needs to take responsibility to have
	 * text in the element
	 * 
	 * @param target
	 * @param time
	 */
	public static void waitForTextToLoad(final EnhancedBy target, int time, String text) {

		FluentWait wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(time, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return Element.findElements(target).getText().contains(text);
				}
			});
		} catch (Exception e) {
			e.getMessage();

		}
	}
	
	/**
	 * wait for element to become clickable
	 * @param selector
	 */
    public static void waitForElementToBeClickable(EnhancedBy selector) {
        try {
          	WebDriverWait wait; wait = new WebDriverWait(AbstractDriver.getWebDriver(), AbstractDriver.TIMEOUT_SECONDS);
          	EnhancedWebElement elements = Element.findElements(selector);
          	wait.until(ExpectedConditions.elementToBeClickable(elements.get(0)));
        } catch (Exception e) {
        		e.getMessage();
        }
    }
}