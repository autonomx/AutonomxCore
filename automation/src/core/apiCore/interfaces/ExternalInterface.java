package core.apiCore.interfaces;


import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import groovy.lang.GroovyClassLoader;

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
		
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getMethod());

		for (KeyValue keyword : keywords) {	
			
			switch (keyword.key.toLowerCase()) {
			case METHOD:
				runExernalMethod(keyword.value.toString());
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
	public static void runExernalMethod(String classmethod) throws Exception {
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

		String[] methodInfo = classmethod.split("\\.");
		if(methodInfo.length < 2)
			Helper.assertFalse("wrong method format. must be class.method");
		
		File sourceFile = getExternalMethodFilePath(methodInfo[0]);
		Class<?> externalClass = groovyClassLoader.parseClass(sourceFile);
		Method method = externalClass.getMethod(methodInfo[1]);
		method.invoke(null);
		groovyClassLoader.close();
	}

	/**
	 * gets File from root directory
	 * root directory: where pom file is located
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
	
}
