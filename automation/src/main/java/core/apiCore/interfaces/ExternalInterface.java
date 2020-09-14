package core.apiCore.interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;

public class ExternalInterface {

	private static final String METHOD = "method";

	/**
	 * interface for restful API calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static Object ExternalInterfaceRunner(ServiceObject serviceObject) {

		try {
			return evaluateTestMethod(serviceObject);
		} catch (Exception e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());
		}
		return null;
	}

	/**
	 * set method to call within module.services.method package
	 * supports single command 
	 * @param serviceObject
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Object evaluateTestMethod(ServiceObject serviceObject) throws Exception {

		// if no method specified
		if (serviceObject.getMethod().isEmpty()) {
			return null;
		}
		
		List<KeyValue> parameterList = null;
		
		// get parameter list
		parameterList = getParameterList(serviceObject);

		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getMethod());

		for (KeyValue keyword : keywords) {

			switch (keyword.key.toLowerCase()) {
			case METHOD:
				Object object = runExernalMethod(keyword.value.toString(), parameterList);
				return object;
			default:
				break;
			}
		}
		return null;
	}
	
	/**
	 * get parameter list
	 * command type methods 
	 * @param serviceObject
	 * @return
	 */
	private static List<KeyValue> getParameterList(ServiceObject serviceObject) {
		List<KeyValue> parameterList = null;
		
		// if command type method. command type includes parameters: response, list values
		if(serviceObject.getMethod().contains("METHOD:Command")) {
			parameterList = DataHelper.getValidationMap(serviceObject.getRequestBody(),";val");
			// set value key value after splitting request body based on ";values:"
			parameterList.get(1).value = DataHelper.replaceParameters(parameterList.get(1).value.toString());
			parameterList.get(1).key = "values";

		}else {
			serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));
			parameterList = DataHelper.getValidationMap(serviceObject.getRequestBody());
		}
		
		return parameterList;
	}

	/**
	 * run method from directory: src/main/java/module/services/method the class and
	 * method name are passed through options column: class.method
	 * 
	 * @param classmethod
	 * @return 
	 * @throws Exception
	 */
	public static Object runExernalMethod(String classmethod, List<KeyValue> parameterList) throws Exception {
		String[] methodInfo = classmethod.split("\\.");
		if (methodInfo.length < 2)
			Helper.assertFalse("wrong method format. must be class.method");

		
		String className = methodInfo[0];
		String methodName = methodInfo[1];
		TestLog.logPass("invoking method: " + methodName + " at class: " + className);
		String sourcePath = getExternalMethodPath(className);

		
		return Helper.runInternalClass(sourcePath, methodName, parameterList);
	}
	
	/**
	 * gets File from root directory root directory: where pom file is located if
	 * classname matches the class in method directory, then the class is returned
	 * 
	 * @param dirs
	 * @return
	 */
	public static String getExternalMethodPath(String classmethod) {

		String path = "module" + File.separator + "services" + File.separator + "method";
		File folder = new File(Helper.getFullPath("src" + File.separator + "main"));
		String classPath = "module.services.method." + classmethod;

		List<File> listOfFiles = new ArrayList<File>();
		listOfFiles = getAllFiles(folder, listOfFiles);

		for (File file : listOfFiles) {
			if (file.getAbsolutePath().contains(path + File.separator + classmethod))
				return classPath;
		}

		Helper.assertFalse("directory for external test method not found: " + path);
		return null;
	}
	

	/**
	 * gets list of files in specified directory
	 * 
	 * @param pathToDir
	 * @param listOfFiles
	 * @return
	 */
	private static List<File> getAllFiles(File pathToDir, List<File> listOfFiles) {
		if (pathToDir.isDirectory()) {
			String[] subdirs = pathToDir.list();
			for (int i = 0; i < subdirs.length; i++) {
				getAllFiles(new File(pathToDir, subdirs[i]), listOfFiles);
			}
		} else {
			listOfFiles.add(pathToDir);
		}
		return listOfFiles;
	}

}
