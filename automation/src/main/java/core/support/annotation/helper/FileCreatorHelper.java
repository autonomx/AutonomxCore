package core.support.annotation.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import core.support.annotation.processor.MainGenerator;

public class FileCreatorHelper {
	
	public static JavaFileObject CONFIG_VARIABLE_FILE_OBJECT = null;
	public static JavaFileObject CONFIG_MODULE_FILE_OBJECT = null;
	public static JavaFileObject moduleManagerFileObject = null;
	public static JavaFileObject moduleFileObject = null;
	public static JavaFileObject CSVDATA_CSV_File_Object = null;
	public static JavaFileObject CSVDATA_DATA_File_Object = null;
	public static JavaFileObject CSVDATA_MODULE_File_Object = null;

	public static String MODULE_ROOT = "module";
	public static String DATA_ROOT = "data";

	/**
	 * test file to generate file creating is working
	 */
	public static void defaultCreateFile() {

		try {
			JavaFileObject file = MainGenerator.PROCESS_ENV.getFiler().createSourceFile("module.appManager");
			BufferedWriter bw = new BufferedWriter(file.openWriter());
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
	public static JavaFileObject createModuleFile() throws IOException {
		moduleFileObject =  MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.MODULE_MANAGER_PATH + "." + PackageHelper.MODULE_CLASS);
		return moduleFileObject;
	}

	/**
	 * create manager for all modules eg. at modules file: moduleManager.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createFile(String path) throws IOException {
		moduleManagerFileObject = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.MODULE_MANAGER_PATH + "." + PackageHelper.MODULE_MANAGER_CLASS);
		return moduleManagerFileObject;
	}

	/**
	 * create file for each module eg. at module.android file: androidPanel.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createPanelManagerFile(String element) throws IOException {
		JavaFileObject fileObject =  MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.getPackagePath(element) + "." + PackageHelper.PANEL_MANAGER_CLASS);
		return fileObject;
	}

	/**
	 * create csv data object files. eg. data.webApp.User.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createFileAbsolutePath(String filePath) throws IOException {
		CSVDATA_CSV_File_Object = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(filePath);
		return CSVDATA_CSV_File_Object;
	}

	/**
	 * create csv module object file. eg. data.webApp.webApp.java
	 * 
	 * @param module
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createCsvModuleObjectFile(String module) throws IOException {
		CSVDATA_CSV_File_Object = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.DATA_PATH + "." + module + "." + module);
		return CSVDATA_CSV_File_Object;
	}

	/**
	 * create data file for csv data object. eg. data.data.java
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createCSVDataObjectFile(File file) throws IOException {
		CSVDATA_DATA_File_Object = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.DATA_PATH + "." + StringUtils.capitalize(DATA_ROOT) );
		return CSVDATA_CSV_File_Object;
	}

	public static JavaFileObject createMarkerFile() throws IOException {
		moduleManagerFileObject = MainGenerator.PROCESS_ENV.getFiler().createSourceFile("marker.marker");
		return moduleManagerFileObject;
	}
}
