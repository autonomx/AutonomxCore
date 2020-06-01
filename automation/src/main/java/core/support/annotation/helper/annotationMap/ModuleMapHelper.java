package core.support.annotation.helper.annotationMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import core.helpers.Helper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import core.support.configReader.PropertiesReader;

public class ModuleMapHelper {
	
	private static String MODULE_ROOT = "module";
	private static String SOURCE_PATH = PropertiesReader.getLocalRootPath() + "src" + File.separator + "main";


	/**
	 * maps the module with the panels containing the Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<String>> getModuleMap(AnnotationObject annotation){
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<File> files = new ArrayList<File>();
		
		// add java and kotlin files
		files = Helper.getFileListWithSubfolders(SOURCE_PATH, ".java", files);
		files = Helper.getFileListWithSubfolders(SOURCE_PATH, ".kt", files);
		
		// get files with annotation
		List<File> annotationFiles = getFilesWithAnnotation(files, annotation);
		Logger.debug(annotation.annotation + " : " + Arrays.toString(annotationFiles.toArray()));
		map = getModuleMap(annotationFiles);

		return map;
	}
	
	/**
	 * get map of module and path
	 * eg. module: webApp adding panel: module.webApp.panel.UserPanel
	 * @param files
	 * @return
	 */
	public static Map<String, List<String>> getModuleMap(List<File> files){
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		
		String module = StringUtils.EMPTY;
		
		for(File file : files) {
			 List<String> filePath = new ArrayList<String>();
			 module = PackageHelper.getModuleFromFullPath(file);
			
			 // get path eg. webApp.panel.UserPanel
			 String path =  MODULE_ROOT + StringUtils.substringAfter(file.getAbsolutePath(), MODULE_ROOT);
			 if(path.startsWith(File.separator)) path = path.substring(1);
			 path = path.split("\\.")[0];
			 path  = path.replace(File.separator,".");
			 
			 // add to map
			 if(map.get(module) == null)
				 filePath = new ArrayList<String>();
			 else filePath = map.get(module);
			 filePath.add(path);
			
			 Logger.debug("module:" +  module + " adding panel: " + path);
			 map.put(module, filePath);
		}
		return map;
	}
	
	/**
	 * filters files that are in data folder
	 * if file has annotation: @Panel, @Data, @Interface, then add to file list
	 * @param files
	 * @return
	 */
	public static List<File> getFilesWithAnnotation(List<File> files, AnnotationObject annotation) {
		List<File> dataFiles = new ArrayList<File>();
		for (File file : files) {
			if (file.getAbsolutePath().contains(File.separator + annotation.parentFolder + File.separator)) {
				// if file has annotation
				if(Helper.isFileContainsString(annotation.annotation, file))
					dataFiles.add(file);
			}
		}
		return dataFiles;
	}
}
