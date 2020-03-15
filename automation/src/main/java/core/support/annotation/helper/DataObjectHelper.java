package core.support.annotation.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import core.helpers.Helper;
import core.support.configReader.PropertiesReader;

public class DataObjectHelper {

	public static String DATA_FOLDER = "data";
	public static String OBJECT_FOLDER = "object";

	private static String SOURCE_PATH = PropertiesReader.getLocalRootPath() + "src" + File.separator + "main";

	/**
	 * gets all csv files in data folders
	 * 
	 * @return
	 */
	public static List<File> getAllCsvDataFiles() {
		List<File> files = new ArrayList<File>();
		files = Helper.getFileListWithSubfolders(SOURCE_PATH, ".csv", files);
		files = DataObjectHelper.getTestDataCsvFiles(files);
		return files;
	}

	/**
	 * filters files that are in data folder
	 * 
	 * @param files
	 * @return
	 */
	public static List<File> getTestDataCsvFiles(List<File> files) {
		List<File> dataFiles = new ArrayList<File>();
		for (File file : files) {
			if (file.getAbsolutePath().contains(DATA_FOLDER) || file.getAbsolutePath().contains(OBJECT_FOLDER)) {
				dataFiles.add(file);
			}
		}
		return dataFiles;
	}

	/**
	 * normalizes method name. eg. web.element.highlight.enable to
	 * webElementHightEnable
	 * 
	 * @param methodName
	 * @return
	 */
	public static String normalizeMethod(String methodName) {
		String method = capitalizeMethod(methodName, "\\.");
		method = capitalizeMethod(method, "-");
		method = capitalizeMethod(method, "_");
		method = capitalizeMethod(method, "@");
		method = capitalizeMethod(method, " ");

		// work around method starting with digits
		if (Character.isDigit(method.charAt(0))) {
			method = "method" + method;
		}
		return method;
	}

	/**
	 * capitalize method name from web.element.highlight.enable to
	 * webElementHightEnable split based on splitter value
	 * 
	 * @param methodName
	 * @return
	 */
	private static String capitalizeMethod(String methodName, String splitter) {
		String methodNormalize = StringUtils.EMPTY;

		String[] words = methodName.split(splitter);
		for (int i = 1; i < words.length; i++) {
			methodNormalize += StringUtils.capitalize(words[i]);
		}
		methodNormalize = words[0] + methodNormalize;
		return methodNormalize;
	}
}
