package core.support.annotation.template.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

public class ServiceData {

	public static JavaFileObject CSV_File_Object = null;
	public static String SERVICE_ROOT = "serviceManager";

	public static void writeServiceDataClass() {
		try {
			writeServiceDataClassImplementation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeServiceDataClassImplementation() throws Exception {

		Logger.debug("<<<<start generating service data>>>>>>");

		String testFolderPath = Config.getValue(TestDataProvider.API_KEYWORD_PATH);
		String csvTestPath = PropertiesReader.getLocalRootPath() + testFolderPath;
		ArrayList<File> csvFiles = Helper.getFileListByType(csvTestPath, ".csv");

		Map<String, ServiceObject> completeServices = new HashMap<String, ServiceObject>();

		// get all service keyword
		for (int i = 0; i < csvFiles.size(); i++) {
			List<Object[]> testCases = CsvReader.getCsvTestList(csvFiles.get(i));
			completeServices.putAll(CsvReader.mapToApiObject(testCases));
		}

		Logger.debug("csv keyword file count: " + csvFiles.size());
		Logger.debug("csv data generated class count: " + completeServices.size());

		// create separate class for each keyword
		for (Entry<String, ServiceObject> entry : completeServices.entrySet()) {
			writeServiceData(entry);
		}

		Logger.debug("<<<<scompleted generating service data>>>>>");
	}

	/**
	 * package module.serviceUiIntegration.panel;
	 * 
	 * import core.apiCore.ServiceManager; import
	 * core.support.objects.ServiceObject;
	 * 
	 * public class GetToken {
	 * 
	 * public GetToken withUsername(String username) { Config.putValue("username",
	 * username); return this; }
	 * 
	 * public GetToken withPassword(String password) { Config.putValue("password",
	 * password); return this; }
	 * 
	 * public void build() {
	 * 
	 * ServiceObject serviceObject = getServiceObject();
	 * 
	 * try { ServiceRunner.runInterface(serviceObject); } catch (Exception e) {
	 * e.printStackTrace(); } }
	 * 
	 * public ServiceObject getService() {
	 * 
	 * return getServiceObject(); }
	 * 
	 * public ServiceObject getServiceObject() {
	 * 
	 * ServiceObject serviceObject = new ServiceObject() .withTestSuite("")
	 * .withTestCaseID("") .withRunFlag("") .withDescription("")
	 * .withInterfaceType("") .withUriPath("") .withContentType("") .withMethod("")
	 * .withOption("") .withRequestHeaders("") .withTemplateFile("")
	 * .withRequestBody("") .withOutputParams("") .withRespCodeExp("")
	 * .withExpectedResponse("") .withTcComments("");
	 * 
	 * return serviceObject; } }
	 * 
	 * @param serviceMap
	 * @throws Exception
	 */

	private static void writeServiceData(Entry<String, ServiceObject> serviceEntry) throws Exception {

		String serviceClassName = StringUtils.capitalize(serviceEntry.getKey());

		String filePath = PackageHelper.SERVICE_PATH + File.separator + serviceClassName;
		File file = FileCreatorHelper.createFileAbsolutePath(filePath);
		FileWriter fw = new FileWriter(file);
	    BufferedWriter  bw = new BufferedWriter(fw);

		List<String> parameters = getParameters(serviceEntry.getValue().getRequestBody());

		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + SERVICE_ROOT + ";\n");
		bw.newLine();
		bw.newLine();

		// if there are parameters, then config reader is required
		if (parameters.size() > 0)
			bw.append("import core.support.configReader.Config;" + "\n");
		bw.append("import core.support.objects.ServiceObject;" + "\n");
		bw.newLine();
		bw.newLine();

		bw.append("public class " + serviceClassName + " {" + "\n");
		bw.newLine();
		bw.newLine();

		/*
		 * public GetToken withUsername(String username) { Config.putValue("username",
		 * username); return this; }
		 */
		for (String parameter : parameters) {
			bw.append("public " + serviceClassName + " with" + StringUtils.capitalize(parameter) + "(String "
					+ parameter + ") {\n");
			bw.append("    Config.putValue(\"" + parameter + "\" , " + parameter + ");" + "\n");
			bw.append("    return this;" + "\n");
			bw.append("}" + "\n");
			bw.newLine();
			bw.newLine();
		}

//		
//		public void build() {
//			
//			ServiceObject serviceObject = getServiceObject();
//			
//			try {
//				ServiceRunner.runInterface(serviceObject);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}		
//		}
		bw.append("public void build ()" + "\n");
		bw.append("{" + "\n");
		bw.append("    ServiceObject serviceObject = getServiceObject();" + "\n");
		bw.append("    try {" + "\n");
		bw.append("    		ServiceRunner.runInterface(serviceObject);" + "\n");
		bw.append("    	   } catch (Exception e) {" + "\n");
		bw.append("    		e.printStackTrace();" + "\n");
		bw.append("	   }" + "\n");
		bw.append("}" + "\n");
		bw.newLine();
		bw.newLine();

//		public ServiceObject getService() {
//			
//			return getServiceObject();
//		}
		bw.append("public ServiceObject getService()" + "\n");
		bw.append("{" + "\n");
		bw.append("    return getServiceObject();" + "\n");
		bw.append("}" + "\n");
		bw.newLine();
		bw.newLine();

//		public ServiceObject getServiceObject()
//		{
//			
//			ServiceObject serviceObject = new ServiceObject()
//					.withTestSuite("")
//					.withTestCaseID("")
//					.withRunFlag("")
//					.withDescription("")
//					.withInterfaceType("")
//					.withUriPath("")
//					.withContentType("")
//					.withMethod("")
//					.withOption("")
//					.withRequestHeaders("")
//					.withTemplateFile("")
//					.withRequestBody("")
//					.withOutputParams("")
//					.withRespCodeExp("")
//					.withExpectedResponse("")
//					.withTcComments("");
//			
//			return serviceObject;
//		}
		bw.append("public ServiceObject getServiceObject() " + "\n");
		bw.append("{" + "\n");
		bw.append("    ServiceObject serviceObject = new ServiceObject()" + "\n");
		bw.append("    				.withTestSuite(\"" + formatString(serviceEntry.getValue().getTestSuite()) + "\")"
				+ "\n");
		bw.append("    				.withTestCaseID(\"" + formatString(serviceEntry.getValue().getTestCaseID()) + "\")"
				+ "\n");
		bw.append("    				.withRunFlag(\"" + formatString(serviceEntry.getValue().getRunFlag()) + "\")"
				+ "\n");
		bw.append("    				.withInterfaceType(\"" + formatString(serviceEntry.getValue().getInterfaceType())
				+ "\")" + "\n");
		bw.append("    				.withUriPath(\"" + formatString(serviceEntry.getValue().getUriPath()) + "\")"
				+ "\n");
		bw.append("    				.withContentType(\"" + formatString(serviceEntry.getValue().getContentType())
				+ "\")" + "\n");
		bw.append("    				.withMethod(\"" + formatString(serviceEntry.getValue().getMethod()) + "\")" + "\n");
		bw.append("    				.withOption(\"" + formatString(serviceEntry.getValue().getOption()) + "\")" + "\n");
		bw.append("    				.withRequestHeaders(\"" + formatString(serviceEntry.getValue().getRequestHeaders())
				+ "\")" + "\n");
		bw.append("    				.withTemplateFile(\"" + formatString(serviceEntry.getValue().getTemplateFile())
				+ "\")" + "\n");
		bw.append("    				.withRequestBody(\"" + formatString(serviceEntry.getValue().getRequestBody())
				+ "\")" + "\n");
		bw.append("    				.withOutputParams(\"" + formatString(serviceEntry.getValue().getOutputParams())
				+ "\")" + "\n");
		bw.append("    				.withRespCodeExp(\"" + formatString(serviceEntry.getValue().getRespCodeExp())
				+ "\")" + "\n");
		bw.append("    				.withExpectedResponse(\""
				+ formatString(serviceEntry.getValue().getExpectedResponse()) + "\")" + "\n");
		bw.append("    				.withTcComments(\"" + formatString(serviceEntry.getValue().getTcComments()) + "\")"
				+ ";\n");
		bw.append("		return serviceObject;" + "\n");
		bw.append("} " + "\n");

		bw.append("}\n");

		bw.flush();
		bw.close();
	}

	private static String formatString(String val) {
		val = Helper.stringRemoveLines(val);
		val = val.replace("\"", "\\\"");
		return val;
	}

	/**
	 * returns a list of paramters in the request body
	 * 
	 * @param requestBody
	 * @return
	 */
	private static List<String> getParameters(String requestBody) {
		List<String> parametersCandidates = Helper.getValuesFromPattern(requestBody, "<(.+?)>");

		List<String> parameters = new ArrayList<String>();

		for (String parameter : parametersCandidates) {
			// if(parameter.contains("$") || parameter.contains("@_"))
			// continue;
			String value = parameter.replace("@", "");
			parameters.add(value);
		}
		return parameters;
	}
}
