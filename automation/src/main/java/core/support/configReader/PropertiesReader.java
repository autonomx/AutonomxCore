package core.support.configReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import core.helpers.Helper;
import core.support.objects.TestObject;

public class PropertiesReader {

	public static String LOCAL_ROOT_PATH = Helper.getRootDir();
	private static String LOCAL_RESOURCE_PATH =  "resources";
	private static String LOCAL_RESOURCE_CLOUD_PATH = "test-classes" + File.separator + "testData"
			+ File.separator + "resources" + File.separator;
	public static String PROPERTIES_TYPE_PROPERTIES = ".property";
	public static String PROPERTIES_TYPE_PROPERTIES_TYPE2 = ".property";
	public static String PROPERTIES_TYPE_CONF = ".conf";

	/**
	 * @param path path to properties file
	 * @return properties list of properties from properties file
	 * @throws Exception exception from getting properties file
	 */
	public static List<Properties> Property(String path) throws Exception {

		List<Properties> properties = new ArrayList<Properties>();

		// get full path for property file
		path = getPropertyPath(path);
		
		if (new File(path).isFile()) {
			properties.addAll(getPropertiesByFileType(path, StringUtils.EMPTY));
		} else {
			properties.addAll(getPropertiesByFileType(path, PROPERTIES_TYPE_PROPERTIES));
			//properties.addAll(getPropertiesByFileType(path, PROPERTIES_TYPE_PROPERTIES_TYPE2));
			properties.addAll(getPropertiesByFileType(path, PROPERTIES_TYPE_CONF));

			if (Helper.getFileList(path).isEmpty())
				Helper.assertFalse("path: '" + path
						+ "' does not have any property files, please verify resources/properties.property for correct path");
		}

		return properties;
	}
	
	/**
	 * recognized config file as a property file. eg. /qa becomes /qa.property
	 * @param path
	 * @return
	 */
	public static String getPropertyPath(String path) {
		
		path = Helper.getFullPath(path);
		Path file = new File(path).toPath();

		if(Files.isDirectory(file) || Files.isRegularFile(file)) {
			return path;
		}
		
		if(!(path.endsWith(".property") || path.endsWith(".config")))
			path = path + ".property";
		
		path = Helper.getFullPath(path);		
		return path;
	}

	/**
	 * gets all properties file by file type in a directory
	 * 
	 * @param path:     directory path
	 * @param fileType: eg. ".conf"
	 * @return list of all properties
	 * @throws Exception exception from getting properties file
	 */
	@SuppressWarnings("serial")
	public static List<Properties> getPropertiesByFileType(String path, String fileType) throws Exception {
		List<Properties> properties = new ArrayList<Properties>();
		List<File> files = new ArrayList<File>();

		if (fileType.isEmpty()) {
			File file = Helper.getFile(path);
			files.add(file);
		} else
			files = Helper.getFileListByType(path, fileType);

		for (File file : files) {

			// get property files
			// Properties prop = new Properties();
			Properties prop = new Properties() {
				@Override
				public synchronized Object put(Object key, Object value) {

					// store key and property file location for duplicate check
					TestObject.getTestInfo().configKeys.put(key.toString(), file.getName());
					return super.put(key, value);
				}
			};
			try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
				prop.load(bis);

			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}

			// add to propery list
			properties.add(prop);
		}
		return properties;
	}

	public static List<Properties> getPropertiesByFileType(String path) throws Exception {
		return getPropertiesByFileType(path, StringUtils.EMPTY);
	}

	/**
	 * @return root path
	 */
	public static String getLocalResourcePath() {
		if (isUsingCloud()) {
			return Helper.getFullPath(LOCAL_RESOURCE_CLOUD_PATH);
		} else {
			return Helper.getFullPath(LOCAL_RESOURCE_PATH);
		}
	}

	/**
	 * @return is using app center
	 */
	public static boolean isUsingCloud() {

		File f = new File(Helper.getFullPath(LOCAL_RESOURCE_CLOUD_PATH));
		if (f.exists() && f.isDirectory()) {
			return true;
		}
		return false;
	}

	/**
	 * gets the value of the properties file based on key value, And sets default
	 * value if value is missing
	 * 
	 * @param key      key in properties file
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

	public static ArrayList<String> getAllFiles(File curDir) {
		ArrayList<String> array = new ArrayList<String>();

		array = getFileList(curDir, array);
		return array;
	}

	/**
	 * gets all files in a directory to get all files: File curDir = new File(".");
	 * getAllFiles(curDir);
	 * 
	 * @param curDir target directory
	 * @return the list of all files in given directory
	 */
	public static ArrayList<String> getFileList(File curDir, ArrayList<String> array) {
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getFileList(f, array);
			if (f.isFile()) {
				// TestLog.ConsoleLog("All files: " + f.getPath() + " : " + f.getName());
				array.add(f.getPath());
			}
		}
		return array;
	}
}