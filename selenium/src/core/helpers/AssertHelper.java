package core.helpers;

import org.junit.Assert;

public class AssertHelper {

	/**
	 * assert true
	 * 
	 * @param message
	 * @param value
	 */
	public static void assertTrue(String message, boolean value) {
		Assert.assertTrue(message, value);
	}
}