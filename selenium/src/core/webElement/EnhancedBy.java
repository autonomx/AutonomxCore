package core.webElement;

import org.openqa.selenium.By;

public class EnhancedBy {

	public By by;
	public String name;

	/**
	 * gets by value for web elements
	 * 
	 * @param by
	 * @param name
	 */
	public EnhancedBy(By by, String name) {
		this.by = by;
		this.name = name;

	}
	
	public EnhancedBy withBy(By by) {
		this.by = by;
		return this;
	}
	
	public EnhancedBy withName(String name) {
		this.name = name;
		return this;
	}
}
