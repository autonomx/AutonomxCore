package core.helpers;

import org.apache.commons.lang3.StringUtils;

import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;

public class LoginHelper {

	/**
	 * if single signin enabled, And new test user is different form previous,
	 * shutdown webdriver And restart user is stored at Suite level
	 * 
	 * @throws Exception
	 */
	protected static void handleDifferentUser() {

		// if single signin is disabled, return
		if (!CrossPlatformProperties.isSingleSignIn())
			return;
		
		String username = TestObject.getTestInfo().user.getUsername();
		String password = TestObject.getTestInfo().user.getPassword();

		boolean isDifferentUser = isDifferentUser();

		// restart the driver if user name has changed
		if (isDifferentUser) {
			restartDriver(username, password);
		}
	}

	/**
	 * @return if the user has changed
	 */
	private static boolean isDifferentUser() {
		// get already logged in user name/password
		String loggedInUsername = TestObject.getDefaultTestInfo().user.getLoggedInUsername();
		String loggedInPassword = TestObject.getDefaultTestInfo().user.getLoggedInPassword();

		TestObject.getDefaultTestInfo().user.withIsLoggedIn(false);

		String username = TestObject.getTestInfo().user.getUsername();
		String password = TestObject.getTestInfo().user.getPassword();

		// user name has changed
		boolean condition1 = !StringUtils.isEmpty(loggedInUsername) && !username.equals(loggedInUsername);

		// password has changed
		boolean condition2 = username.equals(loggedInUsername) && !password.equals(loggedInPassword);

		return condition1 || condition2;
	}
	
	
	/**
	 * if single signin disabled, continue with login
	 * if enabled, continue if:
	 * 	- user has changed
	 *  - logged in user is not set
	 * @return
	 */
	protected static boolean isContinueLogin() {
		
		// if single signin is disabled, return true
		if (!CrossPlatformProperties.isSingleSignIn())
			return true;
		
		// if user has changed, continue with login
		if(isDifferentUser()) return true;
		
		// get already logged in user name/password
		String loggedInUsername = TestObject.getDefaultTestInfo().user.getLoggedInUsername();
		String loggedInPassword = TestObject.getDefaultTestInfo().user.getLoggedInPassword();
		
		if(StringUtils.isEmpty(loggedInUsername) || StringUtils.isEmpty(loggedInPassword))
			return true;

		return false;
	}

	/**
	 * shuts down and relaunch the driver
	 * 
	 * @param username
	 * @param password
	 */
	private static void restartDriver(String username, String password) {
		TestLog.ConsoleLog("logged in user has changed, restarting the driver...");
		
		DriverObject driver = TestObject.getTestInfo().currentDriver;

		// shutdown all the current drivers
		DriverObject.quitTestDrivers();
		AbstractDriver abstractDriver = new AbstractDriver();
		try {
			abstractDriver.setupDriver(driver);
		} catch (Exception e) {
			e.getMessage();
		}
	}

}