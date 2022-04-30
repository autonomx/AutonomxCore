package core.helpers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.util.concurrent.Uninterruptibles;

import core.support.configReader.Config;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import core.uiCore.webElement.EnhancedWebElement;

public class WaitHelper {

	/**
	 * waits for element to be displayed for amount of time specified by 60 seconds
	 * 
	 * @param target
	 */
	public boolean waitForElementToLoad(final EnhancedBy target) {

		return waitForElementToLoad(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to load count() checks if the element is displayed Then
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
	 * @param target: element to wait for
	 * @param time:   max time to wait
	 * @param count:  minimum count of elements to wait for in list
	 * @return
	 */
	public boolean waitForElementToLoad(final EnhancedBy target, int time, int count) {
		if(AbstractDriver.getWebDriver() == null) return false;
		
		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return Element.findElements(target).count() >= count;
			}
		};

		return waitForCondition(condition, target, time);
	}

	/**
	 * waits for element to load And refreshes the app each time to renew the dom
	 * 
	 * @param target
	 * @return
	 */
	public void mobile_waitAndRefreshForElementToLoad(final EnhancedBy target) {
		mobile_waitAndRefreshForElementToLoad(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to load And refreshes the app each time to renew the dom
	 * 
	 * @param target
	 * @return
	 */
	public void mobile_waitAndRefreshForElementToLoad(final EnhancedBy target, int time) {
		if(AbstractDriver.getWebDriver() == null) return;
		
		if (!Helper.mobile_isMobile())
			return;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				Helper.mobile.refreshMobileApp();
				boolean isFound = Element.findElements(target).count() >= 1;
				return isFound;
			}
		};
		waitForCondition(condition, target, time);
	}

