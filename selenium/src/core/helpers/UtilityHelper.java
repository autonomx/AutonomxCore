package core.helpers;

import java.security.SecureRandom;

public class UtilityHelper {

	/**
	 * generates random string of length len
	 * @param len
	 * @return
	 */
	public static String generateRandomString(int len) {
		String AB = "0123456789abcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();

		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}
	
	/**
	 * normalizes string
	 * removes space, new line chars
	 * @param value
	 * @return
	 */
	public static String stringNormalize(String value) {
		value = value
				.toLowerCase()
				.trim()
		        .replace("\n", "")
		        .replace("\r", "");
		return value;
	}

}