package core.support.annotation.template.dataObject;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import core.support.annotation.helper.DataObjectHelper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.PackageHelper;

public class DataClass {
	
	public static JavaFileObject CSV_File_Object = null;
	public static String MODULE_ROOT = "module";
	public static String DATA_ROOT = "data";	
	
	  public static void writeDataClass() throws Exception {
		  List<File> files = DataObjectHelper.getAllCsvDataFiles(); 
		  Set<String> modules = PackageHelper.getModuleList(files); 

		  // return if no data files
		  if(files.isEmpty()) return;
		  
		  writeDataClass(modules);
	  }
		
//	package data;
//
//	public class Data {
//		public static webApp webApp = new webApp();
//		public static androidApp androidApp = new androidApp();
//	}
	public static void writeDataClass(Set<String> modules) throws Exception {
		
		String filePath = PackageHelper.DATA_PATH + "." + StringUtils.capitalize(DATA_ROOT);
		JavaFileObject fileObject = FileCreatorHelper.createFileAbsolutePath(filePath);
		
		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  and Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " +  DATA_ROOT + ";\n");
		bw.newLine();
		bw.newLine();
		
		// import data.webApp.webApp;
		for(String module : modules) {
			bw.append("import " + DATA_ROOT + "." + module + "." +  module + ";" + "\n" );
		}
		bw.newLine();
		bw.newLine();
		
		bw.append("public class " + StringUtils.capitalize(DATA_ROOT) + " {" + "\n");
		bw.newLine();
		bw.newLine();
		
		for(String module : modules) {
			bw.append("    public static " + module + " " + module + " = new " + module + "();" + "\n");
		}
		
		bw.newLine();
		bw.newLine();
		bw.append("}\n");

		bw.flush();
		bw.close();	
	}
}
