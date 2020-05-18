package core.support.annotation.template.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.JavaFileObject;

import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import core.support.configReader.Config;

public class ModuleManager {
	
	public static final String PROJECT_NAME = "project.name";

	public static void writeModuleManagerClass(Map<String, List<String>> panelMap) {
		try {
			writeModuleManagerClassImplementation(panelMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeModuleManagerClassImplementation(Map<String, List<String>> panelMap) throws IOException {
		Logger.debug("start generating module manager class");

		String projectname = Config.getValue(PROJECT_NAME);
		if(projectname.isEmpty()) projectname = "Module"; 
		
		// returns module.android.panel
		String modulePath = PackageHelper.getFirstModuleFullPath(panelMap);

		// returns module
		String rootModulePath = PackageHelper.getRootPath(modulePath);

		// proceed if app manager has not been created
		if (FileCreatorHelper.moduleManagerFileObject != null)
			return;

		// create file: module.appManager.java
		JavaFileObject file = FileCreatorHelper.createFile(rootModulePath, projectname);
		BufferedWriter bw = new BufferedWriter(file.openWriter());


		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + PackageHelper.MODULE_MANAGER_PATH + ";\n");
		bw.newLine();

		// eg. import module.android.androidPanel
		/*
		 * for (Entry<String, List<Element>> entry : panelMap.entrySet()) { Element
		 * firstElement = entry.getValue().get(0); bw.append("import " +
		 * getPackagePath(firstElement) + "." + entry.getKey() + "Panel" + ";\n"); }
		 */
		bw.newLine();

		
		bw.append("public class " + projectname + "Manager {\n");

		// add panel declarations
		for (Entry<String, List<String>> entry : panelMap.entrySet()) {
			String firstElement = entry.getValue().get(0);
			bw.append("	public " + PackageHelper.getPackagePath(firstElement) + "." + PackageHelper.PANEL_MANAGER_CLASS
					+ " " + entry.getKey() + " = new " + PackageHelper.getPackagePath(firstElement) + "."
					+ "PanelManager" + "();\n");
		}

		bw.append("}\n");

		bw.flush();
		bw.close();

		Logger.debug("complete generating module manager class");

	}

}
