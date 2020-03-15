package core.support.annotation.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.lang.model.element.Element;

import org.apache.commons.lang3.StringUtils;

import core.helpers.Helper;

public class FileCreatorHelper {
	
	public final static String GENERATED_SOURCE_PATH = "target" + File.separator + "generated-sources" + File.separator + "annotations" + File.separator;

	public static File CONFIG_VARIABLE_FILE_OBJECT = null;
	public static File CONFIG_MODULE_FILE_OBJECT = null;
	public static File moduleManagerFileObject = null;
	public static File moduleFileObject = null;
	public static File CSVDATA_CSV_File_Object = null;
	public static File CSVDATA_DATA_File_Object = null;
	public static File CSVDATA_MODULE_File_Object = null;

	public static String MODULE_ROOT = "module";
	public static String DATA_ROOT = "data";

	/**
	 * test file to generate file creating is working
	 */
	public static void defaultCreateFile() {

		try {
			File file = Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + "module.appManager" + ".java");
			FileWriter fw = new FileWriter(file);
		    BufferedWriter bw = new BufferedWriter(fw);
			bw.append("/**app manager generated code,don't modify it.\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * create module class
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static File createModuleFile() throws IOException {
		moduleFileObject =  Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + PackageHelper.MODULE_MANAGER_PATH + File.separator + PackageHelper.MODULE_CLASS + ".java");
		return moduleFileObject;
	}

	/**
	 * create manager for all modules eg. at modules file: moduleManager.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static File createFile(String path) throws IOException {
		moduleManagerFileObject = Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + PackageHelper.MODULE_MANAGER_PATH + File.separator + PackageHelper.MODULE_MANAGER_CLASS + ".java");
		return moduleManagerFileObject;
	}

	/**
	 * create file for each module eg. at module.android file: androidPanel.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static File createPanelManagerFile(String element) throws IOException {
		File fileObject =  Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + PackageHelper.getPackageDirectory(element) + File.separator + PackageHelper.PANEL_MANAGER_CLASS + ".java");
		return fileObject;
	}

	/**
	 * create csv data object files. eg. data.webApp.User.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static File createFileAbsolutePath(String filePath) throws IOException {
		CSVDATA_CSV_File_Object = Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + filePath + ".java");
		return CSVDATA_CSV_File_Object;
	}

	/**
	 * create csv module object file. eg. data.webApp.webApp.java
	 * 
	 * @param module
	 * @return
	 * @throws IOException
	 */
	public static File createCsvModuleObjectFile(String module) throws IOException {
		CSVDATA_CSV_File_Object =  Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + PackageHelper.DATA_PATH + File.separator + module + File.separator + module + ".java");
		return CSVDATA_CSV_File_Object;
	}

	/**
	 * create data file for csv data object. eg. data.data.java
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static File createCSVDataObjectFile(File file) throws IOException {
		CSVDATA_CSV_File_Object =  Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + PackageHelper.DATA_PATH + File.separator + StringUtils.capitalize(DATA_ROOT) + ".java");
		return CSVDATA_CSV_File_Object;
	}

	public static File createMarkerFile() throws IOException {
		return Helper.createFileFromPath(Helper.getRootDir() + GENERATED_SOURCE_PATH + "marker" + File.separator + "marker.java");
	}
}
