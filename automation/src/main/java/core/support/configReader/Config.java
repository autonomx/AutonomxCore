package core.support.configReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;

public class Config {

	private static final String CONFIG_GROUP_PREFIX = "config.group.";
	private static final String CONFIG_PROFILE_PREFIX = "config.profile.";

	private static final String CONFIG_PREFIX = "config.";
	private static final String PROFILE_PREFIX = "profile.";
	private static final String GROUP_PREFIX = "profile.group.";

	public static String RESOURCE_PATH = PropertiesReader.getLocalResourcePath();

	/**
	 * gets property value based on key from maven or properties file order: maven
	 * Then properties
	 * 
	 * @param key      key in properties file
	 * @param property
	 * @return string value of property file
	 */
	private static String getStringProperty(String key, Properties property) {
		if (StringUtils.isNotBlank(MavenReader.getStringProperty(key))) {
			return MavenReader.getStringProperty(key);
		}
		if (!PropertiesReader.getStringProperty(key, property).isEmpty()) {
			return PropertiesReader.getStringProperty(key, property);
		}

		return "";
	}

	/**
	 * git all files in given directory
	 * 
	 * @param curDir target directory
	 */
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
	 * get all key values from property files in directory at path Fails if
	 * duplicate key exists. All keys need to be unique
	 * 
	 * @param path path to proeprties file
	 * @return map of all key and values in all property files in given path
	 */
	public static Map<String, String> getAllKeys(String path) {
		Map<String, String> config = new ConcurrentHashMap<String, String>();

		try {
			List<Properties> properties = PropertiesReader.Property(path);

			for (Properties property : properties) {

				for (String key : property.stringPropertyNames()) {
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
	 * loads config And properties files to TestObject config map
	 * 
	 * @param testId id of the test
	 */
	public static void loadConfig(String testId) {

		Map<String, Object> config = loadConfigProperties();
		TestObject.getTestInfo(testId).config.putAll(config);
	}

	/**
	 * loads configs and profiles to TestObject config map
	 * 
	 * @return
	 */
	public static Map<String, Object> loadConfigProperties() {
		Map<String, Object> config = new ConcurrentHashMap<String, Object>();
		TestObject.getTestInfo().configKeys = ArrayListMultimap.create();

		// get all keys from resource path
		Map<String, String> propertiesMap = getAllKeys(RESOURCE_PATH);
		List<String> configPath = new ArrayList<String>();

		// add config, profile and group path
		configPath.addAll(getConfigs(propertiesMap));
		configPath.addAll(getConfigProfiles(propertiesMap));
		configPath.addAll(getConfigGroup(propertiesMap));

		// load config/properties values
		for (String path : configPath) {
			propertiesMap = getAllKeys(path);
			config.putAll(propertiesMap);
		}

		// check for duplicate keys and print warning
		checkForDuplicateKeys();
		return config;
	}

	/**
	 * check for config duplicate keys and print out the key + property file name
	 * 
	 * @return
	 */
	public static List<String> checkForDuplicateKeys() {
		Multimap<String, String> keys = TestObject.getTestInfo().configKeys;
		List<String> dupicateKeys = new ArrayList<String>();

		Set<String> keySet = keys.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {
			String key = (String) keyIterator.next();
			Collection<String> values = keys.get(key);
			if (values.size() > 1) {
				dupicateKeys.add(key);
				TestLog.logWarning(
						"Duplicate key found: " + key + " at property files: " + Arrays.toString(values.toArray()));
			}
		}
		return dupicateKeys;
	}

	/**
	 * get a list of config path from properties.property file prefix: "config.",
	 * not including profiles: config.profile key
	 * 
	 * @param propertiesMap
	 * @return
	 */
	public static List<String> getConfigs(Map<String, String> propertiesMap) {

		List<String> configPath = new ArrayList<String>();

		// get list of profiles from key: config.profile
		for (Entry<String, String> entry : propertiesMap.entrySet()) {
			String key = entry.getKey().toString().trim();
			boolean isConfig = key.startsWith(CONFIG_PREFIX) && !key.startsWith(CONFIG_PROFILE_PREFIX)
					&& !key.startsWith(CONFIG_GROUP_PREFIX);
			if (isConfig) {
				configPath.add(entry.getValue());
			}
		}
		return configPath;
	}

	/**
	 * get the list of profile path specified by profile. in properties.property
	 * file multiple profiles can be separated by ","
	 * 
	 * @param propertiesMap
	 * @return
	 */
	public static List<String> getConfigProfiles(Map<String, String> propertiesMap) {
		List<String> profiles = new ArrayList<String>();
		List<String> profilePath = new ArrayList<String>();

		// get list of profiles from key: profile.
		for (Entry<String, String> entry : propertiesMap.entrySet()) {
			boolean isProfile = entry.getKey().toString().startsWith(PROFILE_PREFIX);
			boolean isCorrectLength = entry.getKey().toString().split("\\.").length == 2;
			if (isProfile && isCorrectLength) {
				String profile = entry.getKey().split("\\.")[1];
				// add profile name to value. eg. environment.dev
				List<String> values = new ArrayList<String>(Arrays.asList(entry.getValue().split(",")));
				values = values.stream().map(String::trim).collect(Collectors.toList());
				profiles.addAll(values.stream().map(c -> profile + "." + c).collect(Collectors.toList()));

			}
		}
		// property value: profile.environment = dev
		// add profile path to list. eg. 'environment.dev'. profile is environment,
		// dev is the property file name: dev.property
		for (String profile : profiles) {
			String profileValue = profile.split("\\.")[0];
			String propertyFile = profile.split("\\.")[1];

			// continue to next profile if value set to none
			if (propertyFile.equals("none"))
				continue;

			if (propertiesMap.get(CONFIG_PROFILE_PREFIX + profileValue) == null)
				Helper.assertFalse("profile not found: " + profile
						+ ". Please add profile to properties.property file as profile." + profile);
			String path = propertiesMap.get(CONFIG_PROFILE_PREFIX + profileValue);
			File file = Helper.getFileByName(path, propertyFile);
			profilePath.add(file.getPath());
		}

		return profilePath;
	}

	/**
	 * get the list of group path specified by profile.group.groupName. in
	 * properties.property file multiple profiles can be separated by ","
	 * 
	 * @param propertiesMap
	 * @return
	 */
	public static List<String> getConfigGroup(Map<String, String> propertiesMap) {
		List<String> profiles = new ArrayList<String>();
		List<String> groupPath = new ArrayList<String>();

		// get list of groups from key: profile.
		for (Entry<String, String> entry : propertiesMap.entrySet()) {
			boolean isProfile = entry.getKey().toString().startsWith(GROUP_PREFIX);
			boolean isCorrectLength = entry.getKey().toString().split("\\.").length == 3;
			if (isProfile && isCorrectLength) {
				String group = entry.getKey().split("\\.")[2];

				// add group name to value. eg. repot.value
				List<String> values = new ArrayList<String>(Arrays.asList(entry.getValue().split(",")));
				values = values.stream().map(String::trim).collect(Collectors.toList());
				profiles.addAll(values.stream().map(c -> group + "." + c).collect(Collectors.toList()));
			}
		}

		// add group path to list
		for (String profile : profiles) {
			String value = profile.split("\\.")[1];

			// continue to next profile if value set to none
			if (value.equals("none"))
				continue;

			if (propertiesMap.get(CONFIG_GROUP_PREFIX + profile) == null)
				Helper.assertFalse("profile not found: " + profile
						+ ". Please add groups to properties.property file as " + CONFIG_GROUP_PREFIX + profile);
			String path = propertiesMap.get(CONFIG_GROUP_PREFIX + profile);
			groupPath.add(path);
		}

		return groupPath;
	}

	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getValue(String key) {
		return getValue(key, false);
	}

	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getValue(String key, boolean isFailable) {

		Object value = TestObject.getTestInfo().config.get(key.trim());
		if(value == null) 
				value = MavenReader.getStringProperty(key);
	
		if (value == null) {
			if (isFailable)
				Helper.assertFalse("value not found, default empty: " + key);

			// keep track of missing config variables
			TestObject.getTestInfo().missingConfigVars.add(key);

			value = StringUtils.EMPTY;
			return value.toString();
		}
		return value.toString();
	}

	/**
	 * gets int value from properties key
	 * 
	 * @param key key in properties file
	 * @return returns the integer value of key from properties
	 */
	public static int getGlobalIntValue(String key) {
		String value = getValue(key, false);
		if (value.isEmpty()) {
			return -1;
		}
		return Integer.valueOf(value);
	}

	/**
	 * gets the object value from property key
	 * 
	 * @param key key in properties file
	 * @return returns the object value of key from properties
	 */
	public static Object getGlobalObjectValue(String key) {
		if (TestObject.getGlobalTestInfo().config.get(key.trim()) == null)
			return StringUtils.EMPTY;
		Object value = TestObject.getTestInfo().config.get(key);
		return value;
	}

	/**
	 * gets boolean value from properties key
	 * 
	 * @param key target key from properties file
	 * @return the boolean value of key from properties
	 */
	public static Boolean getGlobalBooleanValue(String key) {
		String value = getGlobalValue(key, false);
		if (value.isEmpty()) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getGlobalValue(String key) {
		return getGlobalValue(key, false);
	}

	/**
	 * returns config value
	 * 
	 * @param key get string value of key from properties
	 * @return string value of key
	 */
	public static String getGlobalValue(String key, boolean isFailable) {

		Object value = TestObject.getGlobalTestInfo().config.get(key.trim());
		if(value == null) 
			value = MavenReader.getStringProperty(key);
		
		if (value == null) {
			if (isFailable)
				Helper.assertFalse("value not found, default empty: " + key);
			value = StringUtils.EMPTY;
		}
	
		return value.toString();
	}

	/**
	 * gets boolean value from properties key
	 * 
	 * @param key target key from properties file
	 * @return the boolean value of key from properties
	 */
	public static Boolean getBooleanValue(String key) {
		return getBooleanValue(key, false);
	}

	/**
	 * gets boolean value from properties key
	 * 
	 * @param key target key from properties file
	 * @return the boolean value of key from properties
	 */
	public static Boolean getBooleanValue(String key, boolean isFailable) {
		String value = getValue(key, isFailable);
		if (value.isEmpty()) {
			if (isFailable)
				Helper.assertFalse("value not found: " + key);
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	/**
	 * gets the object value from property key
	 * 
	 * @param key key in properties file
	 * @return returns the object value of key from properties
	 */
	public static Object getObjectValue(String key) {
		if (TestObject.getTestInfo().config.get(key.trim()) == null) {
			return null;
		}
		Object value = TestObject.getTestInfo().config.get(key.trim());
		return value;
	}

	/**
	 * gets int value from properties key
	 * 
	 * @param key key in properties file
	 * @return returns the integer value of key from properties
	 */
	public static int getIntValue(String key) {
		return getIntValue(key, false);
	}

	/**
	 * gets int value from properties key
	 * 
	 * @param key key in properties file
	 * @return returns the integer value of key from properties
	 */
	public static int getIntValue(String key, boolean isFailable) {
		String value = getValue(key, isFailable);
		if (value.isEmpty()) {
			if (isFailable)
				Helper.assertFalse("value not found: " + key);
			return -1;
		}
		return Integer.valueOf(value);
	}

	/**
	 * gets double value from properties key
	 * 
	 * @param key key in properties file
	 * @return the double value of key from properties
	 */
	public static double getDoubleValue(String key) {
		return getDoubleValue(key, false);
	}

	/**
	 * gets double value from properties key
	 * 
	 * @param key key in properties file
	 * @return the double value of key from properties
	 */
	public static double getDoubleValue(String key, boolean isFailable) {
		String value = getValue(key, isFailable);
		if (value.isEmpty()) {
			if (isFailable)
				Helper.assertFalse("value not found: " + key);
			return -1;
		}
		return Double.valueOf(value);
	}

	/**
	 * returns a list from config value values separated by ","
	 * 
	 * @param key key in properties file
	 * @return the list of values from key separated by ","
	 */
	public static ArrayList<String> getValueList(String key) {
		return getValueList(key, false);
	}

	/**
	 * returns a list from config value values separated by ","
	 * 
	 * @param key key in properties file
	 * @return the list of values from key separated by ","
	 */
	public static ArrayList<String> getValueList(String key, boolean isFailable) {
		String value = getValue(key, isFailable);
		ArrayList<String> items = new ArrayList<String>();
		if (value == null) {
			if (isFailable)
				Helper.assertFalse("value not found in config files: " + key);
		}
		if (value !=null && !value.isEmpty()) {
			items = new ArrayList<String>(Arrays.asList(value.split(",")));
			items.replaceAll(String::trim);
		}
		return items;
	}

	/**
	 * puts key value pair in config
	 * 
	 * @param key   key in properties file
	 * @param value value associated with key
	 */
	public static void putValue(String key, Object value) {
		putValue(key, value, true);
	}

	/**
	 * puts key value pair in config
	 * 
	 * @param key   key in properties file
	 * @param value value associated with key
	 */
	public static void putValue(String key, Object value, boolean isLog) {
		if (isLog)
			TestLog.logPass("storing in key: " + key + " value: " + value);
		if(value == null) value = "null";
		TestObject.getTestInfo().config.put(key, value);
	}

	public static void putValue(String key, Object value, String info) {
		TestLog.logPass("storing in key: " + key + " value: " + info);
		TestObject.getTestInfo().config.put(key.trim(), value);
	}

	/**
	 * set parent config value
	 * 
	 * @param key
	 * @param value
	 */
	public static void setParentValue(String key, Object value) {
		ServiceObject service = TestObject.getTestInfo().serviceObject;
		TestObject.getParentTestInfo(service).config.put(key.trim(), value);

	}

	/**
	 * set global config value
	 * 
	 * @param key
	 * @param value
	 */
	public static void setGlobalValue(String key, Object value) {
		TestLog.logPass("storing in global key: " + key + " value: " + value);
		TestObject.getGlobalTestInfo().config.put(key.trim(), value);
	}

	/**
	 * get parent config value
	 * 
	 * @param key
	 * @return
	 * @return
	 */
	public static boolean getParentValue(String key) {
		ServiceObject service = TestObject.getTestInfo().serviceObject;
		Object value = TestObject.getParentTestInfo(service).config.get(key);
		if (value == null)
			return false;
		return (boolean) value;
	}

	/**
	 * print a list of missing config variables
	 */
	public static void printMissingConfigVariables() {
		List<String> variables = TestObject.getTestInfo().missingConfigVars;
		if (variables.size() > 0)
			TestLog.ConsoleLog("List of missing config variables. Please see latest version for updated config: "
					+ StringUtils.join(variables, ", "));
	}
}