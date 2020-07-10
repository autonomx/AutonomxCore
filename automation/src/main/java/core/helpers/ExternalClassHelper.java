package core.helpers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import core.apiCore.helpers.JsonHelper;
import core.support.objects.KeyValue;

/**
 * @author ehsan matean
 *
 */
public class ExternalClassHelper {
	
	
	protected static Object runInternalClass(String sourcePath, String methodName, List<KeyValue> parameterList) throws Exception {
		
		Class<?> externalClass = ExternalClassHelper.class.getClassLoader().loadClass(sourcePath);
		Object external = externalClass.newInstance();

		// get list of parameter values
		Object[] parameters = getParameterValues(parameterList);
		
		// get list of parameter names, if set. eg. param1:value1.
		Object[] parameterNames = getParameterNames(parameterList);
		
		// get parameter types of target with format: method:methodName
		Class<?>[] paramTypes = getMethodParameterTypes(externalClass, methodName, parameters, parameterNames);
		parameters = convertObjectToMethodType(paramTypes, parameters);

		// get method 
		Method method = externalClass.getMethod(methodName, paramTypes);
	
		// call the method with parameters
		Object	object = method.invoke(external, parameters);
		
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
	 * gets the list of parameter types for a method in an external class
	 * 
	 * @param external
	 * @param methodName
	 * @return
	 */
	private static Class<?>[] getMethodParameterTypes(Class<?> external, String methodName, Object[] parameterList,
			Object[] parameterNames) {
		Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));
		List<String> matchingMethodList = new ArrayList<String>();
		List<String> methodList = new ArrayList<String>();
		boolean isParameterNamesMatch = true;

		for (Method m : external.getMethods()) {
			methodList.add(m.getName());
			if (m.getName().equals(methodName)) {
				String[] targetParameterNames = info.lookupParameterNames(m);
				matchingMethodList.add("method: " + m.getName() + "(" + Arrays.toString(targetParameterNames) + ")");

				// check if parameter names are provided in the test. if provided, validate
				if (parameterNames.length != 0)
					isParameterNamesMatch = isParameterNamesMatch(parameterNames, targetParameterNames);

				if (m.getParameterCount() == parameterList.length && isParameterNamesMatch) {
					Class<?>[] params = m.getParameterTypes();
					return params;
				}
			}
		}

		// if method name matches, but parameters are not matching
		if (!matchingMethodList.isEmpty()) {
			Helper.assertFalse("method name found, however, method with correct parameters not found: " + methodName
					+ "(" + Arrays.toString(parameterList) + ") not found. methods found: "
					+ Arrays.toString(matchingMethodList.toArray()));

			// if method name does not match
		} else {
			Helper.assertFalse("method: " + methodName + " not found at: " + external.getPackage()
					+ " . methods found: " + Arrays.toString(methodList.toArray()));
		}

		return null;
	}

	private static Object[] getParameterValues(List<KeyValue> parameters) {
		List<Object> parameterList = new ArrayList<Object>();
		
		// is the format: parameterName:value set. if one value is this format, all values should be the same format
		boolean isParameterNamesProvided = isParameterNamesProvided(parameters);
		
		// formats supported: parameterName:parameterValue, or parameterValue
		for (KeyValue parameter : parameters) {
			if(isParameterNamesProvided)
				parameterList.add(parameter.value);
			else
				parameterList.add(parameter.key);
		}
		
		Object[] paramArr = new String[parameterList.size()];
		for(int i = 0; i < parameterList.size(); i++) 
			paramArr[i] = parameterList.get(i);
		
		return paramArr;
	}
	
	protected static Object[] getParameterNames(List<KeyValue> parameters) {
		List<Object> parameterList = new ArrayList<Object>();
		
		// is the format: parameterName:value set. if one value is this format, all values should be the same format
		boolean isParameterNamesProvided = isParameterNamesProvided(parameters);
		
		// only if format is: parameterName:value
		for (KeyValue parameter : parameters) {
			if(isParameterNamesProvided)
				parameterList.add(parameter.key);
		}
		Object[] paramArr = new String[parameterList.size()];
		return parameterList.toArray(paramArr);
	}
	
	/**
	 * parameters should either be all format: parameterName:value or just value 
	 * @param paramters
	 * @return
	 */
	protected static boolean isParameterNamesProvided(List<KeyValue> paramters) {
		boolean isParameterNamesSet = false;
		
		for (KeyValue parameter : paramters) {
			if(!parameter.key.isEmpty() && !parameter.value.toString().isEmpty())
				return true;		
		}
		return isParameterNamesSet;
	}
	
	/**
	 * verify if parameter names match the ones in the method
	 * 
	 * @param external
	 * @param methodName
	 * @param parameterList
	 */
	protected static boolean isParameterNamesMatch(Object[] parameterNames, Object[] parameterList) {

		String parameterNamesString = Arrays.toString(parameterNames);
		String parameterListString = Arrays.toString(parameterList);
		return parameterNamesString.equals(parameterListString);
	}
}