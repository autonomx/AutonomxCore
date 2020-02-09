package core.support.annotation.template.service;


import java.io.BufferedWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import org.apache.commons.lang3.StringUtils;

import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;

public class ServiceRunner {
	
	public static JavaFileObject CSV_File_Object = null;
	public static String SERVICE_ROOT = "serviceManager";
	public static String SERVICE_RUNNER_CLASS = "ServiceRunner";

	public static void writeServiceClass(Map<String, List<Element>> serviceMap)  {
		try {
			writeServiceClassImplementation(serviceMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeServiceClassImplementation(Map<String, List<Element>> serviceMap) throws Exception {
			
			// create separate class for each keyword
			writeServiceRunner(serviceMap);
	  }
	
	/**
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import core.uiCore.drivers.AbstractDriverTestNG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.apiCore.ServiceManager;
import core.apiCore.helpers.CsvReader;
import module.services.interfaces.TestInterface;

public class ServiceManager {

	private static final String TEST_INTERFACE = "RESTfulAPI";

	public static void TestRunner(String TestSuite, String TestCaseID, String RunFlag, String Description, 
			String InterfaceType, String UriPath, String ContentType, String Method, String Option, 
			String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp, 
			String ExpectedResponse, String TcComments, 
			String tcName, String tcIndex, String testType) throws Exception { 
	
			// add parameters to ServiceObject 
			ServiceObject serviceObject = new ServiceObject().setServiceObject(TestSuite, TestCaseID, RunFlag, Description, InterfaceType, 
			UriPath, ContentType, Method, Option, RequestHeaders, TemplateFile, RequestBody, OutputParams, 
			RespCodeExp, ExpectedResponse, TcComments, tcName, 
			tcIndex, testType); 
	
			// setup api driver 
			new AbstractDriverTestNG().setupApiDriver(serviceObject); 
			runInterface(serviceObject); 
	} 
	
	
	public static void runInterface(ServiceObject serviceObject) throws Exception { 
	
	
			switch (serviceObject.getInterfaceType()) {
			case "TestInterface": 
	  		   new TestInterface().testInterface(serviceObject);
					break; 
			default: 
					ServiceManager.runInterface(serviceObject); 
					break; 
			} 
	} 
}

	 * @param serviceMap
	 * @throws Exception 
	 */
	
	private static void writeServiceRunner(Map<String, List<Element>> serviceMap) throws Exception {
	   		
		String serviceClassName = StringUtils.capitalize(SERVICE_RUNNER_CLASS);
		
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
		
		
	
		bw.append("import core.support.logger.TestLog;"+ "\n");
		bw.append("import core.apiCore.helpers.CsvReader;"+ "\n");
		bw.newLine();
		bw.append("import java.util.HashMap;"+ "\n");
		bw.append("import java.util.List;"+ "\n");
		bw.append("import java.util.Map;"+ "\n");
		bw.newLine();
		bw.append("import core.support.objects.ServiceObject;"+ "\n");
		bw.append("import core.uiCore.drivers.AbstractDriverTestNG;"+ "\n");
		bw.append("import core.apiCore.ServiceManager;"+ "\n");
		// add the service imports
		for (Entry<String, List<Element>> entry : serviceMap.entrySet()) {
			for (Element element : entry.getValue()) {
				String servicelPath = element.asType().toString();
				bw.append("import " + servicelPath + ";\n");
			}
		}
		bw.newLine();
		bw.newLine();
		
		
		bw.append("public class " + serviceClassName + " {" + "\n");
		bw.newLine();
		bw.newLine();

		
		/**
		public static void TestRunner(String TestSuite, String TestCaseID, String RunFlag, String Description, 
				String InterfaceType, String UriPath, String ContentType, String Method, String Option, 
				String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp, 
				String ExpectedResponse, String TcComments, 
				String tcName, String tcIndex, String testType) throws Exception { 
		
			// add parameters to ServiceObject 
			ServiceObject serviceObject = new ServiceObject().setServiceObject(TestSuite, TestCaseID, RunFlag, Description, InterfaceType, 
				UriPath, ContentType, Method, Option, RequestHeaders, TemplateFile, RequestBody, OutputParams, 
				RespCodeExp, ExpectedResponse, TcComments, tcName, 
				tcIndex, testType); 
				
			ServiceManager.setupParentObject(serviceObject);
			
			// set test base override
			ServiceManager.setTestBaseOverride(serviceObject);
				
			// run before each test file 
			ServiceManager.runBeforeCsv(serviceObject); 
	
			// setup api driver 
			new AbstractDriverTestNG().setupApiDriver(serviceObject); 
			runInterface(serviceObject); 
	
			// run after each test file 
			ServiceManager.runAfterCsv(serviceObject); 
		}
		 */
		
		bw.append("public static void TestRunner(String TestSuite, String TestCaseID, String RunFlag, String Description," + " \n" );
		bw.append("		String InterfaceType, String UriPath, String ContentType, String Method, String Option," + " \n" );
		bw.append("		String RequestHeaders, String TemplateFile, String RequestBody, String OutputParams, String RespCodeExp," + " \n" );
		bw.append("		String ExpectedResponse, String TcComments," + " \n" );
		bw.append("		String tcName, String tcIndex, String testType, Object... serviceSteps) throws Exception {" + " \n" );
		bw.newLine();
		bw.append("		// add parameters to ServiceObject" + " \n" );
		bw.append("		ServiceObject serviceObject = new ServiceObject().setServiceObject(TestSuite, TestCaseID, RunFlag, Description, InterfaceType," + " \n" );
		bw.append("		UriPath, ContentType, Method, Option, RequestHeaders, TemplateFile, RequestBody, OutputParams," + " \n" );
		bw.append("		RespCodeExp, ExpectedResponse, TcComments, tcName," + " \n" );
		bw.append("		tcIndex, testType, serviceSteps);"+ " \n" );
		bw.newLine();
		bw.append("		// set parent object" + " \n");
		bw.append("		ServiceManager.setupParentObject(serviceObject);" + " \n");
		bw.newLine();
		bw.append("		// set test base override" + " \n");
		bw.append("		ServiceManager.setTestBaseOverride(serviceObject);" + " \n");
		bw.newLine();
		bw.append("		// run before each test file" + " \n");
		bw.append("		ServiceManager.runBeforeCsv(serviceObject);" + " \n");
		bw.newLine();
		bw.append("		// setup api driver" + " \n");
		bw.append("		new AbstractDriverTestNG().setupApiDriver(serviceObject);" + " \n");
		bw.append("		runServiceTests(serviceObject);" + " \n");
		bw.newLine();
		bw.append("		// run after each test file" + " \n");
		bw.append("		ServiceManager.runAfterCsv(serviceObject);" + " \n");
		bw.append("}" + " \n");
		bw.newLine();
		bw.newLine();
		
		
		
		/**
		 public static void runServiceTests(ServiceObject serviceObject) throws Exception { 
			if(serviceObject.getServiceSteps() instanceof HashMap) {
				Map<String, List<Object[]>> testStepMap = (Map<String, List<Object[]>>) serviceObject.getServiceSteps();
				List<Object[]> teststeps = testStepMap.get(CsvReader.getTestname(serviceObject.getTestCaseID()));
				for(int i = 0; i < teststeps.size(); i ++) {
					TestLog.logPass("******** Starting Step " +  (i+1) + " **********");
					ServiceObject stepObject = CsvReader.mapToServiceObject(teststeps.get(i)); 
					runInterface(stepObject);
				}
			}else
				runInterface(serviceObject);
		}
		 */
		
		bw.append("	public static void runServiceTests(ServiceObject serviceObject) throws Exception {" + " \n");
		bw.append("		if(serviceObject.getServiceSteps() instanceof HashMap) {" + " \n");
		bw.append("			Map<String, List<Object[]>> testStepMap = (Map<String, List<Object[]>>) serviceObject.getServiceSteps();" + " \n");
		bw.append("			List<Object[]> teststeps = testStepMap.get(CsvReader.getTestname(serviceObject.getTestCaseID()));" + " \n");
		bw.append("			for(int i = 0; i < teststeps.size(); i ++) {" + " \n");
		bw.append("				TestLog.logPass(\"******** Starting Step \" +  (i+1) + \" **********\");" + " \n");
		bw.append("				ServiceObject stepObject = CsvReader.mapToServiceObject(teststeps.get(i)); " + " \n");
		bw.append("				runInterface(stepObject);" + " \n");
		bw.append("			}" + " \n");
		bw.append("		}else" + " \n");
		bw.append("			runInterface(serviceObject);" + " \n");
		bw.append("	}" + " \n");
		bw.newLine();
		bw.newLine();
		
		
		
		/**
		 
		 public static void runInterface(ServiceObject serviceObject) throws Exception {
		 
		switch (serviceObject.getInterfaceType()) {
		case TEST_INTERFACE:
			new TestInterface(serviceObject);
			break;
		default:
			ServiceManager.TestRunner(serviceObject);
			break;
		}
		 */
		
		bw.append("public static void runInterface(ServiceObject serviceObject) throws Exception {" + " \n");
		bw.newLine();
		bw.newLine();
		bw.append("		switch (serviceObject.getInterfaceType()) {");
		
		for (Entry<String, List<Element>> entry : serviceMap.entrySet()) {
			for (Element element : entry.getValue()) {
				String serviceName = element.getSimpleName().toString();
				// first letter is lower cased
				String lowerCaseServiceName = Character.toLowerCase(serviceName.charAt(0)) + serviceName.substring(1);


				bw.newLine();
				bw.append("		case \"" + serviceName + "\":" + " \n");
				bw.append("  		  new " + serviceName + "()." + lowerCaseServiceName + "(serviceObject);" + " \n");
				bw.append("				break;" + " \n");
			}
		}
		
		bw.append("		default:" + " \n");
		bw.append("				ServiceManager.runInterface(serviceObject);" + " \n");
		bw.append("				break;" + " \n");
		bw.append("		}" + " \n");
		
		bw.append("}" + " \n");
		bw.append("}" + " \n");
		bw.flush();
		bw.close();		
		
		Logger.debug("<<<< completed generating service class: " + serviceClassName + ">>>>");

	}
}
