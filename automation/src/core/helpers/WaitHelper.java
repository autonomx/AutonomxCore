package core.helpers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Uninterruptibles;

import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class WaitHelper {
	
	public final static double WAIT_SHORT_SECONDS = 0.6;
	public final static double WAIT_MED_SECONDS = 1.5;
	public final static double WAIT_LONG_SECONDS = 3;
	public final static double WAIT_ZOOM_SECONDS = 0.2;
	public final static double WAIT_PAN_SECONDS = 0.3;

	/**
	 * waits for element to be displayed for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public void waitForElementToLoad(final EnhancedBy target) {

		waitForElementToLoad(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to load count() checks if the element is displayed then
	 * gets the count number
	 * 
	 * @param target
	 * @param time
	 */
	public boolean waitForElementToLoad(final EnhancedBy target, int time) {
		return waitForElementToLoad(target, time, 1);
	}

	/**
	 * waits for element to load If mobile device, scrolls down the page until
	 * element is visible
	 * 
	 * @param target:
	 *            element to wait for
	 * @param time:
	 *            max time to wait
	 * @param count:
	 *            minimum count of elements to wait for in list
	 * @return
	 */
	public boolean waitForElementToLoad(final EnhancedBy target, int time, int count) {
		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(time))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					// TestLog.ConsoleLog(Element.findElements(target).count()+"");
					return Element.findElements(target).count() >= count;
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
	 * waits for element to load and refreshes the app each time to renew the dom
	 * 
	 * @param target
	 * @return
	 */
	public boolean mobile_waitAndRefreshForElementToLoad(final EnhancedBy target) {

		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(AbstractDriver.TIMEOUT_SECONDS))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					Helper.mobile.refreshMobileApp();
					boolean isFound = Element.findElements(target).count() >= 1;
					return isFound;
				}
			});
		} catch (Exception e) {
			AssertHelper.assertTrue("element: " + target.name + " did not display in allowed time (s) "
					+ AbstractDriver.TIMEOUT_SECONDS, false);
			e.getMessage();
			return false;
		}
		return true;
	}

	/**
	 * waits for either element to load returns true when first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2) {
		return waitForFirstElementToLoad(element1, element2, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for either element to load returns true when first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2, int time) {

		Helper.assertTrue("driver is null", AbstractDriver.getWebDriver() != null);
		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(time))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					boolean isElement1Found = Element.findElements(element1).count() >= 1;
					boolean isElement2Found = Element.findElements(element2).count() >= 1;
					return (isElement1Found || isElement2Found);
				}
			});
		} catch (Exception e) {
			if (time == AbstractDriver.TIMEOUT_SECONDS)
				AssertHelper.assertTrue("element1: " + element1.name + " or element2: " + element2.name
						+ " did not display in allowed time (s) " + time, false);
			e.getMessage();
			return false;
		}
		return true;
	}

	/**
	 * waits for either element to load returns true when first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2,
			final EnhancedBy element3) {
		return waitForFirstElementToLoad(element1, element2, element3, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for either element to load returns true when first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2,
			final EnhancedBy element3, int time) {

		Helper.assertTrue("driver is null", AbstractDriver.getWebDriver() != null);
		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(AbstractDriver.TIMEOUT_SECONDS))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					boolean isElement1Found = Element.findElements(element1).count() >= 1;
					boolean isElement2Found = Element.findElements(element2).count() >= 1;
					boolean isElement3Found = Element.findElements(element3).count() >= 1;
					return (isElement1Found || isElement2Found || isElement3Found);
				}
			});
		} catch (Exception e) {
			if (time == AbstractDriver.TIMEOUT_SECONDS)
				AssertHelper.assertTrue("element1: " + element1.name + " or element2: " + element2.name
						+ " did not display in allowed time (s) " + time, false);
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
	public void waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount) {

		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(AbstractDriver.TIMEOUT_SECONDS))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		wait.until(new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				AssertHelper.assertTrue("element: " + target.name + " did not display in allowed time (s) "
						+ AbstractDriver.TIMEOUT_SECONDS, false);

				return Helper.list.getListCount(target) > originalCount;
			}
		});
	}

	/**
	 * waits for element to not be displayed wait for maximum of 60 seconds
	 * 
	 * @param target
	 */
	public void waitForElementToBeRemoved(final EnhancedBy target) {
		waitForElementToBeRemoved(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to not be displayed
	 * 
	 * @param target
	 * @param time
	 *            : maximum amount of time in seconds to wait
	 */
	public boolean waitForElementToBeRemoved(final EnhancedBy target, int time) {
		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(time))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					EnhancedWebElement elements = Element.findElements(target);
					try {
						// TestLog.ConsoleLog(Element.findElements(target).count()+"");

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
	public void waitForSeconds(double seconds) {
		long miliseconds = (long) (seconds * 1000);
		Uninterruptibles.sleepUninterruptibly(miliseconds, TimeUnit.MILLISECONDS);
		// TestLogger.logPass("Then I wait for '" + seconds + "' seconds");
	}

	/**
	 * waits for webpage to load
	 */
	public void waitForPageToLoad() {

		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(AbstractDriver.TIMEOUT_SECONDS))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

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
	public void waitForListItemToLoad_Contains(final EnhancedBy list, String option) {
		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(AbstractDriver.TIMEOUT_SECONDS))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					return Helper.list.isContainedInList(list, option);
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
	public void waitForTextToLoad(final EnhancedBy target, String text) {

		waitForTextToLoad(target, AbstractDriver.TIMEOUT_SECONDS, text);
	}

	/**
	 * make sure only one element and caller needs to take responsibility to have
	 * text in the element
	 * 
	 * @param target
	 * @param time
	 */
	public void waitForTextToLoad(final EnhancedBy target, int time, String text) {

		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(time))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

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

	public boolean waitForElementToBeClickable(EnhancedBy selector) {
		return waitForElementToBeClickable(selector, AbstractDriver.TIMEOUT_SECONDS);
	}

	public boolean waitForElementToBeClickable(final EnhancedBy target, int time) {

		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(time))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);

		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					// TestLog.ConsoleLog(Element.findElements(target).count()+"");
					EnhancedWebElement elements = Element.findElements(target);
					return elements.count() >= 1 && elements.isEnabled();
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
	
	public boolean waitForClassContain(final EnhancedBy target, int index, String value) {
		return waitForClassContain(target, index,  value, AbstractDriver.TIMEOUT_SECONDS);
	}
	
	public boolean waitForClassContain(final EnhancedBy target, int index, String value, int time) {
		FluentWait<WebDriver> wait = new FluentWait<>(AbstractDriver.getWebDriver()).withTimeout(Duration.ofSeconds(time))
				.pollingEvery(Duration.ofMillis(5)).ignoring(NoSuchElementException.class);
		try {
			wait.until(new Function<WebDriver, Boolean>() {
				@Override
				public Boolean apply(WebDriver driver) {
					EnhancedWebElement elements = Element.findElements(target);
					return elements.getAttribute(index, "class").contains(value);
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
}