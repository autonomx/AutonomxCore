package core.support.annotation.template.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.JavaFileObject;

import core.support.annotation.helper.DataObjectHelper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import core.support.annotation.processor.MainGenerator;
import core.support.configReader.Config;

public class ConfigVariableGenerator {
	
	public static String CONFIG_VARIABLE = "ConfigVariable";

	
	public static void writeConfigVariableClass()  {
		try {
			writeConfigVariableClassImplementation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeConfigVariableClassImplementation() throws IOException {
		Logger.debug("start generating config variable class");

		// proceed if config manager has not been created
		if (FileCreatorHelper.CONFIG_VARIABLE_FILE_OBJECT != null)
			return;

		// create file: ConfigVariable.java
		JavaFileObject fileObject = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.CONFIG_MANAGER_PATH + "." + CONFIG_VARIABLE);
		FileCreatorHelper.CONFIG_VARIABLE_FILE_OBJECT = fileObject;

		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + PackageHelper.CONFIG_MANAGER_PATH + ";\n");
		bw.newLine();
		bw.newLine();
		bw.append("import core.support.configReader.Config;"+ "\n");
		bw.newLine();
		bw.newLine();

		/*
		package configManager;

		public class ConfigVariable {
			
			public static ConfigManager globalParallelTestCount() {
				String value = Config.getValue("global.parallel.testCount");
				return new ConfigModule(value,"1");
			}
			
			public void setValue(String key, String value){
				Config.putValue(key, value.toString());
			}
		}
		*/
		bw.append("public class ConfigVariable {\n");
		bw.newLine();
		
		// loads all property values into config map
		Map<String, Object> config = Config.loadConfigProperties();
		
		/**
		 public static ConfigManager parallelTestCount() {
				return new ConfigManager("parallelTestCount");
		}
			
		 */
		for (Entry<String, Object> entry : config.entrySet()) {
			
			bw.append("	public static ConfigManager "+ DataObjectHelper.normalizeMethod(entry.getKey()) +"() {" + "\n");
			bw.append("		String value = Config.getValue(\""+ entry.getKey() +"\");" + "\n");
			bw.append("		return new ConfigManager(\""+  entry.getKey() + "\", " +  "value" +");" + "\n");
			bw.append("	}" + "\n");
			bw.newLine();
		}
		
		bw.append("	public static void setValue(String key, Object value) {" + "\n");
		bw.append("		Config.putValue(key, value.toString());" + "\n");
		bw.append("	}" + "\n");
		
		bw.append("	public static String getStringValue(String key) {" + "\n");
		bw.append("		String value = Config.getValue(key);" + "\n");
		bw.append("		return value;" + "\n");
		bw.append("	}" + "\n");
		
		bw.append("	public static boolean getBooleanValue(String key) {" + "\n");
		bw.append("		boolean value = Config.getBooleanValue(key);" + "\n");
		bw.append("		return value;" + "\n");
		bw.append("	}" + "\n");
		
		bw.append("	public static int getIntegerValue(String key) {" + "\n");
		bw.append("		int value = Config.getIntValue(key);" + "\n");
		bw.append("		return value;" + "\n");
		bw.append("	}" + "\n");
		
		bw.append("}\n");
		
		
		
		bw.flush();
		bw.close();
		
		Logger.debug("complete generating config variable class");

	}
}
