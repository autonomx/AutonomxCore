package core.support.annotation.template.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

import javax.tools.JavaFileObject;

import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;

public class ConfigManager {

	public static void writeConfigManagerClass() {
		try {
			writeConfigModuleClassImplementation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeConfigModuleClassImplementation() throws IOException {
		Logger.debug("start generating config module class");

		// proceed if config manager has not been created
		if (FileCreatorHelper.CONFIG_MODULE_FILE_OBJECT != null)
			return;

		// create file: ConfigManager.java
		JavaFileObject file = FileCreatorHelper.createFileAbsolutePath(PackageHelper.CONFIG_MANAGER_PATH + "." + PackageHelper.CONFIG_MANAGER_CLASS);
		BufferedWriter bw = new BufferedWriter(file.openWriter());

		
		FileCreatorHelper.CONFIG_MODULE_FILE_OBJECT = file;

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + PackageHelper.CONFIG_MANAGER_PATH + ";\n");
		bw.newLine();
		bw.newLine();

		/*
		 * package configManager;
		 * 
		 * import core.helpers.Helper;
		 * 
		 * public class ConfigManager {
		 * 
		 * String value; String key;
		 * 
		 * public ConfigManager(String key, String val) { this.key = key; this.value =
		 * val; }
		 * 
		 * public String toString() { return this.value.toString(); }
		 * 
		 * public boolean toBoolean() { if(!(this.value instanceof Boolean))
		 * Helper.assertFalse(this.value + " is not a Boolean value" ); return (boolean)
		 * this.value; }
		 * 
		 * public int toInt() { if(!(this.value instanceof Integer))
		 * Helper.assertFalse(this.value + " is not an Integer value" ); return
		 * (Integer) this.value; }
		 * 
		 * public void setValue(String value) { Config.putValue(key, value); } }
		 */
		bw.append("import core.helpers.Helper;" + "\n");
		bw.append("import core.support.configReader.Config;" + "\n");
		bw.newLine();
		bw.newLine();

		bw.append("public class ConfigManager {\n");
		bw.newLine();
		bw.append("	String key;" + "\n");
		bw.append("	String value;" + "\n");
		bw.newLine();
		bw.append("	public ConfigManager(String key, String val) {" + "\n");
		bw.append("		this.key = key;" + "\n");
		bw.append("		this.value = val;" + "\n");
		bw.append("	}" + "\n");
		bw.newLine();
		bw.append("	public String toString() {" + "\n");
		bw.append("		return this.value;" + "\n");
		bw.append("	}" + "\n");
		bw.newLine();
		bw.append("	public boolean toBoolean() {" + "\n");
		bw.append("		if(!Helper.isBoolean(this.value))" + "\n");
		bw.append("			Helper.assertFalse(this.value + \" is not a Boolean value\" );" + "\n");
		bw.append("		return Boolean.valueOf(this.value);" + "\n");
		bw.append("	}" + "\n");
		bw.newLine();
		bw.append("	public int toInt() {" + "\n");
		bw.append("		if(!Helper.isNumeric(this.value))" + "\n");
		bw.append("			Helper.assertFalse(this.value + \" is not a number\" );" + "\n");
		bw.append("		return Integer.valueOf(this.value);" + "\n");
		bw.append("	}" + "\n");
		bw.newLine();
		bw.append("	public double toDouble() {" + "\n");
		bw.append("		if(!Helper.isNumeric(this.value))" + "\n");
		bw.append("			Helper.assertFalse(this.value + \" is not a number\" );" + "\n");
		bw.append("		return Double.valueOf(this.value);" + "\n");
		bw.append("	}" + "\n");
		bw.newLine();
		bw.append("	public void setValue(Object value) {" + "\n");
		bw.append("		Config.putValue(key, value.toString());" + "\n");
		bw.append("	}" + "\n");

		bw.append("}\n");

		bw.flush();
		bw.close();

		Logger.debug("complete generating config manager class");

	}
}
