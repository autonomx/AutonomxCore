package core.helpers;

import java.util.List;

import core.support.objects.ActionObject;
import core.support.objects.ActionObject.ACTION;
import core.support.objects.TestObject;
import core.uiCore.webElement.EnhancedBy;

public class Loginbuilder {

	public Loginbuilder() {
	}

	public Loginbuilder withUsername(EnhancedBy element, String value) {
		ActionObject action = new ActionObject().withElement1(element).withValue(value).withAction(ACTION.FIELD);
		TestObject.getTestInfo().user.withLoginSequence(action);

		// save username
		TestObject.getTestInfo().user.withUsername(value);

		return this;
	}

	public Loginbuilder withPassword(EnhancedBy element, String value) {
		ActionObject action = new ActionObject().withElement1(element).withValue(value).withAction(ACTION.FIELD);
		TestObject.getTestInfo().user.withLoginSequence(action);

		// save password
		TestObject.getTestInfo().user.withPassword(value);

		return this;
	}
	
	public Loginbuilder withOptionalField(EnhancedBy element, String value) {
		ActionObject action = new ActionObject().withElement1(element).withValue(value).withAction(ACTION.OPTIONAL_FIELD);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withFormSubmit(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.SUBMIT);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}
	
	public Loginbuilder withFormSubmit(EnhancedBy element, EnhancedBy expected, EnhancedBy optionalElemennt) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withElement3(optionalElemennt).withAction(ACTION.BUTTON_WAIT_FIRST_ELEMENT);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectNext(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.BUTTON);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}
	
	public Loginbuilder withOptionalButton(EnhancedBy element) {
		ActionObject action = new ActionObject().withElement1(element).withAction(ACTION.OPTIONAL_BUTTON);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectButton(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.BUTTON);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}
	
	public Loginbuilder withWaitForElement(EnhancedBy element) {
		ActionObject action = new ActionObject().withElement1(element).withAction(ACTION.WAIT_ELEMENT);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}
	
	public Loginbuilder withWaitForEitherElement(EnhancedBy element, EnhancedBy element2) {
		ActionObject action = new ActionObject().withElement1(element).withElement1(element2).withAction(ACTION.WAIT_EITHER_ELEMENT);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}

	public void build() {

		// restart the driver if single signin is set and user has changed
		Helper.handleDifferentUser();

		// if conditions for continue login are not met, return
		if (!LoginHelper.isContinueLogin())
			return;

		// set user info at suite level
		String username = TestObject.getTestInfo().user.getUsername();
		String password = TestObject.getTestInfo().user.getPassword();
		TestObject.getDefaultTestInfo().user.withLoggedInUsername(username).withLoggedInPassword(password);

		List<ActionObject> sequence = TestObject.getTestInfo().user.getLoginSequence();
		for (ActionObject action : sequence) {

			switch (action.getAction()) {
			case FIELD:
				Helper.form.setField(action.getElement1(), action.getValue());
				break;
			case OPTIONAL_FIELD:
				if(Helper.isPresent(action.getElement1()))
					Helper.form.setField(action.getElement1(), action.getValue());
				break;
			case BUTTON:
				Helper.click.clickAndExpect(action.getElement1(), action.getElement2());
				break;
			case BUTTON_WAIT_FIRST_ELEMENT:
				Helper.click.clickAndExpect(action.getElement1(), 0, action.getElement2(),action.getElement3());
				break;
			case OPTIONAL_BUTTON:
				if(Helper.isPresent(action.getElement1()))
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
			default:
				Helper.assertFalse("build action is not available: " + action.getAction().name());
				break;
			}
		}
	}
}