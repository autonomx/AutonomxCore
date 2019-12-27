package core.apiCore.interfaces;


import java.io.File;
import java.lang.reflect.Method;
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
		
		File sourceFile = getFilePath(methodInfo[0], "src", "main", "java", "module", "services" , "method");
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
	public static File getFilePath(String classname, String... dirs) {
		String pathKotlin = StringUtils.EMPTY;
		String pathJava = StringUtils.EMPTY;

		String path = Helper.getRootDir();
		for(String dir: dirs) {
			path = path + File.separator + dir;
		}
		pathJava = path + File.separator + classname + ".java";
		pathKotlin = path + File.separator + classname + ".kt";

		File file = new File(pathJava);
		if(!file.exists())
			file = new File(pathKotlin);
		
		return file;
	}
	
}
