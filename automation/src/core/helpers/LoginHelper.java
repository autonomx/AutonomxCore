package core.helpers;

import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;

public class LoginHelper {

	/**
	 * if single signin enabled, And new test user is different form previous,
	 * shutdown webdriver And restart
	 * 
	 * @param newUserName
	 * @throws Exception
	 */
	protected static void handleDifferentUser(String newUserName, String password) {

		TestObject.getTestInfo().withLoggedInUser(newUserName);
		TestObject.getTestInfo().withLoggedInPassword(password);
		TestObject.getTestInfo().withIsLoggedIn(false);

		if (CrossPlatformProperties.isSingleSignIn()) {
			String previousTestId = DriverObject.getPreviousTestId();
			if (previousTestId == null)
				return; // do not restart test if this is the first test

			String previousUser = TestObject.getTestInfo(previousTestId).loggedInUser;
			String previousPassword = TestObject.getTestInfo(previousTestId).loggedInPassword;
			boolean condition1 = !newUserName.equals(previousUser); // user is not the same
			boolean condition2 = newUserName.equals(previousUser) && !password.equals(previousPassword); // password has
																											// changed
			if ((condition1) || condition2) {
				DriverObject driver = TestObject.getTestInfo().currentDriver;
				DriverObject.quitTestDrivers();
				AbstractDriver abstractDriver = new AbstractDriver();
				try {
					abstractDriver.setupDriver(driver);
				} catch (Exception e) {
					e.getMessage();
				}
			}
		}
	}

}