package core.support.annotation.template.service;


import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import core.apiCore.TestDataProvider;
import core.apiCore.helpers.CsvReader;
import core.helpers.Helper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.objects.ServiceObject;

public class Service {
	
	public static JavaFileObject CSV_File_Object = null;
	public static String SERVICE_ROOT = "serviceManager";
	public static String SERVICE_CLASS = "Service";

	public static void writeServiceClass()  {
		try {
			writeServiceClassImplementation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeServiceClassImplementation() throws Exception {
			String testFolderPath = Config.getValue(TestDataProvider.API_KEYWORD_PATH);
			String csvTestPath = PropertiesReader.getLocalRootPath() + testFolderPath;
			ArrayList<File> csvFiles = Helper.getFileList(csvTestPath, ".csv");
			
			Map<String, ServiceObject> completeServices = new HashMap<String, ServiceObject>();

			// get all service keyword
			for (int i = 0; i < csvFiles.size(); i++) {
				List<String[]> testCases = CsvReader.getCsvTestList(csvFiles.get(i));
				completeServices.putAll(CsvReader.mapToApiObject(testCases));
			}
		  
			// create separate class for each keyword
			writeServiceData(completeServices);
	  }
	
	/**
package service;

import core.support.objects.ServiceObject;

public class Service {
	
	public static GetToken getToken = new GetToken();
	
	public ServiceObject create() {
		return new ServiceObject();
	}
}

	 * @param serviceMap
	 * @throws Exception 
	 */
	
	private static void writeServiceData(Map<String, ServiceObject> completeServices) throws Exception {
	   		
		String serviceClassName = StringUtils.capitalize(SERVICE_CLASS);
		
		Logger.debug("<<<<< start generating service class " + serviceClassName + " >>>>");

		
		String filePath = PackageHelper.SERVICE_PATH + "." + serviceClassName;
		JavaFileObject fileObject = FileCreatorHelper.createFileAbsolutePath(filePath);
		
		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + SERVICE_ROOT + ";\n");
		bw.newLine();
		bw.newLine();
		
		bw.append("import core.support.objects.ServiceObject;"+ "\n");
		bw.newLine();
		bw.newLine();

		
		bw.append("public class " + serviceClassName + " {" + "\n");
		bw.newLine();
		bw.newLine();

		
		/*
		public static GetToken getToken = new GetToken();

		 */
		for (Entry<String, ServiceObject> entry : completeServices.entrySet()) {			
			String serviceName = StringUtils.capitalize(entry.getKey());

			bw.append("public static " + serviceName + " " + entry.getKey() + " = new " + serviceName + "();"+ " \n" );
		}
		bw.newLine();
		bw.newLine();
		
//		public ServiceObject create()
//		{
//			return new ServiceObject();
//		}
		bw.append("public static ServiceObject create()" + "\n");
		bw.append("{"+ "\n");
		bw.append("    return new ServiceObject();" + "\n");
		bw.append("}"+ "\n");
		bw.newLine();
		bw.newLine();

		bw.append("}\n");

		bw.flush();
		bw.close();		
		
		Logger.debug("<<<< completed generating service class: " + serviceClassName + ">>>>");

	}
}
