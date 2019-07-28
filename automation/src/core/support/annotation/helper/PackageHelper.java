package core.support.annotation.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;

public class PackageHelper {
	
	public static String MODULE_MANAGER_CLASS = "ModuleManager";
	public static String PANEL_MANAGER_CLASS = "PanelManager";
	public static String ROOT_PATH = "moduleManager";
	public static String MODULE_CLASS = "ModuleBase";
	public static String DATA_PATH = "data";
	public static String SERVICE_PATH = "serviceManager";


	/**
	 * gets module name. eg. module.android.LoginPanel with return android
	 * 
	 * @param element
	 * @return
	 */
	public static String getModuleName(Element element) {
		String sourceClass = element.asType().toString();
		String module = sourceClass.split("\\.")[1];
		return module;
	}

	public static String getPackagePath(Element element) {
		String sourceClass = element.asType().toString();
		String packagePath = ROOT_PATH + "." + sourceClass.split("\\.")[0] + "." + sourceClass.split("\\.")[1];
		return packagePath;
	}
	
	/**
	 * returns package name from file path
	 * eg. module.common.data.user will return module.common.data
	 * @param sourceClass
	 * @return
	 */
	public static String getPackagePath(String sourceClass) {
		int lastIndxDot = sourceClass.lastIndexOf('.');
		return sourceClass.substring(0, lastIndxDot);
	}

	/**
	 * path: module.android.panel
	 * 
	 * @param path
	 * @return "module"
	 */
	public static String getRootPath(String path) {
		return path.split("\\.")[0];
	}

	public static String getAppName(Element element) {
		String sourceClass = element.asType().toString();
		String appName = sourceClass.split("\\.")[1];
		return appName;
	}

	/**
	 * gets the full path of the first module eg. module.android.panel
	 * 
	 * @param panelMap
	 * @return
	 */
	public static String getFirstModuleFullPath(Map<String, List<Element>> panelMap) {
		String sourceClass = "";
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {

			Element firstElement = entry.getValue().get(0);
			sourceClass = firstElement.asType().toString();
			break;
		}
		return sourceClass;
	}
	
	/**
	 * returns the module name from the file path
	 * module is directory after "module" directory
	 * eg. module.webapp.data.user.csv will return webapp
	 * outside module: data.user.csv will return data
	 * @return
	 */
	public static String getModuleFromFullPath(File file) {
		String pattern = Pattern.quote(File.separator); // win, osx, linux have different separators
		String[] directories = file.getAbsolutePath().split(pattern);
		Logger.debug("getModuleFromFullPath: " + file.getAbsolutePath());
		for(int i = 0; i< directories.length; i++) {
			if(directories[i].equals("module"))
				return directories[i+1];
		}
		
		// by default return data. this is applicable for data files outside modules
		return "data";
	}
	
	/**
	 * gets the list of modules that have csv data files
	 * @param files
	 * @return
	 */
	public static Set<String> getModuleList(List<File> files){
		 // unique modules
		 Set<String> modules = new TreeSet<>();

		for(File file : files) {
			String module = getModuleFromFullPath(file);
			modules.add(module);
		}
		
		return modules;
	}
	
	/**
	 * returns true if package path is contained in the name
	 * eg. module.common.data.user
	 * @param value
	 * @return
	 */
	public static boolean hasPackagePath(String value) {
		List<String> list = new ArrayList<String>(Arrays.asList(value.split("\\.")));
		return list.size() > 1;
	}
	
	/**
	 * gets class name from path
	 * eg. module.common.data.user returns login
	 * @param classPath
	 * @return
	 */
	public static String getClassName(String classPath) {
		return classPath.substring(classPath.lastIndexOf('.') + 1).trim();
	}

}
