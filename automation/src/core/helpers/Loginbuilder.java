package core.helpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.support.objects.ActionObject;
import core.support.objects.ActionObject.ACTION;
import core.support.objects.TestObject;
import core.support.objects.UserObject;
import core.support.objects.UserObject.LoginSequence;
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

	public Loginbuilder withFormSubmit(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.SUBMIT);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectNext(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.BUTTON);
		TestObject.getTestInfo().user.withLoginSequence(action);

		return this;
	}

	public Loginbuilder withSelectButton(EnhancedBy element, EnhancedBy expected) {
		ActionObject action = new ActionObject().withElement1(element).withElement2(expected).withAction(ACTION.BUTTON);
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
			case BUTTON:
				Helper.click.clickAndExpect(action.getElement1(), action.getElement2());
				break;
			case SUBMIT:
				Helper.form.formSubmit(action.getElement1(), action.getElement1());
				break;
			default:
				Helper.assertFalse("build action is not available: " + action.getAction().name());
				break;
			}
		}
	}
}