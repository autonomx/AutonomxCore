package core.support.objects;

import core.uiCore.webElement.EnhancedBy;

public class UserObject {

	/**
	 * variables
	 */
	public String username = "";
	public String password = "";
	EnhancedBy loginSuccessElement = null;
	
	public UserObject withUsername(String username) {
		this.username = username;
		return this;
	}
	
	public UserObject withPassword(String password) {
		this.password = password;
		return this;
	}
	
	public UserObject withSuccessIndicator(EnhancedBy loginSuccessElement) {
		this.loginSuccessElement = loginSuccessElement;
		return this;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public EnhancedBy getSuccessIndicator() {
		return this.loginSuccessElement;
	}

}