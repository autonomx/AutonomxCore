package core.support.configReader;

public class MavenReader {

	/**
	 * gets key value from maven properties. format: -Dkey value
	 * 
	 * @param key key in properties file
	 * @return integer value of property key
	 */
	public static int getIntegerProperty(String key) {
		try {
			return Integer.parseInt(System.getProperty(key, "-1"));
		} catch (Exception e) {
			e.getMessage();
		}
		return -1;
	}

	/**
	 * gets the value of the properties file based on key value, And sets default
	 * value if value is missing
	 * 
	 * @param key key in properties file
	 * @return string value of property value
	 */
	public static String getStringProperty(String key) {
		try {
			return System.getProperty(key, "").replace("\"", "").trim();
		} catch (Exception e) {
			e.getMessage();
		}
		return "";
	}

	/**
	 * gets key value from maven properties. format: -Dkey value
	 * 
	 * @param key key in properties file
	 * @return boolean value of proeprlty value
	 */
	public static Boolean getBooleanProperty(String key) {
		try {
			if (System.getProperty(key) == null)
				return null;
			return Boolean.valueOf(System.getProperty(key));
		} catch (Exception e) {
			e.getMessage();
		}
		return null;
	}
}