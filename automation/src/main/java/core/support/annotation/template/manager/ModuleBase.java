package core.support.annotation.template.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import core.helpers.UtilityHelper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;

public class ModuleBase {
	
	public static final String MODULE_BASE_CLASS_NAME = "ModuleBase";

	public static void writeModuleBaseClass(Map<String, List<String>> panelMap) {
		try {
			writeModuleBaseClassImplementation(panelMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeModuleBaseClassImplementation(Map<String, List<String>> panelMap) throws IOException {
		Logger.debug("start generating module base class");

		// returns module.android.panel

		String moduleBasePath = PackageHelper.MODULE_MANAGER_PATH + "." + PackageHelper.MODULE_PACKAGE + "." + MODULE_BASE_CLASS_NAME;
	
		// create file: module.appManager.java
		JavaFileObject file = FileCreatorHelper.createFileAbsolutePath(moduleBasePath);
		BufferedWriter bw = new BufferedWriter(file.openWriter());

		/**
		 * package test.module;

			import core.uiCore.drivers.AbstractDriverTestNG;
			import moduleManager.AutonomxManager;
			
			public class ModuleBase extends AbstractDriverTestNG {
				protected AutonomxManager app = new AutonomxManager();
			}
		 */
		
		String projectname = UtilityHelper.getMavenArtifactId();
		if(projectname.isEmpty()) projectname = "Module"; 

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + PackageHelper.MODULE_MANAGER_PATH + "." + PackageHelper.MODULE_PACKAGE +";\n");
		bw.newLine();
		bw.newLine();
		bw.append("import core.uiCore.drivers.AbstractDriverTestNG;\n");
		bw.append("import moduleManager." + projectname + "Manager;\n");
		bw.newLine();

		bw.append("	public class ModuleBase extends AbstractDriverTestNG {\n");
		bw.append("		protected " + projectname + "Manager app = new " + projectname + "Manager(); {\n");
		bw.append("	}\n");
		bw.append("}");
		bw.flush();
		bw.close();

		Logger.debug("complete generating module base class");
	}
}