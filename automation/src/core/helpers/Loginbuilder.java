package core.helpers;

import java.util.List;

import core.support.exceptions.loginException;
import core.support.objects.ActionObject;
import core.support.objects.ActionObject.ACTION;
import core.support.objects.DriverObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;

public class Loginbuilder {

	public Loginbuilder() {
	}

	public Loginbuilder withUsername(EnhancedBy element, String value) {
		ActionObject action = new ActionObject().withElement1(element).withValue(value).withAction(ACTION.FIELD);
		TestObject.getTestInfo().login.withLoginSequence(action);

		// save username and username field (to check if user is on log in page)
		TestObject.getTestInfo().login.withUsername(value);

		return this;
	}

	public Loginbuilder withPassword(EnhancedBy element, String value) {
		ActionObject action = new ActionObject().withElement1(element).withValue(value).withAction(ACTION.FIELD);
		TestObject.getTestInfo().login.withLoginSequence(action);

		// save password
		TestObject.getTestInfo().login.withPassword(value);

		return this;
	}

	public Loginbuilder withOptionalField(EnhancedBy element, String value) {
		ActionObject action = new ActionObject().withElement1(element).withValue(value)
				.withAction(ACTION.OPTIONAL_FIELD);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withFormSubmit(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.SUBMIT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withFormSubmit(EnhancedBy element, EnhancedBy expected, EnhancedBy optionalElemennt) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected)
				.withElement3(optionalElemennt).withAction(ACTION.BUTTON_WAIT_FIRST_ELEMENT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectNext(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.BUTTON);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withOptionalButton(EnhancedBy element) {
		ActionObject action = new ActionObject().withElement1(element).withAction(ACTION.OPTIONAL_BUTTON);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectButton(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.BUTTON);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectButtonAndWaitForFirstElement(EnhancedBy element, EnhancedBy expected,
			EnhancedBy expected2) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withElement3(expected2)
				.withAction(ACTION.BUTTON_WAIT_FIRST_ELEMENT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withWaitForElement(EnhancedBy element) {
		ActionObject action = new ActionObject().withElement1(element).withAction(ACTION.WAIT_ELEMENT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withWaitForEitherElement(EnhancedBy element, EnhancedBy element2) {
		ActionObject action = new ActionObject().withElement1(element).withElement1(element2)
				.withAction(ACTION.WAIT_EITHER_ELEMENT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withVerifyError(EnhancedBy element, String error) {
		ActionObject action = new ActionObject().withElement1(element).withValue(error).withAction(ACTION.VERIFY_TEXT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withVerifyText(EnhancedBy element, String text) {
		ActionObject action = new ActionObject().withElement1(element).withValue(text).withAction(ACTION.VERIFY_TEXT);
		TestObject.getTestInfo().login.withLoginSequence(action);

		return this;
	}

	public void build() {

		// restart the driver if single signin is set and login has changed
		Helper.handleDifferentUser();

		// if conditions for continue login are not met, return
		if (!LoginHelper.isContinueLogin())
			return;

		// set login info at default test object level
		setGlobalUserCredentials();
		
		List<ActionObject> sequence = TestObject.getTestInfo().login.getLoginSequence();
	
		ensurePageLoaded(sequence);
		
		for (ActionObject action : sequence) {

			switch (action.getAction()) {
			case FIELD:
				Helper.form.setField(action.getElement1(), action.getValue());
				break;
			case OPTIONAL_FIELD:
				if (Helper.isPresent(action.getElement1()))
					Helper.form.setField(action.getElement1(), action.getValue());
				break;
			case BUTTON:
				Helper.click.clickAndExpect(action.getElement1(), action.getElement2());
				break;
			case BUTTON_WAIT_FIRST_ELEMENT:
				Helper.click.clickAndExpect(action.getElement1(), 0, action.getElement2(), action.getElement3());
				break;
			case OPTIONAL_BUTTON:
				if (Helper.isPresent(action.getElement1()))
					Helper.clickAndWait(action.getElement1(), 0);
				break;
			case SUBMIT:
				Helper.form.formSubmitNoRetry(action.getElement1(), action.getElement2());
				break;
			case WAIT_ELEMENT:
				Helper.waitForElementToLoad(action.getElement1());
				break;
			case WAIT_EITHER_ELEMENT:
				Helper.waitForFirstElementToLoad(action.getElement1(), action.getElement2());
				break;
			case VERIFY_TEXT:
				Helper.verifyElementContainingText(action.getElement1(), action.getValue());
				break;
			default:
				Helper.assertFalse("build action is not available: " + action.getAction().name());
				break;
			}
		}
	}
	
	/**
	 * store user credentials at global level
	 * this is used to keep track of user login across different tests
	 * also store username field to check if user is at login page
	 */
	private void setGlobalUserCredentials() {
		// if single sign in is disabled, return 
		if (!CrossPlatformProperties.isSingleSignIn()) return;
		
		// set login info at suite level
		String username = TestObject.getTestInfo().login.getUsername();
		String password = TestObject.getTestInfo().login.getPassword();
		DriverObject.getCurrentDriverObject().login.withLoggedInUsername(username).withLoggedInPassword(password);
	}

	/**
	 * looks for the first element in the login builder
	 * if not displayed, then retry refreshing the page
	 * @param sequence
	 */
	private void ensurePageLoaded(List<ActionObject> sequence) {

		// return if no sequence
		if (sequence.size() == 0)
			return;

		int retry = 3;		
		EnhancedBy firstElement = sequence.get(0).getElement1();
		
		do {
			retry--;
			Helper.waitForElementToLoad(firstElement, AbstractDriver.TIMEOUT_SECONDS - 1);
			if (!Helper.isPresent(firstElement))
				Helper.refreshPage();
	
		} while (!Helper.isPresent(firstElement) && retry > 0);

		// if element is not displayed, through login exception
		if (!Helper.isPresent(firstElement))
				Helper.assertFalse("element '" + firstElement.name + "' did not load");
		
	}
}