	/**
	 * waits for either element to load returns true When first item loads
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
	 * waits for either element to load returns true When first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2, int time) {

		if(AbstractDriver.getWebDriver() == null) return false;
		
		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				boolean isElement1Found = Element.findElements(element1).count() >= 1;
				boolean isElement2Found = Element.findElements(element2).count() >= 1;
				return (isElement1Found || isElement2Found);
			}
		};
		return waitForCondition(condition, element1, time);
	}

	/**
	 * waits for either element to load returns true When first item loads
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
	 * waits for either element to load returns true When first item loads
	 * 
	 * @param element1
	 * @param element2
	 * @param time
	 * @return
	 */
	public boolean waitForFirstElementToLoad(final EnhancedBy element1, final EnhancedBy element2,
			final EnhancedBy element3, int time) {

		if(AbstractDriver.getWebDriver() == null) return false;
		
		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				boolean isElement1Found = Element.findElements(element1).count() >= 1;
				boolean isElement2Found = Element.findElements(element2).count() >= 1;
				boolean isElement3Found = Element.findElements(element3).count() >= 1;
				return (isElement1Found || isElement2Found || isElement3Found);
			}
		};
		return waitForCondition(condition, element1, time);
	}

	/**
	 * waits for element count to increase from the originalCount Usefull When
	 * waiting for a list to expand with additional items
	 * 
	 * @param target
	 * @param originalCount
	 * @return
	 */
	public boolean waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount) {
		return waitForAdditionalElementsToLoad(target, originalCount, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element count to increase from the originalCount Usefull When
	 * waiting for a list to expand with additional items
	 * 
	 * @param target
	 * @param originalCount
	 * @return
	 */
	public boolean waitForAdditionalElementsToLoad(final EnhancedBy target, final int originalCount, int time) {
		if(AbstractDriver.getWebDriver() == null) return false;

		Helper.assertTrue("driver is null", AbstractDriver.getWebDriver() != null);

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return Helper.list.getListCount(target) > originalCount;
			}
		};
		return waitForCondition(condition, target, time);
	}

	/**
	 * waits for element to not be displayed wait for maximum of 60 seconds
	 * 
	 * @param target
	 * @return 
	 */
	public boolean waitForElementToBeRemoved(final EnhancedBy target) {
		return waitForElementToBeRemoved(target, AbstractDriver.TIMEOUT_SECONDS);
	}
	
	/* waits for element to not be displayed wait for maximum of 60 seconds
	 * 
	 * @param target
	 */
	public boolean waitForElementToBeRemoved(final EnhancedBy target, int time, int waitForTargetToLoadInSeconds) {
		waitForElementToLoad(target, waitForTargetToLoadInSeconds);
		return waitForElementToBeRemoved(target, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for element to not be displayed
	 * 
	 * @param target
	 * @param time   : maximum amount of time in seconds to wait
	 */
	public boolean waitForElementToBeRemoved(final EnhancedBy target, int time) {
		if(AbstractDriver.getWebDriver() == null) return false;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
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
		};
		return waitForCondition(condition, target, time);
	}

	/**
	 * waits for number of seconds
	 * 
	 * @param seconds
	 */
	public void waitForSeconds(double seconds) {
		long miliseconds = (long) (seconds * 1000);
		Uninterruptibles.sleepUninterruptibly(miliseconds, TimeUnit.MILLISECONDS);
	}

	/**
	 * waits for web page to load
	 * 
	 * @return
	 */
	public void waitForPageToLoad() {
		if(AbstractDriver.getWebDriver() == null) return;
		
		// only applies to web pages
		if (!Helper.isWebDriver())
			return;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		waitForCondition(condition, null, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * waits for item containing in list to load
	 * 
	 * @param list
	 * @param option
	 * @param time
	 */
	public void waitForListItemToLoad_Contains(final EnhancedBy list, String option) {
		if(AbstractDriver.getWebDriver() == null) return;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return Helper.list.isContainedInList(list, option);
			}
		};
		waitForCondition(condition, list, AbstractDriver.TIMEOUT_SECONDS);
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
	 * make sure only one element And caller needs to take responsibility to have
	 * text in the element
	 * 
	 * @param target
	 * @param time
	 */
	public void waitForTextToLoad(final EnhancedBy target, int time, String text) {
		if(AbstractDriver.getWebDriver() == null) return;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return Element.findElements(target).getText().contains(text);
			}
		};
		waitForCondition(condition, target, time);
	}

	public boolean waitForElementToBeClickable(EnhancedBy selector) {
		return waitForElementToBeClickable(selector, AbstractDriver.TIMEOUT_SECONDS);
	}

	public boolean waitForElementToBeClickable(final EnhancedBy target, int time) {
		if(AbstractDriver.getWebDriver() == null) return false;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				EnhancedWebElement elements = Element.findElements(target);
				return elements.count() >= 1 && elements.isEnabled();
			}
		};
		return waitForCondition(condition, target, time);
	}

	/**
	 * wait for class to contain value
	 * 
	 * @param target
	 * @param index
	 * @param value
	 * @return
	 */
	public boolean waitForClassContain(final EnhancedBy target, int index, String value) {
		return waitForClassContain(target, index, value, AbstractDriver.TIMEOUT_SECONDS);
	}

	/**
	 * wait for class to contain value
	 * 
	 * @param target
	 * @param index
	 * @param value
	 * @param time
	 * @return
	 */
	public boolean waitForClassContain(final EnhancedBy target, int index, String value, int time) {
		if(AbstractDriver.getWebDriver() == null) return false;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				EnhancedWebElement elements = Element.findElements(target);
				return elements.getAttribute(index, "class").contains(value);
			}
		};
		return waitForCondition(condition, target, time);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public boolean waitForAnyTextToLoadContaining(final EnhancedBy target, String... text) {
		return waitForAnyTextToLoadContaining(target, AbstractDriver.TIMEOUT_SECONDS, text);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public boolean waitForAnyTextToLoadContaining(final EnhancedBy target, int time, String... text) {
		if(AbstractDriver.getWebDriver() == null) return false;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				EnhancedWebElement elements = Element.findElements(target);
				String actualValue = elements.getText();

				for (String value : text) {
					if (actualValue.contains(value))
						return true;
				}
				return false;
			}
		};
		return waitForCondition(condition, target, time);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public boolean waitForAnyTextToLoad(final EnhancedBy target, String... text) {
		return waitForAnyTextToLoad(target, AbstractDriver.TIMEOUT_SECONDS, text);
	}

	/**
	 * wait for any text strings to become available
	 * 
	 * @param target
	 * @param time
	 * @param text
	 * @return
	 */
	public boolean waitForAnyTextToLoad(final EnhancedBy target, int time, String... text) {
		if(AbstractDriver.getWebDriver() == null) return false;

		ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				EnhancedWebElement elements = Element.findElements(target);
				String actualValue = elements.getText();
				for (String value : text) {
					if (actualValue.equals(value))
						return true;
				}
				return false;
			}
		};
		return waitForCondition(condition, target, time);
	}

	/**
	 * wait for condition to be true
	 * 
	 * @param condition
	 * @param target
	 * @param time
	 * @return
	 */
	public boolean waitForCondition(ExpectedCondition<Boolean> condition, EnhancedBy target, int time) {
		if(AbstractDriver.getWebDriver() == null) return false;
		
		waitAllJSRequests(time); // if is web and flag is enabled
		
		Wait<WebDriver> wait = new WebDriverWait(AbstractDriver.getWebDriver(), Duration.ofSeconds(time)).pollingEvery(Duration.ofMillis(5))
				.withTimeout(Duration.ofSeconds(time)).ignoring(Exception.class);
		try {
			wait.until(condition);
		} catch (Exception e) {
			if (time == AbstractDriver.TIMEOUT_SECONDS && target != null) {
				AssertHelper.assertTrue(
						"element: " + target.name + " did not meet condition in allowed time (s) " + time, false);
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	public void ajaxComplete() {
		JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();
		try {
			jsExec.executeScript("var callback = arguments[arguments.length - 1];" + "var xhr = new XMLHttpRequest();"
				+ "xhr.open('GET', '/Ajax_call', true);" + "xhr.onreadystatechange = function() {"
				+ "  if (xhr.readyState == 4) {" + "    callback(xhr.responseText);" + "  }" + "};" + "xhr.send();");
		}catch(Exception e) {
			e.getMessage();
		}
	}

	public void waitForJQueryLoad(int time) {
		WebDriverWait jsWait = new WebDriverWait(AbstractDriver.getWebDriver(), Duration.ofSeconds(time));
		JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();

		try {
			ExpectedCondition<Boolean> jQueryLoad = driver -> ((Long) ((JavascriptExecutor) AbstractDriver
					.getWebDriver()).executeScript("return jQuery.active") == 0);
			boolean jqueryReady = (Boolean) jsExec.executeScript("return jQuery.active==0");
			if (!jqueryReady) {
				jsWait.until(jQueryLoad);
			}
		} catch (WebDriverException ignored) {
		}
	}

	public void waitForAngularLoad(int time) {
		String angularReadyScript = "return angular.element(document).injector().get('$http').pendingRequests.length === 0";
		angularLoads(angularReadyScript, time);
	}

	public void waitUntilJSReady(int time) {
		WebDriverWait jsWait = new WebDriverWait(AbstractDriver.getWebDriver(), Duration.ofSeconds(time));
		JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();

		try {
			ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) AbstractDriver.getWebDriver())
					.executeScript("return document.readyState").toString().equals("complete");
			Boolean jsReady = jsExec.executeScript("return document.readyState").toString().equals("complete");
			if(jsReady == null ) return;

			if (!jsReady) {
				jsWait.until(jsLoad);
			}
		} catch (WebDriverException ignored) {
		}
	}

	public void waitUntilJQueryReady(int time) {

		try {
			JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();
			Boolean jQueryDefined = (Boolean) jsExec.executeScript("return typeof jQuery != 'undefined'");
			if(jQueryDefined == null ) return;
	
			if (jQueryDefined) {
				Helper.waitForSeconds(0.02);
				waitForJQueryLoad(time);
				Helper.waitForSeconds(0.2);
			}
		}catch(Exception e) {
			e.getMessage();
		}
	}

	public void waitUntilAngularReady(int time) {
		JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();

		try {
			Boolean angularUnDefined = (Boolean) jsExec.executeScript("return window.angular === undefined");
			if(angularUnDefined == null ) return;
			if (!angularUnDefined) {
				Boolean angularInjectorUnDefined = (Boolean) jsExec
						.executeScript("return angular.element(document).injector() === undefined");
				if(angularInjectorUnDefined == null ) return;

				if (!angularInjectorUnDefined) {
					Helper.waitForSeconds(0.02);
					waitForAngularLoad(time);
					Helper.waitForSeconds(0.02);
				}
			}
		} catch (WebDriverException ignored) {
		}
	}

	public void waitUntilAngular5Ready(int time) {
		JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();

		try {
			Object angular5Check = jsExec
					.executeScript("return getAllAngularRootElements()[0].attributes['ng-version']");
			if (angular5Check != null) {
				Boolean angularPageLoaded = (Boolean) jsExec
						.executeScript("return window.getAllAngularTestabilities().findIndex(x=>!x.isStable()) === -1");
				if(angularPageLoaded == null ) return;

				if (!angularPageLoaded) {
					Helper.waitForSeconds(0.02);
					waitForAngular5Load(time);
					Helper.waitForSeconds(0.02);
				}
			}
		} catch (WebDriverException ignored) {
		}
	}

	public void waitForAngular5Load(int time) {
		String angularReadyScript = "return window.getAllAngularTestabilities().findIndex(x=>!x.isStable()) === -1";
		angularLoads(angularReadyScript, time);
	}

	public void angularLoads(String angularReadyScript, int time) {
		WebDriverWait jsWait = new WebDriverWait(AbstractDriver.getWebDriver(), Duration.ofSeconds(time));
		JavascriptExecutor jsExec = (JavascriptExecutor) AbstractDriver.getWebDriver();

		try {
			ExpectedCondition<Boolean> angularLoad = driver -> Boolean
					.valueOf(((JavascriptExecutor) driver).executeScript(angularReadyScript).toString());
			Boolean angularReady = Boolean.valueOf(jsExec.executeScript(angularReadyScript).toString());
			if(angularReady == null ) return;
			if (!angularReady) {
				jsWait.until(angularLoad);
			}
		} catch (WebDriverException ignored) {
		}
	}

	public void waitAllJSRequests(int time) {
		if(Helper.mobile.isWebDriver() && Config.getBooleanValue("global.web.JsWait.enabled")) {
			
			waitUntilJSReady(time);
			waitUntilJQueryReady(time);
			waitUntilAngularReady(time);
			waitUntilAngular5Ready(time);
		}
	}
}