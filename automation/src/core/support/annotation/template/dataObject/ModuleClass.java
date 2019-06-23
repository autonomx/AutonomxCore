package core.support.annotation.template.dataObject;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import core.support.annotation.helper.DataObjectHelper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import core.support.annotation.helper.annotationMap.DataMapHelper;

public class ModuleClass {
	
	public static JavaFileObject CSV_File_Object = null;
	public static String MODULE_ROOT = "module";
	public static String DATA_ROOT = "data";

	
	public static void writeModuleClass(Map<String, List<Element>> panelMap) {
		try {
			writeModuleClassImplementation(panelMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeModuleClassImplementation(Map<String, List<Element>> dataObjectMap) throws Exception {

		Logger.debug("<<<< start generating data objects module classes >>>>");

		List<File> files = DataObjectHelper.getAllCsvDataFiles();

		writeModuleClasses(files, dataObjectMap);

		Logger.debug("<<<< completed generating data objects module classes >>>>");

	}
	
	/**
	 * write module class, initiating all csv data files
	 * @param files
	 * @throws Exception
	 */
	private static void writeModuleClasses(List<File> files, Map<String, List<Element>> dataObjectMap) throws Exception {
		
		Map<String, List<File>> moduleMap = DataMapHelper.getDataModuleMap(files);
		
		// convert both maps (csv, data object) to common map type for processing
		Map<String, List<String>> simpleModuleMap = convertCsvModuleMap(moduleMap);

		Map<String, List<String>> simpleDataObjectMap = convertDataObjectModuleMap(dataObjectMap);

		// combine both maps
		Map<String, List<String>> combinedMap = DataMapHelper.mergeMaps(simpleModuleMap, simpleDataObjectMap);
	
		
		// log data object count
		Logger.debug("writeModuleClasses: data objects: " + simpleDataObjectMap.size());
		
		// log csv data object count
		Logger.debug("writeModuleClasses: csv objects: " + simpleModuleMap.size());
		
		// log combined data object count
		Logger.debug("writeModuleClasses: combined data objects: " + combinedMap.size());
		
		for (Entry<String, List<String>> entry : combinedMap.entrySet()) {
			writeModuleClass(entry);
		}
	}
	
	
	/**
	 * 	// import data module package
		for (Entry<String, List<Element>> entry : dataObjectMap.entrySet()) {
			Element firstElement = entry.getValue().get(0);
			bw.append("import "+ PackageHelper.getPackagePath(firstElement) + ";" + "\n");
		}
	 */

	/**
	 * 
	 * 
		package data.webApp;
		
		public class webApp {
			
			public User user() {
				return new User();
			}
		}

	 * 
	 * @param file
	 * @throws Exception
	 */
	private static void writeModuleClass(Entry<String, List<String>> entry) throws Exception {
		
		String module = entry.getKey();
		
		Logger.debug("writing module class: " + module);

		// create file: data.webApp.webApp.java
		String filePath = PackageHelper.DATA_PATH + "." + module + "." + module;
		JavaFileObject fileObject = FileCreatorHelper.createFileAbsolutePath(filePath);

		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " +  DATA_ROOT +"." + module + ";\n");
		bw.newLine();
		bw.newLine();
		
		// import data object classes. csv files do not need imports
		for(String filename : entry.getValue()) {
			if(PackageHelper.hasPackagePath(filename)) {
				bw.append("import " + filename + ";" + "\n" );	
			}
		}
		bw.newLine();
		bw.newLine();
		
		bw.append("public class " + module + " {" + "\n");
		bw.newLine();
		bw.newLine();
		
		
		
//		public User user() {
//			return new User();
//		}
		for(String filename : entry.getValue()) {
			filename = PackageHelper.getClassName(filename);
			
			bw.append("public " + filename + " " +  filename.toLowerCase() +"() {" + "\n" );
			bw.append("    return new " + filename + "();" + "\n");
			bw.append("}");
			bw.newLine();
			bw.newLine();
		}
		bw.newLine();
		bw.newLine();


		bw.append("}\n");

		bw.flush();
		bw.close();	
		
	}
	
	
	
	/**
	 * converts csv module map from key:String value: files to key:String ,  value: file names
	 * @param moduleMap
	 * @return
	 */
	private static Map<String, List<String>> convertCsvModuleMap(Map<String, List<File>> moduleMap) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> classes = new ArrayList<String>();
		
		for (Entry<String, List<File>> entry : moduleMap.entrySet()) {
			classes = new ArrayList<String>();
			
			for(File file : entry.getValue()) {
				String csvName =  file.getName().replaceFirst("[.][^.]+$", "");
				classes.add(csvName);
			}
			map.put(entry.getKey(), classes);
		}	
		return map;
	}
	
	/**
	 * converts data object module map from key:String, value: element to key:String , value: file names
	 * @param moduleMap
	 * @return
	 */
	private static Map<String, List<String>> convertDataObjectModuleMap(Map<String, List<Element>> moduleMap) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> classes = new ArrayList<String>();
		
		for (Entry<String, List<Element>> entry : moduleMap.entrySet()) {
			classes = new ArrayList<String>();
			
			for(Element element : entry.getValue()) {
				String classname = element.asType().toString();
				classes.add(classname);
			}
			map.put(entry.getKey(), classes);
		}	
		return map;
	}
}
