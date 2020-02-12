package core.support.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LoginObject {
	
	// sequence of actions for login login
	public enum LoginSequence { USERNAME, PASSWORD, SUBMIT, NEXT, BUTTON }

	private String username = StringUtils.EMPTY;
	private String password = StringUtils.EMPTY;
	private String loggedInUsername = StringUtils.EMPTY;
	private String loggedInPassword = StringUtils.EMPTY;
	private Boolean isLoggedIn = false;
	private List<ActionObject> loginSequence = new ArrayList<ActionObject>();

	public LoginObject withUsername(String username) {
		this.username = username;
		return this;
	}
	
	public LoginObject withPassword(String password) {
		this.password = password;
		return this;
	}
	
	public LoginObject withLoggedInUsername(String loggedInUsername) {
		this.loggedInUsername = loggedInUsername;
		return this;
	}
	
	public LoginObject withLoggedInPassword(String loggedInPassword) {
		this.loggedInPassword = loggedInPassword;
		return this;
	}
	
	public LoginObject withIsLoggedIn(Boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
		return this;
	}
	
	public LoginObject withLoginSequence(ActionObject loginSequence) {
		this.loginSequence.add(loginSequence);
		return this;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getLoggedInUsername() {
		return this.loggedInUsername;
	}
	
	public String getLoggedInPassword() {
		return this.loggedInPassword;
	}
	
	public Boolean getIsLoggedIn() {
		return this.isLoggedIn;
	}
	
	public List<ActionObject> getLoginSequence(){
		return this.loginSequence;
	}
	
}