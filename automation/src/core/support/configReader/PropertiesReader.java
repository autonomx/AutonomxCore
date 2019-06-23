package core.support.configReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import core.helpers.Helper;
import core.support.logger.TestLog;

public class PropertiesReader {

	private static String LOCAL_ROOT_PATH = Helper.getCurrentDir();
	private static String LOCAL_RESOURCE_PATH = LOCAL_ROOT_PATH + "resources" + File.separator;
	private static String LOCAL_RESOURCE_CLOUD_PATH = LOCAL_ROOT_PATH + "test-classes" + File.separator + "testData" + File.separator + "resources" + File.separator;
	private static String PROPERTIES_TYPE_PROPERTIES = ".property";
	private static String PROPERTIES_TYPE_CONF = ".conf";

   /**
    * @param path path to properties file
    * @return properties list of properties from properties file
    * @throws Exception exception from getting properties file
    */
	public static List<Properties> Property(String path) throws Exception {

		List<Properties> properties = new ArrayList<Properties>();
		properties.addAll(getPropertiesByFileType(path, PROPERTIES_TYPE_PROPERTIES));
		properties.addAll(getPropertiesByFileType(path, PROPERTIES_TYPE_CONF));

		return properties;
	}

	/**
	 * gets all properties file by file type in a directory
	 * 
	 * @param path:
	 *            directory path
	 * @param fileType:
	 *            eg. ".conf"
	 * @return list of all properties
	 * @throws Exception exception from getting properties file
	 */
	public static List<Properties> getPropertiesByFileType(String path, String fileType) throws Exception {
		List<Properties> properties = new ArrayList<Properties>();

		List<File> files = Helper.getFileList(path, fileType);

		for (File file : files) {
			// get property files
			FileInputStream fileInput = new FileInputStream(file);
			Properties prop = new Properties();
			prop.load(fileInput);

			// add to propery list
			properties.add(prop);
		}
		return properties;
	}

	/**
	 * @return path to the project root directory
	 */
	public static String getLocalRootPath() {
			return LOCAL_ROOT_PATH;
	}

	/**
	 * @return root path
	 */
	public static String getLocalResourcePath() {
		if (isUsingCloud()) {
			return LOCAL_RESOURCE_CLOUD_PATH;
		} else {
			return LOCAL_RESOURCE_PATH;
		}
	}

	/**
	 * @return is using app center
	 */
	public static boolean isUsingCloud() {

		File f = new File(LOCAL_RESOURCE_CLOUD_PATH);
		if (f.exists() && f.isDirectory()) {
			return true;
		}
		return false;
	}

	/**
	 * gets the value of the properties file based on key value, And sets default
	 * value if value is missing
	 * @param key key in properties file
	 * @param Property target properties from property file
	 * @return string value of the property
	 */
	public static String getStringProperty(String key, Properties Property) {
		try {
			return Property.getProperty(key, "").replace("\"", "").trim();
		} catch (Exception e) {
			e.getMessage();
		}
		return "";
	}

	/**
	 * gets all files in a directory to get all files: File curDir = new File(".");
	 * getAllFiles(curDir);
	 * 
	 * @param curDir target directory
	 * @return the list of all files in given directory
	 */
	public static ArrayList<String> getAllFiles(File curDir) {
		ArrayList<String> array = new ArrayList<String>();
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getAllFiles(f);
			if (f.isFile()) {
				TestLog.ConsoleLog("All files: " + f.getPath() + " : " + f.getName());
				array.add("All files: " + f.getPath() + " : " + f.getName());
			}
		}
		return array;
	}
}