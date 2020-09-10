package core.support.annotation.template.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.googlejavaformat.java.Formatter;

import core.apiCore.TestDataProvider;
import core.apiCore.driver.ApiTestDriver;
import core.apiCore.helpers.CsvReader;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.objects.ServiceObject;

public class ServiceClass {

	private static final String SERVICE_TEST_PATH = "src" + File.separator + "test" + File.separator + "java" + File.separator + "test" + File.separator + "module" + File.separator + "service" +  File.separator + "generated" + File.separator;
	private static final String SERVICE_TEST_ROOT = ".." + File.separator + "apiTestData" + File.separator + "testCases" + File.separator;
	private static final String SERVICE_GENERATE_CLASS = "service.generate.class";

	public static void writeServiceGenerationClass() {
		
		if(!Config.getBooleanValue(SERVICE_GENERATE_CLASS))
			return;
		
		try {
			writeServiceClass();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeServiceClass() throws Exception {
		
		// get current csv file
		boolean includeSubDir = Config.getBooleanValue(CsvReader.SERVICE_CSV_INCLUDE_SUB_DIR);
		String testCsvPath = Config.getValue(TestDataProvider.TEST_DATA_PARALLEL_PATH);
		
		ArrayList<File> testCsvFileList = Helper.getFileListByType(testCsvPath, ".csv", includeSubDir);
		
		for(File csvFile : testCsvFileList) {
			
			// get test rows from csv file
			List<Object[]> csvList = CsvReader.getCsvTestListForTestRunner(csvFile.getName());
			
			// create empty file 
			File testClassPath = createClassFile(csvFile);
			
			writeTestClass(testClassPath, csvList);
				

		}
	}
	
	/**
	 	package test.module.service.generated;

		import core.support.objects.ServiceObject;
		import serviceManager.ServiceRunner;
		
		public class UserValidationReference {
		
			
			 * Retrieve a token from Token Generator
			 * @throws Exception
			
			public void getAdminToken() throws Exception {
				
				ServiceObject service = new ServiceObject();
			    service.withTestSuite("TsUser");
			    service.withTestCaseID("createUser");
			    service.withDescription("create user");
			    service.withInterfaceType("RESTfulAPI");
			    service.withUriPath("/content-manager/explorer/user/?source=users-permissions");
			    service.withContentType("application/x-www-form-urlencoded");
			    service.withMethod("POST");
			    service.withRequestHeaders("Authorization: Bearer <@accessTokenAdmin>");
			    service.withRequestBody(
			        "username:zzz_test<@_TIME_MS_24>, email:testuser+<@_TIME_MS_24>@gmail.com, password:password<@_TIME_MS_24>, confirmed:true");
			    service.withOutputParams("id:<$userId>");
			    service.withRespCodeExp("201");
			    service.withExpectedResponse(
			        "{ \"provider\": \"local\", \"blocked\": null } && _VERIFY.JSON.PART_ \"id\": isNotEmpty");
			    service.withTcIndex("1:6");
			    service.withTcName("UserValidation");
			    service.withTcType("service");
				new AbstractDriverTestNG().setupApiDriver(service);
				
				ServiceRunner.runServiceTests(service);
			}
		}

	 * 
	 * 
	 * @param ServiceObject
	 * @param classFile
	 * @throws Exception 
	 */
	private static void writeTestClass(File classFile, List<Object[]> csvList) throws Exception {
		
		
		StringWriter sw = new StringWriter();
		
		sw.write("/**Auto generated code, do not modify.\n");
		sw.write("*");
		sw.write("**/\n\n\n\n");
		
		sw.write("package " + getPackageName(classFile) + ";" + "\n");
		
		sw.write("import core.support.objects.ServiceObject;" + "\n");
		sw.write("import serviceManager.ServiceRunner;" + "\n");
		sw.write("import org.testng.annotations.Test;" + "\n");
		sw.write("import test.module.service.TestBase;" + "\n");
		
		sw.write("public class "+ ApiTestDriver.getTestClass(classFile.getName()) +" extends TestBase  {");
		
		int priority = 0;
		int index = 0;
		for(Object[] testRow : csvList) {
			    
				priority++;
				ServiceObject ServiceObject = CsvReader.mapToServiceObject(testRow); 
							
				sw.write("	/**" + "\n");
				sw.write("	* " + ServiceObject.getDescription() + "\n");
				sw.write("	* @throws Exception" + "\n");
				sw.write("	*/" + "\n");
				sw.write("  @Test(description =\""+ ServiceObject.getDescription() +"\", priority="+priority+" )" + "\n");
				sw.write("	public void "+ ServiceObject.getTestCaseID() +"() throws Exception {" + "\n");
				sw.write("  ServiceObject service = new ServiceObject();" + "\n");
				sw.write(   "service.withTestSuite(\"" + ServiceData.formatString(ServiceObject.getTestSuite()) + "\");" + "\n");
				sw.write(   "service.withTestCaseID(\"" + ServiceData.formatString(ServiceObject.getTestCaseID()) + "\");" + "\n");
				sw.write(   "service.withDescription(\"" + ServiceData.formatString(ServiceObject.getDescription()) + "\");" + "\n");
		
				
				if(!ServiceObject.getInterfaceType().isEmpty())
					sw.write(   "service.withInterfaceType(\"" + ServiceData.formatString(ServiceObject.getInterfaceType()) + "\");" + "\n");
				
				if(!ServiceObject.getUriPath().isEmpty())
					sw.write(   "service.withUriPath(\"" + ServiceData.formatString(ServiceObject.getUriPath()) + "\");" + "\n");
				
				if(!ServiceObject.getContentType().isEmpty())
					sw.write(   "service.withContentType(\"" + ServiceData.formatString(ServiceObject.getContentType()) + "\");" + "\n");
		
				if(!ServiceObject.getMethod().isEmpty())
					sw.write(   "service.withMethod(\"" + ServiceData.formatString(ServiceObject.getMethod()) + "\");" + "\n");
		
				if(!ServiceObject.getOption().isEmpty())
					sw.write(   "service.withOption(\"" + ServiceData.formatString(ServiceObject.getOption()) + "\");" + "\n");
				
				if(!ServiceObject.getRequestHeaders().isEmpty())
					sw.write(   "service.withRequestHeaders(\"" + ServiceData.formatString(ServiceObject.getRequestHeaders()) + "\");" + "\n");
		
				if(!ServiceObject.getTemplateFile().isEmpty())
					sw.write(   "service.withTemplateFile(\"" + ServiceData.formatString(ServiceObject.getTemplateFile()) + "\");" + "\n");
				
				if(!ServiceObject.getRequestBody().isEmpty())
					sw.write(   "service.withRequestBody(\"" + ServiceData.formatString(ServiceObject.getRequestBody()) + "\");" + "\n");
		
				if(!ServiceObject.getOutputParams().isEmpty())
					sw.write(   "service.withOutputParams(\"" + ServiceData.formatString(ServiceObject.getOutputParams()) + "\");" + "\n");
				
				if(!ServiceObject.getRespCodeExp().isEmpty())
					sw.write(   "service.withRespCodeExp(\"" + ServiceData.formatString(ServiceObject.getRespCodeExp()) + "\");" + "\n");
				
				if(!ServiceObject.getExpectedResponse().isEmpty())
					sw.write(   "service.withExpectedResponse(\"" + ServiceData.formatString(ServiceObject.getExpectedResponse()) + "\");" + "\n");
				
				if(!ServiceObject.getTcComments().isEmpty())
					sw.write(   "service.withTcComments(\"" + ServiceData.formatString(ServiceObject.getTcComments()) + "\");" + "\n");
				
				sw.write(   "service.withTcIndex(\"" + index +":"+ csvList.size() + "\");" + "\n");
				sw.write(   "service.withTcName(\"" + ApiTestDriver.getTestClass(classFile.getName()) + "\");" + "\n");
				sw.write(   "service.withTcType(\"" + "service" + "\");" + "\n");
				sw.write("	ServiceRunner.TestRunner(service);" + "\n");
				sw.write(" }" + "\n");
				
				index++;
			
			}
			sw.write("}" + "\n");
			
	
			String output = sw.toString();
			
		    Formatter formatter = new Formatter();
			output = formatter.formatSource(output);
			
			PrintWriter pw = new PrintWriter(classFile.getAbsolutePath());
			pw.print(output);
	
			pw.close();
		
		
	}
	
	/**
	 * create empty class file from csv file
	 * @param filePath
	 * @return 
	 * @throws FileNotFoundException
	 */
	private static File createClassFile(File csvFile) throws FileNotFoundException {
		String testname = ApiTestDriver.getTestClass(csvFile.getName());
		
		String fileRelativePath = csvFile.getParentFile().getAbsolutePath().replace(new File(Helper.getFullPath(SERVICE_TEST_ROOT)).getAbsolutePath(),"");
		String testClassPath = Helper.getFullPath(SERVICE_TEST_PATH + fileRelativePath + File.separator + testname + ".java");
		
		Helper.createFileFromPath(testClassPath);

		PrintWriter pw = new PrintWriter(testClassPath);
		pw.close();
		
		return new File(testClassPath);
	}
	
	private static String getPackageName(File file) {
		String packagename = StringUtils.EMPTY;
		
		String packageFormat = file.getParentFile().getAbsolutePath().replace(File.separatorChar, '.');
		String[] packagePath = packageFormat.split("test.module.service.generated");
		if(packagePath.length == 1)
			packagename = "test.module.service.generated";
		else
			packagename = "test.module.service.generated" + packagePath[1];
		
		return packagename;
		
	}
}