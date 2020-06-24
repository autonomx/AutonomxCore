package core.helpers;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import core.apiCore.helpers.JsonHelper;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import groovy.lang.GroovyClassLoader;

/**
 * @author ehsan matean
 *
 */
public class ExternalClassHelper {
	
	
	protected static Object runExternalClass(File source, String methodName, List<KeyValue> parameterList) throws Exception {
		
		File classRoot = getClassRootPath(source);
		
		URL url = classRoot.toURI().toURL();    
		URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
		Class<?> externalClass = classLoader.loadClass(getClassPath(source));

		Object[] parameters = getParameterValues(parameterList);
		Object[] parameterNames = getParameterNames(parameterList);	
		Object external = externalClass.newInstance();

		// get parameter types
		Class<?>[] paramTypes = getMethodParameterTypes(externalClass, methodName, parameterNames);
		
		Method method = null;
		try {
			method = externalClass.getMethod(methodName, paramTypes);
		}catch(Exception e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());
		}

		// validate parameter count
		if (parameterNames.length != paramTypes.length)
			Helper.assertFalse("number of parameters must match method parameters");

		parameters = convertObjectToMethodType(paramTypes, parameters);

		// call the method with parameters
		Object object = null;
		try {
			object = method.invoke(external, parameters);
		}catch(Exception e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());
		}
		classLoader.close();
		
		return object;
	}
	
	/**
	 * get class root path
	 * path not including package path
	 * @param source
	 * @return
	 */
	private static File getClassRootPath(File source) {
		String packageName = getPackageName(source);
		String packagePath = StringUtils.replace(packageName, ".", File.separator);
		String root = source.getParentFile().getAbsolutePath().replace(packagePath, "");
		
		return new File(root);
		
	}
	
	/**
	 * get class path
	 * eg. module.service.method.External
	 * External is class name
	 * @param source
	 * @return
	 */
	private static String getClassPath(File source) {
		
		String packageName = getPackageName(source);
		String fileName = FilenameUtils.removeExtension(source.getName());

		return packageName + "." + fileName;	
	}
	
	/**
	 * gets class package name
	 * eg. module.service.method
	 * @param source
	 * @return
	 */
	private static String getPackageName(File source) {
		List<String> packageString = Helper.getLinesInFileStartingWith("package", source);
		String packageLine = StringUtils.EMPTY;
		
		// get the first line which has package name
		if(packageString.size() >= 1)
			packageLine = packageString.get(0);
		
		String[] packageLineArray = packageLine.split(" ");
		if(packageLineArray.length == 2) {
			return packageLineArray[1].replaceFirst(";", "");
		}
		
		Helper.assertFalse("class package name not found at: " + source.getAbsolutePath());
		return StringUtils.EMPTY;
	}
	/**
	 * runs method from external class
	 * @param sourceFile
	 * @param methodName
	 * @param parameterList
	 * @return
	 * @throws Exception
	 */
	protected static Object runExternalClass2(File sourceFile, String methodName, List<KeyValue> parameterList) throws Exception {
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

		Object[] parameters = getParameterValues(parameterList);
		Object[] parameterNames = getParameterNames(parameterList);	
		Class<?> externalClass = groovyClassLoader.parseClass(sourceFile);
		Object external = externalClass.newInstance();

		// get parameter types
		Class<?>[] paramTypes = getMethodParameterTypes(externalClass, methodName, parameterNames);
		
		Method method = null;
		try {
			method = externalClass.getMethod(methodName, paramTypes);
		}catch(Exception e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());
		}

		// validate parameter count
		if (parameterNames.length != paramTypes.length)
			Helper.assertFalse("number of parameters must match method parameters");

		parameters = convertObjectToMethodType(paramTypes, parameters);

		// call the method with parameters
		Object object = null;
		try {
			object = method.invoke(external, parameters);
		}catch(Exception e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());
		}
		groovyClassLoader.close();
		
		return object;
	}

	/**
	 * casts objects to object type
	 * 
	 * @param paramTypes
	 * @param parameterValues
	 * @return
	 */
	private static Object[] convertObjectToMethodType(Class<?>[] paramTypes, Object[] parameterValues) {

		Object[] paramArr = new Object[parameterValues.length];
		for (int i = 0; i < parameterValues.length; i++) {
			
			paramArr[i] = (Object) convertToDataType(paramTypes[i], parameterValues[i]);
		}

		return paramArr;
	}
	
	/**
	 * converts data to their matching types
	 * @param type
	 * @param value
	 * @return
	 */
	private static Object convertToDataType(Class<?> type, Object value) {
		
		if(type.toString().contains("java.util.ArrayList")) {
			List<String> parameterListReconstruct = new ArrayList<String>();
			
			value = value.toString().replace("[", "").replace("]", "");
			Object[] parameterList = value.toString().split(",");
			for(Object listItem: parameterList)
				parameterListReconstruct.add(listItem.toString().trim());
			
			return parameterListReconstruct;
		}else if (type.toString().contains("java.lang.String")) {
			return value.toString();
		}else
			return JsonHelper.convertToObject(value.toString(), false);
	}

	/**
	 * verify if parameter names match the ones in the method
	 * 
	 * @param external
	 * @param methodName
	 * @param parameterList
	 */
	private static boolean isParameterNamesMatch(String[] parameterNames, Object[] parameterList) {

		String parameterNamesString = Arrays.toString(parameterNames);
		String parameterListString = Arrays.toString(parameterList);
		return parameterNamesString.equals(parameterListString);
	}

	/**
	 * gets the list of parameter types for a method in an external class
	 * 
	 * @param external
	 * @param methodName
	 * @return
	 */
	private static Class<?>[] getMethodParameterTypes(Class<?> external, String methodName, Object[] parameterList) {
		Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));
		List<String> methodList = new ArrayList<String>();

		for (Method m : external.getMethods()) {
			if (m.getName().equals(methodName)) {
				String[] parameterNames = info.lookupParameterNames(m);
				methodList.add("method: " + m.getName() + "(" + Arrays.toString(parameterNames) + ")");
				boolean isParameterMatch = isParameterNamesMatch(parameterNames, parameterList);

				if (m.getParameterCount() == parameterList.length && isParameterMatch) {
					Class<?>[] params = m.getParameterTypes();
					return params;
				}
			}

		}
		if (!methodList.isEmpty()) {
			TestLog.logPass("method: " + methodName + "(" + Arrays.toString(parameterList)
					+ ") not found. methods found: " + Arrays.toString(methodList.toArray()));
		}
		return null;
	}

	private static Object[] getParameterValues(List<KeyValue> parameters) {
		List<Object> parameterList = new ArrayList<Object>();
		for (KeyValue parameter : parameters) {
			parameterList.add(parameter.value);
		}
		Object[] paramArr = new String[parameterList.size()];
		
		for(int i = 0; i < parameterList.size(); i++) {
			paramArr[i] = parameterList.get(i);
		}
		
		return paramArr;
	}

	private static Object[] getParameterNames(List<KeyValue> paramters) {
		List<Object> parameterList = new ArrayList<Object>();
		for (KeyValue parameter : paramters) {
			parameterList.add(parameter.key);
		}
		Object[] paramArr = new String[parameterList.size()];
		return parameterList.toArray(paramArr);
	}
}