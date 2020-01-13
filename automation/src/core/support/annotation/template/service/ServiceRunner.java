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
import java.util.List;
import org.apache.commons.lang.StringUtils;
import core.apiCore.ServiceManager;
import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import core.uiCore.drivers.AbstractDriverTestNG;
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
		
		
		bw.append("import core.support.objects.ServiceObject;" + "\n");
		bw.append("import core.uiCore.drivers.AbstractDriverTestNG;" + "\n");
		bw.append("import core.apiCore.ServiceManager;" + "\n");
		bw.append("import java.util.List;" + "\n");
		bw.append("import org.apache.commons.lang.StringUtils;" + "\n");
		bw.append("import core.apiCore.helpers.DataHelper;" + "\n");
		bw.append("import core.helpers.Helper;" + "\n");
		bw.append("import core.support.configReader.Config;" + "\n");
		bw.append("import core.support.logger.TestLog;" + "\n");
		bw.append("import core.support.objects.KeyValue;" + "\n");
		bw.append("import core.support.objects.TestObject;" + "\n");
		
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
		bw.append("		String tcName, String tcIndex, String testType) throws Exception {" + " \n" );
		bw.newLine();
		bw.append("		// add parameters to ServiceObject" + " \n" );
		bw.append("		ServiceObject serviceObject = new ServiceObject().setServiceObject(TestSuite, TestCaseID, RunFlag, Description, InterfaceType," + " \n" );
		bw.append("		UriPath, ContentType, Method, Option, RequestHeaders, TemplateFile, RequestBody, OutputParams," + " \n" );
		bw.append("		RespCodeExp, ExpectedResponse, TcComments, tcName," + " \n" );
		bw.append("		tcIndex, testType);"+ " \n" );
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
		bw.append("		runInterface(serviceObject);" + " \n");
		bw.newLine();
		bw.append("		// run after each test file" + " \n");
		bw.append("		ServiceManager.runAfterCsv(serviceObject);" + " \n");
		bw.append("}" + " \n");
		bw.newLine();
		bw.newLine();
		
		/**
		 
		 public static void runInterface(ServiceObject serviceObject) throws Exception {
		 
		 evaluateOption(serviceObject);

		int runCount = Config.getIntValue(ServiceManager.SERVICE_RUN_COUNT);
		for (int i = 1; i <= runCount; i++) {
			Config.putValue(ServiceManager.SERVICE_RUN_CURRENT_COUNT, i);
			if (i > 1)
				TestLog.ConsoleLog("Starting run: " + i);
		 
		switch (serviceObject.getInterfaceType()) {
		case TEST_INTERFACE:
			new TestInterface(serviceObject);
			break;
		default:
			ServiceManager.TestRunner(serviceObject);
			break;
		}
		evaluateResults();
		 */
		
		bw.append("		public static void runInterface(ServiceObject serviceObject) throws Exception {" + " \n");
		bw.newLine();
		bw.append("			evaluateOption(serviceObject);" + " \n");
		bw.append(" 		int runCount = Config.getIntValue(ServiceManager.SERVICE_RUN_COUNT);" + " \n");
		bw.append(" 		for (int i = 1; i <= runCount; i++) {" + " \n");
		bw.append(" 			Config.putValue(ServiceManager.SERVICE_RUN_CURRENT_COUNT, i);" + " \n");
		bw.append(" 			if (i > 1)" + " \n");
		bw.append(" 				TestLog.ConsoleLog(\"Starting run: \" + i);" + " \n");
		bw.newLine();
		bw.append("			switch (serviceObject.getInterfaceType()) {");
		
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
		
		bw.append("	}" + " \n");

		bw.append("	evaluateResults();"+ " \n");
		bw.append("}" + " \n");
		bw.newLine();
		bw.newLine();
		
		
		/*

		public static void evaluateResults() {
			List<String> errorMessages = TestObject.getTestInfo().testErrors;
			if (!errorMessages.isEmpty()) {
				TestLog.logPass(StringUtils.join(errorMessages, "\n error: "));
				Helper.assertFalse(StringUtils.join(errorMessages, "\n error: "));
			}
		}
		/*
		 * 
		 */
		bw.append("		public static void evaluateResults() {" + " \n");
		bw.append("			List<String> errorMessages = TestObject.getTestInfo().testErrors;" + " \n");
		bw.append("			if (!errorMessages.isEmpty()) {" + " \n");
		bw.append("				TestLog.logPass(StringUtils.join(errorMessages, \"\\n error: \"));" + " \n");
		bw.append("				Helper.assertFalse(StringUtils.join(errorMessages, \"\\n error: \"));" + " \n");
		bw.append("			}" + " \n");
		bw.append("		}" + " \n");
		bw.newLine();
		bw.newLine();
		
		/*
		 * public static void evaluateOption(ServiceObject serviceObject) {

		// reset validation timeout. will be overwritten by option value if set
		resetOptions();

		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key) {
			case ServiceManager.OPTION_RUN_COUNT:
				Config.putValue(ServiceManager.SERVICE_RUN_COUNT, keyword.value);
				break;
			default:
				break;
			}
		}
	}
		 */
		bw.append("	 public static void evaluateOption(ServiceObject serviceObject) {" + " \n");
		bw.append(" 	// reset validation timeout. will be overwritten by option value if set"+ " \n");
		bw.append(" 	resetOptions();" + " \n");
		bw.newLine();
		bw.append(" 	// replace parameters for request body" + " \n");
		bw.append(" 	serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));" + " \n");
		bw.newLine();
		bw.append(" 	// get key value mapping of header parameters" + " \n");
		bw.append(" 	List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());" + " \n");
		bw.newLine();
		bw.append(" 	// iterate through key value pairs for headers, separated by \";\"" + " \n");
		bw.append(" 	for (KeyValue keyword : keywords) {" + " \n");
		bw.newLine();
		bw.append(" 		// if additional options" + " \n");
		bw.append(" 		switch (keyword.key) {" + " \n");
		bw.append(" 		case ServiceManager.OPTION_RUN_COUNT:" + " \n");
		bw.append(" 			Config.putValue(ServiceManager.SERVICE_RUN_COUNT, keyword.value);" + " \n");
		bw.append(" 			break;" + " \n");
		bw.append(" 		default:" + " \n");
		bw.append(" 			break;" + " \n");
		bw.append(" 		}" + " \n");
		bw.append(" 	}" + " \n");
		bw.append("  }" + " \n");
		
		
		/*
		 	private static void resetOptions() {
				Config.putValue(ServiceManager.SERVICE_RUN_COUNT, 1);
		
			}
		 */
		bw.newLine();
		bw.newLine();
		bw.append("	private static void resetOptions() {" + " \n");
		bw.append("		Config.putValue(ServiceManager.SERVICE_RUN_COUNT, 1);" + " \n");
		bw.append("	}" + " \n");
		bw.append("}" + " \n");		
		bw.flush();
		bw.close();		
		
		Logger.debug("<<<< completed generating service class: " + serviceClassName + ">>>>");

	}
}
