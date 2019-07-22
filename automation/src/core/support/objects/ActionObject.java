package core.support.objects;

import org.apache.commons.lang.StringUtils;

import core.uiCore.webElement.EnhancedBy;

public class ActionObject {
	
	// action of actions
	public enum ACTION { FIELD, SUBMIT, BUTTON, OPTIONAL_BUTTON, OPTIONAL_FIELD, BUTTON_WAIT_FIRST_ELEMENT, WAIT_ELEMENT, WAIT_EITHER_ELEMENT }
	
	private EnhancedBy element1;
	private EnhancedBy element2;
	private EnhancedBy element3;
	private String value = StringUtils.EMPTY;
	private ACTION action;
	
	public ActionObject withElement1(EnhancedBy element1) {
		this.element1 = element1;
		return this;
	}
	
	public ActionObject withElement2(EnhancedBy element2) {
		this.element2 = element2;
		return this;
	}
	
	public ActionObject withElement3(EnhancedBy element3) {
		this.element3 = element3;
		return this;
	}
	
	public ActionObject withValue(String value) {
		this.value = value;
		return this;
	}
	
	public ActionObject withAction(ACTION sequence) {
		this.action = sequence;
		return this;
	}
	
	public EnhancedBy getElement1() {
		return this.element1;
	}
	
	public EnhancedBy getElement2() {
		return this.element2;
	}
	
	public EnhancedBy getElement3() {
		return this.element3;
	}
	
	public String getValue() {
		return this.value;
	}
	
	public ACTION getAction() {
		return this.action;
	}
	
}