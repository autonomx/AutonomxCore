package core.apiCore.interfaces;


import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.JsonHelper;
import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import groovy.lang.GroovyClassLoader;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ExternalInterface {

	private static final String METHOD = "method";


	/**
	 * interface for restful API calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception 
	 */
	public static void ExternalInterfaceRunner(ServiceObject serviceObject) throws Exception {
		
		evaluateTestMethod(serviceObject);
		
	}
	
	/**
	 * set method to call within module.services.method package
	 * @param serviceObject
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public static void evaluateTestMethod(ServiceObject serviceObject) throws Exception {
		
		// if no method specified
		if (serviceObject.getMethod().isEmpty()) {
			return;
		}
		
		// replace parameters for request body, including template file (json, xml, or other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));
		List<KeyValue> parameterList = DataHelper.getValidationMap(serviceObject.getRequestBody());
		
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getMethod());

		for (KeyValue keyword : keywords) {	
			
			switch (keyword.key.toLowerCase()) {
			case METHOD:
				runExernalMethod(keyword.value.toString(), parameterList);
				break;
			default:
				break;
			}
			
		}
	}
	
	/**
	 * run method from directory: src/main/java/module/services/method
	 * the class and method name are passed through options column: class.method
	 * @param classmethod
	 * @throws Exception
	 */
	public static void runExernalMethod(String classmethod, List<KeyValue> parameterList) throws Exception {
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
		
		
		Object[] parameters = parameters(parameterList)	;


		String[] methodInfo = classmethod.split("\\.");
		if(methodInfo.length < 2)
			Helper.assertFalse("wrong method format. must be class.method");
		
		String className = methodInfo[0];
		String methodName = methodInfo[1];
		TestLog.logPass("invoking method: " + methodName + " at class: " +  className);
		
		File sourceFile = getExternalMethodFilePath(className);
		Class<?> externalClass = groovyClassLoader.parseClass(sourceFile);
		Object external = externalClass.newInstance(); 
		
		
		
		// get parameter types
		Class<?>[] paramTypes = getMethodParameterTypes(externalClass, methodName, parameterList);
		Method method = externalClass.getMethod(methodName, paramTypes);
		
		// validate parameter count
		if(parameters.length != paramTypes.length)
			Helper.assertFalse("number of parameters must match method parameters");
		
		// call the method with parameters
		method.invoke(external, parameters);
		groovyClassLoader.close();
	}
	
	/**
	 * verify if parameter names match the ones in the method
	 * @param external
	 * @param methodName
	 * @param parameterList
	 */
	public static boolean isParameterNamesMatch(String[] parameterNames, List<KeyValue> parameterList) {

				for(KeyValue keyVal : parameterList) {
					if( !Arrays.stream(parameterNames).anyMatch(keyVal.key::equals)) {
						return false;
					}
				}
		return true;
	}
	
	/**
	 * gets the list of parameter types for a method in an external class
	 * 
	 * @param external
	 * @param methodName
	 * @return
	 */
	public static Class<?>[] getMethodParameterTypes(Class<?> external, String methodName, List<KeyValue> parameterList) {
		Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));
		List<String> methodList = new ArrayList<String>();
		for (Method m : external.getMethods()) {
			if (m.getName().equals(methodName)) {
				String[] parameterNames = info.lookupParameterNames(m);
				methodList.add("method: " +  m.getName() + "(" +  Arrays.toString(parameterNames) + ")");
				boolean isParameterMatch =  isParameterNamesMatch(parameterNames, parameterList);
				
				if(m.getParameterCount() == parameterList.size() && isParameterMatch ) {
					Class<?>[] params = m.getParameterTypes();
					return params;
				}
			}

		}
		if(!methodList.isEmpty()) {
			Object[] parameters = parameters(parameterList)	;
			TestLog.logPass("method: " + methodName + "(" +parameters.toString() + ") not found. methods found: " +  Arrays.toString(methodList.toArray()));
		}
		return null;
	}

	/**
	 * gets File from root directory
	 * root directory: where pom file is located
	 * if classname matches the class in method directory, then the class is returned
	 * @param dirs
	 * @return
	 */
	public static File getExternalMethodFilePath(String classmethod) {

		String path = "module" + File.separator + "services" + File.separator + "method" ;
		File folder = new File(Helper.getRootDir() + "src" + File.separator + "main");
		
		List<File> listOfFiles = new ArrayList<File>();
		listOfFiles = getAllFiles(folder, listOfFiles);

		for (File file : listOfFiles) {
			if(file.getAbsolutePath().contains(path + File.separator + classmethod))
		    return file;
		}
		
		Helper.assertFalse("directory for external test method not found: " + path);
		return null;
	}
	
	/**
	 * gets list of files in specified directory
	 * @param pathToDir
	 * @param listOfFiles
	 * @return
	 */
	private static List<File> getAllFiles(File pathToDir,List<File> listOfFiles) {
	    if (pathToDir.isDirectory()) {
	        String[] subdirs = pathToDir.list();
	        for (int i=0; i<subdirs.length; i++) {
	        	getAllFiles(new File(pathToDir, subdirs[i]), listOfFiles);
	        }
	    } else {
	    	listOfFiles.add(pathToDir);
	    }
	    return listOfFiles;
	}
	
	private static Object[] parameters(List<KeyValue> paramters) {
		List<Object> parameterList = new ArrayList<Object>();
		for(KeyValue parameter: paramters) {
			parameterList.add(parameter.value);	
		}
		Object[] paramArr = new String[parameterList.size()];
		return parameterList.toArray(paramArr);
	}
	
}
