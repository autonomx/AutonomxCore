package core.support.annotation.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import core.helpers.Helper;
import core.support.configReader.PropertiesReader;

public class DataObjectHelper {
	
	public static String DATA_FOLDER = "data";
	private static String SOURCE_PATH =  PropertiesReader.getLocalRootPath() + "src/main/java";

	
	/**
	 * gets all csv files in data folders
	 * @return
	 */
	public static List<File> getAllCsvDataFiles() {
		List<File> files = new ArrayList<File>();
		files = Helper.getFileListWithSubfolders(SOURCE_PATH , ".csv", files);
		files = DataObjectHelper.getTestDataCsvFiles(files);
		return files;
	}
	
	/**
	 * filters files that are in data folder
	 * @param files
	 * @return 
	 */
	public static List<File> getTestDataCsvFiles(List<File> files ) {
		List<File> dataFiles = new ArrayList<File>();
		for(File file : files) {
			if( file.getAbsolutePath().contains(DATA_FOLDER)) {
				dataFiles.add(file);
			}
		}
		return dataFiles;	
	}
}
