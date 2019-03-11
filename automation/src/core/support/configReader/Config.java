package core.support.configReader;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.TestObject;

public class Config {

	private static final String CONFIG_PREFIX = "config.";
	public static String RESOURCE_PATH = PropertiesReader.getLocalResourcePath();

	/**
	 * gets property value based on key from maven or properties file order: maven
	 * then properties
	 * 
	 * @param key
	 * @return
	 */
	private static String getStringProperty(String key, Properties property) {
		if (!MavenReader.getStringProperty(key).isEmpty()) {
			return MavenReader.getStringProperty(key);
		}
		if (!PropertiesReader.getStringProperty(key, property).isEmpty()) {
			return PropertiesReader.getStringProperty(key, property);
		}

		return "";
	}

	public static void getAllFiles(File curDir) {

		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getAllFiles(f);
			if (f.isFile()) {
				System.out.println("All files: " + f.getPath() + " : " + f.getName());
			}
		}
	}

	/**
	 * get all key values from property files in directory at path
	 * Fails if duplicate key exists. All keys need to be unique
	 * @param type
	 * @return
	 */
	public static Map<String, String> getAllKeys(String path) {
		Map<String, String> config = new ConcurrentHashMap<String, String>();

		try {
			List<Properties> properties = PropertiesReader.Property(path);

			for (Properties property : properties) {

				for (String key : property.stringPropertyNames()) {
					Helper.assertTrue("duplicate property/config key exists: " + key + " at folder: " + path, config.get(key) == null);
					String value = getStringProperty(key, property);
					config.put(key, value);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return config;
	}

	/**
	 * loads config and properties files to TestObject config map
	 */
	public static void loadConfig(String testId) {

		// get all keys from resource path
		Map<String, String> propertiesMap = getAllKeys(RESOURCE_PATH);
		TestObject.getTestInfo(testId).config.putAll(propertiesMap);

		// load config/properties values from entries with "config_" prefix
		for (Entry<String, String> entry : propertiesMap.entrySet()) {
			boolean isConfig = entry.getKey().toString().startsWith(CONFIG_PREFIX);
			if (isConfig) {
				propertiesMap = getAllKeys(PropertiesReader.getLocalRootPath() + entry.getValue());
				TestObject.getTestInfo(testId).config.putAll(propertiesMap);
			}
		}
	}

	/**
	 * returns config value
	 * 
	 * @param key
	 * @return
	 */
	public static String getValue(String key) {

		String value = TestObject.getTestInfo().config.get(key);
		if (value == null) {
			// TODO: can cause stack over flow on startup
			System.out.println("value not found, default empty: " + key);
			// TestLog.ConsoleLogWarn("value not found, default empty: " + key);
			value = "";
		}
		List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
		return items.get(0);
	}

	/**
	 * gets boolean value from properties key
	 * 
	 * @param key
	 * @return
	 */
	public static Boolean getBooleanValue(String key) {
		String value = getValue(key);
		if (value.isEmpty()) {
			// TODO: can cause null point exception on start. need investigation
			// TestLog.ConsoleLogWarn("value not found, default false: " + key);
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * gets int value from properties key
	 * 
	 * @param key
	 * @return
	 */
	public static int getIntValue(String key) {
		String value = getValue(key);
		if (value.isEmpty()) {
			// TODO: can cause null point exception on start. need investigation
			// TestLog.ConsoleLogWarn("value not found, default -1: " + key);
			return -1;
		}
		return Integer.valueOf(value);
	}

	/**
	 * returns a list from config value values separated by ","
	 * 
	 * @param key
	 * @return
	 */
	public static List<String> getValueList(String key) {
		String value = TestObject.getTestInfo().config.get(key);
		if (value == null)
			Helper.assertTrue("value not found in config files: " + key, false);
		List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
		return items;
	}

	/**
	 * puts key value pair in config
	 * 
	 * @param key
	 * @param value
	 */
	public static void putValue(String key, String value) {
		TestLog.logPass("storing in key: " + key + " value: " + value);
		TestObject.getTestInfo().config.put(key, value);
	}
}